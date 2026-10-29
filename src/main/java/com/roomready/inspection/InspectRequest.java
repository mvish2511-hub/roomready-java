package com.roomready.inspection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** POST /inspections: which room, who inspects it, and what they found. */
public record InspectRequest(@NotBlank String room, @NotBlank String inspector, @NotNull Result result) {
}
