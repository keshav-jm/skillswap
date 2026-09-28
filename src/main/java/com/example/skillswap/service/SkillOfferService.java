package com.example.skillswap.service;

import com.example.skillswap.dto.CreateSkillOfferRequest;
import com.example.skillswap.dto.SkillOfferResponse;
import com.example.skillswap.entity.Member;
import com.example.skillswap.entity.SkillOffer;
import com.example.skillswap.exception.ResourceNotFoundException;
import com.example.skillswap.repository.SkillOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SkillOfferService {

    private final SkillOfferRepository skillOfferRepository;
    private final MemberService memberService;

    public SkillOfferService(SkillOfferRepository skillOfferRepository, MemberService memberService) {
        this.skillOfferRepository = skillOfferRepository;
        this.memberService = memberService;
    }

    @Transactional
    public SkillOfferResponse createSkillOffer(CreateSkillOfferRequest request) {
        Member member = memberService.findMemberOrThrow(request.getMemberId());
        SkillOffer offer = buildSkillOffer(member, request);
        return SkillOfferResponse.from(skillOfferRepository.save(offer));
    }

    private SkillOffer buildSkillOffer(Member member, CreateSkillOfferRequest request) {
        SkillOffer offer = new SkillOffer();
        offer.setMember(member);
        offer.setSkillName(request.getSkillName());
        offer.setDescription(request.getDescription());
        offer.setAvailableHours(request.getAvailableHours());
        offer.setActive(true);
        return offer;
    }

    @Transactional(readOnly = true)
    public List<SkillOfferResponse> getAllSkillOffers() {
        return skillOfferRepository.findAll()
                .stream()
                .map(SkillOfferResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SkillOfferResponse getSkillOfferById(Long id) {
        return SkillOfferResponse.from(findSkillOfferOrThrow(id));
    }

    @Transactional
    public SkillOfferResponse updateSkillOffer(Long id, CreateSkillOfferRequest request) {
        SkillOffer offer = findSkillOfferOrThrow(id);
        applyUpdate(offer, request);
        return SkillOfferResponse.from(skillOfferRepository.save(offer));
    }

    private void applyUpdate(SkillOffer offer, CreateSkillOfferRequest request) {
        offer.setSkillName(request.getSkillName());
        offer.setDescription(request.getDescription());
        offer.setAvailableHours(request.getAvailableHours());
    }

    @Transactional
    public void deleteSkillOffer(Long id) {
        SkillOffer offer = findSkillOfferOrThrow(id);
        offer.setActive(false); // Soft delete
        skillOfferRepository.save(offer);
    }

    // Internal helper used by other services
    public SkillOffer findSkillOfferOrThrow(Long id) {
        return skillOfferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill offer not found with id: " + id));
    }
}
