package com.pubbunker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pubbunker.exception.RecursoNaoEncontradoException;
import com.pubbunker.model.Usuario;
import com.pubbunker.security.SessaoUsuarioFilter;
import com.pubbunker.security.UsuarioSessao;
import com.pubbunker.service.UsuarioService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.pubbunker.security.ControleTentativas;
import com.pubbunker.security.ProtecaoTentativasFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import java.util.Locale;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
public class SegurancaConfig {

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }

    @Bean
    public HttpSessionCsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(
            HttpSessionCsrfTokenRepository csrfRepository
    ) {
        return new CompositeSessionAuthenticationStrategy(
                List.of(
                        new ChangeSessionIdAuthenticationStrategy(),
                        new CsrfAuthenticationStrategy(csrfRepository)
                )
        );
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UsuarioService service,
            ControleTentativas controle
    ) {
        return tentativa -> {
            String email = tentativa.getName().trim();

            try (var limite = controle.iniciar(
                    "conta:" + email.toLowerCase(Locale.ROOT),
                    10
            )) {
                Usuario usuario = null;

                try {
                    usuario = service.buscarPorEmail(email);
                } catch (RecursoNaoEncontradoException exception) {
                    // Conta ausente e senha incorreta têm a mesma resposta.
                }

                String senha =
                        tentativa.getCredentials() instanceof String valor
                                ? valor
                                : null;

                if (!service.verificarSenha(usuario, senha)) {
                    limite.registrarFalha();

                    throw new BadCredentialsException(
                            "Email ou senha inválidos."
                    );
                }

                var principal = new UsuarioSessao(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getRole()
                );

                return UsernamePasswordAuthenticationToken.authenticated(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority(
                                "ROLE_" + usuario.getRole()
                        ))
                );
            }
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${pubbunker.security.front-origins}") String origins
    ) {
        CorsConfiguration cors = new CorsConfiguration();

        cors.setAllowedOrigins(
                Arrays.stream(origins.split(","))
                        .map(String::trim)
                        .filter(valor -> !valor.isBlank())
                        .toList()
        );

        cors.setAllowedMethods(
                List.of(
                        "GET", "POST", "PUT",
                        "PATCH", "DELETE", "OPTIONS"
                )
        );

        cors.setAllowedHeaders(
                List.of("Content-Type", "X-CSRF-TOKEN")
        );

        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository repository,
            HttpSessionCsrfTokenRepository csrfRepository,
            @Qualifier("corsConfigurationSource")
            CorsConfigurationSource corsSource,
            UsuarioService service,
            ObjectMapper mapper,
            ControleTentativas controle
    ) throws Exception {

        http
                .cors(cors ->
                        cors.configurationSource(corsSource)
                )

                .csrf(csrf ->
                        csrf.csrfTokenRepository(csrfRepository)
                )

                .securityContext(context ->
                        context.securityContextRepository(repository)
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                .requestCache(cache -> cache.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .authorizeHttpRequests(access -> access
                        .requestMatchers(
                                HttpMethod.GET,
                                "/auth/csrf"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/login",
                                "/auth/logout"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/auth/me"
                        ).authenticated()

                        .requestMatchers("/auth/**").denyAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/produtos",
                                "/produtos/{id}",
                                "/adicionais",
                                "/adicionais/{id}",
                                "/comandas/acesso/{codigoAcesso}",
                                "/pedidos/comanda/{codigoAcesso}"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/pedidos"
                        ).permitAll()

                        .requestMatchers(
                                "/produtos/**",
                                "/adicionais/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/comandas/**",
                                "/pedidos/**"
                        ).hasAnyRole("ADMIN", "FUNCIONARIO")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/reservas/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/reservas/**"
                        ).hasAnyRole("ADMIN", "FUNCIONARIO")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/avaliacoes/comanda/{codigoAcesso}"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/avaliacoes/comanda/{codigoAcesso}"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/avaliacoes/comanda/{codigoAcesso}/{id}"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/avaliacoes/comanda/{codigoAcesso}/{id}"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/avaliacoes"
                        ).denyAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/avaliacoes/arquivadas"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/avaliacoes/{id}/arquivar"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/avaliacoes/arquivadas/{id}"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/avaliacoes"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/avaliacoes/**"
                        ).hasAnyRole("ADMIN", "CLIENTE")

                        .requestMatchers(
                                "/avaliacoes/**"
                        ).hasRole("CLIENTE")

                        .anyRequest().denyAll()
                )

                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(
                                (request, response, exception) -> {
                                    response.setStatus(401);
                                    response.setContentType(
                                            "application/json;charset=UTF-8"
                                    );

                                    mapper.writeValue(
                                            response.getWriter(),
                                            Map.of(
                                                    "codigo",
                                                    "SESSAO_INVALIDA",
                                                    "mensagem",
                                                    "Entre na sua conta para continuar."
                                            )
                                    );
                                }
                        )

                        .accessDeniedHandler(
                                (request, response, exception) -> {
                                    response.setStatus(403);
                                    response.setContentType(
                                            "application/json;charset=UTF-8"
                                    );

                                    mapper.writeValue(
                                            response.getWriter(),
                                            Map.of(
                                                    "codigo",
                                                    "REQUISICAO_NAO_AUTORIZADA",
                                                    "mensagem",
                                                    "Requisição não autorizada."
                                            )
                                    );
                                }
                        )
                )

                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .deleteCookies("PUBBUNKER_SESSION")
                        .logoutSuccessHandler(
                                (request, response, authentication) ->
                                        response.setStatus(204)
                        )
                )

                .addFilterBefore(
                        new SessaoUsuarioFilter(service, repository),
                        AnonymousAuthenticationFilter.class
                )

                .addFilterBefore(
                        new ProtecaoTentativasFilter(controle, mapper),
                        CsrfFilter.class
                );

        return http.build();
    }
}