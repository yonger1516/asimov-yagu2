package com.seven.asimov.it.base.predicate;

import java.lang.annotation.Annotation;

import android.test.suitebuilder.TestMethod;

import com.android.internal.util.Predicate;

/**
 * Allows filters test methods and classes that are marked with the certain annotation.
 * 
 * @author Maksim Solodovnikov (msolodovnikov@seven.com)
 */
@SuppressWarnings("restriction")
public class HasAnnotationPredicate implements Predicate<TestMethod> {

    private Class<? extends Annotation> mAnnotationClass;

    public HasAnnotationPredicate(Class<? extends Annotation> annotationClass) {
        mAnnotationClass = annotationClass;
    }

    @Override
    public boolean apply(TestMethod testMethod) {
        return testMethod.getEnclosingClass().getAnnotation(mAnnotationClass) != null
                || testMethod.getAnnotation(mAnnotationClass) != null;
    }
}
