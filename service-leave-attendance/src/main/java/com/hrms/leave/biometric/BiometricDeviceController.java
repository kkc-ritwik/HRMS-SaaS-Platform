package com.hrms.leave.biometric;

import com.hrms.leave.entity.AttendancePunch;
import com.hrms.leave.service.GeofenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST endpoint biometric / face-recognition / RFID devices POST punch events to.
 * Supports vendor-neutral JSON payload + an HMAC-style device-secret header for auth.
 *
 * Most enterprise devices (ZKTeco, eSSL, Anviz, Realtime, Matrix) can be configured to
 * push to a webhook URL. Vendor-specific adapters parse their proprietary payload and
 * normalise to BiometricPunchPayload before saving.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance/biometric")
@RequiredArgsConstructor
public class BiometricDeviceController {

    public interface PunchRepo extends JpaRepository<AttendancePunch, UUID> {}

    @Value("${hrms.attendance.biometric.device-secret:}") private String deviceSecret;

    private final PunchRepo punches;
    private final GeofenceService geofence;

    /** Generic JSON push — any device that can call a webhook can use this. */
    @PostMapping("/punch")
    @Transactional
    public ResponseEntity<Map<String, Object>> push(@RequestHeader(value = "X-Device-Secret", required = false) String secret,
                                                     @RequestBody BiometricPunchPayload p) {
        if (deviceSecret != null && !deviceSecret.isBlank() && !deviceSecret.equals(secret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "bad device secret"));
        }
        AttendancePunch punch = new AttendancePunch();
        punch.setTenantId(p.tenantId());
        punch.setEmployeeId(p.employeeId());
        punch.setPunchTime(p.punchedAt() == null ? Instant.now() : p.punchedAt());
        punch.setType(AttendancePunch.PunchType.valueOf(p.type().toUpperCase()));
        punch.setSource(p.deviceModel() == null ? "BIOMETRIC" : "BIOMETRIC:" + p.deviceModel());
        punch.setIpAddress(p.deviceIp());
        if (p.latitude() != null) punch.setLatitude(BigDecimal.valueOf(p.latitude()));
        if (p.longitude() != null) punch.setLongitude(BigDecimal.valueOf(p.longitude()));
        punch.setValid(geofence.validatePunch(p.tenantId(), punch));
        AttendancePunch saved = punches.save(punch);
        log.info("Biometric punch from {} → employee={} type={}", p.deviceId(), p.employeeId(), p.type());
        return ResponseEntity.ok(Map.of("status", "ok", "punchId", saved.getId(), "valid", saved.isValid()));
    }

    /** Bulk push — devices buffer offline and send batches when they reconnect. */
    @PostMapping("/punch/bulk")
    @Transactional
    public ResponseEntity<Map<String, Object>> pushBulk(@RequestHeader(value = "X-Device-Secret", required = false) String secret,
                                                         @RequestBody List<BiometricPunchPayload> payloads) {
        if (deviceSecret != null && !deviceSecret.isBlank() && !deviceSecret.equals(secret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "bad device secret"));
        }
        int saved = 0;
        for (BiometricPunchPayload p : payloads) {
            try { push(secret, p); saved++; } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(Map.of("status", "ok", "saved", saved, "total", payloads.size()));
    }

    /** Vendor-neutral payload — adapters can be added per device-make if needed. */
    public record BiometricPunchPayload(
            String tenantId, UUID employeeId, String type,
            Instant punchedAt, String deviceId, String deviceModel,
            String deviceIp, Double latitude, Double longitude,
            String biometricMethod) {}
}
