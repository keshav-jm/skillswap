package com.example.skillswap;

import com.example.skillswap.dto.*;
import com.example.skillswap.entity.CreditTransactionType;
import com.example.skillswap.entity.SessionStatus;
import com.example.skillswap.exception.*;
import com.example.skillswap.repository.MemberRepository;
import com.example.skillswap.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class SkillSwapApplicationTests {

    @Autowired
    private MemberService memberService;

    @Autowired
    private SkillOfferService skillOfferService;

    @Autowired
    private SessionRequestService sessionRequestService;

    @Autowired
    private CreditLedgerService creditLedgerService;

    @Autowired
    private MemberRepository memberRepository;

    private Long arunId;
    private Long rahulId;
    private Long javaSkillId;
    private Long cookingSkillId;

    @BeforeEach
    void setUp() {
        arunId = createMember("Arun", "arun@test.com");
        rahulId = createMember("Rahul", "rahul@test.com");
        javaSkillId = createSkill(arunId, "Java Programming", "Learn Java", "20.00");
        cookingSkillId = createSkill(rahulId, "Cooking", "Learn cooking", "10.00");
    }

    private Long createMember(String name, String email) {
        CreateMemberRequest req = new CreateMemberRequest();
        req.setName(name);
        req.setEmail(email);
        req.setPassword("password");
        return memberService.createMember(req).getId();
    }

    private Long createSkill(Long memberId, String skillName, String description, String hours) {
        CreateSkillOfferRequest req = new CreateSkillOfferRequest();
        req.setMemberId(memberId);
        req.setSkillName(skillName);
        req.setDescription(description);
        req.setAvailableHours(new BigDecimal(hours));
        return skillOfferService.createSkillOffer(req).getId();
    }

    private void setCredits(Long memberId, BigDecimal amount) {
        memberRepository.findById(memberId).ifPresent(member -> {
            member.setCreditBalance(amount);
            memberRepository.save(member);
        });
    }

    private SessionRequestResponse createSessionReq(Long requesterId, Long skillOfferId, String hours) {
        CreateSessionRequest req = new CreateSessionRequest();
        req.setRequesterId(requesterId);
        req.setSkillOfferId(skillOfferId);
        req.setRequestedHours(new BigDecimal(hours));
        return sessionRequestService.createSessionRequest(req);
    }

    private SessionRequestResponse completeSessionReq(Long sessionId, Long providerId, String hours) {
        CompleteSessionRequest req = new CompleteSessionRequest();
        req.setProviderId(providerId);
        req.setDeliveredHours(new BigDecimal(hours));
        return sessionRequestService.completeSession(sessionId, req);
    }

    // TEST 1: Member creation
    @Test
    @DisplayName("Test 1: Member can be created successfully")
    void testMemberCreation() {
        MemberResponse response = memberService.getMemberById(arunId);
        assertEquals("Arun", response.getName());
        assertEquals("arun@test.com", response.getEmail());
        assertNotNull(response.getId());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getCreditBalance()));
    }

    // TEST 2: Skill creation
    @Test
    @DisplayName("Test 2: Skill offer can be created")
    void testSkillCreation() {
        SkillOfferResponse response = skillOfferService.getSkillOfferById(javaSkillId);
        assertEquals("Java Programming", response.getSkillName());
        assertEquals(arunId, response.getMemberId());
        assertTrue(response.isActive());
    }

    // TEST 3: Session request creation
    @Test
    @DisplayName("Test 3: Session request created with sufficient credits")
    void testSessionRequestCreation() {
        setCredits(rahulId, new BigDecimal("5.00"));
        SessionRequestResponse response = createSessionReq(rahulId, javaSkillId, "2.00");
        assertNotNull(response.getId());
        assertEquals(SessionStatus.REQUESTED, response.getStatus());
        assertEquals(rahulId, response.getRequesterId());
        assertEquals(arunId, response.getProviderId());
    }

    // TEST 4: Insufficient credits rejected
    @Test
    @DisplayName("Test 4: Session request rejected — insufficient credits")
    void testInsufficientCreditsRejection() {
        assertThrows(InsufficientCreditsException.class,
                () -> createSessionReq(rahulId, javaSkillId, "5.00"));
    }

    // TEST 5: Inactive skill rejected
    @Test
    @DisplayName("Test 5: Session request rejected for inactive skill")
    void testInactiveSkillRejection() {
        skillOfferService.deleteSkillOffer(javaSkillId);
        setCredits(rahulId, new BigDecimal("10.00"));
        assertThrows(InvalidOperationException.class,
                () -> createSessionReq(rahulId, javaSkillId, "2.00"));
    }

    // TEST 6: Provider confirms valid session — status COMPLETED
    @Test
    @DisplayName("Test 6: Provider confirms session — status is COMPLETED")
    void testProviderConfirmsSessionStatus() {
        setCredits(rahulId, new BigDecimal("10.00"));
        SessionRequestResponse created = createSessionReq(rahulId, javaSkillId, "3.00");
        SessionRequestResponse completed = completeSessionReq(created.getId(), arunId, "2.00");
        assertEquals(SessionStatus.COMPLETED, completed.getStatus());
        assertNotNull(completed.getCompletedAt());
    }

    // TEST 7: Credits deducted from requester
    @Test
    @DisplayName("Test 7: Credits deducted from requester after completion")
    void testCreditsDeductedFromRequester() {
        setCredits(rahulId, new BigDecimal("10.00"));
        SessionRequestResponse created = createSessionReq(rahulId, javaSkillId, "3.00");
        completeSessionReq(created.getId(), arunId, "2.00");
        assertEquals(0, new BigDecimal("8.00").compareTo(memberService.getMemberBalance(rahulId)));
    }

    // TEST 8: Credits added to provider
    @Test
    @DisplayName("Test 8: Credits added to provider after completion")
    void testCreditsAddedToProvider() {
        setCredits(rahulId, new BigDecimal("10.00"));
        SessionRequestResponse created = createSessionReq(rahulId, javaSkillId, "3.00");
        completeSessionReq(created.getId(), arunId, "2.00");
        assertEquals(0, new BigDecimal("2.00").compareTo(memberService.getMemberBalance(arunId)));
    }

    // TEST 9: SPENT ledger entry created for requester
    @Test
    @DisplayName("Test 9: SPENT ledger entry created for requester")
    void testSpentLedgerCreated() {
        setCredits(rahulId, new BigDecimal("10.00"));
        SessionRequestResponse created = createSessionReq(rahulId, javaSkillId, "3.00");
        completeSessionReq(created.getId(), arunId, "2.00");
        List<CreditLedgerResponse> rahulLedger = creditLedgerService.getLedgerForMember(rahulId);
        assertEquals(1, rahulLedger.size());
        assertEquals(CreditTransactionType.SPENT, rahulLedger.get(0).getTransactionType());
        assertEquals(0, new BigDecimal("2.00").compareTo(rahulLedger.get(0).getAmount()));
    }

    // TEST 10: EARNED ledger entry created for provider
    @Test
    @DisplayName("Test 10: EARNED ledger entry created for provider")
    void testEarnedLedgerCreated() {
        setCredits(rahulId, new BigDecimal("10.00"));
        SessionRequestResponse created = createSessionReq(rahulId, javaSkillId, "3.00");
        completeSessionReq(created.getId(), arunId, "2.00");
        List<CreditLedgerResponse> arunLedger = creditLedgerService.getLedgerForMember(arunId);
        assertEquals(1, arunLedger.size());
        assertEquals(CreditTransactionType.EARNED, arunLedger.get(0).getTransactionType());
        assertEquals(0, new BigDecimal("2.00").compareTo(arunLedger.get(0).getAmount()));
    }

    // TEST 11: Delivered hours > requested hours rejected
    @Test
    @DisplayName("Test 11: Delivered hours exceeding requested hours rejected")
    void testDeliveredExceedsRequested() {
        setCredits(rahulId, new BigDecimal("10.00"));
        SessionRequestResponse created = createSessionReq(rahulId, javaSkillId, "2.00");
        assertThrows(InvalidOperationException.class,
                () -> completeSessionReq(created.getId(), arunId, "3.00"));
    }

    // TEST 12: Duplicate completion rejected
    @Test
    @DisplayName("Test 12: Duplicate completion rejected")
    void testDuplicateCompletionRejected() {
        setCredits(rahulId, new BigDecimal("10.00"));
        SessionRequestResponse created = createSessionReq(rahulId, javaSkillId, "2.00");
        completeSessionReq(created.getId(), arunId, "2.00");
        assertThrows(InvalidSessionStateException.class,
                () -> completeSessionReq(created.getId(), arunId, "2.00"));
    }

    // TEST 13: Wrong provider cannot complete
    @Test
    @DisplayName("Test 13: Non-owner provider cannot complete a session")
    void testWrongProviderRejected() {
        setCredits(rahulId, new BigDecimal("10.00"));
        SessionRequestResponse created = createSessionReq(rahulId, javaSkillId, "2.00");
        assertThrows(UnauthorizedProviderException.class,
                () -> completeSessionReq(created.getId(), rahulId, "1.00"));
    }

    // BONUS: Self-booking rejected
    @Test
    @DisplayName("Bonus: Requester cannot book their own skill")
    void testSelfBookingRejected() {
        setCredits(arunId, new BigDecimal("10.00"));
        assertThrows(InvalidOperationException.class,
                () -> createSessionReq(arunId, javaSkillId, "1.00"));
    }
}
