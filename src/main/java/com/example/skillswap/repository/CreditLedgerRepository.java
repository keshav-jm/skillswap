package com.example.skillswap.repository;

import com.example.skillswap.entity.CreditLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditLedgerRepository extends JpaRepository<CreditLedger, Long> {

    List<CreditLedger> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
