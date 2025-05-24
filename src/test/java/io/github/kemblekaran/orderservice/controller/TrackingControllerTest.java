package io.github.kemblekaran.orderservice.controller;

import io.github.kemblekaran.orderservice.model.TrackingRequest;
import io.github.kemblekaran.orderservice.response.TrackingNumberGeneratorResponse;
import io.github.kemblekaran.orderservice.service.TrackingService;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrackingControllerTest {

    @Mock
    private TrackingService trackingService;

    private MeterRegistry meterRegistry;

    @InjectMocks
    private TrackingController trackingController;

    public TrackingControllerTest() {
        MockitoAnnotations.openMocks(this);

        meterRegistry = mock(MeterRegistry.class);
        MeterRegistry.Config config = mock(MeterRegistry.Config.class);
        when(meterRegistry.config()).thenReturn(config);
        when(config.clock()).thenReturn(Clock.SYSTEM);

        Counter counter = mock(Counter.class);
        when(meterRegistry.counter(anyString(), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class)))
                .thenReturn(counter);
        when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
        Timer timer = mock(Timer.class);

        when(meterRegistry.timer(anyString(), any(String[].class))).thenReturn(timer);
        when(meterRegistry.timer(anyString(), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class)))
                .thenReturn(timer);

        trackingController = new TrackingController(trackingService, meterRegistry);
    }

    @Test
    void testGetNextTrackingNumber() {
        String originCountryId = "US";
        String destinationCountryId = "IN";
        String weight = "2.5";
        String createdAt = "2024-06-01T12:00:00Z";
        String customerId = UUID.randomUUID().toString();
        String customerName = "John Doe";
        String customerSlug = "john-doe";

        TrackingNumberGeneratorResponse mockResponse = new TrackingNumberGeneratorResponse(
                "TRACK123456", createdAt, false
        );

        when(trackingService.generateTrackingNumber(any(TrackingRequest.class)))
                .thenReturn(mockResponse);

        ResponseEntity<TrackingNumberGeneratorResponse> response = trackingController.getNextTrackingNumber(
                originCountryId, destinationCountryId, weight, createdAt, customerId, customerName, customerSlug
        );

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(200, response.getStatusCode().value());
        assertEquals("TRACK123456", response.getBody().getTrackingNumber());
        assertFalse(response.getBody().isFallbackUsed());
    }
}