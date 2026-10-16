package com.pubbunker.exception;

import lombok.Getter;

@Getter
public class CodigoComandaException extends RuntimeException {
    private final String codigo;

    private CodigoComandaException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public static CodigoComandaException necessario() {
        return new CodigoComandaException(
                "CODIGO_COMANDA_NECESSARIO",
                "Informe os cinco dígitos fornecidos pela equipe para entrar nesta comanda."
        );
    }

    public static CodigoComandaException invalido() {
        return new CodigoComandaException(
                "CODIGO_COMANDA_INVALIDO",
                "Código incorreto. Confira os cinco dígitos com a equipe."
        );
    }
}
