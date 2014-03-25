package com.seven.asimov.it.testcases;

import android.util.Log;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.pcf.*;
import com.seven.asimov.it.utils.sms.SmsUtil;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class E2EFirewallTestCase extends E2ETestCase {
    private static final String TAG = E2EFirewallTestCase.class.getSimpleName();
    private static final Logger logger = LoggerFactory.getLogger(E2EFirewallTestCase.class.getSimpleName());
    protected final int LITTLE_DELAY = 10 * 1000;
    protected final String ocshttpdPath = "@asimov@interception@ocshttpd";
    protected final String interceptionEnabled = "enabled";
    protected final String commonName = "BlockByHostname";
    protected final String pcfGroupName = commonName + "_group";
    protected final String pcfPolicyName = commonName + "_policy";
    protected final String pcfServiceName = commonName + "_service";
    protected final String pcfApplicationName = commonName + "_application";
    protected final String protocolANY = "";
    protected final String protocolTCP = "tcp";
    protected final String appGroup = "BbHname";
    protected final String blockingAppName = "com.seven.asimov";
    protected final String excludingAppName = "com.seven.asimov.it";
    protected final String blockedHost = "tln-dev-testrunner1.7sys.eu";
    protected final String interceptionPath = "@asimov@interception@octcpd";
    protected final String interceptionPolicy = "interception_ports";
    protected final String interceptionPorts = "1:24,26:109,111:219,221:442,444:464,466:586,588:992,994,996:7734,7736:8079,8082:8086,8088:8098,8100:8110,8112:10050,10052:65535";
    protected final String ipv6BlockedIP = "2620:0:5103:3::2";
    protected final String ipv6BlockedHost = "rwc-qa-testrunner1.7sys.eu";

    protected void provisionPCFChanges() {
        TestUtil.sleep(5 * LITTLE_DELAY);
        List<Change> changes = PcfHelper.retrieveAllChanges();
        if (changes != null) {
            for (Change change : changes) {
                //PcfHelper.setChangePriority(change, PcfHelper.Priority.URGENT);
            }
            Log.v("###DEBUG", "provisionPCFChanges");
            changes = PcfHelper.retrieveAllChanges();
            assertNotNull("Failed to list all PCF changes", changes);
            for (Change change : changes) {
                Log.v("###DEBUG", change.toString());
            }
        }
        PcfHelper.provisionAllPendingChanges();
        TestUtil.sleep(LITTLE_DELAY);
        try {
            (new SmsUtil(getContext())).sendPCFUpdate((byte) 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        TestUtil.sleep(LITTLE_DELAY);
    }

    protected void cleanPCFUserGroup(String groupName) {
        User currentUser = PcfHelper.retrieveUserBy7tp();
        assertNotNull("Not found user by 7tp, check your OC Client has msisdn validation enabled.", currentUser);
        List<UserGroup> userGroups = PcfHelper.retrieveAllUsergroupsForUserById(currentUser.getId());

        for (UserGroup group : userGroups) {
            PcfHelper.removeUserFromGroup(group.getId(), currentUser.getId());
        }

        UserGroup userGroup = PcfHelper.retrieveUserGroupByName(groupName);

        if (userGroup != null)
            PcfHelper.removeUserGroupById(userGroup.getId());

        PcfPolicy policy = PcfHelper.retrievePolicyByName(pcfPolicyName);

        if (policy != null)
            PcfHelper.removePolicyById(policy.getId());

        for (Service service : PcfHelper.retrieveAllServices()) {
            if (service.getName().equals(pcfServiceName) || service.getName().equals(pcfApplicationName))
                PcfHelper.removeServiceById(service.getId());
        }

        provisionPCFChanges();
    }

    public JSONObject BlockByHostnamePreparePCF(String[] blockedHosts, boolean blockedTCP, String blockedPort, PcfHelper.InterfaceType blockedInterface, boolean blockServiceOrApp, boolean excludeApp, boolean screenOffCondition) {
        User currentUser = PcfHelper.retrieveUserBy7tp();
        assertNotNull("Not found user by 7tp, check your OC Client has msisdn validation enabled.", currentUser);
        List<String> excludedApps = Arrays.asList(new String[]{excludingAppName});
        Trigger trigger = new Trigger(screenOffCondition, 0, false, 0, 0, false, 0, 0, false, !screenOffCondition);
        PcfPolicy pcfPolicy = new PcfPolicy(pcfPolicyName, PcfHelper.Version.SAVED, null, trigger, excludeApp ? excludedApps : null, PcfHelper.Status.UPTODATE, blockedInterface, PcfHelper.Type.APPLICATION, true);
        PcfPolicy createdPolicy = PcfHelper.createNewPolicy(pcfPolicy);

        List<Restriction> serviceRestrictions = new LinkedList<Restriction>();

        if ((blockedHosts == null) || (blockedHosts.length == 0))
            blockedHosts = new String[]{""};

        for (String host : blockedHosts) {
            serviceRestrictions.add(new Restriction(blockedTCP ? protocolTCP : protocolANY, null, blockedPort, null, host, null));
        }

        List<Restriction> applicationRestrictions = new LinkedList<Restriction>();

        for (String host : blockedHosts) {
            applicationRestrictions.add(new Restriction(blockedTCP ? protocolTCP : protocolANY, null, blockedPort, excludeApp ? excludingAppName : blockingAppName, host, null));
        }

        Service pcfService = new Service("", pcfServiceName, serviceRestrictions, "", "", PcfHelper.Version.SAVED, PcfHelper.Type.SERVICE, null, "", System.currentTimeMillis(), PcfHelper.Status.UPTODATE);
        Service createdService = PcfHelper.createNewService(pcfService);

        Service pcfApplication = new Service("", pcfApplicationName, applicationRestrictions, excludeApp ? excludingAppName : blockingAppName, appGroup, PcfHelper.Version.SAVED, PcfHelper.Type.APPLICATION, null, "", System.currentTimeMillis(), PcfHelper.Status.UPTODATE);
        Service createdApplication = PcfHelper.createNewService(pcfApplication);

        if (blockServiceOrApp)
            PcfHelper.assignServicesToPolicy(createdPolicy.getId(), new GenericId(createdService.getId(), createdService.getName(), createdService.getStatus()));
        else
            PcfHelper.assignServicesToPolicy(createdPolicy.getId(), new GenericId(createdApplication.getId(), createdApplication.getName(), createdApplication.getStatus()));

        UserGroup userGroup = PcfHelper.createNewUserGroup(pcfGroupName);
        PcfHelper.assignPoliciesToGroup(userGroup.getId(), new GenericId(createdPolicy.getId(), createdPolicy.getName(), createdPolicy.getStatus()));
        PcfHelper.assignUsersToGroup(userGroup.getId(), currentUser.getId());
        provisionPCFChanges();

        Service[] deltaServices;
        if (blockServiceOrApp)
            deltaServices = new Service[]{pcfService};
        else
            deltaServices = new Service[]{pcfApplication};

        JSONObject pcfDelta = null;

        try {
            pcfDelta = makePCFDelta(pcfPolicy, deltaServices);
        } catch (Exception e) {
            throw new AssertionFailedError("Failed to calculate PCFDiff due to\n " + ExceptionUtils.getFullStackTrace(e));
        }

        Log.v("###DEBUG", pcfDelta.toString());
        return pcfDelta;
    }

    class RequesterThread implements Callable<Boolean> {
        int timeout;
        HttpRequest requestToSend;
        boolean is404Acceptable;

        public RequesterThread(HttpRequest requestToSend, int timeout, boolean is404Acceptable) {
            this.requestToSend = requestToSend;
            this.timeout = timeout;
            this.is404Acceptable = is404Acceptable;
        }

        @Override
        public Boolean call() throws Exception {
            int statusCode = sendRequest(requestToSend, null, false, false, Body.BODY, timeout, null).getStatusCode();
            assertTrue("Unexpected status code:" + statusCode,
                    (statusCode == HttpStatus.SC_OK) ||
                            ((is404Acceptable) && (statusCode == HttpStatus.SC_NOT_FOUND)));
            Log.v("###DEBUG", "Response received for request: " + requestToSend.getUri());
            return true;
        }
    }

    private static String URI_SCHEME_HTTP = "http://";
    private static String URI_SCHEME_HTTPS = "https://";

    protected String createTestResourceUri(String host, String pathEnd, boolean useSSL, int port) {
        StringBuilder uri = new StringBuilder();
        uri.append(useSSL ? URI_SCHEME_HTTPS : URI_SCHEME_HTTP)
                .append(host).append(":").append(String.valueOf(port)).append("/")
                .append(createTestResourcePath(pathEnd));
        return uri.toString();
    }

    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
    SSLSocketFactory defaultSSLFactory = null;

    /**
     * Trust every server - dont check for any certificate
     */
    protected void trustAllHosts(boolean trust) {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        }};

        // Install the all-trusting trust manager
        try {
            if (trust) {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                defaultSSLFactory = HttpsURLConnection
                        .getDefaultSSLSocketFactory();
                HttpsURLConnection
                        .setDefaultSSLSocketFactory(sc.getSocketFactory());
            } else {
                if (defaultSSLFactory != null)
                    HttpsURLConnection
                            .setDefaultSSLSocketFactory(defaultSSLFactory);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    final int BYPASS_PORT = 8099;
    final int HTTP_PORT = 80;
    final int HTTPS_PORT = 443;

    protected void checkNetworkResourcesAccessible(String resourceEnding, boolean ssl, boolean bypassOC, boolean accessible) {
        checkNetworkResourcesAccessible(blockedHost, resourceEnding, ssl, bypassOC, accessible);
    }

    protected void checkNetworkResourcesAccessible(String host, String resourceEnding, boolean ssl, boolean bypassOC, boolean accessible) {
        checkNetworkResourcesAccessible(host, resourceEnding, ssl, bypassOC, BYPASS_PORT, accessible, false);
    }

    protected void checkNetworkResourcesAccessible(String host, String resourceEnding, boolean ssl, boolean bypassOC, int bypassPORT, boolean accessible, boolean is404Acceptable) {
        final int THREAD_COUNT = 3;
        final int REQUEST_TIMEOUT = 20;
        HttpRequest request;
        List<Future> results = new LinkedList<Future>();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            request = createRequest().setUri(createTestResourceUri(host, (bypassOC ? "_DIRECT_" : "_OC_") + resourceEnding, ssl, bypassOC ? bypassPORT : (ssl ? HTTPS_PORT : HTTP_PORT))).setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

            RequesterThread thread = new RequesterThread(request, REQUEST_TIMEOUT * 1000, is404Acceptable);
            results.add(executor.schedule(thread, i * (REQUEST_TIMEOUT / (2 * THREAD_COUNT)), TimeUnit.SECONDS));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(REQUEST_TIMEOUT * 2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.v("###DEBUG", "Interrupted while waiting for executor threads");
            executor.shutdownNow();
        }

        int successCount = 0;
        for (Future result : results) {
            try {
                result.get();
                successCount++;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof FileNotFoundException) {
                    successCount++;     //FileNotFound exception is thrown when server answers with 404 error.
                    //this means that server is accessible.
                } else if (e.getCause() instanceof IOException) {
                    Log.v("###DEBUG", "Connection failed in RequesterThread: " + ExceptionUtils.getFullStackTrace(e));
                } else {
                    Log.v("###DEBUG", "Exception in RequesterThread: " + ExceptionUtils.getFullStackTrace(e));
                    throw new AssertionFailedError(e.getCause().getMessage());
                }
            } catch (InterruptedException e) {
                Log.v("###DEBUG", "InterruptedException in RequesterThread: " + ExceptionUtils.getMessage(e));
                throw new AssertionFailedError("RequesterThread was interrupted");
            }
        }
        assertTrue("Expected network resources to be " + (accessible ? "reachable" : "not reachable") + " but they are " + ((successCount != 0) ? "reachable" : "not reachable"), (successCount != 0) == accessible);
    }

    public void printIptables() {
        printIptables("filter");
    }

    public void printIptables(String table) {
        try {
            Process iptablesProcess = Runtime.getRuntime().exec(new String[]{"su", "-c", "iptables -t " + table + " -nL -v"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(iptablesProcess.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                Log.v("###DEBUG", line + "\n");
            }
        } catch (Exception e) {
        }
    }

    public String wrapNullString(String str) {
        if (str == null)
            return "";
        else
            return str;
    }

    public JSONObject makePCFDelta(PcfPolicy policy, Service[] services) throws JSONException {
        JSONObject resultObject = new JSONObject();
        JSONArray ruleGroupList = new JSONArray();
        JSONObject deltaObject = new JSONObject();
        ruleGroupList.put(deltaObject);
        resultObject.put("ruleGroupList", ruleGroupList);


        deltaObject.put("active", policy.isActive());
        deltaObject.put("policyName", policy.getName());
        //deltaObject.put("extraProps", JSONObject.NULL);
        deltaObject.put("type", 2);
        deltaObject.put("objectId", "00000000-0000-0000-0000-000000000000");

        if ((policy.getWhitelistedPackages() == null) || (policy.getWhitelistedPackages().isEmpty()))
            deltaObject.put("appList", JSONObject.NULL);
        else {
            JSONArray array = new JSONArray();
            for (String app : policy.getWhitelistedPackages())
                array.put(app);
            deltaObject.put("appList", array);
        }

        transformTriggerToDiff(deltaObject, policy.getTrigger());
        transformServicesToDiff(deltaObject, services, policy.getNetworkInterface().toString());

        return resultObject;
    }

    private void transformServicesToDiff(JSONObject deltaObject, Service[] services, String networkInterface) throws JSONException {
        JSONArray jsonRestrictions = new JSONArray();
        deltaObject.put("restrictionList", jsonRestrictions);
        for (Service service : services)
            for (Restriction restriction : service.getRestrictions()) {
                JSONObject jsonRestriction = new JSONObject();
                jsonRestriction.put("port", restriction.getsPort());
                jsonRestriction.put("networkInterface", networkInterface.equals("all") ? "" : networkInterface);
                jsonRestriction.put("protocol", wrapNullString(restriction.getProtocol()));

                jsonRestriction.put("packageName", wrapNullString(restriction.getPackageName()));

                jsonRestriction.put("objectId", "00000000-0000-0000-0000-000000000000");
                jsonRestriction.put("ip", wrapNullString(restriction.getIp()));
                JSONObject extraProps = new JSONObject();
                if (restriction.getIcmpType() != null && !restriction.getIcmpType().equals("")) {
                    extraProps.put("icmpType", restriction.getIcmpType());
                    jsonRestriction.put("extraProps", extraProps);
                } else
                    jsonRestriction.put("extraProps", JSONObject.NULL);

                jsonRestrictions.put(jsonRestriction);
            }
    }

    private void transformTriggerToDiff(JSONObject deltaObject, Trigger trigger) throws JSONException {
        JSONObject jsonTrigger = new JSONObject();
        deltaObject.put("trigger", jsonTrigger);

        jsonTrigger.put("screen", trigger.isScreen());
        jsonTrigger.put("wifi", false);
        jsonTrigger.put("screenOffPeriod", trigger.getScreenOffPeriod());
        jsonTrigger.put("radio", trigger.isRadio());
        jsonTrigger.put("timeOfDayFrom", trigger.getTimeOfDayFrom());
        jsonTrigger.put("timeOfDay", trigger.isTimeOfDay());
        jsonTrigger.put("periodicLength", trigger.getPeriodLength());
        jsonTrigger.put("applyAlways", trigger.isApplyAlways());
        jsonTrigger.put("timeOfDayTo", trigger.getTimeOfDayTo());
        jsonTrigger.put("periodicBlockLength", trigger.getPeriodBlockLength());
        jsonTrigger.put("periodic", trigger.isPeriodic());

        jsonTrigger.put("extraProps", JSONObject.NULL);
    }

    public void checkPCFDeltaAtServer(String pcfDelta) {
        boolean isSuccess = false;
        try {
            String requestBody = String.format(FIREWALL_QUERY_PATTERN, getName(), "PCF_DELTA", pcfDelta);
            String response = sendPostRequestToRest(FIREWALL_PATH_END, requestBody);
            isSuccess = response != null && response.toLowerCase().contains(SUCCESS);
        } catch (Exception e) {
            Log.e("###DEBUG", "Failed to perform server side checks");
        }

        assertTrue("REST check failed. PCF Delta checks at server side failed ", isSuccess);
    }

    protected void init(String testCase) {
        try {
            notifyRestForTestsStart(TAG + "_" + testCase);
        } catch (Exception e) {
            logger.debug("Tests start REST notification failed");
            e.printStackTrace();
        }
    }

    protected void clean(String testCase) {
        try {
            notifyRestForTestEnd(TAG + "_" + testCase);
        } catch (Exception e) {
            logger.debug("Tests end REST notification failed");
            e.printStackTrace();
        }
    }
}
