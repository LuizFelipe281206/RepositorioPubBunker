package com.pubbunker.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CriarReservaDTO {

    @NotNull(message = "O funcionário deve ser informado.")
    private Long funcionarioId;

    @NotBlank(message = "O nome do cliente deve ser informado.")
    @Size(
            max = 100,
            message = "O nome do cliente deve possuir no máximo 100 caracteres."
    )
    private String nomeCliente;

    @NotNull(message = "A quantidade de pessoas deve ser informada.")
    @Min(
            value = 1,
            message = "A reserva deve possuir pelo menos uma pessoa."
    )
    private Integer quantidadePessoas;

    @NotNull(message = "A data e hora devem ser informadas.")
    @Future(message = "A data e hora da reserva devem estar no futuro.")
    private LocalDateTime dataHora;
}