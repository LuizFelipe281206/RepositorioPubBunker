package com.pubbunker.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pubbunker.exception.MuitasTentativasException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Semaphore;

@RequiredArgsConstructor
public class ProtecaoTentativasFilter extends OncePerRequestFilter {
    private final ControleTentativas controle;
    private final ObjectMapper mapper;
    private final Semaphore loginsEmAndamento = new Semaphore(8);

    private String escopo(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        if ("POST".equals(method) && "/auth/login".equals(path)) {
            return "login";
        }

        if (path.startsWith("/avaliacoes/comanda/") &&
                ("GET".equals(method) || "POST".equals(method) ||
                        "PUT".equals(method) || "DELETE".equals(method))) {
            return "comanda";
        }

        if (("GET".equals(method) &&
                (path.startsWith("/comandas/acesso/") ||
                        path.startsWith("/pedidos/comanda/"))) ||
                ("POST".equals(method) && "/pedidos".equals(path))) {
            return "comanda";
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return escopo(request) == null;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        String escopo = escopo(request);
        boolean login = "login".equals(escopo);

        if (login && !loginsEmAndamento.tryAcquire()) {
            responder(
                    response,
                    503,
                    "SERVIDOR_OCUPADO",
                    "O servidor está ocupado. Tente novamente em instantes.",
                    1
            );
            return;
        }

        try {
            if (request.getServletPath().startsWith("/avaliacoes/comanda/") &&
                    ("POST".equals(request.getMethod()) ||
                            "PUT".equals(request.getMethod()) ||
                            "DELETE".equals(request.getMethod()))) {
                controle.consumir(
                        "alteracao-avaliacao:ip:" + request.getRemoteAddr(),
                        120
                );
            }
            if ("POST".equals(request.getMethod()) &&
                    "/pedidos".equals(request.getServletPath())) {
                controle.consumir(
                        "envio-pedido:ip:" + request.getRemoteAddr(),
                        120
                );
            }
            try (var tentativa = controle.iniciar(
                    "ip:" + escopo + ":" + request.getRemoteAddr(),
                    login ? 20 : 60
            )) {
                try {
                    chain.doFilter(request, response);
                } finally {
                    if (response.getStatus() == 401) {
                        tentativa.registrarFalha();
                    }
                }
            }
        } catch (MuitasTentativasException exception) {
            responder(
                    response,
                    429,
                    "MUITAS_TENTATIVAS",
                    exception.getMessage(),
                    exception.getSegundos()
            );
        } finally {
            if (login) loginsEmAndamento.release();
        }
    }

    private void responder(
            HttpServletResponse response,
            int status,
            String codigo,
            String mensagem,
            long segundos
    ) throws IOException {
        response.setStatus(status);
        response.setHeader("Retry-After", Long.toString(segundos));
        response.setContentType("application/json;charset=UTF-8");

        mapper.writeValue(
                response.getWriter(),
                Map.of(
                        "codigo", codigo,
                        "mensagem", mensagem,
                        "aguardeSegundos", segundos
                )
        );
    }
}