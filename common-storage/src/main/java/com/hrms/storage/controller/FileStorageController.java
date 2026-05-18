package com.hrms.storage.controller;

import com.hrms.security.model.TenantContext;
import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Generic file upload/download endpoints. Mounted by any service that imports common-storage
 * and exposes /api/v1/files endpoints (services that want isolated file APIs can disable
 * this controller with hrms.storage.controller.enabled=false).
 */
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "hrms.storage.controller.enabled", havingValue = "true", matchIfMissing = true)
public class FileStorageController {

    private final StorageService storage;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StoredFile upload(@RequestParam("file") MultipartFile file,
                             @RequestParam(value = "folder", defaultValue = "misc") String folder) throws IOException {
        return storage.upload(TenantContext.get(), folder, file.getOriginalFilename(),
                file.getContentType(), file.getInputStream(), file.getSize());
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> download(@RequestParam("uri") String uri) {
        StoredFile meta = storage.metadata(uri);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + meta.getOriginalFilename() + "\"")
                .contentType(MediaType.parseMediaType(
                        meta.getContentType() == null ? "application/octet-stream" : meta.getContentType()))
                .contentLength(meta.getSizeBytes())
                .body(new InputStreamResource(storage.download(uri)));
    }

    @GetMapping("/presigned-download")
    public Map<String, String> presignedDownload(@RequestParam("uri") String uri,
                                                  @RequestParam(value = "expirySeconds", defaultValue = "3600") int exp) {
        return Map.of("url", storage.presignedDownloadUrl(uri, exp));
    }

    @PostMapping("/presigned-upload")
    public Map<String, String> presignedUpload(@RequestParam("folder") String folder,
                                                @RequestParam("filename") String filename,
                                                @RequestParam(value = "expirySeconds", defaultValue = "3600") int exp) {
        return Map.of("url", storage.presignedUploadUrl(TenantContext.get(), folder, filename, exp));
    }

    @DeleteMapping
    public void delete(@RequestParam("uri") String uri) { storage.delete(uri); }

    @GetMapping("/metadata")
    public StoredFile metadata(@RequestParam("uri") String uri) { return storage.metadata(uri); }
}
