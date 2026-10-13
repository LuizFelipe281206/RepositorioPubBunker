package com.pubbunker.security;

import com.pubbunker.enums.Role;
import java.io.Serializable;

public record UsuarioSessao(
        Long id, String nome, String email, Role role
) implements Serializable {
}