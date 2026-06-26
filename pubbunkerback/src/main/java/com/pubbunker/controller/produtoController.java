package com.pubbunker.controller;

import com.pubbunker.model.produto;
import com.pubbunker.service.produtoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
@CrossOrigin(origins = "*")
public class produtoController {

    private final produtoService service;

    public produtoController(produtoService service) {
        this.service = service;
    }

    @GetMapping
    public List<produto> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public produto buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public produto criar(@RequestBody produto produto) {
        return service.salvar(produto);
    }

    @PutMapping("/{id}")
    public produto atualizar(@PathVariable Long id, @RequestBody produto produto) {
        return service.atualizar(id, produto);
    }
    @DeleteMapping("/{id}")
    public void excluirProduto(@PathVariable Long id) {
        service.deletar(id);
    }
}