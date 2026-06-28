package com.dbp.democarpultec.service;

public interface GeoService {
    GoogleMapsService.Coordinates geocode(String address);

    Double distanceToUtecKm(Double latitude, Double longitude);
}
