package com.pubbunker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor
public class AdicionalPedidoSnapshot {
    @Column(name = "adicional_id", nullable = false)
    private Long adicionalId;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    public AdicionalPedidoSnapshot(Adicional adicional) {
        this.adicionalId = adicional.getId();
        this.nome = adicional.getNome();
        this.preco = adicional.getPreco();
    }
}