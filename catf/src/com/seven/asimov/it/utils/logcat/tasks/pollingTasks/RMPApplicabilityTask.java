package com.seven.asimov.it.utils.logcat.tasks.pollingTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.RMPApplicabilityWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RMPApplicabilityTask extends Task<RMPApplicabilityWrapper> {
    private static final String TAG = RMPApplicabilityTask.class.getSimpleName();

    private static final String CHECKING_RMP_APPLICABILITY_REGEXP = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*Checking RMP applicability: eventTime (\\d+), RMP started at (\\d+), RMP TTL\\(\\) (\\d+) sec, RMP expires at (\\d+)";
    private static final Pattern checkingRMPApplicabilityPattern = Pattern.compile(CHECKING_RMP_APPLICABILITY_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected RMPApplicabilityWrapper parseLine(String line) {
        Matcher matcherRMPApplicability = checkingRMPApplicabilityPattern.matcher(line);
        if (matcherRMPApplicability.find()) {
            RMPApplicabilityWrapper rmpApplicWrapper = new RMPApplicabilityWrapper();
            setTimestampToWrapper(rmpApplicWrapper, matcherRMPApplicability);
            rmpApplicWrapper.setEventTime(Long.parseLong(matcherRMPApplicability.group(3)));
            rmpApplicWrapper.setStartedAt(Long.parseLong(matcherRMPApplicability.group(4)));
            rmpApplicWrapper.setTTL(Long.parseLong(matcherRMPApplicability.group(5)));
            rmpApplicWrapper.setExpires(Long.parseLong(matcherRMPApplicability.group(6)));
            return rmpApplicWrapper;
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
