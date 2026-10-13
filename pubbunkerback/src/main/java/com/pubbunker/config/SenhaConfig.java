package com.pubbunker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

@Configuration
public class SenhaConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder argon2 =
                new Argon2PasswordEncoder(16, 32, 1, 19 * 1024, 2);

        return new DelegatingPasswordEncoder(
                "argon2id",
                Map.of("argon2id", argon2)
        );
    }
}