package com.pubbunker.dto;

import com.pubbunker.enums.StatusPedido;
import com.pubbunker.model.Pedido;

import java.time.LocalDateTime;
import java.util.List;

public class PedidoResponseDTO {

    private Long id;
    private Long clienteId;
    private String clienteNome;
    private List<ProdutoResumoDTO> produtos;
    private StatusPedido status;
    private Double valorTotal;
    private LocalDateTime dataPedido;

    public PedidoResponseDTO(Pedido pedido) {
        this.id = pedido.getId();
        this.clienteId = pedido.getCliente().getId();
        this.clienteNome = pedido.getCliente().getNome();

        this.produtos = pedido.getProdutos()
                .stream()
                .map(ProdutoResumoDTO::new)
                .toList();

        this.status = pedido.getStatus();
        this.valorTotal = pedido.getValorTotal();
        this.dataPedido = pedido.getDataPedido();
    }

    public Long getId() {
        return id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public List<ProdutoResumoDTO> getProdutos() {
        return produtos;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public LocalDateTime getDataPedido() {
        return dataPedido;
    }
}