package com.hrms.corehr.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OrgChartNode {
    private UUID   id;
    private String employeeCode;
    private String displayName;
    private String designation;
    private String department;
    private String photoUrl;
    private String email;

    @Builder.Default
    private List<OrgChartNode> children = new ArrayList<>();
}
