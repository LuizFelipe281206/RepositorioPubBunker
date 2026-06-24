package com.pubbunker.controller;
import com.pubbunker.dto.AtualizarStatusDTO;
import com.pubbunker.dto.CriarPedidoDTO;
import com.pubbunker.enums.StatusPedido;
import com.pubbunker.model.Pedido;
import com.pubbunker.model.produto;
import com.pubbunker.model.Usuario;
import com.pubbunker.repository.pedidoRepository;
import com.pubbunker.repository.produtoRepository;
import com.pubbunker.repository.usuarioRepository;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/pedidos")
@CrossOrigin("*")
public class PedidoController {
    private final pedidoRepository pedidoRepository;
    private final produtoRepository produtoRepository;
    private final usuarioRepository usuarioRepository;
    public PedidoController(
            pedidoRepository pedidoRepository,
            produtoRepository produtoRepository,
            usuarioRepository usuarioRepository
    ) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    @PostMapping
    public Pedido criarPedido(
            @RequestBody CriarPedidoDTO dto
    ) {
        Usuario cliente =
                usuarioRepository.findById(dto.getClienteId())
                        .orElseThrow();
        List<produto> produtos =
                produtoRepository.findAllById(dto.getProdutosIds());
        double total = produtos.stream()
                .mapToDouble(produto::getPreco)
                .sum();
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setProdutos(produtos);
        pedido.setValorTotal(total);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus(StatusPedido.PENDENTE);

        return pedidoRepository.save(pedido);
    }

    @PatchMapping("/status/{id}")
    public Pedido atualizarStatus(
            @PathVariable Long id,
            @RequestBody AtualizarStatusDTO dto
    ) {
        Pedido pedido =
                pedidoRepository.findById(id)
                        .orElseThrow();
        pedido.setStatus(dto.getStatus());
        return pedidoRepository.save(pedido);
    }

    @DeleteMapping("/{id}")
    public void deletarPedido(
            @PathVariable Long id
    ) {
        pedidoRepository.deleteById(id);
    }
}