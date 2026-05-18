package com.rideshare.location_service.service;

import com.rideshare.location_service.dto.DriverLocationRequest;
import com.rideshare.location_service.dto.NearbyDriverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {

    private final RedisTemplate<String,String> redisTemplate;

    //Redis key for all driver locations
    private static final String DRIVERS_GEO_KEY = "drivers:locations";


    /*
    *Update driver location in Redis.
    *called in Every 3 seconds by driver's phone
    *and Maps to Redis GEOADD command
     */
    public void updateDriverLocation(DriverLocationRequest driverLocationRequest){
        log.info("updating location for driver :{}", driverLocationRequest.getDriverId());

        // longitude FIRST, latitude SECOND due to GEOSpatial Standard
        Point driverPoint = new Point(driverLocationRequest.getLongitude(),driverLocationRequest.getLatitude());

        //opsForGeo -> gives access to all RedisGeoSpatial Commands in Java
        redisTemplate.opsForGeo().add(
                DRIVERS_GEO_KEY,
                driverPoint,
                driverLocationRequest.getDriverId()
        );

        log.info("location updated for driver: {}", driverLocationRequest.getDriverId());
    }


    /*
    *find nearby drivers within given radius
    * Which is called by Matching Service on ride request.
    * & Maps to redis GEORADIUS command
     */
    public List<NearbyDriverResponse> findNearbyDrivers(double latitude, double longitude, double radiusInKm){

        log.info("finding drivers near lat: {} long: {} within {}km",latitude, longitude, radiusInKm);

        Circle searchArea = new Circle(
                new Point(longitude,latitude),
                new Distance(radiusInKm, Metrics.KILOMETERS)
        );

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo().radius(
                DRIVERS_GEO_KEY,
                searchArea,
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .includeCoordinates()
                        .includeDistance()
                        .sortAscending()
                        .limit(10)
        );

        List<NearbyDriverResponse> nearbyDrivers = new ArrayList<>();
        if(results != null){
            results.getContent().forEach(result->{
                RedisGeoCommands.GeoLocation<String> location = result.getContent();
                nearbyDrivers.add(new NearbyDriverResponse(
                        location.getName(),
                        location.getPoint().getY(),
                        location.getPoint().getX(),
                        result.getDistance().getValue()
                ));
            });
        }
        log.info("Found {} drivers nearby", nearbyDrivers.size());

        return nearbyDrivers;
    }

    /*
    * Remove drivers when they go offline.
    * and Maps to ZREm command
     */
    public void removeDriver(String driverId){
        log.info("Removing driver: {}", driverId);
        redisTemplate.opsForGeo().remove(DRIVERS_GEO_KEY, driverId);
    }
}
