package com.pubbunker.repository;

import com.pubbunker.enums.Role;
import com.pubbunker.model.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailAndDeletedAtIsNull(String email);

    Optional<Usuario> findByIdAndDeletedAtIsNull(Long id);

    List<Usuario> findByDeletedAtIsNull();

    List<Usuario> findByRoleAndDeletedAtIsNullOrderByNomeAsc(Role role);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Usuario> findByIdAndRoleAndDeletedAtIsNull(Long id, Role role);
}