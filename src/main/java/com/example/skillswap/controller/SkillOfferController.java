package com.example.skillswap.controller;

import com.example.skillswap.dto.CreateSkillOfferRequest;
import com.example.skillswap.dto.SkillOfferResponse;
import com.example.skillswap.service.SkillOfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skills")
@Tag(name = "Skill Offers", description = "Skill offer management APIs")
public class SkillOfferController {

    private final SkillOfferService skillOfferService;

    public SkillOfferController(SkillOfferService skillOfferService) {
        this.skillOfferService = skillOfferService;
    }

    @PostMapping
    @Operation(summary = "Create a new skill offer")
    public ResponseEntity<SkillOfferResponse> createSkillOffer(
            @Valid @RequestBody CreateSkillOfferRequest request) {
        SkillOfferResponse response = skillOfferService.createSkillOffer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all skill offers")
    public ResponseEntity<List<SkillOfferResponse>> getAllSkillOffers() {
        return ResponseEntity.ok(skillOfferService.getAllSkillOffers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a skill offer by ID")
    public ResponseEntity<SkillOfferResponse> getSkillOfferById(@PathVariable Long id) {
        return ResponseEntity.ok(skillOfferService.getSkillOfferById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a skill offer")
    public ResponseEntity<SkillOfferResponse> updateSkillOffer(
            @PathVariable Long id,
            @Valid @RequestBody CreateSkillOfferRequest request) {
        return ResponseEntity.ok(skillOfferService.updateSkillOffer(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate (soft-delete) a skill offer")
    public ResponseEntity<Map<String, String>> deleteSkillOffer(@PathVariable Long id) {
        skillOfferService.deleteSkillOffer(id);
        return ResponseEntity.ok(Map.of("message", "Skill offer deactivated successfully."));
    }
}
