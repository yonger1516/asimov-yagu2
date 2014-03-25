package com.seven.asimov.it.utils.datausage;

import android.content.Context;

public class ApplicationDataDifference {
    private static final String TAG = ApplicationDataDifference.class.getSimpleName();
    private Context context;
    private String packageName;
    private int infoFlag;

    private ApplicationDataUsage startData = null;
    private ApplicationDataUsage endData = null;

    public ApplicationDataDifference(Context context, String packageName, int infoFlag) {
        this.context = context;
        this.packageName = packageName;
        this.infoFlag = infoFlag;
    }

    public void startDataCollecting() {
        startData = new ApplicationDataUsage(context, packageName, infoFlag);
    }

    public void endDataCollecting() {
        endData = new ApplicationDataUsage(context, packageName, infoFlag);
    }

    /*
    public ApplicationDataUsage getStartData() {
        return startData;
    }

    public ApplicationDataUsage getEndData() {
        return endData;
    }
    */
    public long getInDataDifference() {
        return endData.getInDataUsage() - startData.getInDataUsage();
    }

    public long getOutDataDifference() {
        return endData.getOutDataUsage() - startData.getOutDataUsage();
    }

    public long getInPlusOutDataDifference() {
        return endData.getInPlusOutDataUsage() - startData.getInPlusOutDataUsage();
    }

    public long getClearOutDataDifference() {
        return getOutDataDifference() - getInDataDifference();
    }

    public long getClearInDataDifference() {
        return -getClearOutDataDifference();
    }

    @Override
    public String toString() {
        return new StringBuilder(TAG).append(" packageName=").append(packageName).append("\n").
                append(" startData=").append(startData).append("\n").
                append(" endData=").append(endData).append("\n").
                append(" inDataDifference=").append(getInDataDifference()).append("\n").
                append(" outDataDifference=").append(getOutDataDifference()).append("\n").
                toString();
    }

}
