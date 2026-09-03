package com.pubbunker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ItemPedidoRequestDTO {

    @NotNull(
            message = "O produto deve ser informado."
    )
    private Long produtoId;

    @NotNull(
            message = "A quantidade deve ser informada."
    )
    @Min(
            value = 1,
            message = "A quantidade deve ser maior que zero."
    )
    private Integer quantidade;

    private List<Long> adicionaisIds =
            new ArrayList<>();
}