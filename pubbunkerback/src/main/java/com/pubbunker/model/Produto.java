package com.pubbunker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "produto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;
    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal preco;

    @Column(nullable = false, length = 50)
    private String categoria;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "produto_adicionais",
            joinColumns = @JoinColumn(
                    name = "produto_id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "adicional_id"
            )
    )
    @OrderBy("nome ASC")
    private Set<Adicional> adicionaisDisponiveis =
            new LinkedHashSet<>();

    private Boolean ativo;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}