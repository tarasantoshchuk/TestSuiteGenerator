package com.tarasantoshchuk.test_suites_generator;

import com.tarasantoshchuk.test_suites_generator.model.AnnotatedClass;
import com.tarasantoshchuk.test_suites_generator.model.SuiteModel;
import com.tarasantoshchuk.test_suites_generator.model.SuiteType;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class AnnotationProcessor extends AbstractProcessor {
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(UiTest.class.getCanonicalName());
        annotations.add(UnitTest.class.getCanonicalName());
        return annotations;
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        ArrayList<AnnotatedClass> list = new ArrayList<>();
        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(UnitTest.class)) {
            list.add(
                    new AnnotatedClass(
                            annotatedElement.getAnnotation(UnitTest.class).suiteName(),
                            SuiteType.UNIT_TESTS,
                            (TypeElement) annotatedElement
                    )
            );
        }

        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(UiTest.class)) {
            list.add(
                    new AnnotatedClass(
                            annotatedElement.getAnnotation(UiTest.class).suiteName(),
                            SuiteType.UI_TESTS,
                            (TypeElement) annotatedElement
                    )
            );
        }

        if (list.isEmpty()) {
            return true;
        }

        Collection<SuiteModel> suites = SuiteModel.generateSuiteModelClasses(list);

        for (SuiteModel suite: suites) {
            Writer writer;
            try {
                writer = filer
                        .createSourceFile(suite.getSuitePackage()+ "." + suite.getName())
                        .openWriter();
                writer
                        .write(suite.generateClassCode());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
