package com.pubbunker.service;

import com.pubbunker.model.AdicionalPedidoSnapshot;
import com.pubbunker.model.ItemPedido;
import com.pubbunker.model.Pedido;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.math.BigDecimal;

/** Identifica o conteúdo lido pelo atendente sem alterar o esquema do banco. */
public final class RevisaoPedido {
    private RevisaoPedido() {}

    public static String calcular(Pedido pedido) {
        StringBuilder conteudo = new StringBuilder();
        campo(conteudo, pedido.getId());
        campo(conteudo, pedido.getComanda().getId());
        campo(conteudo, pedido.getStatus());
        campo(conteudo, pedido.getObservacao());
        campo(conteudo, pedido.getValorTotal());
        campo(conteudo, pedido.getItens().size());
        pedido.getItens().stream().sorted(Comparator.comparing(ItemPedido::getId)).forEach(item -> {
            campo(conteudo, item.getId());
            campo(conteudo, item.getProduto().getId());
            campo(conteudo, item.getProdutoNome());
            campo(conteudo, item.getQuantidade());
            campo(conteudo, item.getPrecoUnitario());
            campo(conteudo, item.getSubtotal());
            var extras = item.getAdicionaisSnapshot().isEmpty()
                    ? item.getAdicionais().stream().map(AdicionalPedidoSnapshot::new).toList()
                    : item.getAdicionaisSnapshot();
            campo(conteudo, extras.size());
            extras.stream().sorted(Comparator.comparing(AdicionalPedidoSnapshot::getAdicionalId)).forEach(extra -> {
                campo(conteudo, extra.getAdicionalId());
                campo(conteudo, extra.getNome());
                campo(conteudo, extra.getPreco());
            });
        });
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(conteudo.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível", exception);
        }
    }

    private static void campo(StringBuilder destino, Object valor) {
        String texto = valor == null ? "" : valor instanceof BigDecimal numero
                ? numero.stripTrailingZeros().toPlainString() : valor.toString();
        destino.append(texto.length()).append(':').append(texto).append(';');
    }
}
