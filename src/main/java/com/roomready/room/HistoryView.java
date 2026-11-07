package com.roomready.room;

import java.util.List;

/** GET /properties/{property}/rooms/{no}/history: one stay's events, in `at` order. */
public record HistoryView(String room, List<EventView> events) {
}
