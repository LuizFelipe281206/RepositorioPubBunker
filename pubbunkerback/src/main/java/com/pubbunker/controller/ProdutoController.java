package com.pubbunker.controller;

import com.pubbunker.dto.ProdutoResponseDTO;
import com.pubbunker.dto.SalvarProdutoDTO;
import com.pubbunker.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService service;

    @GetMapping
    public List<ProdutoResponseDTO> listarTodos() {
        return service.listarTodos()
                .stream()
                .map(ProdutoResponseDTO::new)
                .toList();
    }

    @GetMapping("/{id}")
    public ProdutoResponseDTO buscarPorId(
            @PathVariable Long id
    ) {
        return new ProdutoResponseDTO(
                service.buscarPorId(id)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResponseDTO criar(
            @Valid
            @RequestBody SalvarProdutoDTO dto
    ) {
        return new ProdutoResponseDTO(
                service.salvar(dto)
        );
    }

    @PutMapping("/{id}")
    public ProdutoResponseDTO atualizar(
            @PathVariable Long id,
            @Valid
            @RequestBody SalvarProdutoDTO dto
    ) {
        return new ProdutoResponseDTO(
                service.atualizar(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirProduto(
            @PathVariable Long id
    ) {
        service.deletar(id);
    }
}