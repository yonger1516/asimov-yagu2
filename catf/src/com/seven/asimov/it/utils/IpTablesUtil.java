package com.seven.asimov.it.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.out;

public final class IpTablesUtil {

    private static final Logger logger = LoggerFactory.getLogger(IpTablesUtil.class.getSimpleName());
    /**
     *
     * @param inputRule - directly net rule
     * @param table  - table in IpTables
     * @param path - IpTables location
     * @throws java.io.IOException
     * @throws InterruptedException
     */
    public static void checkAddRule(String inputRule, String table, String path) throws IOException, InterruptedException {

        String loChain = returnLoParameters(1, inputRule);
        String loRule = returnLoParameters(3, inputRule);

        List command = new ArrayList<String>();
        command.add(path +  " -t " + table + " -L " + loChain);
        List<String> result;
        result = ShellUtil.execWithCompleteResultWithListOutput(command, true);
        logger.info("Shell execution");
        for (String s : result) {
            logger.info(s);
        }

        if(check(result, loRule)) {
            String[] checkedRule = {"su", "-c", path  + " -t " + table + " " + inputRule};
            execute(checkedRule);
        }  else {
            logger.error("The rule is already exists");
        }
    }

    private static boolean check(List<String> result, String rule) {
        rule.replace("-", " ").replace("  ", " ").replace(" ", ".*");

        for(String record : result) {
            if(returnRule(record) != null && returnRule(record).matches(rule)) {
                return false;
            }
        }
        return true;
    }

    public static void checkAddRuleToIPv4Table(String inputRule, String table) throws IOException, InterruptedException {
        checkAddRule(inputRule, table, TFConstantsIF.IPTABLES_PATH);
    }

    public static void checkAddRuleToIPv6Table(String inputRule, String table) throws IOException, InterruptedException {
        if (table.equals("nat")) {
            logger.error("The table 'nat' are not alloved for ip6tables, please, use TPROXY");
        } else {
            checkAddRule(inputRule, table, TFConstantsIF.IP6TABLES_PATH);
        }
    }

    private static String returnRule(String line) {
        String regexspGetRule = "([A-Z]*)\\s*[a-z]*\\s*--\\s*[a-z]*\\s*[a-z]*\\s*([a-zA-Z]*.*)";

        Pattern patternGetRule = Pattern.compile(regexspGetRule);

        Matcher matcherGetRule = patternGetRule.matcher(line);
        if(matcherGetRule.find()) {
            out.println(matcherGetRule.group(1));
            out.println(matcherGetRule.group(2));
            return matcherGetRule.group(2);
        }
        return null;
    }

    /**
     * Function to divide users' input rule
     * @param param - which parameter to get (1 - chain, 2 - target, 3 - direct rule)
     * @param line
     * @return
     */
    private static String returnLoParameters(int param, String line) {

        String regexsp = ".[A-Z]\\s([A-Z]*)(.*)\\s-([a-z])\\s([A-Z]*)(.{0,})";
        Pattern pattern = Pattern.compile(regexsp);
        Matcher matcher = pattern.matcher(line);
        if(matcher.find()) {
            if(param == 1) return matcher.group(1);
            if(param == 2) return matcher.group(4);
            else return matcher.group(2) + matcher.group(5);
        }
        return null;
    }

    public static void execute(String[] command) {
        try {
            Runtime.getRuntime().exec(command).waitFor();
            Thread.sleep(6000);
        } catch (InterruptedException ie) {
            logger.error("Updating IpTable is failed due to : " + ExceptionUtils.getStackTrace(ie));
        } catch (IOException io) {
            logger.error("Updating IpTable is failed due to : " + ExceptionUtils.getStackTrace(io));
        }
    }

