package com.pubbunker.service;

import com.pubbunker.dto.AtualizarAvaliacaoDTO;
import com.pubbunker.dto.CriarAvaliacaoDTO;
import com.pubbunker.enums.Role;
import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.exception.RegraNegocioException;
import com.pubbunker.model.Avaliacao;
import com.pubbunker.model.Comanda;
import com.pubbunker.model.Usuario;
import com.pubbunker.repository.AvaliacaoRepository;
import com.pubbunker.repository.UsuarioRepository;
import com.pubbunker.security.ControleTentativas;
import com.pubbunker.security.IdentidadeUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AvaliacaoService {

    private final IdentidadeUsuario identidadeUsuario;
    private final AvaliacaoRepository avaliacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComandaService comandaService;
    private final ControleTentativas controleTentativas;

    @Transactional(readOnly = true)
    public List<Avaliacao> listarTodas() {
        exigirAdmin();

        return avaliacaoRepository
                .findByDeletedAtIsNullAndArquivadoEmIsNullOrderByDataHoraDesc();
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> listarArquivadas() {
        exigirAdmin();

        return avaliacaoRepository
                .findByDeletedAtIsNullAndArquivadoEmIsNotNullOrderByDataHoraDesc();
    }

    public void arquivar(Long id) {
        exigirAdmin();

        Avaliacao avaliacao = avaliacaoRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Avaliação não encontrada."
                ));

        if (avaliacao.getArquivadoEm() == null) {
            avaliacao.setArquivadoEm(LocalDateTime.now());
            avaliacaoRepository.save(avaliacao);
        }
    }

    public void excluirArquivada(Long id) {
        exigirAdmin();

        Avaliacao avaliacao = avaliacaoRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Avaliação não encontrada."
                ));

        if (avaliacao.getArquivadoEm() == null) {
            throw new RegraNegocioException(
                    "Arquive a avaliação antes de excluí-la."
            );
        }

        avaliacao.setDeletedAt(LocalDateTime.now());
        avaliacaoRepository.save(avaliacao);
    }

    private void exigirAdmin() {
        if (identidadeUsuario.atual().role() != Role.ADMIN) {
            throw new AccessDeniedException("Operação não permitida.");
        }
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> listarPorUsuario(Long usuarioId) {
        identidadeUsuario.exigirProprioUsuarioOuAdmin(usuarioId);

        usuarioRepository
                .findByIdAndDeletedAtIsNull(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado com id: " + usuarioId
                ));

        return avaliacaoRepository
                .findByUsuario_IdAndDeletedAtIsNullOrderByDataHoraDesc(
                        usuarioId
                );
    }

    @Transactional(readOnly = true)
    public Avaliacao buscarPorId(Long id) {
        Avaliacao avaliacao = avaliacaoRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Avaliação não encontrada com id: " + id
                ));

        identidadeUsuario.exigirProprioUsuarioOuAdmin(
                avaliacao.getUsuario() == null
                        ? null
                        : avaliacao.getUsuario().getId()
        );

        return avaliacao;
    }

    public Avaliacao criar(CriarAvaliacaoDTO dto) {
        Long usuarioId = identidadeUsuario.cliente().id();

        Usuario usuario = usuarioRepository
                .findByIdAndDeletedAtIsNull(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado com id: " + usuarioId
                ));

        if (usuario.getRole() != Role.CLIENTE) {
            throw new RegraNegocioException(
                    "Somente clientes podem cadastrar avaliações."
            );
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(usuario);
        avaliacao.setNota(dto.getNota());
        avaliacao.setComentario(normalizarComentario(dto.getComentario()));

        return avaliacaoRepository.save(avaliacao);
    }

    public Avaliacao atualizar(Long id, AtualizarAvaliacaoDTO dto) {
        Avaliacao avaliacao = buscarPorId(id);
        validarAutor(avaliacao);

        avaliacao.setNota(dto.getNota());
        avaliacao.setComentario(normalizarComentario(dto.getComentario()));

        return avaliacaoRepository.save(avaliacao);
    }

    public void deletar(Long id) {
        Avaliacao avaliacao = buscarPorId(id);
        validarAutor(avaliacao);

        avaliacao.setDeletedAt(LocalDateTime.now());
        avaliacaoRepository.save(avaliacao);
    }

    public List<Avaliacao> listarPorComanda(String codigoAcesso) {
        Comanda comanda = comandaService.buscarAtivaPorCodigo(codigoAcesso);

        return avaliacaoRepository
                .findByComanda_IdAndDataAberturaAtendimentoAndDeletedAtIsNullOrderByDataHoraDesc(
                        comanda.getId(),
                        comanda.getDataAbertura()
                );
    }

    public Avaliacao criarPorComanda(
            String codigoAcesso,
            CriarAvaliacaoDTO dto
    ) {
        Comanda comanda = comandaService.buscarAtivaPorCodigo(codigoAcesso);

        controleTentativas.consumir(
                "envio-avaliacao:atendimento:" + comanda.getId()
                        + ":" + comanda.getDataAbertura(),
                10
        );

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setComanda(comanda);
        avaliacao.setDataAberturaAtendimento(comanda.getDataAbertura());
        avaliacao.setNomeCliente(normalizarNome(dto.getNomeCliente()));
        avaliacao.setNota(dto.getNota());
        avaliacao.setComentario(normalizarComentario(dto.getComentario()));

        return avaliacaoRepository.save(avaliacao);
    }

    public Avaliacao atualizarPorComanda(
            String codigoAcesso,
            Long id,
            AtualizarAvaliacaoDTO dto
    ) {
        Comanda comanda = comandaService.buscarAtivaPorCodigo(codigoAcesso);
        Avaliacao avaliacao = buscarDoAtendimento(comanda, id);

        avaliacao.setNomeCliente(normalizarNome(dto.getNomeCliente()));
        avaliacao.setNota(dto.getNota());
        avaliacao.setComentario(normalizarComentario(dto.getComentario()));

        return avaliacaoRepository.save(avaliacao);
    }

    public void deletarPorComanda(String codigoAcesso, Long id) {
        Comanda comanda = comandaService.buscarAtivaPorCodigo(codigoAcesso);
        Avaliacao avaliacao = buscarDoAtendimento(comanda, id);

        avaliacao.setDeletedAt(LocalDateTime.now());
        avaliacaoRepository.save(avaliacao);
    }

    private Avaliacao buscarDoAtendimento(Comanda comanda, Long id) {
        return avaliacaoRepository
                .findByIdAndComanda_IdAndDataAberturaAtendimentoAndDeletedAtIsNull(
                        id,
                        comanda.getId(),
                        comanda.getDataAbertura()
                )
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Avaliação não encontrada neste atendimento."
                ));
    }

    private String normalizarNome(String nome) {
        return nome == null || nome.isBlank() ? null : nome.trim();
    }

    private String normalizarComentario(String comentario) {
        return comentario == null ? "" : comentario.trim();
    }

    private void validarAutor(Avaliacao avaliacao) {
        Long usuarioId = identidadeUsuario.cliente().id();

        if (avaliacao.getUsuario() == null
                || !usuarioId.equals(avaliacao.getUsuario().getId())) {
            throw new AccessDeniedException("Operação não permitida.");
        }
    }
}