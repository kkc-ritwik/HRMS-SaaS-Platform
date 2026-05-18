package com.hrms.storage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "hrms.storage")
public class StorageProperties {
    /** s3-compatible endpoint (MinIO / AWS S3). e.g. http://localhost:9000 */
    private String endpoint = "http://localhost:9000";
    private String accessKey = "hrms_minio";
    private String secretKey = "hrms_minio_secret";
    private String region = "us-east-1";
    /** Default bucket per tenant if not specified. Buckets are auto-created if missing. */
    private String defaultBucket = "hrms";
    /** Max upload size in bytes. */
    private long maxUploadSize = 50L * 1024 * 1024;
    /** Presigned URL expiry in seconds. */
    private int presignedUrlExpirySeconds = 3600;
    /** Allowed MIME types (empty = allow all). */
    private java.util.Set<String> allowedMimeTypes = java.util.Set.of();
    /** Path style access (true for MinIO, false for AWS). */
    private boolean pathStyleAccess = true;
}
