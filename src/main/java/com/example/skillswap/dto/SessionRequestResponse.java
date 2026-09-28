package com.example.skillswap.dto;

import com.example.skillswap.entity.SessionRequest;
import com.example.skillswap.entity.SessionStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class SessionRequestResponse {

    private Long id;
    private Long requesterId;
    private String requesterName;
    private Long skillOfferId;
    private String skillName;
    private Long providerId;
    private String providerName;
    private BigDecimal requestedHours;
    private BigDecimal deliveredHours;
    private SessionStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;

    public static SessionRequestResponse from(SessionRequest sr) {
        SessionRequestResponse response = new SessionRequestResponse();
        response.setId(sr.getId());
        response.setRequesterId(sr.getRequester().getId());
        response.setRequesterName(sr.getRequester().getName());
        response.setSkillOfferId(sr.getSkillOffer().getId());
        response.setSkillName(sr.getSkillOffer().getSkillName());
        response.setProviderId(sr.getSkillOffer().getMember().getId());
        response.setProviderName(sr.getSkillOffer().getMember().getName());
        response.setRequestedHours(sr.getRequestedHours());
        response.setDeliveredHours(sr.getDeliveredHours());
        response.setStatus(sr.getStatus());
        response.setRequestedAt(sr.getRequestedAt());
        response.setConfirmedAt(sr.getConfirmedAt());
        response.setCompletedAt(sr.getCompletedAt());
        return response;
    }
}
