package com.hrms.onboarding.preboard;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/onboarding/preboard")
@RequiredArgsConstructor
public class PreOnboardingController {

    private final PreOnboardingService svc;

    // HR creates / re-invites
    @PostMapping("/invite")
    public PreOnboardingPortal invite(@RequestParam UUID candidateId,
                                       @RequestParam UUID offerId,
                                       @RequestParam String name,
                                       @RequestParam String email,
                                       @RequestParam(required = false) LocalDate proposedJoinDate) {
        return svc.createForCandidate(candidateId, offerId, name, email, proposedJoinDate);
    }

    // Candidate-facing endpoints (token-based)
    @GetMapping("/portal/{token}") public PreOnboardingPortal view(@PathVariable String token) { return svc.byToken(token); }

    @PostMapping("/portal/{token}/documents")
    public PreOnboardingPortal upload(@PathVariable String token,
                                       @RequestParam String documentType,
                                       @RequestParam String storageUri) {
        return svc.uploadDocument(token, documentType, storageUri);
    }
    @PostMapping("/portal/{token}/details")
    public PreOnboardingPortal saveDetails(@PathVariable String token, @RequestBody Map<String, Object> details) {
        return svc.savePersonalDetails(token, details);
    }
    @PostMapping("/portal/{token}/handbook-ack")
    public PreOnboardingPortal ackHandbook(@PathVariable String token) { return svc.acknowledgeHandbook(token); }
    @PostMapping("/portal/{token}/complete")
    public PreOnboardingPortal complete(@PathVariable String token) { return svc.complete(token); }
}
