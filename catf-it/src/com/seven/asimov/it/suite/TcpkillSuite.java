package com.seven.asimov.it.suite;

import android.test.suitebuilder.TestSuiteBuilder;
import junit.framework.Test;

public class TcpkillSuite {
    public static Test suite() {
        TestSuiteBuilder builder = new TestSuiteBuilder(TcpkillSuite.class);
        builder.includePackages("com.seven.asimov.it.tests.dispatchers.tcpkill");
        return builder.build();
    }
}
