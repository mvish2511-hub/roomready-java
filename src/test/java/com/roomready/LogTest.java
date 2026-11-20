package com.roomready;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;

/** Every request and every event leaves one line on stdout, and no line ever holds a guest. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:log-test")
@ExtendWith(OutputCaptureExtension.class)
@Import(FixedClockConfig.class)
class LogTest {

    @Autowired
    TestRestTemplate http;

    @Test
    void aCheckoutAndAnAssignmentAreLoggedWithoutTheGuest(CapturedOutput output) {
        http.postForObject("/checkouts", Map.of("room", "214", "at", "2026-09-28T10:40:00Z"), Map.class);
        assertThat(http.postForEntity("/assignments", Map.of("room", "214", "housekeeper", "HK03"), String.class)
                .getStatusCode().value()).isEqualTo(201);

        String out = output.getOut();
        assertThat(out.lines().filter(l -> l.contains("method=POST") && l.contains("path=/checkouts"))).hasSize(1);
        assertThat(out.lines().filter(l -> l.contains("method=POST") && l.contains("path=/assignments"))).hasSize(1);
        assertThat(out).contains("kind=checked_out", "kind=assigned");
        assertThat(output.getAll()).doesNotContain("BK-214");
    }
}
