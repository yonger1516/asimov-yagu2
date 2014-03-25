package com.seven.asimov.it.suite;

import android.test.suitebuilder.TestSuiteBuilder;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.predicate.HasAnnotationPredicate;
import com.seven.asimov.it.base.predicate.NegatePredicate;
import junit.framework.Test;

public class ConnectivitySocketSuite {
    public static Test suite() {
        TestSuiteBuilder builder = new TestSuiteBuilder(ConnectivitySocketSuite.class);
        builder.includePackages("com.seven.asimov.it.tests.connectivity.socket");
        builder.addRequirements(new NegatePredicate(new HasAnnotationPredicate(Ignore.class)));
        return builder.build();
    }
}
