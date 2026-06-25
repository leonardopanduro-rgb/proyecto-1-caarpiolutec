package com.dbp.democarpultec.service;

public interface GoogleMapsService {
    Coordinates geocode(String address);

    Double drivingDistanceKm(
            double originLatitude,
            double originLongitude,
            double destinationLatitude,
            double destinationLongitude
    );

    record Coordinates(Double latitude, Double longitude) {
    }
}
