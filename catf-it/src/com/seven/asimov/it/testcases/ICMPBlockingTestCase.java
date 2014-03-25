package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.tasks.firewallTasks.FirewallLogTask;
import com.seven.asimov.it.utils.logcat.wrappers.FirewallLogWrapper;
import com.seven.asimov.it.utils.pcf.*;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ICMPBlockingTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(ICMPBlockingTestCase.class.getSimpleName());

    protected static final int TARGET = 1;
    protected static final int PROTOCOL = 2;
    protected static final int SOURCE = 3;
    protected static final int DESTINATION = 4;
    protected static final int DESCRIPTION = 5;
    protected final int LITTLE_DELAY = 10 * 1000;
    protected final String scriptName = "icmpBlocking";
    protected final String pcfPolicyName = scriptName + "_policy";
    protected final String pcfGroupName = scriptName + "_group";
    protected final String pcfServiceName = scriptName + "_service";
    protected final String HOST = "2607:f8b0:400a:803::1013";
    protected final String HOST1 = "2607:f8b0:400a:803::1012";
    protected final String TF_PACKAGENAME = "com.seven.asimov.it";

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

    protected void provisionPCFChanges() {
        TestUtil.sleep(5 * LITTLE_DELAY);
        List<Change> changes = PcfHelper.retrieveAllChanges();
        if (changes != null) {
            for (Change change : changes) {
                PcfHelper.setChangePriority(change, PcfHelper.Priority.URGENT);
            }
            logger.debug("provisionPCFChanges");
            changes = PcfHelper.retrieveAllChanges();
            for (Change change : changes) {
                logger.info(change.toString());
            }
        }
        PcfHelper.provisionAllPendingChanges();
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

    protected int getRefCountForRule(long ruleID) throws Exception {
        final String IPTABLES_REGEX = "Chain Z7BASECHAIN([0-9A-Fa-f]+) \\((\\d+) references\\)";
        final Pattern iptablesPattern = Pattern.compile(IPTABLES_REGEX);
        Matcher matcher;
        Integer result = null;

        Process iptablesProcess = Runtime.getRuntime().exec(new String[]{"su", "-c", "iptables -t filter -L"});
        BufferedReader reader = new BufferedReader(new InputStreamReader(iptablesProcess.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            logger.info(line + "\n");
            matcher = iptablesPattern.matcher(line);
            if (matcher.find()) {
                logger.info("Matched " + line + "\n");
                logger.info("group(1) " + matcher.group(1).toLowerCase() + "\n");
                logger.info("ruleID " + Long.toHexString(ruleID).toLowerCase() + "\n");
                if (matcher.group(1).toLowerCase().endsWith(Long.toHexString(ruleID).toLowerCase())) {
                    logger.info("Found " + line + "\n");
                    assertNull("Found more then one iptables record with id=" + ruleID, result);
                    result = Integer.valueOf(matcher.group(2));
                }
            }

        }

        return result == null ? 0 : result;
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
            logger.debug("cleanServicesFromPCFPolicy exception:" + ExceptionUtils.getStackTrace(e));
        }
    }

    protected void modifyActiveState(String pcfPolicyName, boolean enable) {
        try {
            PcfHelper.updateActiveState(pcfPolicyName, !enable);
            PcfHelper.updateActiveState(pcfPolicyName, enable);
        } catch (Exception e) {
            logger.debug("Failed to modifyActiveState:" + ExceptionUtils.getStackTrace(e));
        }
        provisionPCFChanges();
    }

    protected String ip6tables() throws Exception {
        Process process = Runtime.getRuntime().exec("su -c ip6tables -t filter -L -n");
        process.waitFor();
        InputStream is = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuffer result = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line).append('\n');
        }
        return result.toString();
    }

    protected boolean pingHost(String host) throws IOException {
        logger.info("start ping");
        String resultString = null;
        try {
            Process process = Runtime.getRuntime().exec("su -c ping6 -c 5 " + host);
            process.waitFor();
            InputStream is = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuffer result = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append('\n');
            }
            resultString = result.toString();
            logger.info(result.toString());
        } catch (InterruptedException interruptedExceprion) {
            ExceptionUtils.getStackTrace(interruptedExceprion);
        }
        logger.info("end ping");

        return resultString.contains("packets received");
    }

    protected void addNewRestriction(Restriction... restriction) {
        PcfPolicy policy = PcfHelper.retrievePolicyByName(pcfPolicyName);
        List<Restriction> listRestrictions = Arrays.asList(restriction);
        Service pcfService = new Service("", pcfServiceName, listRestrictions, TF_PACKAGENAME, "", PcfHelper.Version.SAVED, PcfHelper.Type.APPLICATION, null, "", System.currentTimeMillis(), PcfHelper.Status.UPTODATE);
        Service createdService = PcfHelper.createNewService(pcfService);

        PcfHelper.assignServicesToPolicy(policy.getId(), new GenericId(createdService.getId(), createdService.getName(), createdService.getStatus()));

        modifyActiveState(pcfPolicyName, true);
    }

    protected boolean isIp6tables(long id, int i, String val) throws Exception {
        String reg = "(\\S+).\\s+(\\S+)\\s+(\\S+).\\s+(\\S+).\\s+(.*)";
        Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);

        Process process = Runtime.getRuntime().exec("su -c ip6tables -t filter -L -n");
        process.waitFor();
        InputStream is = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        boolean flag = false;
        while ((line = reader.readLine()) != null) {
            if (line.toLowerCase().contains(Long.toHexString(id).toLowerCase()) && !line.contains("all")) {
                flag = true;
            }
            if (flag) {
                if (line.startsWith("target")) {
                    continue;
                }
                if (line.isEmpty()) {
                    break;
                }
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    if (matcher.group(i).contains(val)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
