package com.pubbunker.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "itens_pedido")
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

    @Column(
            name = "produto_nome",
            nullable = false,
            length = 255
    )
    private String produtoNome;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "item_pedido_adicionais",
            joinColumns = @JoinColumn(
                    name = "item_pedido_id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "adicional_id"
            ),
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_item_pedido_adicional",
                            columnNames = {
                                    "item_pedido_id",
                                    "adicional_id"
                            }
                    )
            }
    )
    @OrderBy("nome ASC")
    private Set<Adicional> adicionais = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "item_pedido_adicionais_snapshot",
            joinColumns = @JoinColumn(name = "item_pedido_id")
    )
    @OrderColumn(name = "posicao")
    private List<AdicionalPedidoSnapshot> adicionaisSnapshot =
            new ArrayList<>();

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