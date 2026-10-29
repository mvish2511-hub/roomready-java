package com.roomready.inspection;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/** Inspections. There is one property. Parses the request and delegates; no rules live here. */
@RestController
public class InspectionController {

    static final String PROPERTY = "harbour-view";

    private final InspectionService inspections;

    public InspectionController(InspectionService inspections) {
        this.inspections = inspections;
    }

    @PostMapping("/inspections")
    @ResponseStatus(HttpStatus.CREATED)
    public InspectionResponse inspect(@Valid @RequestBody InspectRequest body) {
        return inspections.inspect(PROPERTY, body.room(), body.inspector(), body.result());
    }
}
