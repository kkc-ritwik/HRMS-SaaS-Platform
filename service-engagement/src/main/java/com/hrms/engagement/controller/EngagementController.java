package com.hrms.engagement.controller;

import com.hrms.engagement.entity.*;
import com.hrms.engagement.service.EngagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/engagement")
@RequiredArgsConstructor
public class EngagementController {

    private final EngagementService svc;

    // Surveys
    @PostMapping("/surveys") public Survey createSurvey(@RequestBody Survey s) { return svc.createSurvey(s); }
    @PostMapping("/surveys/{id}/launch") public Survey launch(@PathVariable UUID id) { return svc.launch(id); }
    @PostMapping("/surveys/{id}/responses") public SurveyResponse respond(@PathVariable UUID id, @RequestBody SurveyResponse r) {
        r.setSurveyId(id); return svc.respond(r);
    }
    @GetMapping("/surveys/{id}/responses") public List<SurveyResponse> responses(@PathVariable UUID id) {
        return svc.responses(id);
    }
    @GetMapping("/surveys/{id}/enps") public Map<String, Object> enps(@PathVariable UUID id,
                                                                       @RequestParam(defaultValue = "score") String key) {
        Double v = svc.computeEnps(id, key);
        return Map.of("enps", v == null ? -1 : v, "questionKey", key);
    }

    // Polls
    @PostMapping("/polls") public Poll createPoll(@RequestBody Poll p) { return svc.createPoll(p); }
    @PostMapping("/polls/{id}/vote") public PollVote vote(@PathVariable UUID id, @RequestBody PollVote v) {
        v.setPollId(id); return svc.vote(v);
    }
    @GetMapping("/polls/{id}/tally") public Map<Integer, Long> tally(@PathVariable UUID id) { return svc.pollTally(id); }

    // Kudos
    @PostMapping("/kudos") public Kudos give(@RequestBody Kudos k) { return svc.giveKudos(k); }
    @GetMapping("/kudos/feed") public Page<Kudos> feed(Pageable p) { return svc.publicFeed(p); }
    @GetMapping("/kudos/received") public Page<Kudos> received(@RequestParam UUID recipientId, Pageable p) {
        return svc.received(recipientId, p);
    }
}
