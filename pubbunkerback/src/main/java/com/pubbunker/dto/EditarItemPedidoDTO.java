package com.pubbunker.dto;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditarItemPedidoDTO extends ItemPedidoRequestDTO {
    @Positive(message = "O identificador do item deve ser maior que zero.")
    private Long itemId;
}
