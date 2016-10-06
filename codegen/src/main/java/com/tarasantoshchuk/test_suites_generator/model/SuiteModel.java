package com.tarasantoshchuk.test_suites_generator.model;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.lang.model.element.Modifier;

public class SuiteModel {
    private String mSuiteName;
    private SuiteType mSuiteType;
    private List<AnnotatedClass> mSuiteComponents = new ArrayList<>();

    public SuiteModel(String suiteName, SuiteType suiteType) {
        this.mSuiteName = suiteName;
        this.mSuiteType = suiteType;
    }

    public void addSuiteComponent(AnnotatedClass suiteComponent) {
        mSuiteComponents.add(suiteComponent);
    }

    public String generateClassCode() {
        AnnotationSpec runWithAnnotation = AnnotationSpec
                .builder(RunWith.class)
                .addMember("value", "$T.class", Suite.class)
                .build();

        AnnotationSpec suiteClassesAnnotation = AnnotationSpec
                .builder(Suite.SuiteClasses.class)
                .addMember("value", getSuiteClassesFormat(), (Object[]) toClassNames(mSuiteComponents))
                .build();

        TypeSpec suiteSpec = TypeSpec.classBuilder(getName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(runWithAnnotation)
                .addAnnotation(suiteClassesAnnotation)
                .build();

        JavaFile javaFile = JavaFile.builder(getSuitePackage(), suiteSpec)
                .build();

        return javaFile.toString();
    }

    public String getSuitePackage() {
        if (mSuiteComponents.isEmpty()) {
            return "com.tarasantoshchuk.test_suite_generator";
        }


        String[] packageTokens = mSuiteComponents.get(0).getClassName().packageName().split("\\\\.");
        int lastMatchedTokenIndex = packageTokens.length;

        for (AnnotatedClass annotatedClass: mSuiteComponents) {
            String[] thisPackageTokens = annotatedClass.getClassName().packageName().split("\\\\.");
            lastMatchedTokenIndex = Math.min(lastMatchedTokenIndex, thisPackageTokens.length);
            lastMatchedTokenIndex = getLastMatchedTokenIndex(packageTokens, thisPackageTokens, lastMatchedTokenIndex);
        }

        return buildPackageName(packageTokens, lastMatchedTokenIndex);
    }

    private int getLastMatchedTokenIndex(String[] packageTokens, String[] thisPackageTokens, int lastMatchedTokenIndex) {
        int result = 0;
        while(result < lastMatchedTokenIndex) {
            if(!packageTokens[result].equals(thisPackageTokens[result])) {
                //fixme shitty fix to make it work at least somehow
                return lastMatchedTokenIndex;
            }
            result++;
        }
        return result;
    }

    private String buildPackageName(String[] packageTokens, int lastMatchedTokenIndex) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < lastMatchedTokenIndex; i++) {
            builder.append(packageTokens[i]);

            if (i < lastMatchedTokenIndex - 1) {
                builder.append(".");
            }
        }
        return builder.toString();
    }

    private ClassName[] toClassNames(List<AnnotatedClass> classes) {
        ClassName[] classNames = new ClassName[classes.size()];
        for(int i = 0; i < classes.size(); i++) {
            classNames[i] = classes.get(i).getClassName();
        }
        return classNames;
    }

    private String getSuiteClassesFormat() {
        StringBuilder builder = new StringBuilder("{");
        for(int i = 0; i < mSuiteComponents.size(); i++) {
            builder.append("\n$T.class");
            if (i + 1 < mSuiteComponents.size()) {
                builder.append(",");
            }
        }
        builder.append("\n}");
        return builder.toString();
    }

    public static Collection<SuiteModel> generateSuiteModelClasses(List<AnnotatedClass> list) {
        Collection<SuiteModel> unitTestSuites = generateTestSuites(list, SuiteType.UNIT_TESTS);
        Collection<SuiteModel> uiTestSuites = generateTestSuites(list, SuiteType.UI_TESTS);

        List<SuiteModel> result = new ArrayList<>();
        result.addAll(unitTestSuites);
        result.addAll(uiTestSuites);

        return result;
    }

    private static Collection<SuiteModel> generateTestSuites(List<AnnotatedClass> list, SuiteType suiteType) {
        HashMap<String, SuiteModel> testSuites = new HashMap<>();

        SuiteModel defaultSuite = new SuiteModel("", suiteType);
        testSuites.put("", defaultSuite);
        for(AnnotatedClass annotatedClass: list) {
            if (annotatedClass.getmSuiteType() == suiteType) {
                SuiteModel suiteModel = testSuites.get(annotatedClass.getmSuiteName());

                if (suiteModel == null) {
                    suiteModel = new SuiteModel(annotatedClass.getmSuiteName(), suiteType);
                    testSuites.put(annotatedClass.getmSuiteName(), suiteModel);
                }

                suiteModel.addSuiteComponent(annotatedClass);

                if (suiteModel != defaultSuite) {
                    defaultSuite.addSuiteComponent(annotatedClass);
                }
            }
        }

        return testSuites.values();
    }

    public String getName() {
        return mSuiteName + mSuiteType.namePrefix() + "Suite";
    }
}
