package com.conexoessolidarias.api;

import java.net.URI;
import com.conexoessolidarias.service.R2StorageService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("prod")
public class UploadController {
    private final R2StorageService storage;
    public UploadController(R2StorageService storage) { this.storage = storage; }

    @GetMapping("/uploads/{folder}/{filename}")
    public ResponseEntity<Void> image(@PathVariable String folder, @PathVariable String filename) {
        String url = storage.publicReadUrl(folder, filename);
        if (url == null) return ResponseEntity.notFound().build();
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url))
                .cacheControl(CacheControl.noStore()).build();
    }
}
