package com.pubbunker.controller;

import com.pubbunker.dto.LoginRequestDTO;
import com.pubbunker.dto.LoginResponseDTO;
import com.pubbunker.security.UsuarioSessao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextRepository;
    private final SessionAuthenticationStrategy sessionStrategy;

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequestDTO dto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            var authentication =
                    authenticationManager.authenticate(
                            UsernamePasswordAuthenticationToken.unauthenticated(
                                    dto.getEmail(),
                                    dto.getSenha()
                            )
                    );

            sessionStrategy.onAuthentication(
                    authentication,
                    request,
                    response
            );

            var context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            contextRepository.saveContext(
                    context, request, response
            );

            return ResponseEntity.ok(
                    resposta(
                            (UsuarioSessao) authentication.getPrincipal()
                    )
            );

        } catch (BadCredentialsException exception) {
            return ResponseEntity.status(401)
                    .body(
                            Map.of(
                                    "mensagem",
                                    "Email ou senha inválidos."
                            )
                    );
        }
    }

    @GetMapping("/me")
    public LoginResponseDTO me(
            @AuthenticationPrincipal UsuarioSessao usuario
    ) {
        return resposta(usuario);
    }

    private LoginResponseDTO resposta(UsuarioSessao usuario) {
        return new LoginResponseDTO(
                usuario.id(),
                usuario.nome(),
                usuario.email(),
                usuario.role()
        );
    }
}