package com.tarasantoshchuk.test_suite_generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

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
        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(UiTest.class)) {
            try {
                AnnotationSpec runWithAnnotation = AnnotationSpec
                        .builder(RunWith.class)
                        .addMember("value", "$T.class", Suite.class)
                        .build();

                AnnotationSpec suiteClassesAnnotation = AnnotationSpec
                        .builder(Suite.SuiteClasses.class)
                        .addMember("value", "{$T.class}", annotatedElement)
                        .build();

                TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addAnnotation(runWithAnnotation)
                        .addAnnotation(suiteClassesAnnotation)
                        .build();

                JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
                        .build();

                Writer writer = filer
                        .createSourceFile("com.example.helloworld.HelloWorld")
                        .openWriter();
                writer
                        .write(javaFile.toString());
                writer.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }
}
