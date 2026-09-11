package com.pubbunker.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CriarPedidoDTO {

    @NotBlank(message = "O código da comanda deve ser informado.")
    @Size(max = 36, message = "O código da comanda é inválido.")
    private String codigoComanda;

    @Size(
            max = 500,
            message = "A observação deve possuir no máximo 500 caracteres."
    )
    private String observacao;

    @Valid
    @NotEmpty(message = "O pedido deve possuir pelo menos um item.")
    private List<ItemPedidoRequestDTO> itens;
}