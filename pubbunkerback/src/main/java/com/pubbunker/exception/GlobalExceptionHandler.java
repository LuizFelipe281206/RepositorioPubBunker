package com.pubbunker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> tratarPermissao(
            AccessDeniedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "status", 403,
                        "codigo", "REQUISICAO_NAO_AUTORIZADA",
                        "mensagem",
                        "Você não tem permissão para realizar esta operação."
                ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> tratarSessao(
            AuthenticationException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "status", 401,
                        "codigo", "SESSAO_INVALIDA",
                        "mensagem",
                        "Entre na sua conta para continuar."
                ));
    }

    @ExceptionHandler(AcessoComandaInvalidoException.class)
    public ResponseEntity<Map<String, Object>> tratarAcessoComanda(
            AcessoComandaInvalidoException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "status", 401,
                        "codigo", "ACESSO_COMANDA_INVALIDO",
                        "mensagem", exception.getMessage()
                ));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException exception
    ) {
        Map<String, Object> resposta = new HashMap<>();

        resposta.put("data", LocalDateTime.now());
        resposta.put("status", HttpStatus.NOT_FOUND.value());
        resposta.put("erro", "Recurso não encontrado");
        resposta.put("mensagem", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(resposta);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, Object>> tratarRegraNegocio(
            RegraNegocioException exception
    ) {
        Map<String, Object> resposta = new HashMap<>();

        resposta.put("data", LocalDateTime.now());
        resposta.put("status", HttpStatus.BAD_REQUEST.value());
        resposta.put("erro", "Erro de regra de negócio");
        resposta.put("mensagem", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(resposta);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarValidacao(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> campos = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(erro ->
                        campos.put(
                                erro.getField(),
                                erro.getDefaultMessage()
                        )
                );

        Map<String, Object> resposta = new HashMap<>();

        resposta.put("data", LocalDateTime.now());
        resposta.put("status", HttpStatus.BAD_REQUEST.value());
        resposta.put("erro", "Dados inválidos");
        resposta.put("codigo", "DADOS_INVALIDOS");
        resposta.put("mensagem", "Confira os dados informados.");
        resposta.put("campos", campos);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(resposta);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> tratarCorpoInvalido(
            HttpMessageNotReadableException exception
    ) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "codigo", "DADOS_INVALIDOS",
                "mensagem", "Confira o formato e os valores dos dados enviados."
        ));
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<Map<String, Object>> tratarParametroInvalido(
            Exception exception
    ) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "codigo", "DADOS_INVALIDOS",
                "mensagem", "Um parâmetro da requisição é inválido ou não foi informado."
        ));
    }

    @ExceptionHandler({
            HttpMediaTypeNotSupportedException.class,
            HttpMediaTypeNotAcceptableException.class,
            HttpRequestMethodNotSupportedException.class
    })
    public ResponseEntity<Map<String, Object>> tratarFormatoOuMetodo(
            Exception exception
    ) {
        ErrorResponse erroHttp = (ErrorResponse) exception;
        int status = erroHttp.getStatusCode().value();
        String mensagem = switch (status) {
            case 405 -> "O método enviado não é permitido para esta operação.";
            case 406 -> "O formato de resposta solicitado não está disponível.";
            default -> "Envie os dados no formato JSON.";
        };

        return ResponseEntity.status(status)
                .headers(erroHttp.getHeaders())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "status", status,
                        "codigo", "REQUISICAO_INVALIDA",
                        "mensagem", mensagem
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> tratarErroGeral(
            Exception exception
    ) {
        Map<String, Object> resposta = new HashMap<>();

        resposta.put("data", LocalDateTime.now());
        resposta.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        resposta.put("erro", "Erro interno");
        resposta.put(
                "mensagem",
                "O servidor não conseguiu concluir a operação."
        );
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(resposta);
    }
    @ExceptionHandler(MuitasTentativasException.class)
    public ResponseEntity<Map<String, Object>> tratarTentativas(
            MuitasTentativasException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header(
                        "Retry-After",
                        Long.toString(exception.getSegundos())
                )
                .body(Map.of(
                        "codigo", "MUITAS_TENTATIVAS",
                        "mensagem", exception.getMessage(),
                        "aguardeSegundos", exception.getSegundos()
                ));
    }
    @ExceptionHandler(CorpoMuitoGrandeException.class)
    public ResponseEntity<Map<String, Object>> tratarCorpoMuitoGrande(
            CorpoMuitoGrandeException exception
    ) {
        return ResponseEntity.status(413).body(Map.of(
                "status", 413,
                "codigo", "CORPO_MUITO_GRANDE",
                "mensagem", exception.getMessage()
        ));
    }
}
