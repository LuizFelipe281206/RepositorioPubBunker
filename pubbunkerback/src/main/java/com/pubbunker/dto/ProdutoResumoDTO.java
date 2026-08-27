package com.pubbunker.dto;

import com.pubbunker.model.Produto;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProdutoResumoDTO {

    private final Long id;
    private final String nome;
    private final BigDecimal preco;
    private final String categoria;

    public ProdutoResumoDTO(Produto produto) {
        this.id = produto.getId();
        this.nome = produto.getNome();
        this.preco = produto.getPreco();
        this.categoria = produto.getCategoria();
    }
}