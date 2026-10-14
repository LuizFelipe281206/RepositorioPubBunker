package com.pubbunker.service;

import com.pubbunker.exception.RegraNegocioException;

import java.math.BigDecimal;

public final class ValoresPedido {

    private static final BigDecimal VALOR_MAXIMO =
            new BigDecimal("10000.00");

    private ValoresPedido() {
    }

    public static BigDecimal validar(BigDecimal valor) {
        if (valor == null
                || valor.signum() < 0
                || valor.compareTo(VALOR_MAXIMO) > 0
                || valor.stripTrailingZeros().scale() > 2) {
            throw new RegraNegocioException(
                    "Um valor calculado do pedido ultrapassa os limites permitidos."
            );
        }

        return valor;
    }
}