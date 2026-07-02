package com.hrms.engagement.service;

import com.hrms.engagement.entity.*;
import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EngagementService {

    public interface SurveyRepo extends JpaRepository<Survey, UUID> {
        Page<Survey> findByTenantIdAndStatus(String tenantId, Survey.Status s, Pageable p);
        List<Survey> findByTenantId(String tenantId);
    }
    public interface SurveyResponseRepo extends JpaRepository<SurveyResponse, UUID> {
        List<SurveyResponse> findByTenantIdAndSurveyId(String tenantId, UUID surveyId);
    }
    public interface PollRepo extends JpaRepository<Poll, UUID> {
        Page<Poll> findByTenantId(String tenantId, Pageable p);
    }
    public interface PollVoteRepo extends JpaRepository<PollVote, UUID> {
        List<PollVote> findByTenantIdAndPollId(String tenantId, UUID pollId);
    }
    public interface KudosRepo extends JpaRepository<Kudos, UUID> {
        Page<Kudos> findByTenantIdAndRecipientId(String tenantId, UUID recipientId, Pageable p);
        Page<Kudos> findByTenantIdAndIsPublicTrueOrderByCreatedAtDesc(String tenantId, Pageable p);
    }

    private final SurveyRepo surveys;
    private final SurveyResponseRepo responses;
    private final PollRepo polls;
    private final PollVoteRepo votes;
    private final KudosRepo kudos;
    private final EventPublisher events;

    // ── Surveys ──────────────────────────────────────────────────────────────
    public List<Survey> listSurveys() { return surveys.findByTenantId(TenantContext.get()); }
    public List<Poll> listPolls() { return polls.findByTenantId(TenantContext.get(), Pageable.unpaged()).getContent(); }

    @Transactional public Survey createSurvey(Survey s) {
        s.setTenantId(TenantContext.get()); if (s.getStatus()==null) s.setStatus(Survey.Status.DRAFT); return surveys.save(s);
    }
    @Transactional public Survey launch(UUID id) {
        Survey s = surveys.findById(id).orElseThrow(); s.setStatus(Survey.Status.ACTIVE);
        events.publish(Topics.ENGAGEMENT, DomainEvent.of("survey.launched", "engagement",
                s.getTenantId(), id.toString(), "Survey", Map.of("title", s.getTitle())));
        return surveys.save(s);
    }
    @Transactional public SurveyResponse respond(SurveyResponse r) {
        r.setTenantId(TenantContext.get()); return responses.save(r);
    }
    public List<SurveyResponse> responses(UUID surveyId) {
        return responses.findByTenantIdAndSurveyId(TenantContext.get(), surveyId);
    }
    /** Simple eNPS calculator: % promoters (9-10) − % detractors (0-6). */
    public Double computeEnps(UUID surveyId, String questionKey) {
        List<SurveyResponse> all = responses(surveyId);
        if (all.isEmpty()) return null;
        long promoters = 0, detractors = 0;
        for (SurveyResponse r : all) {
            Object v = r.getAnswers() == null ? null : r.getAnswers().get(questionKey);
            if (!(v instanceof Number n)) continue;
            int x = n.intValue();
            if (x >= 9) promoters++;
            else if (x <= 6) detractors++;
        }
        return 100.0 * (promoters - detractors) / all.size();
    }

    // ── Polls ────────────────────────────────────────────────────────────────
    @Transactional public Poll createPoll(Poll p) { p.setTenantId(TenantContext.get()); return polls.save(p); }
    @Transactional public PollVote vote(PollVote v) { v.setTenantId(TenantContext.get()); return votes.save(v); }
    public Map<Integer, Long> pollTally(UUID pollId) {
        Map<Integer, Long> counts = new HashMap<>();
        for (PollVote v : votes.findByTenantIdAndPollId(TenantContext.get(), pollId)) {
            if (v.getSelectedOptions() != null)
                for (Integer o : v.getSelectedOptions()) counts.merge(o, 1L, Long::sum);
        }
        return counts;
    }

    // ── Kudos ────────────────────────────────────────────────────────────────
    @Transactional public Kudos giveKudos(Kudos k) {
        k.setTenantId(TenantContext.get());
        if (k.getPoints() == null) k.setPoints(10);
        Kudos saved = kudos.save(k);
        events.publish(Topics.ENGAGEMENT, DomainEvent.of("kudos.given", "engagement",
                k.getTenantId(), saved.getId().toString(), "Kudos",
                Map.of("recipientId", k.getRecipientId(), "value", k.getValue() == null ? "" : k.getValue())));
        return saved;
    }
    public Page<Kudos> publicFeed(Pageable p) {
        return kudos.findByTenantIdAndIsPublicTrueOrderByCreatedAtDesc(TenantContext.get(), p);
    }
    public Page<Kudos> received(UUID recipientId, Pageable p) {
        return kudos.findByTenantIdAndRecipientId(TenantContext.get(), recipientId, p);
    }
}
