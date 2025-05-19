package io.github.kemblekaran.orderservice.controller;

import io.github.kemblekaran.orderservice.model.TrackingRequest;
import io.github.kemblekaran.orderservice.response.TrackingNumberGeneratorResponse;
import io.github.kemblekaran.orderservice.service.TrackingService;
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

    private static final Logger logger = LoggerFactory.getLogger(TrackingController.class);

    @Autowired
    private TrackingService trackingService;

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
        return ResponseEntity.ok(trackingService.generateTrackingNumber(
                TrackingRequest.builder()
                        .originCountryId(originCountryId)
                        .destinationCountryId(destinationCountryId)
                        .weight(Double.valueOf(weight))
                        .customerId(UUID.fromString(customerId))
                        .customerName(customerName)
                        .customerSlug(customerSlug)
                        .createdAt(createdAt)
                        .build()
        ));
    }
}