    /**
     * Get Application Uid.
     *
     * @param context     android.content.Context
     * @param packageName application package name
     * @return application UID
     */
    public static int getApplicationUid(Context context, String packageName) {
        int result = 0;
        final PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(packageName)) {
                result = packageInfo.uid;
                break;
            }
        }
        return result;
    }

    /**
     * Ban Network for all applications
     *
     * @param ban if value true rule is added and blocked any network activities,
     *            if value false - rule removed
     */
    public static void banNetworkForAllApplications(boolean ban) throws IOException, InterruptedException {
        if (ban) {
            checkAddRuleToIPv4Table("-I OUTPUT -j REJECT", "filter");
        } else {
            checkAddRuleToIPv4Table("-D OUTPUT -j REJECT", "filter");
        }
    }

    /**
     * Allow network for application
     *
     * @param addRule        if value true rule added, if false - rule removed
     * @param applicationUid application UID
     */
    public static void allowNetworkForApplication(boolean addRule, int applicationUid) throws IOException, InterruptedException {
        if (addRule) {
            checkAddRuleToIPv4Table("-I OUTPUT -m owner --uid-owner " + applicationUid + " -j ACCEPT", "filter");
        } else {
            checkAddRuleToIPv4Table("-D OUTPUT -m owner --uid-owner " + applicationUid + " -j ACCEPT", "filter");
        }
    }

    /**
     * Allow network for application
     *
     * @param addRule     if value true rule added, if false - rule removed
     * @param packageName application package name
     * @param context     android.content.Context
     */
    public static void allowNetworkForApplication(boolean addRule, String packageName, Context context) throws IOException, InterruptedException {
        int uid = getApplicationUid(context, packageName);
        allowNetworkForApplication(addRule, uid);
    }

    /**
     * Print into log current state selected table of iptables
     */
    public static void printSelectedTableIpTables(String table){
        List<String> command = new ArrayList<String>(1);
        command.add("iptables -t "+table+" -n -L -v --line-numbers");
        List<String> output = ShellUtil.execWithCompleteResultWithListOutput(command, true);
        for (String str : output){
            logger.info(str);
        }
    }

    /**
     * Make relay server unreachable/reachable
     * @param banRelay if true make unreachable
     */
    public static void banRelayServer(boolean banRelay) throws IOException, InterruptedException {
        if (banRelay) {
            checkAddRuleToIPv4Table("-I INPUT -m conntrack --ctorigdstport 7735 -j REJECT", "filter");
        } else {
            checkAddRuleToIPv4Table("-D INPUT -m conntrack --ctorigdstport 7735 -j REJECT", "filter");
        }
    }

    public static void banRelayServer(boolean banRelay, boolean useTcpkill) throws InterruptedException, IOException {
        if (!banRelay || !useTcpkill) {
            banRelayServer(banRelay);
            return;
        } else {
            TcpkillUtil relayTcpkill = new TcpkillUtil(TFConstantsIF.EXTERNAL_IP, TFConstantsIF.RELAY_PORT);
            banRelayServer(banRelay);
            relayTcpkill.startProcess();
            TestUtil.sleep(5 * 1000);
            relayTcpkill.stopProcess();
        }
    }

    /**
     * Rule to add port bypath OC
     * @param port
     * @param add if true make bypath
     */
    public static void bypassPort(int port, boolean add) throws IOException, InterruptedException {
        if(add) {
            checkAddRuleToIPv4Table("-I OUTPUT -m conntrack --ctorigdstport " + port + " -j ACCEPT", "nat");
        } else {
            checkAddRuleToIPv4Table("-D OUTPUT -m conntrack --ctorigdstport " + port + " -j ACCEPT", "nat");
        }
    }

    public static int findRule(String regexp, String table, String chain) {
        int count = 0;
        List command = new ArrayList<String>();
        command.add(TFConstantsIF.IPTABLES_PATH +  " -t " + table + " -L " + chain);
        List<String> result;
        result = ShellUtil.execWithCompleteResultWithListOutput(command, true);
        for (String s : result) {
            if(s.matches(regexp)) {
                count++;
            }
        }
        return count;
    }

    public static void banAllAppsButOC(boolean ban, Context context) throws IOException, InterruptedException {
        final int csa = IpTablesUtil.getApplicationUid(context, TFConstantsIF.OC_PACKAGE_NAME);
        final int csat = IpTablesUtil.getApplicationUid(context, TFConstantsIF.IT_PACKAGE_NAME);
        IpTablesUtil.banNetworkForAllApplications(ban);
        IpTablesUtil.allowNetworkForApplication(ban, csa);
        IpTablesUtil.allowNetworkForApplication(ban, csat);
    }

    public static void banAddress(String address) throws IOException {
        String[] command = {"su", "-c", TFConstantsIF.IPTABLES_PATH + " -A INPUT -s " + address + " -j DROP"};
        Runtime.getRuntime().exec(command);
    }

    public static void unbanAddress(String address) throws IOException {
        String[] command = {"su", "-c", TFConstantsIF.IPTABLES_PATH + " -D INPUT -s  " + address + " -j DROP"};
        Runtime.getRuntime().exec(command);
    }
}
