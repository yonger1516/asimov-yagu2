package com.seven.asimov.it.utils.logcat;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.*;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyAddedTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyReceivedTask;
import com.seven.asimov.it.utils.logcat.wrappers.*;
import junit.framework.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.seven.asimov.it.asserts.CATFAssert.assertNotNull;

public final class LogcatChecks {

    private static final Map<Integer, String> trafficTypes = new HashMap<Integer, String>();
    private static final Logger logger = LoggerFactory.getLogger(LogcatChecks.class.getSimpleName());

    private LogcatChecks() {
    }

    public static PolicyWrapper checkPolicyAdded(PolicyAddedTask policyAddedTask,String name, String value, LogEntryWrapper lew) throws Exception {
        PolicyWrapper policyLogEntry;
        int entryNumber = lew != null ? lew.getEntryNumber() : 0;
        do {
            policyLogEntry = policyAddedTask.getEntryAfter(entryNumber);
            if (policyLogEntry == null) break;
            entryNumber = policyLogEntry.getEntryNumber();
        } while (!(name.equals(policyLogEntry.getName()) && value.equals(policyLogEntry.getValue())));

        assertNotNull("Log record for " + name + " policy adding not found!", policyLogEntry);
        return policyLogEntry;
    }

    public static RadioStateWrapper checkRadioState(RadioStateTask radioStateTask,boolean expectedState, LogEntryWrapper lew) throws Exception {
        RadioStateWrapper radioStateLogEntry;
        int entryNumber = lew != null ? lew.getEntryNumber() : 0;
        do {
            radioStateLogEntry = radioStateTask.getEntryAfter(entryNumber);
            if (radioStateLogEntry == null) break;
            entryNumber = radioStateLogEntry.getEntryNumber();
        } while (radioStateLogEntry.isRadioUp() != expectedState);
        assertNotNull("Log record for radio state up not found!", radioStateLogEntry);
        return radioStateLogEntry;
    }

    public static PolicyReceivedWrapper checkPolicyReceived(PolicyReceivedTask policyReceivedTask, String policyName, String policyValue, LogEntryWrapper lew) throws Exception {
        PolicyReceivedWrapper policyReceivedEntry;
        int entryNumber = lew != null ? lew.getEntryNumber() : 0;
        do {
            policyReceivedEntry = policyReceivedTask.getEntryAfter(entryNumber);
            if (policyReceivedEntry == null) break;
            entryNumber = policyReceivedEntry.getEntryNumber();
        } while (!policyName.equals(policyReceivedEntry.getName()) && !policyValue.equals(policyReceivedEntry.getValue()));
        assertNotNull("Log record for received policy state up not found!", policyReceivedEntry);
        return policyReceivedEntry;
    }

    public static DumpTransactionWrapper checkDumpTransaction(DumpTransactionTask dumpTransactionTask,String direction, LogEntryWrapper lew) throws Exception {
        int entryNumber = lew != null ? lew.getEntryNumber() : 0;
        DumpTransactionWrapper dumpTransactionLogEntry;
        do {
            dumpTransactionLogEntry = dumpTransactionTask.getEntryAfter(entryNumber);
            if (dumpTransactionLogEntry == null) break;
        } while (!dumpTransactionLogEntry.getDirection().equals(direction));
        assertNotNull("Log record for dump transaction not found!", dumpTransactionLogEntry);
        //assertTrue("Dump transaction " + direction + " error! \n" + dumpTransactionLogEntry, dumpTransactionLogEntry.getDirection().equals(direction));
        return dumpTransactionLogEntry;
    }

    public static ReportTransferWrapperNN checkReportTransfer(ReportTransferTaskNN reportTransferTaskNN, String type, String toStatus, LogEntryWrapper lew, boolean recordExist) throws Exception {
        ReportTransferWrapperNN reportTransferLogEntry;
        int entryNumber = lew != null ? lew.getEntryNumber() : 0;
        do {
            reportTransferLogEntry = reportTransferTaskNN.getEntryAfter(entryNumber);
            if (reportTransferLogEntry == null) break;
            entryNumber = reportTransferLogEntry.getEntryNumber();
        } while (!(reportTransferLogEntry.getType().equals(type) && (toStatus != null ? reportTransferLogEntry.getToStatus().equals(toStatus) : true)));
        if (recordExist) {
            assertNotNull("Log record for crcs report transfer not found!", reportTransferLogEntry);
        } else {
            Assert.assertNull("Log record for crcs report shouldn't exist!", reportTransferLogEntry);
        }
        return reportTransferLogEntry;
    }

