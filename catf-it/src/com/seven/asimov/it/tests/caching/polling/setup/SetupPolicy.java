package com.seven.asimov.it.tests.caching.polling.setup;

import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.pms.PMSUtil;
import com.seven.asimov.it.utils.pms.Policy;

import java.util.ArrayList;
import java.util.List;

public class SetupPolicy extends TcpDumpTestCase {

    public void testSetupPolicy() throws Exception {
        String ZERO = "0";
        String ONE = "1";
        List<Policy> list = new ArrayList<Policy>();

        list.add(Policy.Policies.ENABLED.getPolicy(ZERO, false));
        list.add(Policy.Policies.TRANSPARENT.getPolicy(ONE, false));
        list.add(Policy.Policies.ROAMING_WIFI.getPolicy(ONE, false));
        list.add(Policy.Policies.CACHE_INVALIDATE_AGGRESSIVENESS.getPolicy(ZERO, true));
        list.add(Policy.Policies.NO_CACHE_INVALIDATE_AGGRESSIVENESS.getPolicy(ZERO, true));
        list.add(Policy.Policies.OUT_OF_ORDER_AGGRESSIVENESS.getPolicy(ZERO, true));
        list.add(new Policy(Policy.Policies.RESPONSE_HEADER_RULES.getName(), "X-OC-.*:.*\\r\\n",
                Policy.Policies.RESPONSE_HEADER_RULES.getPath() + "@" + TEST_RESOURCE_HOST + "@.*", true));
        list.add(new Policy(Policy.Policies.REQUEST_HEADER_RULES.getName(), "X-OC-.*:.*\\r\\n",
                Policy.Policies.REQUEST_HEADER_RULES.getPath() + "@" + TEST_RESOURCE_HOST + "@.*", true));
        PMSUtil.addPolicies(list.toArray(new Policy[list.size()]));
        logSleeping(25 * 1000);
        PMSUtil.pushToClient();
    }
}