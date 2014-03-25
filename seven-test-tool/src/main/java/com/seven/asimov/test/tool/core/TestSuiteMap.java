package com.seven.asimov.test.tool.core;

import com.seven.asimov.test.tool.serialization.TestSuite;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * TestSuiteMap.
 */
public class TestSuiteMap {

    private Map<Integer, TestSuite> mTestSuiteMap;

    public TestSuiteMap() {
        mTestSuiteMap = new HashMap<Integer, TestSuite>();
    }

    public Collection<TestSuite> values() {
        return mTestSuiteMap.values();
    }

    public TestSuite get(Integer key) {
        return mTestSuiteMap.get(key);
    }

    public void clear() {
        mTestSuiteMap.clear();
    }

    public TestSuite put(Integer key, TestSuite value) {
        return mTestSuiteMap.put(key, value);
    }

    public void putAll(Map<Integer, TestSuite> testSuites) {
        mTestSuiteMap.putAll(testSuites);
    }

    public TestSuite containsValue(TestSuite testSuite) {
        Iterator<Entry<Integer, TestSuite>> entries = mTestSuiteMap.entrySet().iterator();
        while (entries.hasNext()) {
            Entry<Integer, TestSuite> entry = entries.next();
            if (entry.getValue().getName().equalsIgnoreCase(testSuite.getName())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public int getSelectedItemsCount() {

        int result = 0;

        Iterator<Entry<Integer, TestSuite>> entries = mTestSuiteMap.entrySet().iterator();
        while (entries.hasNext()) {
            Entry<Integer, TestSuite> entry = entries.next();
            if (entry.getValue().isChekedForExecution()) {
                result++;
            }
        }
        return result;
    }

    public int size() {
        return mTestSuiteMap.size();
    }
}
