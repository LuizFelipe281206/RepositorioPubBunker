package com.pubbunker.security;

import com.pubbunker.enums.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class IdentidadeUsuario {

    public UsuarioSessao atual() {
        var authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UsuarioSessao usuario)) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Sessão inválida."
            );
        }

        return usuario;
    }

    public UsuarioSessao cliente() {
        UsuarioSessao usuario = atual();

        if (usuario.role() != Role.CLIENTE) {
            throw new AccessDeniedException("Operação não permitida.");
        }

        return usuario;
    }

    public UsuarioSessao equipe() {
        UsuarioSessao usuario = atual();

        if (usuario.role() != Role.ADMIN &&
                usuario.role() != Role.FUNCIONARIO) {
            throw new AccessDeniedException("Operação não permitida.");
        }

        return usuario;
    }

    public void exigirProprioUsuarioOuAdmin(Long usuarioId) {
        UsuarioSessao usuario = atual();

        if (usuario.role() != Role.ADMIN &&
                (usuario.role() != Role.CLIENTE ||
                        !usuario.id().equals(usuarioId))) {
            throw new AccessDeniedException("Operação não permitida.");
        }
    }
}