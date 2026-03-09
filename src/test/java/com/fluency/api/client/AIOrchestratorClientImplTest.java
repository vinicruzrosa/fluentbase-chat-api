package com.fluency.api.client;

import com.fluency.api.fixture.ChatAnalysisFixture;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class AIOrchestratorClientImplTest {

    private MockWebServer mockWebServer;
    private AIOrchestratorClientImpl client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/").toString();
        client = new AIOrchestratorClientImpl(WebClient.builder(), baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void analyzeMessage_shouldReturnChatAnalysisOnSuccess() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(ChatAnalysisFixture.defaultAiResponseJson()));

        StepVerifier.create(client.analyzeMessage("Hola mundo"))
                .assertNext(result -> {
                    assertThat(result.isCorrect()).isTrue();
                    assertThat(result.getReply()).isEqualTo("Bien hecho");
                })
                .verifyComplete();
    }

    @Test
    void analyzeMessage_shouldThrowRuntimeExceptionOn4xxError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader("Content-Type", "application/json")
                .setBody("{}"));

        StepVerifier.create(client.analyzeMessage("bad input"))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("Erro ao chamar AI Orchestrator"))
                .verify();
    }

    @Test
    void analyzeMessage_shouldThrowRuntimeExceptionOn5xxError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("{}"));

        StepVerifier.create(client.analyzeMessage("test"))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("Erro ao chamar AI Orchestrator"))
                .verify();
    }

    @Test
    void analyzeMessage_shouldDeserializeAllFieldsCorrectly() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(ChatAnalysisFixture.fullAiResponseJson()));

        StepVerifier.create(client.analyzeMessage("test"))
                .assertNext(result -> {
                    assertThat(result.isCorrect()).isFalse();
                    assertThat(result.getExplanation()).isEqualTo("Needs correction");
                    assertThat(result.getReply()).isEqualTo("Try again");
                    assertThat(result.getInferredContext()).isEqualTo("casual");
                    assertThat(result.getCorrection()).isEqualTo("corrected form");
                    assertThat(result.getSuggestions()).containsExactly("suggestion1", "suggestion2");
                })
                .verifyComplete();
    }

    @Test
    void analyzeMessage_shouldSendCorrectRequestBody() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(ChatAnalysisFixture.defaultAiResponseJson()));

        StepVerifier.create(client.analyzeMessage("Hola mundo"))
                .expectNextCount(1)
                .verifyComplete();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/chat");
        assertThat(request.getBody().readUtf8()).contains("\"message\":\"Hola mundo\"");
        assertThat(request.getHeader("Content-Type")).contains("application/json");
    }
}
