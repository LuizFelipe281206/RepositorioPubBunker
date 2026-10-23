package com.pubbunker.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class EditarPedidoDTO {
    @NotBlank(message = "Reabra a edição para carregar a versão atual do pedido.")
    @Pattern(regexp = "[a-f0-9]{64}", message = "Reabra a edição para carregar a versão atual do pedido.")
    private String revisao;

    @Size(max = 500, message = "A observação deve possuir no máximo 500 caracteres.")
    private String observacao;

    @NotEmpty(message = "O pedido deve possuir pelo menos um item.")
    @Size(max = 50, message = "O pedido deve possuir no máximo 50 itens.")
    private List<@NotNull(message = "O pedido não pode conter um item nulo.") @Valid EditarItemPedidoDTO> itens;
}
