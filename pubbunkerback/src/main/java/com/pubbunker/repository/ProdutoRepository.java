package com.pubbunker.repository;

import com.pubbunker.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByDeletedAtIsNull();

    Optional<Produto> findByIdAndDeletedAtIsNull(Long id);

    List<Produto> findAllByIdInAndDeletedAtIsNullAndAtivoTrue(
            Collection<Long> ids
    );
}