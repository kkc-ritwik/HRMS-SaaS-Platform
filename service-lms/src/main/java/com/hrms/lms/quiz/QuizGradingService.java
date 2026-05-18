package com.hrms.lms.quiz;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Auto-grades a quiz attempt: walks the questions, scores each one, computes final %,
 * marks passed/failed against the assessment's passingScore.
 */
@Service
@RequiredArgsConstructor
public class QuizGradingService {

    public interface QuestionRepo extends JpaRepository<Question, UUID> {
        List<Question> findByAssessmentIdOrderByDisplayOrderAsc(UUID assessmentId);
    }

    private final QuestionRepo questions;

    @Transactional
    public QuizAttempt grade(QuizAttempt attempt, int passingPercent) {
        List<Question> qs = questions.findByAssessmentIdOrderByDisplayOrderAsc(attempt.getAssessmentId());
        Map<String, Object> answers = attempt.getAnswers() == null ? Map.of() : attempt.getAnswers();
        Map<String, Object> grading = new LinkedHashMap<>();

        int earned = 0;
        int max = 0;
        for (Question q : qs) {
            int qMax = q.getPoints() == null ? 1 : q.getPoints();
            max += qMax;
            int qEarned = scoreQuestion(q, answers.get(q.getId().toString()));
            earned += qEarned;
            grading.put(q.getId().toString(), Map.of(
                    "earned", qEarned, "max", qMax, "correct", qEarned == qMax));
        }

        BigDecimal pct = max == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(100.0 * earned / max).setScale(2, RoundingMode.HALF_UP);
        attempt.setScore(BigDecimal.valueOf(earned));
        attempt.setMaxScore(BigDecimal.valueOf(max));
        attempt.setScorePercent(pct);
        attempt.setPassed(pct.doubleValue() >= passingPercent);
        attempt.setGrading(grading);
        return attempt;
    }

    private int scoreQuestion(Question q, Object answer) {
        if (answer == null) return 0;
        int p = q.getPoints() == null ? 1 : q.getPoints();
        switch (q.getType()) {
            case MCQ, TRUE_FALSE -> {
                String picked = answer.toString();
                if (q.getOptions() != null) {
                    for (Question.Option o : q.getOptions()) {
                        if (Boolean.TRUE.equals(o.getIsCorrect()) && o.getId().equals(picked)) return p;
                    }
                }
                return 0;
            }
            case MSQ -> {
                if (!(answer instanceof Collection<?> picked)) return 0;
                Set<String> pickedIds = new HashSet<>();
                for (Object o : picked) pickedIds.add(o.toString());
                Set<String> correctIds = new HashSet<>();
                if (q.getOptions() != null) {
                    for (Question.Option o : q.getOptions())
                        if (Boolean.TRUE.equals(o.getIsCorrect())) correctIds.add(o.getId());
                }
                return pickedIds.equals(correctIds) ? p : 0;
            }
            case SHORT_ANSWER -> {
                String text = answer.toString().trim().toLowerCase();
                if (q.getCorrectAnswers() == null) return 0;
                for (String c : q.getCorrectAnswers()) {
                    if (c != null && c.trim().toLowerCase().equals(text)) return p;
                }
                return 0;
            }
            case LONG_ANSWER -> { return 0; /* requires manual grading */ }
        }
        return 0;
    }
}
