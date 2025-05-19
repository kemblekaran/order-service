package io.github.kemblekaran.order_service.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class TrackingRequest {

    @NotBlank
    private String originCountryId;

    @NotBlank
    private String destinationCountryId;

    private Double weight;

    private String createdAt;

    @NotBlank
    private UUID customerId;

    @NotBlank
    private String customerName;

    private String customerSlug;
}
