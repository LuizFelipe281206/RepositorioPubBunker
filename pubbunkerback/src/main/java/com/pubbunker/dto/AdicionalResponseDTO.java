package com.pubbunker.dto;

import com.pubbunker.model.Adicional;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class AdicionalResponseDTO {

    private final Long id;
    private final String nome;
    private final BigDecimal preco;
    private final Boolean ativo;
    private final String categoria;

    public AdicionalResponseDTO(
            Adicional adicional
    ) {
        this.id = adicional.getId();
        this.nome = adicional.getNome();
        this.preco = adicional.getPreco();
        this.ativo = adicional.getAtivo();
        this.categoria = adicional.getCategoria();
    }
}