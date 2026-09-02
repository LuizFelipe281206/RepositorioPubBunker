package com.pubbunker.controller;

import com.pubbunker.dto.ComandaResponseDTO;
import com.pubbunker.dto.CriarComandaDTO;
import com.pubbunker.service.ComandaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comandas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ComandaController {

    private final ComandaService service;

    @GetMapping
    public List<ComandaResponseDTO> listarTodas() {
        return service.listarTodas()
                .stream()
                .map(ComandaResponseDTO::new)
                .toList();
    }

    @GetMapping("/{id}")
    public ComandaResponseDTO buscarPorId(
            @PathVariable Long id
    ) {
        return new ComandaResponseDTO(
                service.buscarPorId(id)
        );
    }

    @GetMapping("/acesso/{codigoAcesso}")
    public ComandaResponseDTO acessar(
            @PathVariable String codigoAcesso
    ) {
        return new ComandaResponseDTO(
                service.buscarAtivaPorCodigo(
                        codigoAcesso
                )
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComandaResponseDTO criar(
            @Valid @RequestBody CriarComandaDTO dto
    ) {
        return new ComandaResponseDTO(
                service.criar(dto)
        );
    }

    @PatchMapping("/{id}/abrir")
    public ComandaResponseDTO abrir(
            @PathVariable Long id
    ) {
        return new ComandaResponseDTO(
                service.abrir(id)
        );
    }

    @PatchMapping("/{id}/fechar")
    public ComandaResponseDTO fechar(
            @PathVariable Long id
    ) {
        return new ComandaResponseDTO(
                service.fechar(id)
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