package com.pubbunker.repository;
import com.pubbunker.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface pedidoRepository
        extends JpaRepository<Pedido, Long> {
}