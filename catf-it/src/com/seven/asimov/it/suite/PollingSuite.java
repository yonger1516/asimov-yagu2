package com.seven.asimov.it.suite;

import android.os.Build;
import android.test.suitebuilder.TestSuiteBuilder;
import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.predicate.HasAnnotationPredicate;
import com.seven.asimov.it.base.predicate.NegatePredicate;
import junit.framework.Test;

public class PollingSuite {
    public static Test suite() {
        TestSuiteBuilder builder = new TestSuiteBuilder(PollingSuite.class);
        builder.includePackages(
                "com.seven.asimov.it.tests.caching.polling.batterydrain",
                "com.seven.asimov.it.tests.caching.polling.longpoll",
                "com.seven.asimov.it.tests.caching.polling.ordinary",
                "com.seven.asimov.it.tests.caching.polling.overlapping.base",
                "com.seven.asimov.it.tests.caching.polling.overlapping.preventparking",
                "com.seven.asimov.it.tests.caching.polling.rlp",
                "com.seven.asimov.it.tests.caching.polling.rmp",
                "com.seven.asimov.it.tests.caching.polling.rr"
        );
        builder.addRequirements(new NegatePredicate(new HasAnnotationPredicate(Ignore.class)));
        if (Build.MODEL.equals("functional_tests")) {
            builder.addRequirements(new NegatePredicate(new HasAnnotationPredicate(DeviceOnly.class)));
        }
        return builder.build();
    }
}
