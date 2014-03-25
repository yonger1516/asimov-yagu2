package com.seven.asimov.it.suite;

import android.os.Build;
import android.test.suitebuilder.TestSuiteBuilder;
import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.predicate.HasAnnotationPredicate;
import com.seven.asimov.it.base.predicate.NegatePredicate;
import junit.framework.Test;

public class OCBuild {
    public static Test suite() {
        TestSuiteBuilder builder = new TestSuiteBuilder(OCBuild.class);
        builder.includePackages("com.seven.asimov.it.complex.build");
        builder.addRequirements(new NegatePredicate(new HasAnnotationPredicate(Ignore.class)));
        if (Build.MODEL.equals("framework")) {
            builder.addRequirements(new NegatePredicate(new HasAnnotationPredicate(DeviceOnly.class)));
        }
        return builder.build();
    }
}
