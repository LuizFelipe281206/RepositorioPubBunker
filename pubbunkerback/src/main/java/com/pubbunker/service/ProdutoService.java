package com.pubbunker.service;

import com.pubbunker.dto.SalvarProdutoDTO;
import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.exception.RegraNegocioException;
import com.pubbunker.model.Adicional;
import com.pubbunker.model.Produto;
import com.pubbunker.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ProdutoService {

    private static final String INDICE_NOME =
            "uk_produto_nome_nao_excluido";

    private final ProdutoRepository repository;
    private final AdicionalService adicionalService;

    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return repository.findByDeletedAtIsNull();
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        return repository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Produto não encontrado com id: " + id
                        )
                );
    }

    private Produto buscarParaAtualizacao(Long id) {
        return repository.buscarParaAtualizacao(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto não encontrado com id: " + id
                ));
    }

    public Produto salvar(SalvarProdutoDTO dto) {
        Produto produto = new Produto();

        aplicarDados(produto, dto, true);

        return salvarNoBanco(produto);
    }

    public Produto atualizar(Long id, SalvarProdutoDTO dto) {
        Produto produto = buscarParaAtualizacao(id);

        aplicarDados(produto, dto, false);

        return salvarNoBanco(produto);
    }

    private void aplicarDados(
            Produto produto,
            SalvarProdutoDTO dto,
            boolean novoProduto
    ) {
        String nome = dto.getNome()
                .trim()
                .replaceAll("\\s+", " ");

        if (repository.existeOutroProdutoComNome(
                nome,
                produto.getId()
        )) {
            throw new RegraNegocioException(
                    "Já existe um produto com este nome."
            );
        }

        produto.setNome(nome);
        produto.setDescricao(dto.getDescricao().trim());
        produto.setPreco(dto.getPreco());
        produto.setCategoria(dto.getCategoria().trim());

        if (novoProduto) {
            produto.setAtivo(
                    dto.getAtivo() == null || dto.getAtivo()
            );
        } else if (dto.getAtivo() != null) {
            produto.setAtivo(dto.getAtivo());
        }

        List<Adicional> adicionais =
                adicionalService.buscarAtivosPorIds(
                        dto.getAdicionaisIds()
                );

        produto.setAdicionaisDisponiveis(
                new LinkedHashSet<>(adicionais)
        );
    }

    private Produto salvarNoBanco(Produto produto) {
        try {
            return repository.saveAndFlush(produto);
        } catch (DataIntegrityViolationException exception) {
            if (violouNomeUnico(exception)) {
                throw new RegraNegocioException(
                        "Já existe um produto com este nome."
                );
            }

            throw exception;
        }
    }

    private boolean violouNomeUnico(Throwable exception) {
        Set<Throwable> visitadas = Collections.newSetFromMap(
                new IdentityHashMap<>()
        );
        ArrayDeque<Throwable> pendentes = new ArrayDeque<>();
        pendentes.add(exception);

        while (!pendentes.isEmpty()) {
            Throwable causa = pendentes.removeFirst();

            if (!visitadas.add(causa)) {
                continue;
            }

            if (
                    causa instanceof ConstraintViolationException violacao
                            && "23505".equals(violacao.getSQLState())
                            && INDICE_NOME.equals(violacao.getConstraintName())
            ) {
                return true;
            }

            if (
                    causa instanceof PSQLException postgres
                            && "23505".equals(postgres.getSQLState())
                            && postgres.getServerErrorMessage() != null
                            && INDICE_NOME.equals(
                            postgres.getServerErrorMessage().getConstraint()
                    )
            ) {
                return true;
            }

            if (causa.getCause() != null) {
                pendentes.addLast(causa.getCause());
            }

            if (
                    causa instanceof SQLException sql
                            && sql.getNextException() != null
            ) {
                pendentes.addLast(sql.getNextException());
            }
        }

        return false;
    }

    public void deletar(Long id) {
        Produto produto = buscarParaAtualizacao(id);

        if (Boolean.TRUE.equals(produto.getAtivo())) {
            throw new RegraNegocioException(
                    "Um produto ativo não pode ser excluído. "
                            + "Inative-o antes da exclusão."
            );
        }

        produto.setDeletedAt(LocalDateTime.now());
        repository.save(produto);
    }
}

