package com.pubbunker.repository;

import com.pubbunker.model.Avaliacao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepository
        extends JpaRepository<Avaliacao, Long> {

    @EntityGraph(attributePaths = "usuario")
    List<Avaliacao>
    findByDeletedAtIsNullOrderByDataHoraDesc();

    @EntityGraph(attributePaths = "usuario")
    Optional<Avaliacao>
    findByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = "usuario")
    List<Avaliacao>
    findByUsuario_IdAndDeletedAtIsNullOrderByDataHoraDesc(
            Long usuarioId
    );
}