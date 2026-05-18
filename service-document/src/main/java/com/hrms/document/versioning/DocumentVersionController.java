package com.hrms.document.versioning;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents/{documentId}/versions")
@RequiredArgsConstructor
public class DocumentVersionController {

    private final DocumentVersionService svc;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentVersion upload(@PathVariable UUID documentId,
                                   @RequestParam("file") MultipartFile file,
                                   @RequestParam(required = false) String changeSummary,
                                   @RequestParam UUID uploadedBy) throws IOException {
        return svc.uploadNewVersion(documentId, file, changeSummary, uploadedBy);
    }

    @GetMapping
    public List<DocumentVersion> history(@PathVariable UUID documentId) { return svc.history(documentId); }

    @PostMapping("/{versionNumber}/revert")
    public DocumentVersion revert(@PathVariable UUID documentId, @PathVariable int versionNumber) {
        return svc.revertTo(documentId, versionNumber);
    }
}
