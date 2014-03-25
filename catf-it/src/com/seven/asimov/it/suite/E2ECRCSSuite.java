package com.seven.asimov.it.suite;

import android.test.suitebuilder.TestSuiteBuilder;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.predicate.HasAnnotationPredicate;
import com.seven.asimov.it.base.predicate.NegatePredicate;
import junit.framework.Test;

public class E2ECRCSSuite {
    public static Test suite() {
        TestSuiteBuilder builder = new TestSuiteBuilder(E2ECRCSSuite.class);
        builder.includePackages("com.seven.asimov.it.tests.e2e.crcs");
        builder.addRequirements(new NegatePredicate(new HasAnnotationPredicate(Ignore.class)));
        return builder.build();
    }
}
