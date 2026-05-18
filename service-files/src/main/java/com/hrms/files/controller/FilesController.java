package com.hrms.files.controller;

import com.hrms.files.entity.*;
import com.hrms.files.service.FilesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/file-vault")
@RequiredArgsConstructor
public class FilesController {

    private final FilesService svc;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileRecord upload(@RequestParam UUID ownerId,
                              @RequestParam(required = false) UUID folderId,
                              @RequestParam("file") MultipartFile file,
                              @RequestParam(required = false) String description) throws IOException {
        return svc.upload(ownerId, folderId, file, description);
    }

    @GetMapping("/mine") public Page<FileRecord> mine(@RequestParam UUID ownerId, Pageable p) { return svc.myFiles(ownerId, p); }

    @PostMapping("/folders") public FileFolder createFolder(@RequestBody FileFolder f) { return svc.createFolder(f); }
    @GetMapping("/folders/children") public List<FileFolder> children(@RequestParam UUID ownerId,
                                                                       @RequestParam(required = false) UUID parentId) {
        return svc.children(ownerId, parentId);
    }

    @PostMapping("/{id}/share") public FileShare share(@PathVariable UUID id, @RequestBody FileShare s) {
        return svc.share(id, s);
    }
    @GetMapping("/{id}/download") public Map<String, String> download(@PathVariable UUID id,
                                                                       @RequestParam(defaultValue = "3600") int expiry) {
        return Map.of("url", svc.presignedDownload(id, expiry));
    }
}
