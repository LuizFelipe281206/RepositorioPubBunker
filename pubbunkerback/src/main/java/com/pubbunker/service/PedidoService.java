package com.pubbunker.service;

import com.pubbunker.dto.AtualizarStatusDTO;
import com.pubbunker.dto.CriarPedidoDTO;
import com.pubbunker.dto.EditarPedidoDTO;
import com.pubbunker.dto.EditarItemPedidoDTO;
import com.pubbunker.enums.StatusComanda;
import java.util.ArrayList;
import java.util.Objects;
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
import com.pubbunker.security.ControleTentativas;

@Service
@RequiredArgsConstructor
@Transactional
public class PedidoService {

    private final ControleTentativas controleTentativas;
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

        controleTentativas.consumir(
                "envio-pedido:atendimento:" + comanda.getId()
                        + ":" + comanda.getDataAbertura(),
                10
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
            ItemPedido item = montarItem(itemRecebido, produtosPorId.get(itemRecebido.getProdutoId()));
            pedido.adicionarItem(item);
            valorTotal = ValoresPedido.validar(valorTotal.add(item.getSubtotal()));
        }

        pedido.setValorTotal(valorTotal);

        return pedidoRepository.save(pedido);
    }

    private ItemPedido montarItem(ItemPedidoRequestDTO recebido, Produto produto) {
        if (recebido.getAdicionaisIds() != null && new LinkedHashSet<>(recebido.getAdicionaisIds()).size() != recebido.getAdicionaisIds().size()) {
            throw new RegraNegocioException("Confira os adicionais selecionados: não repita o mesmo adicional.");
        }
        Integer quantidade = recebido.getQuantidade();

        List<Adicional> adicionais =
                adicionalService.buscarAtivosParaPedido(
                        recebido.getAdicionaisIds()
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

        BigDecimal precoUnitario = ValoresPedido.validar(
                produto.getPreco().add(valorAdicionais)
        );

        BigDecimal subtotal = ValoresPedido.validar(
                precoUnitario.multiply(BigDecimal.valueOf(quantidade))
        );

        ItemPedido item = new ItemPedido();
        item.setProduto(produto);
        item.setProdutoNome(produto.getNome());
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(precoUnitario);
        item.setSubtotal(subtotal);
        item.setAdicionais(new LinkedHashSet<>(adicionais));
        item.setAdicionaisSnapshot(adicionais.stream()
                .map(AdicionalPedidoSnapshot::new)
                .collect(Collectors.toCollection(java.util.ArrayList::new)));

        return item;
    }

    public Pedido atualizar(Long id, EditarPedidoDTO dto) {
        Pedido pedido = buscarParaAtualizacao(id);
        if (pedido.getStatus() != StatusPedido.PENDENTE && pedido.getStatus() != StatusPedido.EM_PREPARO) {
            throw new RegraNegocioException("Só é possível editar pedidos pendentes ou em preparo.");
        }
        Comanda comanda = comandaService.buscarPorId(pedido.getComanda().getId());
        if (comanda.getStatus() != StatusComanda.EM_USO || comanda.getDataAbertura() == null
                || pedido.getDataPedido().isBefore(comanda.getDataAbertura())) {
            throw new RegraNegocioException("Este pedido não pertence a uma comanda em atendimento.");
        }
        if (!Objects.equals(RevisaoPedido.calcular(pedido), dto.getRevisao())) {
            throw new RegraNegocioException(
                    "Este pedido foi alterado por outro atendente. Feche e reabra a edição para conferir os dados atuais.",
                    "PEDIDO_DESATUALIZADO");
        }

        Map<Long, ItemPedido> originais = pedido.getItens().stream()
                .collect(Collectors.toMap(ItemPedido::getId, Function.identity()));
        Set<Long> retidos = new LinkedHashSet<>();
        List<ItemPedido> preparados = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (EditarItemPedidoDTO recebido : dto.getItens()) {
            ItemPedido original = recebido.getItemId() == null ? null : originais.get(recebido.getItemId());
            if (recebido.getItemId() != null && (original == null || !retidos.add(recebido.getItemId())
                    || !original.getProduto().getId().equals(recebido.getProdutoId()))) {
                throw new RegraNegocioException("Confira os itens do pedido. Um dos itens foi repetido ou não pertence a este pedido.");
            }
            List<Long> ids = recebido.getAdicionaisIds() == null ? List.of() : recebido.getAdicionaisIds();
            Set<Long> unicos = new LinkedHashSet<>(ids);
            if (ids.size() > 4 || unicos.size() != ids.size()) {
                throw new RegraNegocioException("Escolha até 4 adicionais diferentes por item do pedido.");
            }
            Set<Long> idsOriginais = original == null ? Set.of()
                    : original.getAdicionaisSnapshot().isEmpty()
                    ? original.getAdicionais().stream().map(Adicional::getId).collect(Collectors.toSet())
                    : original.getAdicionaisSnapshot().stream().map(AdicionalPedidoSnapshot::getAdicionalId).collect(Collectors.toSet());

            ItemPedido preparado;
            if (original != null && unicos.equals(idsOriginais)) {
                // Alterar quantidade ou observação mantém os preços e nomes contratados.
                preparado = new ItemPedido();
                preparado.setProduto(original.getProduto());
                preparado.setProdutoNome(original.getProdutoNome());
                preparado.setPrecoUnitario(original.getPrecoUnitario());
                preparado.setQuantidade(recebido.getQuantidade());
                preparado.setSubtotal(ValoresPedido.validar(original.getPrecoUnitario()
                        .multiply(BigDecimal.valueOf(recebido.getQuantidade()))));
                preparado.setAdicionais(new LinkedHashSet<>(original.getAdicionais()));
                preparado.setAdicionaisSnapshot(new ArrayList<>(original.getAdicionaisSnapshot()));
            } else {
                Produto produto = produtoRepository.findByIdAndDeletedAtIsNull(recebido.getProdutoId())
                        .filter(p -> Boolean.TRUE.equals(p.getAtivo()))
                        .orElseThrow(() -> new RegraNegocioException("Este produto não está mais disponível. Remova-o ou escolha outro produto."));
                // Nova configuração usa os preços atuais do catálogo, como na criação do pedido.
                preparado = montarItem(recebido, produto);
            }
            preparado.setId(recebido.getItemId());
            preparados.add(preparado);
            total = ValoresPedido.validar(total.add(preparado.getSubtotal()));
        }

        pedido.getItens().removeIf(item -> !retidos.contains(item.getId()));
        for (ItemPedido preparado : preparados) {
            if (preparado.getId() == null) {
                pedido.adicionarItem(preparado);
            } else {
                ItemPedido destino = originais.get(preparado.getId());
                destino.setProdutoNome(preparado.getProdutoNome());
                destino.setQuantidade(preparado.getQuantidade());
                destino.setPrecoUnitario(preparado.getPrecoUnitario());
                destino.setSubtotal(preparado.getSubtotal());
                destino.getAdicionais().clear();
                destino.getAdicionais().addAll(preparado.getAdicionais());
                destino.getAdicionaisSnapshot().clear();
                destino.getAdicionaisSnapshot().addAll(preparado.getAdicionaisSnapshot());
            }
        }
        pedido.setObservacao(dto.getObservacao() == null || dto.getObservacao().isBlank() ? null : dto.getObservacao().trim());
        pedido.setValorTotal(total);
        return pedidoRepository.saveAndFlush(pedido);
    }

    public Pedido atualizarStatus(Long id, AtualizarStatusDTO dto) {
        Pedido pedido = buscarParaAtualizacao(id);

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
        Pedido pedido = buscarParaAtualizacao(id);

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

    private Pedido buscarParaAtualizacao(Long id) {
        return pedidoRepository
                .buscarParaAtualizacao(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Pedido não encontrado com id: " + id
                ));
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
