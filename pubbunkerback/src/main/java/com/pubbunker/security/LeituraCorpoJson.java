package com.pubbunker.security;

import com.pubbunker.exception.CorpoMuitoGrandeException;

import java.io.IOException;
import java.io.InputStream;

public final class LeituraCorpoJson {

    public static final int TAMANHO_MAXIMO = 1024 * 1024;

    private LeituraCorpoJson() {
    }

    public static byte[] ler(
            InputStream entrada,
            long tamanhoInformado
    ) throws IOException {
        if (tamanhoInformado > TAMANHO_MAXIMO) {
            throw new CorpoMuitoGrandeException();
        }

        byte[] corpo = entrada.readNBytes(TAMANHO_MAXIMO + 1);

        if (corpo.length > TAMANHO_MAXIMO) {
            throw new CorpoMuitoGrandeException();
        }

        return corpo;
    }
}