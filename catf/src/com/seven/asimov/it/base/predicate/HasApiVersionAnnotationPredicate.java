package com.seven.asimov.it.base.predicate;

import android.os.Build;
import android.test.suitebuilder.TestMethod;
import com.android.internal.util.Predicate;
import com.seven.asimov.it.annotation.SinceAndroidApi;

/**
 * Predicate to use with @{@link SinceAndroidApi}
 */
public class HasApiVersionAnnotationPredicate implements Predicate<TestMethod> {

    @Override
    public boolean apply(TestMethod testMethod) {
        int apiVersion = Build.VERSION.SDK_INT;
        return ((testMethod.getEnclosingClass().getAnnotation(SinceAndroidApi.class) != null
                && Integer.valueOf(testMethod.getEnclosingClass().getAnnotation(SinceAndroidApi.class).value()[0]) > apiVersion)
                || (testMethod.getAnnotation(SinceAndroidApi.class) != null
                && Integer.valueOf(testMethod.getAnnotation(SinceAndroidApi.class).value()[0]) > apiVersion));
    }
}
