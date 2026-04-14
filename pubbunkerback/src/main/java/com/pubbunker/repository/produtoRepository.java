package com.pubbunker.repository;

import com.pubbunker.model.produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface produtoRepository extends JpaRepository<produto, Long> {
}