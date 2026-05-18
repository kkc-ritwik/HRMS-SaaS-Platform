package com.hrms.mail.service;

import com.hrms.mail.model.MailRequest;

import java.util.concurrent.CompletableFuture;

public interface MailService {
    /** Send synchronously — throws on failure. */
    void send(MailRequest req);

    /** Send asynchronously — never throws to caller; failures logged + retried per config. */
    CompletableFuture<Void> sendAsync(MailRequest req);
}
