package com.roomready.checkout;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** POST /checkouts: which room, when the guest left, and when the next guest is due if the desk knows it. */
public record CheckoutRequest(@NotBlank String room, @NotNull Instant at, Instant arrivalAt) {
}