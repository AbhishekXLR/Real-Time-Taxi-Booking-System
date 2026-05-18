package com.rideshare.matchingservice.service;

import com.rideshare.matchingservice.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideEventConsumer {

    private final MatchingService matchingService;

    /**
     * Listens to ride.requested kafka topic
     * It will be triggered every time Ride Service published a new ride is requested

     * FLOW-->
     * RideService -> kafka (ride.requested) -> This Consumer -> MatchingService
     */

    @KafkaListener(
            topics = "ride.requested",
            groupId = "matching-service-group"
    )
    public void consumeRideRequestedEvent(RideRequestedEvent event){
        try{
            matchingService.matchDriverForRide(event);

        }catch(Exception e){
            log.error("Error processing ride request: {} -> {}", event.getRideId(), e.getMessage());
          // In production: send to dead letter queue for retry
        }

    }
}
