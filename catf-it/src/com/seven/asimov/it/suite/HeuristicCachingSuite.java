package com.seven.asimov.it.suite;

import android.test.suitebuilder.TestSuiteBuilder;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.predicate.HasAnnotationPredicate;
import com.seven.asimov.it.base.predicate.NegatePredicate;
import junit.framework.Test;

public class HeuristicCachingSuite {
    public static Test suite() {
        TestSuiteBuilder builder = new TestSuiteBuilder(KeepAliveConditionSuite.class);
        builder.includePackages("com.seven.asimov.it.tests.caching.heuristic");
        builder.addRequirements(new NegatePredicate(new HasAnnotationPredicate(Ignore.class)));
        return builder.build();
    }
}
