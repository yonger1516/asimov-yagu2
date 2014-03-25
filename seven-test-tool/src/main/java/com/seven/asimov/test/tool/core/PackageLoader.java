package com.seven.asimov.test.tool.core;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class PackageLoader {
    private static final String TAG = "PackageLoader";

    public static String loadAutomatedTests(File destinationDir) {
        String itApkName = getITFileName();
        if (itApkName != null) {
            copyTestAppToFilesDir(itApkName, destinationDir);
            return itApkName;
        } else {
            return null;
        }
    }

    public static String getITFileName() {
        String name = null;
        String line;
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "ls /data/app/"});
            BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = is.readLine()) != null) {
                if (line.contains("com.seven.asimov.it")) {
                    name = line.trim();
                }
            }
            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return name;
    }

    private static boolean copyTestAppToFilesDir(String apkName, File destinationDir) {
        String fullPath = destinationDir + File.separator + apkName;
        File destinationFile = new File(fullPath);
        try {
            if (destinationFile.exists()) {
                destinationFile.delete();
            }
            Runtime.getRuntime().exec(new String[]{"su", "-c",
                    "cat /data/app/" + apkName + " > " + destinationDir + File.separator + apkName}).waitFor();

            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy apk to files dir: " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
