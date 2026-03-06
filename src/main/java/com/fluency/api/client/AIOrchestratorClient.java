package com.fluency.api.client;

import com.fluency.api.model.ChatAnalysis;
import reactor.core.publisher.Mono;

public interface AIOrchestratorClient {

    Mono<ChatAnalysis> analyzeMessage(String text);
}
