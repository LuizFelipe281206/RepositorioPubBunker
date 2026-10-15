package com.pubbunker.service;

import com.pubbunker.dto.SalvarProdutoDTO;
import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.exception.RegraNegocioException;
import com.pubbunker.model.Adicional;
import com.pubbunker.model.Produto;
import com.pubbunker.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;

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

    public Produto salvar(SalvarProdutoDTO dto) {
        Produto produto = new Produto();

        aplicarDados(produto, dto, true);

        return salvarNoBanco(produto);
    }

    public Produto atualizar(Long id, SalvarProdutoDTO dto) {
        Produto produto = buscarPorId(id);

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
        for (
                Throwable causa = exception;
                causa != null;
                causa = causa.getCause()
        ) {
            if (
                    causa instanceof ConstraintViolationException violacao
                            && INDICE_NOME.equals(
                            violacao.getConstraintName()
                    )
            ) {
                return true;
            }
        }

        return false;
    }

    public void deletar(Long id) {
        Produto produto = buscarPorId(id);

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