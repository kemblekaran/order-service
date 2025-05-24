package io.github.kemblekaran.orderservice.controller;

import io.github.kemblekaran.orderservice.OrderServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = OrderServiceApplication.class)
@AutoConfigureMockMvc
class TrackingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetNextTrackingNumberEndpoint() throws Exception {
        String customerId = UUID.randomUUID().toString();

        mockMvc.perform(get("/next-tracking-number")
                        .param("origin_country_id", "US")
                        .param("destination_country_id", "IN")
                        .param("weight", "2.5")
                        .param("created_at", "2024-06-01T12:00:00Z")
                        .param("customer_id", customerId)
                        .param("customer_name", "John Doe")
                        .param("customer_slug", "john-doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingNumber").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.fallbackUsed").exists());
    }
}