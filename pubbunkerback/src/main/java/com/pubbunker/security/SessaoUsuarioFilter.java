package com.pubbunker.security;

import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.model.Usuario;
import com.pubbunker.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class SessaoUsuarioFilter extends OncePerRequestFilter {

    private final UsuarioService usuarioService;
    private final SecurityContextRepository contextRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        var authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null &&
                authentication.getPrincipal() instanceof UsuarioSessao sessao) {

            try {
                Usuario usuario =
                        usuarioService.buscarPorId(sessao.id());

                if (usuario.getRole() == null) {
                    throw new RecursoNaoEncontradoException(
                            "Conta indisponível."
                    );
                }

                var principal = new UsuarioSessao(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getRole()
                );

                var atualizado =
                        UsernamePasswordAuthenticationToken.authenticated(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority(
                                        "ROLE_" + usuario.getRole()
                                ))
                        );

                var context =
                        SecurityContextHolder.createEmptyContext();

                context.setAuthentication(atualizado);
                SecurityContextHolder.setContext(context);

                contextRepository.saveContext(
                        context, request, response
                );

            } catch (RecursoNaoEncontradoException exception) {
                HttpSession session = request.getSession(false);

                if (session != null) {
                    session.invalidate();
                }

                SecurityContextHolder.clearContext();

                contextRepository.saveContext(
                        SecurityContextHolder.createEmptyContext(),
                        request,
                        response
                );
            }
        }

        chain.doFilter(request, response);
    }
}