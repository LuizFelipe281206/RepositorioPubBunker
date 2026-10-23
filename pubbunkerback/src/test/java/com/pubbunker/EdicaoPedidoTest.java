package com.pubbunker;

import com.pubbunker.dto.*;
import com.pubbunker.enums.*;
import com.pubbunker.exception.RegraNegocioException;
import com.pubbunker.model.*;
import com.pubbunker.repository.*;
import com.pubbunker.security.ControleTentativas;
import com.pubbunker.service.*;
import jakarta.validation.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EdicaoPedidoTest {
    PedidoRepository pedidos;
    ProdutoRepository produtos;
    ComandaService comandas;
    AdicionalService adicionais;
    PedidoService service;
    Pedido pedido;
    Produto produto;
    static ValidatorFactory factory;
    static Validator validator;

    @BeforeAll static void iniciarValidador() { factory=Validation.buildDefaultValidatorFactory();validator=factory.getValidator(); }
    @AfterAll static void fecharValidador() { factory.close(); }

    @BeforeEach void preparar() {
        pedidos=mock(PedidoRepository.class);produtos=mock(ProdutoRepository.class);
        comandas=mock(ComandaService.class);adicionais=mock(AdicionalService.class);
        service=new PedidoService(mock(ControleTentativas.class),pedidos,produtos,comandas,adicionais);
        var comanda=new Comanda();comanda.setId(3L);comanda.setStatus(StatusComanda.EM_USO);
        comanda.setDataAbertura(LocalDateTime.now().minusHours(1));
        produto=new Produto();produto.setId(7L);produto.setNome("Lanche atual");produto.setPreco(new BigDecimal("100.00"));produto.setAtivo(true);
        pedido=new Pedido();pedido.setId(9L);pedido.setComanda(comanda);pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setDataPedido(LocalDateTime.now());pedido.setValorTotal(new BigDecimal("10.00"));
        var item=new ItemPedido();item.setId(11L);item.setProduto(produto);item.setProdutoNome("Lanche original");
        item.setPrecoUnitario(new BigDecimal("10.00"));item.setSubtotal(new BigDecimal("10.00"));item.setQuantidade(1);
        pedido.adicionarItem(item);
        when(pedidos.buscarParaAtualizacao(9L)).thenReturn(Optional.of(pedido));
        when(pedidos.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        when(comandas.buscarPorId(3L)).thenReturn(comanda);
        when(produtos.findByIdAndDeletedAtIsNull(7L)).thenReturn(Optional.of(produto));
        when(adicionais.buscarAtivosParaPedido(any())).thenReturn(List.of());
    }
    EditarItemPedidoDTO linha(Long id, int quantidade) {
        var item=new EditarItemPedidoDTO();item.setItemId(id);item.setProdutoId(7L);item.setQuantidade(quantidade);return item;
    }
    EditarPedidoDTO dados(EditarItemPedidoDTO... itens) {
        var dto=new EditarPedidoDTO();dto.setRevisao(RevisaoPedido.calcular(pedido));dto.setItens(List.of(itens));return dto;
    }
    @Test void preservaPrecoNomeEIdentidadeAoMudarQuantidade() {
        var dto=dados(linha(11L,3));dto.setObservacao("  Sem gelo  ");
        var salvo=service.atualizar(9L,dto);
        assertEquals(11L,salvo.getItens().get(0).getId());
        assertEquals("Lanche original",salvo.getItens().get(0).getProdutoNome());
        assertEquals(new BigDecimal("30.00"),salvo.getValorTotal());assertEquals("Sem gelo",salvo.getObservacao());
        assertEquals(StatusPedido.PENDENTE,salvo.getStatus());verifyNoInteractions(produtos,adicionais);
    }
    @Test void permiteEditarEmPreparoSemReiniciarStatus() {
        pedido.setStatus(StatusPedido.EM_PREPARO);service.atualizar(9L,dados(linha(11L,2)));
        assertEquals(StatusPedido.EM_PREPARO,pedido.getStatus());
    }
    @Test void preservaConfiguracaoAntigaMesmoComProdutoInativo() {
        produto.setAtivo(false);service.atualizar(9L,dados(linha(11L,2)));
        assertEquals(new BigDecimal("20.00"),pedido.getValorTotal());
    }
    @Test void trocaItemUsandoPrecoAtual() {
        service.atualizar(9L,dados(linha(null,2)));
        assertEquals(1,pedido.getItens().size());assertNull(pedido.getItens().get(0).getId());
        assertEquals(new BigDecimal("200.00"),pedido.getValorTotal());
    }
    @Test void naoAceitaProdutoInativoEmItemNovo() {
        produto.setAtivo(false);assertThrows(RegraNegocioException.class,()->service.atualizar(9L,dados(linha(null,1))));
        assertEquals(11L,pedido.getItens().get(0).getId());verify(pedidos,never()).saveAndFlush(any());
    }
    @Test void bloqueiaPedidoConcluido() {
        pedido.setStatus(StatusPedido.CONCLUIDO);
        assertThrows(RegraNegocioException.class,()->service.atualizar(9L,dados(linha(11L,1))));
    }
    @Test void bloqueiaComandaFechada() {
        pedido.getComanda().setStatus(StatusComanda.DISPONIVEL);
        assertThrows(RegraNegocioException.class,()->service.atualizar(9L,dados(linha(11L,1))));
    }
    @Test void bloqueiaPedidoDeAtendimentoAnterior() {
        pedido.getComanda().setDataAbertura(pedido.getDataPedido().plusMinutes(1));
        assertThrows(RegraNegocioException.class,()->service.atualizar(9L,dados(linha(11L,1))));
    }
    @Test void bloqueiaRevisaoDesatualizadaSemSalvar() {
        var dto=dados(linha(11L,2));pedido.setObservacao("Alterado por outro atendente");
        var erro=assertThrows(RegraNegocioException.class,()->service.atualizar(9L,dto));
        assertEquals("PEDIDO_DESATUALIZADO",erro.getCodigo());verify(pedidos,never()).saveAndFlush(any());
    }
    @Test void rejeitaItemDeOutroPedido() {
        assertThrows(RegraNegocioException.class,()->service.atualizar(9L,dados(linha(999L,1))));
    }
    @Test void rejeitaItemRepetido() {
        assertThrows(RegraNegocioException.class,()->service.atualizar(9L,dados(linha(11L,1),linha(11L,1))));
    }
    @Test void naoPermiteTrocarProdutoUsandoIdentidadeDeItemAntigo() {
        var item=linha(11L,1);item.setProdutoId(8L);
        assertThrows(RegraNegocioException.class,()->service.atualizar(9L,dados(item)));
    }
    @Test void falhaNoTotalNaoMutaItensOriginais() {
        produto.setPreco(new BigDecimal("1000.00"));
        assertThrows(RegraNegocioException.class,()->service.atualizar(9L,dados(linha(11L,2),linha(null,11))));
        assertEquals(1,pedido.getItens().get(0).getQuantidade());assertEquals(new BigDecimal("10.00"),pedido.getValorTotal());
    }
    @Test void aceitaTotalExato10000() {
        produto.setPreco(new BigDecimal("1000.00"));service.atualizar(9L,dados(linha(null,10)));
        assertEquals(new BigDecimal("10000.00"),pedido.getValorTotal());
    }
    @Test void recusaAdicionaisRepetidosECincoAdicionais() {
        for(var ids:List.of(List.of(1L,1L),List.of(1L,2L,3L,4L,5L))) {
            var item=linha(11L,1);item.setAdicionaisIds(ids);
            assertThrows(RegraNegocioException.class,()->service.atualizar(9L,dados(item)));
        }
    }
    @Test void dtoRejeitaPedidoVazioRevisaoAusenteEObservacaoLonga() {
        var dto=new EditarPedidoDTO();dto.setItens(List.of());dto.setObservacao("a".repeat(501));
        assertEquals(3,validator.validate(dto).size());
    }
    @Test void dtoValidaOsLimitesDosItens() {
        var item=linha(11L,101);item.setAdicionaisIds(List.of(1L,2L,3L,4L,5L));
        assertEquals(2,validator.validate(dados(item)).size());
    }
    @Test void revisaoIndependeDeEscalaMonetaria() {
        var antes=RevisaoPedido.calcular(pedido);pedido.setValorTotal(new BigDecimal("10"));
        pedido.getItens().get(0).setPrecoUnitario(new BigDecimal("10"));
        pedido.getItens().get(0).setSubtotal(new BigDecimal("10"));
        assertEquals(antes,RevisaoPedido.calcular(pedido));
    }
    @Test void revisaoMudaComQuantidadeEStatus() {
        var antes=RevisaoPedido.calcular(pedido);pedido.getItens().get(0).setQuantidade(2);
        assertNotEquals(antes,RevisaoPedido.calcular(pedido));antes=RevisaoPedido.calcular(pedido);
        pedido.setStatus(StatusPedido.EM_PREPARO);assertNotEquals(antes,RevisaoPedido.calcular(pedido));
    }
}
