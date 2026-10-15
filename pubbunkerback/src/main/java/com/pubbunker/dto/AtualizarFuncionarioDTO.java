package com.pubbunker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record AtualizarFuncionarioDTO(
        @NotBlank(message = "Informe o nome.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @NotBlank(message = "Informe o email.")
        @Email(message = "Informe um email válido.")
        @Size(max = 254, message = "O email deve ter no máximo 254 caracteres.")
        String email
) {
    public AtualizarFuncionarioDTO {
        nome = nome == null ? null : nome.trim();
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}