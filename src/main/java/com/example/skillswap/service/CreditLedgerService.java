package com.example.skillswap.service;

import com.example.skillswap.dto.CreditLedgerResponse;
import com.example.skillswap.exception.ResourceNotFoundException;
import com.example.skillswap.repository.CreditLedgerRepository;
import com.example.skillswap.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreditLedgerService {

    private final CreditLedgerRepository creditLedgerRepository;
    private final MemberRepository memberRepository;

    public CreditLedgerService(CreditLedgerRepository creditLedgerRepository,
                               MemberRepository memberRepository) {
        this.creditLedgerRepository = creditLedgerRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<CreditLedgerResponse> getLedgerForMember(Long memberId) {
        ensureMemberExists(memberId);
        return creditLedgerRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(CreditLedgerResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getMemberBalance(Long memberId) {
        return memberRepository.findById(memberId)
                .map(member -> member.getCreditBalance())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
    }

    private void ensureMemberExists(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
    }
}
