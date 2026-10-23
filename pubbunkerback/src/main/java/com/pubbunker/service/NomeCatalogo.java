package com.pubbunker.service;

public final class NomeCatalogo {
    private NomeCatalogo() { }

    public static String limparBordas(String texto) {
        return texto == null ? null : texto.replaceAll("^[\\s\\p{Z}\\uFEFF]+|[\\s\\p{Z}\\uFEFF]+$", "");
    }

    public static String normalizar(String nome) {
        return nome == null ? null : nome.replaceAll("[\\s\\p{Z}\\uFEFF]+", " ").trim();
    }
}
