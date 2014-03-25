package com.seven.asimov.it.utils.datausage;

import android.content.Context;
import com.seven.asimov.it.utils.tcpdump.Direction;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: imiflig
 * Date: 11/4/13
 * Time: 1:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationDataUsage {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationDataUsage.class.getSimpleName());

    private Context context;
    private long inDataUsage;
    private long outDataUsage;
    private String packageName;
    private int infoFlag;

    /*
    public ApplicationDataUsage(Context context,String packageName) {
        this.context = context;
        this.packageName = packageName;
        this.infoFlag = PackageManager.GET_META_DATA;
        countData();
    }
    */
    public ApplicationDataUsage(Context context, String packageName, int infoFlag) {
        this.context = context;
        this.packageName = packageName;
        this.infoFlag = infoFlag;
        countData();
    }

    private void countData() {
        try {
            inDataUsage = DataUsageUtil.getDataUsage(context, packageName, Direction.TO_US, infoFlag);
            outDataUsage = DataUsageUtil.getDataUsage(context, packageName, Direction.FROM_US, infoFlag);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public long getInDataUsage() {
        return inDataUsage;
    }

    public long getOutDataUsage() {
        return outDataUsage;
    }

    public long getInPlusOutDataUsage() {
        return inDataUsage + outDataUsage;
    }

    @Override
    public String toString() {
        return new StringBuilder(ApplicationDataUsage.class.toString()).append(" inDataUsage=").append(inDataUsage).
                append(" outDataUsage=").append(outDataUsage).toString();
    }
}

