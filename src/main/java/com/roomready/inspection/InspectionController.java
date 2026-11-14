package com.roomready.inspection;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.roomready.error.ProblemExamples;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;

/** Inspections. There is one property. Parses the request and delegates; no rules live here. */
@RestController
@Tag(name = "inspections", description = "Somebody else checks the clean")
public class InspectionController {

    static final String PROPERTY = "harbour-view";

    private final InspectionService inspections;

    public InspectionController(InspectionService inspections) {
        this.inspections = inspections;
    }

    @PostMapping("/inspections")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inspect a cleaned room: a fail sends it back to dirty; a pass makes it ready")
    @ApiResponse(responseCode = "201", description = "Inspected")
    @ApiResponse(
            responseCode = "400", description = "A body that is not valid",
            content = @Content(mediaType = ProblemExamples.PROBLEM,
                    examples = @ExampleObject(name = "validation", value = ProblemExamples.VALIDATION_INSPECT)))
    @ApiResponse(
            responseCode = "404", description = "No such room, or no such housekeeper",
            content = @Content(mediaType = ProblemExamples.PROBLEM, examples = {
                    @ExampleObject(name = "no-such-room", value = ProblemExamples.NO_SUCH_ROOM_INSPECT),
                    @ExampleObject(name = "no-such-housekeeper", value = ProblemExamples.NO_SUCH_HOUSEKEEPER_INSPECT)}))
    @ApiResponse(
            responseCode = "409", description = "Not waiting for inspection, or the inspector cleaned it",
            content = @Content(mediaType = ProblemExamples.PROBLEM, examples = {
                    @ExampleObject(name = "step-skipped", value = ProblemExamples.STEP_SKIPPED_INSPECT),
                    @ExampleObject(name = "self-inspection", value = ProblemExamples.SELF_INSPECTION)}))
    public InspectionResponse inspect(@Valid @RequestBody InspectRequest body) {
        return inspections.inspect(PROPERTY, body.room(), body.inspector(), body.result());
    }
}
