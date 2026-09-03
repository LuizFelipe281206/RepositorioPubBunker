package com.pubbunker.repository;

import com.pubbunker.model.Adicional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AdicionalRepository
        extends JpaRepository<Adicional, Long> {

    List<Adicional>
    findByDeletedAtIsNullOrderByNomeAsc();

    Optional<Adicional>
    findByIdAndDeletedAtIsNull(Long id);

    List<Adicional>
    findAllByIdInAndDeletedAtIsNullAndAtivoTrue(
            Collection<Long> ids
    );
}