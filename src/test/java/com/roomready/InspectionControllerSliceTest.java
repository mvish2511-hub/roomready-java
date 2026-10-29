package com.roomready;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.roomready.error.SelfInspection;
import com.roomready.error.StepSkipped;
import com.roomready.inspection.InspectionController;
import com.roomready.inspection.InspectionService;
import com.roomready.inspection.Result;

/**
 * Only the web layer - the inspection controller and the one exception handler - with the service replaced by
 * a mock. It proves each refusal the service throws is mapped to its status and type, without a database.
 */
@WebMvcTest(InspectionController.class)
class InspectionControllerSliceTest {

    @Autowired
    MockMvc http;

    @MockitoBean
    InspectionService inspections;

    @Test
    void selfInspectionIsMappedTo409() throws Exception {
        doThrow(new SelfInspection("214", "Sunita")).when(inspections)
                .inspect("harbour-view", "214", "HK03", Result.pass);
        http.perform(post("/inspections").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"room\":\"214\",\"inspector\":\"HK03\",\"result\":\"pass\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("/errors/self-inspection"));
    }

    @Test
    void stepSkippedIsMappedTo409WithStateAndNeeds() throws Exception {
        doThrow(new StepSkipped("214", "cleaning", "finish")).when(inspections)
                .inspect("harbour-view", "214", "HK01", Result.pass);
        http.perform(post("/inspections").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"room\":\"214\",\"inspector\":\"HK01\",\"result\":\"pass\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("/errors/step-skipped"))
                .andExpect(jsonPath("$.needs").value("finish"));
    }
}

