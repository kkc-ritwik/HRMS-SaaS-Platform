package com.hrms.storage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class StoredFile {
    private String bucket;
    private String objectKey;
    private String originalFilename;
    private String contentType;
    private long sizeBytes;
    private String etag;
    /** Internal canonical URL (e.g. s3://bucket/key) — store this in DB, never a presigned URL */
    private String storageUri;
}
