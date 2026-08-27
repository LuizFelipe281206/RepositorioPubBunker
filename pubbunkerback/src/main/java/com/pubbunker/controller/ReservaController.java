package com.pubbunker.controller;

import com.pubbunker.dto.AtualizarReservaDTO;
import com.pubbunker.dto.AtualizarStatusReservaDTO;
import com.pubbunker.dto.CriarReservaDTO;
import com.pubbunker.dto.ReservaResponseDTO;
import com.pubbunker.enums.StatusReserva;
import com.pubbunker.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService service;

    @GetMapping
    public List<ReservaResponseDTO> listarTodas() {
        return service.listarTodas()
                .stream()
                .map(ReservaResponseDTO::new)
                .toList();
    }

    @GetMapping("/{id}")
    public ReservaResponseDTO buscarPorId(
            @PathVariable Long id
    ) {
        return new ReservaResponseDTO(
                service.buscarPorId(id)
        );
    }

    @GetMapping("/status/{status}")
    public List<ReservaResponseDTO> listarPorStatus(
            @PathVariable StatusReserva status
    ) {
        return service.listarPorStatus(status)
                .stream()
                .map(ReservaResponseDTO::new)
                .toList();
    }

    @GetMapping("/funcionario/{funcionarioId}")
    public List<ReservaResponseDTO> listarPorFuncionario(
            @PathVariable Long funcionarioId
    ) {
        return service.listarPorFuncionario(funcionarioId)
                .stream()
                .map(ReservaResponseDTO::new)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponseDTO criar(
            @Valid @RequestBody CriarReservaDTO dto
    ) {
        return new ReservaResponseDTO(
                service.criar(dto)
        );
    }

    @PutMapping("/{id}")
    public ReservaResponseDTO atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarReservaDTO dto
    ) {
        return new ReservaResponseDTO(
                service.atualizar(id, dto)
        );
    }

    @PatchMapping("/{id}/status")
    public ReservaResponseDTO atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusReservaDTO dto
    ) {
        return new ReservaResponseDTO(
                service.atualizarStatus(id, dto)
        );
    }

    @PatchMapping("/{id}/cancelar")
    public ReservaResponseDTO cancelar(
            @PathVariable Long id
    ) {
        return new ReservaResponseDTO(
                service.cancelar(id)
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