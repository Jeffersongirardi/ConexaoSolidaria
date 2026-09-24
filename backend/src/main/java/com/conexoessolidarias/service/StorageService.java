package com.conexoessolidarias.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstração de armazenamento de arquivos.
 * Implementação atual: disco local (desenvolvimento).
 * Futura: provedor externo (ex.: Cloudinary) para produção.
 */
public interface StorageService {

    /**
     * @return URL pública do arquivo salvo, ou null se inválido.
     */
    String save(MultipartFile file, String subdir);
}
