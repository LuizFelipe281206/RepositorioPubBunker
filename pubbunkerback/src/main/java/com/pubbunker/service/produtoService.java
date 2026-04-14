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

    public produto salvar(produto produto) {
        return repository.save(produto);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }

    public produto atualizar(Long id, produto produto) {
        produto.setId(id);
        return repository.save(produto);
    }
}