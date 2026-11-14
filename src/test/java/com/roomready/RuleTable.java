package com.roomready;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The rule table. One row per case (src/test/resources/rules.csv): its setup, its action, and the status and
 * type expected under each INSPECTION_REQUIRED_FOR setting. Every row runs on a fresh hotel, on the whole app through
 * MockMvc, with the clock fixed at 10:40. The three subclasses run the same table under each setting.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:rule-table")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(FixedClockConfig.class)
abstract class RuleTable {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    MockMvc http;

    /** Which column this run checks: 0 every type inspected, 1 Deluxe and Suite, 2 Standard and Deluxe.*/
    abstract int column();

    @ParameterizedTest(name = "{0} [{1}]")
    @CsvFileSource(resources = "/rules.csv", numLinesToSkip = 1)
    void rule(String name, String tag, String setup, String action,int statusAll, String typeAll,
            int statusDeluxeSuite, String typeDeluxeSuite,    int statusStandardDeluxe, String typeStandardDeluxe)
            throws Exception {
        Map<String, String> last = new HashMap<>();
        for (String text : setup == null ? new String[0] : setup.split(";")) {
            assertThat(step(text, last).getStatus()).as(name + " setup " + text).isIn(200, 201);
        }
        MockHttpServletResponse answer = step(action, last);
        int[] statuses = {statusAll, statusDeluxeSuite, statusStandardDeluxe};
        String[] types = {typeAll, typeDeluxeSuite, typeStandardDeluxe};
        assertThat(answer.getStatus()).as(name + " status").isEqualTo(statuses[column()]);
        if (types[column()] != null) {
            assertThat(JSON.readTree(answer.getContentAsString()).path("type").asText()).as(name + " type")
                    .isEqualTo(types[column()]);
        }
    }

        private MockHttpServletResponse step(String text, Map<String, String> last) throws Exception {
        String[] w = text.trim().split(" ");
        return switch (w[0]) {
            case "checkout" -> {
                last.put("room", w[1]);
                yield send("/checkouts", "{\"room\":\"" + w[1] + "\",\"at\":\"2026-09-28T10:40:00Z\"}");
            }
            case "checkout_without_time" -> send("/checkouts", "{\"room\":\"" + w[1] + "\"}");
            case "assign" -> {
                MockHttpServletResponse r = send("/assignments",
                        "{\"room\":\"" + w[1] + "\",\"housekeeper\":\"" + w[2] + "\"}");
                if (r.getStatus() == 201) {
                    last.put("assignment", JSON.readTree(r.getContentAsString()).path("assignmentId").asText());
                }
                yield r;
            }
            case "start", "finish" -> send("/assignments/" + last.get("assignment") + "/" + w[0], null);
            case "inspect" -> send("/inspections", "{\"room\":\"" + last.get("room") + "\",\"inspector\":\""
                    + w[1] + "\",\"result\":\"" + w[2] + "\"}");
            default -> throw new IllegalArgumentException("unknown step " + text);
        };
    }

        private MockHttpServletResponse send(String path, String body) throws Exception {
        var request = post(path);
        if (body != null) {
            request = request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return http.perform(request).andReturn().getResponse();
    }
}
