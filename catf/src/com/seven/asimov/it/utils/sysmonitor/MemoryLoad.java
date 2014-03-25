package com.seven.asimov.it.utils.sysmonitor;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;

public class MemoryLoad {

    private static final String TAG = MemoryLoad.class.getSimpleName();
    public static final int KB = 0;
    public static final int MB = 1;
    public static final int GB = 2;

    private Context mContext;

    public MemoryLoad(Context paramContext) {
        this.mContext = paramContext;
    }

    /**
     * Return information about the available memory
     *
     * @return Return information about the available memory (Mb)
     */
    public float getAvailableMemory() {
        StatFs localStatFs = new StatFs(Environment.getDataDirectory().getPath());
        return (float) ((long) localStatFs.getBlockSize() * (long) localStatFs.getFreeBlocks()) / 1024.0F / 1024.0F;
    }

    /**
     * Return information about the available RAM
     *
     * @return Return information about the available RAM (Kb)
     */
    public int getAvailableRAM() {
        ActivityManager localActivityManager = (ActivityManager) mContext.getSystemService("activity");
        ActivityManager.MemoryInfo localMemoryInfo = new ActivityManager.MemoryInfo();
        localActivityManager.getMemoryInfo(localMemoryInfo);
        return (int) (localMemoryInfo.availMem / 1024L);
    }

    /**
     * Return information about used RAM
     */
    public int getUsedRAM() {
        return getTotalRAM() - getAvailableRAM();
    }

    /**
     * Return information about the memory usage of one or more processes.
     *
     * @param pid The pid of the process or processes whose memory usage is to be retrieved.
     * @return Returns memory usage of one or more processes (Kb).
     */
    public int getProcessMemory(int... pid) {
        ActivityManager localActivityManager = (ActivityManager) mContext.getSystemService("activity");
        Debug.MemoryInfo[] memoryInfoArr = localActivityManager.getProcessMemoryInfo(pid);
        int sumMem = 0;
        for (Debug.MemoryInfo memInfo : memoryInfoArr) {
            sumMem += memInfo.getTotalPss();
        }
        return sumMem;
    }

    /**
     * Return information about the total memory
     *
     * @return Return information about the total memory (Mb)
     */
    public float getTotalMemory() {
        StatFs localStatFs = new StatFs(Environment.getDataDirectory().getPath());
        return (float) ((long) localStatFs.getBlockSize() * (long) localStatFs.getBlockCount()) / 1024.0F / 1024.0F;
    }

    /**
     * Return information about the total RAM
     *
     * @return Return information about the total RAM (Kb)
     */
    public int getTotalRAM() {
        int size = 0;
        try {
            String[] arrayOfString = new java.io.RandomAccessFile("/proc/meminfo", "r").readLine().split(" kB")[0].split(" ");
            size = Integer.parseInt(arrayOfString[(-1 + arrayOfString.length)]);
            return Math.round(size);
        } catch (IOException localIOException) {
            Log.e(TAG, ExceptionUtils.getStackTrace(localIOException));
        }
        return size;
    }
}
