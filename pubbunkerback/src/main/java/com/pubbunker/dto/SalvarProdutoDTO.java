package com.pubbunker.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SalvarProdutoDTO {

    @DecimalMax(
            value = "1000.00",
            message = "O preço do produto deve ser no máximo R$ 1.000,00."
    )
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

    @Size(max = 10, message = "O produto deve possuir no máximo 10 adicionais.")
    private List<
            @NotNull(message = "O identificador do adicional deve ser informado.")
            @Positive(message = "O identificador do adicional deve ser maior que zero.")
                    Long
            > adicionaisIds = new ArrayList<>();
}