    public static ReportTransferWrapperNN checkReportTransferByStatus(ReportTransferParametrizedTask reportTransferParametrizedTask, LogEntryWrapper lew, boolean recordExist) throws Exception {
        ReportTransferWrapperNN reportTransferLogEntry;
        int entryNumber = lew != null ? lew.getEntryNumber() : 0;
        do {
            reportTransferLogEntry = reportTransferParametrizedTask.getEntryAfter(entryNumber);
            if (reportTransferLogEntry == null) break;
            entryNumber = reportTransferLogEntry.getEntryNumber();
        }
        while (!(reportTransferLogEntry.getType().equals(reportTransferParametrizedTask.getType()/*type*/) && (reportTransferParametrizedTask.getStatus()/*status*/ != null ? reportTransferLogEntry.getStatus().equals(reportTransferParametrizedTask.getStatus()/*status*/) : true)));
        if (recordExist) {
            assertNotNull("Log record for crcs report transfer not found!", reportTransferLogEntry);
        } else {
            Assert.assertNull("Log record for crcs report shouldn't exist!", reportTransferLogEntry);
        }
        return reportTransferLogEntry;
    }

    public static ReportTransferWrapperNN checkReportTransfer(ReportTransferTaskNN reportTransferTaskNN, String type, String toStatus, Integer token, LogEntryWrapper lew, boolean recordExist) throws Exception {
        ReportTransferWrapperNN reportTransferLogEntry;
        if (token == null) {
            reportTransferLogEntry = checkReportTransfer(reportTransferTaskNN, type, toStatus, lew, recordExist);
        } else {
            //logger.trace("ReportTransferWrapperNN: else");
            do {
                reportTransferLogEntry = checkReportTransfer(reportTransferTaskNN, type, toStatus, lew, recordExist);
                if (reportTransferLogEntry == null) break;
                //logger.trace("ReportTransferWrapperNN: reportTransferLogEntry: " + reportTransferLogEntry);
            } while (token.intValue() != Integer.parseInt(reportTransferLogEntry.getToken()));
        }
        return reportTransferLogEntry;
    }

    public static ReportTransferWrapperNN checkReportTransfer(ReportTransferParametrizedTask reportTransferParametrizedTask, Integer token, LogEntryWrapper lew, boolean recordExist) throws Exception {
        ReportTransferWrapperNN reportTransferLogEntry;
        int entryNumber = lew != null ? lew.getEntryNumber() : 0;

        do {
            reportTransferLogEntry = reportTransferParametrizedTask.getEntryAfter(entryNumber);
            if (reportTransferLogEntry == null) break;
            entryNumber = reportTransferLogEntry.getEntryNumber();
        } while (!reportTransferLogEntry.getToken().equals(token.toString()));

        if (recordExist) {
            assertNotNull("Log record for crcs report transfer not found!", reportTransferLogEntry);
        } else {
            if (reportTransferLogEntry != null) {
                logger.warn(reportTransferLogEntry.toString());
            } else logger.warn("reportTransferLogEntry is null");

            Assert.assertNull("Log record for crcs report shouldn't exist!", reportTransferLogEntry);
        }
        return reportTransferLogEntry;
    }

    public static RadioLogEntry checkRadioLog(RadiologTask radiologTask, RadioStateType expectedType, LogEntryWrapper lew) throws Exception {
        RadioLogEntry radioLogEntry;
        int entryNumber = lew != null ? lew.getEntryNumber() : 0;
        do {
            radioLogEntry = radiologTask.getEntryAfter(entryNumber);
            if (radioLogEntry == null) break;
            entryNumber = radioLogEntry.getEntryNumber();
        } while (radioLogEntry.getCurrentState() != expectedType);
        assertNotNull("Log record for radio state type not found!", radioLogEntry);
        return radioLogEntry;
    }

    public static LogEntryWrapper checkLogEntryExist(Task task, LogEntryWrapper lew) throws Exception {
        LogEntryWrapper logEntry;
        int entryNumber = lew != null ? lew.getEntryNumber() : 0;
        logEntry = task.getEntryAfter(entryNumber);
        assertNotNull("Log entry in task " + task.getClass().getSimpleName() + " not found!", logEntry);
        return logEntry;
    }

    public static LogEntryWrapper checkLogEntryNotExist(Task task, LogEntryWrapper lew) throws Exception {
        LogEntryWrapper logEntry;
        int entryNumber = lew != null ? lew.getEntryNumber() : 0;
        logEntry = task.getEntryAfter(entryNumber);
        Assert.assertNull("Log entry in task " + task.getClass().getSimpleName() + " shouldn't exist!", logEntry);
        return logEntry;
    }

