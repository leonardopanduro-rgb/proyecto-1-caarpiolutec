package com.dbp.democarpultec.service.impl;

import com.dbp.democarpultec.service.GoogleMapsService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
@Slf4j
public class GoogleMapsServiceImpl implements GoogleMapsService {
    private final RestClient restClient;
    private final String apiKey;

    public GoogleMapsServiceImpl(
            RestClient.Builder restClientBuilder,
            @Value("${app.google-maps.api-key:}") String apiKey,
            @Value("${app.google-maps.base-url:https://maps.googleapis.com/maps/api}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public GoogleMapsService.Coordinates geocode(String address) {
        if (!isConfigured() || address == null || address.isBlank()) {
            return null;
        }

        try {
            GeocodeResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/geocode/json")
                            .queryParam("address", address)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(GeocodeResponse.class);

            if (response == null || !"OK".equals(response.status())
                    || response.results() == null || response.results().isEmpty()
                    || response.results().get(0).geometry() == null
                    || response.results().get(0).geometry().location() == null) {
                return null;
            }

            Location location = response.results().get(0).geometry().location();
            return new GoogleMapsService.Coordinates(location.lat(), location.lng());
        } catch (RestClientException exception) {
            log.warn("Google Maps geocoding request failed");
            return null;
        }
    }

    public Double drivingDistanceKm(
            double originLatitude,
            double originLongitude,
            double destinationLatitude,
            double destinationLongitude
    ) {
        if (!isConfigured()) {
            return null;
        }

        try {
            DistanceMatrixResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/distancematrix/json")
                            .queryParam("origins", originLatitude + "," + originLongitude)
                            .queryParam("destinations", destinationLatitude + "," + destinationLongitude)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(DistanceMatrixResponse.class);

            if (response == null || !"OK".equals(response.status())
                    || response.rows() == null || response.rows().isEmpty()
                    || response.rows().get(0).elements() == null || response.rows().get(0).elements().isEmpty()
                    || !"OK".equals(response.rows().get(0).elements().get(0).status())
                    || response.rows().get(0).elements().get(0).distance() == null
                    || response.rows().get(0).elements().get(0).distance().value() == null) {
                return null;
            }

            long distanceMeters = response.rows().get(0).elements().get(0).distance().value();
            return roundTo2Decimals(distanceMeters / 1000.0);
        } catch (RestClientException exception) {
            log.warn("Google Maps distance request failed");
            return null;
        }
    }

    private boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    private double roundTo2Decimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GeocodeResponse(String status, List<GeocodeResult> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GeocodeResult(Geometry geometry) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Geometry(Location location) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Location(Double lat, Double lng) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DistanceMatrixResponse(String status, List<DistanceRow> rows) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DistanceRow(List<DistanceElement> elements) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DistanceElement(String status, DistanceValue distance) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DistanceValue(Long value) {
    }
}
