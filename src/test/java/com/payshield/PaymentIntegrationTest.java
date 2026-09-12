package com.payshield;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest @AutoConfigureMockMvc @Testcontainers(disabledWithoutDocker = true)
class PaymentIntegrationTest {
  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
  @DynamicPropertySource static void database(DynamicPropertyRegistry r) { r.add("spring.datasource.url", postgres::getJdbcUrl); r.add("spring.datasource.username", postgres::getUsername); r.add("spring.datasource.password", postgres::getPassword); }
  @Autowired MockMvc mvc;
  @Test void paymentAndExactIdempotentReplayReturnCreated() throws Exception {
    String body="{\"amount\":100.00,\"currency\":\"INR\",\"merchantReference\":\"order-it\",\"customerId\":\"customer-it\",\"paymentToken\":\"tok_visa_success\"}";
    for(int i=0;i<2;i++) mvc.perform(post("/api/v1/payments").header("Idempotency-Key","integration-key").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
  }
}
