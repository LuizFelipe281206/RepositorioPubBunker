package com.pubbunker.dto;
import com.pubbunker.enums.StatusPedido;

public class AtualizarStatusDTO {
    private StatusPedido status;

    public AtualizarStatusDTO() {
    }
    public StatusPedido getStatus() {
        return status;
    }
    public void setStatus(StatusPedido status) {
        this.status = status;
    }
}