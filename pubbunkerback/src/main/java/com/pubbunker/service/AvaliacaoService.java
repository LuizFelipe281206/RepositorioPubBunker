package com.pubbunker.service;

import com.pubbunker.dto.AtualizarAvaliacaoDTO;
import com.pubbunker.dto.CriarAvaliacaoDTO;
import com.pubbunker.enums.Role;
import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.exception.RegraNegocioException;
import com.pubbunker.model.Avaliacao;
import com.pubbunker.model.Usuario;
import com.pubbunker.repository.AvaliacaoRepository;
import com.pubbunker.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<Avaliacao> listarTodas() {
        return avaliacaoRepository
                .findByDeletedAtIsNullOrderByDataHoraDesc();
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> listarPorUsuario(Long usuarioId) {
        usuarioRepository
                .findByIdAndDeletedAtIsNull(usuarioId)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Usuário não encontrado com id: "
                                        + usuarioId
                        )
                );

        return avaliacaoRepository
                .findByUsuario_IdAndDeletedAtIsNullOrderByDataHoraDesc(
                        usuarioId
                );
    }

    @Transactional(readOnly = true)
    public Avaliacao buscarPorId(Long id) {
        return avaliacaoRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Avaliação não encontrada com id: " + id
                        )
                );
    }

    public Avaliacao criar(CriarAvaliacaoDTO dto) {
        Usuario usuario = usuarioRepository
                .findByIdAndDeletedAtIsNull(dto.getUsuarioId())
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Usuário não encontrado com id: "
                                        + dto.getUsuarioId()
                        )
                );

        if (usuario.getRole() != Role.CLIENTE) {
            throw new RegraNegocioException(
                    "Somente clientes podem cadastrar avaliações."
            );
        }

        Avaliacao avaliacao = new Avaliacao();

        avaliacao.setUsuario(usuario);
        avaliacao.setNota(dto.getNota());
        avaliacao.setComentario(
                dto.getComentario() == null
                        ? ""
                        : dto.getComentario().trim()
        );
        return avaliacaoRepository.save(avaliacao);
    }

    public Avaliacao atualizar(
            Long id,
            AtualizarAvaliacaoDTO dto
    ) {
        Avaliacao avaliacao = buscarPorId(id);

        avaliacao.setNota(dto.getNota());
        avaliacao.setComentario(
                dto.getComentario() == null
                        ? ""
                        : dto.getComentario().trim()
        );

        return avaliacaoRepository.save(avaliacao);
    }

    public void deletar(Long id) {
        Avaliacao avaliacao = buscarPorId(id);

        avaliacao.setDeletedAt(LocalDateTime.now());

        avaliacaoRepository.save(avaliacao);
    }
}