package com.pubbunker.repository;

import com.pubbunker.model.Comanda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComandaRepository
        extends JpaRepository<Comanda, Long> {

    List<Comanda> findByDeletedAtIsNullOrderByNumeroAsc();

    Optional<Comanda> findByIdAndDeletedAtIsNull(Long id);

    Optional<Comanda> findByCodigoAcessoAndDeletedAtIsNull(
            String codigoAcesso
    );

    boolean existsByNumero(Integer numero);
}