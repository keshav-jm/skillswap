package com.example.skillswap.dto;

import com.example.skillswap.entity.CreditLedger;
import com.example.skillswap.entity.CreditTransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreditLedgerResponse {

    private Long id;
    private Long memberId;
    private String memberName;
    private Long sessionRequestId;
    private BigDecimal amount;
    private CreditTransactionType transactionType;
    private String description;
    private LocalDateTime createdAt;

    public static CreditLedgerResponse from(CreditLedger ledger) {
        CreditLedgerResponse response = new CreditLedgerResponse();
        response.setId(ledger.getId());
        response.setMemberId(ledger.getMember().getId());
        response.setMemberName(ledger.getMember().getName());
        response.setSessionRequestId(ledger.getSessionRequest().getId());
        response.setAmount(ledger.getAmount());
        response.setTransactionType(ledger.getTransactionType());
        response.setDescription(ledger.getDescription());
        response.setCreatedAt(ledger.getCreatedAt());
        return response;
    }
}
