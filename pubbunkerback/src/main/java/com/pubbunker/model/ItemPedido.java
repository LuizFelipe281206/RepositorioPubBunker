package com.pubbunker.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "itens_pedido",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_item_pedido",
                        columnNames = {
                                "pedido_id",
                                "produto_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(
            name = "preco_unitario",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal precoUnitario;

    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal subtotal;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}