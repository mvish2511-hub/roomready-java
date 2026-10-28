package com.roomready.checkout;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/** Checkouts. There is one property, Harbour View. Parses the request and delegates; no rules live here. */
@RestController
public class CheckoutController {

    static final String PROPERTY = "harbour-view";

    private final CheckoutService checkouts;

    public CheckoutController(CheckoutService checkouts) {
        this.checkouts = checkouts;
    }

    @PostMapping("/checkouts")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest body) {
        CheckoutResponse done = checkouts.checkout(PROPERTY, body.room(), body.at(), body.arrivalAt());
        return ResponseEntity.created(URI.create("/properties/" + PROPERTY + "/rooms/" + done.room())).body(done);
    }
}
