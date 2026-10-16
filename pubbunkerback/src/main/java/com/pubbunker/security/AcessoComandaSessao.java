package com.pubbunker.security;

import com.pubbunker.exception.CodigoComandaException;
import com.pubbunker.model.Comanda;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class AcessoComandaSessao {
    private static final String ATRIBUTO =
            AcessoComandaSessao.class.getName() + ".autorizacoes";
    private static final int MAX_ATENDIMENTOS = 16;

    private record Autorizacao(
            String codigoAcesso,
            LocalDateTime dataAbertura
    ) implements Serializable {
        boolean corresponde(Comanda comanda) {
            return Objects.equals(codigoAcesso, comanda.getCodigoAcesso())
                    && Objects.equals(dataAbertura, comanda.getDataAbertura());
        }
    }

    private HttpServletRequest requisicao() {
        if (RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes atributos) {
            return atributos.getRequest();
        }
        throw CodigoComandaException.necessario();
    }

    private Map<Long, Autorizacao> autorizacoes(HttpSession sessao) {
        if (sessao == null) return Map.of();

        try {
            Object salvo = sessao.getAttribute(ATRIBUTO);
            if (!(salvo instanceof Map<?, ?> mapa)) return Map.of();

            Map<Long, Autorizacao> validas = new LinkedHashMap<>();
            mapa.forEach((chave, valor) -> {
                if (chave instanceof Long id && valor instanceof Autorizacao autorizacao) {
                    validas.put(id, autorizacao);
                }
            });
            return validas;
        } catch (IllegalStateException exception) {
            return Map.of();
        }
    }

    public boolean estaAutorizada(Comanda comanda) {
        var autorizacao = autorizacoes(requisicao().getSession(false))
                .get(comanda.getId());
        return autorizacao != null && autorizacao.corresponde(comanda);
    }

    public void exigirAutorizacao(Comanda comanda) {
        if (!estaAutorizada(comanda)) {
            throw CodigoComandaException.necessario();
        }
    }

    public String enderecoRemoto() {
        return requisicao().getRemoteAddr();
    }

    // Chamado pelo controller somente depois do commit da autorização.
    public void autorizar(Comanda comanda, HttpServletRequest request) {
        HttpSession sessao = request.getSession();

        synchronized (sessao) {
            try {
                Map<Long, Autorizacao> novas = new LinkedHashMap<>(autorizacoes(sessao));
                var existente = novas.get(comanda.getId());
                if (existente != null && existente.corresponde(comanda)) return;
                novas.remove(comanda.getId());
                while (novas.size() >= MAX_ATENDIMENTOS) {
                    novas.remove(novas.keySet().iterator().next());
                }
                novas.put(comanda.getId(), new Autorizacao(
                        comanda.getCodigoAcesso(), comanda.getDataAbertura()
                ));

                request.changeSessionId();
                sessao.setAttribute(ATRIBUTO, Collections.unmodifiableMap(novas));
            } catch (IllegalStateException exception) {
                throw CodigoComandaException.necessario();
            }
        }
    }
}
