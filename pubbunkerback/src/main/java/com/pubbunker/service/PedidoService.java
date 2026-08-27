package com.pubbunker.service;

import com.pubbunker.dto.AtualizarStatusDTO;
import com.pubbunker.dto.CriarPedidoDTO;
import com.pubbunker.dto.ItemPedidoRequestDTO;
import com.pubbunker.enums.StatusPedido;
import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.exception.RegraNegocioException;
import com.pubbunker.model.ItemPedido;
import com.pubbunker.model.Pedido;
import com.pubbunker.model.Produto;
import com.pubbunker.model.Usuario;
import com.pubbunker.repository.PedidoRepository;
import com.pubbunker.repository.ProdutoRepository;
import com.pubbunker.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return pedidoRepository.findByDeletedAtIsNull();
    }

    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long id) {
        return pedidoRepository
                .findByIdAndDeletedAtIsNull(id)
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

        if (dto.getItens() == null || dto.getItens().isEmpty()) {
            throw new RegraNegocioException(
                    "O pedido deve possuir pelo menos um item."
            );
        }

        Usuario cliente = usuarioRepository
                .findByIdAndDeletedAtIsNull(dto.getClienteId())
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException(
                                "Usuário não encontrado com id: "
                                        + dto.getClienteId()
                        )
                );

        Map<Long, Integer> quantidadesPorProduto =
                dto.getItens()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        ItemPedidoRequestDTO::getProdutoId,
                                        ItemPedidoRequestDTO::getQuantidade,
                                        Integer::sum,
                                        LinkedHashMap::new
                                )
                        );

        List<Produto> produtos = produtoRepository
                .findAllByIdInAndDeletedAtIsNullAndAtivoTrue(
                        quantidadesPorProduto.keySet()
                );

        if (produtos.size() != quantidadesPorProduto.size()) {
            throw new RecursoNaoEncontradoException(
                    "Um ou mais produtos não foram encontrados ou estão inativos."
            );
        }

        Map<Long, Produto> produtosPorId =
                produtos.stream()
                        .collect(
                                Collectors.toMap(
                                        Produto::getId,
                                        Function.identity()
                                )
                        );

        Pedido pedido = new Pedido();

        pedido.setCliente(cliente);
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setDataPedido(LocalDateTime.now());

        BigDecimal valorTotal = BigDecimal.ZERO;

        for (
                Map.Entry<Long, Integer> entrada :
                quantidadesPorProduto.entrySet()
        ) {
            Produto produto = produtosPorId.get(entrada.getKey());
            Integer quantidade = entrada.getValue();

            BigDecimal precoUnitario = produto.getPreco();

            BigDecimal subtotal = precoUnitario.multiply(
                    BigDecimal.valueOf(quantidade)
            );

            ItemPedido item = new ItemPedido();

            item.setProduto(produto);
            item.setQuantidade(quantidade);
            item.setPrecoUnitario(precoUnitario);
            item.setSubtotal(subtotal);

            pedido.adicionarItem(item);

            valorTotal = valorTotal.add(subtotal);
        }

        pedido.setValorTotal(valorTotal);

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
        LocalDateTime dataExclusao = LocalDateTime.now();

        pedido.setDeletedAt(dataExclusao);

        pedido.getItens().forEach(
                item -> item.setDeletedAt(dataExclusao)
        );

        pedidoRepository.save(pedido);
    }
}