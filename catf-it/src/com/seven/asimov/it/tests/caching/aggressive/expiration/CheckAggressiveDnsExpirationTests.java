package com.seven.asimov.it.tests.caching.aggressive.expiration;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.testcases.DnsTestCase;
import com.seven.asimov.it.utils.MobileNetworkUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


@DeviceOnly
public class CheckAggressiveDnsExpirationTests extends DnsTestCase {
    private static final Logger logger = LoggerFactory.getLogger(CheckAggressiveDnsExpirationTests.class.getSimpleName());
    private static final int AGGRESSIVENESS_LEVEL_0 = 0;
    private static final int AGGRESSIVENESS_LEVEL_1 = 1;
    private static final int AGGRESSIVENESS_LEVEL_2 = 2;
    private static final int AGGRESSIVENESS_LEVEL_3 = 3;
    private final static int DNS_EXPIRE_SHIFT = 305 * 1000;


    @Override
    protected void runTest() throws Throwable {
        boolean isPassed;
        int numberOfAttempts = 0;
        List<String> counts = new ArrayList<String>();
        do {
            isPassed = true;
            numberOfAttempts++;
            try {

                super.runTest();

            } catch (Throwable throwable) {
                logger.error("Test failed due to " + ExceptionUtils.getStackTrace(throwable));
                isPassed = false;
                counts.add("Test failed due to Exception in " + numberOfAttempts + " attempt");
                cleanOCDnsCache();
                Thread.sleep(2 * 60 * 1000);
            }
        } while (!isPassed && numberOfAttempts < 3);

        assertTrue("The test was failed three times ", counts.size() != 3);
    }

    public void test_000_SetUpTests() throws Exception {
        switchRestartFailover(true);
        switchRestartFailover(false);
        MobileNetworkUtil.init(getContext()).on3gOnly();
        cleanOCDnsCache();
    }

