package com.pubbunker.controller;

import com.pubbunker.dto.AtualizarStatusDTO;
import com.pubbunker.dto.CriarPedidoDTO;
import com.pubbunker.dto.PedidoResponseDTO;
import com.pubbunker.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pedidos")
@CrossOrigin("*")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService service;

    @GetMapping
    public List<PedidoResponseDTO> listarPedidos() {
        return service.listarTodos()
                .stream()
                .map(PedidoResponseDTO::new)
                .toList();
    }

    @GetMapping("/comanda/{codigoAcesso}")
    public List<PedidoResponseDTO> listarPorComanda(
            @PathVariable String codigoAcesso
    ) {
        return service.listarPorComanda(codigoAcesso)
                .stream()
                .map(PedidoResponseDTO::new)
                .toList();
    }

    @GetMapping("/{id}")
    public PedidoResponseDTO buscarPorId(
            @PathVariable Long id
    ) {
        return new PedidoResponseDTO(
                service.buscarPorId(id)
        );
    }

    @PostMapping
    public PedidoResponseDTO criarPedido(
            @Valid @RequestBody CriarPedidoDTO dto
    ) {
        return new PedidoResponseDTO(
                service.criar(dto)
        );
    }

    @PatchMapping("/status/{id}")
    public PedidoResponseDTO atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusDTO dto
    ) {
        return new PedidoResponseDTO(
                service.atualizarStatus(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public void deletarPedido(
            @PathVariable Long id
    ) {
        service.deletar(id);
    }
}