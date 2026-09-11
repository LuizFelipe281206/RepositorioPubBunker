package com.pubbunker.exception;

public class AcessoComandaInvalidoException extends RuntimeException {

    public AcessoComandaInvalidoException() {
        super(
                "Acesso da comanda inválido ou encerrado. "
                        + "Solicite o QR Code do atendimento atual."
        );
    }
}