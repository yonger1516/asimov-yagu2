package com.seven.asimov.it.utils.sysmonitor;

import android.util.Log;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public enum CPULoad {
    INSTANCE;

    private static final String TAG = CPULoad.class.getSimpleName();
    private CpuUpdateThread cpuUpdateThread;
    private boolean updateThreadWorks = false;
    private ArrayList<Integer> process = new ArrayList<Integer>();
    private ConcurrentHashMap<Integer, Float> processCpuUsageMap = new ConcurrentHashMap<Integer, Float>();
    private volatile float totalCpuUsage = 1.1F;

    public void addProcess(Integer... pid) {
        if (updateThreadWorks) {
            cpuUpdateThread.addPid(pid);
            Log.i(TAG, "CPULoad, addProcess, updateThreadWorks= " + updateThreadWorks + "  cpuUpdateThread.pidList.size()= " + cpuUpdateThread.pidList.size());
        } else {
            process.addAll(Arrays.asList(pid));
            Log.i(TAG, "CPULoad, addProcess, updateThreadWorks= " + updateThreadWorks + "  process.size()= " + process.size());
        }
    }

    /**
     * Process Cpu Usage in percent
     *
     * @param pid Process pid
     * @return Process Cpu usage or null if no such pid in processCpuUsageMap
     */
    public Float getProcessCpuUsage(Integer pid) {
        Float result = processCpuUsageMap.get(pid);
        return result;
    }

    /**
     * Total Cpu Usage in percent
     *
     * @return total Cpu usage
     */
    public float getTotalCpuUsage() {
        if (totalCpuUsage < 0) return 0;
        if (totalCpuUsage > 100) return 100;
        return totalCpuUsage;
    }

    public void removeProcess(Integer pid) {
        cpuUpdateThread.removePid(pid);
        if (processCpuUsageMap.containsKey(pid)) processCpuUsageMap.remove(pid);
    }

    public void startUpdate() throws InterruptedException {
        startUpdate(1000L);
    }

    public void startUpdate(long intervalForUpdate) throws InterruptedException {
        if (updateThreadWorks) return;
        cpuUpdateThread = new CpuUpdateThread();
        cpuUpdateThread.sleepTime = intervalForUpdate;
        if (!process.isEmpty()) cpuUpdateThread.pidList.addAll(process);
        process.clear();
        updateThreadWorks = true;
        cpuUpdateThread.start();
        Thread.sleep(5 * 1000);
    }

    public void stopUpdate() throws InterruptedException {
        cpuUpdateThread.stopThread();
        cpuUpdateThread.join();
        updateThreadWorks = false;
    }

    private class CpuUpdateThread extends Thread {
        private final String TAG = CpuUpdateThread.class.getSimpleName();
        private HashMap<Integer, ProcessStat> oldProcessStatMap = new HashMap<Integer, ProcessStat>();
        private HashMap<Integer, ProcessStat> newProcessStatMap = new HashMap<Integer, ProcessStat>();
        private ArrayList<Integer> pidList = new ArrayList<Integer>();
        private CpuStat oldCpuStat;
        private CpuStat newCpuStat;
        private boolean continueUpdate = true;
        private String file = "/proc/stat";
        private long sleepTime;

        public void addPid(Integer... pid) {
            for (Integer processPid : pid) {
                if (!pidList.contains(processPid)) {
                    pidList.add(processPid);
                }
            }
            Log.i(TAG, "addPid, pidList.size() = " + pidList.size());
        }

        private void calculateProcessCpuUsage() {
            Iterator<Integer> iterator = newProcessStatMap.keySet().iterator();
            while (iterator.hasNext()) {
                Integer pid = iterator.next();
                long deltaTime;
                long totalDeltaTime;
                if (isOldProcess(pid)) {
                    long deltaUTime = newProcessStatMap.get(pid).getuTime() - oldProcessStatMap.get(pid).getuTime();
                    long deltaSTime = newProcessStatMap.get(pid).getsTime() - oldProcessStatMap.get(pid).getsTime();
                    deltaTime = deltaUTime + deltaSTime;
                } else {
                    deltaTime = 0L;
                }
                totalDeltaTime = (newCpuStat.getUserTime() + newCpuStat.getSystemTime() + newCpuStat.getNiceTime() +
                        newCpuStat.getIdleTime()) - (oldCpuStat.getUserTime() + oldCpuStat.getSystemTime() +
                        oldCpuStat.getNiceTime() + oldCpuStat.getIdleTime());
                float processCpuUsage = (float) (deltaTime * 100 / totalDeltaTime);
                processCpuUsageMap.put(pid, processCpuUsage);
            }
        }

        private void calculateTotalCpuUsage() {
            long totalTimeDif = newCpuStat.getTotalTime() - oldCpuStat.getTotalTime();
            float cpuUsage = (float) (totalTimeDif) / (newCpuStat.getIdleTime() + totalTimeDif - oldCpuStat.getIdleTime());
            totalCpuUsage = 100.0F * cpuUsage;
        }

        private long calculateTotalTime(CpuStat cpuStat) {
            return cpuStat.getUserTime() + cpuStat.getNiceTime() + cpuStat.getSystemTime() +
                    cpuStat.getIoWaitTime() + cpuStat.getIrqTime() + cpuStat.getSoftIrqTime();
        }

        private void getProcessesStats() {
            newProcessStatMap = new HashMap<Integer, ProcessStat>();
            for (Integer pid : pidList) {
                ProcessStat processStat = readProcessStat(pid);
                if (processStat != null) {
                    newProcessStatMap.put(pid, processStat);
                }
            }
        }

        private boolean isOldProcess(Integer pid) {
            return oldProcessStatMap.containsKey(pid);
        }

        private CpuStat readCpuStat() {
            CpuStat cpuStat;
            String[] array = readFile(file);
            cpuStat = new CpuStat(Long.parseLong(array[1]), Long.parseLong(array[2]),
                    Long.parseLong(array[3]), Long.parseLong(array[4]), Long.parseLong(array[5]),
                    Long.parseLong(array[6]), Long.parseLong(array[7]), Long.parseLong(array[8]),
                    Long.parseLong(array[9]), Long.parseLong(array[10]));
            cpuStat.setTotalTime(calculateTotalTime(cpuStat));
            return cpuStat;
        }

        /**
         * Read first line from the file and split it by regexp "\s+"
         *
         * @param fileName file name
         * @return Array of strings
         */
        private String[] readFile(String fileName) {
            BufferedReader bufferedReader = null;
            String statLine = "";
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)), 1000);
                statLine = bufferedReader.readLine();
            } catch (FileNotFoundException fne) {
                Log.e(TAG, ExceptionUtils.getStackTrace(fne));
            } catch (IOException ioe) {
                Log.e(TAG, ExceptionUtils.getStackTrace(ioe));
            } finally {
                try {
                    if (bufferedReader != null) bufferedReader.close();
                } catch (IOException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                }
            }
            return statLine.split(" +");
        }

        /**
         * Read process stat file based on the pid
         *
         * @param pid process pid
         * @return ProcessStat object or null if illegal pid
         */
        private ProcessStat readProcessStat(int pid) {
            ProcessStat processStat;
            String[] array = readFile("/proc/" + pid + "/stat");
            if (array.length == 1) return null;
            processStat = new ProcessStat(pid, Long.parseLong(array[13]), Long.parseLong(array[14]));
            return processStat;
        }

        /**
         * Remove pid from PID list
         *
         * @param pid process pid
         */
        public void removePid(Integer pid) {
            Iterator<Integer> iterator = pidList.iterator();
            while (iterator.hasNext()) {
                Integer processPid = iterator.next();
                if (processPid.equals(pid)) {
                    iterator.remove();
                    return;
                }
            }
        }

        @Override
        public void run() {
            oldCpuStat = readCpuStat();
            sleepThread(1000L);
            while (continueUpdate) {
                newCpuStat = readCpuStat();
                if (!pidList.isEmpty()) {
                    getProcessesStats();
                    calculateProcessCpuUsage();
                    oldProcessStatMap = newProcessStatMap;
                }
                calculateTotalCpuUsage();
                oldCpuStat = newCpuStat;
                sleepThread(sleepTime);
            }
        }

        private void sleepThread(long time) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                Log.e(TAG, ExceptionUtils.getStackTrace(e));
            }
        }

        public void stopThread() {
            continueUpdate = false;
        }
    }
}
