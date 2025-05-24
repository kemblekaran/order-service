package io.github.kemblekaran.orderservice.util;

import io.github.kemblekaran.orderservice.exception.RedisGenerateCounterException;
import static io.github.kemblekaran.orderservice.util.Constants.Metrics.METRIC_TAG_CUSTOMER_ID;
import static io.github.kemblekaran.orderservice.util.Constants.Metrics.METRIC_TAG_DESTINATION;
import static io.github.kemblekaran.orderservice.util.Constants.Metrics.METRIC_TAG_ORIGIN;
import static io.github.kemblekaran.orderservice.util.Constants.Metrics.METRIC_TAG_SLUG;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
@Getter
public class TrackingNumberGenerator {

    private final RedisTemplate<String, String> redisTemplate;
    private boolean fallbackUsed = false;
    private final MeterRegistry meterRegistry;

    public TrackingNumberGenerator(RedisTemplate<String, String> redisTemplate, MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
    }

    @CircuitBreaker(name = "redisTracker", fallbackMethod = "fallbackTrackingNumber")
    public String generate(String origin, String dest, String slug, UUID customerId) {

        fallbackUsed = false;
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            slug = cleanSlug(slug);

            String key = String.join(":", "track", origin, dest, slug);
            Long counter = redisTemplate.opsForValue().increment(key);

            if (counter == null) {
                log.error("Failed to generate tracking number: Redis returned null for key {}", key);
                meterRegistry.counter("tracking.generator.failed",
                        METRIC_TAG_ORIGIN, origin,
                        METRIC_TAG_DESTINATION, dest,
                        METRIC_TAG_SLUG, slug,
                        METRIC_TAG_CUSTOMER_ID, customerId.toString()
                ).increment();
                throw new RedisGenerateCounterException("Error generating redis counter");
            }

            meterRegistry.counter("tracking.generator.success",
                    METRIC_TAG_ORIGIN, origin,
                    METRIC_TAG_DESTINATION, dest,
                    METRIC_TAG_SLUG, slug
            ).increment();

            String shortSlug = getShortSlug(slug);

            return (origin + dest + "-" + shortSlug + counter).toUpperCase();
        } finally {
            sample.stop(meterRegistry.timer("tracking.generator.time",
                    METRIC_TAG_ORIGIN, origin,
                    METRIC_TAG_DESTINATION, dest,
                    METRIC_TAG_SLUG, slug
            ));
        }

    }

    public String fallbackTrackingNumber(String origin, String dest, String slug, UUID customerId, Throwable t) {
        fallbackUsed = true;

        slug = cleanSlug(slug);

        meterRegistry.counter("tracking.generator.fallback",
                        METRIC_TAG_ORIGIN, origin, METRIC_TAG_DESTINATION, dest, "slug", slug)
                     .increment();
        log.warn("Fallback used due to redis error: {}", t.getMessage());


        String shortSlug = getShortSlug(slug);
        String cleanedUuid = Objects.isNull(customerId) || customerId.toString()
                .isEmpty() ? UUID.nameUUIDFromBytes((origin + dest + slug + System.currentTimeMillis()).getBytes())
                .toString() : customerId.toString().replace("-", "").substring(0, 6).toUpperCase();
        return (origin + dest + "-" + shortSlug + cleanedUuid).toUpperCase();
    }

    private String cleanSlug(String slug) {
        return (slug == null) ? "" : slug.replace("-", "");
    }

    private String getShortSlug(String slug) {
        return slug.length() >= 2
                ? slug.substring(0, 2).toUpperCase()
                : slug.toUpperCase();
    }
}
