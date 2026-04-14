package com.pubbunker.controller;

import com.pubbunker.model.produto;
import com.pubbunker.service.produtoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
@CrossOrigin("*") // permite Vue acessar
public class produtoController {

    private final produtoService service;

    public produtoController(produtoService service) {
        this.service = service;
    }

    @GetMapping
    public List<produto> listar() {
        return service.listarTodos();
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
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}