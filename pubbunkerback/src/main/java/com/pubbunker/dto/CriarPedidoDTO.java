package com.pubbunker.dto;

import jakarta.validation.Valid;
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

    @Valid
    @NotEmpty(message = "O pedido deve possuir pelo menos um item.")
    private List<ItemPedidoRequestDTO> itens;
}