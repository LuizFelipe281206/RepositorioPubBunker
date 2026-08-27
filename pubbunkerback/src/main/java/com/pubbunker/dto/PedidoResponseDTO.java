package com.pubbunker.dto;

import com.pubbunker.enums.StatusPedido;
import com.pubbunker.model.Pedido;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PedidoResponseDTO {

    private final Long id;
    private final Long clienteId;
    private final String clienteNome;
    private final List<ItemPedidoResponseDTO> itens;
    private final StatusPedido status;
    private final BigDecimal valorTotal;
    private final LocalDateTime dataPedido;

    public PedidoResponseDTO(Pedido pedido) {
        this.id = pedido.getId();
        this.clienteId = pedido.getCliente().getId();
        this.clienteNome = pedido.getCliente().getNome();

        this.itens = pedido.getItens()
                .stream()
                .map(ItemPedidoResponseDTO::new)
                .toList();

        this.status = pedido.getStatus();
        this.valorTotal = pedido.getValorTotal();
        this.dataPedido = pedido.getDataPedido();
    }
}