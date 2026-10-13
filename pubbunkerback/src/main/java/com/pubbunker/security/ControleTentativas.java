package com.pubbunker.security;

import com.pubbunker.exception.MuitasTentativasException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

@Component
public class ControleTentativas {
    private final Clock clock;
    private final long janela;
    private final int maxChaves;
    private final Map<String, Entrada> entradas = new HashMap<>();

    public ControleTentativas() {
        this(Clock.systemUTC(), 60_000, 10_000);
    }

    ControleTentativas(Clock clock, long janela, int maxChaves) {
        this.clock = clock;
        this.janela = janela;
        this.maxChaves = maxChaves;
    }

    public synchronized Tentativa iniciar(String chave, int limite) {
        long agora = clock.millis();
        Entrada entrada = entradas.get(chave);

        if (entrada != null &&
                entrada.emAndamento == 0 &&
                agora - entrada.inicio >= janela) {
            entradas.remove(chave);
            entrada = null;
        }

        if (entrada == null) {
            if (entradas.size() >= maxChaves) {
                entradas.entrySet().removeIf(item ->
                        item.getValue().emAndamento == 0 &&
                                agora - item.getValue().inicio >= janela
                );
            }

            if (entradas.size() >= maxChaves) {
                throw new MuitasTentativasException(1);
            }

            entrada = new Entrada(agora);
            entradas.put(chave, entrada);
        }

        if (entrada.falhas + entrada.emAndamento >= limite) {
            long segundos = entrada.falhas >= limite
                    ? Math.max(
                    1,
                    (janela - (agora - entrada.inicio) + 999) / 1000
            )
                    : 1;

            throw new MuitasTentativasException(segundos);
        }

        entrada.emAndamento++;
        return new Tentativa(entrada);
    }

    private static class Entrada {
        final long inicio;
        int falhas;
        int emAndamento;

        Entrada(long inicio) {
            this.inicio = inicio;
        }
    }

    public final class Tentativa implements AutoCloseable {
        private final Entrada entrada;
        private boolean falhou;
        private boolean encerrada;

        private Tentativa(Entrada entrada) {
            this.entrada = entrada;
        }

        public void registrarFalha() {
            falhou = true;
        }

        @Override
        public void close() {
            synchronized (ControleTentativas.this) {
                if (encerrada) return;

                encerrada = true;
                entrada.emAndamento--;

                if (falhou) entrada.falhas++;
            }
        }
    }
}