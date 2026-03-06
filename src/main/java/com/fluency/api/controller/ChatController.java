package com.fluency.api.controller;

import com.fluency.api.dto.ChatMessageRequestDto;
import com.fluency.api.model.ChatAnalysis;
import com.fluency.api.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Gerenciamento de conversas e análises")
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "Envia mensagem", description = "Envia texto para análise e persiste o resultado.")
    @PostMapping("/{chatId}/messages")
    public Mono<ChatAnalysis> sendMessage(@PathVariable String chatId,
                                          @Valid @RequestBody ChatMessageRequestDto request) {
        return chatService.sendMessage(chatId, request.message());
    }

    @Operation(summary = "Lista histórico", description = "Retorna todas as análises de um chat específico.")
    @GetMapping("/{chatId}")
    public Flux<ChatAnalysis> getHistory(@PathVariable String chatId) {
        return chatService.getChatHistory(chatId);
    }
}
