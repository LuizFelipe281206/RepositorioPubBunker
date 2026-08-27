package com.pubbunker.dto;

import com.pubbunker.enums.StatusReserva;
import com.pubbunker.model.Reserva;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservaResponseDTO {

    private final Long id;
    private final Long funcionarioId;
    private final String funcionarioNome;
    private final String nomeCliente;
    private final Integer quantidadePessoas;
    private final LocalDateTime dataHora;
    private final StatusReserva status;
    private final LocalDateTime dataCriacao;

    public ReservaResponseDTO(Reserva reserva) {
        this.id = reserva.getId();

        this.funcionarioId =
                reserva.getFuncionario().getId();

        this.funcionarioNome =
                reserva.getFuncionario().getNome();

        this.nomeCliente = reserva.getNomeCliente();
        this.quantidadePessoas =
                reserva.getQuantidadePessoas();

        this.dataHora = reserva.getDataHora();
        this.status = reserva.getStatus();
        this.dataCriacao = reserva.getDataCriacao();
    }
}