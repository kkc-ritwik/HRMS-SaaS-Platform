package com.hrms.mail.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MailAttachment {
    private String filename;
    private String contentType;
    private byte[] content;
    /** If set, attachment is fetched from storage instead of inline. */
    private String storageUri;
}
