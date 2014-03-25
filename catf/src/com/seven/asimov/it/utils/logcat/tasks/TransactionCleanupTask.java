package com.seven.asimov.it.utils.logcat.tasks;

import com.seven.asimov.it.utils.logcat.wrappers.TransactionCleanupWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransactionCleanupTask extends Task<TransactionCleanupWrapper> {
    private static final String TRANSACTION_CLEANUP_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*(-\\d).*(\\w+).*connection to ochttpd failed, initiating asynchronous transactions cleanup and reconnect";
    private final Pattern pattern = Pattern.compile(TRANSACTION_CLEANUP_REGEXP);

    @Override
    protected TransactionCleanupWrapper parseLine(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            TransactionCleanupWrapper transactionCleanupWrapper = new TransactionCleanupWrapper(matcher.group(3));
            setTimestampToWrapper(transactionCleanupWrapper, matcher);
            return transactionCleanupWrapper;
        }
        return null;
    }

    public static void main(String[] args) {
        String s2 = ":08-16 18:47:38.395 E/Asimov::JNI::OCEngine( 5796): 2013/08/16 18:47:38.399593 EEST 6716 [ERROR]\t[oci_connections.cpp:822] (-8) - OC2 connection to ochttpd failed, initiating asynchronous transactions cleanup and reconnect";

        TransactionCleanupTask task = new TransactionCleanupTask();
        TransactionCleanupWrapper wrapper = task.parseLine(s2);
        System.out.println(wrapper);
        System.out.println(wrapper.getTimestamp());
    }

}