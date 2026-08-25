package com.pubbunker.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CriarPedidoDTO {

    @NotNull(message = "O cliente deve ser informado.")
    private Long clienteId;

    @NotEmpty(message = "O pedido deve possuir pelo menos um produto.")
    private List<Long> produtosIds;
}