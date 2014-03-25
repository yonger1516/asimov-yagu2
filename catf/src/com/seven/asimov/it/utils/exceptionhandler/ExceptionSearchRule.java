package com.seven.asimov.it.utils.exceptionhandler;

import java.lang.reflect.Method;

public class ExceptionSearchRule {
    private Class sourceClass;
    private Method sourceMethod;
    private Class throwableClass;
    private String logMessage;

    public ExceptionSearchRule(String sourceClass, String throwableClass, String logMessage) throws ClassNotFoundException {
        this.sourceClass = Class.forName(sourceClass);
        this.throwableClass = Class.forName(throwableClass);
        this.logMessage = logMessage;
    }

    public ExceptionSearchRule(String sourceClass, String sourceMethod, String throwableClass, String logMessage) throws ClassNotFoundException, NoSuchMethodException {
        this(sourceClass, throwableClass, logMessage);
        this.sourceMethod = this.sourceClass.getDeclaredMethod(sourceMethod);
    }

    public ExceptionSearchRule(Class sourceClass, Method sourceMethod, Class throwableClass) {
        this.sourceClass = sourceClass;
        this.sourceMethod = sourceMethod;
        this.throwableClass = throwableClass;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public boolean equalsClass(Object obj) {
        if (!(obj instanceof ExceptionSearchRule)) return false;
        ExceptionSearchRule rule = (ExceptionSearchRule)obj;
        return  this.throwableClass == rule.throwableClass &&
                this.sourceClass == rule.sourceClass;
    }

    @Override
    public boolean equals(Object obj) {
        if (!equalsClass(obj)) return false;
        ExceptionSearchRule rule = (ExceptionSearchRule)obj;
        return  this.sourceMethod.equals(rule.sourceMethod);
    }

    @Override
    public String toString() {
        return (new StringBuilder("ExceptionSearchRule ").append(" sourceClass:").append(sourceClass.getSimpleName()).
                append(" sourceMethod:").append(sourceMethod.getName()).append(" throwableClass:").append(throwableClass.getSimpleName()).
                toString());
    }

}
