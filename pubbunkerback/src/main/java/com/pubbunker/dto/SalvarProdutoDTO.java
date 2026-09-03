package com.pubbunker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SalvarProdutoDTO {

    @NotBlank(
            message = "O nome do produto deve ser informado."
    )
    @Size(
            max = 100,
            message = "O nome deve possuir no máximo 100 caracteres."
    )
    private String nome;

    @NotBlank(
            message = "A descrição do produto deve ser informada."
    )
    @Size(
            max = 500,
            message = "A descrição deve possuir no máximo 500 caracteres."
    )
    private String descricao;

    @NotNull(
            message = "O preço do produto deve ser informado."
    )
    @DecimalMin(
            value = "0.01",
            message = "O preço deve ser maior que zero."
    )
    @Digits(
            integer = 8,
            fraction = 2,
            message = "O preço deve possuir no máximo duas casas decimais."
    )
    private BigDecimal preco;

    @NotBlank(
            message = "A categoria deve ser informada."
    )
    @Size(
            max = 50,
            message = "A categoria deve possuir no máximo 50 caracteres."
    )
    private String categoria;

    private Boolean ativo;

    private List<Long> adicionaisIds =
            new ArrayList<>();
}