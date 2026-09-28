package com.example.skillswap.service;

import com.example.skillswap.dto.CompleteSessionRequest;
import com.example.skillswap.dto.CreateSessionRequest;
import com.example.skillswap.dto.SessionRequestResponse;
import com.example.skillswap.entity.*;
import com.example.skillswap.exception.*;
import com.example.skillswap.repository.CreditLedgerRepository;
import com.example.skillswap.repository.MemberRepository;
import com.example.skillswap.repository.SessionRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionRequestService {

    private final SessionRequestRepository sessionRequestRepository;
    private final MemberRepository memberRepository;
    private final CreditLedgerRepository creditLedgerRepository;
    private final MemberService memberService;
    private final SkillOfferService skillOfferService;

    public SessionRequestService(
            SessionRequestRepository sessionRequestRepository,
            MemberRepository memberRepository,
            CreditLedgerRepository creditLedgerRepository,
            MemberService memberService,
            SkillOfferService skillOfferService) {
        this.sessionRequestRepository = sessionRequestRepository;
        this.memberRepository = memberRepository;
        this.creditLedgerRepository = creditLedgerRepository;
        this.memberService = memberService;
        this.skillOfferService = skillOfferService;
    }

    // RULE 1,2,8,9: Create session request — no credit transfer here
    @Transactional
    public SessionRequestResponse createSessionRequest(CreateSessionRequest request) {
        Member requester = memberService.findMemberOrThrow(request.getRequesterId());
        SkillOffer skillOffer = skillOfferService.findSkillOfferOrThrow(request.getSkillOfferId());
        validateSessionCreation(requester, skillOffer, request.getRequestedHours());
        SessionRequest savedSession = buildAndSaveSession(requester, skillOffer, request.getRequestedHours());
        return SessionRequestResponse.from(savedSession);
    }

    private void validateSessionCreation(Member requester, SkillOffer skillOffer, BigDecimal hours) {
        if (skillOffer.getMember().getId().equals(requester.getId())) {
            throw new InvalidOperationException("A member cannot request their own skill offer.");
        }
        if (!skillOffer.isActive()) {
            throw new InvalidOperationException("Cannot book a session for an inactive skill offer.");
        }
        if (requester.getCreditBalance().compareTo(hours) < 0) {
            throw new InsufficientCreditsException(
                    "Insufficient credits. Balance: " + requester.getCreditBalance()
                            + ", requested: " + hours + " hours.");
        }
    }

    private SessionRequest buildAndSaveSession(Member requester, SkillOffer skillOffer, BigDecimal hours) {
        SessionRequest sr = new SessionRequest();
        sr.setRequester(requester);
        sr.setSkillOffer(skillOffer);
        sr.setRequestedHours(hours);
        sr.setStatus(SessionStatus.REQUESTED);
        return sessionRequestRepository.save(sr);
    }

    // RULE 3,4,5,6,7,10: Complete session and transfer credits atomically
    @Transactional
    public SessionRequestResponse completeSession(Long sessionId, CompleteSessionRequest request) {
        SessionRequest sr = findSessionOrThrow(sessionId);
        validateSessionState(sr, sessionId);
        validateProviderOwnership(sr, request.getProviderId());
        BigDecimal deliveredHours = request.getDeliveredHours();
        validateDeliveredHours(deliveredHours, sr.getRequestedHours());
        transferCredits(sr, deliveredHours);
        return SessionRequestResponse.from(finalizeSession(sr, deliveredHours));
    }

    private void validateSessionState(SessionRequest sr, Long sessionId) {
        if (sr.getStatus() == SessionStatus.COMPLETED) {
            throw new InvalidSessionStateException(
                    "Session " + sessionId + " is already completed. Cannot credit twice.");
        }
        if (sr.getStatus() == SessionStatus.REJECTED || sr.getStatus() == SessionStatus.CANCELLED) {
            throw new InvalidSessionStateException(
                    "Session " + sessionId + " is " + sr.getStatus() + " and cannot be completed.");
        }
    }

    private void validateProviderOwnership(SessionRequest sr, Long providerId) {
        Long actualProviderId = sr.getSkillOffer().getMember().getId();
        if (!actualProviderId.equals(providerId)) {
            throw new UnauthorizedProviderException(
                    "Only the skill owner (provider ID: " + actualProviderId + ") can complete this session.");
        }
    }

    private void validateDeliveredHours(BigDecimal delivered, BigDecimal requested) {
        if (delivered.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Delivered hours must be greater than zero.");
        }
        if (delivered.compareTo(requested) > 0) {
            throw new InvalidOperationException(
                    "Delivered hours (" + delivered + ") cannot exceed requested hours (" + requested + ").");
        }
    }

    private void transferCredits(SessionRequest sr, BigDecimal deliveredHours) {
        Long requesterId = sr.getRequester().getId();
        Long providerId = sr.getSkillOffer().getMember().getId();
        Member requester = memberRepository.findByIdForUpdate(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found."));
        Member provider = memberRepository.findByIdForUpdate(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found."));
        validateRequesterBalance(requester, deliveredHours);
        applyBalanceChanges(requester, provider, deliveredHours);
        saveLedgerEntries(sr, requester, provider, deliveredHours);
    }

    private void validateRequesterBalance(Member requester, BigDecimal deliveredHours) {
        if (requester.getCreditBalance().compareTo(deliveredHours) < 0) {
            throw new InsufficientCreditsException(
                    "Requester balance (" + requester.getCreditBalance()
                            + ") is insufficient to cover delivered hours (" + deliveredHours + ").");
        }
    }

    private void applyBalanceChanges(Member requester, Member provider, BigDecimal hours) {
        requester.setCreditBalance(requester.getCreditBalance().subtract(hours));
        provider.setCreditBalance(provider.getCreditBalance().add(hours));
        memberRepository.save(requester);
        memberRepository.save(provider);
    }

    private void saveLedgerEntries(SessionRequest sr, Member requester, Member provider, BigDecimal hours) {
        creditLedgerRepository.save(buildLedgerEntry(requester, sr, hours, CreditTransactionType.SPENT,
                "Credits spent for session #" + sr.getId() + " (" + sr.getSkillOffer().getSkillName() + ")"));
        creditLedgerRepository.save(buildLedgerEntry(provider, sr, hours, CreditTransactionType.EARNED,
                "Credits earned for session #" + sr.getId() + " (" + sr.getSkillOffer().getSkillName() + ")"));
    }

    private CreditLedger buildLedgerEntry(Member member, SessionRequest sr, BigDecimal amount,
                                           CreditTransactionType type, String description) {
        CreditLedger entry = new CreditLedger();
        entry.setMember(member);
        entry.setSessionRequest(sr);
        entry.setAmount(amount);
        entry.setTransactionType(type);
        entry.setDescription(description);
        return entry;
    }

    private SessionRequest finalizeSession(SessionRequest sr, BigDecimal deliveredHours) {
        sr.setDeliveredHours(deliveredHours);
        sr.setStatus(SessionStatus.COMPLETED);
        sr.setCompletedAt(LocalDateTime.now());
        return sessionRequestRepository.save(sr);
    }

    // Provider rejects the session request
    @Transactional
    public SessionRequestResponse rejectSession(Long sessionId, Long providerId) {
        SessionRequest sr = findSessionOrThrow(sessionId);
        validateRejectConditions(sr, sessionId, providerId);
        sr.setStatus(SessionStatus.REJECTED);
        return SessionRequestResponse.from(sessionRequestRepository.save(sr));
    }

    private void validateRejectConditions(SessionRequest sr, Long sessionId, Long providerId) {
        if (sr.getStatus() != SessionStatus.REQUESTED) {
            throw new InvalidSessionStateException(
                    "Only REQUESTED sessions can be rejected. Current status: " + sr.getStatus());
        }
        Long actualProviderId = sr.getSkillOffer().getMember().getId();
        if (!actualProviderId.equals(providerId)) {
            throw new UnauthorizedProviderException("Only the skill owner can reject this session.");
        }
    }

    @Transactional(readOnly = true)
    public SessionRequestResponse getSessionRequest(Long id) {
        return SessionRequestResponse.from(findSessionOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<SessionRequestResponse> getAllSessionRequests() {
        return sessionRequestRepository.findAll()
                .stream()
                .map(SessionRequestResponse::from)
                .collect(Collectors.toList());
    }

    private SessionRequest findSessionOrThrow(Long id) {
        return sessionRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session request not found with id: " + id));
    }
}
