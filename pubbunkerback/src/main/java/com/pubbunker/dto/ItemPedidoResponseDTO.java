package com.pubbunker.dto;

import com.pubbunker.model.ItemPedido;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ItemPedidoResponseDTO {

    private final Long id;
    private final Long produtoId;
    private final String produtoNome;
    private final Integer quantidade;
    private final BigDecimal precoUnitario;
    private final BigDecimal subtotal;

    public ItemPedidoResponseDTO(ItemPedido item) {
        this.id = item.getId();
        this.produtoId = item.getProduto().getId();
        this.produtoNome = item.getProduto().getNome();
        this.quantidade = item.getQuantidade();
        this.precoUnitario = item.getPrecoUnitario();
        this.subtotal = item.getSubtotal();
    }
}