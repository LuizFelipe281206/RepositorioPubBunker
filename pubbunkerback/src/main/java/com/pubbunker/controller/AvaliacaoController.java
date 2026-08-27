package com.pubbunker.controller;

import com.pubbunker.dto.AtualizarAvaliacaoDTO;
import com.pubbunker.dto.AvaliacaoResponseDTO;
import com.pubbunker.dto.CriarAvaliacaoDTO;
import com.pubbunker.service.AvaliacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avaliacoes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoService service;

    @GetMapping
    public List<AvaliacaoResponseDTO> listarTodas() {
        return service.listarTodas()
                .stream()
                .map(AvaliacaoResponseDTO::new)
                .toList();
    }

    @GetMapping("/{id}")
    public AvaliacaoResponseDTO buscarPorId(
            @PathVariable Long id
    ) {
        return new AvaliacaoResponseDTO(
                service.buscarPorId(id)
        );
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<AvaliacaoResponseDTO> listarPorUsuario(
            @PathVariable Long usuarioId
    ) {
        return service.listarPorUsuario(usuarioId)
                .stream()
                .map(AvaliacaoResponseDTO::new)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AvaliacaoResponseDTO criar(
            @Valid @RequestBody CriarAvaliacaoDTO dto
    ) {
        return new AvaliacaoResponseDTO(
                service.criar(dto)
        );
    }

    @PutMapping("/{id}")
    public AvaliacaoResponseDTO atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarAvaliacaoDTO dto
    ) {
        return new AvaliacaoResponseDTO(
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