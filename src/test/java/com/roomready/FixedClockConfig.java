package com.roomready;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** A clock fixed at 10:40 on 28 September in place of the app's own, for tests that import it (the rule table). */
@TestConfiguration
public class FixedClockConfig {

    @Bean
    @Primary
    Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-09-28T10:40:00Z"), ZoneOffset.UTC);
    }
}
