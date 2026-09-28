package com.example.skillswap.repository;

import com.example.skillswap.entity.SkillOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillOfferRepository extends JpaRepository<SkillOffer, Long> {

    List<SkillOffer> findByMemberId(Long memberId);

    List<SkillOffer> findByActiveTrue();
}
