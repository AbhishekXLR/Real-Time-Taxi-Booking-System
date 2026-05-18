package com.rideshare.matchingservice.service;

import com.rideshare.matchingservice.client.LocationServiceClient;
import com.rideshare.matchingservice.dto.NearbyDriverResponse;
import com.rideshare.matchingservice.event.RideMatchedEvent;
import com.rideshare.matchingservice.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchingService {

    private final LocationServiceClient locationServiceClient;

    private final KafkaTemplate<String, RideMatchedEvent> kafkaTemplate;

    private static final String RIDE_MATCHED_TOPIC = "ride.matched";
    private static final double DEFAULT_SEARCH_RADIUS_KM = 5.0;

    /**
     * Main matching algorithm
     * Called when RideRequestEvent is consumed by kafka

     * STEPS:
     * 1. Ask Location Service for nearby drivers
     * 2. Score each driver and pick the best one.
     * 3. Publish RideMatchedEvent to kafka
     */
    public void matchDriverForRide(RideRequestedEvent event){

        List<NearbyDriverResponse> nearbyDriver = locationServiceClient.getNearbyDrivers(event.getPickupLatitude(), event.getPickupLongitude(), DEFAULT_SEARCH_RADIUS_KM);

        if(nearbyDriver.isEmpty()){
            log.warn("No Drivers found near ride");
            return;
        }
        // STEP 2: Score each driver and pick the best one.
        Optional<NearbyDriverResponse> bestDriver = findBestDriver(nearbyDriver);

        if(bestDriver.isEmpty()){
            log.warn("Could not find suitable driver for ride:");
            return;
        }
        NearbyDriverResponse assignedDriver = bestDriver.get();

        //Step 3: Publish RideMatchedEvent to kafka
        RideMatchedEvent matchedEvent = new RideMatchedEvent(
                event.getRideId(),
                event.getRiderId(),
                assignedDriver.getDriverId(),
                assignedDriver.getLatitude(),
                assignedDriver.getLongitude(),
                assignedDriver.getDistanceInKm()
        );
        kafkaTemplate.send(RIDE_MATCHED_TOPIC,event.getRideId(),matchedEvent);
        log.info("RideMatchedEvent has been published");

    }

    /**
     ** Driver Scoring Algorithm-->
     * Factors to be considered:
     * Distance = 70%
     * Rating = 30%  We can add more criteria together to be 100%

     * SCORE: Driver nearest to rider location will have high score i.e. distance is inversely proportional to score
     *        = (1/distance) * distanceWeight + rating * ratingWeight
     **/
    private Optional<NearbyDriverResponse> findBestDriver(List<NearbyDriverResponse> drivers){

        double distanceWeight = 0.7; // 70%
        double ratingWeight = 0.3; // 30%

        return drivers.stream().max(Comparator.comparingDouble(driver ->{
            // Distance which is closer = higher score
            // add 0.1 (if distance equals to 0) to avoid divisible by zero
            double distanceScore = 1/(driver.getDistanceInKm() +0.1);

            // for rating simulation assuming rating between 4.0 to 5.0
            // for production take the rating from Driver service

            double simulatedRating = 4.0+ Math.random();

            //final weighted score
            return (distanceScore * distanceWeight) +(simulatedRating * ratingWeight);

        }));

    }
}
