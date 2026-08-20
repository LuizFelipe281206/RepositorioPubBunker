package com.pubbunker.dto;

import com.pubbunker.model.Produto;

public class ProdutoResumoDTO {

    private Long id;
    private String nome;
    private Double preco;
    private String categoria;

    public ProdutoResumoDTO(Produto produto) {
        this.id = produto.getId();
        this.nome = produto.getNome();
        this.preco = produto.getPreco();
        this.categoria = produto.getCategoria();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Double getPreco() {
        return preco;
    }

    public String getCategoria() {
        return categoria;
    }
}