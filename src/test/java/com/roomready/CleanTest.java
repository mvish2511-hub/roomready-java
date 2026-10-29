package com.roomready;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
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

/** Sunita cleans 214 - assigned, started, finished - with the clock fixed so every stamp is exact. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:clean-test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CleanTest {

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

    @BeforeEach
    void theGuestHasLeft214() throws Exception {
        http.perform(post("/checkouts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"room\":\"214\",\"at\":\"2026-09-28T10:40:00Z\"}")).andExpect(status().isCreated());
        http.perform(post("/assignments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"room\":\"214\",\"housekeeper\":\"HK03\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignmentId").value("AS001"))
                .andExpect(jsonPath("$.assignedTo").value("Sunita"));
    }

    @Test
    void sunitaStartsAndFinishesAndTheRoomWaitsForInspection() throws Exception {
        http.perform(post("/assignments/AS001/start")).andExpect(status().isOk());
        http.perform(get("/properties/harbour-view/rooms/214")).andExpect(jsonPath("$.state").value("cleaning"));
        http.perform(post("/assignments/AS001/finish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finishedAt").value("2026-09-28T10:40:00Z"))
                .andExpect(jsonPath("$.cleanMinutes").value(0));
        http.perform(get("/properties/harbour-view/rooms/214")).andExpect(jsonPath("$.state").value("toinspect"));
    }

 @Test
    void finishingBeforeStartingIsStepSkippedNeedingStart() throws Exception {
        http.perform(post("/assignments/AS001/finish"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("/errors/step-skipped"))
                .andExpect(jsonPath("$.state").value("assigned"))
                .andExpect(jsonPath("$.needs").value("start"));
    }

    @Test
    void theBoardAndTheListShowSunitaOn214() throws Exception {
        http.perform(get("/properties/harbour-view/rooms/214")).andExpect(jsonPath("$.assignedTo").value("Sunita"));
        http.perform(get("/properties/harbour-view/housekeepers"))
                .andExpect(jsonPath("$.housekeepers[?(@.id == 'HK03')].openAssignment").value("214"));
    }
}
