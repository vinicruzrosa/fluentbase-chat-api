package com.fluency.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequestDto(
        @NotBlank(message = "A mensagem não pode ser vazia")
        String message
) {}
