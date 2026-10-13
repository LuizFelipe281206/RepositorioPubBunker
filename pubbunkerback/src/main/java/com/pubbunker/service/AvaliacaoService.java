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
import com.pubbunker.security.IdentidadeUsuario;
import org.springframework.security.access.AccessDeniedException;

@Service
@RequiredArgsConstructor
@Transactional
public class AvaliacaoService {

    private final IdentidadeUsuario identidadeUsuario;
    private final AvaliacaoRepository avaliacaoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<Avaliacao> listarTodas() {
        return avaliacaoRepository
                .findByDeletedAtIsNullOrderByDataHoraDesc();
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> listarPorUsuario(Long usuarioId) {
        identidadeUsuario.exigirProprioUsuarioOuAdmin(usuarioId);
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
        Avaliacao avaliacao = avaliacaoRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Avaliação não encontrada com id: " + id
                        )
                );

        identidadeUsuario.exigirProprioUsuarioOuAdmin(
                avaliacao.getUsuario().getId()
        );

        return avaliacao;
    }

    public Avaliacao criar(CriarAvaliacaoDTO dto) {
        Long usuarioId = identidadeUsuario.cliente().id();
        Usuario usuario = usuarioRepository
                .findByIdAndDeletedAtIsNull(usuarioId)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Usuário não encontrado com id: "
                                        + usuarioId
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
        validarAutor(avaliacao);

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
        validarAutor(avaliacao);

        avaliacao.setDeletedAt(LocalDateTime.now());

        avaliacaoRepository.save(avaliacao);
    }

    private void validarAutor(Avaliacao avaliacao) {
        Long usuarioId = identidadeUsuario.cliente().id();

        if (!usuarioId.equals(avaliacao.getUsuario().getId())) {
            throw new AccessDeniedException("Operação não permitida.");
        }
    }
}