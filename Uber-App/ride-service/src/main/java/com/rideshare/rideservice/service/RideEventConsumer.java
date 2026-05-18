package com.rideshare.rideservice.service;

import com.rideshare.rideservice.event.RideMatchedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

//It is use to listen back from the MatchingService after a driver is assigned.
@Service
@Slf4j
@RequiredArgsConstructor
public class RideEventConsumer {

    private final RideService rideService;

    @KafkaListener(
            topics = "ride.matched",
            groupId = "ride-service-group"
    )
    public void consumerRideMatchedEvent(RideMatchedEvent event){
        rideService.updateRideWithDriver(event.getRideId(), event.getDriverId());
    }
}
