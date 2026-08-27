package com.pubbunker.repository;

import com.pubbunker.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByDeletedAtIsNull();

    Optional<Pedido> findByIdAndDeletedAtIsNull(Long id);
}