package com.dbp.democarpultec.service.impl;

import com.dbp.democarpultec.service.GeoService;
import com.dbp.democarpultec.service.GoogleMapsService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeoServiceImpl implements GeoService {
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final double utecLatitude;
    private final double utecLongitude;
    private final GoogleMapsService googleMapsService;

    public GeoServiceImpl(
            @Value("${app.utec.latitude:-12.068335}") double utecLatitude,
            @Value("${app.utec.longitude:-77.080902}") double utecLongitude,
            GoogleMapsService googleMapsService
    ) {
        this.utecLatitude = utecLatitude;
        this.utecLongitude = utecLongitude;
        this.googleMapsService = googleMapsService;
    }

    public GoogleMapsService.Coordinates geocode(String address) {
        return googleMapsService.geocode(address);
    }

    public Double distanceToUtecKm(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }

        Double drivingDistanceKm = googleMapsService.drivingDistanceKm(
                latitude,
                longitude,
                utecLatitude,
                utecLongitude
        );
        if (drivingDistanceKm != null) {
            return drivingDistanceKm;
        }

        return roundTo2Decimals(haversineKm(latitude, longitude, utecLatitude, utecLongitude));
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(dLon / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private double roundTo2Decimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
