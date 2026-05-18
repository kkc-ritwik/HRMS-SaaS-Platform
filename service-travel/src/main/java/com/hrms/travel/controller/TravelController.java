package com.hrms.travel.controller;

import com.hrms.travel.entity.*;
import com.hrms.travel.service.TravelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/travel")
@RequiredArgsConstructor
public class TravelController {

    private final TravelService svc;

    // Trips
    @PostMapping("/trips") public TripRequest createTrip(@RequestBody TripRequest t) { return svc.createTrip(t); }
    @PostMapping("/trips/{id}/submit") public TripRequest submit(@PathVariable UUID id) { return svc.submit(id); }
    @PostMapping("/trips/{id}/approve") public TripRequest approve(@PathVariable UUID id, @RequestParam UUID approverId) { return svc.approve(id, approverId); }
    @PostMapping("/trips/{id}/reject") public TripRequest reject(@PathVariable UUID id, @RequestParam String reason) { return svc.reject(id, reason); }
    @GetMapping("/trips/me") public Page<TripRequest> myTrips(@RequestParam UUID employeeId, Pageable p) { return svc.myTrips(employeeId, p); }
    @GetMapping("/trips/{id}") public TripRequest get(@PathVariable UUID id) { return svc.get(id); }

    // Itinerary
    @PostMapping("/itinerary") public TripItinerary addLeg(@RequestBody TripItinerary leg) { return svc.addLeg(leg); }
    @GetMapping("/itinerary/{tripId}") public List<TripItinerary> itinerary(@PathVariable UUID tripId) { return svc.itinerary(tripId); }

    // Advances
    @PostMapping("/advances") public TravelAdvance requestAdvance(@RequestBody TravelAdvance a) { return svc.requestAdvance(a); }
    @PostMapping("/advances/{id}/disburse") public TravelAdvance disburse(@PathVariable UUID id) { return svc.disburseAdvance(id); }

    // Mileage
    @PostMapping("/mileage") public MileageClaim claimMileage(@RequestBody MileageClaim c) { return svc.claimMileage(c); }

    // Per diem
    @GetMapping("/per-diem/{country}") public List<PerDiemRate> rates(@PathVariable String country) { return svc.ratesForCountry(country); }
}
