package com.fluency.api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidation_shouldReturn400WithValidationMessage() {
        WebExchangeBindException ex = mock(WebExchangeBindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        ObjectError error = new ObjectError("message", "A mensagem não pode ser vazia");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(error));

        StepVerifier.create(handler.handleValidation(ex))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    Map<String, Object> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.get("status")).isEqualTo(400);
                    assertThat(body.get("message")).isEqualTo("A mensagem não pode ser vazia");
                })
                .verifyComplete();
    }

    @Test
    void handleBadRequest_shouldReturn400WithExceptionMessage() {
        IllegalArgumentException ex = new IllegalArgumentException("chatId inválido");

        StepVerifier.create(handler.handleBadRequest(ex))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    Map<String, Object> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.get("status")).isEqualTo(400);
                    assertThat(body.get("message")).isEqualTo("chatId inválido");
                })
                .verifyComplete();
    }

    @Test
    void handleAll_shouldReturn500WithGenericMessage() {
        Exception ex = new NullPointerException("unexpected");

        StepVerifier.create(handler.handleAll(ex))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    Map<String, Object> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.get("status")).isEqualTo(500);
                    assertThat(body.get("message")).isEqualTo("Erro inesperado no servidor");
                })
                .verifyComplete();
    }

    @Test
    void buildResponse_shouldContainTimestampStatusAndMessage() {
        IllegalArgumentException ex = new IllegalArgumentException("test");

        StepVerifier.create(handler.handleBadRequest(ex))
                .assertNext(response -> {
                    Map<String, Object> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body).containsKeys("timestamp", "status", "message");
                    assertThat(body.get("timestamp")).isNotNull();
                })
                .verifyComplete();
    }
}
