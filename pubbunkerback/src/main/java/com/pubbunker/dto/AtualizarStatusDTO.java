package com.pubbunker.dto;

import com.pubbunker.enums.StatusPedido;
import jakarta.validation.constraints.NotNull;

public class AtualizarStatusDTO {

    @NotNull(message = "O status deve ser informado.")
    private StatusPedido status;

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }
}