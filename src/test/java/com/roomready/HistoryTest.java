package com.roomready;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;

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

import com.roomready.room.EventLog;

/** 214's day is logged in order, and the log can only be added to and read. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:history-test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class HistoryTest {

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

    @Test
    void twoFourteensDayIsCheckedOutAssignedStartedFinishedInspected() throws Exception {
        http.perform(post("/checkouts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"room\":\"214\",\"at\":\"2026-09-28T10:40:00Z\"}")).andExpect(status().isCreated());
        http.perform(post("/assignments").contentType(MediaType.APPLICATION_JSON)
                .content("{\"room\":\"214\",\"housekeeper\":\"HK03\"}")).andExpect(status().isCreated());
        http.perform(post("/assignments/AS001/start")).andExpect(status().isOk());
        http.perform(post("/assignments/AS001/finish")).andExpect(status().isOk());
        http.perform(post("/inspections").contentType(MediaType.APPLICATION_JSON)
                .content("{\"room\":\"214\",\"inspector\":\"HK01\",\"result\":\"pass\"}"))
                .andExpect(status().isCreated());

        http.perform(get("/properties/harbour-view/rooms/214/history"))
                .andExpect(jsonPath("$.events.length()").value(5))
                .andExpect(jsonPath("$.events[0].kind").value("checked_out"))
                .andExpect(jsonPath("$.events[1].housekeeper").value("HK03"))
                .andExpect(jsonPath("$.events[4].kind").value("inspected"))
                .andExpect(jsonPath("$.events[4].inspector").value("HK01"));
    }

    @Test
    void theLogCanOnlyAppendAndRead() {
        String[] publicMethods = Arrays.stream(EventLog.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()) && !m.isSynthetic())
                .map(Method::getName).sorted().toArray(String[]::new);
        assertThat(publicMethods).containsExactly("append", "forStay");
    }
}

