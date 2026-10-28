package com.roomready;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Checking a guest out, with the clock fixed at 10:40 so the times are exact. A fresh hotel for each test. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:checkout-test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CheckoutTest {

    @TestConfiguration
    static class FixedClock {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-09-28T10:40:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    MockMvc http;

    ResultActions checkout(String body) throws Exception {
        return http.perform(post("/checkouts").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void theGuestLeaves214AndItTurnsDirty() throws Exception {
        checkout("{\"room\":\"214\",\"at\":\"2026-09-28T10:40:00Z\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.state").value("dirty"))
                .andExpect(header().string("Location", "/properties/harbour-view/rooms/214"));
        http.perform(get("/properties/harbour-view/rooms/214")).andExpect(jsonPath("$.state").value("dirty"));
    }

    @Test
    void checkingOut214AgainIsRoomNotOccupied() throws Exception {
        checkout("{\"room\":\"214\",\"at\":\"2026-09-28T10:40:00Z\"}").andExpect(status().isCreated());
        checkout("{\"room\":\"214\",\"at\":\"2026-09-28T10:40:00Z\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/errors/room-not-occupied"));
    }

    @Test
    void aMissingTimeIsAValidationErrorNamingAt() throws Exception {
        checkout("{\"room\":\"214\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/errors/validation"))
                .andExpect(jsonPath("$.errors[0].field").value("at"));
    }

    @Test
    void aTimeBeforeTheGuestCheckedInIsAValidationErrorNamingAt() throws Exception {
        checkout("{\"room\":\"214\",\"at\":\"2026-09-26T10:40:00Z\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/errors/validation"))
                .andExpect(jsonPath("$.errors[0].field").value("at"));
    }

    @Test
    void aTimeInTheFutureIsAValidationErrorToo() throws Exception {
        checkout("{\"room\":\"214\",\"at\":\"2026-09-28T11:40:00Z\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("at"));
    }
}
