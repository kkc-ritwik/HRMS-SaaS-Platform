package com.hrms.audit.listener;

import com.hrms.audit.entity.AuditLog;
import com.hrms.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists audit rows in their OWN transaction, decoupled from the business transaction's
 * Hibernate flush.
 *
 * Saving an AuditLog directly inside a {@code @PostPersist}/{@code @PostUpdate} callback mutates
 * Hibernate's in-flight ActionQueue while it is being iterated, which throws
 * ConcurrentModificationException and fails the commit. {@link AuditEntityListener} therefore
 * defers the write to this bean (from afterCommit), which runs in a fresh transaction.
 */
@Component
@RequiredArgsConstructor
public class AuditLogWriter {

    private final AuditLogRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(AuditLog log) {
        repository.save(log);
    }
}
