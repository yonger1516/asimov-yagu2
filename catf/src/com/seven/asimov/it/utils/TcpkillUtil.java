package com.seven.asimov.it.utils;

import com.seven.asimov.it.IntegrationTestRunnerGa;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TcpkillUtil {
    private static final Logger logger = LoggerFactory.getLogger(TcpkillUtil.class.getSimpleName());
    private String hostIP;
    private Integer port;
    private Integer pid;
    private boolean run;
    private TcpkillMonitor monitor = new TcpkillMonitor();

    public TcpkillUtil(String hostIP, Integer port) {
        this.hostIP = hostIP;
        this.port = port;
        logger.info("new instance");
    }

    public void startProcess() {
        run = true;
        monitor.start();
    }

    private void runTcpkill() {
        try {
            String command = IntegrationTestRunnerGa.mTcpkillPath;
            if (StringUtils.isNotEmpty(hostIP)) {
                command += " host " + hostIP;
            }
            if (port != null) {
                command += " port " + port;
            }
            logger.info("dropConnection: command:" + command);
            String[] suCommand = {"su", "-c", command};  // + " &"
            List<Integer> beforePids = SystemUtil.getPids("tcpkill");
            logPids(beforePids);
            Process process = Runtime.getRuntime().exec(suCommand);
            //Log.v(TAG, "dropConnection: process:");
            //Log.v(TAG, "dropConnection: getInputStream() " + Shell.getStreamData(process.getInputStream()));
            //Log.v(TAG, "dropConnection: getErrorStream() " + Shell.getStreamData(process.getErrorStream()));
            List<Integer> afterePids = SystemUtil.getPids("tcpkill");
            logPids(afterePids);
            afterePids.removeAll(beforePids);
            logPids(afterePids);
            if (afterePids.size() != 1) {
                ShellUtil.killProcesses(afterePids);
                throw new AssertionError("Tcpkill process count must be 1!");
            }
            pid = afterePids.get(0);
            logger.info("process " + pid + " was created");
            //process.waitFor();
        } catch (Exception e) {
            logger.debug(ExceptionUtils.getStackTrace(e));
        }
    }

    private void logPids(List<Integer> pids) {
        logger.info("logPids: start");
        for (int pid : pids) {
            logger.info("pid=" + pid);
        }
    }


    public void stopProcess() throws IOException, InterruptedException {
        run = false;
        //wait for TcpkillMonitor to finish
        Thread.sleep(3 * 1000);
        ShellUtil.kill(pid, 9);
    }

    class TcpkillMonitor extends Thread {
        public void run() {
            logger.info("thread run()");
            while (run) {
                if (pid == null) {
                    runTcpkill();
                } else if (!SystemUtil.processExist(pid)) {
                    logger.info("process " + pid + " not found!");
                    runTcpkill();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    logger.debug(ExceptionUtils.getStackTrace(ie));
                }
            }
        }
    }

    private static class SystemUtil {

        public static List<Integer> getPids(String attr) {
            Matcher matcher;
            List<Integer> pids = new ArrayList<Integer>();
            final Pattern pidPatern = Pattern.compile(String.format(TFConstantsIF.PID_REGEXP, "tcpkill"), Pattern.CASE_INSENSITIVE);
            String output = ShellUtil.execSimple("ps " + attr);
            String[] lines = output.split("\n");
            for (String line : lines) {
                if (line.startsWith("USER")) continue;
                matcher = pidPatern.matcher(line);
                if (matcher.find()) {
                    pids.add(Integer.valueOf(matcher.group(2)));
                    logger.info("getPids: found pid: " + matcher.group(2));
                } else {
                    logger.info("getPids: ps line " + line + " doesn't match to regex!");
                }
            }
            return pids;
        }

        public static boolean processExist(int pid) {
            List<Integer> pids = getPids(Integer.toString(pid));
            int osPid;
            if (pids.size() > 0 && pids.get(0) != null) {
                osPid = pids.get(0);
            } else {
                return false;
            }
            logger.info("processExist: pid=" + pid + " osPid=" + osPid);
            return pid == osPid;
        }
    }
}
