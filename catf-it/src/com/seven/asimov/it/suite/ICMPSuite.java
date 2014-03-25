package com.seven.asimov.it.suite;

import android.test.suitebuilder.TestSuiteBuilder;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.predicate.HasAnnotationPredicate;
import com.seven.asimov.it.base.predicate.NegatePredicate;
import junit.framework.Test;

public class ICMPSuite {
    public static Test suite() {
        TestSuiteBuilder builder = new TestSuiteBuilder(ICMPSuite.class);
        builder.includePackages("com.seven.asimov.it.tests.firewall.icmp");
        builder.addRequirements(new NegatePredicate(new HasAnnotationPredicate(Ignore.class)));
        return builder.build();
    }
}
