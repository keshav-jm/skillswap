package com.example.skillswap.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateSessionRequest {

    @NotNull(message = "Requester member ID is required")
    private Long requesterId;

    @NotNull(message = "Skill offer ID is required")
    private Long skillOfferId;

    @NotNull(message = "Requested hours is required")
    @Positive(message = "Requested hours must be positive")
    private BigDecimal requestedHours;
}
