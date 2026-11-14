package com.roomready.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/** The live settings, read-only - the rule book screen reads them from here. */
@RestController
@Tag(name = "settings", description = "The rules the app is running with")
public class SettingsController {

    private final RoomReadySettings settings;

    public SettingsController(RoomReadySettings settings) {
        this.settings = settings;
    }

    @GetMapping("/settings")
    @Operation(summary = "The room types that need inspection, and when a clean counts as stalled")
    @ApiResponse(responseCode = "200", description = "The current settings")
    public SettingsView settings() {
        return new SettingsView(settings.inspectionRequiredFor(), settings.stallAfterMinutes());
    }
}
