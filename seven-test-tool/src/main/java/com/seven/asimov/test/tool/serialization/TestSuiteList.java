package com.seven.asimov.test.tool.serialization;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * TestSuiteList - serializable class.
 *
 * @author Maksim Selivanov
 */

@Root
public class TestSuiteList {

    @ElementList(name = "testSuites", entry = "testSuite", required = false)
    public List<TestSuite> testSuites = new ArrayList<TestSuite>();

    public TestSuiteList() {
    }

    public List<TestSuite> getTestSuites() {
        return testSuites;
    }
}
