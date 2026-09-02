package com.pubbunker.repository;

import com.pubbunker.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByDeletedAtIsNullOrderByDataPedidoAsc();

    Optional<Pedido> findByIdAndDeletedAtIsNull(Long id);

    List<Pedido>
    findByComanda_IdAndDataPedidoGreaterThanEqualAndDeletedAtIsNullOrderByDataPedidoAsc(
            Long comandaId,
            LocalDateTime dataAbertura
    );
}