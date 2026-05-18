package com.rideshare.rideservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    //Topic where RideService publishes Ride request
    //Matching Service subscribes to this topic

    @Bean
    public NewTopic rideRequestedTopic(){
        return TopicBuilder.name("ride.requested")
                .partitions(3)
                .replicas(1)
                .build();
    }


    //Topic where MatchingService publishes match result
    // RideService subscribes to this Topic
    @Bean
    public NewTopic rideMatchedTopic(){
        return TopicBuilder.name("ride.matched") //--> topic listed in matching service
                .partitions(3)
                .replicas(1)
                .build();
    }
}
