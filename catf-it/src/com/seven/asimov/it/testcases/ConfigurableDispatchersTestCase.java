package com.seven.asimov.it.testcases;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.IpTablesUtil;
import com.seven.asimov.it.utils.logcat.tasks.CLQConstructedTask;
import com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks.CLQConstructedDnsTask;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyAddedTask;
import com.seven.asimov.it.utils.logcat.wrappers.*;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.tcpdump.HttpSession;
import com.seven.asimov.it.utils.tcpdump.TcpDumpUtil;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConfigurableDispatchersTestCase extends TcpDumpTestCase {

    private static final String TAG = ConfigurableDispatchersTestCase.class.getSimpleName();

    protected final static String INTERCEPTION_PORTS = "interception_ports";
    protected final static String INTERCEPTION_PATH = "@asimov@interception@";
    protected final static String ASIMOV_HTTP_PATH = "@asimov@http@";
    protected final static String TYPE = "type";
    protected final static String Z_ORDER = "z_order";
    protected String ocPackageName = "com.seven.asimov";
    protected String tfPackageName = "com.seven.asimov.it";

    public List<String> addPersonalInterceptionProperty(String name, String interceptionPorts, String type, String zOrder) {
        List<String> list = new ArrayList<String>();
        list.add(INTERCEPTION_PATH + name);
        PMSUtil.createNameSpace(INTERCEPTION_PATH.substring(0, INTERCEPTION_PATH.length() - 1), name);
        list.add(PMSUtil.createPersonalScopeProperty(INTERCEPTION_PORTS, INTERCEPTION_PATH + name, interceptionPorts, true, false));
        list.add(PMSUtil.createPersonalScopeProperty(TYPE, INTERCEPTION_PATH + name, type, true, false));
        list.add(PMSUtil.createPersonalScopeProperty(Z_ORDER, INTERCEPTION_PATH + name, zOrder, true));
        return list;
    }

    public void removePersonalInterceptionProperties(List<String> idPropertiesList) {
        if (idPropertiesList == null) return;
        for (int i = idPropertiesList.size() - 1; i >= 0; i--) {
            if (i == 0) {
                PMSUtil.deleteNamespace(idPropertiesList.get(i));
            } else {
                PMSUtil.deleteProperty(idPropertiesList.get(i));
            }
        }
    }

    public boolean checkInterceptionPolicyReceived(PolicyAddedTask policyAddedTask, String interceptionPorts, String type, String zOrder) {
        boolean interceptionPortsPolicyFound = false;
        boolean typePolicyFound = false;
        boolean zOrderPolicyFound = false;
        for (PolicyWrapper policyLogEntry : policyAddedTask.getLogEntries()) {
            if (INTERCEPTION_PORTS.equals(policyLogEntry.getName()) && interceptionPorts.equals(policyLogEntry.getValue())) {
                interceptionPortsPolicyFound = true;
            }
            if (TYPE.equals(policyLogEntry.getName()) && type.equals(policyLogEntry.getValue())) {
                typePolicyFound = true;
            }
            if (Z_ORDER.equals(policyLogEntry.getName()) && zOrder.equals(policyLogEntry.getValue())) {
                zOrderPolicyFound = true;
            }
            if (interceptionPortsPolicyFound && typePolicyFound && zOrderPolicyFound) return true;
        }
        return false;
    }

    public boolean checkPolicyReceived(PolicyAddedTask policyAddedTask, String propertyName, String value) {
        boolean propertyFound = false;
        for (PolicyWrapper entry : policyAddedTask.getLogEntries()) {
            if (entry.getName().equals(propertyName) && entry.getValue().equals(value)) {
                propertyFound = true;
                break;
            }
        }
        return propertyFound;
    }

    public boolean checkChangedDispatcherState(List<DispatcherStateWrapper> stateWrapperList, String dispatcherName,
                                               long startTime, long endTime, DispatcherState... stateList) {
        int count = 0;
        if (dispatcherName == null) {
            for (DispatcherStateWrapper wrapper : stateWrapperList) {
                if (wrapper.getName().contains("http") || wrapper.getName().equals("http")) {
                    dispatcherName = wrapper.getName();
                    break;
                }
            }
            assertNotNull("HTTP dispatcher not found in sys log", dispatcherName);
        }
        for (DispatcherState state : stateList) {
            for (DispatcherStateWrapper wrapper : stateWrapperList) {
                if (wrapper.getTimestamp() < startTime || wrapper.getTimestamp() > endTime) continue;
                if (state.toString().equals(wrapper.getState())) {
                    count++;
                    break;
                }
            }
        }
        return stateList.length == count;
    }

    public boolean checkChangedDispatcherState(List<DispatcherStateWrapper> stateWrapperList,
                                               long startTime, long endTime, DispatcherState... stateList) {
        return checkChangedDispatcherState(stateWrapperList, null, startTime, endTime, stateList);
    }

    public boolean checkDispatcherNotChangeItState(List<DispatcherStateWrapper> stateWrapperList, long startTime, long endTime, DispatcherState... stateList) {
        String dispatcherName = null;
        for (DispatcherStateWrapper wrapper : stateWrapperList) {
            if (wrapper.getName().contains("http")) {
                dispatcherName = wrapper.getName();
                break;
            }
        }
        assertNotNull("HTTP dispatcher not found in sys log", dispatcherName);
        Iterator<DispatcherStateWrapper> iterator = stateWrapperList.iterator();
        DispatcherStateWrapper wrapper;
        while (iterator.hasNext()) {
            wrapper = iterator.next();
            if (wrapper.getTimestamp() < startTime || wrapper.getTimestamp() > endTime
                    || !wrapper.getName().equals(dispatcherName)) iterator.remove();
        }
        int index = 0;
        boolean foundState = false;
        for (int i = 0; i < stateList.length; i++) {
            for (int j = index; j < stateWrapperList.size(); j++) {
                if (stateWrapperList.get(j).getState().equals(stateList[i].toString())) {
                    foundState = true;
                    index = j;
                    break;
                }
            }
            if (foundState && i < stateList.length - 1) {
                foundState = false;
            } else if (foundState && i == stateList.length - 1) {
                for (int j = index; j < stateWrapperList.size(); j++) {
                    if (!stateWrapperList.get(j).getState().equals(stateList[i].toString())) {
                        foundState = false;
                        break;
                    }
                }
            } else {
                break;
            }
        }
        return foundState;
    }

    public boolean checkTcpTrafficProcessedBySpecifiedDispatcher(String dispatcher, CLQConstructedTask clqConstructedTask) {
        boolean result = false;
        for (CLQConstructedWrapper entry : clqConstructedTask.getLogEntries()) {
            if (entry.getDispatcher().toLowerCase().equals(dispatcher.toLowerCase())) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean checkUdpTrafficProcessedBySpecifiedDispatcher(String dispatcher, CLQConstructedDnsTask clqConstructedDnsTask) {
        boolean result = false;
        for (CLQConstructedDnsWrapper entry : clqConstructedDnsTask.getLogEntries()) {
            if (entry.getDispatcher().toLowerCase().equals(dispatcher.toLowerCase())) {
                result = true;
                break;
            }
        }
        return result;
    }

    public void openBrowser(Context mContext, String uri) {
        Uri address = Uri.parse(uri);
        Intent openLink = new Intent(Intent.ACTION_VIEW, address);
        openLink.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(openLink);
        logSleeping(15 * 1000);
    }

    public void launchAnotherApplication(Context mContext, String appPackage) {
        Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(appPackage);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(launchIntent);
        logSleeping(15 * 1000);
    }

    public boolean checkTrafficInTcpDumpByHost(TcpDumpUtil tcpDumpUtil, long startTime, long endTime, String host, boolean ssl) throws UnknownHostException {
        String address = InetAddress.getByName(host).getHostAddress();
        List<HttpSession> sessionList;
        if (ssl) {
            sessionList = tcpDumpUtil.getHttpsSessions(startTime, endTime);
        } else {
            sessionList = tcpDumpUtil.getHttpSessions(startTime, endTime);
        }
        for (HttpSession session : sessionList) {
            if (session.getServerAddress().equals(address)) return true;
        }
        return false;
    }

    public boolean checkTrafficInLogCutByHost(List<NetlogEntry> netlogEntryList, String host, String applicationName) {
        for (NetlogEntry entry : netlogEntryList) {
            if (entry.getApplicationName().equals(applicationName) && host.contains(entry.getHost())) return true;
        }
        return false;
    }

    public boolean checkTrafficInLogCutByPackage(List<NetlogEntry> netlogEntryList, String applicationName) {
        for (NetlogEntry entry : netlogEntryList) {
            if (entry.getApplicationName().equals(applicationName) && !(entry.getOpType() == OperationType.proxy_dns
                    || entry.getOpType() == OperationType.proxy_dns_delayed)) return true;
        }
        return false;
    }

    public boolean isApplicationInstalled(Context mContext, String packageName) {
        boolean appExists = false;
        final PackageManager pm = mContext.getPackageManager();
        for (ApplicationInfo packageInfo : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
            if (packageInfo.packageName.equals(packageName)) {
                appExists = true;
                break;
            }
        }
        return appExists;
    }

    public List<Integer> addNetworkRules(Context mContext, String... packageList) throws IOException, InterruptedException {
        List<Integer> uidList = new ArrayList<Integer>();
        PackageManager pm = mContext.getPackageManager();
        List<ApplicationInfo> appPackages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (String mPackage : packageList) {
            for (ApplicationInfo packageInfo : appPackages) {
                if (packageInfo.packageName.equals(mPackage)) {
                    int uid = packageInfo.uid;
                    uidList.add(uid);
                    IpTablesUtil.allowNetworkForApplication(true, uid);
                    break;
                }
            }
        }
        IpTablesUtil.banNetworkForAllApplications(true);
        return uidList;
    }

    public void removeNetworkRules(List<Integer> uidList) throws IOException, InterruptedException {
        IpTablesUtil.banNetworkForAllApplications(false);
        for (Integer uid : uidList) {
            IpTablesUtil.allowNetworkForApplication(false, uid);
        }
    }

    public boolean isNetworkAccessible() throws URISyntaxException, IOException {
        HttpRequest request = createRequest().setUri("http://www.belkonditer.ru/img/main/img720.jpg").setMethod("GET")
                .addHeaderField("Connection", "close")
                .addHeaderField("Cache-Control", "no-cache, no-store")
                .addHeaderField("Pragma", "no-cache")
                .getRequest();
        HttpResponse response = null;
        for (int i = 0; i < 5; i++) {
            try {
                response = sendRequest(request);
                if (response != null) {
                    return true;
                }
            } catch (UnknownHostException e) {
                Log.e(TAG, ExceptionUtils.getStackTrace(e));
            } finally {
                logSleeping(30 * 1000);
            }

        }
        return false;
    }
}
