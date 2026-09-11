package com.pubbunker.service;

import com.pubbunker.dto.CriarComandaDTO;
import com.pubbunker.enums.StatusComanda;
import com.pubbunker.enums.StatusPedido;
import com.pubbunker.exception.AcessoComandaInvalidoException;
import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.exception.RegraNegocioException;
import com.pubbunker.model.Comanda;
import com.pubbunker.repository.ComandaRepository;
import com.pubbunker.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ComandaService {

    private final ComandaRepository comandaRepository;
    private final PedidoRepository pedidoRepository;

    @Transactional(readOnly = true)
    public List<Comanda> listarTodas() {
        return comandaRepository
                .findByDeletedAtIsNullOrderByNumeroAsc();
    }

    @Transactional(readOnly = true)
    public Comanda buscarPorId(Long id) {
        return comandaRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Comanda não encontrada com id: " + id
                        )
                );
    }

    public Comanda buscarAtivaPorCodigo(String codigoAcesso) {
        if (codigoAcesso == null || codigoAcesso.isBlank()) {
            throw new AcessoComandaInvalidoException();
        }

        String codigo = codigoAcesso.trim();

        Comanda comanda = comandaRepository
                .findByCodigoAcessoAndDeletedAtIsNull(codigo)
                .orElseThrow(AcessoComandaInvalidoException::new);

        if (
                comanda.getStatus() != StatusComanda.EM_USO
                        || comanda.getDataAbertura() == null
                        || !codigo.equals(comanda.getCodigoAcesso())
        ) {
            throw new AcessoComandaInvalidoException();
        }

        return comanda;
    }

    private Comanda buscarParaAtualizacao(Long id) {
        return comandaRepository
                .buscarParaAtualizacao(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Comanda não encontrada."
                        )
                );
    }

    public Comanda criar(CriarComandaDTO dto) {
        if (comandaRepository.existsByNumero(dto.getNumero())) {
            throw new RegraNegocioException(
                    "Já existe uma comanda com o número "
                            + dto.getNumero() + "."
            );
        }

        Comanda comanda = new Comanda();
        comanda.setNumero(dto.getNumero());
        comanda.setCodigoAcesso(UUID.randomUUID().toString());
        comanda.setStatus(StatusComanda.DISPONIVEL);

        return comandaRepository.save(comanda);
    }

    public Comanda abrir(Long id) {
        Comanda comanda = buscarParaAtualizacao(id);

        if (comanda.getStatus() == StatusComanda.EM_USO) {
            throw new RegraNegocioException(
                    "A comanda já está em uso."
            );
        }

        comanda.setCodigoAcesso(UUID.randomUUID().toString());
        comanda.setStatus(StatusComanda.EM_USO);
        comanda.setDataAbertura(LocalDateTime.now());
        comanda.setDataFechamento(null);

        return comandaRepository.save(comanda);
    }

    public Comanda fechar(Long id) {
        Comanda comanda = buscarParaAtualizacao(id);

        if (comanda.getStatus() != StatusComanda.EM_USO) {
            throw new RegraNegocioException(
                    "A comanda não está em uso."
            );
        }

        boolean possuiPedidosEmAndamento = pedidoRepository
                .existsByComanda_IdAndStatusInAndDeletedAtIsNull(
                        comanda.getId(),
                        List.of(
                                StatusPedido.PENDENTE,
                                StatusPedido.EM_PREPARO
                        )
                );

        if (possuiPedidosEmAndamento) {
            throw new RegraNegocioException(
                    "A comanda possui pedidos em andamento."
            );
        }

        comanda.setStatus(StatusComanda.DISPONIVEL);
        comanda.setCodigoAcesso(UUID.randomUUID().toString());
        comanda.setDataFechamento(LocalDateTime.now());

        return comandaRepository.save(comanda);
    }

    public void deletar(Long id) {
        Comanda comanda = buscarParaAtualizacao(id);

        if (comanda.getStatus() == StatusComanda.EM_USO) {
            throw new RegraNegocioException(
                    "Uma comanda em uso não pode ser arquivada."
            );
        }

        comanda.setDeletedAt(LocalDateTime.now());
        comandaRepository.save(comanda);
    }
}