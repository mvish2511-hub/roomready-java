package com.roomready.room;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.roomready.error.ProblemExamples;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * The board, one room and the housekeepers. Parses the request and delegates; no rules live here.
 */
@RestController
@Tag(name = "rooms", description = "The board, one room, its day, and the housekeepers")
@RequestMapping("/properties/{property}")
public class RoomController {

    private final RoomService rooms;

    public RoomController(RoomService rooms) {
        this.rooms = rooms;
    }

    @GetMapping("/rooms")
    @Operation(summary = "The board: every room with its state, optionally one floor, one state and/or one type")
    @ApiResponse(responseCode = "200", description = "The rooms shown and how many of them are ready and dirty")
    @ApiResponse(
            responseCode = "400", description = "A floor, state or type that is not allowed",
            content = @Content(mediaType = ProblemExamples.PROBLEM,
                    examples = @ExampleObject(name = "validation", value = ProblemExamples.VALIDATION_QUERY)))
    @ApiResponse(
            responseCode = "404", description = "No such property",
            content = @Content(mediaType = ProblemExamples.PROBLEM,
                    examples = @ExampleObject(name = "no-such-property", value = ProblemExamples.NO_SUCH_PROPERTY)))
    public BoardView board(@PathVariable String property,
                           @RequestParam(required = false) @Min(1) @Max(4) Integer floor,
                           @RequestParam(required = false) RoomState state,
                           @RequestParam(required = false) RoomType type) {
        return rooms.board(property, floor, state, type);
    }

    @GetMapping("/rooms/{no}")
    @Operation(summary = "One room: its floor, type and state and, while somebody is on it, who and for how long")
    @ApiResponse(responseCode = "200", description = "The room")
    @ApiResponse(
            responseCode = "404", description = "No such room, or no such property",
            content = @Content(
                    mediaType = ProblemExamples.PROBLEM,
                    examples = {
                            @ExampleObject(name = "no-such-room", value = ProblemExamples.NO_SUCH_ROOM),
                            @ExampleObject(name = "no-such-property", value = ProblemExamples.NO_SUCH_PROPERTY)}))
    public RoomView room(@PathVariable String property, @PathVariable String no) {
        return rooms.room(property, no);
    }

    
    @GetMapping("/rooms/{no}/history")
    @Operation(summary = "A room's day: every event of one stay (by default the current one), in order")
    @ApiResponse(responseCode = "200", description = "The events, oldest first")
    @ApiResponse(
            responseCode = "404", description = "No such room",
            content = @Content(mediaType = ProblemExamples.PROBLEM,
                    examples = @ExampleObject(name = "no-such-room",
                            value = ProblemExamples.NO_SUCH_ROOM_HISTORY)))
    public HistoryView history(@PathVariable String property, @PathVariable String no,
                               @RequestParam(required = false) String stay) {
        return rooms.history(property, no, stay);
    }
    
    @GetMapping("/housekeepers")
    @Operation(summary = "The housekeepers, each with the room of the open assignment they hold")
    @ApiResponse(responseCode = "200", description = "The housekeepers")
    @ApiResponse(
            responseCode = "404", description = "No such property",
            content = @Content(mediaType = ProblemExamples.PROBLEM,
                    examples = @ExampleObject(name = "no-such-property",
                            value = ProblemExamples.NO_SUCH_PROPERTY)))
    public HousekeepersView housekeepers(@PathVariable String property) {
        return rooms.housekeepers(property);
    }
}