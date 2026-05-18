package com.hrms.clients.corehr.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeDto {
    private UUID id;
    private String tenantId;
    private UUID userId;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String displayName;
    private String email;
    private String workEmail;
    private String phone;
    private UUID departmentId;
    private UUID designationId;
    private UUID locationId;
    private UUID managerId;
    private String employmentStatus;
    private String employmentType;
    private LocalDate joinDate;
    private LocalDate dateOfBirth;
    private LocalDate exitDate;
    private String costCenterCode;
}
