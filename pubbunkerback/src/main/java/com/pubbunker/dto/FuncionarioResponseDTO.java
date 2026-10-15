package com.pubbunker.dto;

import com.pubbunker.model.Usuario;

public record FuncionarioResponseDTO(Long id, String nome, String email) {
    public FuncionarioResponseDTO(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}