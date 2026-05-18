package com.hrms.travel.repository;

import com.hrms.travel.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public final class TravelRepositories {
    private TravelRepositories() {}

    public interface TripRequestRepository extends JpaRepository<TripRequest, UUID> {
        Page<TripRequest> findByTenantIdAndEmployeeId(String tenantId, UUID employeeId, Pageable p);
        Page<TripRequest> findByTenantIdAndStatus(String tenantId, TripRequest.TripStatus s, Pageable p);
    }
    public interface TripItineraryRepository extends JpaRepository<TripItinerary, UUID> {
        List<TripItinerary> findByTenantIdAndTripId(String tenantId, UUID tripId);
    }
    public interface TravelAdvanceRepository extends JpaRepository<TravelAdvance, UUID> {
        List<TravelAdvance> findByTenantIdAndTripId(String tenantId, UUID tripId);
        Page<TravelAdvance> findByTenantIdAndEmployeeId(String tenantId, UUID employeeId, Pageable p);
    }
    public interface MileageClaimRepository extends JpaRepository<MileageClaim, UUID> {
        Page<MileageClaim> findByTenantIdAndEmployeeId(String tenantId, UUID employeeId, Pageable p);
    }
    public interface PerDiemRateRepository extends JpaRepository<PerDiemRate, UUID> {
        List<PerDiemRate> findByTenantIdAndCountryAndIsActiveTrue(String tenantId, String country);
    }
}
