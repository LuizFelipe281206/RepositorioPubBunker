package com.pubbunker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    private String nome;
    private String descricao;

    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal preco;

    private String categoria;
    private Boolean ativo;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}