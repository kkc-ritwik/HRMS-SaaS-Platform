package com.hrms.corehr.dto;

import com.hrms.corehr.entity.Employee;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EmployeeDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank @Size(max = 100) private String firstName;
        @Size(max = 100)           private String middleName;
        @NotBlank @Size(max = 100) private String lastName;
        @NotBlank @Email           private String email;
        @Email                     private String workEmail;
        @Size(max = 30)            private String phone;
        @Size(max = 20)            private String phoneSecondary;
        private LocalDate dateOfBirth;
        private Employee.Gender gender;
        private Employee.MaritalStatus maritalStatus;
        @Size(max = 100) private String nationality;
        private UUID departmentId;
        private UUID designationId;
        private UUID locationId;
        private UUID managerId;
        private UUID secondaryManagerId;
        @NotNull private Employee.EmploymentType employmentType;
        @NotNull private LocalDate joinDate;
        private Integer noticePeriodDays;
        private Map<String, Object> customFields;
        private UUID userId;
        // Statutory
        @Size(max = 10)  private String panNumber;
        @Size(max = 12)  private String aadharNumber;
        @Size(max = 12)  private String uanNumber;
        // Banking
        @Size(max = 100) private String bankName;
        @Size(max = 30)  private String bankAccountNumber;
        @Size(max = 11)  private String ifscCode;
        // Supplemental
        @Size(max = 5)   private String bloodGroup;
    }

    @Getter @Setter
    public static class UpdateRequest {
        @Size(max = 100) private String firstName;
        @Size(max = 100) private String middleName;
        @Size(max = 100) private String lastName;
        @Email           private String workEmail;
        @Size(max = 30)  private String phone;
        @Size(max = 20)  private String phoneSecondary;
        private LocalDate dateOfBirth;
        private Employee.Gender gender;
        private Employee.MaritalStatus maritalStatus;
        @Size(max = 100) private String nationality;
        private String profilePictureUrl;
        private UUID departmentId;
        private UUID designationId;
        private UUID locationId;
        private UUID managerId;
        private UUID secondaryManagerId;
        private UUID shiftId;
        private UUID payGradeId;
        private Employee.EmploymentType employmentType;
        private Employee.EmploymentStatus employmentStatus;
        private LocalDate confirmationDate;
        private LocalDate probationEndDate;
        private LocalDate exitDate;
        private LocalDate resignationDate;
        private LocalDate lastWorkingDate;
        private Integer noticePeriodDays;
        private Map<String, Object> customFields;
        // Statutory
        @Size(max = 10)  private String panNumber;
        @Size(max = 12)  private String aadharNumber;
        @Size(max = 12)  private String uanNumber;
        @Size(max = 17)  private String esiNumber;
        // Banking
        @Size(max = 100) private String bankName;
        @Size(max = 30)  private String bankAccountNumber;
        @Size(max = 11)  private String ifscCode;
        @Size(max = 100) private String bankBranch;
        // Supplemental
        @Size(max = 5)   private String bloodGroup;
        @Size(max = 1000) private String aboutMe;
        @Size(max = 50)  private String costCenterCode;
        private String tags;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID id;
        private String tenantId;
        private UUID userId;
        private String employeeCode;
        private String firstName;
        private String middleName;
        private String lastName;
        private String fullName;
        private String displayName;
        private String email;
        private String workEmail;
        private String phone;
        private String phoneSecondary;
        private LocalDate dateOfBirth;
        private Employee.Gender gender;
        private Employee.MaritalStatus maritalStatus;
        private String nationality;
        private String profilePictureUrl;
        private String bloodGroup;
        private String aboutMe;
        private UUID departmentId;
        private String departmentName;
        private UUID designationId;
        private String designationName;
        private UUID locationId;
        private String locationName;
        private UUID managerId;
        private String managerName;
        private UUID secondaryManagerId;
        private UUID shiftId;
        private UUID payGradeId;
        private Employee.EmploymentType employmentType;
        private Employee.EmploymentStatus employmentStatus;
        private LocalDate joinDate;
        private LocalDate confirmationDate;
        private LocalDate probationEndDate;
        private LocalDate exitDate;
        private LocalDate resignationDate;
        private LocalDate lastWorkingDate;
        private Integer noticePeriodDays;
        private String costCenterCode;
        private String tags;
        private Map<String, Object> customFields;
        // Statutory
        private String panNumber;
        private String aadharNumber;
        private String uanNumber;
        private String esiNumber;
        // Banking
        private String bankName;
        private String bankAccountNumber;
        private String ifscCode;
        private String bankBranch;
        private List<AddressInfo> addresses;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Getter @Setter @Builder
    public static class ListItem {
        private UUID id;
        private String employeeCode;
        private String fullName;
        private String displayName;
        private String email;
        private String departmentName;
        private String designationName;
        private String locationName;
        private Employee.EmploymentStatus employmentStatus;
        private LocalDate joinDate;
    }

    @Getter @Setter @Builder
    public static class DirectoryItem {
        private UUID id;
        private String employeeCode;
        private String fullName;
        private String displayName;
        private String emailWork;
        private String phonePrimary;
        private String departmentName;
        private String designationName;
        private String locationName;
        private String photoUrl;
    }

    @Getter @Setter @Builder
    public static class AddressInfo {
        private UUID id;
        private String addressType;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String country;
        private String postalCode;
    }

    @Getter @Setter @Builder
    public static class LifecycleEventResponse {
        private UUID id;
        private String eventType;
        private LocalDate eventDate;
        private LocalDate effectiveDate;
        private String oldValue;
        private String newValue;
        private String remarks;
        private String reason;
        private String comments;
        private UUID approvedBy;
        private String performedBy;
        private Instant createdAt;
    }

    @Getter @Setter
    public static class LifecycleEvent {
        @NotBlank
        private String eventType;

        @NotNull
        private LocalDate effectiveDate;

        private String reason;
        private String comments;
        private String oldValueJson;
        private String newValueJson;
    }
}
