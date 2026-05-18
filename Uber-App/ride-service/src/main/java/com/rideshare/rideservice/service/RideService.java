package com.rideshare.rideservice.service;

import com.rideshare.rideservice.dto.RideRequest;
import com.rideshare.rideservice.dto.RideResponse;
import com.rideshare.rideservice.event.RideRequestedEvent;
import com.rideshare.rideservice.model.Ride;
import com.rideshare.rideservice.model.RideStatus;
import com.rideshare.rideservice.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideService {

    private final RideRepository rideRepository;

    private final KafkaTemplate<String, RideRequestedEvent> kafkaTemplate;
    private static final String RIDE_REQUESTED_TOPIC = "ride.requested";


    public RideResponse requestRide(RideRequest request){
        log.info("New Ride request from rider: {}", request.getRiderId());

        Ride ride = new Ride();
        ride.setRiderId(request.getRiderId());
        ride.setPickupLatitude(request.getPickupLatitude());
        ride.setPickupLongitude(request.getPickupLongitude());
        ride.setPickupAddress(request.getPickupAddress());
        ride.setDropLatitude(request.getDropLatitude());
        ride.setDropLongitude(request.getDropLongitude());
        ride.setDropAddress(request.getDropAddress());
        ride.setStatus(RideStatus.REQUESTED);
        ride.setEstimatedFare(calculateEstimateFare(request));

        Ride savedRide = rideRepository.save(ride);



        //Publishing Event to Kafka, then Matching Service will consume this and find nearest driver.

        RideRequestedEvent event = new RideRequestedEvent(
        savedRide.getId(),
        savedRide.getRiderId(),
        savedRide.getPickupLatitude(),
        savedRide.getPickupLongitude(),
        savedRide.getPickupAddress(),
        savedRide.getDropLatitude(),
        savedRide.getDropLongitude(),
        savedRide.getDropAddress()
        );

        kafkaTemplate.send(RIDE_REQUESTED_TOPIC, savedRide.getId(), event);
        log.info("RideRequestedEvent published to Kafka for ride: {}" , savedRide.getId());

        //Updating status to Matching
        savedRide.setStatus(RideStatus.MATCHING);
        rideRepository.save(savedRide);

        return mapToResponse(savedRide);

    }

    public RideResponse startRide(String rideId) {
        Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new RuntimeException("Ride not found"));
        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new RuntimeException("Ride cannot be started. Current Status: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.RIDE_STARTED);
        ride.setStartedAt(LocalDateTime.now());
        rideRepository.save(ride);
        return mapToResponse(ride);
    }

    public RideResponse completeRide(String rideId){
        Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new RuntimeException("Ride not found"));
        if (ride.getStatus() != RideStatus.RIDE_STARTED) {
            throw new RuntimeException("Ride cannot be completed. Current Status: " + ride.getStatus());
        }
        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());

        ride.setActualFare(ride.getEstimatedFare());
        rideRepository.save(ride);

        return mapToResponse(ride);

    }

    public RideResponse cancelRide(String rideId){
        Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new RuntimeException("Ride not found"));

        ride.setStatus(RideStatus.CANCELLED);
        rideRepository.save(ride);

        return mapToResponse(ride);
    }

    public RideResponse getRideById(String rideId){
        Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new RuntimeException("Ride not found"));

        return mapToResponse(ride);
    }

    public List<RideResponse> getRidesByRider(String riderId){
        return rideRepository.findByRiderIdOrderByCreatedAtDesc(riderId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private double calculateEstimateFare(RideRequest request){
        // using Haversine distance Calculation
        double latitude1 = Math.toRadians(request.getPickupLatitude());
        double latitude2 = Math.toRadians(request.getDropLatitude());

        double longitude1 = Math.toRadians(request.getPickupLongitude());
        double longitude2 = Math.toRadians(request.getDropLongitude());

        double differenceLatitude = latitude2 - latitude1;
        double differenceLong = longitude2 - longitude1;

        double a = Math.pow(Math.sin(differenceLatitude / 2),2)
                  +Math.cos(latitude1) * Math.cos(latitude2)
                  *Math.pow(Math.sin(differenceLong / 2),2);

        double angle = 2 * Math.asin(Math.sqrt(a));
        double distanceKm = 6371 * angle;

        // base fare : Rs50 + Rs 12 per km
        double fare = 50 + (12 * distanceKm);

        return Math.round(fare * 100) / 100.0;
    }


    /* This method is use to change the status of ride, after a driver is assigned.
       RideService calls matching service with Ride Status = Matching,
       then if a Driver is found then Ride Status = Accepted and then Matching Service will call Ride Service with this Status.
     */
    public void updateRideWithDriver(String rideId, String driverId){
        Ride ride = rideRepository.findById(rideId).orElseThrow(()-> new RuntimeException("Ride not Found"));
        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ACCEPTED);
        rideRepository.save(ride);
    }

    private RideResponse mapToResponse(Ride ride){
        RideResponse response = new RideResponse();
        response.setId(ride.getId());
        response.setRiderId(ride.getRiderId());
        response.setDriverId(ride.getDriverId());
        response.setPickupLatitude(ride.getPickupLatitude());
        response.setPickupLongitude(ride.getPickupLongitude());
        response.setPickupAddress(ride.getPickupAddress());
        response.setDropLatitude(ride.getDropLatitude());
        response.setDropLongitude(ride.getDropLongitude());
        response.setDropAddress(ride.getDropAddress());
        response.setStatus(ride.getStatus());
        response.setEstimatedFare(ride.getEstimatedFare());
        response.setActualFare(ride.getActualFare());
        response.setCreatedAt(ride.getCreatedAt());
        response.setStartedAt(ride.getStartedAt());
        response.setCompletedAt(ride.getCompletedAt());
        return response;
    }
}
