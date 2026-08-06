package com.pubbunker.service;
import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.exception.RegraNegocioException;
import com.pubbunker.dto.AtualizarStatusDTO;
import com.pubbunker.dto.CriarPedidoDTO;
import com.pubbunker.enums.StatusPedido;
import com.pubbunker.model.Pedido;
import com.pubbunker.model.Usuario;
import com.pubbunker.model.produto;
import com.pubbunker.repository.pedidoRepository;
import com.pubbunker.repository.produtoRepository;
import com.pubbunker.repository.usuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoService {

    private final pedidoRepository pedidoRepository;
    private final produtoRepository produtoRepository;
    private final usuarioRepository usuarioRepository;

    public PedidoService(
            pedidoRepository pedidoRepository,
            produtoRepository produtoRepository,
            usuarioRepository usuarioRepository
    ) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Pedido não encontrado com id: " + id
                        )
                );
    }

    public Pedido criar(CriarPedidoDTO dto) {

        if (dto.getClienteId() == null) {
            throw new RegraNegocioException(
                    "O cliente deve ser informado."
            );
        }

        if (
                dto.getProdutosIds() == null ||
                        dto.getProdutosIds().isEmpty()
        ) {
            throw new RegraNegocioException(
                    "O pedido deve possuir pelo menos um produto."
            );
        }

        Usuario cliente = usuarioRepository
                .findById(dto.getClienteId())
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Usuário não encontrado com id: "
                                        + dto.getClienteId()
                        )
                );

        List<produto> produtos = produtoRepository
                .findAllById(dto.getProdutosIds());

        if (produtos.size() != dto.getProdutosIds().size()) {
            throw new RecursoNaoEncontradoException(
                    "Um ou mais produtos não foram encontrados."
            );
        }

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

    public Pedido atualizarStatus(
            Long id,
            AtualizarStatusDTO dto
    ) {
        Pedido pedido = buscarPorId(id);

        if (dto.getStatus() == null) {
            throw new RegraNegocioException(
                    "O novo status deve ser informado."
            );
        }

        pedido.setStatus(dto.getStatus());

        return pedidoRepository.save(pedido);
    }

    public void deletar(Long id) {
        Pedido pedido = buscarPorId(id);
        pedidoRepository.delete(pedido);
    }
}