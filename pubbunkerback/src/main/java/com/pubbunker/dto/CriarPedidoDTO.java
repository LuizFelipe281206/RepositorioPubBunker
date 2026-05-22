package com.pubbunker.dto;
import java.util.List;

public class CriarPedidoDTO {
    private Long clienteId;
    private List<Long> produtosIds;

    public CriarPedidoDTO() {
    }
    public Long getClienteId() {
        return clienteId;
    }
    public List<Long> getProdutosIds() {
        return produtosIds;
    }
    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
    public void setProdutosIds(List<Long> produtosIds) {
        this.produtosIds = produtosIds;
    }
}