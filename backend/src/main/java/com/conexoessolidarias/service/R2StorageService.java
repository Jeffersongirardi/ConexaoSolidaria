package com.conexoessolidarias.service;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@Profile("prod")
public class R2StorageService implements StorageService {
    private static final Logger log = LoggerFactory.getLogger(R2StorageService.class);
    private static final Set<String> FOLDERS = Set.of("campaigns", "avatars", "ofertas", "comprovantes", "updates");
    private static final Set<String> PUBLIC_FOLDERS = Set.of("campaigns", "avatars", "ofertas");
    private static final Map<String, String> TYPES = Map.of("png", "image/png", "jpg", "image/jpeg",
            "jpeg", "image/jpeg", "gif", "image/gif", "webp", "image/webp");
    private static final Pattern PATH = Pattern.compile("^/uploads/(campaigns|avatars|ofertas|comprovantes|updates)/[a-f0-9]{32}\\.(png|jpg|jpeg|gif|webp)$");
    private static final long MAX_SIZE = 5 * 1024 * 1024;
    private final S3Client client;
    private final S3Presigner presigner;
    private final String bucket;

    public R2StorageService(S3Client client, S3Presigner presigner,
                           @Value("${app.storage.r2.bucket}") String bucket) {
        if (!bucket.matches("[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]")) {
            throw new IllegalArgumentException("R2_BUCKET inválido");
        }
        this.client = client;
        this.presigner = presigner;
        this.bucket = bucket;
    }

    @Override
    public String save(MultipartFile file, String subdir) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_SIZE || !FOLDERS.contains(subdir)) return null;
        String name = file.getOriginalFilename();
        if (name == null || !name.contains(".")) return null;
        String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        String mime = TYPES.get(ext);
        if (mime == null || (file.getContentType() != null && !mime.equalsIgnoreCase(file.getContentType()))) return null;
        String key = subdir + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length > MAX_SIZE) return null;
            client.putObject(PutObjectRequest.builder().bucket(bucket).key(key)
                    .contentType(mime).cacheControl("private, no-store").build(), RequestBody.fromBytes(bytes));
            return "/uploads/" + key;
        } catch (IOException | software.amazon.awssdk.core.exception.SdkException e) {
            log.warn("Falha ao enviar imagem ao R2 ({})", e.getClass().getSimpleName());
            throw new IllegalStateException("Não foi possível salvar a imagem. Tente novamente.");
        }
    }

    @Override
    public void delete(String url) {
        if (url == null || !PATH.matcher(url).matches()) return;
        try {
            client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(url.substring(9)).build());
        } catch (software.amazon.awssdk.core.exception.SdkException e) {
            log.warn("Falha ao excluir imagem do R2 ({})", e.getClass().getSimpleName());
            throw new IllegalStateException("Não foi possível excluir a imagem. Tente novamente.");
        }
    }

    /** Apenas fotos públicas podem usar o redirecionamento anônimo /uploads. */
    public String publicReadUrl(String folder, String filename) {
        String path = "/uploads/" + folder + "/" + filename;
        if (!PUBLIC_FOLDERS.contains(folder) || !PATH.matcher(path).matches()) return null;
        return sign(path);
    }

    @Override
    public String resolveReadUrl(String url) {
        if (url == null || !url.startsWith("/uploads/")) return url;
        if (!PATH.matcher(url).matches()) return null;
        String folder = url.substring(9, url.indexOf('/', 9));
        return PUBLIC_FOLDERS.contains(folder) ? url : sign(url);
    }

    private String sign(String path) {
        return presigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(path.substring(9)).build())
                .build()).url().toExternalForm();
    }
}
