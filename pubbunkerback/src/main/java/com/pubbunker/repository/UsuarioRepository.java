package com.pubbunker.repository;

import com.pubbunker.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailAndDeletedAtIsNull(String email);

    Optional<Usuario> findByIdAndDeletedAtIsNull(Long id);

    List<Usuario> findByDeletedAtIsNull();
}