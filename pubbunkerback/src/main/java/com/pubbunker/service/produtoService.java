package com.pubbunker.service;

import com.pubbunker.model.produto;
import com.pubbunker.repository.produtoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class produtoService {

    private final produtoRepository repository;

    public produtoService(produtoRepository repository) {
        this.repository = repository;
    }

    public List<produto> listarTodos() {
        return repository.findAll();
    }

    public produto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));
    }

    public produto salvar(produto produto) {
        if (produto.getAtivo() == null) {
            produto.setAtivo(true);
        }
        return repository.save(produto);
    }

    public produto atualizar(Long id, produto produtoAtualizado) {
        produto produtoExistente = buscarPorId(id);

        produtoExistente.setNome(produtoAtualizado.getNome());
        produtoExistente.setDescricao(produtoAtualizado.getDescricao());
        produtoExistente.setPreco(produtoAtualizado.getPreco());
        produtoExistente.setCategoria(produtoAtualizado.getCategoria());
        produtoExistente.setAtivo(produtoAtualizado.getAtivo());

        return repository.save(produtoExistente);
    }

    public void deletar(Long id) {
        produto produto = buscarPorId(id);
        repository.delete(produto);
    }
}