package com.pubbunker.repository;

import com.pubbunker.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByDeletedAtIsNull();

    Optional<Produto> findByIdAndDeletedAtIsNull(Long id);

    List<Produto> findAllByIdInAndDeletedAtIsNullAndAtivoTrue(
            Collection<Long> ids
    );

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM produto p
                WHERE p.deleted_at IS NULL
                  AND lower(btrim(regexp_replace(
                      p.nome, '[[:space:]]+', ' ', 'g'
                  ))) = lower(btrim(regexp_replace(
                      :nome, '[[:space:]]+', ' ', 'g'
                  )))
                  AND (
                      CAST(:idIgnorado AS BIGINT) IS NULL
                      OR p.id <> CAST(:idIgnorado AS BIGINT)
                  )
            )
            """, nativeQuery = true)
    boolean existeOutroProdutoComNome(
            @Param("nome") String nome,
            @Param("idIgnorado") Long idIgnorado
    );
}