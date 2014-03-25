package com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.DispatcherEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatcherReconnectTask extends Task <DispatcherEvent> {

    private static final String TAG = DispatcherReconnectTask.class.getSimpleName();

    private static final String HTTP_DISPATCHER_RECONNECT_FAILED_REGEXP =
            "(201[0-9]\\/[0-9][0-9]\\/[0-9][0-9].[0-2][0-9]\\:[0-9][0-9]\\:[0-9][0-9]\\.[0-9]*)\\s" +
                    "([A-Z]*)(.*)(OC2 to HTTP Dispatcher: reconnect failed)";

    private static final String HTTP_DISPATCHER_CONNECTED_REGEXP =
            "(201[0-9]\\/[0-9][0-9]\\/[0-9][0-9].[0-2][0-9]\\:[0-9][0-9]\\:[0-9][0-9]\\.[0-9]*)\\s" +
                    "([A-Z]*)(.*)(OC2 to HTTP Dispatcher connected)";

    private static final String HTTPS_DISPATCHER_RECONNECT_FAILED_REGEXP =
            "(201[0-9]\\/[0-9][0-9]\\/[0-9][0-9].[0-2][0-9]\\:[0-9][0-9]\\:[0-9][0-9]\\.[0-9]*)\\s" +
                    "([A-Z]*)(.*)(OC2 to HTTPS Dispatcher: reconnect failed)";

    private static final String HTTPS_DISPATCHER_CONNECTED_REGEXP =
            "(201[0-9]\\/[0-9][0-9]\\/[0-9][0-9].[0-2][0-9]\\:[0-9][0-9]\\:[0-9][0-9]\\.[0-9]*)\\s" +
                    "([A-Z]*)(.*)(OC2 to HTTPS Dispatcher connected)";

    private static final String DNS_DISPATCHER_RECONNECT_FAILED_REGEXP =
            "(201[0-9]\\/[0-9][0-9]\\/[0-9][0-9].[0-2][0-9]\\:[0-9][0-9]\\:[0-9][0-9]\\.[0-9]*)\\s" +
                    "([A-Z]*)(.*)(OC2 to DNS Dispatcher: reconnect failed)";

    private static final String DNS_DISPATCHER_CONNECTED_REGEXP =
            "(201[0-9]\\/[0-9][0-9]\\/[0-9][0-9].[0-2][0-9]\\:[0-9][0-9]\\:[0-9][0-9]\\.[0-9]*)\\s" +
                    "([A-Z]*)(.*)(OC2 to DNS Dispatcher connected)";

    private static final String TCP_DISPATCHER_RECONNECT_FAILED_REGEXP =
            "(201[0-9]\\/[0-9][0-9]\\/[0-9][0-9].[0-2][0-9]\\:[0-9][0-9]\\:[0-9][0-9]\\.[0-9]*)\\s" +
                    "([A-Z]*)(.*)(OC2 to TCP Dispatcher: reconnect failed)";

    private static final String TCP_DISPATCHER_CONNECTED_REGEXP =
            "(201[0-9]\\/[0-9][0-9]\\/[0-9][0-9].[0-2][0-9]\\:[0-9][0-9]\\:[0-9][0-9]\\.[0-9]*)\\s" +
                    "([A-Z]*)(.*)(OC2 to TCP Dispatcher connected)";

    private static final Pattern httpDispatcherReconnectedFailedPattern = Pattern.compile(HTTP_DISPATCHER_RECONNECT_FAILED_REGEXP);
    private static final Pattern httpDispatcherConnectedPattern = Pattern.compile(HTTP_DISPATCHER_CONNECTED_REGEXP);
    private static final Pattern httpsDispatcherReconnectedFailedPattern = Pattern.compile(HTTPS_DISPATCHER_RECONNECT_FAILED_REGEXP);
    private static final Pattern httpsDispatcherConnectedPattern = Pattern.compile(HTTPS_DISPATCHER_CONNECTED_REGEXP);
    private static final Pattern dnsDispatcherReconnectedFailedPattern = Pattern.compile(DNS_DISPATCHER_RECONNECT_FAILED_REGEXP);
    private static final Pattern dnsDispatcherConnectedPattern = Pattern.compile(DNS_DISPATCHER_CONNECTED_REGEXP);
    private static final Pattern tcpDispatcherReconnectedFailedPattern = Pattern.compile(TCP_DISPATCHER_RECONNECT_FAILED_REGEXP);
    private static final Pattern tcpDispatcherConnectedPattern = Pattern.compile(TCP_DISPATCHER_CONNECTED_REGEXP);

    protected DispatcherEvent parseLine(String line) {
        Matcher matcher;
        matcher = httpDispatcherReconnectedFailedPattern.matcher(line);
        if (matcher.find()) {
            return wrapDispatcherEvent(matcher, DispatcherEvent.DISPATCHER_TYPE.HTTP, DispatcherEvent.DISPATHER_EVENT.RECONNECTFAILED);
        }
        matcher = httpDispatcherConnectedPattern.matcher(line);
        if (matcher.find()) {
            return wrapDispatcherEvent(matcher, DispatcherEvent.DISPATCHER_TYPE.HTTP, DispatcherEvent.DISPATHER_EVENT.CONNECTED);
        }
        matcher = httpsDispatcherReconnectedFailedPattern.matcher(line);
        if (matcher.find()) {
            return wrapDispatcherEvent(matcher, DispatcherEvent.DISPATCHER_TYPE.HTTPS, DispatcherEvent.DISPATHER_EVENT.RECONNECTFAILED);
        }
        matcher = httpsDispatcherConnectedPattern.matcher(line);
        if (matcher.find()) {
            return wrapDispatcherEvent(matcher, DispatcherEvent.DISPATCHER_TYPE.HTTPS, DispatcherEvent.DISPATHER_EVENT.CONNECTED);
        }
        matcher = dnsDispatcherReconnectedFailedPattern.matcher(line);
        if (matcher.find()) {
            return wrapDispatcherEvent(matcher, DispatcherEvent.DISPATCHER_TYPE.DNS, DispatcherEvent.DISPATHER_EVENT.RECONNECTFAILED);
        }
        matcher = dnsDispatcherConnectedPattern.matcher(line);
        if (matcher.find()) {
            return wrapDispatcherEvent(matcher, DispatcherEvent.DISPATCHER_TYPE.DNS, DispatcherEvent.DISPATHER_EVENT.CONNECTED);
        }
        matcher = tcpDispatcherReconnectedFailedPattern.matcher(line);
        if (matcher.find()) {
            return wrapDispatcherEvent(matcher, DispatcherEvent.DISPATCHER_TYPE.TCP, DispatcherEvent.DISPATHER_EVENT.RECONNECTFAILED);
        }
        matcher = tcpDispatcherConnectedPattern.matcher(line);
        if (matcher.find()) {
            return wrapDispatcherEvent(matcher, DispatcherEvent.DISPATCHER_TYPE.TCP, DispatcherEvent.DISPATHER_EVENT.CONNECTED);
        }
        return null;
    }

    private DispatcherEvent wrapDispatcherEvent(Matcher matcher, DispatcherEvent.DISPATCHER_TYPE type, DispatcherEvent.DISPATHER_EVENT event) {
        DispatcherEvent disp = new DispatcherEvent();
        disp.setTimezone("GTM");
        setTimestampToWrapper(disp, matcher);
        disp.setType(type);
        disp.setEvent(event);
        Log.e(TAG, disp.toString());
        return disp;
    }
}
