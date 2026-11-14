package com.roomready.assignment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.roomready.error.ProblemExamples;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Assignments and the clean. There is one property. Parses the request and delegates; no rules here. */
@RestController
@Tag(name = "assignments", description = "Who cleans a room, and the clean")
public class AssignmentController {

    static final String PROPERTY = "harbour-view";

    private final AssignmentService assignments;

    public AssignmentController(AssignmentService assignments) {
        this.assignments = assignments;
    }

    @PostMapping("/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Assign a dirty room to a housekeeper")
    @ApiResponse(responseCode = "201", description = "Assigned")
    @ApiResponse(responseCode = "400", description = "A body that is not valid",
            content = @Content(mediaType = ProblemExamples.PROBLEM,
                    examples = @ExampleObject(name = "validation", value = ProblemExamples.VALIDATION_ASSIGN)))
    @ApiResponse(responseCode = "404", description = "No such room, or no such housekeeper",
            content = @Content(mediaType = ProblemExamples.PROBLEM, examples = {
                    @ExampleObject(name = "no-such-room", value = ProblemExamples.NO_SUCH_ROOM_ASSIGN),
                    @ExampleObject(name = "no-such-housekeeper", value = ProblemExamples.NO_SUCH_HOUSEKEEPER_ASSIGN)}))
    @ApiResponse(responseCode = "409", description = "The room is not dirty, or somebody is already on it",
            content = @Content(mediaType = ProblemExamples.PROBLEM, examples = {
                    @ExampleObject(name = "step-skipped", value = ProblemExamples.STEP_SKIPPED_ASSIGN),
                    @ExampleObject(name = "already-assigned", value = ProblemExamples.ALREADY_ASSIGNED)}))
    public AssignResponse assign(@Valid @RequestBody AssignRequest body) {
        return assignments.assign(PROPERTY, body.room(), body.housekeeper());
    }

    @PostMapping("/assignments/{assignmentId}/start")
    @Operation(summary = "Start the clean: the room is cleaning")
    @ApiResponse(responseCode = "200", description = "Started")
    @ApiResponse(
            responseCode = "404", description = "No such assignment",
            content = @Content(mediaType = ProblemExamples.PROBLEM, examples = @ExampleObject(
                    name = "no-such-assignment",
                    value = ProblemExamples.NO_SUCH_ASSIGNMENT_START)))
    @ApiResponse(
            responseCode = "409", description = "The room is not assigned",
            content = @Content(mediaType = ProblemExamples.PROBLEM, examples = @ExampleObject(
                    name = "step-skipped", value = ProblemExamples.STEP_SKIPPED_START)))
    public StartResponse start(@PathVariable String assignmentId) {
        return assignments.start(PROPERTY, assignmentId);
    }

    @PostMapping("/assignments/{assignmentId}/finish")
    @Operation(
            summary = "Finish the clean: its minutes are stamped; the room waits for inspection, or is ready at "
                    + "once when its type needs none")
    @ApiResponse(responseCode = "200", description = "Finished")
    @ApiResponse(
            responseCode = "404", description = "No such assignment",
            content = @Content(mediaType = ProblemExamples.PROBLEM, examples = @ExampleObject(
                    name = "no-such-assignment",
                    value = ProblemExamples.NO_SUCH_ASSIGNMENT_FINISH)))
    @ApiResponse(
            responseCode = "409", description = "The room is not being cleaned",
            content = @Content(mediaType = ProblemExamples.PROBLEM, examples = @ExampleObject(
                    name = "step-skipped", value = ProblemExamples.STEP_SKIPPED_FINISH)))
    public FinishResponse finish(@PathVariable String assignmentId) {
        return assignments.finish(PROPERTY, assignmentId);
    }
}
