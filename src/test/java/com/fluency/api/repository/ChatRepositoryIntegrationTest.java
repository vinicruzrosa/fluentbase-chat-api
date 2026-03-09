package com.fluency.api.repository;

import com.fluency.api.model.ChatAnalysis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@EnableReactiveMongoAuditing
class ChatRepositoryIntegrationTest {

    @Autowired
    private ChatRepository chatRepository;

    @BeforeEach
    void cleanUp() {
        chatRepository.deleteAll().block();
    }

    @Test
    void save_shouldPersistChatAnalysisAndGenerateId() {
        ChatAnalysis analysis = ChatAnalysis.builder()
                .chatId("chat-1")
                .message("Hello")
                .isCorrect(true)
                .build();

        StepVerifier.create(chatRepository.save(analysis))
                .assertNext(saved -> assertThat(saved.getId()).isNotNull())
                .verifyComplete();
    }

    @Test
    void save_shouldPopulateCreatedAtField() {
        ChatAnalysis analysis = ChatAnalysis.builder()
                .chatId("chat-1")
                .message("Hello")
                .build();

        StepVerifier.create(chatRepository.save(analysis))
                .assertNext(saved -> assertThat(saved.getCreatedAt()).isNotNull())
                .verifyComplete();
    }

    @Test
    void findByChatId_shouldReturnAllAnalysesForGivenChatId() {
        ChatAnalysis a1 = ChatAnalysis.builder().chatId("chat-1").message("m1").build();
        ChatAnalysis a2 = ChatAnalysis.builder().chatId("chat-1").message("m2").build();
        ChatAnalysis a3 = ChatAnalysis.builder().chatId("chat-2").message("m3").build();

        chatRepository.saveAll(Flux.just(a1, a2, a3)).blockLast();

        StepVerifier.create(chatRepository.findByChatId("chat-1"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findByChatId_shouldReturnEmptyFluxWhenChatIdDoesNotExist() {
        StepVerifier.create(chatRepository.findByChatId("nonexistent"))
                .verifyComplete();
    }

    @Test
    void findByChatId_shouldReturnCorrectFieldValues() {
        ChatAnalysis analysis = ChatAnalysis.builder()
                .chatId("chat-1")
                .message("Hola")
                .isCorrect(false)
                .explanation("Needs work")
                .reply("Try again")
                .inferredContext("casual")
                .correction("corrected")
                .suggestions(List.of("s1", "s2"))
                .build();

        chatRepository.save(analysis).block();

        StepVerifier.create(chatRepository.findByChatId("chat-1"))
                .assertNext(found -> {
                    assertThat(found.getChatId()).isEqualTo("chat-1");
                    assertThat(found.getMessage()).isEqualTo("Hola");
                    assertThat(found.isCorrect()).isFalse();
                    assertThat(found.getExplanation()).isEqualTo("Needs work");
                    assertThat(found.getReply()).isEqualTo("Try again");
                    assertThat(found.getInferredContext()).isEqualTo("casual");
                    assertThat(found.getCorrection()).isEqualTo("corrected");
                    assertThat(found.getSuggestions()).containsExactly("s1", "s2");
                })
                .verifyComplete();
    }
}
