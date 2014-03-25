package com.seven.asimov.it.suite;

import android.test.suitebuilder.TestSuiteBuilder;
import com.seven.asimov.it.base.predicate.HasAnnotationPredicate;
import com.seven.asimov.it.base.predicate.NegatePredicate;
import junit.framework.Test;
import org.junit.Ignore;

public class SmokeSuite {
    public static Test suite() {
        TestSuiteBuilder builder = new TestSuiteBuilder(SmokeSuite.class);
        builder.includePackages("com.seven.asimov.it.tests.smoke");
        builder.addRequirements(new NegatePredicate(new HasAnnotationPredicate(Ignore.class)));
        return builder.build();
    }
}
