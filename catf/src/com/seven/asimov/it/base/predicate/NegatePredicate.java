package com.seven.asimov.it.base.predicate;

import android.test.suitebuilder.TestMethod;

import com.android.internal.util.Predicate;

/**
 * Allows negating any checks performed by the decorated predicate.
 * 
 * @author Maksim Solodovnikov (msolodovnikov@seven.com)
 */
@SuppressWarnings("restriction")
public class NegatePredicate implements Predicate<TestMethod> {

    private final Predicate<TestMethod> mPredicate;

    public NegatePredicate(Predicate<TestMethod> predicate) {
        mPredicate = predicate;
    }

    @Override
    public boolean apply(TestMethod testMethod) {
        return !mPredicate.apply(testMethod);
    }
}
