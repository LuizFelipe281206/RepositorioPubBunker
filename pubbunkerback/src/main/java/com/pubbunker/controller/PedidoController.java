package com.pubbunker.controller;

import com.pubbunker.dto.AtualizarStatusDTO;
import com.pubbunker.dto.CriarPedidoDTO;
import com.pubbunker.model.Pedido;
import com.pubbunker.service.PedidoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
@CrossOrigin("*")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Pedido> listarPedidos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public Pedido buscarPorId(
            @PathVariable Long id
    ) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public Pedido criarPedido(
            @RequestBody CriarPedidoDTO dto
    ) {
        return service.criar(dto);
    }

    @PatchMapping("/status/{id}")
    public Pedido atualizarStatus(
            @PathVariable Long id,
            @RequestBody AtualizarStatusDTO dto
    ) {
        return service.atualizarStatus(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deletarPedido(
            @PathVariable Long id
    ) {
        service.deletar(id);
    }
}