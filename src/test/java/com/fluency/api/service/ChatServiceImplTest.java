package com.fluency.api.service;

import com.fluency.api.client.AIOrchestratorClient;
import com.fluency.api.fixture.ChatAnalysisFixture;
import com.fluency.api.model.ChatAnalysis;
import com.fluency.api.repository.ChatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private AIOrchestratorClient aiOrchestratorClient;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    void sendMessage_shouldCallAiAndSaveResult() {
        ChatAnalysis aiResponse = ChatAnalysisFixture.defaultAnalysis();
        when(aiOrchestratorClient.analyzeMessage("Hello")).thenReturn(Mono.just(aiResponse));
        when(chatRepository.save(any(ChatAnalysis.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(chatService.sendMessage("chat-1", "Hello"))
                .assertNext(result -> {
                    assertThat(result.getChatId()).isEqualTo("chat-1");
                    assertThat(result.getMessage()).isEqualTo("Hello");
                })
                .verifyComplete();

        verify(aiOrchestratorClient).analyzeMessage("Hello");
        verify(chatRepository).save(any(ChatAnalysis.class));
    }

    @Test
    void sendMessage_shouldPropagateAiError() {
        when(aiOrchestratorClient.analyzeMessage("bad"))
                .thenReturn(Mono.error(new RuntimeException("Erro ao chamar AI Orchestrator")));

        StepVerifier.create(chatService.sendMessage("chat-1", "bad"))
                .expectError(RuntimeException.class)
                .verify();

        verify(chatRepository, never()).save(any());
    }

    @Test
    void sendMessage_shouldPropagateSaveError() {
        ChatAnalysis aiResponse = ChatAnalysisFixture.defaultAnalysis();
        when(aiOrchestratorClient.analyzeMessage("Hello")).thenReturn(Mono.just(aiResponse));
        when(chatRepository.save(any(ChatAnalysis.class)))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(chatService.sendMessage("chat-1", "Hello"))
                .expectError(RuntimeException.class)
                .verify();

        verify(aiOrchestratorClient).analyzeMessage("Hello");
    }

    @Test
    void sendMessage_shouldMapAllFieldsFromAiResponse() {
        ChatAnalysis aiResponse = ChatAnalysis.builder()
                .isCorrect(false)
                .explanation("Needs fix")
                .reply("Try again")
                .inferredContext("casual")
                .correction("corrected")
                .suggestions(List.of("s1", "s2"))
                .build();

        when(aiOrchestratorClient.analyzeMessage("Hola")).thenReturn(Mono.just(aiResponse));
        when(chatRepository.save(any(ChatAnalysis.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(chatService.sendMessage("my-chat", "Hola"))
                .assertNext(result -> {
                    assertThat(result.getChatId()).isEqualTo("my-chat");
                    assertThat(result.getMessage()).isEqualTo("Hola");
                    assertThat(result.isCorrect()).isFalse();
                    assertThat(result.getExplanation()).isEqualTo("Needs fix");
                    assertThat(result.getReply()).isEqualTo("Try again");
                    assertThat(result.getInferredContext()).isEqualTo("casual");
                    assertThat(result.getCorrection()).isEqualTo("corrected");
                    assertThat(result.getSuggestions()).containsExactly("s1", "s2");
                })
                .verifyComplete();
    }

    @Test
    void getChatHistory_shouldReturnFluxFromRepository() {
        ChatAnalysis a1 = ChatAnalysisFixture.defaultAnalysis();
        ChatAnalysis a2 = ChatAnalysisFixture.withChatId("chat-1");
        when(chatRepository.findByChatId("chat-1")).thenReturn(Flux.just(a1, a2));

        StepVerifier.create(chatService.getChatHistory("chat-1"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getChatHistory_shouldReturnEmptyFluxWhenNoChatFound() {
        when(chatRepository.findByChatId("nonexistent")).thenReturn(Flux.empty());

        StepVerifier.create(chatService.getChatHistory("nonexistent"))
                .verifyComplete();
    }
}
