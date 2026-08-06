package com.pubbunker.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CriarPedidoDTO {

    @NotNull(message = "O cliente deve ser informado.")
    private Long clienteId;

    @NotEmpty(message = "O pedido deve possuir pelo menos um produto.")
    private List<Long> produtosIds;

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<Long> getProdutosIds() {
        return produtosIds;
    }

    public void setProdutosIds(List<Long> produtosIds) {
        this.produtosIds = produtosIds;
    }
}