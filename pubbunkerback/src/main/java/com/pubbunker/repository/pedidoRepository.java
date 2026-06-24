package com.pubbunker.repository;

import com.pubbunker.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface pedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByDeletedAtIsNull();
}