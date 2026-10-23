package com.pubbunker.service;

import com.pubbunker.exception.RegraNegocioException;

import java.math.BigDecimal;

public final class ValoresPedido {

    private static final BigDecimal VALOR_MAXIMO =
            new BigDecimal("10000.00");

    private ValoresPedido() {
    }

    public static BigDecimal validar(BigDecimal valor) {
        if (valor == null || valor.signum() < 0
                || valor.stripTrailingZeros().scale() > 2) {
            throw new RegraNegocioException("Confira os preços e as quantidades do pedido.",
                    "VALOR_PEDIDO_INVALIDO");
        }
        if (valor.compareTo(VALOR_MAXIMO) > 0) {
            throw new RegraNegocioException(
                    "O total do pedido deve ser no máximo R$ 10.000,00. Remova itens ou reduza as quantidades para continuar.",
                    "LIMITE_PEDIDO_EXCEDIDO");
        }

        return valor;
    }
}