package com.tarasantoshchuk.test_suites_generator.model;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;

public class SuiteComponentModel {
    private String mSuiteName;
    private SuiteType mSuiteType;
    private TypeElement mTypeElement;

    public SuiteComponentModel(String suiteName, SuiteType suiteType, TypeElement typeElement) {
        mSuiteName = suiteName;
        mSuiteType = suiteType;
        mTypeElement = typeElement;
    }

    String getSuiteName() {
        return mSuiteName;
    }

    SuiteType getSuiteType() {
        return mSuiteType;
    }

    ClassName getClassName() {
        return ClassName.get(mTypeElement);
    }
}
