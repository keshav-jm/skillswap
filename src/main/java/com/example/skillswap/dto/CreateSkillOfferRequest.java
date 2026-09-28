package com.example.skillswap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateSkillOfferRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotBlank(message = "Skill name is required")
    @Size(min = 2, max = 200, message = "Skill name must be between 2 and 200 characters")
    private String skillName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Available hours is required")
    @Positive(message = "Available hours must be positive")
    private BigDecimal availableHours;
}
