package com.pubbunker.repository;

import com.pubbunker.model.Avaliacao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepository
        extends JpaRepository<Avaliacao, Long> {

    @EntityGraph(attributePaths = {"usuario", "comanda"})
    List<Avaliacao> findByDeletedAtIsNullOrderByDataHoraDesc();

    @EntityGraph(attributePaths = {"usuario", "comanda"})
    Optional<Avaliacao> findByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = {"usuario", "comanda"})
    List<Avaliacao> findByUsuario_IdAndDeletedAtIsNullOrderByDataHoraDesc(
            Long usuarioId
    );

    @EntityGraph(attributePaths = {"usuario", "comanda"})
    List<Avaliacao>
    findByComanda_IdAndDataAberturaAtendimentoAndDeletedAtIsNullOrderByDataHoraDesc(
            Long comandaId,
            LocalDateTime dataAberturaAtendimento
    );

    @EntityGraph(attributePaths = {"usuario", "comanda"})
    Optional<Avaliacao>
    findByIdAndComanda_IdAndDataAberturaAtendimentoAndDeletedAtIsNull(
            Long id,
            Long comandaId,
            LocalDateTime dataAberturaAtendimento
    );

    @EntityGraph(attributePaths = {"usuario", "comanda"})
    List<Avaliacao>
    findByDeletedAtIsNullAndArquivadoEmIsNullOrderByDataHoraDesc();

    @EntityGraph(attributePaths = {"usuario", "comanda"})
    List<Avaliacao>
    findByDeletedAtIsNullAndArquivadoEmIsNotNullOrderByDataHoraDesc();
}