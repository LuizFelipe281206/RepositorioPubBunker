package com.pubbunker.dto;

import com.pubbunker.model.Adicional;
import com.pubbunker.model.AdicionalPedidoSnapshot;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class AdicionalPedidoResponseDTO {
    private final Long id;
    private final String nome;
    private final BigDecimal preco;

    public AdicionalPedidoResponseDTO(AdicionalPedidoSnapshot snapshot) {
        this.id = snapshot.getAdicionalId();
        this.nome = snapshot.getNome();
        this.preco = snapshot.getPreco();
    }

    // Pedidos anteriores não armazenavam o preço histórico do adicional.
    public AdicionalPedidoResponseDTO(Adicional adicional) {
        this.id = adicional.getId();
        this.nome = adicional.getNome();
        this.preco = null;
    }
}