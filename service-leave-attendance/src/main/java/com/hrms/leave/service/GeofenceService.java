package com.hrms.leave.service;

import com.hrms.leave.entity.AttendancePunch;
import com.hrms.leave.entity.Geofence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Validates a punch by checking distance from any active geofence.
 * Uses the Haversine formula for great-circle distance (accurate enough for sub-km radii).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeofenceService {

    public interface GeofenceRepo extends JpaRepository<Geofence, UUID> {
        List<Geofence> findByTenantIdAndActiveTrue(String tenantId);
    }

    private static final double EARTH_R = 6371000.0; // metres
    private final GeofenceRepo geofences;

    public boolean validatePunch(String tenantId, AttendancePunch punch) {
        if (punch.getLatitude() == null || punch.getLongitude() == null) {
            log.debug("Punch has no coordinates — geofence skipped");
            return true; // Punches without GPS pass through (legacy clients)
        }
        List<Geofence> active = geofences.findByTenantIdAndActiveTrue(tenantId);
        if (active.isEmpty()) return true; // No fences defined → no enforcement
        return active.stream().anyMatch(g -> within(punch.getLatitude(), punch.getLongitude(), g));
    }

    public Geofence nearestFence(String tenantId, BigDecimal lat, BigDecimal lng) {
        return geofences.findByTenantIdAndActiveTrue(tenantId).stream()
                .min((a, b) -> Double.compare(
                        haversine(lat, lng, a.getCenterLatitude(), a.getCenterLongitude()),
                        haversine(lat, lng, b.getCenterLatitude(), b.getCenterLongitude())))
                .orElse(null);
    }

    private boolean within(BigDecimal lat, BigDecimal lng, Geofence g) {
        double d = haversine(lat, lng, g.getCenterLatitude(), g.getCenterLongitude());
        return d <= g.getRadiusMeters();
    }

    private double haversine(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        double phi1 = Math.toRadians(lat1.doubleValue());
        double phi2 = Math.toRadians(lat2.doubleValue());
        double dPhi = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLambda = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());
        double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2) * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_R * c;
    }
}
