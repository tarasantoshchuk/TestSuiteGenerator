package com.tarasantoshchuk.test_suites_generator.messager;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class Messager {
    private javax.annotation.processing.Messager mMessager;

    public Messager(javax.annotation.processing.Messager messager) {
        mMessager = messager;
    }

    public void warn(String message, Element element) {
        mMessager.printMessage(Diagnostic.Kind.WARNING, message, element);
    }

    public void error(String message, Element element) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
