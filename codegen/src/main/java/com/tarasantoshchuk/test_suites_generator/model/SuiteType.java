package com.tarasantoshchuk.test_suites_generator.model;

public enum SuiteType {
    UNIT_TESTS("UnitTest"),
    UI_TESTS("UiTest");

    private String mNamePrefix;
    SuiteType(String namePrefix) {
        mNamePrefix = namePrefix;
    }

    public String namePrefix() {
        return mNamePrefix;
    }
}
