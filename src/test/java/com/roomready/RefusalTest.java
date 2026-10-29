package com.roomready;

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
import org.springframework.test.web.servlet.ResultActions;

/** The three ways the board says no, on the whole app - 214 checked out and assigned to Sunita first. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:refusal-test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RefusalTest {

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

    ResultActions send(String path, String body) throws Exception {
        return http.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @BeforeEach
    void sunitaIsOn214() throws Exception {
        send("/checkouts", "{\"room\":\"214\",\"at\":\"2026-09-28T10:40:00Z\"}").andExpect(status().isCreated());
        send("/assignments", "{\"room\":\"214\",\"housekeeper\":\"HK03\"}").andExpect(status().isCreated());
    }

    @Test
    void inspectingARoomNobodyHasFinishedIsStepSkipped() throws Exception {
        send("/inspections", "{\"room\":\"214\",\"inspector\":\"HK01\",\"result\":\"pass\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("/errors/step-skipped"))
                .andExpect(jsonPath("$.needs").value("start"));
    }

    @Test
    void assigning214AgainIsAlreadyAssignedNamingSunita() throws Exception {
        send("/assignments", "{\"room\":\"214\",\"housekeeper\":\"HK04\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("/errors/already-assigned"))
                .andExpect(jsonPath("$.assignedTo").value("Sunita"));
    }

    @Test
    void sunitaMayNotInspectHerOwnRoomButGraceMay() throws Exception {
        http.perform(post("/assignments/AS001/start")).andExpect(status().isOk());
        http.perform(post("/assignments/AS001/finish")).andExpect(status().isOk());
        send("/inspections", "{\"room\":\"214\",\"inspector\":\"HK03\",\"result\":\"pass\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("/errors/self-inspection"));
        send("/inspections", "{\"room\":\"214\",\"inspector\":\"HK01\",\"result\":\"fail\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.inspectedBy").value("Grace"))
                .andExpect(jsonPath("$.state").value("dirty"));
    }
}
