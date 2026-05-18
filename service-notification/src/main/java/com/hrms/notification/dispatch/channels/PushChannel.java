package com.hrms.notification.dispatch.channels;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.hrms.notification.dispatch.DispatchRequest;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;

@Slf4j
@Component
public class PushChannel {

    @Value("${hrms.notification.fcm.service-account-path:}") private String serviceAccountPath;
    @Value("${hrms.notification.fcm.project-id:}") private String projectId;

    private boolean enabled = false;

    @PostConstruct
    void init() {
        if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
            log.warn("FCM service account not configured — push channel disabled.");
            return;
        }
        try (FileInputStream in = new FileInputStream(serviceAccountPath)) {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(in))
                        .setProjectId(projectId).build());
            }
            enabled = true;
            log.info("FCM push channel initialized (project={})", projectId);
        } catch (Exception e) {
            log.warn("FCM init failed — push channel disabled: {}", e.getMessage());
        }
    }

    public void send(DispatchRequest req) {
        if (!enabled || req.getToFcmTokens() == null) {
            log.warn("Push skipped — enabled={} tokens={}", enabled, req.getToFcmTokens());
            return;
        }
        for (String token : req.getToFcmTokens()) {
            try {
                String body = req.getBody() != null ? req.getBody() : "";
                Message msg = Message.builder()
                        .setToken(token)
                        .setNotification(Notification.builder()
                                .setTitle(req.getSubject() == null ? "HRMS" : req.getSubject())
                                .setBody(body).build())
                        .putData("category", req.getCategory() == null ? "" : req.getCategory())
                        .putData("referenceType", req.getReferenceType() == null ? "" : req.getReferenceType())
                        .putData("referenceId", req.getReferenceId() == null ? "" : req.getReferenceId())
                        .build();
                FirebaseMessaging.getInstance().send(msg);
            } catch (Exception e) {
                log.error("Push to {} failed: {}", token, e.getMessage());
            }
        }
    }
}
