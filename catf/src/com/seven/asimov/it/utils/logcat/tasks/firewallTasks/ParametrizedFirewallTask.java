package com.seven.asimov.it.utils.logcat.tasks.firewallTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.ParametrizedFirewallWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParametrizedFirewallTask extends Task<ParametrizedFirewallWrapper> {
    private static final String TAG = ParametrizedFirewallTask.class.getSimpleName();
    private static final String digitRegexp = "\\d+";
    private static final String commonTypeRegexp = "rule|chain";
    private static final String allRegexp = ".*";
    private static final String commonEventRegexp = "created|deleted|inserted|enabled|disabled|flushed";
    private static String regexp = "(201[0-9]/[0-9]+/[0-9]+.[0-9]+:[0-9]+:[0-9]+.[0-9]+).([A-Z]*).*FirewallLog:\\s(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*),firewall,(%s),(%s),(%s),(%s),(%s),(%s),(%s),(%s),(%s)";

    private Pattern pattern;

    public ParametrizedFirewallTask() {
        this(null, null, null, null, null, null, null, null, null);
    }

    public ParametrizedFirewallTask(String versionRegexp, String regexpType, String idChainRegexp, String nameRegexp, String actionRegexp, String eventRegexp, String interfaceRegexp, String ipVersionRegexp, String sequenceNumberRegexp) {
        regexp = String.format(regexp,
                versionRegexp == null ? digitRegexp : versionRegexp,
                regexpType == null ? commonTypeRegexp : regexpType,
                idChainRegexp == null ? allRegexp : idChainRegexp,
                nameRegexp == null ? allRegexp : nameRegexp,
                actionRegexp == null ? allRegexp : actionRegexp,
                eventRegexp == null ? commonEventRegexp : eventRegexp,
                interfaceRegexp == null ? allRegexp : interfaceRegexp,
                ipVersionRegexp == null ? allRegexp : ipVersionRegexp,
                sequenceNumberRegexp == null ? allRegexp : sequenceNumberRegexp);

        pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
    }

    @Override
    protected ParametrizedFirewallWrapper parseLine(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            ParametrizedFirewallWrapper wrapper = new ParametrizedFirewallWrapper();
            setTimestampToWrapper(wrapper, matcher);

            wrapper.setVersion(matcher.group(4));
            wrapper.setType(matcher.group(5));
            wrapper.setChainId(matcher.group(6));
            wrapper.setName(matcher.group(7));
            wrapper.setAction(matcher.group(8));
            wrapper.setEvent(matcher.group(9));
            wrapper.setInterfaceType(matcher.group(10));
            wrapper.setIpVersion(matcher.group(11));
            wrapper.setSequenceNumber(matcher.group(12));
            return wrapper;
        }
        return null;
    }

    public static void main(String... args) {
//        String s2 = "12-22 11:12:24.575 D/Asimov::Native::OCEngine(15040): 2013/12/22 11:12:24.578920 GMT 16237 [DEBUG]\t[report_service.cpp:408] (0) - FirewallLog: 2013-12-22 11:12:24.578,firewall,4,rule,954902655,,accept,inserted,,unknown,1";
//        String s2 = "12-22 11:12:24.575 D/Asimov::Native::OCEngine(15040): 2013/12/22 11:12:24.579083 GMT 16237 [DEBUG]\t[report_service.cpp:408] (0) - FirewallLog: 2013-12-22 11:12:24.578,firewall,4,chain,954902655,,none,enabled,,ipv4|ipv6,2";
//        String s2 = "12-22 11:16:56.285 D/Asimov::Native::OCEngine(17482): 2013/12/22 11:16:56.291344 GMT 17529 [DEBUG]\t[report_service.cpp:408] (0) - FirewallLog: 2013-12-22 11:16:56.266,firewall,4,chain,0,Z7BASECHAIN,none,flushed,,ipv4|ipv6,4";
        String s2 = "12-22 11:17:08.795 D/Asimov::Native::OCEngine(17482): 2013/12/22 11:17:08.800493 GMT 17529 [DEBUG]\t[report_service.cpp:408] (0) - FirewallLog: 2013-12-22 11:17:08.768,firewall,4,chain,901012060,,none,enabled,,ipv4|ipv6,7";
        ParametrizedFirewallTask task = new ParametrizedFirewallTask(null, null, null, null, null, null, null, null, null);
        ParametrizedFirewallWrapper wrapper = task.parseLine(s2);
        System.out.println(wrapper);
        System.out.println(wrapper.getTimestamp());
    }
}
