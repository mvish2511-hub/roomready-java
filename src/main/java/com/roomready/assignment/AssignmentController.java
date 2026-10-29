package com.roomready.assignment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/** Assignments and the clean. There is one property. Parses the request and delegates; no rules here. */
@RestController
public class AssignmentController {

    static final String PROPERTY = "harbour-view";

    private final AssignmentService assignments;

    public AssignmentController(AssignmentService assignments) {
        this.assignments = assignments;
    }

    @PostMapping("/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignResponse assign(@Valid @RequestBody AssignRequest body) {
        return assignments.assign(PROPERTY, body.room(), body.housekeeper());
    }

    @PostMapping("/assignments/{assignmentId}/start")
    public StartResponse start(@PathVariable String assignmentId) {
        return assignments.start(PROPERTY, assignmentId);
    }

    @PostMapping("/assignments/{assignmentId}/finish")
    public FinishResponse finish(@PathVariable String assignmentId) {
        return assignments.finish(PROPERTY, assignmentId);
    }
}
