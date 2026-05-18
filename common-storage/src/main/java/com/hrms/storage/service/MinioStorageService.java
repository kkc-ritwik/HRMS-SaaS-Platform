package com.hrms.storage.service;

import com.hrms.storage.config.StorageProperties;
import com.hrms.storage.model.StoredFile;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private static final String URI_PREFIX = "s3://";
    private final MinioClient client;
    private final StorageProperties props;
    private final Tika tika = new Tika();

    @Override
    public StoredFile upload(String tenantId, String folder, String originalFilename,
                             String contentType, InputStream content, long sizeBytes) {
        if (sizeBytes > props.getMaxUploadSize()) {
            throw new IllegalArgumentException("File exceeds max upload size of " + props.getMaxUploadSize());
        }
        if (contentType == null || contentType.isBlank()) {
            try {
                byte[] sniff = content.readNBytes(8192);
                contentType = tika.detect(sniff, originalFilename);
                content = new java.io.SequenceInputStream(new ByteArrayInputStream(sniff), content);
            } catch (Exception e) {
                contentType = "application/octet-stream";
            }
        }
        if (!props.getAllowedMimeTypes().isEmpty() && !props.getAllowedMimeTypes().contains(contentType)) {
            throw new IllegalArgumentException("MIME type not allowed: " + contentType);
        }

        String bucket = bucketFor(tenantId);
        ensureBucket(bucket);

        String objectKey = buildObjectKey(folder, originalFilename);
        try {
            ObjectWriteResponse resp = client.putObject(PutObjectArgs.builder()
                    .bucket(bucket).object(objectKey)
                    .stream(content, sizeBytes, -1)
                    .contentType(contentType)
                    .userMetadata(Map.of(
                            "tenant-id", tenantId == null ? "" : tenantId,
                            "original-filename", originalFilename == null ? "" : originalFilename))
                    .build());
            return StoredFile.builder()
                    .bucket(bucket).objectKey(objectKey)
                    .originalFilename(originalFilename).contentType(contentType)
                    .sizeBytes(sizeBytes).etag(resp.etag())
                    .storageUri(URI_PREFIX + bucket + "/" + objectKey)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream download(String storageUri) {
        Loc loc = parse(storageUri);
        try {
            return client.getObject(GetObjectArgs.builder().bucket(loc.bucket).object(loc.key).build());
        } catch (Exception e) {
            throw new RuntimeException("Download failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String presignedDownloadUrl(String storageUri, int expirySeconds) {
        Loc loc = parse(storageUri);
        try {
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET).bucket(loc.bucket).object(loc.key)
                    .expiry(expirySeconds, TimeUnit.SECONDS).build());
        } catch (Exception e) {
            throw new RuntimeException("Presign failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String presignedUploadUrl(String tenantId, String folder, String filename, int expirySeconds) {
        String bucket = bucketFor(tenantId);
        ensureBucket(bucket);
        String key = buildObjectKey(folder, filename);
        try {
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT).bucket(bucket).object(key)
                    .expiry(expirySeconds, TimeUnit.SECONDS).build());
        } catch (Exception e) {
            throw new RuntimeException("Presign-upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String storageUri) {
        Loc loc = parse(storageUri);
        try {
            client.removeObject(RemoveObjectArgs.builder().bucket(loc.bucket).object(loc.key).build());
        } catch (Exception e) {
            throw new RuntimeException("Delete failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String storageUri) {
        try {
            metadata(storageUri);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<StoredFile> listByPrefix(String tenantId, String prefix) {
        String bucket = bucketFor(tenantId);
        List<StoredFile> out = new ArrayList<>();
        try {
            for (Result<Item> r : client.listObjects(ListObjectsArgs.builder()
                    .bucket(bucket).prefix(prefix).recursive(true).build())) {
                Item it = r.get();
                out.add(StoredFile.builder().bucket(bucket).objectKey(it.objectName())
                        .sizeBytes(it.size()).etag(it.etag())
                        .storageUri(URI_PREFIX + bucket + "/" + it.objectName()).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("List failed: " + e.getMessage(), e);
        }
        return out;
    }

    @Override
    public StoredFile metadata(String storageUri) {
        Loc loc = parse(storageUri);
        try {
            StatObjectResponse s = client.statObject(StatObjectArgs.builder().bucket(loc.bucket).object(loc.key).build());
            return StoredFile.builder()
                    .bucket(loc.bucket).objectKey(loc.key)
                    .contentType(s.contentType()).sizeBytes(s.size()).etag(s.etag())
                    .originalFilename(s.userMetadata().getOrDefault("original-filename", loc.key))
                    .storageUri(storageUri).build();
        } catch (Exception e) {
            throw new RuntimeException("Stat failed: " + e.getMessage(), e);
        }
    }

    @Override
    public StoredFile copy(String sourceUri, String tenantId, String destFolder, String destFilename) {
        Loc src = parse(sourceUri);
        String dstBucket = bucketFor(tenantId);
        ensureBucket(dstBucket);
        String dstKey = buildObjectKey(destFolder, destFilename);
        try {
            ObjectWriteResponse resp = client.copyObject(CopyObjectArgs.builder()
                    .bucket(dstBucket).object(dstKey)
                    .source(CopySource.builder().bucket(src.bucket).object(src.key).build())
                    .build());
            return StoredFile.builder().bucket(dstBucket).objectKey(dstKey)
                    .etag(resp.etag()).storageUri(URI_PREFIX + dstBucket + "/" + dstKey).build();
        } catch (Exception e) {
            throw new RuntimeException("Copy failed: " + e.getMessage(), e);
        }
    }

    private String bucketFor(String tenantId) {
        // Sanitize: S3 buckets must be DNS-safe, lowercase, 3-63 chars
        String safe = (tenantId == null ? "default" : tenantId).toLowerCase().replaceAll("[^a-z0-9-]", "-");
        return props.getDefaultBucket() + "-" + safe;
    }

    private void ensureBucket(String bucket) {
        try {
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created bucket {}", bucket);
            }
        } catch (Exception e) {
            throw new RuntimeException("Bucket ensure failed: " + e.getMessage(), e);
        }
    }

    private String buildObjectKey(String folder, String filename) {
        String f = (folder == null || folder.isBlank()) ? "misc" : folder;
        String date = LocalDate.now().toString();
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String safe = filename == null ? "file.bin" : filename.replaceAll("[^A-Za-z0-9._-]", "_");
        return f + "/" + date + "/" + unique + "-" + safe;
    }

    private record Loc(String bucket, String key) {}

    private Loc parse(String uri) {
        if (uri == null || !uri.startsWith(URI_PREFIX)) {
            throw new IllegalArgumentException("Invalid storage URI: " + uri);
        }
        String trimmed = uri.substring(URI_PREFIX.length());
        int slash = trimmed.indexOf('/');
        if (slash <= 0) throw new IllegalArgumentException("Invalid storage URI: " + uri);
        return new Loc(trimmed.substring(0, slash), trimmed.substring(slash + 1));
    }
}
