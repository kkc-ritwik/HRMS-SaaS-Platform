package com.hrms.asset.workspace;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Desk + booking REST surface.
 *   GET   /api/asset/workspace/desks?floorId=
 *   GET   /api/asset/workspace/desks/availability?date=&floorId=
 *   POST  /api/asset/workspace/bookings
 *   POST  /api/asset/workspace/bookings/{id}/check-in
 *   POST  /api/asset/workspace/bookings/{id}/cancel
 *   GET   /api/asset/workspace/bookings/mine?from=&to=
 */
@RestController
@RequestMapping("/api/asset/workspace")
@RequiredArgsConstructor
public class WorkspaceController {

    public interface DeskRepo extends JpaRepository<Desk, UUID> {
        @Query("SELECT d FROM Desk d WHERE d.tenantId = :t AND d.active = true " +
                "AND (:floor IS NULL OR d.floorId = :floor)")
        List<Desk> active(@Param("t") String tenant, @Param("floor") UUID floor);
    }

    public interface BookingRepo extends JpaRepository<DeskBooking, UUID> {
        @Query("SELECT b FROM DeskBooking b WHERE b.tenantId = :t AND b.bookingDate = :d AND b.status IN ('BOOKED','CHECKED_IN')")
        List<DeskBooking> onDate(@Param("t") String tenant, @Param("d") LocalDate date);

        @Query("SELECT b FROM DeskBooking b WHERE b.tenantId = :t AND b.employeeId = :e " +
                "AND b.bookingDate BETWEEN :from AND :to ORDER BY b.bookingDate")
        List<DeskBooking> forEmployee(@Param("t") String tenant, @Param("e") UUID emp,
                                      @Param("from") LocalDate from, @Param("to") LocalDate to);

        @Query("SELECT b FROM DeskBooking b WHERE b.deskId = :d AND b.bookingDate = :date AND b.status IN ('BOOKED','CHECKED_IN')")
        Optional<DeskBooking> existing(@Param("d") UUID desk, @Param("date") LocalDate date);
    }

    private final DeskRepo desks;
    private final BookingRepo bookings;

    @GetMapping("/desks")
    public List<Desk> listDesks(@RequestParam(required = false) UUID floorId) {
        return desks.active(TenantContext.get(), floorId);
    }

    @GetMapping("/desks/availability")
    public Map<String, Object> availability(@RequestParam LocalDate date,
                                            @RequestParam(required = false) UUID floorId) {
        String t = TenantContext.get();
        List<Desk> all = desks.active(t, floorId);
        Set<UUID> booked = new HashSet<>();
        for (DeskBooking b : bookings.onDate(t, date)) booked.add(b.getDeskId());

        List<UUID> free = new ArrayList<>();
        for (Desk d : all) {
            if (d.getType() == Desk.DeskType.HOT_DESK && !booked.contains(d.getId())) free.add(d.getId());
        }
        return Map.of(
                "date", date.toString(),
                "totalDesks", all.size(),
                "availableHotDesks", free.size(),
                "availableDeskIds", free
        );
    }

    @PostMapping("/bookings")
    @Transactional
    public DeskBooking book(@RequestBody Map<String, Object> body) {
        UUID deskId = UUID.fromString((String) body.get("deskId"));
        LocalDate date = LocalDate.parse((String) body.get("date"));
        UUID employeeId = UUID.fromString((String) body.get("employeeId"));

        bookings.existing(deskId, date).ifPresent(b -> {
            throw new IllegalStateException("Desk already booked for " + date);
        });

        Desk d = desks.findById(deskId).orElseThrow();
        if (Boolean.TRUE.equals(d.getDedicated()) &&
                d.getPermanentEmployeeId() != null && !d.getPermanentEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("Desk is permanently assigned to another employee");
        }

        DeskBooking b = new DeskBooking();
        b.setTenantId(TenantContext.get());
        b.setDeskId(deskId);
        b.setEmployeeId(employeeId);
        b.setBookingDate(date);
        b.setPurpose((String) body.get("purpose"));
        b.setQrToken(UUID.randomUUID().toString());
        return bookings.save(b);
    }

    @PostMapping("/bookings/{id}/check-in")
    @Transactional
    public DeskBooking checkIn(@PathVariable UUID id, @RequestBody(required = false) Map<String, Object> body) {
        DeskBooking b = bookings.findById(id).orElseThrow();
        if (b.getStatus() != DeskBooking.Status.BOOKED) {
            throw new IllegalStateException("Booking is not in BOOKED state");
        }
        if (body != null && body.get("qrToken") != null
                && !body.get("qrToken").equals(b.getQrToken())) {
            throw new IllegalStateException("QR token mismatch");
        }
        b.setStatus(DeskBooking.Status.CHECKED_IN);
        b.setCheckedInAt(OffsetDateTime.now());
        return bookings.save(b);
    }

    @PostMapping("/bookings/{id}/cancel")
    @Transactional
    public DeskBooking cancel(@PathVariable UUID id) {
        DeskBooking b = bookings.findById(id).orElseThrow();
        b.setStatus(DeskBooking.Status.CANCELLED);
        return bookings.save(b);
    }

    @GetMapping("/bookings/mine")
    public List<DeskBooking> mine(@RequestParam UUID employeeId,
                                  @RequestParam LocalDate from,
                                  @RequestParam LocalDate to) {
        return bookings.forEmployee(TenantContext.get(), employeeId, from, to);
    }
}
