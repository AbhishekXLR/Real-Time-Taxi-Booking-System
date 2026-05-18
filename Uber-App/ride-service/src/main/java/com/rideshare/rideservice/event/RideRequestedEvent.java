package com.rideshare.rideservice.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
* Event published to Kafka, then Matching Service will consume this.
* TOPIC: ride.requested
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideRequestedEvent {

    private String rideId;

    private String riderId;


    private double pickupLatitude;

    private double pickupLongitude;

    private String pickupAddress;


    private double dropLatitude;

    private double dropLongitude;

    private String dropAddress;
}
