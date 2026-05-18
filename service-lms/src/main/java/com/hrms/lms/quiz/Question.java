package com.hrms.lms.quiz;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "lms_questions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Auditable("Question")
@EntityListeners(AuditEntityListener.class)
public class Question extends BaseEntity {

    @Column(name = "assessment_id", nullable = false) private UUID assessmentId;
    @Column(name = "text", nullable = false, length = 2000) private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    private Type type;

    /** For MCQ / MSQ / TRUE_FALSE â€” { id, text, isCorrect }. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "jsonb")
    private List<Option> options;

    /** For SHORT_ANSWER â€” accepted answers (case-insensitive match). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "correct_answers", columnDefinition = "jsonb")
    private List<String> correctAnswers;

    @Column(name = "points", nullable = false) private Integer points = 1;
    @Column(name = "explanation", length = 2000) private String explanation;
    @Column(name = "display_order") private Integer displayOrder;

    public enum Type { MCQ, MSQ, TRUE_FALSE, SHORT_ANSWER, LONG_ANSWER }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Option {
        private String id;
        private String text;
        private Boolean isCorrect;
    }
}
