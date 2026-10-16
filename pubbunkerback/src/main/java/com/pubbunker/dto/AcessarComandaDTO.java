package com.pubbunker.dto;

import jakarta.validation.constraints.Pattern;

public record AcessarComandaDTO(
        @Pattern(
                regexp = "[0-9]{5}",
                message = "Informe os cinco dígitos do código da comanda."
        )
        String codigoEntrada
) {
}
