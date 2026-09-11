package com.pubbunker.service;

import com.pubbunker.dto.AtualizarStatusDTO;
import com.pubbunker.dto.CriarPedidoDTO;
import com.pubbunker.dto.ItemPedidoRequestDTO;
import com.pubbunker.enums.StatusPedido;
import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.exception.RegraNegocioException;
import com.pubbunker.model.Adicional;
import com.pubbunker.model.AdicionalPedidoSnapshot;
import com.pubbunker.model.Comanda;
import com.pubbunker.model.ItemPedido;
import com.pubbunker.model.Pedido;
import com.pubbunker.model.Produto;
import com.pubbunker.repository.PedidoRepository;
import com.pubbunker.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ComandaService comandaService;
    private final AdicionalService adicionalService;

    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return pedidoRepository
                .findByDeletedAtIsNullOrderByDataPedidoAsc();
    }

    public List<Pedido> listarPorComanda(String codigoAcesso) {
        Comanda comanda =
                comandaService.buscarAtivaPorCodigo(codigoAcesso);

        return pedidoRepository
                .findByComanda_IdAndDataPedidoGreaterThanEqualOrderByDataPedidoAsc(
                        comanda.getId(),
                        comanda.getDataAbertura()
                );
    }

    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long id) {
        return pedidoRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Pedido não encontrado com id: " + id
                        )
                );
    }

    public Pedido criar(CriarPedidoDTO dto) {
        if (
                dto.getCodigoComanda() == null
                        || dto.getCodigoComanda().isBlank()
        ) {
            throw new RegraNegocioException(
                    "O código da comanda deve ser informado."
            );
        }

        if (dto.getItens() == null || dto.getItens().isEmpty()) {
            throw new RegraNegocioException(
                    "O pedido deve possuir pelo menos um item."
            );
        }

        Comanda comanda = comandaService.buscarAtivaPorCodigo(
                dto.getCodigoComanda().trim()
        );

        Set<Long> produtosIds = new LinkedHashSet<>();

        for (ItemPedidoRequestDTO item : dto.getItens()) {
            produtosIds.add(item.getProdutoId());
        }

        List<Produto> produtos = produtoRepository
                .findAllByIdInAndDeletedAtIsNullAndAtivoTrue(produtosIds);

        if (produtos.size() != produtosIds.size()) {
            throw new RecursoNaoEncontradoException(
                    "Um ou mais produtos não foram encontrados ou estão inativos."
            );
        }

        Map<Long, Produto> produtosPorId = produtos.stream()
                .collect(Collectors.toMap(
                        Produto::getId,
                        Function.identity()
                ));

        Pedido pedido = new Pedido();
        pedido.setComanda(comanda);
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setDataPedido(LocalDateTime.now());

        pedido.setObservacao(
                dto.getObservacao() == null
                        || dto.getObservacao().isBlank()
                        ? null
                        : dto.getObservacao().trim()
        );

        BigDecimal valorTotal = BigDecimal.ZERO;

        for (ItemPedidoRequestDTO itemRecebido : dto.getItens()) {
            Produto produto =
                    produtosPorId.get(itemRecebido.getProdutoId());

            Integer quantidade = itemRecebido.getQuantidade();

            List<Adicional> adicionais =
                    adicionalService.buscarAtivosPorIds(
                            itemRecebido.getAdicionaisIds()
                    );

            Set<Long> adicionaisPermitidos = produto
                    .getAdicionaisDisponiveis()
                    .stream()
                    .map(Adicional::getId)
                    .collect(Collectors.toSet());

            for (Adicional adicional : adicionais) {
                if (!adicionaisPermitidos.contains(adicional.getId())) {
                    throw new RegraNegocioException(
                            "O adicional " + adicional.getNome()
                                    + " não está disponível para o produto "
                                    + produto.getNome() + "."
                    );
                }
            }

            BigDecimal valorAdicionais = adicionais.stream()
                    .map(Adicional::getPreco)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal precoUnitario =
                    produto.getPreco().add(valorAdicionais);

            BigDecimal subtotal = precoUnitario.multiply(
                    BigDecimal.valueOf(quantidade)
            );

            ItemPedido item = new ItemPedido();
            item.setProduto(produto);
            item.setQuantidade(quantidade);
            item.setPrecoUnitario(precoUnitario);
            item.setSubtotal(subtotal);
            item.setAdicionais(new LinkedHashSet<>(adicionais));
            item.setAdicionaisSnapshot(adicionais.stream()
                    .map(AdicionalPedidoSnapshot::new)
                    .collect(Collectors.toCollection(java.util.ArrayList::new)));

            pedido.adicionarItem(item);
            valorTotal = valorTotal.add(subtotal);
        }

        pedido.setValorTotal(valorTotal);

        return pedidoRepository.save(pedido);
    }

    public Pedido atualizarStatus(Long id, AtualizarStatusDTO dto) {
        Pedido pedido = buscarPorId(id);

        if (dto.getStatus() == null) {
            throw new RegraNegocioException(
                    "O novo status deve ser informado."
            );
        }

        validarTransicaoStatus(pedido.getStatus(), dto.getStatus());

        pedido.setStatus(dto.getStatus());

        return pedidoRepository.save(pedido);
    }

    public void deletar(Long id) {
        Pedido pedido = buscarPorId(id);

        if (pedido.getStatus() != StatusPedido.CONCLUIDO) {
            throw new RegraNegocioException(
                    "O pedido só pode ser fechado quando estiver concluído."
            );
        }

        LocalDateTime dataExclusao = LocalDateTime.now();

        pedido.setDeletedAt(dataExclusao);
        pedido.getItens().forEach(
                item -> item.setDeletedAt(dataExclusao)
        );

        pedidoRepository.save(pedido);
    }

    private void validarTransicaoStatus(
            StatusPedido statusAtual,
            StatusPedido novoStatus
    ) {
        boolean transicaoPermitida =
                (statusAtual == StatusPedido.PENDENTE
                        && novoStatus == StatusPedido.EM_PREPARO)
                        || (statusAtual == StatusPedido.EM_PREPARO
                        && novoStatus == StatusPedido.CONCLUIDO);

        if (!transicaoPermitida) {
            throw new RegraNegocioException(
                    "Transição de status inválida. O pedido deve seguir a ordem "
                            + "PENDENTE, EM_PREPARO e CONCLUIDO."
            );
        }
    }
}