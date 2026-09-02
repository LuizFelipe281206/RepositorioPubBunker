package com.pubbunker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CriarComandaDTO {

    @NotNull(message = "O número da comanda deve ser informado.")
    @Positive(message = "O número da comanda deve ser maior que zero.")
    private Integer numero;
}