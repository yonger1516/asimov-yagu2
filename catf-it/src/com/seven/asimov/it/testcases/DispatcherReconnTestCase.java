package com.seven.asimov.it.testcases;

import android.util.Log;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.logcat.tasks.DSCTask;
import com.seven.asimov.it.utils.logcat.tasks.ServiceLogTask;
import com.seven.asimov.it.utils.logcat.wrappers.DSCWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.ServiceLogWrapper;
import com.seven.asimov.it.utils.pms.Policy;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.seven.asimov.it.utils.pms.PMSUtil.addPolicies;
import static com.seven.asimov.it.utils.pms.PMSUtil.cleanPaths;


public class DispatcherReconnTestCase extends TcpDumpTestCase {

    private final Integer dispatchersID[] = {537285397, 775035067, 368413178, 1574866868};
    private final String dispatchers[] = {"octcpd", "ochttpd", "ocdnsd", "ocshttpd"};
    private final String enginePath = "@asimov@failovers@restart@engine";
    private final String controllerPath = "@asimov@failovers@restart@controller";
    private final String dispatchersPath = "@asimov@failovers@restart@dispatchers";
    private String paramName = "enabled";
    private String paramValue = "false";

    protected void checkAllDispatchersLogged(DSCTask dscTask) {
        List<Integer> dispatchersList = new ArrayList<Integer>(Arrays.asList(dispatchersID));

        for (DSCWrapper wrapper : dscTask.getLogEntries())
            dispatchersList.remove(new Integer(wrapper.getDispID()));

        assertTrue("Expected to find DSC for all dispatchers, but no DSC for next dispatchers: " + dispatchersList, dispatchersList.isEmpty());

    }

    protected void checkAllDispatchersLogged(ServiceLogTask serviceLog) {
        List<String> dispatchersList = new ArrayList<String>(Arrays.asList(dispatchers));

        for (ServiceLogWrapper wrapper : serviceLog.getLogEntries())
            dispatchersList.remove(wrapper.getExtra1());

        assertTrue("Expected to find ServiceLog for all dispatchers, but no ServiceLog for next dispatchers: " + dispatchersList, dispatchersList.isEmpty());
    }

    protected void switchRestartFailover(boolean enabled) throws Exception {
        if (!enabled) {
            try {
                addPolicies(new Policy[]{
                        new Policy(paramName, paramValue, enginePath, true),
                        new Policy(paramName, paramValue, controllerPath, true),
                        new Policy(paramName, paramValue, dispatchersPath, true)});
            } catch (Throwable e) {
                Log.e("###DEBUG", "Exception while switching Reset Failover: " + ExceptionUtils.getStackTrace(e));
            }
            logSleeping(TFConstantsIF.WAIT_FOR_POLICY_UPDATE * 2);
        } else {
            cleanPaths(new String[]{enginePath, controllerPath, dispatchersPath});
        }

    }
}
