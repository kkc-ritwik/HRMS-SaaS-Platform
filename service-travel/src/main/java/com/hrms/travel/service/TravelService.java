package com.hrms.travel.service;

import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.security.model.TenantContext;
import com.hrms.travel.entity.*;
import com.hrms.travel.repository.TravelRepositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TravelService {

    private final TripRequestRepository tripRepo;
    private final TripItineraryRepository itinRepo;
    private final TravelAdvanceRepository advanceRepo;
    private final MileageClaimRepository mileageRepo;
    private final PerDiemRateRepository perDiemRepo;
    private final EventPublisher events;

    // ── Trip lifecycle ───────────────────────────────────────────────────────
    @Transactional
    public TripRequest createTrip(TripRequest req) {
        req.setTenantId(TenantContext.get());
        req.setStatus(TripRequest.TripStatus.DRAFT);
        TripRequest saved = tripRepo.save(req);
        events.publish(Topics.TRAVEL, DomainEvent.of("trip.created", "travel",
                saved.getTenantId(), saved.getId().toString(), "Trip", Map.of("employeeId", saved.getEmployeeId())));
        return saved;
    }

    @Transactional
    public TripRequest submit(UUID tripId) {
        TripRequest t = get(tripId);
        t.setStatus(TripRequest.TripStatus.SUBMITTED);
        TripRequest saved = tripRepo.save(t);
        events.publish(Topics.TRAVEL, DomainEvent.of("trip.submitted", "travel",
                t.getTenantId(), tripId.toString(), "Trip", Map.of()));
        return saved;
    }

    @Transactional
    public TripRequest approve(UUID tripId, UUID approverId) {
        TripRequest t = get(tripId);
        t.setStatus(TripRequest.TripStatus.APPROVED);
        t.setApproverId(approverId);
        t.setApprovedAt(OffsetDateTime.now());
        TripRequest saved = tripRepo.save(t);
        events.publish(Topics.TRAVEL, DomainEvent.of("trip.approved", "travel",
                t.getTenantId(), tripId.toString(), "Trip", Map.of("approverId", approverId)));
        return saved;
    }

    @Transactional
    public TripRequest reject(UUID tripId, String reason) {
        TripRequest t = get(tripId);
        t.setStatus(TripRequest.TripStatus.REJECTED);
        t.setRejectionReason(reason);
        return tripRepo.save(t);
    }

    public Page<TripRequest> myTrips(UUID employeeId, Pageable p) {
        return tripRepo.findByTenantIdAndEmployeeId(TenantContext.get(), employeeId, p);
    }

    public TripRequest get(UUID id) {
        return tripRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Trip", "id", id));
    }

    // ── Itinerary ────────────────────────────────────────────────────────────
    @Transactional
    public TripItinerary addLeg(TripItinerary leg) {
        leg.setTenantId(TenantContext.get());
        return itinRepo.save(leg);
    }
    public List<TripItinerary> itinerary(UUID tripId) {
        return itinRepo.findByTenantIdAndTripId(TenantContext.get(), tripId);
    }

    // ── Advance ──────────────────────────────────────────────────────────────
    @Transactional
    public TravelAdvance requestAdvance(TravelAdvance a) {
        a.setTenantId(TenantContext.get());
        a.setStatus(TravelAdvance.Status.REQUESTED);
        return advanceRepo.save(a);
    }
    @Transactional
    public TravelAdvance disburseAdvance(UUID id) {
        TravelAdvance a = advanceRepo.findById(id).orElseThrow();
        a.setStatus(TravelAdvance.Status.DISBURSED);
        a.setDisbursedAt(java.time.LocalDate.now());
        return advanceRepo.save(a);
    }

    // ── Mileage ──────────────────────────────────────────────────────────────
    @Transactional
    public MileageClaim claimMileage(MileageClaim c) {
        c.setTenantId(TenantContext.get());
        BigDecimal km = c.getKilometers() == null ? BigDecimal.ZERO : c.getKilometers();
        BigDecimal rate = c.getRatePerKm() == null ? BigDecimal.ZERO : c.getRatePerKm();
        c.setAmount(km.multiply(rate).setScale(2, RoundingMode.HALF_UP));
        c.setStatus(MileageClaim.Status.SUBMITTED);
        MileageClaim saved = mileageRepo.save(c);
        events.publish(Topics.TRAVEL, DomainEvent.of("mileage.submitted", "travel",
                saved.getTenantId(), saved.getId().toString(), "Mileage", Map.of("amount", saved.getAmount())));
        return saved;
    }

    // ── Per diem ─────────────────────────────────────────────────────────────
    public List<PerDiemRate> ratesForCountry(String country) {
        return perDiemRepo.findByTenantIdAndCountryAndIsActiveTrue(TenantContext.get(), country);
    }
}
