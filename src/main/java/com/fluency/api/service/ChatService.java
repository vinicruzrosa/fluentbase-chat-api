package com.fluency.api.service;

import com.fluency.api.model.ChatAnalysis;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatService {

    Mono<ChatAnalysis> sendMessage(String chatId, String message);

    Flux<ChatAnalysis> getChatHistory(String chatId);
}
