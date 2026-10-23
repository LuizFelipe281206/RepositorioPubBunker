package com.pubbunker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import com.pubbunker.service.NomeCatalogo;
import lombok.Setter;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Pattern;

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

    @NotBlank(message = "A categoria do adicional deve ser informada.")
    @Size(max = 50, message = "A categoria deve possuir no máximo 50 caracteres.")
    @Pattern(regexp = "Bebidas|Porções|Lanches", message = "Selecione Bebidas, Porções ou Lanches.")
    private String categoria;

    private Boolean ativo;
    public void setNome(String nome) { this.nome = NomeCatalogo.normalizar(nome); }
    public void setCategoria(String categoria) { this.categoria = categoria == null ? null : categoria.trim(); }
}