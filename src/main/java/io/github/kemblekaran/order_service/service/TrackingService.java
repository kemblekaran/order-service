package io.github.kemblekaran.order_service.service;

import io.github.kemblekaran.order_service.model.TrackingRequest;
import io.github.kemblekaran.order_service.response.TrackingNumberGeneratorResponse;
import io.github.kemblekaran.order_service.util.TrackingNumberGenerator;
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
