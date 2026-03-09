package com.fluency.api.fixture;

import com.fluency.api.model.ChatAnalysis;

import java.util.List;

public final class ChatAnalysisFixture {

    private ChatAnalysisFixture() {}

    public static ChatAnalysis defaultAnalysis() {
        return ChatAnalysis.builder()
                .chatId("chat-1")
                .message("Hola mundo")
                .isCorrect(true)
                .explanation("Grammatically correct")
                .reply("Bien hecho")
                .inferredContext("greeting")
                .correction(null)
                .suggestions(List.of("Try formal register"))
                .build();
    }

    public static ChatAnalysis withChatId(String chatId) {
        return ChatAnalysis.builder()
                .chatId(chatId)
                .message("test message")
                .isCorrect(false)
                .explanation("Needs correction")
                .reply("Try again")
                .inferredContext("casual")
                .correction("corrected form")
                .suggestions(List.of("suggestion1", "suggestion2"))
                .build();
    }

    public static String defaultAiResponseJson() {
        return """
                {
                    "correct": true,
                    "explanation": "Grammatically correct",
                    "reply": "Bien hecho",
                    "inferredContext": "greeting",
                    "correction": null,
                    "suggestions": ["Try formal register"]
                }
                """;
    }

    public static String fullAiResponseJson() {
        return """
                {
                    "correct": false,
                    "explanation": "Needs correction",
                    "reply": "Try again",
                    "inferredContext": "casual",
                    "correction": "corrected form",
                    "suggestions": ["suggestion1", "suggestion2"]
                }
                """;
    }
}
