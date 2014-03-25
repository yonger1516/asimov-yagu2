package com.seven.asimov.it.utils.logcat.tasks.sessionBasicTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.DnsVerdictWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DnsVerdictTask extends Task<DnsVerdictWrapper> {
    private static final String TAG = DnsVerdictTask.class.getSimpleName();

    private String dnsVerdictRegexp = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*DTRX \\[(%s)\\].*: verdict (%s)";
    private Pattern dnsVerdictPattern;

    private static final String dnsRegex = "\\S+";

    public DnsVerdictTask(String dnsTransaction, String verdict) {
        Log.i(TAG, String.format("Task was initialized with corresponding verdict: %s", verdict));
        dnsVerdictRegexp = String.format(dnsVerdictRegexp,
                dnsTransaction == null ? dnsRegex : dnsTransaction,
                verdict == null ? dnsRegex : verdict);
        Log.i(TAG, String.format("Dns regexp 2: %s", dnsVerdictRegexp));
        dnsVerdictPattern = Pattern.compile(dnsVerdictRegexp, Pattern.CASE_INSENSITIVE);
    }

    @Override
    protected DnsVerdictWrapper parseLine(String line) {
        Matcher matcher2 = dnsVerdictPattern.matcher(line);
        if(matcher2.find()) {
            DnsVerdictWrapper wrapper = new DnsVerdictWrapper();
            setTimestampToWrapper(wrapper, matcher2);
            wrapper.setTransactionId(matcher2.group(3));
            wrapper.setVerdict(matcher2.group(4));
            return wrapper;
        }
        return null;
    }

    private DnsVerdictWrapper configure( Matcher matcher){
        DnsVerdictWrapper wrapper = new DnsVerdictWrapper();
        setTimestampToWrapper(wrapper, matcher);
        wrapper.setTransactionId(matcher.group(3));
        wrapper.setVerdict(matcher.group(4));
        return wrapper;
    }

    public static void main(String[] args) {
        String s2 = "01-15 08:44:10.740 D/Asimov::Native::OCEngine( 6509): 2014/01/15 8:44:10.745878 GMT 7449 [DEBUG]\t[dns_task.cpp:81] (0) - ocdnsd DTRX [045F0DBA]: verdict MISS";
//        String s2 = "12-14 20:18:54.075 D/Asimov::Native::OCEngine(13361): 2013/12/14 20:18:54.082152 GMT 13396 [DEBUG]\t[dns_task.cpp:66] (0) - ocdnsd DTRX [32EDDE60] (www.ukr.net): verdict HIT (2 total hits)";
        DnsVerdictTask task = new DnsVerdictTask(null, "MISS");
        DnsVerdictWrapper wrapper = task.parseLine(s2);
        System.out.println(wrapper);
        System.out.println(wrapper.getTimestamp());
    }
}
