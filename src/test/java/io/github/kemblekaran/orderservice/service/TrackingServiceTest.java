package io.github.kemblekaran.orderservice.service;

import io.github.kemblekaran.orderservice.model.TrackingRequest;
import io.github.kemblekaran.orderservice.response.TrackingNumberGeneratorResponse;
import io.github.kemblekaran.orderservice.util.TrackingNumberGenerator;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

class TrackingServiceTest {

    private TrackingNumberGenerator trackingNumberGenerator;
    private TrackingService trackingService;

    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {

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

        trackingNumberGenerator = mock(TrackingNumberGenerator.class);
        trackingService = new TrackingService(trackingNumberGenerator);

        java.lang.reflect.Field field;
        try {
            field = TrackingService.class.getDeclaredField("trackingNumberGenerator");
            field.setAccessible(true);
            field.set(trackingService, trackingNumberGenerator);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGenerateTrackingNumber() {

        UUID customerId = UUID.randomUUID();

        TrackingRequest request = mock(TrackingRequest.class);
        when(request.getOriginCountryId()).thenReturn("US");
        when(request.getDestinationCountryId()).thenReturn("IN");
        when(request.getCustomerSlug()).thenReturn("john-doe");
        when(request.getCustomerId()).thenReturn(customerId);

        when(trackingNumberGenerator.generate("US", "IN", "john-doe", customerId))
                .thenReturn("TRACK123456");
        when(trackingNumberGenerator.isFallbackUsed()).thenReturn(false);

        TrackingNumberGeneratorResponse response = trackingService.generateTrackingNumber(request);

        assertEquals("TRACK123456", response.getTrackingNumber());
        assertNotNull(response.getCreatedAt());
        assertFalse(response.isFallbackUsed());
    }
}
