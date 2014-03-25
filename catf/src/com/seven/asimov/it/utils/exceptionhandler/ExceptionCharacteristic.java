package com.seven.asimov.it.utils.exceptionhandler;

import com.seven.asimov.it.base.constants.TFConstantsIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ExceptionCharacteristic {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionCharacteristic.class.getSimpleName());

    private Throwable ta;
    private Class taClass;
    private List<ExceptionSearchRule> rules = new ArrayList<ExceptionSearchRule>();

    public ExceptionCharacteristic(Throwable ta) {
        this.ta = ta;
        taClass = ta.getClass();
        StackTraceElement[] stackTraceElements = ta.getStackTrace();
        for (StackTraceElement ste : stackTraceElements) {
            if (ste.getClassName().contains(TFConstantsIF.IT_PACKAGE_NAME) && ste.getMethodName().toLowerCase().contains("test_")) {
                try {
                    logger.trace("Class=" + ste.getClassName() + "   Method=" + ste.getMethodName() + "   Exception=" + taClass.getName());
                    Class steClass = Class.forName(ste.getClassName());
                    logger.trace("Class=" + steClass.getSimpleName());
                    Method steMethod = steClass.getMethod(ste.getMethodName());
                    logger.trace("Method=" + steMethod.getName());
                    ExceptionSearchRule rule = new ExceptionSearchRule(steClass, steMethod, taClass);
                    logger.debug("Searching " + rule.toString());
                    rules.add(rule);
                } catch (NoSuchMethodException nme) {
                    logger.warn(String.format("Method %s was not found in class %s", ste.getMethodName(), ste.getClassName()));
                } catch (ClassNotFoundException cnfe) {
                    logger.warn(String.format("Class %s was not found", ste.getClassName()));
                }
            }
        }
    }

    public List<ExceptionSearchRule> getRules() {
        return rules;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
        for (ExceptionSearchRule rule : rules) {
            sb.append(": \n").append(rule.toString());
        }
        return sb.toString();
    }
}
