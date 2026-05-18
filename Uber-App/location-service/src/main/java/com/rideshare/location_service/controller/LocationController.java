package com.rideshare.location_service.controller;

import com.rideshare.location_service.dto.DriverLocationRequest;
import com.rideshare.location_service.dto.NearbyDriverResponse;
import com.rideshare.location_service.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@Slf4j
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    // driver's phone calls this in every 3 seconds
    @PostMapping("/driver/update")
    public ResponseEntity<String> updateDriverLocation(@RequestBody DriverLocationRequest driverLocationRequest){
    locationService.updateDriverLocation(driverLocationRequest);
    return ResponseEntity.ok("Driver's Location updated");
    }

    //Matching service calls this when a ride is requested
    @GetMapping("/drivers/nearby")
    public ResponseEntity<List<NearbyDriverResponse>> getNearbyDrivers(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam (defaultValue = "5.0") double radius){
        return ResponseEntity.ok(locationService.findNearbyDrivers(latitude, longitude, radius));

    }

    //when driver goes offline
    @DeleteMapping("/drivers/{driverID}")
    public ResponseEntity<String> removeDriver(@PathVariable String driverID){
        locationService.removeDriver(driverID);
        return ResponseEntity.ok("Driver removed successfully");
    }
}
