package com.seven.asimov.it.utils.sysmonitor;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import com.seven.asimov.it.utils.ShellUtil;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemMonitorUtil {

    private static SystemMonitorUtil instance;
    private String ocPackageName;
    private String reportFileName;
    private String directoryPath;
    private Context context;
    private List<String> processesName;
    private InfoCollectorThread infoCollectorThread;
    private PidReaderThread pidReaderThread;
    private boolean writeToFile = true;
    private static boolean withOc;
    private static List<AppInfo> testResults;

    private SystemMonitorUtil(Context context, String ocPackageName, String reportFileName, String directoryPath) {
        this.ocPackageName = ocPackageName;
        this.reportFileName = reportFileName;
        this.directoryPath = directoryPath;
        this.context = context;
    }

    public static SystemMonitorUtil getInstance(Context context, String ocPackageName, String reportFileName, String directoryPath) {
        if (instance == null) {
            instance = new SystemMonitorUtil(context, ocPackageName, reportFileName, directoryPath);
        }
        withOc = ocPackageName != null;
        return instance;
    }

    public void start() throws IOException, InterruptedException, PackageManager.NameNotFoundException {
        processesName = new ArrayList<String>();
        if (withOc) {

            processesName.add("occ");
            processesName.add(ocPackageName);
            readDispatchersName();
        }
        infoCollectorThread = new InfoCollectorThread(withOc, processesName);
        if (withOc) {
            pidReaderThread = new PidReaderThread(processesName, infoCollectorThread);
            pidReaderThread.start();
            Thread.sleep(3 * 1000);
        }
        infoCollectorThread.start();
        Thread.sleep(5 * 1000);
    }

    public void stop() throws InterruptedException {
        if (withOc) {
            pidReaderThread.stopPidReaderThread();
        }
        infoCollectorThread.stopThread();
        infoCollectorThread.join();
        instance = null;
    }

    public void startedNewTest(String testName) {
        infoCollectorThread.setTestName(testName);
    }

    public void endedTest() {
        infoCollectorThread.testEnded();
    }

    private void readDispatchersName() throws IOException, InterruptedException {
        String[] chmodDirectory = {"su", "-c", "chmod -R 777 /data/"};
        Runtime.getRuntime().exec(chmodDirectory).waitFor();
        Thread.sleep(5000);
        String dispatcherFilePath = "/data/misc/openchannel/dispatchers.cfg";
        String defaultDispatcherFilePath = "/data/misc/openchannel/dispatchers_default.cfg";
        BufferedReader bufferedReader = null;
        final String DISPATCHER_REGEXP = "([a-z]+);([1-9]);(.*);;([0-9]+)";
        final Pattern dispatcherPattern = Pattern.compile(DISPATCHER_REGEXP, Pattern.CASE_INSENSITIVE);
        try {
            String line;
            if (new File(dispatcherFilePath).exists()) {
                bufferedReader = new BufferedReader(new FileReader(dispatcherFilePath));
            } else if (new File(defaultDispatcherFilePath).exists()) {
                bufferedReader = new BufferedReader(new FileReader(defaultDispatcherFilePath));
            } else {
                throw new FileNotFoundException("dispatchers.cfg and dispatchers_default.cfg are not exists");
            }
            while ((line = bufferedReader.readLine()) != null) {
                Matcher matcher = dispatcherPattern.matcher(line);
                if (matcher.find()) {
                    processesName.add(matcher.group(1));
                }
            }
        } finally {
            if (bufferedReader != null) bufferedReader.close();
        }
    }

    public void setWriteToFile(boolean writeToFile) {
        this.writeToFile = writeToFile;
    }

    public static List<AppInfo> getTestResults() {
        return testResults;
    }

    private class InfoCollectorThread extends Thread {
        private final String TAG = InfoCollectorThread.class.getSimpleName();
        private boolean continueWork = true;
        private MemoryLoad memoryLoad;
        private CPULoad cpuLoad;
        private boolean withOC;
        private List<AppInfo> infoList;
        private List<String> ocProcessNameList;
        private List<String> columnNameList;
        private List<String> testNameList;
        private int index = -1;
        private boolean needToClearPreviousInfo = false;
        private boolean needToSaveInfo = false;
        private ExcelReport excelReport;
        private ConcurrentHashMap<String, Integer> ocProcessesMap = new ConcurrentHashMap<String, Integer>();

        private InfoCollectorThread(boolean withOC, List<String> processNameList) throws InterruptedException,
                IOException, PackageManager.NameNotFoundException {
            this.withOC = withOC;
            this.ocProcessNameList = processNameList;
            this.cpuLoad = CPULoad.INSTANCE;
            this.cpuLoad.startUpdate();
            this.memoryLoad = new MemoryLoad(context);
            this.infoList = new ArrayList<AppInfo>();
            this.testNameList = new ArrayList<String>();
            this.excelReport = new ExcelReport(context, ocPackageName, reportFileName, directoryPath);
        }

        public void stopThread() throws InterruptedException {
            this.continueWork = false;
            this.cpuLoad.stopUpdate();
            if (needToSaveInfo) {
                needToSaveInfo = false;
                saveTestInfo();
            }
        }

        @Override
        public void run() {
            createColumnNameList();
            while (true) {
                if (needToSaveInfo) {
                    needToSaveInfo = false;
                    saveTestInfo();
                }
                if (needToClearPreviousInfo) {
                    infoList.clear();
                    needToClearPreviousInfo = false;
                }
                if (withOC) {
                    collectInfoWithOC();
                } else {
                    collectInfo();
                }
                if (!continueWork) {
                    break;
                }

                sleepThread(3 * 1000);
            }
        }

        public void addPid(String process, int pid) {
            ocProcessesMap.put(process, pid);
        }

        private void collectInfo() {
            AppInfo appInfo = new AppInfo();
            appInfo.setTime(System.currentTimeMillis());
            appInfo.setCpuTotal(cpuLoad.getTotalCpuUsage());
            appInfo.setTotalMemory(memoryLoad.getTotalMemory());
            appInfo.setAvailableMemory(memoryLoad.getAvailableMemory());
            appInfo.setTotalRAM(memoryLoad.getTotalRAM());
            appInfo.setUsedRAM(memoryLoad.getUsedRAM());
            infoList.add(appInfo);
        }

        private void createColumnNameList() {
            columnNameList = new ArrayList<String>();
            columnNameList.add("Timestamp");
            columnNameList.add("Total CPU usage, %");
            columnNameList.add("Available Memory, Mb");
            columnNameList.add("Total Memory, Mb");
            columnNameList.add("Total RAM size, Kb");
            columnNameList.add("Total RAM usage, Kb");
            if (withOC) {
                columnNameList.add("Controller CPU usage, %");
                columnNameList.add("Controller RAM usage, Kb");
                columnNameList.add("Engine CPU usage, %");
                columnNameList.add("Engine RAM usage, Kb");
                for (int i = 2; i < processesName.size(); i++) {
                    columnNameList.add(processesName.get(i) + " CPU usage, %");
                    columnNameList.add(processesName.get(i) + " RAM usage, Kb");
                }
            }
        }

        private void collectInfoWithOC() {
            AppInfoOC appInfo = new AppInfoOC();
            appInfo.setTime(System.currentTimeMillis());
            appInfo.setCpuTotal(cpuLoad.getTotalCpuUsage());
            appInfo.setTotalMemory(memoryLoad.getTotalMemory());
            appInfo.setAvailableMemory(memoryLoad.getAvailableMemory());
            appInfo.setTotalRAM(memoryLoad.getTotalRAM());
            appInfo.setUsedRAM(memoryLoad.getUsedRAM());
            int pid;
            for (int i = 0; i < ocProcessNameList.size(); i++) {
                if (i == 0) {
                    pid = ocProcessesMap.get(ocProcessNameList.get(i));
                    Float cpuController = cpuLoad.getProcessCpuUsage(pid);
                    appInfo.setCpuController(cpuController == null ? -1 : cpuController);
                    appInfo.setMemController(memoryLoad.getProcessMemory(pid));
                } else if (i == 1) {
                    pid = ocProcessesMap.get(ocProcessNameList.get(i));
                    Float cpuEngine = cpuLoad.getProcessCpuUsage(pid);
                    appInfo.setCpuEngine(cpuEngine == null ? -1 : cpuEngine);
                    appInfo.setMemEngine(memoryLoad.getProcessMemory(pid));
                } else {
                    DispatcherInfo dispatcherInfo = new DispatcherInfo();
                    dispatcherInfo.setName(ocProcessNameList.get(i));
                    pid = ocProcessesMap.get(ocProcessNameList.get(i));
                    Float dispatcherCpu = cpuLoad.getProcessCpuUsage(pid);
                    dispatcherInfo.setCpuUsage(dispatcherCpu == null ? -1 : dispatcherCpu);
                    dispatcherInfo.setMemoryUsage(memoryLoad.getProcessMemory(pid));
                    appInfo.addDispatcher(dispatcherInfo);
                }
            }
            infoList.add(appInfo);
        }

        private void saveTestInfo() {
            testResults = new ArrayList<AppInfo>(infoList);
            Collections.copy(testResults, infoList);
            if (writeToFile) {
                excelReport.createNewSheet(testResults, testNameList.get(index), columnNameList);
            }
        }

        private void sleepThread(long time) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                Log.e(TAG, ExceptionUtils.getStackTrace(e));
            }
        }

        public void setTestName(String testName) {
            this.testNameList.add(testName);
            this.needToClearPreviousInfo = true;
        }

        public void testEnded() {
            index++;
            this.needToSaveInfo = true;
        }
    }

    private class PidReaderThread extends Thread {

        private final String TAG = PidReaderThread.class.getSimpleName();
        private HashMap<String, Integer> ocProcessesMap = new HashMap<String, Integer>();
        private List<String> ocProcessNameList;
        private CPULoad cpuLoad;
        private boolean firstTime = true;
        private InfoCollectorThread collectorThread;
        private boolean continueWork = true;

        private PidReaderThread(List<String> ocProcessNameList, InfoCollectorThread collectorThread) {
            this.ocProcessNameList = ocProcessNameList;
            this.collectorThread = collectorThread;
            this.cpuLoad = CPULoad.INSTANCE;
        }

        @Override
        public void run() {
            while (continueWork) {
                readPid();
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, ExceptionUtils.getStackTrace(e));
                }
            }
        }

        private void readPid() {
            String[] psOutput = ShellUtil.execSimple("ps").split("\\n+");
            for (String processName : ocProcessNameList) {
                for (String entry : psOutput) {
                    if (entry.endsWith(processName)) {
                        Log.i(TAG, entry);
                        String[] arr = entry.split("\\s+");
                        int newPid = Integer.parseInt(arr[1]);
                        if (!ocProcessesMap.containsKey(processName) || !ocProcessesMap.get(processName).equals(newPid)) {
                            updatePid(processName, newPid);
                        }
                    }
                }
            }
            firstTime = false;
        }

        private void updatePid(String process, int newPid) {
            if (!continueWork) return;
            cpuLoad.addProcess(newPid);
            if (!firstTime) cpuLoad.removeProcess(ocProcessesMap.get(process));
            collectorThread.addPid(process, newPid);
            ocProcessesMap.put(process, newPid);
        }

        public void stopPidReaderThread() {
            this.continueWork = false;
        }
    }
}
