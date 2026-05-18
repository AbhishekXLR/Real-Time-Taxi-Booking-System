# Real-Time-Taxi-Booking-System
It is a Java Backend microservice project in which user can book a cab by giving latitude and Longitude for the destination

It is a Ride Booking Backend System using Spring Boot microservices architecture
to simulate real-time ride allocation scenarios.

I implemented microservices for Ride Management, Driver Matching, and Location Tracking
using REST APIs and Kafka-based event communication.

Used Redis Geospatial Indexing ( GEOADD, GEOSEARCH, GEODIST ) to store driver
locations and find nearby drivers within a given radius.

In this I have Implemented Kafka Producers and Consumers for asynchronous ride request processing
and ride status updates.

Designed driver matching logic & fare estimation algorithms for ride allocation workflows.

# This project also contains docker compose file for kafka and zookeeper and redis.
