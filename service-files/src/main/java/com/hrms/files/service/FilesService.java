package com.hrms.files.service;

import com.hrms.files.entity.*;
import com.hrms.security.model.TenantContext;
import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FilesService {

    public interface FileRepo extends JpaRepository<FileRecord, UUID> {
        Page<FileRecord> findByTenantIdAndOwnerId(String tenantId, UUID ownerId, Pageable p);
        Page<FileRecord> findByTenantIdAndFolderId(String tenantId, UUID folderId, Pageable p);
    }
    public interface FolderRepo extends JpaRepository<FileFolder, UUID> {
        List<FileFolder> findByTenantIdAndOwnerIdAndParentId(String tenantId, UUID ownerId, UUID parentId);
    }
    public interface ShareRepo extends JpaRepository<FileShare, UUID> {
        List<FileShare> findByTenantIdAndFileId(String tenantId, UUID fileId);
    }

    private final FileRepo files;
    private final FolderRepo folders;
    private final ShareRepo shares;
    private final StorageService storage;

    @Transactional
    public FileRecord upload(UUID ownerId, UUID folderId, MultipartFile mp, String description) throws IOException {
        StoredFile sf = storage.upload(TenantContext.get(),
                "files/" + ownerId, mp.getOriginalFilename(),
                mp.getContentType(), mp.getInputStream(), mp.getSize());
        return files.save(FileRecord.builder()
                .tenantId(TenantContext.get())
                .ownerId(ownerId).folderId(folderId)
                .filename(mp.getOriginalFilename())
                .contentType(mp.getContentType()).sizeBytes(mp.getSize())
                .storageUri(sf.getStorageUri()).checksumSha256(sf.getEtag())
                .description(description).isPublic(false)
                .build());
    }

    @Transactional
    public FileFolder createFolder(FileFolder f) {
        f.setTenantId(TenantContext.get()); return folders.save(f);
    }

    @Transactional
    public FileShare share(UUID fileId, FileShare s) {
        s.setTenantId(TenantContext.get()); s.setFileId(fileId);
        s.setShareToken(UUID.randomUUID().toString().replace("-",""));
        if (s.getExpiresAt() == null) s.setExpiresAt(OffsetDateTime.now().plusDays(30));
        return shares.save(s);
    }

    public Page<FileRecord> myFiles(UUID ownerId, Pageable p) {
        return files.findByTenantIdAndOwnerId(TenantContext.get(), ownerId, p);
    }

    public List<FileFolder> children(UUID ownerId, UUID parentId) {
        return folders.findByTenantIdAndOwnerIdAndParentId(TenantContext.get(), ownerId, parentId);
    }

    public String presignedDownload(UUID fileId, int expiry) {
        FileRecord f = files.findById(fileId).orElseThrow();
        return storage.presignedDownloadUrl(f.getStorageUri(), expiry);
    }
}
