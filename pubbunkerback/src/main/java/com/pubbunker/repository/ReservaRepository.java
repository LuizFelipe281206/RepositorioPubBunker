package com.pubbunker.repository;

import com.pubbunker.enums.StatusReserva;
import com.pubbunker.model.Reserva;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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