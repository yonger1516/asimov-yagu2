package com.seven.asimov.it.utils.logcat.wrappers;

import android.util.Log;

public class CashEntryWraper extends LogEntryWrapper {

    private static CashEntryWraper wraper = new CashEntryWraper();

    private String ceId;
    private boolean isDelitedFromBD;
    private boolean isDelitedFromFS;
    private boolean isDelitedFromCash;

    private CashEntryWraper(){}

    public static CashEntryWraper getInstanse() {
        return wraper;
    }

    public String getCeId() {
        return ceId;
    }

    public void setCeId(String ceId) {
        this.ceId = ceId;
    }

    public boolean isDelitedFromBD() {
        return isDelitedFromBD;
    }

    public void setDelitedFromBD(boolean delitedFromBD) {
        isDelitedFromBD = delitedFromBD;
    }

    public boolean isDelitedFromFS() {
        return isDelitedFromFS;
    }

    public void setDelitedFromFS(boolean delitedFromFS) {
        isDelitedFromFS = delitedFromFS;
    }

    public boolean isDelitedFromCash() {
        return isDelitedFromCash;
    }

    public void setDelitedFromCash(boolean delitedFromCash) {
        isDelitedFromCash = delitedFromCash;
    }

    public boolean isAllRemoved() {
        return isDelitedFromCash && isDelitedFromFS && isDelitedFromBD;
    }

    @Override
    public String toString() {
        return "CashEntryWraper{" +
                "ceId='" + ceId + '\'' +
                ", isDelitedFromBD=" + isDelitedFromBD +
                ", isDelitedFromFS=" + isDelitedFromFS +
                ", isDelitedFromCash=" + isDelitedFromCash +
                '}';
    }
}
