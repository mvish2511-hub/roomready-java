package com.roomready.room;

import java.util.List;

/** GET /properties/{property}/housekeepers. */
public record HousekeepersView(List<HousekeeperView> housekeepers) {
}
