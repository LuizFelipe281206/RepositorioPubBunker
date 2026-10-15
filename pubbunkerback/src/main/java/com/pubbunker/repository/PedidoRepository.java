package com.pubbunker.repository;

import com.pubbunker.enums.StatusPedido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.pubbunker.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByDeletedAtIsNullOrderByDataPedidoAsc();

    Optional<Pedido> findByIdAndDeletedAtIsNull(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :id and p.deletedAt is null")
    Optional<Pedido> buscarParaAtualizacao(@Param("id") Long id);

    List<Pedido>
    findByComanda_IdAndDataPedidoGreaterThanEqualOrderByDataPedidoAsc(
            Long comandaId,
            LocalDateTime dataAbertura
    );

    boolean existsByComanda_IdAndStatusInAndDeletedAtIsNull(
            Long comandaId,
            Collection<StatusPedido> status
    );
}
