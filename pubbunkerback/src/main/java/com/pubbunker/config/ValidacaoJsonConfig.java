package com.pubbunker.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidacaoJsonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer validarTiposJson() {
        return builder -> builder.postConfigurer(mapper -> {
            mapper.enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS);
            mapper.coercionConfigFor(LogicalType.Integer)
                    .setCoercion(CoercionInputShape.Float, CoercionAction.Fail);
        });
    }
}
