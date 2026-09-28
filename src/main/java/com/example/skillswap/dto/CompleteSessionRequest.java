package com.example.skillswap.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class CompleteSessionRequest {
    @NotNull(message = "Provider ID is required")
    private Long providerId;

    @NotNull(message = "Delivered hours is required")
    @Positive(message = "Delivered hours must be positive")
    private BigDecimal deliveredHours;
}