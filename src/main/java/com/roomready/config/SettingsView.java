package com.roomready.config;

import java.util.List;

import com.roomready.room.RoomType;

/** GET /settings: the rules the app is running with right now. */
public record SettingsView(List<RoomType> inspectionRequiredFor, int stallAfterMinutes) {
}
