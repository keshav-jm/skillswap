package com.example.skillswap.controller;

import com.example.skillswap.dto.CreditLedgerResponse;
import com.example.skillswap.service.CreditLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/credits")
@Tag(name = "Credits", description = "Credit ledger and balance APIs")
public class CreditLedgerController {

    private final CreditLedgerService creditLedgerService;

    public CreditLedgerController(CreditLedgerService creditLedgerService) {
        this.creditLedgerService = creditLedgerService;
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get all credit transactions for a member")
    public ResponseEntity<List<CreditLedgerResponse>> getLedger(@PathVariable Long memberId) {
        return ResponseEntity.ok(creditLedgerService.getLedgerForMember(memberId));
    }

    @GetMapping("/member/{memberId}/balance")
    @Operation(summary = "Get credit balance for a member")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable Long memberId) {
        BigDecimal balance = creditLedgerService.getMemberBalance(memberId);
        return ResponseEntity.ok(Map.of("memberId", memberId, "creditBalance", balance));
    }
}
