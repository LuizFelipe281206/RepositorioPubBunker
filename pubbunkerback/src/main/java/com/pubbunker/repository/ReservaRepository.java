package com.pubbunker.repository;

import com.pubbunker.enums.StatusReserva;
import com.pubbunker.model.Reserva;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository
        extends JpaRepository<Reserva, Long> {

    @EntityGraph(attributePaths = "funcionario")
    List<Reserva>
    findByDeletedAtIsNullOrderByDataHoraAsc();

    @EntityGraph(attributePaths = "funcionario")
    Optional<Reserva>
    findByIdAndDeletedAtIsNull(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reserva r where r.id = :id and r.deletedAt is null")
    Optional<Reserva> buscarParaAtualizacao(@Param("id") Long id);

    @EntityGraph(attributePaths = "funcionario")
    List<Reserva>
    findByStatusAndDeletedAtIsNullOrderByDataHoraAsc(
            StatusReserva status
    );

    @EntityGraph(attributePaths = "funcionario")
    List<Reserva>
    findByFuncionario_IdAndDeletedAtIsNullOrderByDataHoraAsc(
            Long funcionarioId
    );
}
