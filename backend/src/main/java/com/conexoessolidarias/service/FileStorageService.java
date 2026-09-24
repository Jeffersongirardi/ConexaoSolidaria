package com.conexoessolidarias.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");
    private static final Set<String> ALLOWED_MIMES = Set.of("image/png", "image/jpeg", "image/gif", "image/webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        for (String sub : new String[]{"campaigns", "updates", "avatars", "comprovantes"}) {
            try {
                Files.createDirectories(Paths.get(uploadDir, sub));
            } catch (IOException ignored) {}
        }
    }

    public String save(MultipartFile file, String subdir) {
        if (file == null || file.isEmpty()) return null;

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) return null;

        String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) return null;
        if (file.getSize() > MAX_SIZE) return null;

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIMES.contains(contentType.toLowerCase())) {
            return null;
        }
        if (subdir.contains("..") || subdir.contains("/") || subdir.contains("\\")) return null;

        String newName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            Path targetPath = Paths.get(uploadDir, subdir, newName).normalize();
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!targetPath.toAbsolutePath().startsWith(base)) return null;
            Files.copy(file.getInputStream(), targetPath);
            return "/uploads/" + subdir + "/" + newName;
        } catch (IOException e) {
            log.warn("Falha ao salvar arquivo: {}", e.getMessage());
            return null;
        }
    }

    public void delete(String url) {
        if (url == null || !url.startsWith("/uploads/")) return;
        try {
            String relative = url.replaceFirst("^/uploads/", "");
            Path path = Paths.get(uploadDir, relative).normalize();
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!path.toAbsolutePath().startsWith(base)) return;
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Falha ao deletar arquivo {}: {}", url, e.getMessage());
        }
    }
}
