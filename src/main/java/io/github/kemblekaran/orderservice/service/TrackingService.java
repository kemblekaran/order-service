package io.github.kemblekaran.orderservice.service;

import io.github.kemblekaran.orderservice.model.TrackingRequest;
import io.github.kemblekaran.orderservice.response.TrackingNumberGeneratorResponse;
import io.github.kemblekaran.orderservice.util.TrackingNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class TrackingService {

    @Autowired
    private TrackingNumberGenerator trackingNumberGenerator;

    public TrackingNumberGeneratorResponse generateTrackingNumber(TrackingRequest trackingRequest) {
        String trackingNumber = trackingNumberGenerator.generate(
                trackingRequest.getOriginCountryId(),
                trackingRequest.getDestinationCountryId(),
                trackingRequest.getCustomerSlug(),
                trackingRequest.getCustomerId()
        );

        return new TrackingNumberGeneratorResponse(
                trackingNumber,
                OffsetDateTime.now().toString(),
                trackingNumberGenerator.isFallbackUsed()
        );
    }
}
