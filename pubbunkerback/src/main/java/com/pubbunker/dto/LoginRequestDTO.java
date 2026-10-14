package com.pubbunker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {

    @NotBlank(message = "O email deve ser informado.")
    @Email(message = "Informe um email válido.")
    @Size(max = 254, message = "O email deve possuir no máximo 254 caracteres.")
    private String email;

    @NotBlank(message = "A senha deve ser informada.")
    @Size(max = 128, message = "A senha deve possuir no máximo 128 caracteres.")
    private String senha;
}