package io.github.kemblekaran.orderservice.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TrackingNumberGeneratorResponse {

    private String trackingNumber;

    private String createdAt;

    private boolean fallbackUsed;

    private int generationTime;

    public TrackingNumberGeneratorResponse(String trackingNumber, String createdAt, boolean fallbackUsed, int generationTime) {
        this(trackingNumber, createdAt, fallbackUsed);
        this.generationTime = generationTime;
    }

    public TrackingNumberGeneratorResponse(String trackingNumber, String createdAt, boolean fallbackUsed) {
        this.trackingNumber = trackingNumber;
        this.createdAt = createdAt;
        this.fallbackUsed = fallbackUsed;
    }
}
