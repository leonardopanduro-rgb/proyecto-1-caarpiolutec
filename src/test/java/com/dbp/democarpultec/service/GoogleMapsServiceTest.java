package com.dbp.democarpultec.service;

import com.dbp.democarpultec.service.impl.GoogleMapsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GoogleMapsServiceTest {
    private MockRestServiceServer server;
    private GoogleMapsService googleMapsService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        googleMapsService = new GoogleMapsServiceImpl(
                builder,
                "test-key",
                "https://maps.googleapis.com/maps/api"
        );
    }

    @Test
    void shouldReturnCoordinatesWhenGeocodingSucceeds() {
        server.expect(requestTo(containsString("/geocode/json")))
                .andExpect(requestTo(containsString("address=Miraflores")))
                .andExpect(requestTo(containsString("key=test-key")))
                .andRespond(withSuccess("""
                        {
                          "status": "OK",
                          "results": [
                            {"geometry": {"location": {"lat": -12.121, "lng": -77.031}}}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        GoogleMapsService.Coordinates coordinates = googleMapsService.geocode("Miraflores");

        assertEquals(-12.121, coordinates.latitude());
        assertEquals(-77.031, coordinates.longitude());
        server.verify();
    }

    @Test
    void shouldReturnDrivingDistanceWhenDistanceMatrixSucceeds() {
        server.expect(requestTo(containsString("/distancematrix/json")))
                .andExpect(requestTo(containsString("origins=-12.121")))
                .andExpect(requestTo(containsString("key=test-key")))
                .andRespond(withSuccess("""
                        {
                          "status": "OK",
                          "rows": [
                            {"elements": [{"status": "OK", "distance": {"value": 12340}}]}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        Double distance = googleMapsService.drivingDistanceKm(-12.121, -77.031, -12.068335, -77.080902);

        assertEquals(12.34, distance);
        server.verify();
    }

    @Test
    void shouldNotRequestGoogleWhenApiKeyIsMissing() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer emptyKeyServer = MockRestServiceServer.bindTo(builder).build();
        GoogleMapsService serviceWithoutKey = new GoogleMapsServiceImpl(
                builder,
                "",
                "https://maps.googleapis.com/maps/api"
        );

        assertNull(serviceWithoutKey.geocode("Miraflores"));
        assertNull(serviceWithoutKey.drivingDistanceKm(-12.121, -77.031, -12.068335, -77.080902));
        emptyKeyServer.verify();
    }
}
