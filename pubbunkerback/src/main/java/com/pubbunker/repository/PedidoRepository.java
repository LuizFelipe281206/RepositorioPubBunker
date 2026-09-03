package com.pubbunker.repository;

import com.pubbunker.enums.StatusPedido;
import com.pubbunker.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository
        extends JpaRepository<Pedido, Long> {

    List<Pedido>
    findByDeletedAtIsNullOrderByDataPedidoAsc();

    Optional<Pedido>
    findByIdAndDeletedAtIsNull(Long id);

    List<Pedido>
    findByComanda_IdAndDataPedidoGreaterThanEqualAndDeletedAtIsNullOrderByDataPedidoAsc(
            Long comandaId,
            LocalDateTime dataAbertura
    );

    boolean
    existsByComanda_IdAndStatusInAndDeletedAtIsNull(
            Long comandaId,
            Collection<StatusPedido> status
    );

    List<Pedido>
    findByCliente_IdAndDeletedAtIsNullOrderByDataPedidoDesc(
            Long clienteId
    );
}