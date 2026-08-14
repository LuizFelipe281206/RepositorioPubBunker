package com.pubbunker.service;
import com.pubbunker.model.Produto;
import com.pubbunker.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import com.pubbunker.exception.RecursoNaoEncontradoException;
import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public List<Produto> listarTodos() {
        return repository.findAll();
    }

    public Produto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Produto não encontrado com id: " + id
                        )
                );
    }

    public Produto salvar(Produto produto) {
        if (produto.getAtivo() == null) {
            produto.setAtivo(true);
        }
        return repository.save(produto);
    }

    public Produto atualizar(Long id, Produto produtoAtualizado) {
        Produto produtoExistente = buscarPorId(id);

        produtoExistente.setNome(produtoAtualizado.getNome());
        produtoExistente.setDescricao(produtoAtualizado.getDescricao());
        produtoExistente.setPreco(produtoAtualizado.getPreco());
        produtoExistente.setCategoria(produtoAtualizado.getCategoria());
        produtoExistente.setAtivo(produtoAtualizado.getAtivo());

        return repository.save(produtoExistente);
    }

    public void deletar(Long id) {
        Produto produto = buscarPorId(id);
        produto.setAtivo(false);
        repository.save(produto);
    }
}