    public static PowerLogEntry getPowerEntryFromStr(String str) {
        String pattern1 = "(PowerLog\\:).(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*)," +
                "(power,.),([_a-zA-Z]*),([0-9]*),([\\-0-9]*),([\\-0-9]*),([\\-0-9]*)";
        String pattern2 = "(PowerLog\\:).(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*)," +
                "(power,.),([_a-zA-Z]*),([0-9]*),([\\-0-9]*),([\\-0-9]*)";

        Pattern p1 = Pattern.compile(pattern1, Pattern.CASE_INSENSITIVE);
        Matcher m1 = p1.matcher(str);
        Pattern p2 = Pattern.compile(pattern2, Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(str);
        if (m1.find()) {
            PowerLogEntry result = new PowerLogEntry();
            result.setTimestamp(getUnixTimeFromString(m1.group(2)));
            try {
                result.setState(BatteryStateType.valueOf(m1.group(4)));
            } catch (IllegalArgumentException e) {
                result.setState(BatteryStateType.unknown);
            }
            result.setBattery_level(Integer.parseInt(m1.group(5)));
            result.setBattery_delta(Integer.parseInt(m1.group(6)));
            result.setTime_in_previous_state(Integer.parseInt(m1.group(7)));
            result.setOptimization(Integer.parseInt(m1.group(8)));
            return result;
        } else if (m2.find()) {
            PowerLogEntry result = new PowerLogEntry();
            result.setTimestamp(getUnixTimeFromString(m2.group(2)));
            try {
                result.setState(BatteryStateType.valueOf(m2.group(4)));
            } catch (IllegalArgumentException e) {
                result.setState(BatteryStateType.unknown);
            }
            result.setBattery_level(Integer.parseInt(m2.group(5)));
            result.setBattery_delta(Integer.parseInt(m2.group(6)));
            result.setTime_in_previous_state(Integer.parseInt(m2.group(7)));
            result.setOptimization(-1);
            return result;
        } else {
            return null;
        }
    }

    public static TrafficLogEntry getTrafficEntryFromStr(String str) {
        String pattern1 = "(TrafficLog\\:).(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*)," +
                "(traffic,.),([_a-zA-Z4]*),([0-9]*),([0-9]*),([\\-0-9]*)";
        String pattern2 = "(TrafficLog\\:).(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*)," +
                "(traffic,.),([_a-zA-Z4]*),([0-9]*),([0-9]*)";
        Pattern p1 = Pattern.compile(pattern1, Pattern.CASE_INSENSITIVE);
        Pattern p2 = Pattern.compile(pattern2, Pattern.CASE_INSENSITIVE);
        Matcher m1 = p1.matcher(str);
        Matcher m2 = p2.matcher(str);
        if (m1.find()) {
            TrafficLogEntry result = new TrafficLogEntry();
            result.setTimestamp(getUnixTimeFromString(m1.group(2)));
            result.setTypeTraffic(m1.group(4));
            result.setTrafficTypeId(getTrafficTypeId(result.getTypeTraffic()));
            result.setRx(Integer.parseInt(m1.group(5)));
            result.setRx(Integer.parseInt(m1.group(6)));
            result.setOptimization(Integer.parseInt(m1.group(7)));
            return result;
        } else if (m2.find()) {
            TrafficLogEntry result = new TrafficLogEntry();
            result.setTimestamp(getUnixTimeFromString(m2.group(2)));
            result.setTypeTraffic(m2.group(4));
            result.setTrafficTypeId(getTrafficTypeId(result.getTypeTraffic()));
            result.setRx(Integer.parseInt(m2.group(5)));
            result.setRx(Integer.parseInt(m2.group(6)));
            result.setOptimization(-1);
            return result;
        } else {
            return null;
        }
    }

    public static RadioLogEntry getRadioEntryFromStr(String str) {
        String pattern1 = "(RadioLog\\:).(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*)," +
                "(radio,.),([_a-zA-Z]*),([_a-zA-Z]*),([0-9]*),([\\-0-9]*)";
        String pattern2 = "(RadioLog\\:).(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*)," +
                "(radio,.),([_a-zA-Z]*),([_a-zA-Z]*),([0-9]*)";

        Pattern p1 = Pattern.compile(pattern1, Pattern.CASE_INSENSITIVE);
        Matcher m1 = p1.matcher(str);
        Pattern p2 = Pattern.compile(pattern2, Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(str);
        if (m1.find()) {
            RadioLogEntry result = new RadioLogEntry();
            result.setTimestamp(getUnixTimeFromString(m1.group(2)));
            try {
                result.setCurrentState(RadioStateType.valueOf(m1.group(4)));
            } catch (IllegalArgumentException e) {
                result.setCurrentState(RadioStateType.unknown);
            }
            try {
                result.setPreviousState(RadioStateType.valueOf(m1.group(5)));
            } catch (IllegalArgumentException e) {
                result.setPreviousState(RadioStateType.unknown);
            }
            result.setTimeInPreviousState(Integer.parseInt(m1.group(6)));
            result.setOptimization(Integer.parseInt(m1.group(7)));
            return result;
        } else if (m2.find()) {
            RadioLogEntry result = new RadioLogEntry();
            result.setTimestamp(getUnixTimeFromString(m2.group(2)));
            try {
                result.setCurrentState(RadioStateType.valueOf(m2.group(4)));
            } catch (IllegalArgumentException e) {
                result.setCurrentState(RadioStateType.unknown);
            }
            try {
                result.setPreviousState(RadioStateType.valueOf(m2.group(5)));
            } catch (IllegalArgumentException e) {
                result.setPreviousState(RadioStateType.unknown);
            }
            result.setTimeInPreviousState(Integer.parseInt(m2.group(6)));
            result.setOptimization(-1);
            return result;
        } else {
            return null;
        }
    }

    public static NetlogEntry getNetlogEntryFromStr(String str) {
        Pattern netlog3 = Pattern.compile(",netlog,3,");
        Pattern netlog6 = Pattern.compile(",netlog,6,");
        Pattern netlog7 = Pattern.compile(",netlog,7,");
        Pattern netlog8 = Pattern.compile(",netlog,8,");
        Pattern netlog9 = Pattern.compile(",netlog,9,");
        Pattern netlog10 = Pattern.compile(",netlog,10,");
        Matcher m3 = netlog3.matcher(str);
        if (m3.find())
            return getNetlog3EntryFromStr(str);
        Matcher m6 = netlog6.matcher(str);
        if (m6.find())
            return getNetlog6EntryFromStr(str);
        Matcher m7 = netlog7.matcher(str);
        if (m7.find())
            return getNetlog7EntryFromStr(str);
        Matcher m8 = netlog8.matcher(str);
        if (m8.find())
            return getNetlog8EntryFromStr(str);
        Matcher m9 = netlog9.matcher(str);
        if (m9.find())
            return getNetlog9EntryFromStr(str);
        Matcher m10 = netlog10.matcher(str);
        if (m10.find())
            return getNetlog10EntryFromStr(str);

        return null;
    }


    private static NetlogEntry getNetlog3EntryFromStr(String str) {

        String pattern = "(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*)(,netlog,3,)" +
                "([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*)," +
                "([\\-a-z0-9_\\.]*)[:\\d]*,([_a-z0-9\\.]*),([a-z0-9\\.]*),([a-z0-9_\\.]*),([a-z0-9]*),([a-z0-9_\\.]*)," +
                "([\\-0-9]*),([\\-0-9]*)" +
                "";

        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        if (m.find()) {
            NetlogEntry result = new NetlogEntry();
            result.setTimestamp(getUnixTimeFromString(m.group(1)));
            result.setClient_in(Integer.parseInt(m.group(3)));
            result.setClient_out(Integer.parseInt(m.group(4)));
            result.setServer_in(Integer.parseInt(m.group(5)));
            result.setServer_out(Integer.parseInt(m.group(6)));
            result.setCache_in(Integer.parseInt(m.group(7)));
            result.setCache_out(Integer.parseInt(m.group(8)));

            result.setHost(m.group(9));

            result.setApplicationName(m.group(10));

            try {
                result.setAppStatus(AppStatusType.valueOf(m.group(11)));
            } catch (IllegalArgumentException e) {
                result.setAppStatus(AppStatusType.unknown);
            }
            try {
                result.setOpType(OperationType.valueOf(m.group(12)));
            } catch (IllegalArgumentException e) {
                result.setOpType(OperationType.unknown);
            }

            try {
                result.setProtocolType(ProtocolType.valueOf(m.group(13).toUpperCase()));
            } catch (IllegalArgumentException e) {
                if (m.group(13).equals("7tp"))
                    result.setProtocolType(ProtocolType.Z7TP);
                else
                    result.setProtocolType(ProtocolType.UNKNOWN);
            }

            try {
                result.setInterfaceType(InterfaceType.valueOf(m.group(14).toUpperCase()));
            } catch (IllegalArgumentException e) {
                result.setInterfaceType(InterfaceType.UNKNOWN);
            }

            result.setResponseTime(Integer.parseInt(m.group(15)));
            result.setRequestId(Integer.parseInt(m.group(16)));
            return result;
        } else {
            return null;
        }
    }


    private static NetlogEntry getNetlog6EntryFromStr(String str) {

        NetlogEntry result = new NetlogEntry();
        String pattern = "(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*)(,netlog,6,)" +
                "([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*)," +
                "([\\-a-z0-9\\.]*)[:\\d]*,([_a-z0-9\\.]*),([a-z0-9\\.]*),([a-z0-9_\\.]*),([a-z0-9]*),([a-z0-9_\\.]*)," +
                "([\\-0-9]*),([\\-0-9]*),([\\-0-9]*),([a-z0-9_\\.]*),([\\-0-9]*),([\\-a-z0-9_\\.]*),([0-9]*)";
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        if (m.find()) {
            result.setTimestamp(getUnixTimeFromString(m.group(1)));
            result.setClient_in(Integer.parseInt(m.group(3)));
            result.setClient_out(Integer.parseInt(m.group(4)));
            result.setServer_in(Integer.parseInt(m.group(5)));
            result.setServer_out(Integer.parseInt(m.group(6)));
            result.setCache_in(Integer.parseInt(m.group(7)));
            result.setCache_out(Integer.parseInt(m.group(8)));

            result.setHost(m.group(9));

            result.setApplicationName(m.group(10));


            try {
                result.setAppStatus(AppStatusType.valueOf(m.group(11)));
            } catch (IllegalArgumentException e) {
                result.setAppStatus(AppStatusType.unknown);
            }
            try {
                result.setOpType(OperationType.valueOf(m.group(12)));
            } catch (IllegalArgumentException e) {
                result.setOpType(OperationType.unknown);
            }

            try {
                result.setProtocolType(ProtocolType.valueOf(m.group(13).toUpperCase()));
            } catch (IllegalArgumentException e) {
                if (m.group(13).equals("7tp"))
                    result.setProtocolType(ProtocolType.Z7TP);
                else
                    result.setProtocolType(ProtocolType.UNKNOWN);
            }

            try {
                result.setInterfaceType(InterfaceType.valueOf(m.group(14).toUpperCase()));
            } catch (IllegalArgumentException e) {
                result.setInterfaceType(InterfaceType.UNKNOWN);
            }
            result.setResponseTime(Integer.parseInt(m.group(15)));
            result.setRequestId(Integer.parseInt(m.group(16)));

            result.setStatusCode(Integer.parseInt(m.group(17)));
            result.setContentType(m.group(18));
            result.setHeaderLength(Integer.parseInt(m.group(19)));
            result.setContentLength(Integer.parseInt(m.group(20)));
            try {
                result.setOprimization(Integer.parseInt(m.group(21)));
            } catch (NumberFormatException e) {
                String subStr = str.substring(str.lastIndexOf(",") + 1);
                try {
                    result.setOprimization(Integer.parseInt(subStr));
                } catch (NumberFormatException e2) {
                }
            }
        } else {
            return null;
        }
        String pattern2 = "(loport\\:).([0-9]*)(.*netport\\:).([0-9]*)";
        Pattern p2 = Pattern.compile(pattern2);
        Matcher m2 = p2.matcher(str);
        if (m2.find()) {
            result.setLoport(Integer.parseInt(m2.group(2)));
            result.setNetport(Integer.parseInt(m2.group(4)));
        }
        return result;
    }

    private static NetlogEntry getNetlog7EntryFromStr(String str) {

        NetlogEntry result = new NetlogEntry();

        String pattern = "(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*)(,netlog,7,)" +
                "([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*)," +
                "([\\-a\\:-z0-9\\.]*),([_a-z0-9\\.]*),([a-z0-9\\.]*),([a-z0-9_\\.]*),([a-z0-9]*),([a-z0-9_\\.]*)," +
                "([\\-0-9]*),([\\-0-9]*),([\\-0-9]*),([\\-a-z0-9_\\.]*),([a-z0-9_\\.]*),([\\-0-9]*),([\\-0-9]*)" +
                ",([\\-a-zA-Z0-9]*),([\\-\\[\\]\\/a-zA-Z0-9]*),([\\-0-9]*),([\\-0-9]*)";


        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        if (m.find()) {
            result.setTimestamp(getUnixTimeFromString(m.group(1)));
            result.setClient_in(Integer.parseInt(m.group(3)));
            result.setClient_out(Integer.parseInt(m.group(4)));
            result.setServer_in(Integer.parseInt(m.group(5)));
            result.setServer_out(Integer.parseInt(m.group(6)));
            result.setCache_in(Integer.parseInt(m.group(7)));
            result.setCache_out(Integer.parseInt(m.group(8)));

            result.setHost(m.group(9));

            result.setApplicationName(m.group(10));

            try {
                result.setAppStatus(AppStatusType.valueOf(m.group(11)));
            } catch (IllegalArgumentException e) {
                result.setAppStatus(AppStatusType.unknown);
            }

            try {
                result.setOpType(OperationType.valueOf(m.group(12)));
            } catch (IllegalArgumentException e) {
                result.setOpType(OperationType.unknown);
            }

            try {
                result.setProtocolType(ProtocolType.valueOf(m.group(13).toUpperCase()));
            } catch (IllegalArgumentException e) {
                if (m.group(13).equals("7tp"))
                    result.setProtocolType(ProtocolType.Z7TP);
                else
                    result.setProtocolType(ProtocolType.UNKNOWN);
            }

            try {
                result.setInterfaceType(InterfaceType.valueOf(m.group(14).toUpperCase()));
            } catch (IllegalArgumentException e) {
                result.setInterfaceType(InterfaceType.UNKNOWN);
            }
            result.setResponseTime(Integer.parseInt(m.group(15)));
            result.setRequestId(Long.parseLong(m.group(16)));

            result.setStatusCode(Integer.parseInt(m.group(17)));
            result.setErrorCode(m.group(18));
            result.setContentType(m.group(19));
            result.setHeaderLength(Integer.parseInt(m.group(20)));
            result.setContentLength(Integer.parseInt(m.group(21)));
            result.setResponseHash(m.group(22));
            result.setAnalysis(m.group(23));
            result.setOprimization(Integer.parseInt(m.group(24)));
            result.setDstPort(Integer.parseInt(m.group(25)));

        } else {
            return null;
        }
        String pattern2 = "(loport\\:).([0-9]*)(.*netport\\:).([0-9]*)";
        Pattern p2 = Pattern.compile(pattern2);
        Matcher m2 = p2.matcher(str);
        if (m2.find()) {
            result.setLoport(Integer.parseInt(m2.group(2)));
            result.setNetport(Integer.parseInt(m2.group(4)));
        }
        return result;
    }

    private static NetlogEntry getNetlog8EntryFromStr(String str) {

        NetlogEntry result = new NetlogEntry();

        String pattern = "(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*)(,netlog,8,)" +
                "([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*)," +
                "([\\-a\\:-z0-9\\.]*),([_a-z0-9\\.]*),([a-z0-9\\.]*),([a-z0-9_\\.]*),([a-z0-9]*),([a-z0-9_\\.]*)," +
                "([\\-0-9]*),([\\-0-9]*),([\\-0-9]*),([\\-a-z0-9_\\.]*),([a-z0-9_\\.]*),([\\-0-9]*),([\\-0-9]*)" +
                ",([\\-a-zA-Z0-9]*),([\\-\\[\\]\\/a-zA-Z0-9]*),([\\-0-9]*),([\\-0-9]*)";

        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        if (m.find()) {

            result.setTimestamp(getUnixTimeFromString(m.group(1)));

            result.setClient_in(Integer.parseInt(m.group(3)));
            result.setClient_out(Integer.parseInt(m.group(4)));
            result.setServer_in(Integer.parseInt(m.group(5)));
            result.setServer_out(Integer.parseInt(m.group(6)));
            result.setCache_in(Integer.parseInt(m.group(7)));
            result.setCache_out(Integer.parseInt(m.group(8)));

            result.setHost(m.group(9));

            result.setApplicationName(m.group(10));

            try {
                result.setAppStatus(AppStatusType.valueOf(m.group(11)));
            } catch (IllegalArgumentException e) {
                result.setAppStatus(AppStatusType.unknown);
            }

            try {
                result.setOpType(OperationType.valueOf(m.group(12)));
            } catch (IllegalArgumentException e) {
                result.setOpType(OperationType.unknown);
            }

            try {
                result.setProtocolType(ProtocolType.valueOf(m.group(13).toUpperCase()));
            } catch (IllegalArgumentException e) {
                if (m.group(13).equals("7tp"))
                    result.setProtocolType(ProtocolType.Z7TP);
                else
                    result.setProtocolType(ProtocolType.UNKNOWN);
            }

            try {
                result.setInterfaceType(InterfaceType.valueOf(m.group(14).toUpperCase()));
            } catch (IllegalArgumentException e) {
                result.setInterfaceType(InterfaceType.UNKNOWN);
            }
            result.setResponseTime(Integer.parseInt(m.group(15)));
            result.setRequestId(Long.parseLong(m.group(16)));

            result.setStatusCode(Integer.parseInt(m.group(17)));
            result.setErrorCode(m.group(18));
            result.setContentType(m.group(19));
            result.setHeaderLength(Integer.parseInt(m.group(20)));
            result.setContentLength(Integer.parseInt(m.group(21)));
            result.setResponseHash(m.group(22));
            result.setAnalysis(m.group(23));
            result.setOprimization(Integer.parseInt(m.group(24)));
            result.setDstPort(Integer.parseInt(m.group(25)));

        } else {
            return null;
        }
        String pattern2 = "(IP\\: )([a-f0-9\\.\\:]*)(.*loport\\:).([0-9]*)(.*netport\\:).([0-9]*)";
        Pattern p2 = Pattern.compile(pattern2);
        Matcher m2 = p2.matcher(str);
        if (m2.find()) {
            result.setOriginalIp(m2.group(2));
            result.setLoport(Integer.parseInt(m2.group(4)));
            result.setNetport(Integer.parseInt(m2.group(6)));
        }
        return result;
    }

    private static NetlogEntry getNetlog9EntryFromStr(String str) {

        NetlogEntry result = new NetlogEntry();

        String pattern = "(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*)(,netlog,9,)" +
                "([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*)," +
                "([\\-a\\:-z0-9\\.]*),([_a-z0-9\\.]*),([a-z0-9\\.]*),([a-z0-9_\\.]*),([_\\-/a-z0-9]*),([a-z0-9_\\.]*)," +
                "([\\-0-9]*),([\\-0-9]*),([\\-0-9]*),([\\-a-z0-9_\\.]*),([a-z0-9_\\.]*),([\\-0-9]*),([\\-0-9]*)" +
                ",([\\-a-zA-Z0-9]*),([\\-\\[\\]\\/a-zA-Z0-9]*),([\\-0-9]*),([\\-0-9]*),([\\-0-9]*),([_\\-/a-z0-9]*)";

        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        if (m.find()) {

            result.setTimestamp(getUnixTimeFromString(m.group(1)));

            result.setClient_in(Integer.parseInt(m.group(3)));
            result.setClient_out(Integer.parseInt(m.group(4)));
            result.setServer_in(Integer.parseInt(m.group(5)));
            result.setServer_out(Integer.parseInt(m.group(6)));
            result.setCache_in(Integer.parseInt(m.group(7)));
            result.setCache_out(Integer.parseInt(m.group(8)));

            result.setHost(m.group(9));

            result.setApplicationName(m.group(10));

            try {
                result.setAppStatus(AppStatusType.valueOf(m.group(11)));
            } catch (IllegalArgumentException e) {
                result.setAppStatus(AppStatusType.unknown);
            }

            try {
                result.setOpType(OperationType.valueOf(m.group(12)));
            } catch (IllegalArgumentException e) {
                result.setOpType(OperationType.unknown);
            }

            try {
                result.setProtocolType(ProtocolType.valueOf(m.group(13).toUpperCase()));
            } catch (IllegalArgumentException e) {
                if (m.group(13).equals("7tp"))
                    result.setProtocolType(ProtocolType.Z7TP);
                else
                    result.setProtocolType(ProtocolType.UNKNOWN);
            }

            try {
                result.setInterfaceType(InterfaceType.valueOf(m.group(14).toUpperCase()));
            } catch (IllegalArgumentException e) {
                result.setInterfaceType(InterfaceType.UNKNOWN);
            }
            result.setResponseTime(Integer.parseInt(m.group(15)));
            result.setRequestId(Long.parseLong(m.group(16)));

            result.setStatusCode(Integer.parseInt(m.group(17)));
            result.setErrorCode(m.group(18));
            result.setContentType(m.group(19));
            result.setHeaderLength(Integer.parseInt(m.group(20)));
            result.setContentLength(Integer.parseInt(m.group(21)));
            result.setResponseHash(m.group(22));
            result.setAnalysis(m.group(23));
            result.setOprimization(Integer.parseInt(m.group(24)));
            result.setDstPort(Integer.parseInt(m.group(25)));

        } else {
            return null;
        }
        String pattern2 = "(IP\\: )([a-f0-9\\.\\:]*)(.*loport\\:).([0-9]*)(.*netport\\:).([0-9]*)";
        Pattern p2 = Pattern.compile(pattern2);
        Matcher m2 = p2.matcher(str);
        if (m2.find()) {
            result.setOriginalIp(m2.group(2));
            result.setLoport(Integer.parseInt(m2.group(4)));
            result.setNetport(Integer.parseInt(m2.group(6)));
        }
        return result;
    }

    private static NetlogEntry getNetlog10EntryFromStr(String str) {

        NetlogEntry result = new NetlogEntry();

        String pattern = "(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*)(,netlog,10,)" +
                "([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*),([0-9]*),([\\-a\\:-z0-9\\.]*),([_a-z0-9\\.]*)," +
                "([a-z0-9\\.]*),([a-z0-9_\\.]*),([_\\-/a-z0-9]*),([_\\-/a-z0-9]*),([a-z0-9_\\.]*),([\\-0-9]*)," +
                "([\\-0-9]*),([0-9a-fA-F]*),([\\-0-9]*),([\\-0-9]*),([\\-0-9]*),([\\-a-z0-9_\\.]*),([/a-z0-9_\\.]*)," +
                "([\\-0-9]*),([\\-0-9]*),([\\-a-zA-Z0-9]*),([\\-\\[\\]\\/a-zA-Z0-9]*),([\\-0-9]*),([\\-0-9]*)";

        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        if (m.find()) {

            result.setTimestamp(getUnixTimeFromString(m.group(1)));

            result.setClient_in(Integer.parseInt(m.group(3)));
            result.setClient_out(Integer.parseInt(m.group(4)));
            result.setServer_in(Integer.parseInt(m.group(5)));
            result.setServer_out(Integer.parseInt(m.group(6)));
            result.setCache_in(Integer.parseInt(m.group(7)));
            result.setCache_out(Integer.parseInt(m.group(8)));

            result.setHost(m.group(9));

            result.setApplicationName(m.group(10));

            try {
                result.setAppStatus(AppStatusType.valueOf(m.group(11)));
            } catch (IllegalArgumentException e) {
                result.setAppStatus(AppStatusType.unknown);
            }

            try {
                result.setOpType(OperationType.valueOf(m.group(12)));
            } catch (IllegalArgumentException e) {
                result.setOpType(OperationType.unknown);
            }

            result.setLocalProtocolStack(m.group(13));
            result.setNetworkProtocolStack(m.group(14));
//            try {
//                result.setProtocolType(ProtocolType.valueOf(m.group(13).toUpperCase()));
//            } catch (IllegalArgumentException e) {
//                if (m.group(13).equals("7tp"))
//                    result.setProtocolType(ProtocolType.Z7TP);
//                else
//                    result.setProtocolType(ProtocolType.UNKNOWN);
//            }

            try {
                result.setInterfaceType(InterfaceType.valueOf(m.group(15).toUpperCase()));
            } catch (IllegalArgumentException e) {
                result.setInterfaceType(InterfaceType.UNKNOWN);
            }
            result.setResponseTime(Integer.parseInt(m.group(16)));
            result.setRequestId(Long.parseLong(m.group(19)));

            result.setStatusCode(Integer.parseInt(m.group(21)));
            result.setErrorCode(m.group(22));
            result.setContentType(m.group(23));
            result.setHeaderLength(Integer.parseInt(m.group(24)));
            result.setContentLength(Integer.parseInt(m.group(25)));
            result.setResponseHash(m.group(26));
            result.setAnalysis(m.group(27));
            result.setOprimization(Integer.parseInt(m.group(28)));
            result.setDstPort(Integer.parseInt(m.group(29)));

        } else {
            return null;
        }
        String pattern2 = "(IP\\: )([a-f0-9\\.\\:]*)(.*loport\\:).([0-9]*)(.*netport\\:).([0-9]*)";
        Pattern p2 = Pattern.compile(pattern2);
        Matcher m2 = p2.matcher(str);
        if (m2.find()) {
            result.setOriginalIp(m2.group(2));
            result.setLoport(Integer.parseInt(m2.group(4)));
            result.setNetport(Integer.parseInt(m2.group(6)));
        }
        return result;
    }

    public static Timestamp getGmtTimestamp(long timestamp) {
        return new Timestamp(timestamp - getGmtOffset());
    }

    private static long getGmtOffset() {
        TimeZone timeZone = TimeZone.getDefault();
        return timeZone.getOffset(0);
    }

    public static String getFileSeparator() {
        return System.getProperty("file.separator");
    }

    public static long getUnixTimeFromString(String date) {
        String format;
        if (date.contains("-")) {
            format = "yyyy-MM-dd HH:mm:ss.SSS";
        } else {
            format = "yyyy/MM/dd HH:mm:ss.SSS";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date parsed;
        try {
            parsed = sdf.parse(date);
        } catch (ParseException e) {
            return 0;
        }
        return parsed.getTime() - parsed.getTimezoneOffset() * 1000 * 60;
    }

    public static long getUnixTimeFromString(String date, String timeZoneName) {
        String format;
        if (date.contains("-")) {
            format = "yyyy-MM-dd HH:mm:ss.SSS";
        } else {
            format = "yyyy/MM/dd HH:mm:ss.SSS";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date parsed;
        try {
            parsed = sdf.parse(date);
        } catch (ParseException e) {
            return 0;
        }
        return parsed.getTime();
    }

    private static long getTimeZoneOffset(String timeZone) {
        TimeZone localTimeZone = TimeZone.getTimeZone(timeZone);
        return localTimeZone.getOffset(0);
    }

    private static int getTrafficTypeId(String trafficType) {
        int result = 4;
        for (int key : trafficTypes.keySet()) {
            if (trafficTypes.get(key).equals(trafficType)) {
                return key;
            }
        }
        return result;
    }
}
