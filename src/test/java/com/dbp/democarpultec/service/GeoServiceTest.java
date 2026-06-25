package com.dbp.democarpultec.service;

import com.dbp.democarpultec.service.impl.GeoServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeoServiceTest {

    @Test
    void shouldReturnZeroDistanceWhenUsingUtecCoordinates() {
        GeoService geoService = localOnlyGeoService();

        Double distance = geoService.distanceToUtecKm(-12.068335, -77.080902);

        assertNotNull(distance);
        assertEquals(0.0, distance);
    }

    @Test
    void shouldReturnNullWhenCoordinatesAreMissing() {
        GeoService geoService = localOnlyGeoService();

        Double distance = geoService.distanceToUtecKm(null, -77.08);

        assertNull(distance);
    }

    @Test
    void shouldReturnPositiveDistanceWhenCoordinatesAreDifferent() {
        GeoService geoService = localOnlyGeoService();

        Double distance = geoService.distanceToUtecKm(-12.046374, -77.042793);

        assertNotNull(distance);
        assertTrue(distance > 0.0);
    }

    @Test
    void shouldUseDrivingDistanceWhenGoogleMapsReturnsAResult() {
        GoogleMapsService googleMapsService = mock(GoogleMapsService.class);
        when(googleMapsService.drivingDistanceKm(-12.121, -77.031, -12.068335, -77.080902))
                .thenReturn(12.34);
        GeoService geoService = new GeoServiceImpl(-12.068335, -77.080902, googleMapsService);

        Double distance = geoService.distanceToUtecKm(-12.121, -77.031);

        assertEquals(12.34, distance);
    }

    private GeoService localOnlyGeoService() {
        GoogleMapsService googleMapsService = mock(GoogleMapsService.class);
        when(googleMapsService.drivingDistanceKm(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(null);
        return new GeoServiceImpl(-12.068335, -77.080902, googleMapsService);
    }
}
