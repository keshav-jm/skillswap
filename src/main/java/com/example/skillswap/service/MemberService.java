package com.example.skillswap.service;

import com.example.skillswap.dto.CreateMemberRequest;
import com.example.skillswap.dto.MemberResponse;
import com.example.skillswap.entity.Member;
import com.example.skillswap.exception.InvalidOperationException;
import com.example.skillswap.exception.ResourceNotFoundException;
import com.example.skillswap.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // Create new member
    @Transactional
    public MemberResponse createMember(CreateMemberRequest request) {

        checkEmailNotTaken(request.getEmail());

        Member member = new Member();

        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPassword(request.getPassword());

        // New members start with zero credits
        member.setCreditBalance(BigDecimal.ZERO);

        Member savedMember = memberRepository.save(member);

        return MemberResponse.from(savedMember);
    }

    // Check duplicate email
    private void checkEmailNotTaken(String email) {

        if (memberRepository.existsByEmail(email)) {
            throw new InvalidOperationException(
                    "A member with email '" + email + "' already exists."
            );
        }
    }

    // Get all members
    @Transactional(readOnly = true)
    public List<MemberResponse> getAllMembers() {

        return memberRepository.findAll()
                .stream()
                .map(MemberResponse::from)
                .toList();
    }

    // Get member by ID
    @Transactional(readOnly = true)
    public MemberResponse getMemberById(Long id) {

        return MemberResponse.from(
                findMemberOrThrow(id)
        );
    }

    // Get member's credit balance
    @Transactional(readOnly = true)
    public BigDecimal getMemberBalance(Long id) {

        return findMemberOrThrow(id)
                .getCreditBalance();
    }

    // Find member entity for other services
    @Transactional(readOnly = true)
    public Member findMemberOrThrow(Long id) {

        return memberRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Member not found with id: " + id
                        )
                );
    }
}