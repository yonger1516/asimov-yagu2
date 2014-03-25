package com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks;


import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.TcpKillWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TcpKillTask extends Task <TcpKillWrapper> {
    public static final int TCP_KILL_REGEX_NUMBER = 1;
    public static final int SIGCHLD_REGEX_NUMBER = 2;
    private static final String TAG = TcpKillTask.class.getSimpleName();
    private static final String TCP_KILL_REGEX =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*Started tcpkill process, PID ([0-9]*)";
    private static final String SIGCHLD_REGEX =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*SIGCHLD received from PID ([0-9]*)";
    private String KILL_PORTS_REGEX =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*Going to kill ports \"%s\" \\(interface: default\\)";
    private Pattern tcpKillPattern;
    private Pattern killPortsPattern;

    public TcpKillTask(int regexpNumber) {
        switch (regexpNumber) {
            case 1:
                Log.v(TAG, "Tcp Kill Regexp = " + TCP_KILL_REGEX);
                tcpKillPattern = Pattern.compile(TCP_KILL_REGEX, Pattern.CASE_INSENSITIVE);
                break;
            case 2:
                Log.v(TAG, "SIGCHLD Regexp = " + SIGCHLD_REGEX);
                tcpKillPattern = Pattern.compile(SIGCHLD_REGEX, Pattern.CASE_INSENSITIVE);
                break;
        }

    }

    public TcpKillTask(String ports) {
        KILL_PORTS_REGEX = String.format(KILL_PORTS_REGEX, ports);
        Log.v(TAG, "Kill ports Regexp = " + KILL_PORTS_REGEX);
        killPortsPattern = Pattern.compile(KILL_PORTS_REGEX, Pattern.CASE_INSENSITIVE);
    }

    @Override
    protected TcpKillWrapper parseLine(String line) {
        if (tcpKillPattern != null) {
            Matcher matcher = tcpKillPattern.matcher(line);
            if (matcher.find()) {
                TcpKillWrapper wrapper = new TcpKillWrapper();
                setTimestampToWrapper(wrapper, matcher);
                wrapper.setPID(Integer.parseInt(matcher.group(3)));
                return wrapper;
            }
        }
        if (killPortsPattern != null) {
            Matcher matcher = killPortsPattern.matcher(line);
            if (matcher.find()) {
                TcpKillWrapper wrapper = new TcpKillWrapper();
                setTimestampToWrapper(wrapper, matcher);
                return wrapper;
            }
        }
        return null;
    }
}
