package com.seven.asimov.it.utils.logcat.tasks.securityTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.ProfileWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ProfileTask extends Task<ProfileWrapper> {

    private static final String TAG = ProfileTask.class.getSimpleName();

    private static final String PROFILE_REGEXP = "([A-Z]?)/Asimov::Java::TransportSettings\\(\\s?[0-9]+\\): " +
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).* Profile: .* OS : (.*) Vendor : .* " +
            "Model : (.*) IMSI : [0-9]+ IMEI : ([0-9]+) Host ID : [0-9]+ MSISDN : ([0-9]+)";
    private static final Pattern profilePattern = Pattern.compile(PROFILE_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected ProfileWrapper parseLine(String line) {
        Matcher matcher = profilePattern.matcher(line);
        if (matcher.find()) {
            ProfileWrapper wrapper = new ProfileWrapper();
            wrapper.setLogLevel(matcher.group(1));
            setTimestampToWrapper(wrapper, matcher, 2, 3);
            wrapper.setOs(matcher.group(4));
            wrapper.setModel(matcher.group(5));
            wrapper.setImei(Long.parseLong(matcher.group(6)));
            wrapper.setMsisdn(Long.parseLong(matcher.group(7)));
            return wrapper;
        }
        return null;
    }
}