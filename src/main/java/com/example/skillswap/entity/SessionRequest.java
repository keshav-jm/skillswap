package com.example.skillswap.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "session_requests")
@Getter
@Setter
@NoArgsConstructor
public class SessionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal requestedHours;

    @Column(precision = 10, scale = 2)
    private BigDecimal deliveredHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.REQUESTED;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime confirmedAt;

    private LocalDateTime completedAt;

    /**
     * The member who is requesting the session (will spend credits).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;

    /**
     * The skill being requested. The provider is obtained via skillOffer.getMember().
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_offer_id", nullable = false)
    private SkillOffer skillOffer;

    @PrePersist
    public void prePersist() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
    }
}
