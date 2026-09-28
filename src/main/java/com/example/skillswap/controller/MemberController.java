package com.example.skillswap.controller;

import com.example.skillswap.dto.CreateMemberRequest;
import com.example.skillswap.dto.MemberResponse;
import com.example.skillswap.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Members", description = "Member management APIs")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @Operation(summary = "Create a new member")
    public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody CreateMemberRequest request) {
        MemberResponse response = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all members")
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a member by ID")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Get a member's credit balance")
    public ResponseEntity<Map<String, Object>> getMemberBalance(@PathVariable Long id) {
        BigDecimal balance = memberService.getMemberBalance(id);
        return ResponseEntity.ok(Map.of("memberId", id, "creditBalance", balance));
    }
}
