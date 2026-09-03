package com.pubbunker.service;

import com.pubbunker.dto.SalvarProdutoDTO;
import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.model.Adicional;
import com.pubbunker.model.Produto;
import com.pubbunker.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProdutoService {

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
                .orElseThrow(
                        () ->
                                new RecursoNaoEncontradoException(
                                        "Produto não encontrado com id: "
                                                + id
                                )
                );
    }

    public Produto salvar(
            SalvarProdutoDTO dto
    ) {
        Produto produto = new Produto();

        aplicarDados(
                produto,
                dto,
                true
        );

        return repository.save(produto);
    }

    public Produto atualizar(
            Long id,
            SalvarProdutoDTO dto
    ) {
        Produto produto = buscarPorId(id);

        aplicarDados(
                produto,
                dto,
                false
        );

        return repository.save(produto);
    }

    private void aplicarDados(
            Produto produto,
            SalvarProdutoDTO dto,
            boolean novoProduto
    ) {
        produto.setNome(dto.getNome().trim());

        produto.setDescricao(
                dto.getDescricao().trim()
        );

        produto.setPreco(dto.getPreco());

        produto.setCategoria(
                dto.getCategoria().trim()
        );

        if (novoProduto) {
            produto.setAtivo(
                    dto.getAtivo() == null ||
                            dto.getAtivo()
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

    public void deletar(Long id) {
        Produto produto = buscarPorId(id);

        produto.setAtivo(false);
        produto.setDeletedAt(
                LocalDateTime.now()
        );

        repository.save(produto);
    }
}