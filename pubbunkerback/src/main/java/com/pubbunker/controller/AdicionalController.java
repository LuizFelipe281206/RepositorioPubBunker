package com.pubbunker.controller;

import com.pubbunker.dto.AdicionalResponseDTO;
import com.pubbunker.dto.SalvarAdicionalDTO;
import com.pubbunker.service.AdicionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adicionais")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdicionalController {

    private final AdicionalService service;

    @GetMapping
    public List<AdicionalResponseDTO> listarTodos() {
        return service.listarTodos()
                .stream()
                .map(AdicionalResponseDTO::new)
                .toList();
    }

    @GetMapping("/{id}")
    public AdicionalResponseDTO buscarPorId(
            @PathVariable Long id
    ) {
        return new AdicionalResponseDTO(
                service.buscarPorId(id)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdicionalResponseDTO criar(
            @Valid
            @RequestBody SalvarAdicionalDTO dto
    ) {
        return new AdicionalResponseDTO(
                service.criar(dto)
        );
    }

    @PutMapping("/{id}")
    public AdicionalResponseDTO atualizar(
            @PathVariable Long id,
            @Valid
            @RequestBody SalvarAdicionalDTO dto
    ) {
        return new AdicionalResponseDTO(
                service.atualizar(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(
            @PathVariable Long id
    ) {
        service.deletar(id);
    }
}