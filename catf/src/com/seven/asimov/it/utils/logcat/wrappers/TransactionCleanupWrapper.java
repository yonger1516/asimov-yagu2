package com.seven.asimov.it.utils.logcat.wrappers;

public class TransactionCleanupWrapper extends LogEntryWrapper {
    private int code;

    public TransactionCleanupWrapper(String code) {
        System.out.println(code);
        this.code = Integer.parseInt(code);
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "TransactionCleanupWrapper{" +
                "code='" + code + '\'' +
                '}';
    }
}