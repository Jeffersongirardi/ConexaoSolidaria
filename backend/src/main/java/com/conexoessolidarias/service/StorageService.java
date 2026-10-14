package com.conexoessolidarias.service;

import org.springframework.web.multipart.MultipartFile;

/** Armazenamento local em dev e bucket privado R2 em prod. */
public interface StorageService {
    /** Retorna caminho estável para persistir; null se arquivo inválido. */
    String save(MultipartFile file, String subdir);
    void delete(String url);

    /** Chamado apenas após autorizar acesso ao registro que contém o arquivo. */
    default String resolveReadUrl(String url) { return url; }
}
