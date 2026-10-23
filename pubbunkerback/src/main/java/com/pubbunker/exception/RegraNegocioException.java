package com.pubbunker.exception;

public class RegraNegocioException extends RuntimeException {
    private final String codigo;

    public RegraNegocioException(String mensagem) {
        this(mensagem, "REGRA_NEGOCIO");
    }

    public RegraNegocioException(String mensagem, String codigo) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() { return codigo; }
}
