package com.tarasantoshchuk.test_suites_generator.model;

import com.tarasantoshchuk.test_suites_generator.UiTest;
import com.tarasantoshchuk.test_suites_generator.UnitTest;

import java.lang.annotation.Annotation;

public enum SuiteType {
    UNIT_TESTS("UnitTest"),
    UI_TESTS("UiTest"),
    ALL_TESTS("Test");

    private String mNameSuffix;
    SuiteType(String nameSuffix) {
        mNameSuffix = nameSuffix;
    }

    public String nameSuffix() {
        return mNameSuffix;
    }

    public static SuiteType forAnnotation(Class<? extends Annotation> annotation) {
        if (annotation == UnitTest.class) {
            return UNIT_TESTS;
        } else if (annotation == UiTest.class) {
            return UI_TESTS;
        } else {
            throw new RuntimeException("No suite type for annotation " + annotation.getName());
        }
    }
}