    /**
     * 1. Set aggressiveness level to 0. Set ttl to 300s.
     * 2. Send dns request with radio up and screen on. Request should be resolved from network.
     * 3. Move time 305s forward.
     * 4. Send dns request with radio up and screen on. Request should be resolved from network.
     * 5. Move time 305s forward.
     * 6. Send dns request with radio up and screen off. Request should be resolved from network.
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_001_DnsCeExpiration_0_ScreenOffRadioUp() throws Throwable {
        checkAggressiveDnsExpiration(hosts[0], AGGRESSIVENESS_LEVEL_0,
                new ScreenState[]{ScreenState.SCREEN_ON, ScreenState.SCREEN_ON, ScreenState.SCREEN_OFF},
                new RadioState[]{RadioState.RADIO_UP, RadioState.RADIO_UP, RadioState.RADIO_UP},
                new boolean[]{true, true, true}, new int[]{0, DNS_EXPIRE_SHIFT, DNS_EXPIRE_SHIFT});
    }

    /**
     * 1. Set aggressiveness level to 0. Set ttl to 300s.
     * 2. Send dns request with radio up and screen on. Request should be resolved from network.
     * 3. Move time 305s forward.
     * 4. Send dns request with radio down and screen on. Request should be resolved from network.
     * 5. Move time 305s forward.
     * 6. Send dns request with radio down and screen off. Request should be resolved from network.
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_002_DnsCeExpiration_0_ScreenOffRadioDown() throws Throwable {
        checkAggressiveDnsExpiration(hosts[1], AGGRESSIVENESS_LEVEL_0,
                new ScreenState[]{ScreenState.SCREEN_ON, ScreenState.SCREEN_ON, ScreenState.SCREEN_OFF},
                new RadioState[]{RadioState.RADIO_UP, RadioState.RADIO_DOWN, RadioState.RADIO_DOWN},
                new boolean[]{true, true, true}, new int[]{0, DNS_EXPIRE_SHIFT, DNS_EXPIRE_SHIFT});
    }

    /**
     * 1. Set aggressiveness level to 1. Set ttl to 300s.
     * 2. Send dns request with radio up and screen on. Request should be resolved from network.
     * 3. Move time 305s forward.
     * 4. Send dns request with radio up and screen on. Request should be resolved from network.
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_003_DnsCeExpiration_1_ScreenOn() throws Throwable {
        checkAggressiveDnsExpiration(hosts[2], AGGRESSIVENESS_LEVEL_1,
                new ScreenState[]{ScreenState.SCREEN_ON, ScreenState.SCREEN_ON},
                new RadioState[]{RadioState.RADIO_UP, RadioState.RADIO_UP},
                new boolean[]{true, true}, new int[]{0, DNS_EXPIRE_SHIFT});
    }

    /**
     * 1. Set aggressiveness level to 1. Set ttl to 300s.
     * 2. Send dns request with radio up and screen on. Request should be resolved from network.
     * 3. Move time 305s forward.
     * 4. Send dns request with radio up and screen off. Request should be resolved from cache.
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_004_DnsCeExpiration_1_ScreenOff() throws Throwable {
        checkAggressiveDnsExpiration(hosts[3], AGGRESSIVENESS_LEVEL_1,
                new ScreenState[]{ScreenState.SCREEN_ON, ScreenState.SCREEN_OFF},
                new RadioState[]{RadioState.RADIO_UP, RadioState.RADIO_UP},
                new boolean[]{true, false}, new int[]{0, DNS_EXPIRE_SHIFT});
    }

    /**
     * 1. Set aggressiveness level to 2. Set ttl to 300s.
     * 2. Send dns request with radio up and screen on. Request should be resolved from network.
     * 3. Move time 305s forward.
     * 4. Send dns request with radio up and screen on. Request should be resolved from network.
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_005_DnsCeExpiration_2_ScreenOnRadioUp() throws Throwable {
        checkAggressiveDnsExpiration(hosts[4], AGGRESSIVENESS_LEVEL_2,
                new ScreenState[]{ScreenState.SCREEN_ON, ScreenState.SCREEN_ON},
                new RadioState[]{RadioState.RADIO_UP, RadioState.RADIO_UP},
                new boolean[]{true, true}, new int[]{0, DNS_EXPIRE_SHIFT});
    }

    /**
     * 1. Set aggressiveness level to 2. Set ttl to 300s.
     * 2. Send dns request with radio up and screen on. Request should be resolved from network.
     * 3. Move time 305s forward.
     * 4. Send dns request with radio down and screen on. Request should be resolved from cache.
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_006_DnsCeExpiration_2_ScreenOnRadioDown() throws Throwable {
        checkAggressiveDnsExpiration(hosts[5], AGGRESSIVENESS_LEVEL_2,
                new ScreenState[]{ScreenState.SCREEN_ON, ScreenState.SCREEN_ON},
                new RadioState[]{RadioState.RADIO_UP, RadioState.RADIO_DOWN},
                new boolean[]{true, false}, new int[]{0, DNS_EXPIRE_SHIFT});
    }

    /**
     * 1. Set aggressiveness level to 2. Set ttl to 300s.
     * 2. Send dns request with radio up and screen off. Request should be resolved from network.
     * 3. Move time 305s forward.
     * 4. Send dns request with radio down and screen off. Request should be resolved from cache.
     * 5. Move time 305s forward.
     * 6. Send dns request with radio up and screen off. Request should be resolved from cache.
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_007_DnsCeExpiration_2_ScreenOffRadioUp() throws Throwable {
        checkAggressiveDnsExpiration(hosts[6], AGGRESSIVENESS_LEVEL_2,
                new ScreenState[]{ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF},
                new RadioState[]{RadioState.RADIO_UP, RadioState.RADIO_DOWN, RadioState.RADIO_UP},
                new boolean[]{true, false, false}, new int[]{0, DNS_EXPIRE_SHIFT, DNS_EXPIRE_SHIFT});
    }

    /**
     * 1. Set aggressiveness level to 3. Set ttl to 300s.
     * 2. Send dns request with radio up and screen on. Request should be resolved from network.
     * 3. Move time 305s forward.
     * 4. Send dns request with radio up and screen on. Request should be resolved from cache.
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_008_DnsCeExpiration_3_ScreenOnRadioUp() throws Throwable {
        checkAggressiveDnsExpiration(hosts[7], AGGRESSIVENESS_LEVEL_3,
                new ScreenState[]{ScreenState.SCREEN_ON, ScreenState.SCREEN_ON},
                new RadioState[]{RadioState.RADIO_UP, RadioState.RADIO_UP},
                new boolean[]{true, false}, new int[]{0, DNS_EXPIRE_SHIFT});

    }

    /**
     * 1. Set aggressiveness level to 3. Set ttl to 300s.
     * 2. Send dns request with radio up and screen on. Request should be resolved from network.
     * 3. Move time 305s forward.
     * 4. Send dns request with radio down and screen on. Request should be resolved from cache.
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_009_DnsCeExpiration_3_ScreenOnRadioDown() throws Throwable {
        checkAggressiveDnsExpiration(hosts[8], AGGRESSIVENESS_LEVEL_3,
                new ScreenState[]{ScreenState.SCREEN_ON, ScreenState.SCREEN_ON},
                new RadioState[]{RadioState.RADIO_UP, RadioState.RADIO_DOWN},
                new boolean[]{true, false}, new int[]{0, DNS_EXPIRE_SHIFT});
    }

    /**
     * 1. Set aggressiveness level to 3. Set ttl to 300s.
     * 2. Send dns request with radio up and screen off. Request should be resolved from network.
     * 3. Move time 305s forward.
     * 4. Send dns request with radio down and screen off. Request should be resolved from cache.
     * 3. Move time 305s forward.
     * 4. Send dns request with radio up and screen off. Request should be resolved from cache.
     *
     * @throws Throwable
     */
    @LargeTest
    public void test_010_DnsCeExpiration_3_ScreenOffRadioUp() throws Throwable {
        checkAggressiveDnsExpiration(hosts[9], AGGRESSIVENESS_LEVEL_3,
                new ScreenState[]{ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF, ScreenState.SCREEN_OFF},
                new RadioState[]{RadioState.RADIO_UP, RadioState.RADIO_DOWN, RadioState.RADIO_UP},
                new boolean[]{true, false, false}, new int[]{0, DNS_EXPIRE_SHIFT, DNS_EXPIRE_SHIFT});
    }

    public void test_099_FinishTests() throws Exception {
        switchRestartFailover(true);
    }


}

