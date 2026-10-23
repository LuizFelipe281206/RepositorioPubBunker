package com.pubbunker.service;

import com.pubbunker.dto.SalvarAdicionalDTO;
import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.model.Adicional;
import com.pubbunker.repository.AdicionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import com.pubbunker.exception.RegraNegocioException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AdicionalService {

    private final AdicionalRepository repository;

    @Transactional(readOnly = true)
    public List<Adicional> listarTodos() {
        return repository
                .findByDeletedAtIsNullOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Adicional buscarPorId(Long id) {
        return repository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(
                        () ->
                                new RecursoNaoEncontradoException(
                                        "Adicional não encontrado com id: "
                                                + id
                                )
                );
    }

    private Adicional buscarParaAtualizacao(Long id) {
        return repository.buscarParaAtualizacao(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Adicional não encontrado com id: " + id
                ));
    }

    @Transactional(readOnly = true)
    public List<Adicional> buscarAtivosPorIds(
            Collection<Long> ids
    ) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        Set<Long> idsUnicos =
                new LinkedHashSet<>(ids);

        List<Adicional> adicionais =
                repository
                        .findAllByIdInAndDeletedAtIsNullAndAtivoTrue(
                                idsUnicos
                        );

        if (adicionais.size() != idsUnicos.size()) {
            throw new RecursoNaoEncontradoException(
                    "Um ou mais adicionais não foram encontrados ou estão inativos."
            );
        }

        return adicionais;
    }

    @Transactional(readOnly = true)
    public List<Adicional> buscarAtivosParaPedido(Collection<Long> ids) {
        if (ids != null && ids.size() > 4) {
            throw new RegraNegocioException("Selecione no máximo 4 adicionais por item do pedido.");
        }
        return buscarAtivosPorIds(ids);
    }

    public Adicional criar(
            SalvarAdicionalDTO dto
    ) {
        Adicional adicional = new Adicional();

        aplicarDados(adicional, dto);

        adicional.setAtivo(
                dto.getAtivo() == null ||
                        dto.getAtivo()
        );

        return salvarNoBanco(adicional);
    }

    public Adicional atualizar(
            Long id,
            SalvarAdicionalDTO dto
    ) {
        Adicional adicional = buscarParaAtualizacao(id);

        aplicarDados(adicional, dto);

        if (dto.getAtivo() != null) {
            adicional.setAtivo(dto.getAtivo());
        }

        return salvarNoBanco(adicional);
    }

    private void aplicarDados(Adicional adicional, SalvarAdicionalDTO dto) {
        String nome = NomeCatalogo.normalizar(dto.getNome());
        if (repository.existeOutroAdicionalComNome(nome, adicional.getId())) {
            throw new RegraNegocioException("Já existe um adicional com este nome.");
        }
        adicional.setNome(nome);
        adicional.setPreco(dto.getPreco());
        adicional.setCategoria(dto.getCategoria().trim());
    }

    private Adicional salvarNoBanco(Adicional adicional) {
        try {
            return repository.saveAndFlush(adicional);
        } catch (DataIntegrityViolationException exception) {
            if (ConflitoBanco.nomeUnico(exception, "uk_adicional_nome_nao_excluido")) {
                throw new RegraNegocioException("Já existe um adicional com este nome.");
            }
            throw exception;
        }
    }

    public void deletar(Long id) {
        Adicional adicional = buscarParaAtualizacao(id);

        if (Boolean.TRUE.equals(adicional.getAtivo())) {
            throw new RegraNegocioException(
                    "Um adicional ativo não pode ser excluído. "
                            + "Inative-o antes da exclusão."
            );
        }

        adicional.setDeletedAt(LocalDateTime.now());
        repository.save(adicional);
    }
}
