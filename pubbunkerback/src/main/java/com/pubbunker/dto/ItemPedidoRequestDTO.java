package com.pubbunker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ItemPedidoRequestDTO {

    @NotNull(message = "O produto deve ser informado.")
    @Positive(message = "O identificador do produto deve ser maior que zero.")
    private Long produtoId;

    @NotNull(message = "A quantidade deve ser informada.")
    @Min(value = 1, message = "A quantidade deve ser maior que zero.")
    @Max(value = 100, message = "A quantidade deve ser no máximo 100 por item.")
    private Integer quantidade;

    @Size(max = 20, message = "Cada item deve possuir no máximo 20 adicionais.")
    private List<
            @NotNull(message = "O identificador do adicional deve ser informado.")
            @Positive(message = "O identificador do adicional deve ser maior que zero.")
                    Long
            > adicionaisIds = new ArrayList<>();
}