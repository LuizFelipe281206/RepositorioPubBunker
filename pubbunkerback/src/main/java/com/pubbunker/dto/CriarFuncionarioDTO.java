package com.pubbunker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record CriarFuncionarioDTO(
        @NotBlank(message = "Informe o nome.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @NotBlank(message = "Informe o email.")
        @Email(message = "Informe um email válido.")
        @Size(max = 254, message = "O email deve ter no máximo 254 caracteres.")
        String email,

        @NotBlank(message = "Informe a senha.")
        @Size(min = 8, max = 128,
                message = "A senha deve ter entre 8 e 128 caracteres.")
        @Pattern(regexp = "(?s).*[0-9].*",
                message = "A senha deve conter pelo menos um número.")
        @Pattern(regexp = "(?s).*[\\p{P}\\p{S}].*",
                message = "A senha deve conter pelo menos um caractere especial.")
        String senha
) {
    public CriarFuncionarioDTO {
        nome = nome == null ? null : nome.trim();
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}