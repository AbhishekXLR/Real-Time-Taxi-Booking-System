package com.rideshare.rideservice.model;


/**
 * Flow:
 * REQUESTED -> MATCHING -> ACCEPTED -> DRIVER_ARRIVING
 *           -> RIDE_STARTED -> COMPLETED
 *           -> CANCELLED (can happen at any stages)
 */
public enum RideStatus {

    REQUESTED,
    MATCHING,
    ACCEPTED,
    DRIVER_ARRIVING,
    RIDE_STARTED,
    COMPLETED,
    CANCELLED

}
