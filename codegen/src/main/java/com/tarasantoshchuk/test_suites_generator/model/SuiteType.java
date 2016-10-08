package com.tarasantoshchuk.test_suites_generator.model;

public enum SuiteType {
    UNIT_TESTS("UnitTest"),
    UI_TESTS("UiTest");

    private String mNameSuffix;
    SuiteType(String nameSuffix) {
        mNameSuffix = nameSuffix;
    }

    public String nameSuffix() {
        return mNameSuffix;
    }
}
