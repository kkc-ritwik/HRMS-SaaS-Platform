package com.hrms.asset.workspace;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Floor plan + live seating map.
 *   GET   /api/asset/workspace/floors?officeId=
 *   POST  /api/asset/workspace/floors
 *   PUT   /api/asset/workspace/floors/{id}
 *   GET   /api/asset/workspace/floors/{id}/seating-map?date=    — composite view
 */
@RestController
@RequestMapping("/api/asset/workspace/floors")
@RequiredArgsConstructor
public class FloorPlanController {

    public interface FloorRepo extends JpaRepository<Floor, UUID> {
        @Query("SELECT f FROM Floor f WHERE f.tenantId = :t AND (:o IS NULL OR f.officeId = :o) AND f.active = true")
        List<Floor> active(@Param("t") String tenant, @Param("o") UUID office);
    }

    public interface DeskRepo extends JpaRepository<Desk, UUID> {
        @Query("SELECT d FROM Desk d WHERE d.tenantId = :t AND d.floorId = :f")
        List<Desk> onFloor(@Param("t") String tenant, @Param("f") UUID floor);
    }

    public interface BookingRepo extends JpaRepository<DeskBooking, UUID> {
        @Query("SELECT b FROM DeskBooking b WHERE b.tenantId = :t AND b.bookingDate = :d AND b.status IN ('BOOKED','CHECKED_IN')")
        List<DeskBooking> onDate(@Param("t") String tenant, @Param("d") LocalDate d);
    }

    private final FloorRepo floors;
    private final DeskRepo desks;
    private final BookingRepo bookings;

    @GetMapping
    public List<Floor> list(@RequestParam(required = false) UUID officeId) {
        return floors.active(TenantContext.get(), officeId);
    }

    @PostMapping
    @Transactional
    public Floor create(@RequestBody Floor f) {
        f.setTenantId(TenantContext.get());
        if (f.getActive() == null) f.setActive(true);
        return floors.save(f);
    }

    @PutMapping("/{id}")
    @Transactional
    public Floor update(@PathVariable UUID id, @RequestBody Floor in) {
        Floor f = floors.findById(id).orElseThrow();
        f.setName(in.getName());
        f.setCapacity(in.getCapacity());
        f.setWidthPx(in.getWidthPx());
        f.setHeightPx(in.getHeightPx());
        f.setFloorplanSvg(in.getFloorplanSvg());
        f.setFloorplanImageUri(in.getFloorplanImageUri());
        f.setZones(in.getZones());
        return floors.save(f);
    }

    /** Returns floor metadata + every desk on it + bookings/permanent-assignees for given date. */
    @GetMapping("/{id}/seating-map")
    public Map<String, Object> seatingMap(@PathVariable UUID id, @RequestParam LocalDate date) {
        String tenant = TenantContext.get();
        Floor f = floors.findById(id).orElseThrow();
        List<Desk> floorDesks = desks.onFloor(tenant, id);

        Map<UUID, UUID> bookedBy = new HashMap<>();
        for (DeskBooking b : bookings.onDate(tenant, date)) bookedBy.put(b.getDeskId(), b.getEmployeeId());

        List<Map<String, Object>> deskNodes = new ArrayList<>();
        for (Desk d : floorDesks) {
            Map<String, Object> n = new LinkedHashMap<>();
            n.put("id", d.getId().toString());
            n.put("code", d.getCode());
            n.put("type", d.getType().name());
            n.put("x", d.getXCoord());
            n.put("y", d.getYCoord());
            n.put("zone", d.getZone());
            UUID occupant = Boolean.TRUE.equals(d.getDedicated())
                    ? d.getPermanentEmployeeId()
                    : bookedBy.get(d.getId());
            n.put("occupantEmployeeId", occupant == null ? null : occupant.toString());
            n.put("status", occupant == null ? "FREE" : "OCCUPIED");
            deskNodes.add(n);
        }

        return Map.of(
                "floorId", id,
                "name", f.getName(),
                "widthPx", f.getWidthPx(),
                "heightPx", f.getHeightPx(),
                "zones", f.getZones() == null ? List.of() : f.getZones(),
                "date", date.toString(),
                "desks", deskNodes
        );
    }
}
