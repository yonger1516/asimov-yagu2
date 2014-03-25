package com.seven.asimov.it.utils.exceptionhandler;

import java.util.ArrayList;
import java.util.List;

public class TFExceptionProcessor {
    public static void processException(Throwable throwable) {
        Throwable tt = throwable;
        List<ExceptionCharacteristic> characteristics = new ArrayList<ExceptionCharacteristic>();
        do {
            characteristics.add(new ExceptionCharacteristic(tt));
            tt = tt.getCause();
        } while (tt != null);

        for (ExceptionCharacteristic ec : characteristics)
            ExceptionSearchEngine.searchRule(ec);
    }
}
