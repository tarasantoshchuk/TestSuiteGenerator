package com.other_package;

import com.tarasantoshchuk.test_suites_generator.BrokenTest;
import com.tarasantoshchuk.test_suites_generator.UiTest;

@UiTest
@BrokenTest(brokenSince = 1476003461000L, brokenBy = "taras")
public class DummyClass {
}
