package com.fluency.api.client;

import com.fluency.api.model.ChatAnalysis;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class AIOrchestratorClient {

    private final WebClient webClient;

    public AIOrchestratorClient(WebClient.Builder webClientBuilder,
                                @Value("${ai-orchestrator.url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<ChatAnalysis> analyzeMessage(String text) {
        return webClient.post()
                .uri("/chat")
                .bodyValue(Map.of("message", text))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> Mono.error(new RuntimeException("Erro ao chamar AI Orchestrator")))
                .bodyToMono(ChatAnalysis.class);
    }
}
