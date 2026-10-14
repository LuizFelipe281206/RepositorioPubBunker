package com.pubbunker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Positive;

@Getter
@Setter
public class SalvarAdicionalDTO {
    @NotBlank(
            message = "O nome do adicional deve ser informado."
    )
    @Size(
            max = 100,
            message = "O nome deve possuir no máximo 100 caracteres."
    )
    private String nome;

    @NotNull(message = "O preço do adicional deve ser informado.")
    @DecimalMin(value = "0.00", message = "O preço não pode ser negativo.")
    @Digits(
            integer = 8,
            fraction = 2,
            message = "O preço deve possuir no máximo duas casas decimais."
    )
    @DecimalMax(
            value = "100.00",
            message = "O preço do adicional deve ser no máximo R$ 100,00."
    )
    private BigDecimal preco;

    private Boolean ativo;
}