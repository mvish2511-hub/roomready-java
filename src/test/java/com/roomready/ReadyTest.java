package com.roomready;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.roomready.inspection.ReadinessRepository;

/**
 * Grace passes 214 at 12:20 for a guest due at 13:00 - 40 minutes of margin - and a pass that fails part-way
 * leaves nothing behind. The clock is fixed at 12:20, so the checkout and the clean happen at that moment too.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:ready-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReadyTest {

    @TestConfiguration
    static class FixedClock {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-09-28T12:20:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    TestRestTemplate http;

    @MockitoSpyBean
    ReadinessRepository readiness;

    @BeforeEach
    void sunitaHasCleaned214() {
        http.postForObject("/checkouts", Map.of("room", "214", "at", "2026-09-28T12:20:00Z",
                "arrivalAt", "2026-09-28T13:00:00Z"), Map.class);
        http.postForObject("/assignments", Map.of("room", "214", "housekeeper", "HK03"), Map.class);
        http.postForObject("/assignments/AS001/start", null, Map.class);
        http.postForObject("/assignments/AS001/finish", null, Map.class);
    }

    @Test
    void gracePassesItWithFortyMinutesOfMargin() {
        Map<?, ?> pass = http.postForObject("/inspections",
                Map.of("room", "214", "inspector", "HK01", "result", "pass"), Map.class);
        assertThat(pass.get("state")).isEqualTo("ready");
        assertThat(pass.get("marginMinutes")).isEqualTo(40);
        assertThat(http.getForObject("/properties/harbour-view/rooms/214", Map.class).get("state")).isEqualTo("ready");
    }

    @Test
    void aPassThatFailsPartWayLeavesTheRoomToInspect() {
        doThrow(new IllegalStateException("injected: the readiness stamp cannot be written")).when(readiness)
                .insert(anyLong(), anyString(), any(), any(), any());
        int status = http.postForEntity("/inspections", Map.of("room", "214", "inspector", "HK01", "result", "pass"),
                String.class).getStatusCode().value();
        assertThat(status).isEqualTo(500);
        assertThat(http.getForObject("/properties/harbour-view/rooms/214", Map.class).get("state"))
                .isEqualTo("toinspect");
    }
}
