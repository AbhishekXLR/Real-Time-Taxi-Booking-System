package com.rideshare.rideservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="rides")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // who request a ride
    @Column(nullable = false)
    private String riderId;

    //who accepted the ride (null until matched)
    private String driverId;

    @Column(nullable = false)
    private double pickupLatitude;

    @Column(nullable = false)
    private double pickupLongitude;

    @Column(nullable = false)
    private String pickupAddress;

    @Column(nullable = false)
    private double dropLatitude;

    @Column(nullable = false)
    private double dropLongitude;

    @Column(nullable = false)
    private String dropAddress;

    //Ride status = tracks the lifecycle
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status;

    //Fare details
    private double estimatedFare;

    private double actualFare;

    //Timestamps
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

}
