package com.roomready;

import org.springframework.test.context.TestPropertySource;

/** The same table and rows, with INSPECTION_REQUIRED_FOR set to Standard,Deluxe. */
@TestPropertySource(properties = "inspection-required-for=Standard,Deluxe")
class RuleTableStandardAndDeluxeInspectedTest extends RuleTable {

    @Override
    int column() {
        return 2;
    }
}