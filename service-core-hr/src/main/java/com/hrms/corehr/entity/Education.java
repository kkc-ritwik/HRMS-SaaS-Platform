package com.hrms.corehr.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "education")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Education extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "institution", nullable = false, length = 255)
    private String institution;

    @Column(name = "degree", nullable = false, length = 150)
    private String degree;

    @Column(name = "field_of_study", length = 150)
    private String fieldOfStudy;

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "end_year")
    private Integer endYear;

    @Column(name = "grade", length = 50)
    private String grade;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "year_of_passing")
    private Integer yearOfPassing;

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "document_url", columnDefinition = "TEXT")
    private String documentUrl;
}
