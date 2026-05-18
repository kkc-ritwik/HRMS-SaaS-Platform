package com.hrms.workplace;

import com.hrms.security.model.TenantContext;
import com.hrms.workplace.meetingroom.MeetingRoom;
import com.hrms.workplace.meetingroom.RoomBooking;
import com.hrms.workplace.transport.CabBooking;
import com.hrms.workplace.visitor.Visitor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workplace")
@RequiredArgsConstructor
public class WorkplaceController {

    public interface VisitorRepo extends JpaRepository<Visitor, UUID> {
        List<Visitor> findByTenantIdAndStatus(String tenantId, Visitor.Status s);
    }
    public interface RoomRepo extends JpaRepository<MeetingRoom, UUID> {
        List<MeetingRoom> findByTenantIdAndLocationIdAndActiveTrue(String tenantId, UUID locationId);
    }
    public interface BookingRepo extends JpaRepository<RoomBooking, UUID> {
        List<RoomBooking> findByTenantIdAndRoomIdAndStartsAtBetween(String tenantId, UUID roomId, Instant from, Instant to);
    }
    public interface CabRepo extends JpaRepository<CabBooking, UUID> {
        List<CabBooking> findByTenantIdAndEmployeeIdAndPickupAtAfter(String tenantId, UUID employeeId, Instant since);
    }

    private final VisitorRepo visitors;
    private final RoomRepo rooms;
    private final BookingRepo bookings;
    private final CabRepo cabs;

    // ── Visitors ────────────────────────────────────────────────────────────
    @PostMapping("/visitors") @Transactional
    public Visitor preRegister(@RequestBody Visitor v) { v.setTenantId(TenantContext.get()); return visitors.save(v); }
    @PostMapping("/visitors/{id}/check-in") @Transactional
    public Visitor checkIn(@PathVariable UUID id) {
        Visitor v = visitors.findById(id).orElseThrow();
        v.setStatus(Visitor.Status.CHECKED_IN);
        v.setArrivalAt(Instant.now());
        return visitors.save(v);
    }
    @PostMapping("/visitors/{id}/check-out") @Transactional
    public Visitor checkOut(@PathVariable UUID id) {
        Visitor v = visitors.findById(id).orElseThrow();
        v.setStatus(Visitor.Status.CHECKED_OUT);
        v.setActualDepartureAt(Instant.now());
        return visitors.save(v);
    }
    @GetMapping("/visitors/in-building")
    public List<Visitor> currentlyInBuilding() {
        return visitors.findByTenantIdAndStatus(TenantContext.get(), Visitor.Status.CHECKED_IN);
    }

    // ── Meeting rooms ──────────────────────────────────────────────────────
    @PostMapping("/rooms") @Transactional
    public MeetingRoom createRoom(@RequestBody MeetingRoom r) { r.setTenantId(TenantContext.get()); return rooms.save(r); }
    @GetMapping("/rooms/at/{locationId}")
    public List<MeetingRoom> roomsAt(@PathVariable UUID locationId) {
        return rooms.findByTenantIdAndLocationIdAndActiveTrue(TenantContext.get(), locationId);
    }

    @PostMapping("/bookings") @Transactional
    public RoomBooking book(@RequestBody RoomBooking b) {
        b.setTenantId(TenantContext.get());
        List<RoomBooking> conflicts = bookings.findByTenantIdAndRoomIdAndStartsAtBetween(
                b.getTenantId(), b.getRoomId(), b.getStartsAt().minusSeconds(1), b.getEndsAt().plusSeconds(1));
        if (!conflicts.isEmpty()) throw new IllegalStateException("Room is double-booked");
        return bookings.save(b);
    }

    @GetMapping("/bookings/room/{roomId}")
    public List<RoomBooking> bookingsForRoom(@PathVariable UUID roomId,
                                              @RequestParam Instant from, @RequestParam Instant to) {
        return bookings.findByTenantIdAndRoomIdAndStartsAtBetween(TenantContext.get(), roomId, from, to);
    }

    // ── Cab bookings ───────────────────────────────────────────────────────
    @PostMapping("/cabs") @Transactional
    public CabBooking requestCab(@RequestBody CabBooking c) {
        c.setTenantId(TenantContext.get()); return cabs.save(c);
    }
    @GetMapping("/cabs/mine")
    public List<CabBooking> myCabs(@RequestParam UUID employeeId, @RequestParam(required = false) Instant since) {
        return cabs.findByTenantIdAndEmployeeIdAndPickupAtAfter(TenantContext.get(), employeeId,
                since == null ? Instant.now().minusSeconds(86400L * 30) : since);
    }
}
