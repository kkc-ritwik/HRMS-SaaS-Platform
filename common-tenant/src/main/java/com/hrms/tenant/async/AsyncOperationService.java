package com.hrms.tenant.async;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Bookkeeping for {@link AsyncOperation}. Services use this to:
 *   1. Create a tracking row before kicking off the background task.
 *   2. Update progress while the worker churns.
 *   3. Mark complete/failed with a final artefact location.
 *
 * Concurrent updates use the @Version field — repeat with backoff on OptimisticLock.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncOperationService {

    public interface Repo extends JpaRepository<AsyncOperation, UUID> {
        @Query("SELECT o FROM AsyncOperation o WHERE o.tenantId = :t AND o.status IN ('QUEUED','RUNNING') ORDER BY o.startedAt")
        List<AsyncOperation> inFlight(@Param("t") String tenant);
    }

    private final Repo repo;

    @Transactional
    public AsyncOperation create(String type, String description,
                                 Map<String, Object> params, Long itemsTotal) {
        AsyncOperation op = new AsyncOperation();
        op.setTenantId(TenantContext.get());
        op.setOperationType(type);
        op.setDescription(description);
        op.setParameters(params);
        op.setItemsTotal(itemsTotal);
        op.setRetentionUntil(OffsetDateTime.now().plusDays(7));
        return repo.save(op);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void start(UUID id) {
        repo.findById(id).ifPresent(op -> {
            op.setStatus(AsyncOperation.Status.RUNNING);
            op.setStartedAt(OffsetDateTime.now());
            repo.save(op);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void progress(UUID id, long processed, long failed) {
        repo.findById(id).ifPresent(op -> {
            op.setItemsProcessed(processed);
            op.setItemsFailed(failed);
            if (op.getItemsTotal() != null && op.getItemsTotal() > 0) {
                op.setProgressPercent((int) Math.min(100, processed * 100 / op.getItemsTotal()));
            }
            repo.save(op);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(UUID id, String resultLocation, Map<String, Object> metadata) {
        repo.findById(id).ifPresent(op -> {
            op.setStatus(AsyncOperation.Status.COMPLETED);
            op.setFinishedAt(OffsetDateTime.now());
            op.setProgressPercent(100);
            op.setResultLocation(resultLocation);
            op.setResultMetadata(metadata);
            repo.save(op);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(UUID id, String code, String message) {
        repo.findById(id).ifPresent(op -> {
            op.setStatus(AsyncOperation.Status.FAILED);
            op.setFinishedAt(OffsetDateTime.now());
            op.setErrorCode(code);
            op.setErrorMessage(message == null ? null
                    : message.substring(0, Math.min(3990, message.length())));
            repo.save(op);
        });
    }
}
