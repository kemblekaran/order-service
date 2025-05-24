package io.github.kemblekaran.orderservice.util;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;

class TrackingNumberGeneratorTest {

    private MeterRegistry meterRegistry;
    private ValueOperations<String, String> valueOperations;
    private TrackingNumberGenerator generator;

    @BeforeEach
    void setUp() {
        RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
        meterRegistry = mock(MeterRegistry.class);
        valueOperations = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
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

        generator = new TrackingNumberGenerator(redisTemplate, meterRegistry);
    }

    @Test
    void testShouldReturnTrackingNumberOnSuccess() {
        when(valueOperations.increment(anyString())).thenReturn(123L);

        String result = generator.generate("US", "IN", "customer-slug", UUID.randomUUID());

        assertNotNull(result);
        assertTrue(result.startsWith("USIN-"));
        assertFalse(generator.isFallbackUsed());
        verify(meterRegistry).counter(contains("success"), any(), any(), any(), any(), any(), any());
        verify(meterRegistry).timer(contains("time"), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testFallbackTrackingNumber() {
        String origin = "US";
        String dest = "IN";
        String slug = "customer-slug";
        UUID customerId = UUID.randomUUID();
        Throwable redisException = new RuntimeException("Redis down");

        String result = generator.fallbackTrackingNumber(origin, dest, slug, customerId, redisException);

        assertNotNull(result);
        assertTrue(result.startsWith("USIN-"));
        assertTrue(generator.isFallbackUsed());
        verify(meterRegistry).counter(contains("fallback"), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testShouldHandleNullSlug() {
        when(valueOperations.increment(anyString())).thenReturn(1L);

        String result = generator.generate("US", "IN", null, UUID.randomUUID());

        assertNotNull(result);
        assertTrue(result.startsWith("USIN-"));
    }

    @Test
    void testShouldHandleEmptySlug() {
        when(valueOperations.increment(anyString())).thenReturn(1L);

        String result = generator.generate("US", "IN", "", UUID.randomUUID());

        assertNotNull(result);
        assertTrue(result.startsWith("USIN-"));
    }

    @Test
    void testShouldHandleNullOriginAndDestination() {
        when(valueOperations.increment(anyString())).thenReturn(1L);

        String result = generator.generate(null, null, "slug", UUID.randomUUID());

        assertNotNull(result);
        assertTrue(result.contains("-"));
    }

    @Test
    void testShouldHandleNullCustomerId() {
        when(valueOperations.increment(anyString())).thenReturn(1L);

        String result = generator.generate("US", "IN", "slug", null);

        assertNotNull(result);
        assertTrue(result.startsWith("USIN-"));
    }

    @Test
    void testFallbackTrackingNumberShouldHandleNulls() {
        String result = generator.fallbackTrackingNumber(null, null, null, null, new Exception("fail"));

        assertNotNull(result);
        assertTrue(result.contains("-"));
    }

    @Test
    void testShouldHandleVeryShortSlug() {
        when(valueOperations.increment(anyString())).thenReturn(1L);

        String result = generator.generate("US", "IN", "a", UUID.randomUUID());

        assertNotNull(result);
        assertTrue(result.startsWith("USIN-"));
    }

    @Test
    void testShouldHandleLongSlug() {
        when(valueOperations.increment(anyString())).thenReturn(1L);

        String result = generator.generate("US", "IN", "averylongslugwithmanycharacters", UUID.randomUUID());

        assertNotNull(result);
        assertTrue(result.startsWith("USIN-"));
    }
}
