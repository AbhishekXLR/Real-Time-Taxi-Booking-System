package com.rideshare.rideservice.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequest {

    @NotBlank(message = "Rider Id is required")
    private String riderId;

    @NotNull(message = "Pickup latitude is required")
    private double pickupLatitude;

    @NotNull(message = "Pickup longitude is required")
    private double pickupLongitude;

    @NotBlank(message = "Pickup Address is required")
    private String pickupAddress;

    @NotNull(message = "Drop latitude is required")
    private double dropLatitude;

    @NotNull(message = "Drop longitude is required")
    private double dropLongitude;

    @NotBlank(message = "Drop Address is required")
    private String dropAddress;
}
