package com.fluency.api.e2e;

import com.fluency.api.fixture.ChatAnalysisFixture;
import com.fluency.api.repository.ChatRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ChatFlowE2ETest {

    static MockWebServer aiMockServer;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ChatRepository chatRepository;

    @BeforeAll
    static void startMockServer() throws IOException {
        aiMockServer = new MockWebServer();
        aiMockServer.start();
    }

    @AfterAll
    static void stopMockServer() throws IOException {
        aiMockServer.shutdown();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("ai-orchestrator.url", () -> aiMockServer.url("/").toString());
    }

    @BeforeEach
    void cleanDb() {
        chatRepository.deleteAll().block();
    }

    @Test
    void fullFlow_sendMessageAndRetrieveHistory() {
        aiMockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(ChatAnalysisFixture.defaultAiResponseJson()));

        webTestClient.post()
                .uri("/api/v1/chats/chat-1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"message\":\"Hola mundo\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.chatId").isEqualTo("chat-1")
                .jsonPath("$.message").isEqualTo("Hola mundo")
                .jsonPath("$.correct").isEqualTo(true)
                .jsonPath("$.reply").isEqualTo("Bien hecho");

        webTestClient.get()
                .uri("/api/v1/chats/chat-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].chatId").isEqualTo("chat-1")
                .jsonPath("$[0].message").isEqualTo("Hola mundo");
    }

    @Test
    void sendMessage_shouldPersistToMongoDB() {
        aiMockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(ChatAnalysisFixture.defaultAiResponseJson()));

        webTestClient.post()
                .uri("/api/v1/chats/chat-persist/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"message\":\"Test persist\"}")
                .exchange()
                .expectStatus().isOk();

        StepVerifier.create(chatRepository.findByChatId("chat-persist"))
                .assertNext(saved -> {
                    assertThat(saved.getChatId()).isEqualTo("chat-persist");
                    assertThat(saved.getMessage()).isEqualTo("Test persist");
                    assertThat(saved.getCreatedAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void sendMessage_shouldReturn500WhenAiServiceReturns500() {
        aiMockServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("{}"));

        webTestClient.post()
                .uri("/api/v1/chats/chat-err/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"message\":\"Hello\"}")
                .exchange()
                .expectStatus().is5xxServerError();

        StepVerifier.create(chatRepository.findByChatId("chat-err"))
                .verifyComplete();
    }

    @Test
    void sendMessage_shouldReturn400WhenMessageIsBlank() {
        webTestClient.post()
                .uri("/api/v1/chats/chat-1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"message\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("A mensagem não pode ser vazia");
    }

    @Test
    void getHistory_shouldReturnMultipleMessages() {
        for (int i = 0; i < 3; i++) {
            aiMockServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(ChatAnalysisFixture.defaultAiResponseJson()));

            webTestClient.post()
                    .uri("/api/v1/chats/chat-multi/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"message\":\"msg" + i + "\"}")
                    .exchange()
                    .expectStatus().isOk();
        }

        webTestClient.get()
                .uri("/api/v1/chats/chat-multi")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3);
    }

    @Test
    void getHistory_shouldReturnEmptyForNewChat() {
        webTestClient.get()
                .uri("/api/v1/chats/brand-new-chat")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void sendMessage_shouldReturn500WhenAiServiceReturns4xx() {
        aiMockServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody("{}"));

        webTestClient.post()
                .uri("/api/v1/chats/chat-4xx/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"message\":\"Hello\"}")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
