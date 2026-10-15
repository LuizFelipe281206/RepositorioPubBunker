package com.pubbunker.controller;

import com.pubbunker.dto.AtualizarFuncionarioDTO;
import com.pubbunker.dto.CriarFuncionarioDTO;
import com.pubbunker.dto.FuncionarioResponseDTO;
import com.pubbunker.service.FuncionarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/funcionarios")
@RequiredArgsConstructor
public class FuncionarioController {
    private final FuncionarioService service;

    @GetMapping
    public List<FuncionarioResponseDTO> listar() {
        return service.listar().stream()
                .map(FuncionarioResponseDTO::new)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FuncionarioResponseDTO criar(
            @Valid @RequestBody CriarFuncionarioDTO dto
    ) {
        return new FuncionarioResponseDTO(service.criar(dto));
    }

    @PutMapping("/{id}")
    public FuncionarioResponseDTO atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarFuncionarioDTO dto
    ) {
        return new FuncionarioResponseDTO(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Long id) {
        service.desativar(id);
    }
}