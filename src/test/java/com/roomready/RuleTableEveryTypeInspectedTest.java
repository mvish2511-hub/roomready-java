package com.roomready;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.Test;

/** The same table and rows, with INSPECTION_REQUIRED_FOR at its default (every type inspected). */
class RuleTableEveryTypeInspectedTest extends RuleTable {

    @Override
    int column() {
        return 0;
    }

    @Test
    void settingsShowTheDefaults() throws Exception {
        http.perform(get("/settings"))
                .andExpect(jsonPath("$.inspectionRequiredFor.length()").value(3))
                .andExpect(jsonPath("$.stallAfterMinutes").value(60));
    }
}