package com.pubbunker.repository;

import com.pubbunker.model.Produto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByDeletedAtIsNull();

    Optional<Produto> findByIdAndDeletedAtIsNull(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id and p.deletedAt is null")
    Optional<Produto> buscarParaAtualizacao(@Param("id") Long id);

    List<Produto> findAllByIdInAndDeletedAtIsNullAndAtivoTrue(
            Collection<Long> ids
    );

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM produto p
                WHERE p.deleted_at IS NULL
                  AND nome_catalogo_chave(p.nome) = nome_catalogo_chave(:nome)
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
