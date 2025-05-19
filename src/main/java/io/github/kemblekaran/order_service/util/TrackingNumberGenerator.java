package io.github.kemblekaran.order_service.util;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class TrackingNumberGenerator {

    private final RedisTemplate<String, String> redisTemplate;
    private boolean fallbackUsed = false;

    @Autowired
    public TrackingNumberGenerator(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @CircuitBreaker(name = "redisTracker", fallbackMethod = "fallbackTrackingNumber")
    public String generate(String origin, String dest, String slug, UUID customerId) {
        String key = String.join(":", "track", origin, dest, slug);
        Long counter = redisTemplate.opsForValue().increment(key);
        fallbackUsed = false;
        String shortSlug = slug.replace("-", "").substring(0, 2).toUpperCase();
        return (origin + dest + "-" + shortSlug + counter).toUpperCase();
    }

    public String fallbackTrackingNumber(String origin, String dest, String slug, UUID customerId, Throwable t) {
        fallbackUsed = true;
        log.warn("Fallback used due to redis error: {}", t.getMessage());
        String shortSlug = slug.replace("-", "").substring(0, 2).toUpperCase();
        String cleanedUuid = customerId.toString().replace("-", "").substring(0, 6).toUpperCase();
        return (origin + dest + "-" + shortSlug + cleanedUuid).toUpperCase();
    }

    public boolean isFallbackUsed() {
        return fallbackUsed;
    }
}
