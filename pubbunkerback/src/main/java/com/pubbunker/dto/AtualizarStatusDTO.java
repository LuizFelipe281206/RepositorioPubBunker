package com.pubbunker.dto;

import com.pubbunker.enums.StatusPedido;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtualizarStatusDTO {

    @NotNull(message = "O status deve ser informado.")
    private StatusPedido status;
}