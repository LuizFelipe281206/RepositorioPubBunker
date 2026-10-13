package com.pubbunker.config;

import com.pubbunker.service.UsuarioService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MigracaoSenhasConfig {

    @Bean
    @ConditionalOnProperty(
            name = "pubbunker.security.migrar-senhas",
            havingValue = "true"
    )
    public ApplicationRunner migrarSenhasAntigas(
            UsuarioService service
    ) {
        return args -> service.migrarSenhasAntigas();
    }
}