package com.example.skillswap.repository;

import com.example.skillswap.entity.SessionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRequestRepository extends JpaRepository<SessionRequest, Long> {

    List<SessionRequest> findByRequesterId(Long requesterId);

    List<SessionRequest> findBySkillOfferMemberId(Long providerId);
}
