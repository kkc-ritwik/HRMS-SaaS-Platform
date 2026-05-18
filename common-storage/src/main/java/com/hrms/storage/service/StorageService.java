package com.hrms.storage.service;

import com.hrms.storage.model.StoredFile;

import java.io.InputStream;
import java.util.List;

public interface StorageService {

    StoredFile upload(String tenantId, String folder, String originalFilename, String contentType,
                      InputStream content, long sizeBytes);

    /** Direct download (use sparingly — prefer presigned URLs for large files). */
    InputStream download(String storageUri);

    /** Time-limited URL clients can use directly to download. */
    String presignedDownloadUrl(String storageUri, int expirySeconds);

    /** Time-limited URL clients can PUT to (browser direct-upload pattern). */
    String presignedUploadUrl(String tenantId, String folder, String filename, int expirySeconds);

    void delete(String storageUri);

    boolean exists(String storageUri);

    List<StoredFile> listByPrefix(String tenantId, String prefix);

    StoredFile metadata(String storageUri);

    /** Copy an object (e.g. when promoting a draft document to a finalized location). */
    StoredFile copy(String sourceUri, String tenantId, String destFolder, String destFilename);
}
