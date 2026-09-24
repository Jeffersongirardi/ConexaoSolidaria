package com.conexoessolidarias.api.dto;

import com.conexoessolidarias.model.User;

public record UserDTO(
        Long id,
        String nome,
        String email,
        String tipo,
        String telefone,
        String whatsapp,
        String cep,
        String cidade,
        String estado,
        String avatarUrl,
        Boolean ativo,
        Long instituicaoId,
        Boolean instituicaoAprovada) {

    public static UserDTO from(User u) {
        Long instId = u.getInstitutionProfile() != null ? u.getInstitutionProfile().getId() : null;
        Boolean aprovada = u.getInstitutionProfile() != null
                ? u.getInstitutionProfile().getAprovado() : null;
        return new UserDTO(u.getId(), u.getNome(), u.getEmail(), u.getTipo(),
                u.getTelefone(), u.getWhatsapp(), u.getCep(), u.getCidade(), u.getEstado(),
                u.getAvatarUrl(), u.getAtivo(), instId, aprovada);
    }
}
