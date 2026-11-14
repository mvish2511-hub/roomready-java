package com.roomready.checkout;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.roomready.error.ProblemExamples;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

/** Checkouts. There is one property, Harbour View. Parses the request and delegates; no rules live here. */
@RestController
@Tag(name = "checkouts", description = "A guest leaves; the room is dirty")
public class CheckoutController {

    static final String PROPERTY = "harbour-view";

    private final CheckoutService checkouts;

    public CheckoutController(CheckoutService checkouts) {
        this.checkouts = checkouts;
    }

    @PostMapping("/checkouts")
    @Operation(summary = "Check a guest out: the stay ends and the room is dirty")
    @ApiResponse(responseCode = "201", description = "Dirty; the room is at the Location given")
    @ApiResponse(
            responseCode = "400", description = "A body that is not valid, or a room with no guest",
            content = @Content(
                    mediaType = ProblemExamples.PROBLEM,
                    examples = {
                            @ExampleObject(name = "validation", value = ProblemExamples.VALIDATION_CHECKOUT),
                            @ExampleObject(name = "room-not-occupied", value = ProblemExamples.ROOM_NOT_OCCUPIED)}))
    @ApiResponse(
            responseCode = "404", description = "No such room",
            content = @Content(mediaType = ProblemExamples.PROBLEM,
                    examples = @ExampleObject(name = "no-such-room",
                            value = ProblemExamples.NO_SUCH_ROOM_CHECKOUT)))
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest body) {
        CheckoutResponse done = checkouts.checkout(PROPERTY, body.room(), body.at(), body.arrivalAt());
        return ResponseEntity.created(URI.create("/properties/" + PROPERTY + "/rooms/" + done.room())).body(done);
    }
}
