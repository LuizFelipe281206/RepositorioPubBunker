package com.pubbunker.dto;

import com.pubbunker.enums.StatusComanda;
import com.pubbunker.model.Comanda;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ComandaResponseDTO {

    private final Long id;
    private final Integer numero;
    private final String codigoAcesso;
    private final StatusComanda status;
    private final LocalDateTime dataAbertura;
    private final LocalDateTime dataFechamento;

    public ComandaResponseDTO(Comanda comanda) {
        this.id = comanda.getId();
        this.numero = comanda.getNumero();
        this.codigoAcesso = comanda.getCodigoAcesso();
        this.status = comanda.getStatus();
        this.dataAbertura = comanda.getDataAbertura();
        this.dataFechamento = comanda.getDataFechamento();
    }
}