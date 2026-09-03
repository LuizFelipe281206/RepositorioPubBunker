package com.pubbunker.dto;

import com.pubbunker.model.Produto;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class ProdutoResponseDTO {

    private final Long id;
    private final String nome;
    private final String descricao;
    private final BigDecimal preco;
    private final String categoria;
    private final Boolean ativo;

    private final List<AdicionalResponseDTO>
            adicionaisDisponiveis;

    public ProdutoResponseDTO(Produto produto) {
        this.id = produto.getId();
        this.nome = produto.getNome();
        this.descricao = produto.getDescricao();
        this.preco = produto.getPreco();
        this.categoria = produto.getCategoria();
        this.ativo = produto.getAtivo();

        this.adicionaisDisponiveis =
                produto.getAdicionaisDisponiveis()
                        .stream()
                        .map(AdicionalResponseDTO::new)
                        .toList();
    }
}