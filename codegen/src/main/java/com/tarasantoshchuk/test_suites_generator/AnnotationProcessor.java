package com.tarasantoshchuk.test_suites_generator;

import com.tarasantoshchuk.test_suites_generator.messager.Messager;
import com.tarasantoshchuk.test_suites_generator.model.SuiteComponentModel;
import com.tarasantoshchuk.test_suites_generator.model.SuiteModel;
import com.tarasantoshchuk.test_suites_generator.model.SuiteType;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class AnnotationProcessor extends AbstractProcessor {
    // milliseconds in 30 days
    private static final long BROKEN_TEST_LIFESPAN = 30L * 24 * 60 * 60 * 1000;

    private Filer mFiler;
    private Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mFiler = processingEnv.getFiler();
        mMessager = new Messager(processingEnv.getMessager());
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();

        annotations.add(UiTest.class.getCanonicalName());
        annotations.add(UnitTest.class.getCanonicalName());
        annotations.add(BrokenTest.class.getCanonicalName());

        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        final ArrayList<SuiteComponentModel> list = new ArrayList<>();

        traceAnnotatedClasses(roundEnvironment, UnitTest.class, new AnnotatedClassIterator<UnitTest>() {
            @Override
            public void onAnnotatedClass(TypeElement annotatedElement, UnitTest annotation) {
                /**
                 * skip and raise error for elements with {@link BrokenTest} annotation - unit tests should be fixed immediately
                 */
                if (!hasAnnotation(annotatedElement, BrokenTest.class)) {
                    list.add(
                            new SuiteComponentModel(
                                    annotation.suiteName(),
                                    SuiteType.forAnnotation(UnitTest.class),
                                    annotatedElement
                            )
                    );
                } else {
                    mMessager.error("broken unit test - should be fixed immediately", annotatedElement);
                }
            }
        });

        traceAnnotatedClasses(roundEnvironment, UiTest.class, new AnnotatedClassIterator<UiTest>() {
            @Override
            public void onAnnotatedClass(TypeElement annotatedElement, UiTest annotation) {
                /**
                 * broken ui tests is allowed, warnings/errors for them are raised during {@link BrokenTest} annotation processing
                 */
                if (!hasAnnotation(annotatedElement, BrokenTest.class)) {
                    list.add(
                            new SuiteComponentModel(
                                    annotation.suiteName(),
                                    SuiteType.forAnnotation(UiTest.class),
                                    annotatedElement
                            )
                    );
                }
            }
        });

        traceAnnotatedClasses(roundEnvironment, BrokenTest.class, new AnnotatedClassIterator<BrokenTest>() {
            @Override
            public void onAnnotatedClass(TypeElement annotatedElement, BrokenTest annotation) {
                long brokenSince = annotation.brokenSince();

                long maxFixTime = brokenSince + BROKEN_TEST_LIFESPAN;
                Date maxFixDate = new Date(maxFixTime);

                if (System.currentTimeMillis() - brokenSince < BROKEN_TEST_LIFESPAN) {
                    mMessager.warn(
                            String.format("broken test found, make sure it is fixed by %s", maxFixDate),
                            annotatedElement);
                } else {
                    mMessager.error(
                            String.format("broken test found, should have been fixed by %s", maxFixDate),
                            annotatedElement
                    );
                }
            }
        });

        //if there are annotated classes - generate test suites
        if (!list.isEmpty()) {
            Collection<SuiteModel> suites = SuiteModel.assembleSuiteModels(list);
            createSuiteClasses(suites);
        }
        return true;
    }

    private void createSuiteClasses(Collection<SuiteModel> suites) {
        for (SuiteModel suite: suites) {
            Writer writer;
            try {
                writer = mFiler
                        .createSourceFile(suite.getQualifiedName())
                        .openWriter();
                writer
                        .write(suite.generateClassCode());
                writer.close();
            } catch (IOException e) {
                mMessager.warn(e.getMessage(), null);
            }
        }
    }

    private <T extends Annotation> void traceAnnotatedClasses(RoundEnvironment roundEnvironment, Class<T> annotationClass, AnnotatedClassIterator<T> iterator) {
        for (Element element: roundEnvironment.getElementsAnnotatedWith(annotationClass)) {
            TypeElement typeElement = getValidTypeElement(element);
            T annotation = element.getAnnotation(annotationClass);

            iterator.onAnnotatedClass(typeElement, annotation);
        }
    }

    private TypeElement getValidTypeElement(Element element) {
        if (element instanceof TypeElement && (element.getKind() == ElementKind.CLASS)) {
            TypeElement typeElement = (TypeElement) element;

            if (!typeElement.getModifiers().contains(Modifier.PUBLIC)) {
                mMessager.error("Annotated element must be public", element);
            }

            return typeElement;
        } else {
            mMessager.error("Annotated element is not a class", element);
            return null;
        }
    }

    private boolean hasAnnotation(Element element, Class<? extends Annotation> annotationClass) {
        return element.getAnnotation(annotationClass) != null;
    }

    private interface AnnotatedClassIterator<T> {
        void onAnnotatedClass(TypeElement annotatedElement, T annotation);
    }
}
