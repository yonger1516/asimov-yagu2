package com.seven.asimov.it.testcases;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.IpTablesUtil;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CacheSizeTestCase extends TcpDumpTestCase{
    private static final String TAG = CacheSizeTestCase.class.getSimpleName();

    protected void fillSDCardMemoryUpTo300MBfree() {
        int mb = 300;
        long availableSpace = getExternalAvailableSpaceInBytes();
        if (availableSpace != -1L) {
            long space = availableSpace - mb * 1024 * 1024;
            File file = new File(Environment.getExternalStorageDirectory(), "bigFile");
            byte[] buff = new byte[1024];
            try {
                FileOutputStream f = new FileOutputStream(file);
                for (long i = 0; i < space / buff.length; i++)
                    f.write(buff, 0, buff.length);
                f.flush();
                f.close();
            } catch (Exception e) {
                Log.e(TAG, ExceptionUtils.getMessage(e));
            }
        }
    }

    protected void freeMemory() {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "bigFile");
            file.delete();
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getMessage(e));
            e.printStackTrace();
        }
    }

    protected static long getExternalAvailableSpaceInBytes() {
        long availableSpace = -1L;
        try {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtils.getMessage(e));
        }
        return availableSpace;
    }

    protected static long getAvailableMemoryInMB() {
        return (getExternalAvailableSpaceInBytes()/1024L/1024L);
    }

    protected void forbidTraffic(boolean forbid) throws IOException, InterruptedException {
        final int csa = IpTablesUtil.getApplicationUid(getContext(), "com.seven.asimov");
        final int csat = IpTablesUtil.getApplicationUid(getContext(), "com.seven.asimov.it");
        if (forbid){
            IpTablesUtil.banNetworkForAllApplications(true);
            IpTablesUtil.allowNetworkForApplication(true, csa);
            IpTablesUtil.allowNetworkForApplication(true, csat);
        }else{
            IpTablesUtil.banNetworkForAllApplications(false);
            IpTablesUtil.allowNetworkForApplication(false, csa);
            IpTablesUtil.allowNetworkForApplication(false, csat);
        }
    }
}
