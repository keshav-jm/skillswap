package com.example.skillswap.controller;

import com.example.skillswap.dto.CompleteSessionRequest;
import com.example.skillswap.dto.CreateSessionRequest;
import com.example.skillswap.dto.SessionRequestResponse;
import com.example.skillswap.service.SessionRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/session-requests")
@Tag(name = "Session Requests", description = "Session request management APIs")
public class SessionRequestController {

    private final SessionRequestService sessionRequestService;

    public SessionRequestController(SessionRequestService sessionRequestService) {
        this.sessionRequestService = sessionRequestService;
    }

    @PostMapping
    @Operation(summary = "Create a new session request",
               description = "Requester books a session. Credits are NOT transferred yet.")
    public ResponseEntity<SessionRequestResponse> createSessionRequest(
            @Valid @RequestBody CreateSessionRequest request) {
        SessionRequestResponse response = sessionRequestService.createSessionRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all session requests")
    public ResponseEntity<List<SessionRequestResponse>> getAllSessionRequests() {
        return ResponseEntity.ok(sessionRequestService.getAllSessionRequests());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a session request by ID")
    public ResponseEntity<SessionRequestResponse> getSessionRequest(@PathVariable Long id) {
        return ResponseEntity.ok(sessionRequestService.getSessionRequest(id));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Complete a session and transfer credits",
               description = "Provider logs delivered hours. Credits are transferred from requester to provider.")
    public ResponseEntity<SessionRequestResponse> completeSession(
            @PathVariable Long id,
            @Valid @RequestBody CompleteSessionRequest request) {
        return ResponseEntity.ok(sessionRequestService.completeSession(id, request));
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirm a session (alias for complete)")
    public ResponseEntity<SessionRequestResponse> confirmSession(
            @PathVariable Long id,
            @Valid @RequestBody CompleteSessionRequest request) {
        return ResponseEntity.ok(sessionRequestService.completeSession(id, request));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a session request (provider only)")
    public ResponseEntity<SessionRequestResponse> rejectSession(
            @PathVariable Long id,
            @RequestParam Long providerId) {
        return ResponseEntity.ok(sessionRequestService.rejectSession(id, providerId));
    }
}
