package com.pubbunker.dto;

import com.pubbunker.enums.StatusComanda;
import com.pubbunker.model.Comanda;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ComandaResponseDTO {

    private final Long id;
    private final Integer numero;
    private final String codigoAcesso;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String codigoEntrada;
    private final StatusComanda status;
    private final LocalDateTime dataAbertura;
    private final LocalDateTime dataFechamento;

    public ComandaResponseDTO(Comanda comanda) {
        this(comanda, false);
    }

    public ComandaResponseDTO(Comanda comanda, boolean paraEquipe) {
        this.id = comanda.getId();
        this.numero = comanda.getNumero();
        this.codigoAcesso = comanda.getCodigoAcesso();
        this.codigoEntrada = paraEquipe && comanda.getStatus() == StatusComanda.EM_USO
                ? comanda.getCodigoEntrada() : null;
        this.status = comanda.getStatus();
        this.dataAbertura = comanda.getDataAbertura();
        this.dataFechamento = comanda.getDataFechamento();
    }
}
