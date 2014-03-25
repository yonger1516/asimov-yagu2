package com.seven.asimov.it.suite;

import android.test.suitebuilder.TestSuiteBuilder;
import com.seven.asimov.it.annotation.Execute;
import com.seven.asimov.it.base.predicate.HasAnnotationPredicate;
import junit.framework.Test;

public class Mysuite {
    public static Test suite() {
        TestSuiteBuilder builder = new TestSuiteBuilder(Mysuite.class);
        //builder.includePackages("com.seven.asimov.it.complex.functional.dns.tests");
        builder.includePackages("com.seven.asimov.it.tests.crcs.reporting");
        //builder.includePackages("com.seven.asimov.it.complex.functional.crcs.netlog.tests");
            //builder.includePackages("com.seven.asimov.it.complex.regression.tests");
        //builder.includePackages("com.seven.asimov.it.complex.functional.caching.tests");
        //builder.includePackages("com.seven.asimov.it.complex.functional.apptraffic.tests");
            //builder.includePackages("com.seven.asimov.it.complex.functional.crcs.ifch.tests");
        //builder.includePackages("com.seven.asimov.it.complex.functional.policy.tests2");
            //builder.includePackages("com.seven.asimov.it.complex.functional.connectivity.tests");
            //builder.includePackages("com.seven.asimov.it.complex.functional.conditions.traffic.tests");
        //builder.includePackages("com.seven.asimov.it.complex.functional.crcs.radiolog.tests");
        //builder.includePackages("com.seven.asimov.it.complex.functional.conditions.tests");
        //builder.includePackages("com.seven.asimov.it.complex.functional.crcs.tests2");
        //builder.includePackages("com.seven.asimov.it.complex.functional.proxy.tests");
        //builder.includePackages("com.seven.asimov.it.complex.functional.crcs.tests5");
        //builder.includePackages("com.seven.asimov.it.complex.functional.stream.tests");
        //builder.includePackages("com.seven.asimov.it.tests.caching.polling.rmp");
        builder.includePackages("com.seven.asimov.it.tests.crcs.netlog");

        //builder.addRequirements(new NegatePredicate(new HasAnnotationPredicate(Ignore.class)), new HasAnnotationPredicate(Execute.class));
        //builder.addRequirements(new NegatePredicate(new HasAnnotationPredicate(Ignore.class)));
        builder.addRequirements(new HasAnnotationPredicate(Execute.class));
        /*
        if (Build.MODEL.equals("functional_tests")) {
            builder.addRequirements(new NegatePredicate(new HasAnnotationPredicate(DeviceOnly.class)));
        }
        */
        return builder.build();
    }
}
