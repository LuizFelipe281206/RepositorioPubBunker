package com.pubbunker.config;

import com.pubbunker.security.LeituraCorpoJson;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

@ControllerAdvice
public class LimiteCorpoJsonAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(
            MethodParameter parametro,
            Type tipo,
            Class<? extends HttpMessageConverter<?>> conversor
    ) {
        return MappingJackson2HttpMessageConverter.class
                .isAssignableFrom(conversor);
    }

    @Override
    public HttpInputMessage beforeBodyRead(
            HttpInputMessage entrada,
            MethodParameter parametro,
            Type tipo,
            Class<? extends HttpMessageConverter<?>> conversor
    ) throws IOException {
        byte[] corpo = LeituraCorpoJson.ler(
                entrada.getBody(),
                entrada.getHeaders().getContentLength()
        );

        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(corpo);
            }

            @Override
            public HttpHeaders getHeaders() {
                return entrada.getHeaders();
            }
        };
    }
}