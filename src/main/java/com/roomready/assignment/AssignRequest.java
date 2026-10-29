package com.roomready.assignment;

import jakarta.validation.constraints.NotBlank;

/** POST /assignments: which room, to which housekeeper. */
public record AssignRequest(@NotBlank String room, @NotBlank String housekeeper) {
}
