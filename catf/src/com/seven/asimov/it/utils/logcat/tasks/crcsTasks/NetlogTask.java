package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import android.util.Log;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.logcat.LogcatChecks;
import com.seven.asimov.it.utils.logcat.wrappers.AppStatusType;
import com.seven.asimov.it.utils.logcat.wrappers.InterfaceType;
import com.seven.asimov.it.utils.logcat.wrappers.NetlogEntry;
import com.seven.asimov.it.utils.logcat.wrappers.OperationType;
import com.seven.asimov.it.utils.logcat.tasks.Task;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetlogTask extends Task<NetlogEntry> {

    private static final String TAG = NetlogTask.class.getSimpleName();

    private static final String NETLOG_V10_REGEXP = "(201[3-9]/[0-1][0-9]/[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9]+).([a-z]*).*" +
            "(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*)(,netlog,10,)" +
            "([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*),([\\-a\\:-z0-9\\.]*),([_a-z0-9\\.]*)," +
            "([a-z0-9\\.]*),([a-z0-9_\\.]*),([_\\-/a-z0-9]*),([_\\-/a-z0-9]*),([a-z0-9_\\.]*),([\\-0-9]*)," +
            "([\\-0-9]*),([0-9a-fA-F]*),([\\-0-9]*),([\\-0-9]*),([\\-0-9]*),([\\-a-z0-9_\\.]*),([/a-z0-9_\\.]*)," +
            "([\\-0-9]*),([\\-0-9]*),([\\-a-zA-Z0-9]*),([\\-\\[\\]\\/a-zA-Z0-9]*),([\\-0-9]*),([\\-0-9]*)";

    private static final String NETLOG_V11_REGEXP = "(201[3-9]/[0-1][0-9]/[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9].[0-9]+).([a-z]*).*" +
            "(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*)(,netlog,)11," +
            "([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*),([\\-a\\:-z0-9\\.]*),([_a-z0-9\\.]*)," +
            "([a-z0-9\\.]*),([a-z0-9_\\.]*),([_\\-/a-z0-9]*),([_\\-/a-z0-9]*),([a-z0-9_\\.]*),([\\-0-9]*)," +
            "([\\-0-9]*),([0-9a-fA-F]*),([\\-0-9]*),([\\-0-9]*),([\\-0-9]*),([\\-a-z0-9_\\.]*),([/a-z0-9_\\.]*)," +
            "([\\-0-9]*),([\\-0-9]*),([\\-a-zA-Z0-9]*),([\\-\\[\\]\\/a-zA-Z0-9]*),([\\-0-9]*),([\\-0-9]*)";

    private static final String NETLOG_V12_REGEXP = "(201[3-9]/[0-1][0-9]/[0-3][0-9].[0-2]*[0-9]:[0-5]*[0-9]:[0-5]*[0-9].[0-9]+).([a-z]*).*" +
            "(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*)(,netlog,)12," +
            "([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*),([\\-a\\:-z0-9\\.]*),([_a-z0-9\\.]*)," +
            "([a-z0-9\\.]*),([a-z0-9_\\.]*),([_\\-/a-z0-9]*),([_\\-/a-z0-9]*),([a-z0-9_\\.]*),([\\-0-9]*)," +
            "([\\-0-9]*),([0-9a-fA-F]*),([\\-0-9]*),([\\-0-9]*),([\\-0-9]*),([\\-a-z0-9_\\.]*),([/a-z0-9_\\.]*)," +
            "([\\-0-9]*),([\\-0-9]*),([\\-a-zA-Z0-9]*),([\\-\\[\\]\\/a-zA-Z0-9]*),([\\-0-9]*),([\\-0-9\\.]*)," +
            "([0-9]*),([\\-0-9\\.]*),([0-9]*),([0-9]*)";

    private static final String NETLOG_V13_REGEXP = "(201[3-9]/[0-1][0-9]/[0-3][0-9].[0-2]*[0-9]:[0-5]*[0-9]:[0-5]*[0-9].[0-9]+).([a-z]*).*" +
            "(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*)(,netlog,)13,([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*)," +
            "([0-9]*),([0-9a-z.:-]*),([_a-z0-9\\.]*),([a-z0-9\\.]*),([a-z0-9_\\.]*),([_\\-/a-z0-9]*),([_\\-/a-z0-9]*),([a-z0-9_\\.]*),([\\-0-9]*)," +
            "([\\-0-9]*),([0-9a-fA-F]*),([\\-0-9]*),([\\-0-9]*),([\\-0-9]*),([\\-0-9]*),([a-z\\/]*),([\\-0-9]*),([\\-0-9]*),([\\-a-zA-Z0-9]*)," +
            "([\\-\\[\\]\\/a-zA-Z0-9]*),([\\-0-9]*),([\\\\0-9a-z:.]*),([0-9]*),([\\.0-9a-z:.]*),([0-9]*),([0-9]*),([0-9]*),([a-z_]*)";

    private static final Pattern netlogV10Pattern = Pattern.compile(NETLOG_V10_REGEXP, Pattern.CASE_INSENSITIVE);
    private static final Pattern netlogV11Pattern = Pattern.compile(NETLOG_V11_REGEXP, Pattern.CASE_INSENSITIVE);
    private static final Pattern netlogV12Pattern = Pattern.compile(NETLOG_V12_REGEXP, Pattern.CASE_INSENSITIVE);
    private static final Pattern netlogV13Pattern = Pattern.compile(NETLOG_V13_REGEXP, Pattern.CASE_INSENSITIVE);

    private static final String IP_PORTS_INFO_REGEXP = "(.*loport\\:).([0-9]*)(.*netport\\:).([0-9]*)";
    private static final Pattern ipPortsInfoPattern = Pattern.compile(IP_PORTS_INFO_REGEXP, Pattern.CASE_INSENSITIVE);


    private int version;
    private Pattern pattern;

    public NetlogTask() {
        this.version = TFConstantsIF.NETLOG_VERSION;
        switch (version) {
            case 10: {
                pattern = netlogV10Pattern;
                break;
            }
            case 11: {
                pattern = netlogV11Pattern;
                break;
            }
            case 12: {
                pattern = netlogV12Pattern;
                break;
            }
            case 13: {
                pattern = netlogV13Pattern;
                break;
            }
            default: {
                throw new IllegalArgumentException("Please check netlog version!!!");
            }
        }
    }

    @Override
    public NetlogEntry parseLine(String line) {
        try {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                //Log.v(TAG, "line found: " + line);
                NetlogEntry entry = new NetlogEntry();
                entry.setVersion(version);
                entry.setTimestamp(LogcatChecks.getUnixTimeFromString(matcher.group(3)));
                entry.setClient_in(Integer.parseInt(matcher.group(5)));
                entry.setClient_out(Integer.parseInt(matcher.group(6)));
                entry.setServer_in(Integer.parseInt(matcher.group(7)));
                entry.setServer_out(Integer.parseInt(matcher.group(8)));
                entry.setCache_in(Integer.parseInt(matcher.group(9)));
                entry.setCache_out(Integer.parseInt(matcher.group(10)));

                entry.setHost(matcher.group(11));
                entry.setApplicationName(matcher.group(12));

                try {
                    entry.setAppStatus(AppStatusType.valueOf(matcher.group(13)));
                } catch (IllegalArgumentException e) {
                    entry.setAppStatus(AppStatusType.unknown);
                }

                try {
                    entry.setOpType(OperationType.valueOf(matcher.group(14)));
                } catch (IllegalArgumentException e) {
                    entry.setOpType(OperationType.unknown);
                }

                entry.setLocalProtocolStack(matcher.group(15));
                entry.setNetworkProtocolStack(matcher.group(16));

                try {
                    entry.setInterfaceType(InterfaceType.valueOf(matcher.group(17).toUpperCase()));
                } catch (IllegalArgumentException e) {
                    entry.setInterfaceType(InterfaceType.UNKNOWN);
                }
                int i = 0;
                if (version == 13) i = 2;
                entry.setResponseTime(Integer.parseInt(matcher.group(16 + i)) / 1000);
                entry.setRequestId(Long.parseLong(matcher.group(19 + i)));
                entry.setStatusCode(Integer.parseInt(matcher.group(21 + i)));
                entry.setErrorCode(matcher.group(22 + i));
                entry.setContentType(matcher.group(23 + i));
                entry.setHeaderLength(Integer.parseInt(matcher.group(24 + i)));
                entry.setContentLength(Integer.parseInt(matcher.group(25 + i)));
                entry.setResponseHash(matcher.group(26 + i));
                entry.setAnalysis(matcher.group(27 + i));
                entry.setOprimization(Integer.parseInt(matcher.group(28 + i)));
                entry.setDstPort(Integer.parseInt(matcher.group(30 + i)));


                matcher = ipPortsInfoPattern.matcher(line);
                if (matcher.find()) {
                    entry.setLoport(Integer.parseInt(matcher.group(2)));
                    entry.setNetport(Integer.parseInt(matcher.group(4)));
                }
                return entry;
            }
        } catch (NumberFormatException e) {
            Log.v(TAG, "NumberFormatException in line: " + line);
            throw new IllegalArgumentException(e);
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}