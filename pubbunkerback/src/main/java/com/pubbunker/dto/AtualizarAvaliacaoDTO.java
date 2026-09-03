package com.pubbunker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtualizarAvaliacaoDTO {

    @NotNull(message = "A nota deve ser informada.")
    @Min(
            value = 1,
            message = "A nota mínima é 1."
    )
    @Max(
            value = 5,
            message = "A nota máxima é 5."
    )
    private Integer nota;

    @Size(
            max = 255,
            message = "O comentário deve possuir no máximo 255 caracteres."
    )
    private String comentario;
}