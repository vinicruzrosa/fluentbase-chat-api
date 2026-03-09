package com.fluency.api.controller;

import com.fluency.api.dto.ChatMessageRequestDto;
import com.fluency.api.exception.GlobalExceptionHandler;
import com.fluency.api.fixture.ChatAnalysisFixture;
import com.fluency.api.model.ChatAnalysis;
import com.fluency.api.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@WebFluxTest(ChatController.class)
@Import(GlobalExceptionHandler.class)
class ChatControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ChatService chatService;

    @Test
    void sendMessage_shouldReturn200WithChatAnalysis() {
        ChatAnalysis expected = ChatAnalysisFixture.defaultAnalysis();
        when(chatService.sendMessage("chat-1", "Hola mundo")).thenReturn(Mono.just(expected));

        webTestClient.post()
                .uri("/api/v1/chats/chat-1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ChatMessageRequestDto("Hola mundo"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.chatId").isEqualTo("chat-1")
                .jsonPath("$.message").isEqualTo("Hola mundo")
                .jsonPath("$.correct").isEqualTo(true)
                .jsonPath("$.reply").isEqualTo("Bien hecho")
                .jsonPath("$.explanation").isEqualTo("Grammatically correct");
    }

    @Test
    void sendMessage_shouldReturn400WhenMessageIsBlank() {
        webTestClient.post()
                .uri("/api/v1/chats/chat-1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"message\": \"\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("A mensagem não pode ser vazia");
    }

    @Test
    void sendMessage_shouldReturn400WhenMessageIsNull() {
        webTestClient.post()
                .uri("/api/v1/chats/chat-1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void sendMessage_shouldReturnErrorWhenBodyIsMissing() {
        webTestClient.post()
                .uri("/api/v1/chats/chat-1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void sendMessage_shouldReturn500WhenServiceThrowsUnexpectedException() {
        when(chatService.sendMessage("chat-1", "Hello"))
                .thenReturn(Mono.error(new RuntimeException("unexpected")));

        webTestClient.post()
                .uri("/api/v1/chats/chat-1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ChatMessageRequestDto("Hello"))
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getHistory_shouldReturn200WithFluxOfChatAnalysis() {
        ChatAnalysis a1 = ChatAnalysisFixture.defaultAnalysis();
        ChatAnalysis a2 = ChatAnalysisFixture.withChatId("chat-1");
        when(chatService.getChatHistory("chat-1")).thenReturn(Flux.just(a1, a2));

        webTestClient.get()
                .uri("/api/v1/chats/chat-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].chatId").isEqualTo("chat-1");
    }

    @Test
    void getHistory_shouldReturn200WithEmptyArrayWhenNoHistory() {
        when(chatService.getChatHistory("chat-1")).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/chats/chat-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void sendMessage_shouldReturnErrorWhenContentTypeIsNotJson() {
        webTestClient.post()
                .uri("/api/v1/chats/chat-1/messages")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("Hello")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
