package com.seven.asimov.it.testcases;


import android.util.Log;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.tasks.firewallTasks.FirewallLogTask;
import com.seven.asimov.it.utils.logcat.wrappers.FirewallLogWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.LogEntryWrapper;
import com.seven.asimov.it.utils.pcf.*;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpStatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptsTestCase extends TcpDumpTestCase {
    protected final int LITTLE_DELAY = 10 * 1000;

    protected void provisionPCFChanges() {
        TestUtil.sleep(5 * LITTLE_DELAY);
        List<Change> changes = PcfHelper.retrieveAllChanges();
        if (changes != null) {
            for (Change change : changes) {
                PcfHelper.setChangePriority(change, PcfHelper.Priority.URGENT);
            }
            Log.v("###DEBUG", "provisionPCFChanges");
            changes = PcfHelper.retrieveAllChanges();
            for (Change change : changes) {
                Log.v("###DEBUG", change.toString());
            }
        }
        PcfHelper.provisionAllPendingChanges();
    }

    protected void cleanPCFUserGroup(String groupName) {
        UserGroup userGroup = PcfHelper.retrieveUserGroupByName(groupName);

        if (userGroup == null)
            return;

        User currentUser = PcfHelper.retrieveUserBy7tp();
        assertNotNull("Not found user by 7tp, check your OC Client has msisdn validation enabled.", currentUser);
        List<UserGroup> userGroups = PcfHelper.retrieveAllUsergroupsForUserById(currentUser.getId());

        for (UserGroup group : userGroups) {
            PcfHelper.removeUserFromGroup(group.getId(), currentUser.getId());
        }

        List<PcfPolicy> pcfPolicies = PcfHelper.retrieveListOfPoliciesForUserGroup(userGroup.getId());
        for (PcfPolicy pcfPolicy : pcfPolicies) {
            List<Service> pcfServices = PcfHelper.retrieveListOfServicesForPolicy(pcfPolicy.getId());
            if ((pcfServices != null) && (!pcfServices.isEmpty()))
                PcfHelper.removeServicesFromPolicy(pcfPolicy.getId(), (Service[]) pcfServices.toArray(new Service[pcfServices.size()]));

            for (Service service : pcfServices)
                PcfHelper.removeServiceById(service.getId());

            PcfHelper.removePolicyById(pcfPolicy.getId());
        }

        PcfHelper.removeUserGroupById(userGroup.getId());
        provisionPCFChanges();
    }


    protected void modifyActiveState(String pcfPolicyName, boolean enable) {
        try {
            PcfHelper.updateActiveState(pcfPolicyName, !enable);
            PcfHelper.updateActiveState(pcfPolicyName, enable);
        } catch (Exception e) {
            Log.e("###DEBUG", "Failed to modifyActiveState:" + ExceptionUtils.getStackTrace(e));
        }
        provisionPCFChanges();
    }

    protected void setUpPcfPolicy(String pcfGroupName, String pcfPolicyName, String pcfServiceName, String applicationName, boolean active) {
        try {

            User currentUser = PcfHelper.retrieveUserBy7tp();
            assertNotNull("Not found user by 7tp, check your OC Client has msisdn validation enabled.", currentUser);

            ArrayList<Restriction> listRestrictions = new ArrayList<Restriction>();
            listRestrictions.add(new Restriction("tcp", PcfHelper.InterfaceType.ALL, "80", applicationName, null, null));
            Service pcfService = new Service("", pcfServiceName, listRestrictions, applicationName, "", PcfHelper.Version.SAVED, PcfHelper.Type.APPLICATION, null, "", System.currentTimeMillis(), PcfHelper.Status.UPTODATE);
            Trigger trigger = new Trigger(true, 0, false, 0, 0, false, 0, 0, false, false);
            PcfPolicy pcfPolicy = new PcfPolicy(pcfPolicyName, PcfHelper.Version.SAVED, "", trigger, null, PcfHelper.Status.UPTODATE, PcfHelper.InterfaceType.ALL, PcfHelper.Type.APPLICATION, active);
            PcfPolicy createdPolicy = PcfHelper.createNewPolicy(pcfPolicy);
            Service createdService = PcfHelper.createNewService(pcfService);

            PcfHelper.assignServicesToPolicy(createdPolicy.getId(), new GenericId(createdService.getId(), createdService.getName(), createdService.getStatus()));

            UserGroup userGroup = PcfHelper.createNewUserGroup(pcfGroupName);
            PcfHelper.assignPoliciesToGroup(userGroup.getId(), new GenericId(createdPolicy.getId(), createdPolicy.getName(), createdPolicy.getStatus()));

            PcfHelper.assignUsersToGroup(userGroup.getId(), currentUser.getId());
            provisionPCFChanges();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected int getRefCountForRule(long ruleID) throws Exception {
        final String IPTABLES_REGEX = "Chain Z7BASECHAIN([0-9A-Fa-f]+) \\((\\d+) references\\)";
        final Pattern iptablesPattern = Pattern.compile(IPTABLES_REGEX);
        Matcher matcher;
        Integer result = null;

        Process iptablesProcess = Runtime.getRuntime().exec(new String[]{"su", "-c", "iptables -t filter -L"});
        BufferedReader reader = new BufferedReader(new InputStreamReader(iptablesProcess.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            Log.v("###DEBUG", line + "\n");
            matcher = iptablesPattern.matcher(line);
            if (matcher.find()) {
                Log.v("###DEBUG", "Matched " + line + "\n");
                Log.v("###DEBUG", "group(1) " + matcher.group(1).toLowerCase() + "\n");
                Log.v("###DEBUG", "ruleID " + Long.toHexString(ruleID).toLowerCase() + "\n");
                if (matcher.group(1).toLowerCase().endsWith(Long.toHexString(ruleID).toLowerCase())) {
                    Log.v("###DEBUG", "Found " + line + "\n");
                    assertNull("Found more then one iptables record with id=" + ruleID, result);
                    result = Integer.valueOf(matcher.group(2));
                }
            }

        }

        return result == null ? 0 : result;
    }

    protected long checkFirewallRuleApplied(String ruleName, FirewallLogTask task, int refCount) throws Exception {
        assertFalse("Expected to have FirewallLog entries but found none", task.getLogEntries().isEmpty());
        FirewallLogWrapper lastWrapper = null;
        for (FirewallLogWrapper wrapper : task.getLogEntries())
            if (wrapper.getName().indexOf(ruleName) != -1)
                lastWrapper = wrapper;

        assertTrue("Expected to find FirewallLog for script " + ruleName, lastWrapper != null);

        int actualRefCount = getRefCountForRule(lastWrapper.getChainID());
        assertEquals("Expected to have rule:" + ruleName + ", refcount:" + refCount + " but actual refcount is:" + actualRefCount, actualRefCount, refCount);
        return lastWrapper.getChainID();
    }

    class RequesterThread implements Callable<Boolean> {
        int timeout;
        HttpRequest requestToSend;

        public RequesterThread(HttpRequest requestToSend, int timeout) {
            this.requestToSend = requestToSend;
            this.timeout = timeout;
        }

        @Override
        public Boolean call() throws Exception {
            assertTrue(sendRequest(requestToSend, null, false, false, Body.BODY, timeout, null).getStatusCode() == HttpStatus.SC_OK);
            return true;
        }
    }

    protected void checkNetworkResourcesAccessible(String resourceEnding, boolean accessible) {
        final int THREAD_COUNT = 3;
        final int REQUEST_TIMEOUT = 20;
        HttpRequest request;
        List<Future> results = new LinkedList<Future>();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            request = createRequest().setUri(createTestResourceUri(resourceEnding)).setMethod("GET").getRequest();
            RequesterThread thread = new RequesterThread(request, REQUEST_TIMEOUT * 1000);
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
            } catch (Exception e) {
                Log.v("###DEBUG", "Exception in RequesterThread: " + ExceptionUtils.getStackTrace(e));
            }
        }
        assertTrue("Expected network resources to be " + (accessible ? "reachable" : "not reachable") + " but they are " + ((successCount != 0) ? "reachable" : "not reachable"), (successCount != 0) == accessible);
    }

    protected void setUpServicesForPolicy(String pcfPolicyName, String pcfServiceName, String applicationName, boolean active) {
        try {
            PcfPolicy pcfPolicy = PcfHelper.retrievePolicyByName(pcfPolicyName);
            if (pcfPolicy == null)
                return;
            ArrayList<Restriction> listRestrictions = new ArrayList<Restriction>();
            listRestrictions.add(new Restriction("tcp", PcfHelper.InterfaceType.ALL, "80", applicationName, null, null));
            Service pcfService = new Service("", pcfServiceName, listRestrictions, applicationName, "", PcfHelper.Version.SAVED, PcfHelper.Type.APPLICATION, null, "", System.currentTimeMillis(), PcfHelper.Status.UPTODATE);
            Service createdService = PcfHelper.createNewService(pcfService);

            PcfHelper.assignServicesToPolicy(pcfPolicy.getId(), new GenericId(createdService.getId(), createdService.getName(), createdService.getStatus()));

            modifyActiveState(pcfPolicyName, active);
        } catch (Exception e) {
            Log.e("###DEBUG", "setUpServicesForPolicy exception:" + ExceptionUtils.getStackTrace(e));
        }
    }

    protected void cleanServicesFromPCFPolicy(String pcfPolicyName) {
        try {
            PcfPolicy pcfPolicy = PcfHelper.retrievePolicyByName(pcfPolicyName);
            if (pcfPolicy == null)
                return;
            List<Service> pcfServices = PcfHelper.retrieveListOfServicesForPolicy(pcfPolicy.getId());
            if ((pcfServices != null) && (!pcfServices.isEmpty()))
                PcfHelper.removeServicesFromPolicy(pcfPolicy.getId(), (Service[]) pcfServices.toArray(new Service[pcfServices.size()]));

            for (Service service : pcfServices)
                PcfHelper.removeServiceById(service.getId());

            modifyActiveState(pcfPolicyName, true);
        } catch (Exception e) {
            Log.e("###DEBUG", "cleanServicesFromPCFPolicy exception:" + ExceptionUtils.getStackTrace(e));
        }
    }

    protected void waitForTimerConditionTrigger(LogEntryWrapper timerActivationEntry, int timerValue) {
        if ((System.currentTimeMillis() - timerActivationEntry.getTimestamp()) < (timerValue * 1000 + LITTLE_DELAY))
            TestUtil.sleep((timerValue * 1000 + LITTLE_DELAY) - (System.currentTimeMillis() - timerActivationEntry.getTimestamp()));
    }
}
