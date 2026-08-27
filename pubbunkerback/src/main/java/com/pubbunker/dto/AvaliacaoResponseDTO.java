package com.pubbunker.dto;

import com.pubbunker.model.Avaliacao;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AvaliacaoResponseDTO {

    private final Long id;
    private final Long usuarioId;
    private final String usuarioNome;
    private final Integer nota;
    private final String comentario;
    private final LocalDateTime dataHora;

    public AvaliacaoResponseDTO(Avaliacao avaliacao) {
        this.id = avaliacao.getId();
        this.usuarioId = avaliacao.getUsuario().getId();
        this.usuarioNome = avaliacao.getUsuario().getNome();
        this.nota = avaliacao.getNota();
        this.comentario = avaliacao.getComentario();
        this.dataHora = avaliacao.getDataHora();
    }
}