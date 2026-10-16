package com.pubbunker.repository;

import com.pubbunker.model.Adicional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AdicionalRepository
        extends JpaRepository<Adicional, Long> {

    List<Adicional>
    findByDeletedAtIsNullOrderByNomeAsc();

    Optional<Adicional>
    findByIdAndDeletedAtIsNull(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Adicional a where a.id = :id and a.deletedAt is null")
    Optional<Adicional> buscarParaAtualizacao(@Param("id") Long id);

    List<Adicional>
    findAllByIdInAndDeletedAtIsNullAndAtivoTrue(
            Collection<Long> ids
    );
}
