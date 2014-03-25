package com.seven.asimov.it.base.predicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import android.test.suitebuilder.TestMethod;

import com.android.internal.util.Predicate;

/**
 * Allows filtering test cases that belong to certain test classes.
 * 
 * @author Maksim Solodovnikov (msolodovnikov@seven.com)
 */
@SuppressWarnings("restriction")
public class TestClassPredicate implements Predicate<TestMethod> {

    private final List<Class<? extends TestCase>> mTestClasses;

    public TestClassPredicate(Class<? extends TestCase>... testClass) {
        mTestClasses = new ArrayList<Class<? extends TestCase>>();
        Collections.addAll(mTestClasses, testClass);
    }

    @Override
    public boolean apply(TestMethod testMethod) {
        return mTestClasses.contains(testMethod.getEnclosingClass());
    }
}
