package com.seven.asimov.it.suite;

import android.os.Build;
import android.test.suitebuilder.TestSuiteBuilder;
import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.predicate.HasAnnotationPredicate;
import com.seven.asimov.it.base.predicate.NegatePredicate;
import junit.framework.Test;

public class IfchSuite {
    public static Test suite() {
        TestSuiteBuilder builder = new TestSuiteBuilder(IfchSuite.class);
        builder.includePackages("com.seven.asimov.it.tests.crcs.ifch");
        builder.addRequirements(new NegatePredicate(new HasAnnotationPredicate(Ignore.class)));
        if (Build.MODEL.equals("functional_tests")) {
            builder.addRequirements(new NegatePredicate(new HasAnnotationPredicate(DeviceOnly.class)));
        }

        return builder.build();
    }
}
