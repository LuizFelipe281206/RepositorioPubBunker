package com.pubbunker.exception;

public class MuitasTentativasException extends RuntimeException {
    private final long segundos;

    public MuitasTentativasException(long segundos) {
        super(
                "Muitas tentativas. Aguarde " + segundos
                        + " segundos e tente novamente."
        );
        this.segundos = segundos;
    }

    public long getSegundos() {
        return segundos;
    }
}