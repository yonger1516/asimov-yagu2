package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.CustomService;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.ShellUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.httpsTasks.FCLTask;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;
import com.seven.asimov.it.utils.sms.SmsUtil;
import com.seven.asimov.it.utils.tcpdump.Interface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BypassPortTestCase extends TcpDumpTestCase {

    protected String name = "bypass_list";
    protected String path = "@asimov@interception";
    protected static HashMap<Integer, HttpRequest> requests = new HashMap();
    protected static String DEFAULT_SSL = ",,,,,,";
    public static List<CustomService> services = new ArrayList<CustomService>();

    private static final Logger logger = LoggerFactory.getLogger(BypassPortTestCase.class.getSimpleName());


    /**
     * @param requests            - http requests to different ports
     * @param configValue         - policy value
     * @param expectedHttpCount   - expected count of loopback http sessions
     * @param expectedHttpsCount  - // -
     * @param expectedCustomCount -//-
     * @param checks              - configured set of needed checks
     * @throws Exception
     */
    protected void bypathPortTest(HashMap<Integer, HttpRequest> requests, String configValue, int expectedHttpCount,
                                  int expectedHttpsCount, int expectedCustomCount, boolean[] checks) throws Exception {

        boolean configurateByPolicy = checks[0];
        boolean removePolicy = checks[1];
        boolean shouldBeInDispatCfg = checks[2];
        boolean shouldbeInIptables = checks[3];

        FCLTask fclHttpsTask = new FCLTask();
        LogcatUtil logcat;
        try {
            if (configValue != null) {
                if (configurateByPolicy) {
                    PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(name, configValue, path, true)});
                }
                if (removePolicy && checkIpTableCfg()) {
                    PMSUtil.cleanPaths(new String[]{path});
                    (new SmsUtil(AsimovTestCase.getStaticContext())).sendPolicyUpdate((byte) 1);
                }
            }
            TestUtil.sleep(TFConstantsIF.MINUTE);

            int local_hs = 0;
            if (requests != null) {
                long startTime = System.currentTimeMillis();
                for (Map.Entry<Integer, HttpRequest> entry : requests.entrySet()) {
                    logger.info("KEY:" + entry.getKey().intValue());
                    switch (entry.getKey().intValue()) {
                        case 443: {
                            logger.info("case 443");
                            sendHttpsRequest(entry.getValue());
                            break;
                        }
                        case 80: {
                            logger.info("case 80");
                            sendRequest2(entry.getValue());
                            break;
                        }
                        default: {
                            logger.info("case default");
                            logcat = new LogcatUtil(getContext(), fclHttpsTask);
                            logcat.start();
                            sendHttpsRequest(entry.getValue());
                            logcat.stop();
                            if(fclHttpsTask.getLogEntries() != null) {
                                local_hs += fclHttpsTask.getLogEntries().size();
                            }
                        }
                    }
                }

                long stopTime = System.currentTimeMillis();

                int actualHttpCount = 0;
                int actualHttpsCount = 0;

                for (Map.Entry<Integer, HttpRequest> entry : requests.entrySet()) {
                    if (entry.getKey().equals(80)) {
                        actualHttpCount += tcpDump.getHttpSessions(entry.getValue().getUri(), AsimovTestCase.TEST_RESOURCE_HOST, Interface.LOOPBACK, startTime, stopTime).size();
                    } else if (entry.getKey().equals(443)) {
                        actualHttpsCount += tcpDump.getHttpsSessions(AsimovTestCase.TEST_RESOURCE_HOST, Interface.LOOPBACK, startTime, stopTime).size();
                    }
                }
                assertEquals("HttpSession check", expectedHttpCount, actualHttpCount);
                assertEquals("HttpsSession check", expectedHttpsCount, actualHttpsCount);
                assertEquals("CustomSession check", expectedCustomCount, local_hs);
            }
            assertEquals("Dispatchers check", shouldBeInDispatCfg, checkDispatcherCfg(configValue));
            assertEquals("IpTables check", shouldbeInIptables, checkIpTableCfg());

        } finally {
            PMSUtil.cleanPaths(new String[]{path});
            services.clear();
        }
    }

    protected boolean setupInitialValue() throws Exception {
        boolean positive = true;
        String oldValue = "com.seven.asimov.it;11400:11500";
        try {
            PMSUtil.addPoliciesWithCheck(new Policy[]{new Policy(name, oldValue, path, true)});
        } catch (Throwable throwable) {
            positive = false;
        } finally {
            PMSUtil.cleanPaths(new String[]{path});
        }
        return positive;
    }

    protected static List<CustomService> prepareCustomPortsForTest(List<CustomService> services, int count) throws Exception {
        return prepareCustomPortsForTest(services, count, false);
    }

    protected static List<CustomService> prepareCustomPortsForTest(List<CustomService> services, boolean invalidate) throws Exception {
        return prepareCustomPortsForTest(services, 0, invalidate);
    }

    protected static List<CustomService> prepareCustomPortsForTest(List<CustomService> services, int count, boolean invalidate) throws Exception {
        if (invalidate && services != null) {
            for (CustomService sc : services) {
                CustomService.stopCustomSerivceSafelyParallel(sc);
            }
        } else {
            if (services != null) {
                for (int i = 0; i < count; i++) {
                    services.add(CustomService.reserveAndStartCustomSerivceParallel(DEFAULT_SSL));
                }
            }
        }
        return services;
    }

    protected String createPolicyValue(List<CustomService> services) {
        StringBuilder value = new StringBuilder();
        value.append("com.seven.asimov.it;");
        for (int i = 0; i < services.size() - 1; i++) {
            if (i == services.size() - 2) {
                value.append(services.get(i).port);
            } else {
                value.append(services.get(i).port + ",");
            }
        }
        return value.toString();
    }

    protected boolean checkDispatcherCfg(String policy) throws IOException, InterruptedException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(TFConstantsIF.DISPATCHERS_CFG_PATH));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.equals(policy + ";")) return true;
        }
        return false;
    }

    protected boolean checkIpTableCfg() throws IOException {

        String UID = null;
        String psRegexsp = "(u0_[a-z0-9]*).*com.seven.asimov.it";

        Matcher matcher;
        Process process = Runtime.getRuntime().exec("ps");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            logger.info(line);
            matcher = Pattern.compile(psRegexsp).matcher(line);
            if (matcher.find()) {
                UID = matcher.group(1);
            }
        }

        logger.info("UID_com.seven.asimov.test.tool" + UID);
        return findIptableRule(UID, true) && findIptableRule(UID, false);
    }

    /**
     * @param UID      - application UID
     * @param ip4table - if true - search in iptables, else - in ip6tables
     * @return
     */
    protected boolean findIptableRule(String UID, boolean ip4table) {

        Matcher matcher;
        String ipTableRegexsp = "CONNMARK\\s*all.*owner\\sUID\\smatch\\s" + UID + ".*";
        logger.info("UID_com.seven.asimov.test.tool" + UID);
        List<String> command = new ArrayList<String>();
        if (ip4table) {
            command.add(TFConstantsIF.IPTABLES_PATH + " -t mangle -L Z7BASECHAIN-prior");
        } else {
            command.add(TFConstantsIF.IP6TABLES_PATH + " -t mangle -L Z7BASECHAIN-prior");
        }
        List<String> output = ShellUtil.execWithCompleteResultWithListOutput(command, true);
        for (String str : output) {
            logger.info(str);
            matcher = Pattern.compile(ipTableRegexsp).matcher(str);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }
}
