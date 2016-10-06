package com.tarasantoshchuk.test_suites_generator.model;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;

public class AnnotatedClass {
    private String mSuiteName;
    private SuiteType mSuiteType;
    private TypeElement mTypeElement;

    public AnnotatedClass(String mSuiteName, SuiteType mSuiteType, TypeElement mTypeElement) {
        this.mSuiteName = mSuiteName;
        this.mSuiteType = mSuiteType;
        this.mTypeElement = mTypeElement;
    }

    public String getmSuiteName() {
        return mSuiteName;
    }

    public SuiteType getmSuiteType() {
        return mSuiteType;
    }

    public TypeElement getmTypeElement() {
        return mTypeElement;
    }

    public ClassName getClassName() {
        return ClassName.get(getmTypeElement());
    }
}
