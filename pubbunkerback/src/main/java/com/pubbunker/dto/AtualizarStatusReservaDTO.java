package com.pubbunker.dto;

import com.pubbunker.enums.StatusReserva;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtualizarStatusReservaDTO {

    @NotNull(message = "O novo status deve ser informado.")
    private StatusReserva status;
}