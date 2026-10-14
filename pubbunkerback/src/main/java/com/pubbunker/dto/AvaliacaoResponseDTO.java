package com.pubbunker.dto;

import com.pubbunker.model.Avaliacao;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AvaliacaoResponseDTO {

    private final Long id;
    private final Long usuarioId;
    private final String usuarioNome;
    private final String nomeCliente;
    private final Integer nota;
    private final String comentario;
    private final LocalDateTime dataHora;
    private final Integer comandaNumero;
    private final LocalDateTime dataAberturaAtendimento;

    public AvaliacaoResponseDTO(Avaliacao avaliacao) {
        this.id = avaliacao.getId();

        this.usuarioId = avaliacao.getUsuario() == null
                ? null
                : avaliacao.getUsuario().getId();

        this.nomeCliente = avaliacao.getNomeCliente();

        this.usuarioNome = nomeCliente != null
                ? nomeCliente
                : avaliacao.getUsuario() != null
                  ? avaliacao.getUsuario().getNome()
                  : "Anônimo";
        this.comandaNumero = avaliacao.getComanda() == null
                ? null
                : avaliacao.getComanda().getNumero();

        this.dataAberturaAtendimento =
                avaliacao.getDataAberturaAtendimento();

        this.nota = avaliacao.getNota();

        this.comentario = avaliacao.getComentario();
        this.dataHora = avaliacao.getDataHora();
    }
}