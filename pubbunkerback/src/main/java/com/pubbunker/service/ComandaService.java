package com.pubbunker.service;

import com.pubbunker.dto.CriarComandaDTO;
import com.pubbunker.enums.StatusComanda;
import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.exception.RegraNegocioException;
import com.pubbunker.model.Comanda;
import com.pubbunker.repository.ComandaRepository;
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

    @Transactional(readOnly = true)
    public List<Comanda> listarTodas() {
        return comandaRepository
                .findByDeletedAtIsNullOrderByNumeroAsc();
    }

    @Transactional(readOnly = true)
    public Comanda buscarPorId(Long id) {
        return comandaRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Comanda não encontrada com id: " + id
                        )
                );
    }

    @Transactional(readOnly = true)
    public Comanda buscarAtivaPorCodigo(
            String codigoAcesso
    ) {
        if (
                codigoAcesso == null ||
                        codigoAcesso.isBlank()
        ) {
            throw new RegraNegocioException(
                    "O código de acesso da comanda deve ser informado."
            );
        }

        Comanda comanda = comandaRepository
                .findByCodigoAcessoAndDeletedAtIsNull(
                        codigoAcesso.trim()
                )
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Comanda não encontrada."
                        )
                );

        if (comanda.getStatus() != StatusComanda.EM_USO) {
            throw new RegraNegocioException(
                    "Esta comanda não está liberada para uso."
            );
        }

        return comanda;
    }

    public Comanda criar(CriarComandaDTO dto) {
        if (comandaRepository.existsByNumero(dto.getNumero())) {
            throw new RegraNegocioException(
                    "Já existe uma comanda com o número "
                            + dto.getNumero()
                            + "."
            );
        }

        Comanda comanda = new Comanda();

        comanda.setNumero(dto.getNumero());
        comanda.setCodigoAcesso(
                UUID.randomUUID().toString()
        );
        comanda.setStatus(StatusComanda.DISPONIVEL);

        return comandaRepository.save(comanda);
    }

    public Comanda abrir(Long id) {
        Comanda comanda = buscarPorId(id);

        if (comanda.getStatus() == StatusComanda.EM_USO) {
            throw new RegraNegocioException(
                    "A comanda já está em uso."
            );
        }

        comanda.setStatus(StatusComanda.EM_USO);
        comanda.setDataAbertura(LocalDateTime.now());
        comanda.setDataFechamento(null);

        return comandaRepository.save(comanda);
    }

    public Comanda fechar(Long id) {
        Comanda comanda = buscarPorId(id);

        if (comanda.getStatus() != StatusComanda.EM_USO) {
            throw new RegraNegocioException(
                    "A comanda não está em uso."
            );
        }

        comanda.setStatus(StatusComanda.DISPONIVEL);
        comanda.setDataFechamento(LocalDateTime.now());

        return comandaRepository.save(comanda);
    }

    public void deletar(Long id) {
        Comanda comanda = buscarPorId(id);

        if (comanda.getStatus() == StatusComanda.EM_USO) {
            throw new RegraNegocioException(
                    "Uma comanda em uso não pode ser arquivada."
            );
        }

        comanda.setDeletedAt(LocalDateTime.now());

        comandaRepository.save(comanda);
    }
}