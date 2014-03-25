package com.seven.asimov.test.tool.utils;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class MemoryStatusUtil {
    static private final int ERROR = -1;
    static private final int KiB = 0;
    static private final int MiB = 1;
    static private final int GiB = 2;

    static public boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    static public long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    static public long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    static public long getUsedInternalMemorySize() {
        return getTotalInternalMemorySize() - getAvailableInternalMemorySize();
    }

    static public long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return ERROR;
        }
    }

    static public long getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } else {
            return ERROR;
        }
    }

    static public long getUsedExternalMemorySize() {
        if (externalMemoryAvailable()) {
            return getTotalInternalMemorySize() - getAvailableInternalMemorySize();
        } else {
            return ERROR;
        }
    }

    static public String formatSizeKiB(long size) {
        return formatSize(size, KiB);
    }

    static public String formatSizeMiB(long size) {
        return formatSize(size, MiB);
    }

    static public String formatSizeGiB(long size) {
        return formatSize(size, GiB);
    }

    static private String formatSize(long size, int type) {
        String suffix = null;
        double s = 0;

        if (size >= 1024 && type >= KiB) {
            suffix = "KiB";
            s = size / 1024;
            if (s >= 1024 && type >= MiB) {
                suffix = "MiB";
                s /= 1024;
                if (s >= 1024 && type >= GiB) {
                    suffix = "GiB";
                    s /= 1024;
                }
            }
        }

        double newDouble = new BigDecimal(s).setScale(2, RoundingMode.UP).doubleValue();
        String result = Double.toString(newDouble);

        return result + suffix;
    }

    static public double sizeKiB(long size) {
        return size(size, KiB);
    }

    static public double sizeMiB(long size) {
        return size(size, MiB);
    }

    static public double sizeGiB(long size) {
        return size(size, GiB);
    }

    static private double size(long size, int type) {
        double s = 0;

        if (size >= 1024 && type >= KiB) {
            s = size / 1024;
            if (s >= 1024 && type >= MiB) {
                s /= 1024;
                if (s >= 1024 && type >= GiB) {
                    s /= 1024;
                }
            }
        }

        return new BigDecimal(s).setScale(2, RoundingMode.UP).doubleValue();
    }
}
