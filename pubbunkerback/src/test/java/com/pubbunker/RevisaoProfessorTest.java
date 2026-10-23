package com.pubbunker;

import com.pubbunker.dto.*;
import com.pubbunker.service.*;
import com.pubbunker.exception.RegraNegocioException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RevisaoProfessorTest {
    static ValidatorFactory factory;
    static Validator validator;
    @BeforeAll static void iniciar() { factory=Validation.buildDefaultValidatorFactory();validator=factory.getValidator(); }
    @AfterAll static void fechar() { factory.close(); }
    private SalvarProdutoDTO produto() {
        var p=new SalvarProdutoDTO();p.setNome("Hambúrguer");p.setDescricao("Descrição");
        p.setCategoria("Lanches");p.setPreco(new BigDecimal("1000.00"));return p;
    }
    private SalvarAdicionalDTO adicional() {
        var a=new SalvarAdicionalDTO();a.setNome("Bacon");a.setCategoria("Lanches");a.setPreco(new BigDecimal("100"));return a;
    }
    @Test void aceitaProdutoBaseNoLimite() { assertTrue(validator.validate(produto()).isEmpty()); }
    @Test void recusaProdutoBaseAcimaDoLimite() { var p=produto();p.setPreco(new BigDecimal("1000.01"));assertFalse(validator.validate(p).isEmpty()); }
    @Test void recusaProdutoGratuito() { var p=produto();p.setPreco(BigDecimal.ZERO);assertFalse(validator.validate(p).isEmpty()); }
    @Test void aceitaQuatroAdicionaisProduto() { var p=produto();p.setAdicionaisIds(List.of(1L,2L,3L,4L));assertTrue(validator.validate(p).isEmpty()); }
    @Test void aceitaCincoAdicionaisNoCadastroDoProduto() { var p=produto();p.setAdicionaisIds(List.of(1L,2L,3L,4L,5L));assertTrue(validator.validate(p).isEmpty()); }
    @Test void aceitaMilAdicionaisNoCadastroDoProduto() { var p=produto();p.setAdicionaisIds(java.util.stream.LongStream.rangeClosed(1,1000).boxed().toList());assertTrue(validator.validate(p).isEmpty()); }
    @Test void servicoDoPedidoRejeitaQuintoAdicionalAntesDaConsulta() { var service=new AdicionalService(null);var e=assertThrows(RegraNegocioException.class,()->service.buscarAtivosParaPedido(List.of(1L,2L,3L,4L,5L)));assertTrue(e.getMessage().contains("item do pedido")); }
    @Test void rejeitaAdicionalNuloOuNegativo() { var p=produto();p.setAdicionaisIds(List.of(-1L));assertFalse(validator.validate(p).isEmpty()); }
    @Test void aceitaNome100EDescricao500() { var p=produto();p.setNome("a".repeat(100));p.setDescricao("a".repeat(500));assertTrue(validator.validate(p).isEmpty()); }
    @Test void rejeitaNome101() { var p=produto();p.setNome("a".repeat(101));assertFalse(validator.validate(p).isEmpty()); }
    @Test void rejeitaDescricao501() { var p=produto();p.setDescricao("a".repeat(501));assertFalse(validator.validate(p).isEmpty()); }
    @Test void normalizaEspacosUnicodeNome() { var p=produto();p.setNome("\u00a0Batata\u2009\u2009frita\u00a0");assertEquals("Batata frita",p.getNome()); }
    @Test void rejeitaNomeOuDescricaoSoEspacosUnicode() { var p=produto();p.setNome("\u00a0\u2009");p.setDescricao("\u00a0\u2009");assertEquals(2,validator.validate(p).size()); }
    @Test void aceitaTresCategoriasAdicionais() { for(String c:List.of("Bebidas","Porções","Lanches")){var a=adicional();a.setCategoria(c);assertTrue(validator.validate(a).isEmpty());} }
    @Test void exigeCategoriaAdicional() { var a=adicional();a.setCategoria(null);assertFalse(validator.validate(a).isEmpty()); }
    @Test void rejeitaCategoriaInexistente() { var a=adicional();a.setCategoria("Outra");assertFalse(validator.validate(a).isEmpty()); }
    @Test void aceitaAdicionalGratis() { var a=adicional();a.setPreco(BigDecimal.ZERO);assertTrue(validator.validate(a).isEmpty()); }
    @Test void rejeitaAdicionalAcima100() { var a=adicional();a.setPreco(new BigDecimal("100.01"));assertFalse(validator.validate(a).isEmpty()); }
    @Test void rejeitaPrecoComTresCasas() { var a=adicional();a.setPreco(new BigDecimal("1.005"));assertFalse(validator.validate(a).isEmpty()); }
    @Test void aceitaValorUnitarioComAdicionaisAcimaMil() { assertEquals(new BigDecimal("1100.00"),ValoresPedido.validar(new BigDecimal("1000.00").add(new BigDecimal("100")))); }
    @Test void aceitaPedido10000() { assertEquals(new BigDecimal("10000"),ValoresPedido.validar(new BigDecimal("10000"))); }
    @Test void rejeitaPedidoAcima10000ComCodigoEspecifico() { var e=assertThrows(RegraNegocioException.class,()->ValoresPedido.validar(new BigDecimal("10000.01")));assertEquals("LIMITE_PEDIDO_EXCEDIDO",e.getCodigo());assertTrue(e.getMessage().contains("R$ 10.000,00")); }
    @Test void rejeitaValorNegativoOuFracionado() { assertThrows(RegraNegocioException.class,()->ValoresPedido.validar(new BigDecimal("-1")));assertThrows(RegraNegocioException.class,()->ValoresPedido.validar(new BigDecimal("0.001"))); }
    @Test void quantidadeItemDe1a100() { var i=new ItemPedidoRequestDTO();i.setProdutoId(1L);for(int q:List.of(1,100)){i.setQuantidade(q);assertTrue(validator.validate(i).isEmpty());}for(int q:List.of(0,101)){i.setQuantidade(q);assertFalse(validator.validate(i).isEmpty());} }
    @Test void recusaCincoAdicionaisNoPedido() { var i=new ItemPedidoRequestDTO();i.setProdutoId(1L);i.setQuantidade(1);i.setAdicionaisIds(List.of(1L,2L,3L,4L,5L));assertFalse(validator.validate(i).isEmpty()); }
    @Test void funcionarioAceitaSenha8ComNumeroEEspecial() { assertTrue(validator.validate(new CriarFuncionarioDTO("Nome","nome@pubbunker.com","1234567!")).isEmpty()); }
    @Test void funcionarioRejeitaSenhaCurtaOuSemEspecial() { for(String senha:List.of("123456!","abcdefgh1","abcdefg!"))assertFalse(validator.validate(new CriarFuncionarioDTO("Nome","nome@pubbunker.com",senha)).isEmpty()); }
    @Test void funcionarioExigeDominioDoPub() { assertFalse(validator.validate(new CriarFuncionarioDTO("Nome","nome@example.com","1234567!")).isEmpty()); }
    @Test void identificaApenasIndiceDeNomeCorreto() { var e=new ConstraintViolationException("nome",new SQLException("duplicado","23505"),"uk_adicional_nome_nao_excluido");assertTrue(ConflitoBanco.nomeUnico(new RuntimeException(e),"uk_adicional_nome_nao_excluido"));assertFalse(ConflitoBanco.nomeUnico(e,"outro_indice")); }
    @Test void outraRestricaoNaoEConfundidaComNomeRepetido() { var e=new ConstraintViolationException("check",new SQLException("check","23514"),"uk_adicional_nome_nao_excluido");assertFalse(ConflitoBanco.nomeUnico(e,"uk_adicional_nome_nao_excluido")); }
}
