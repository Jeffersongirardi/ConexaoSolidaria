package com.conexoessolidarias.service;

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
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${app.upload.dir:src/main/resources/static/uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        for (String sub : new String[]{"needs", "updates", "avatars", "comprovantes"}) {
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

        String newName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            Path targetPath = Paths.get(uploadDir, subdir, newName);
            Files.copy(file.getInputStream(), targetPath);
            return "/uploads/" + subdir + "/" + newName;
        } catch (IOException e) {
            return null;
        }
    }
}
