package com.pubbunker.repository;

import com.pubbunker.model.Comanda;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComandaRepository extends JpaRepository<Comanda, Long> {

    List<Comanda> findByDeletedAtIsNullOrderByNumeroAsc();

    Optional<Comanda> findByIdAndDeletedAtIsNull(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Comanda> findByCodigoAcessoAndDeletedAtIsNull(
            String codigoAcesso
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "select c from Comanda c "
                    + "where c.id = :id and c.deletedAt is null"
    )
    Optional<Comanda> buscarParaAtualizacao(@Param("id") Long id);

    boolean existsByNumero(Integer numero);
}