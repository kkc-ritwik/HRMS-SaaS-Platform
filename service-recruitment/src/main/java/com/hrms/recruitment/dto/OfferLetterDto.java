package com.hrms.recruitment.dto;

import com.hrms.recruitment.entity.OfferLetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class OfferLetterDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotNull  private UUID       applicationId;
        @NotNull  private BigDecimal offeredCtc;
        @NotBlank private String     offeredTitle;
        private LocalDate            joiningDate;
        private LocalDate            offerExpiryDate;
        private String               templateUsed;
    }

    @Getter @Setter
    public static class RespondRequest {
        /** true = accepted, false = declined */
        @NotNull private Boolean     accepted;
        private String               responseNotes;
    }

    @Getter @Setter @Builder
    public static class Response {
        private UUID                   id;
        private UUID                   applicationId;
        private BigDecimal             offeredCtc;
        private String                 offeredTitle;
        private LocalDate              joiningDate;
        private LocalDate              offerExpiryDate;
        private String                 templateUsed;
        private String                 content;
        private OfferLetter.OfferStatus status;
        private Instant                sentAt;
        private Instant                respondedAt;
        private String                 responseNotes;
        private Instant                createdAt;
        private Instant                updatedAt;
    }
}
