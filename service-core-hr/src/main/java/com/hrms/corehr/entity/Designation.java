package com.hrms.corehr.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "designations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Designation extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "grade", length = 20)
    private String grade;

    @Column(name = "band", length = 20)
    private String band;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
