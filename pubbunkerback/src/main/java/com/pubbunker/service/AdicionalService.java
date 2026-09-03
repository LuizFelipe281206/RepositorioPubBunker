package com.pubbunker.service;

import com.pubbunker.dto.SalvarAdicionalDTO;
import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.model.Adicional;
import com.pubbunker.repository.AdicionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Adicional criar(
            SalvarAdicionalDTO dto
    ) {
        Adicional adicional = new Adicional();

        adicional.setNome(dto.getNome().trim());
        adicional.setPreco(dto.getPreco());

        adicional.setAtivo(
                dto.getAtivo() == null ||
                        dto.getAtivo()
        );

        return repository.save(adicional);
    }

    public Adicional atualizar(
            Long id,
            SalvarAdicionalDTO dto
    ) {
        Adicional adicional = buscarPorId(id);

        adicional.setNome(dto.getNome().trim());
        adicional.setPreco(dto.getPreco());

        if (dto.getAtivo() != null) {
            adicional.setAtivo(dto.getAtivo());
        }

        return repository.save(adicional);
    }

    public void deletar(Long id) {
        Adicional adicional = buscarPorId(id);

        adicional.setAtivo(false);
        adicional.setDeletedAt(
                LocalDateTime.now()
        );

        repository.save(adicional);
    }
}