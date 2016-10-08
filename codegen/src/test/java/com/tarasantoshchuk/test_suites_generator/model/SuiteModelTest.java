package com.tarasantoshchuk.test_suites_generator.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class SuiteModelTest {
    @Test
    public void packageTest() {
        int matchIndex = SuiteModel.getLastMatchedTokenIndex(
                "com.package.sub1".split(Pattern.quote(".")),
                "com.package.sub2".split(Pattern.quote(".")),
                3
        );
        assertEquals(2, matchIndex);
    }
}
