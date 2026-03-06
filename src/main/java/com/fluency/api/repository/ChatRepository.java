package com.fluency.api.repository;

import com.fluency.api.model.ChatAnalysis;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ChatRepository extends ReactiveMongoRepository<ChatAnalysis, String> {

    Flux<ChatAnalysis> findByChatId(String chatId);
}
