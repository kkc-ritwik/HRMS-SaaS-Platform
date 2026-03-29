package com.hrms.performance.entity;

import lombok.*;

import java.time.LocalDate;

/** POJO serialised into the one_on_ones.action_items JSONB column. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActionItem {
    private String    item;
    private String    owner;       // employeeId string
    private LocalDate dueDate;
    private boolean   completed;
}
