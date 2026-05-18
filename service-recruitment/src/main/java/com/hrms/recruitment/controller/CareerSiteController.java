package com.hrms.recruitment.controller;

import com.hrms.recruitment.parser.ResumeParser;
import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Public, unauthenticated endpoints exposed to the careers website.
 * Used by the front-end careers page (https://yourcompany.com/careers).
 * Tenant context resolved via path param so the same backend can serve multiple companies.
 */
@RestController
@RequestMapping("/api/public/v1/careers/{tenantId}")
@RequiredArgsConstructor
public class CareerSiteController {

    private final ResumeParser parser;
    private final StorageService storage;

    /** Anonymous candidate uploads a resume — we extract fields they can confirm before submitting. */
    @PostMapping(value = "/resume/parse", consumes = "multipart/form-data")
    public Map<String, Object> parseResume(@PathVariable String tenantId,
                                            @RequestParam("file") MultipartFile file) throws IOException {
        ResumeParser.ParsedResume p = parser.parse(file.getInputStream(), file.getOriginalFilename());
        // Persist the raw resume into MinIO so we can reference it on submission
        StoredFile sf = storage.upload(tenantId, "careers-uploads",
                file.getOriginalFilename(), file.getContentType(),
                file.getInputStream(), file.getSize());
        return Map.of(
                "parsed", p,
                "storageUri", sf.getStorageUri());
    }
}
