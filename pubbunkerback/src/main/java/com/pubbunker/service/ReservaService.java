package com.pubbunker.service;

import com.pubbunker.dto.AtualizarReservaDTO;
import com.pubbunker.dto.AtualizarStatusReservaDTO;
import com.pubbunker.dto.CriarReservaDTO;
import com.pubbunker.enums.Role;
import com.pubbunker.enums.StatusReserva;
import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.exception.RegraNegocioException;
import com.pubbunker.model.Reserva;
import com.pubbunker.model.Usuario;
import com.pubbunker.repository.ReservaRepository;
import com.pubbunker.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<Reserva> listarTodas() {
        return reservaRepository
                .findByDeletedAtIsNullOrderByDataHoraAsc();
    }

    @Transactional(readOnly = true)
    public Reserva buscarPorId(Long id) {
        return reservaRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Reserva não encontrada com id: " + id
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Reserva> listarPorStatus(
            StatusReserva status
    ) {
        return reservaRepository
                .findByStatusAndDeletedAtIsNullOrderByDataHoraAsc(
                        status
                );
    }

    @Transactional(readOnly = true)
    public List<Reserva> listarPorFuncionario(
            Long funcionarioId
    ) {
        usuarioRepository
                .findByIdAndDeletedAtIsNull(funcionarioId)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Funcionário não encontrado com id: "
                                        + funcionarioId
                        )
                );

        return reservaRepository
                .findByFuncionario_IdAndDeletedAtIsNullOrderByDataHoraAsc(
                        funcionarioId
                );
    }

    public Reserva criar(CriarReservaDTO dto) {
        Usuario funcionario = usuarioRepository
                .findByIdAndDeletedAtIsNull(
                        dto.getFuncionarioId()
                )
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Funcionário não encontrado com id: "
                                        + dto.getFuncionarioId()
                        )
                );

        if (
                funcionario.getRole() != Role.FUNCIONARIO &&
                        funcionario.getRole() != Role.ADMIN
        ) {
            throw new RegraNegocioException(
                    "Somente funcionários ou administradores podem cadastrar reservas."
            );
        }

        Reserva reserva = new Reserva();

        reserva.setFuncionario(funcionario);
        reserva.setNomeCliente(
                dto.getNomeCliente().trim()
        );
        reserva.setQuantidadePessoas(
                dto.getQuantidadePessoas()
        );
        reserva.setDataHora(dto.getDataHora());
        reserva.setStatus(StatusReserva.RESERVADA);

        return reservaRepository.save(reserva);
    }

    public Reserva atualizar(
            Long id,
            AtualizarReservaDTO dto
    ) {
        Reserva reserva = buscarPorId(id);

        validarReservaEditavel(reserva);

        reserva.setNomeCliente(
                dto.getNomeCliente().trim()
        );
        reserva.setQuantidadePessoas(
                dto.getQuantidadePessoas()
        );
        reserva.setDataHora(dto.getDataHora());

        return reservaRepository.save(reserva);
    }

    public Reserva atualizarStatus(
            Long id,
            AtualizarStatusReservaDTO dto
    ) {
        Reserva reserva = buscarPorId(id);
        StatusReserva statusAtual = reserva.getStatus();
        StatusReserva novoStatus = dto.getStatus();

        if (statusAtual == novoStatus) {
            return reserva;
        }

        if (!transicaoPermitida(statusAtual, novoStatus)) {
            throw new RegraNegocioException(
                    "Não é possível alterar o status de "
                            + statusAtual
                            + " para "
                            + novoStatus
                            + "."
            );
        }

        reserva.setStatus(novoStatus);

        return reservaRepository.save(reserva);
    }

    public Reserva cancelar(Long id) {
        Reserva reserva = buscarPorId(id);

        if (
                reserva.getStatus() == StatusReserva.CONCLUIDA ||
                        reserva.getStatus() == StatusReserva.CANCELADA
        ) {
            throw new RegraNegocioException(
                    "A reserva não pode ser cancelada no status atual."
            );
        }

        reserva.setStatus(StatusReserva.CANCELADA);

        return reservaRepository.save(reserva);
    }

    public void deletar(Long id) {
        Reserva reserva = buscarPorId(id);

        reserva.setDeletedAt(LocalDateTime.now());

        reservaRepository.save(reserva);
    }

    private void validarReservaEditavel(Reserva reserva) {
        if (
                reserva.getStatus() == StatusReserva.CONCLUIDA ||
                        reserva.getStatus() == StatusReserva.CANCELADA
        ) {
            throw new RegraNegocioException(
                    "Reservas concluídas ou canceladas não podem ser editadas."
            );
        }
    }

    private boolean transicaoPermitida(
            StatusReserva statusAtual,
            StatusReserva novoStatus
    ) {
        return switch (statusAtual) {
            case RESERVADA ->
                    novoStatus == StatusReserva.CONFIRMADA ||
                            novoStatus == StatusReserva.CANCELADA;

            case CONFIRMADA ->
                    novoStatus == StatusReserva.CONCLUIDA ||
                            novoStatus == StatusReserva.CANCELADA;

            case CONCLUIDA, CANCELADA -> false;
        };
    }
}