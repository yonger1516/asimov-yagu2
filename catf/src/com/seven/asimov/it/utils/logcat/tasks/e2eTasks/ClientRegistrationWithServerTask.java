package com.seven.asimov.it.utils.logcat.tasks.e2eTasks;

import android.util.Log;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.ClientRegistrationWithServerWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.TimeZones;

import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientRegistrationWithServerTask extends Task<ClientRegistrationWithServerWrapper> {
    private static final String TAG = ClientRegistrationWithServerWrapper.class.getSimpleName();

    private static final String UNAUTHENTICATED_CHALLENGE_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*<--- Received an unauthenticated challenge .*>0\\-0\\-.*hint.[0-9]*";
    private static final Pattern unauthenticatedChallenge = Pattern.compile(UNAUTHENTICATED_CHALLENGE_REGEXP, Pattern.CASE_INSENSITIVE);

    private static final String DIFFIE_HELLMAN_REQUEST_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Sending an unauthenticated register Diffie-Hellman request.*hint.[0-9]*";
    private static final Pattern diffieHellmanRequestPattern = Pattern.compile(DIFFIE_HELLMAN_REQUEST_REGEXP, Pattern.CASE_INSENSITIVE);

    private static final String DIFFIE_HELLMAN_RESPONSE_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Received an unauthenticated register Diffie-Hellman response .*>0\\-([0-9A-Za-z]*)\\-.*";
    private static final Pattern diffieHellmanResponsePattern = Pattern.compile(DIFFIE_HELLMAN_RESPONSE_REGEXP, Pattern.CASE_INSENSITIVE);

    private static ClientRegistrationWithServerWrapper wrapper = new ClientRegistrationWithServerWrapper();

    @Override
    protected ClientRegistrationWithServerWrapper parseLine(String line) {
        Matcher matcher1 = unauthenticatedChallenge.matcher(line);
        Matcher matcher2 = diffieHellmanRequestPattern.matcher(line);
        Matcher matcher3 = diffieHellmanResponsePattern.matcher(line);
        if (matcher1.find()) {
            wrapper.setTimestamp(setTimestampForCurrentMatcher(matcher1, 1));
            return wrapper;
        }
        if (matcher2.find()) {
            setTimestampForCurrentMatcher(matcher2, 2);
            return wrapper;
        }
        if (matcher3.find()) {
            setTimestampForCurrentMatcher(matcher3, 3);
            wrapper.setZ7tpAddress(matcher3.group(3));
            return wrapper;
        }
        return wrapper;
    }

    private long setTimestampForCurrentMatcher(Matcher matcher, int number) {
        TimeZone.setDefault(TimeZone.getTimeZone("GTM"));
        Log.d(TAG,"Set timezone to GMT");
        int hour = 0;
        try {
            hour = TimeZones.valueOf(matcher.group(2)).getId();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        long timestamp = DateUtil.format(matcher.group(1).replaceAll("/", "-")) + hour * 3600 * 1000;
        wrapper.getTimestamps()[number] = timestamp;
        return timestamp;
    }

    public static void main(String[] args) {
        ClientRegistrationWithServerTask task = new ClientRegistrationWithServerTask();
        String s="05-26 19:21:34.199: D/Asimov::Java::AbstractZ7TransportMultiplexer(527): 2013/05/26 19:21:34.202000 EEST 59 [DEBUG] [com.seven.transport.AbstractZ7TransportMultiplexer] <--- Received an unauthenticated register Diffie-Hellman response [0-1-0->0-59e2-0:1] of 290 bytes from endpoint 0-1-0. The message has a relay id hint 0";
        ClientRegistrationWithServerWrapper wrapper1 = task.parseLine(s);
        System.out.println(wrapper1);
        }
}



