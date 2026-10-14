package com.pubbunker.exception;

public class CorpoMuitoGrandeException extends RuntimeException {

    public CorpoMuitoGrandeException() {
        super("O corpo da requisição deve possuir no máximo 1 MiB.");
    }
}