package com.seven.asimov.it.utils.logcat.tasks.preemptiveSocketClosureTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.PreemptiveWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreemptiveTask extends Task<PreemptiveWrapper> {
    private static final String TAG = PreemptiveTask.class.getSimpleName();
    private String preemptivePortRegexp =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*Preemptive port configuration updated to port_range=('%s'), detection_time=(%s), cooldown_time=(%s)";

    private String preemptiveIpRegexp =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*New preemptive ip configuration for '(\\*)' was added \\(port_conf_number = ([0-9]*)\\)";

    private String pscPreemptivePortRegexp = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*Ports \\[%s\\], cooldown_time: (%s), detection_time: (%s)";

    private Pattern preemptivePortPattern;
    private Pattern preemptiveIpPattern;
    private Pattern pscPreemptivePortPattern;

    private String portRange;
    private String detectionTime;
    private String cooldownTime;

    public PreemptiveTask(String portRange, String cooldownTime, String detectionTime) {
        this.portRange = portRange;
        this.cooldownTime = cooldownTime;
        this.detectionTime = detectionTime;
        preemptivePortRegexp = String.format(preemptivePortRegexp, portRange, detectionTime, cooldownTime);
        preemptivePortPattern = Pattern.compile(preemptivePortRegexp, Pattern.CASE_INSENSITIVE);
        Log.v(TAG, "Preemptive Port Regexp= " + preemptivePortRegexp);
        pscPreemptivePortRegexp = String.format(pscPreemptivePortRegexp, portRange, cooldownTime, detectionTime);
        pscPreemptivePortPattern = Pattern.compile(pscPreemptivePortRegexp, Pattern.CASE_INSENSITIVE);
        Log.v(TAG, "PSC Preemptive Port Regexp= " + pscPreemptivePortRegexp);
    }

    public PreemptiveTask() {
        preemptiveIpPattern = Pattern.compile(preemptiveIpRegexp, Pattern.CASE_INSENSITIVE);
        Log.v(TAG, "Preemptive IP Regexp= " + preemptiveIpRegexp);
    }

    @Override
    protected PreemptiveWrapper parseLine(String line) {
        if (preemptivePortPattern != null) {
            Matcher preemptivePortMatcher = preemptivePortPattern.matcher(line);
            if (preemptivePortMatcher.find()) {
                PreemptiveWrapper preemptiveWrapper = new PreemptiveWrapper();
                setTimestampToWrapper(preemptiveWrapper, preemptivePortMatcher);
                preemptiveWrapper.setPortRange(portRange);
                preemptiveWrapper.setCooldownTime(cooldownTime);
                preemptiveWrapper.setDetectionTime(detectionTime);
                return preemptiveWrapper;
            }
        }
        if (preemptiveIpPattern != null) {
            Matcher preemptiveIpPatternMatcher = preemptiveIpPattern.matcher(line);
            if (preemptiveIpPatternMatcher.find()) {
                PreemptiveWrapper preemptiveWrapper = new PreemptiveWrapper();
                setTimestampToWrapper(preemptiveWrapper, preemptiveIpPatternMatcher);
                return preemptiveWrapper;
            }
        }
        if (pscPreemptivePortPattern != null) {
            Matcher pscPreemptivePortPatternMatcher = pscPreemptivePortPattern.matcher(line);
            if (pscPreemptivePortPatternMatcher.find()) {
                PreemptiveWrapper preemptiveWrapper = new PreemptiveWrapper();
                setTimestampToWrapper(preemptiveWrapper, pscPreemptivePortPatternMatcher);
                preemptiveWrapper.setPortRange(portRange);
                preemptiveWrapper.setCooldownTime(cooldownTime);
                preemptiveWrapper.setDetectionTime(detectionTime);
                return preemptiveWrapper;
            }
        }

        return null;
    }
}
