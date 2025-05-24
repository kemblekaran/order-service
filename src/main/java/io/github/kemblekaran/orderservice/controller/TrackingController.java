package io.github.kemblekaran.orderservice.controller;

import io.github.kemblekaran.orderservice.model.TrackingRequest;
import io.github.kemblekaran.orderservice.response.TrackingNumberGeneratorResponse;
import io.github.kemblekaran.orderservice.service.TrackingService;
import io.github.kemblekaran.orderservice.util.Constants;
import static io.github.kemblekaran.orderservice.util.Constants.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Slf4j
public class TrackingController {

    private final TrackingService trackingService;
    private final MeterRegistry meterRegistry;

    public TrackingController(TrackingService trackingService, MeterRegistry meterRegistry) {
        this.trackingService = trackingService;
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/next-tracking-number")
    public ResponseEntity<TrackingNumberGeneratorResponse> getNextTrackingNumber(
            @RequestParam("origin_country_id") String originCountryId,
            @RequestParam("destination_country_id") String destinationCountryId,
            @RequestParam("weight") String weight,
            @RequestParam("created_at") String createdAt,
            @RequestParam("customer_id") String customerId,
            @RequestParam("customer_name") String customerName,
            @RequestParam("customer_slug") String customerSlug
            ) {

        log.info("Received request for next tracking number: origin={}, destination={}, customerId={}", originCountryId, destinationCountryId, customerId);

        Timer.Sample sample = Timer.start(meterRegistry);

        meterRegistry.counter("tracking.controller.requests",
                Metrics.METRIC_TAG_ORIGIN, originCountryId,
                Metrics.METRIC_TAG_DESTINATION, destinationCountryId,
                Metrics.METRIC_TAG_CUSTOMER_ID, customerId
        ).increment();

        TrackingNumberGeneratorResponse response = trackingService.generateTrackingNumber(
                TrackingRequest.builder()
                        .originCountryId(originCountryId)
                        .destinationCountryId(destinationCountryId)
                        .weight(Double.valueOf(weight))
                        .customerId(UUID.fromString(customerId))
                        .customerName(customerName)
                        .customerSlug(customerSlug)
                        .createdAt(createdAt)
                        .build()
        );

        sample.stop(meterRegistry.timer("tracking.controller.response.time",
                Metrics.METRIC_TAG_ORIGIN, originCountryId,
                Metrics.METRIC_TAG_DESTINATION, destinationCountryId,
                Metrics.METRIC_TAG_CUSTOMER_ID, customerId
        ));

        log.info("Generated tracking number: {}", response.getTrackingNumber());
        return ResponseEntity.ok(response);
    }
}
