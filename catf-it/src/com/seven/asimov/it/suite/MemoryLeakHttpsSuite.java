package com.seven.asimov.it.suite;

import android.test.suitebuilder.TestSuiteBuilder;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.predicate.HasAnnotationPredicate;
import com.seven.asimov.it.base.predicate.NegatePredicate;
import com.seven.asimov.it.base.predicate.TestClassPredicate;
import com.seven.asimov.it.tests.stability.base.StabilityMemoryLeakHttpReqLoadTests;
import com.seven.asimov.it.tests.stability.base.StabilityMemoryLeakHttpSizeLoadTests;
import com.seven.asimov.it.tests.stability.base.StabilityMemoryLeakHttpsSizeLoadTests;
import com.seven.asimov.it.tests.stability.base.StabilityTests;
import junit.framework.Test;

public class MemoryLeakHttpsSuite {
    public static Test suite() {
        TestSuiteBuilder builder = new TestSuiteBuilder(MemoryLeakHttpsSuite.class);
        builder.includePackages("com.seven.asimov.it.tests.stability.base");
        builder.addRequirements(new NegatePredicate(new TestClassPredicate(StabilityTests.class)));
        builder.addRequirements(new NegatePredicate(new TestClassPredicate(StabilityMemoryLeakHttpReqLoadTests.class)));
        builder.addRequirements(new NegatePredicate(new TestClassPredicate(StabilityMemoryLeakHttpsSizeLoadTests.class)));
        builder.addRequirements(new NegatePredicate(new TestClassPredicate(StabilityMemoryLeakHttpSizeLoadTests.class)));
        builder.addRequirements(new NegatePredicate(new HasAnnotationPredicate(Ignore.class)));
        return builder.build();
    }
}