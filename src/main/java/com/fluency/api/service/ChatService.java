package com.fluency.api.service;

import com.fluency.api.client.AIOrchestratorClient;
import com.fluency.api.model.ChatAnalysis;
import com.fluency.api.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final AIOrchestratorClient aiOrchestratorClient;

    public Mono<ChatAnalysis> sendMessage(String chatId, String message) {
        return aiOrchestratorClient.analyzeMessage(message)
                .map(aiResponse -> ChatAnalysis.builder()
                        .chatId(chatId)
                        .message(message)
                        .isCorrect(aiResponse.isCorrect())
                        .explanation(aiResponse.getExplanation())
                        .reply(aiResponse.getReply())
                        .inferredContext(aiResponse.getInferredContext())
                        .correction(aiResponse.getCorrection())
                        .suggestions(aiResponse.getSuggestions())
                        .build())
                .flatMap(chatRepository::save);
    }

    public Flux<ChatAnalysis> getChatHistory(String chatId) {
        return chatRepository.findByChatId(chatId);
    }
}
