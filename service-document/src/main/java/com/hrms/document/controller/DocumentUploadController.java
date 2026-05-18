package com.hrms.document.controller;

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
import java.util.UUID;

/**
 * Real upload/download for documents. Persists files in object storage and
 * returns the canonical storage URI which clients write to Document.file_url.
 */
@RestController
@RequestMapping("/api/v1/documents/files")
@RequiredArgsConstructor
public class DocumentUploadController {

    private final StorageService storage;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StoredFile upload(@RequestParam("file") MultipartFile file,
                              @RequestParam(value = "employeeId", required = false) UUID employeeId,
                              @RequestParam(value = "documentType", defaultValue = "general") String documentType)
            throws IOException {
        String folder = "employee-docs/" + documentType + (employeeId == null ? "" : "/" + employeeId);
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

    @DeleteMapping
    public void delete(@RequestParam("uri") String uri) { storage.delete(uri); }
}
