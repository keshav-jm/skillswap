package com.example.skillswap.dto;

import com.example.skillswap.entity.SkillOffer;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class SkillOfferResponse {

    private Long id;
    private Long memberId;
    private String memberName;
    private String skillName;
    private String description;
    private BigDecimal availableHours;
    private boolean active;
    private LocalDateTime createdAt;

    public static SkillOfferResponse from(SkillOffer offer) {
        SkillOfferResponse response = new SkillOfferResponse();
        response.setId(offer.getId());
        response.setMemberId(offer.getMember().getId());
        response.setMemberName(offer.getMember().getName());
        response.setSkillName(offer.getSkillName());
        response.setDescription(offer.getDescription());
        response.setAvailableHours(offer.getAvailableHours());
        response.setActive(offer.isActive());
        response.setCreatedAt(offer.getCreatedAt());
        return response;
    }
}
