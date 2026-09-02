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

    private final Long comandaId;
    private final Integer numeroComanda;

    private final Long clienteId;
    private final String clienteNome;

    private final List<ItemPedidoResponseDTO> itens;
    private final StatusPedido status;
    private final BigDecimal valorTotal;
    private final String observacao;
    private final LocalDateTime dataPedido;

    public PedidoResponseDTO(Pedido pedido) {
        this.id = pedido.getId();

        if (pedido.getComanda() != null) {
            this.comandaId =
                    pedido.getComanda().getId();

            this.numeroComanda =
                    pedido.getComanda().getNumero();
        } else {
            this.comandaId = null;
            this.numeroComanda = null;
        }

        if (pedido.getCliente() != null) {
            this.clienteId =
                    pedido.getCliente().getId();

            this.clienteNome =
                    pedido.getCliente().getNome();
        } else {
            this.clienteId = null;
            this.clienteNome = null;
        }

        this.itens = pedido.getItens()
                .stream()
                .map(ItemPedidoResponseDTO::new)
                .toList();

        this.status = pedido.getStatus();
        this.valorTotal = pedido.getValorTotal();
        this.observacao = pedido.getObservacao();
        this.dataPedido = pedido.getDataPedido();
    }
}