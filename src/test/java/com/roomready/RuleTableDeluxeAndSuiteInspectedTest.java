package com.roomready;

import org.springframework.test.context.TestPropertySource;

/** The same table and rows, with INSPECTION_REQUIRED_FOR set to Deluxe,Suite. */
@TestPropertySource(properties = "inspection-required-for=Deluxe,Suite")
class RuleTableDeluxeAndSuiteInspectedTest extends RuleTable {

    @Override
    int column() {
        return 1;
    }
}