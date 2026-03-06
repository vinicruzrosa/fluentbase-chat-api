package com.fluency.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "chats")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatAnalysis {

    @Id
    private String id;
    private String chatId;
    private String message;
    private boolean isCorrect;
    private String explanation;
    private String reply;
    private String inferredContext;
    private String correction;
    private List<String> suggestions;

    @CreatedDate
    private LocalDateTime createdAt;
}
