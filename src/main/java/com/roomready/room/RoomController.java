package com.roomready.room;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** The board, one room and the housekeepers. Parses the request and delegates; no rules live here. */
@RestController
@RequestMapping("/properties/{property}")
public class RoomController {

    private final RoomService rooms;

    public RoomController(RoomService rooms) {
        this.rooms = rooms;
    }

    @GetMapping("/rooms")
    public BoardView board(@PathVariable String property,
                           @RequestParam(required = false) @Min(1) @Max(4) Integer floor,
                           @RequestParam(required = false) RoomState state,
                           @RequestParam(required = false) RoomType type) {
        return rooms.board(property, floor, state, type);
    }

    @GetMapping("/rooms/{no}")
    public RoomView room(@PathVariable String property, @PathVariable String no) {
        return rooms.room(property, no);
    }

    @GetMapping("/housekeepers")
    public HousekeepersView housekeepers(@PathVariable String property) {
        return rooms.housekeepers(property);
    }
}
