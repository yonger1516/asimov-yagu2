package com.seven.asimov.it.tests.caching.aggressive.iwc;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.testcases.IWCTestCase;
import com.seven.asimov.it.utils.sa.SaRestUtil;
import org.junit.After;
import org.junit.Before;

@DeviceOnly
public class CheckAggressiveIWCTests extends IWCTestCase {

    private static final String IWC_0_RI_SCREEN_ON_RADIO_UP = "iwc_0_ri_screen_on_radio_up";
    private static final String IWC_0_RI_SCREEN_ON_RADIO_DOWN = "iwc_0_ri_screen_on_radio_down";
    private static final String IWC_0_RI_SCREEN_OFF_RADIO_UP = "iwc_0_ri_screen_off_radio_up";
    private static final String IWC_0_RI_SCREEN_OFF_RADIO_DOWN = "iwc_0_ri_screen_off_radio_down";

    private static final String IWC_0_LP_SCREEN_OFF_RADIO_DOWN = "iwc_0_lp_screen_off_radio_down";
    private static final String IWC_0_LP_SCREEN_OFF_RADIO_UP = "iwc_0_lp_screen_off_radio_up";
    private static final String IWC_0_LP_SCREEN_ON_RADIO_DOWN = "iwc_0_lp_screen_on_radio_down";
    private static final String IWC_0_LP_SCREEN_ON_RADIO_UP = "iwc_0_lp_screen_on_radio_up";

    private static final String IWC_1_RI_SCREEN_ON_RADIO_UP = "iwc_1_ri_screen_on_radio_up";
    private static final String IWC_1_RI_SCREEN_ON_RADIO_DOWN = "iwc_1_ri_screen_on_radio_down";
    private static final String IWC_1_RI_SCREEN_OFF_RADIO_UP = "iwc_1_ri_screen_off_radio_up";
    private static final String IWC_1_RI_SCREEN_OFF_RADIO_DOWN = "iwc_1_ri_screen_off_radio_down";

    private static final String IWC_1_LP_SCREEN_OFF_RADIO_DOWN = "iwc_1_lp_screen_off_radio_down";
    private static final String IWC_1_LP_SCREEN_OFF_RADIO_UP = "iwc_1_lp_screen_off_radio_up";
    private static final String IWC_1_LP_SCREEN_ON_RADIO_DOWN = "iwc_1_lp_screen_on_radio_down";
    private static final String IWC_1_LP_SCREEN_ON_RADIO_UP = "iwc_1_lp_screen_on_radio_up";

    private static final String IWC_2_RI_SCREEN_ON_RADIO_UP = "iwc_2_ri_screen_on_radio_up";
    private static final String IWC_2_RI_SCREEN_ON_RADIO_DOWN = "iwc_2_ri_screen_on_radio_down";
    private static final String IWC_2_RI_SCREEN_OFF_RADIO_UP = "iwc_2_ri_screen_off_radio_up";
    private static final String IWC_2_RI_SCREEN_OFF_RADIO_DOWN = "iwc_2_ri_screen_off_radio_down";

    private static final String IWC_2_LP_SCREEN_OFF_RADIO_DOWN = "iwc_2_lp_screen_off_radio_down";
    private static final String IWC_2_LP_SCREEN_OFF_RADIO_UP = "iwc_2_lp_screen_off_radio_up";
    private static final String IWC_2_LP_SCREEN_ON_RADIO_DOWN = "iwc_2_lp_screen_on_radio_down";
    private static final String IWC_2_LP_SCREEN_ON_RADIO_UP = "iwc_2_lp_screen_on_radio_up";

    private static final String IWC_3_RI_SCREEN_ON_RADIO_UP = "iwc_3_ri_screen_on_radio_up";
    private static final String IWC_3_RI_SCREEN_ON_RADIO_DOWN = "iwc_3_ri_screen_on_radio_down";
    private static final String IWC_3_RI_SCREEN_OFF_RADIO_UP = "iwc_3_ri_screen_off_radio_up";
    private static final String IWC_3_RI_SCREEN_OFF_RADIO_DOWN = "iwc_3_ri_screen_off_radio_down";

    private static final String IWC_3_LP_SCREEN_OFF_RADIO_DOWN = "iwc_3_lp_screen_off_radio_down";
    private static final String IWC_3_LP_SCREEN_OFF_RADIO_UP = "iwc_3_lp_screen_off_radio_up";
    private static final String IWC_3_LP_SCREEN_ON_RADIO_DOWN = "iwc_3_lp_screen_on_radio_down";
    private static final String IWC_3_LP_SCREEN_ON_RADIO_UP = "iwc_3_lp_screen_on_radio_up";


    @Before
    public void setUp() throws Exception {
        SaRestUtil.init();
    }

    @After
    public void tearDown(){
        SaRestUtil.close();
    }

    /**
     * <p>For RI after receiving INVALIDATED_W_CACHE OC should immediately retrieved cache data in such cases:
     * connection to Relay is closed,cache_invalidate_aggressiveness=0, screen ON and radio UP.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 4 requests, and
     * another one for rest 1 requests</p>
     * <p>Pattern [0,65,65,65,65]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 3rd response polling should start.</li>
     * <li>4th request should be HITed.</li>
     * <li>Connection to Relay should be closed, radio state should be UP due to actively browsing.</li>
     * <li>Before 5th request came to client INVALIDATED_W_CACHE should be received from server via SMS due to connection to Relay closed.</li>
     * <li>After receiving  INVALIDATED_W_CACHE, OC should immediately retrieved cache data due to screen ON and, radio UP.</li>
     * <li>5th request should be HITed with received cache.</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_0_RiScreenOnRadioUp() throws Throwable {
        checkAggressiveIWC(IWC_0_RI_SCREEN_ON_RADIO_UP, AGGRESSIVENESS_LEVEL_0,
                ScreenState.SCREEN_ON, RadioState.RADIO_UP, false, true);
    }

    /**
     * <p>For RI after receiving INVALIDATED_W_CACHE OC should immediately retrieved cache data after sending next
     * request by APP in such cases: connection to Relay is closed,
     * cache_invalidate_aggressiveness=0, screen ON, radio DOWN.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 4 requests,
     * and another one for rest 2 requests. </p>
     * <p>Pattern [0,65,65,65,65,65]</p>
     * <p>Create such policy: asimov@http@cache_invalidate_aggressiveness=0</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 3rd response polling should start.</li>
     * <li>4th request should be HITed.</li>
     * <li>Connection to Relay should be closed, radio state should be DOWN.</li>
     * <li>Before 5th request came to client INVALIDATED_W_CACHE should be received from server via SMS due to
     * connection to Relay closed. OC shouldnt retrieved cache data.</li>
     * <li>After 5th request is send, OC should immediately retrieved cache data due to screen ON and send request.</li>
     * <li>5th request should be HITed with received cache.</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_0_RiScreenOnRadioDown() throws Throwable {
        checkAggressiveIWC(IWC_0_RI_SCREEN_ON_RADIO_DOWN, AGGRESSIVENESS_LEVEL_0,
                ScreenState.SCREEN_ON, RadioState.RADIO_DOWN, false, true);
    }

    /**
     * <p>For RI after receiving INVALIDATED_W_CACHE OC should immediately retrieved cache data in such cases:
     * connection to Relay is closed,cache_invalidate_aggressiveness=0, screen OFF and radio UP.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 4 requests,
     * and another one for rest 1 requests.</p>
     * <p>Pattern [0,65,65,65,65]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 3rd response polling should start.</li>
     * <li>4th request should be HITed.</li>
     * <li>Connection to Relay should be closed, radio state should be UP due to actively browsing.</li>
     * <li>Before 5th request came to client INVALIDATED_W_CACHE received from server via SMS due to connection to Relay closed.</li>
     * <li>After receiving  INVALIDATED_W_CACHE, OC should immediately retrieved cache data due to screen OFF and, radio UP.</li>
     * <li>5th request should be HITed with received cache.</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_0_RiScreenOffRadioUp() throws Throwable {
        checkAggressiveIWC(IWC_0_RI_SCREEN_OFF_RADIO_UP, AGGRESSIVENESS_LEVEL_0,
                ScreenState.SCREEN_OFF, RadioState.RADIO_UP, false, true);
    }

    /**
     * <p>For RI after receiving INVALIDATED_W_CACHE OC should immediately retrieved cache data after sending next
     * request by APP in such cases: connection to Relay is closed, cache_invalidate_aggressiveness=0,
     * screen OFF, radio state DOWN.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 4 requests,
     * and another one for rest 2 requests. </p>
     * <p>Pattern [0,65,65,65,65,65]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 3rd response polling should start.</li>
     * <li>4th request should be HITed.</li>
     * <li>Connection to Relay should be closed, radio state should be DOWN. </li>
     * <li>Before 5th request came to client INVALIDATED_W_CACHE received from server via SMS
     * due to connection to Relay closed. OC shouldnt retrieved cache data.</li>
     * <li>After 5th request is send, OC should immediately retrieved cache data due to screen OFF and send request.</li>
     * <li>5th request should be HITed with received cache.</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_0_RiScreenOffRadioDown() throws Throwable {
        checkAggressiveIWC(IWC_0_RI_SCREEN_OFF_RADIO_DOWN, AGGRESSIVENESS_LEVEL_0,
                ScreenState.SCREEN_OFF, RadioState.RADIO_DOWN, false, true);
    }

    /**
     * <p>For LP after receiving INVALIDATED_W_CACHE OC should immediately retrieved cache data in such cases:
     * connection to Relay is closed, cache_invalidate_aggressiveness=0, screen ON and radio UP.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 3 requests,
     * and another one for rest 1 requests. </p>
     * <p>Pattern [0,100,100,100]</p>
     * <p>Delay [75,75,75,75]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 2rd response polling should start (with hash H1).  </li>
     * <li>3rd request should has verdict HIT. </li>
     * <li>Connection to Relay should be closed, radio state UP due to actively browsing.</li>
     * <li>Before 3th response came to client INVALIDATED_W_CACHE received from server via SMS.</li>
     * <li>After receiving  INVALIDATED_W_CACHE, OC should immediately retrieved cache data due to screen ON and radio UP.</li>
     * <li>3rd response should be HITed with received cache.</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_0_LpScreenOnRadioUp() throws Throwable {
        checkAggressiveIWC(IWC_0_LP_SCREEN_ON_RADIO_UP, AGGRESSIVENESS_LEVEL_0,
                ScreenState.SCREEN_ON, RadioState.RADIO_UP, true, true);
    }

    /**
     * <p>For LP after receiving INVALIDATED_W_CACHE OC should immediately retrieved cache data in such cases:
     * connection to Relay is closed, cache_invalidate_aggressiveness=0, screen ON and radio DOWN.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 3 requests,
     * and another one for rest 1 requests. </p>
     * <p>Pattern [0,100,100,100]</p>
     * <p>Delay [75,75,75,75]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 2nd response polling should start (with hash H1).  </li>
     * <li>3rd request should has verdict HIT. </li>
     * <li>Connection to Relay should be closed, radio state DOWN.</li>
     * <li>Before 3th response came to client INVALIDATED_W_CACHE received from server via SMS.</li>
     * <li>After receiving  INVALIDATED_W_CACHE, OC should immediately retrieved cache data due to screen ON and, radio DOWN.</li>
     * <li>3rd response should be HITed with received cache.</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_0_LpScreenOnRadioDown() throws Throwable {
        checkAggressiveIWC(IWC_0_LP_SCREEN_ON_RADIO_DOWN, AGGRESSIVENESS_LEVEL_0,
                ScreenState.SCREEN_ON, RadioState.RADIO_DOWN, true, true);
    }

    /**
     * <p>For LP after receiving INVALIDATED_W_CACHE OC should immediately retrieved cache data in such cases:
     * connection to Relay is closed, cache_invalidate_aggressiveness=0, screen OFF and radio UP.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 3 requests,
     * and another one for rest 1 requests. </p>
     * <p>Pattern [0,100,100,100]</p>
     * <p>Delay [75,75,75,75]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 2nd response polling should start (with hash H1).</li>
     * <li>3rd request should has verdict HIT. </li>
     * <li>Connection to Relay should be closed, radio state UP due to actively browsing.</li>
     * <li>Before 3th response came to client INVALIDATED_W_CACHE received from server via SMS.</li>
     * <li>After receiving  INVALIDATED_W_CACHE, OC should immediately retrieved cache data due to screen OFF and radio UP.</li>
     * <li>3rd response should be HITed with received cache.</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_0_LpScreenOffRadioUp() throws Throwable {
        checkAggressiveIWC(IWC_0_LP_SCREEN_OFF_RADIO_UP, AGGRESSIVENESS_LEVEL_0,
                ScreenState.SCREEN_OFF, RadioState.RADIO_UP, true, true);
    }

    /**
     * <p>For LP after receiving INVALIDATED_W_CACHE OC should immediately retrieved cache data in such cases:
     * connection to Relay is closed, cache_invalidate_aggressiveness=0, screen OFF and radio DOWN.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 3 requests,
     * and another one for rest 1 requests.</p>
     * <p>Pattern [0,100,100,100]</p>
     * <p>Delay [75,75,75,75]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 2nd response polling should start (with hash H1).  </li>
     * <li>3rd request should has verdict HIT. </li>
     * <li>Connection to Relay should be closed, radio state DOWN.</li>
     * <li>Before 3th response came to client INVALIDATED_W_CACHE received from server via SMS.</li>
     * <li>After receiving  INVALIDATED_W_CACHE, OC should immediately retrieved cache data due to screen OFF and, radio DOWN.</li>
     * <li>3rd response should be HITed with received cache.</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_0_LpScreenOffRadioDown() throws Throwable {
        checkAggressiveIWC(IWC_0_LP_SCREEN_OFF_RADIO_DOWN, AGGRESSIVENESS_LEVEL_0,
                ScreenState.SCREEN_OFF, RadioState.RADIO_DOWN, true, true);
    }

    // 1 - remote cache is retrieved on Screen ON either radio up OR application request

    /**
     * <p>For RI after receiving INVALIDATED_W_CACHE OC should immediately retrieved cache data when  radio state
     * changed from DOWN to UP in such cases: connection to Relay is closed, cache_invalidate_aggressiveness=1, screen ON.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 4 requests,
     * and another one for rest 1 requests. </p>
     * <p>Pattern [0,65,65,65,65]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 3rd response polling should start.</li>
     * <li>4th request should be HITed.</li>
     * <li>Connection to Relay should be closed, radio state should be DOWN.</li>
     * <li>Before 5th request came to client INVALIDATED_W_CACHE received from server via SMS due to connection to Relay closed.</li>
     * <li>After receiving  INVALIDATED_W_CACHE, OC shouldnt  retrieved cache data due to screen ON.</li>
     * <li>Radio state changed to UP due to actively browsing. OC should immediately  retrieved cache data due to screen ON.</li>
     * <li>5th request should be HITed with new cache.</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_1_RiScreenOnRadioUp() throws Throwable {
        checkAggressiveIWC(IWC_1_RI_SCREEN_ON_RADIO_UP, AGGRESSIVENESS_LEVEL_1,
                ScreenState.SCREEN_ON, RadioState.RADIO_UP, false, true);
    }

    /**
     * <p>For RI after receiving INVALIDATED_W_CACHE OC should immediately retrieved cache data after sending next
     * request by APP in such cases: connection to Relay is closed, cache_invalidate_aggressiveness=1,
     * screen ON, radio DOWN.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 4 requests,
     * and another one for rest 2 requests. </p>
     * <p>Pattern [0,65,65,65,65,65]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 3rd response polling should start.</li>
     * <li>4th request should be HITed.</li>
     * <li>Connection to Relay should be closed, radio state should be DOWN.</li>
     * <li>Before 5th request came to client INVALIDATED_W_CACHE received from server via SMS due to connection
     * to Relay closed. OC shouldnt retrieved cache data.</li>
     * <li>After 5th request is send, OC should immediately retrieved cache data due to screen ON and send request.</li>
     * <li>5th request should be HITed with received cache.</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_1_RiScreenOnRadioDown() throws Throwable {
        checkAggressiveIWC(IWC_1_RI_SCREEN_ON_RADIO_DOWN, AGGRESSIVENESS_LEVEL_1,
                ScreenState.SCREEN_ON, RadioState.RADIO_DOWN, false, true);
    }

    /**
     * <p>For RI after receiving INVALIDATED_W_CACHE OC shouldnt  retrieved cache data after sending next request by APP
     * and radio state changes from DOWN to UP in such cases: connection to Relay is closed,
     * cache_invalidate_aggressiveness=1, screen OFF.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 4 requests,
     * and another one for rest 2 requests.</p>
     * <p>Pattern [0,65,65,65,65,65]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 3rd response polling should start (with hash H1). </li>
     * <li>4th request should be HITed.</li>
     * <li>Connection to Relay should be closed, radio state should be DOWN.</li>
     * <li>Before 5th request came to client INVALIDATED_W_CACHE received from server via SMS due to connection to Relay closed.</li>
     * <li>During next time of  test OC shouldnt retrieved cache data due to screen OFF and
     * cache_invalidate_aggressiveness=1, despite of send CLQ and radio state changed to UP.</li>
     * <li>5th request should be HITed (with hash H1).</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_1_RiScreenOffRadioUp() throws Throwable {
        checkAggressiveIWC(IWC_1_RI_SCREEN_OFF_RADIO_UP, AGGRESSIVENESS_LEVEL_1,
                ScreenState.SCREEN_OFF, RadioState.RADIO_UP, false, false);
    }

    /**
     * <p></p>
     * <p></p>
     * <p></p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_1_RiScreenOffRadioDown() throws Throwable {
        checkAggressiveIWC(IWC_1_RI_SCREEN_OFF_RADIO_DOWN, AGGRESSIVENESS_LEVEL_1,
                ScreenState.SCREEN_OFF, RadioState.RADIO_DOWN, false, false);
    }

    /**
     * <p>For LP after receiving INVALIDATED_W_CACHE OC should immediately retrieved cache data in such cases:
     * connection to Relay is closed, cache_invalidate_aggressiveness=1, screen ON and radio state UP.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 3 requests,
     * and another one for rest 1 requests. </p>
     * <p>Pattern [0,100,100,100]</p>
     * <p>Delay [75,75,75,75]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 2nd response polling should start .</li>
     * <li>3rd request should has verdict HIT. </li>
     * <li>Connection to Relay should be closed, radio state UP due to actively browsing.</li>
     * <li>Before 3th response came to client INVALIDATED_W_CACHE received from server via SMS.</li>
     * <li>After receiving  INVALIDATED_W_CACHE, OC should immediately retrieved cache data due to screen ON and radio UP.</li>
     * <li>3rd response should be HITed with received cache.</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_1_LpScreenOnRadioUp() throws Throwable {
        //AIWC_10
        checkAggressiveIWC(IWC_1_LP_SCREEN_ON_RADIO_UP, AGGRESSIVENESS_LEVEL_1,
                ScreenState.SCREEN_ON, RadioState.RADIO_UP, true, true);
    }

    /**
     * <p>For LP after receiving INVALIDATED_W_CACHE OC should immediately retrieved cache data in such cases:
     * connection to Relay is closed, cache_invalidate_aggressiveness=1, screen ON and radio DOWN.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 3 requests,
     * and another one for rest 1 requests. </p>
     * <p>Pattern [0,100,100,100]</p>
     * <p>Delay [75,75,75,75]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 2nd response polling should start.</li>
     * <li>3rd request should has verdict HIT. </li>
     * <li>Connection to Relay should be closed, radio state DOWN.</li>
     * <li>Before 3th response came to client INVALIDATED_W_CACHE received from server via SMS.</li>
     * <li>After receiving  INVALIDATED_W_CACHE, OC should immediately retrieved cache data due to screen ON and, radio DOWN.</li>
     * <li>3rd response should be HITed with received cache.</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_1_LpScreenOnRadioDown() throws Throwable {
        //AIWC_09   , 12
        checkAggressiveIWC(IWC_1_LP_SCREEN_ON_RADIO_DOWN, AGGRESSIVENESS_LEVEL_1,
                ScreenState.SCREEN_ON, RadioState.RADIO_DOWN, true, true);
    }

    /**
     * <p>For LP after receiving INVALIDATED_W_CACHE OC shouldnt  retrieved cache data in such cases:
     * connection to Relay is closed, cache_invalidate_aggressiveness=1, screen OFF and radio state changed from DOWN to UP.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 3 requests,
     * and another one for rest 1 requests. </p>
     * <p>Pattern [0,100,100,100]</p>
     * <p>Delay [75,75,75,75]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 2nd response polling should start (with hash H1).  </li>
     * <li>3rd request should has verdict HIT. </li>
     * <li>Connection to Relay should be closed, radio state DOWN.</li>
     * <li>Before 3th response came to client INVALIDATED_W_CACHE received from server via SMS.</li>
     * <li>After receiving  INVALIDATED_W_CACHE, OC shouldnt retrieved cache data.</li>
     * <li>Radio state changed to UP due to executed command from terminal. OC shouldnt retrieved cache data.</li>
     * <li>3rd response should be HITed (with hash H1).</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_1_LpScreenOffRadioUp() throws Throwable {
        //AIWC_11
        checkAggressiveIWC(IWC_1_LP_SCREEN_OFF_RADIO_UP, AGGRESSIVENESS_LEVEL_1,
                ScreenState.SCREEN_OFF, RadioState.RADIO_UP, true, false);
    }

    /**
     * <p></p>
     * <p></p>
     * <p></p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_1_LpScreenOffRadioDown() throws Throwable {
        checkAggressiveIWC(IWC_1_LP_SCREEN_OFF_RADIO_DOWN, AGGRESSIVENESS_LEVEL_1,
                ScreenState.SCREEN_OFF, RadioState.RADIO_DOWN, true, false);
    }

    // 2 - remote cache is retrieved on Screen ON and radio up (app requests ignored)

    /**
     * <p>For RI after receiving INVALIDATED_W_CACHE OC should immediately retrieved cache data
     * In such cases: connection to Relay is closed, cache_invalidate_aggressiveness=2, screen ON and radio UP.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 4 requests,
     * and another one for rest 1 requests. </p>
     * <p>Pattern [0,65,65,65,65]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 3rd response polling should start.</li>
     * <li>4th request should be HITed.</li>
     * <li>Connection to Relay should be closed, radio state should be UP due to actively browsing.</li>
     * <li>Before 5th request came to client INVALIDATED_W_CACHE received from server via SMS due to connection to Relay closed.</li>
     * <li> After receiving  INVALIDATED_W_CACHE, OC should immediately retrieved cache data due to screen ON and, radio UP.</li>
     * <li>5th request should be HITed.</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_2_RiScreenOnRadioUp() throws Throwable {
        checkAggressiveIWC(IWC_2_RI_SCREEN_ON_RADIO_UP, AGGRESSIVENESS_LEVEL_2,
                ScreenState.SCREEN_ON, RadioState.RADIO_UP, false, true);
    }

    /**
     * <p>After receiving INVALIDATED_W_CACHE OC shouldnt  retrieved cache data after sending next request by APP
     * in such cases:connection to Relay is closed, cache_invalidate_aggressiveness=2,screen ON and radio DOWN.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 4 requests,
     * and another one for rest 1 requests. </p>
     * <p>Pattern [0,65,65,65,65]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 3rd response polling should start (with hash H1).</li>
     * <li>4th request should be HITed.</li>
     * <li>Connection to Relay should be closed, radio state should be DOWN.</li>
     * <li>Before 5th request came to client INVALIDATED_W_CACHE received from server via SMS due to connection to Relay closed.</li>
     * <li>OC shouldnt retrieved cache data due to screen ON, radio DOWN, despite CLQ.</li>
     * <li>5th request should be HITed (with hash H1).</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_2_RiScreenOnRadioDown() throws Throwable {
           checkAggressiveIWC(IWC_2_RI_SCREEN_ON_RADIO_DOWN, AGGRESSIVENESS_LEVEL_2,
                    ScreenState.SCREEN_ON, RadioState.RADIO_DOWN, false, false);
    }

    /**
     * <p>After receiving INVALIDATED_W_CACHE OC shouldnt  retrieved cache data after sending next request by APP in such cases:
     * Connection to Relay is closed, cache_invalidate_aggressiveness=2, screen OFF and radio changed from DOWN to UP.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 4 requests, and another one for rest 1 requests.</p>
     * <p>Pattern [0,65,65,65,65]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 3rd response polling should start (with hash H1). </li>
     * <li>4th request should be HITed.</li>
     * <li>Connection to Relay should be closed, radio state should be DOWN.</li>
     * <li>Before 5th request came to client INVALIDATED_W_CACHE received from server via SMS due to connection to Relay closed.</li>
     * <li>During next time of test OC shouldnt retrieved cache data due to screen OFF and cache_invalidate_aggressiveness=2,
     * despite of send CLQ and radio state changed to UP.</li>
     * <li>5th request should be HITed (with hash H1).</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_2_RiScreenOffRadioUp() throws Throwable {
        checkAggressiveIWC(IWC_2_RI_SCREEN_OFF_RADIO_UP, AGGRESSIVENESS_LEVEL_2,
                ScreenState.SCREEN_OFF, RadioState.RADIO_UP, false, false);
    }

    /**
     * <p></p>
     * <p></p>
     * <p></p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_2_RiScreenOffRadioDown() throws Throwable {
        checkAggressiveIWC(IWC_2_RI_SCREEN_OFF_RADIO_DOWN, AGGRESSIVENESS_LEVEL_2,
                ScreenState.SCREEN_OFF, RadioState.RADIO_DOWN, false, false);
    }

    /**
     * <p></p>
     * <p></p>
     * <p></p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_2_LpScreenOnRadioUp() throws Throwable {
        checkAggressiveIWC(IWC_2_LP_SCREEN_ON_RADIO_UP, AGGRESSIVENESS_LEVEL_2,
                ScreenState.SCREEN_ON, RadioState.RADIO_UP, true, true);
    }

    /**
     * <p></p>
     * <p></p>
     * <p></p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * </ol>
     *
     * @throws Throwable
     */
    public void testAggressiveIWC_2_LpScreenOnRadioDown() throws Throwable {
            checkAggressiveIWC(IWC_2_LP_SCREEN_ON_RADIO_DOWN, AGGRESSIVENESS_LEVEL_2,
                    ScreenState.SCREEN_ON, RadioState.RADIO_DOWN, true, false);
    }

    /**
     * <p></p>
     * <p></p>
     * <p></p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_2_LpScreenOffRadioUp() throws Throwable {
        checkAggressiveIWC(IWC_2_LP_SCREEN_OFF_RADIO_UP, AGGRESSIVENESS_LEVEL_2,
                ScreenState.SCREEN_OFF, RadioState.RADIO_UP, true, false);
    }

    /**
     * <p></p>
     * <p></p>
     * <p></p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_2_LpScreenOffRadioDown() throws Throwable {
        checkAggressiveIWC(IWC_2_LP_SCREEN_OFF_RADIO_DOWN, AGGRESSIVENESS_LEVEL_2,
                ScreenState.SCREEN_OFF, RadioState.RADIO_DOWN, true, false);
    }

    // 3 - completely ignore invalidates

    /**
     * <p>After receiving INVALIDATED_W_CACHE OC shouldnt  retrieved cache data after sending next request by APP in such cases:
     * connection to Relay is closed, cache_invalidate_aggressiveness=3, screen ON and radio changed from DOWN to UP.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 4 requests, and another one for rest 1 requests.</p>
     * <p>Pattern [0,65,65,65,65]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 3rd response polling should start (with hash H1). </li>
     * <li>4th request should be HITed.</li>
     * <li>Connection to Relay should be closed, radio state should be DOWN.</li>
     * <li>Before 5th request came to client INVALIDATED_W_CACHE received from server via SMS due to connection to Relay closed.</li>
     * <li>During next time of test OC shouldnt retrieved cache data due to cache_invalidate_aggressiveness=3, despite of send CLQ and radio state changed to UP, screen ON.</li>
     * <li>5th request should be HITed (with hash H1).</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_3_RiScreenOnRadioUp() throws Throwable {
        checkAggressiveIWC(IWC_3_RI_SCREEN_ON_RADIO_UP, AGGRESSIVENESS_LEVEL_3,
                ScreenState.SCREEN_ON, RadioState.RADIO_UP, false, false);
    }

    /**
     * <p></p>
     * <p></p>
     * <p></p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_3_RiScreenOnRadioDown() throws Throwable {
        checkAggressiveIWC(IWC_3_RI_SCREEN_ON_RADIO_DOWN, AGGRESSIVENESS_LEVEL_3,
                ScreenState.SCREEN_ON, RadioState.RADIO_DOWN, false, false);
    }

    /**
     * <p>After receiving INVALIDATED_W_CACHE OC shouldnt  retrieved cache data after sending next request by APP in such cases:
     * connection to Relay is closed, cache_invalidate_aggressiveness=3, screen OFF and radio changed from DOWN to UP.</p>
     * <p>A test resource is needed for this test case that returns the same responses for first 4 requests, and another one for rest 1 requests. </p>
     * <p>Pattern [0,65,65,65,65]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>After receiving of 3rd response polling should start (with hash H1)</li>
     * <li>4th request should be HITed.</li>
     * <li>Connection to Relay should be closed, radio state should be DOWN.  </li>
     * <li>Before 5th request came to client INVALIDATED_W_CACHE received from server via SMS due to connection to Relay closed.</li>
     * <li>During next time of  test OC shouldnt retrieved cache data due to cache_invalidate_aggressiveness=3, despite of send CLQ and radio state changed to UP, screen OFF.</li>
     * <li>5th request should be HITed (with hash H1).</li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_3_RiScreenOffRadioUp() throws Throwable {
        checkAggressiveIWC(IWC_3_RI_SCREEN_OFF_RADIO_UP, AGGRESSIVENESS_LEVEL_3,
                ScreenState.SCREEN_OFF, RadioState.RADIO_UP, false, false);
    }

    /**
     * <p></p>
     * <p></p>
     * <p></p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_3_RiScreenOffRadioDown() throws Throwable {
        checkAggressiveIWC(IWC_3_RI_SCREEN_OFF_RADIO_DOWN, AGGRESSIVENESS_LEVEL_3,
                ScreenState.SCREEN_OFF, RadioState.RADIO_DOWN, false, false);
    }

    /**
     * <p></p>
     * <p></p>
     * <p></p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_3_LpScreenOnRadioUp() throws Throwable {
        checkAggressiveIWC(IWC_3_LP_SCREEN_ON_RADIO_UP, AGGRESSIVENESS_LEVEL_3,
                ScreenState.SCREEN_ON, RadioState.RADIO_UP, true, false);
    }

    /**
     * <p></p>
     * <p></p>
     * <p></p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_3_LpScreenOnRadioDown() throws Throwable {
        checkAggressiveIWC(IWC_3_LP_SCREEN_ON_RADIO_DOWN, AGGRESSIVENESS_LEVEL_3,
                ScreenState.SCREEN_ON, RadioState.RADIO_DOWN, true, false);
    }

    /**
     * <p></p>
     * <p></p>
     * <p></p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_3_LpScreenOffRadioUp() throws Throwable {
        checkAggressiveIWC(IWC_3_LP_SCREEN_OFF_RADIO_UP, AGGRESSIVENESS_LEVEL_3,
                ScreenState.SCREEN_OFF, RadioState.RADIO_UP, true, false);
    }

    /**
     * <p></p>
     * <p></p>
     * <p></p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * </ol>
     *
     * @throws Throwable
     */
    @LargeTest
    public void testAggressiveIWC_3_LpScreenOffRadioDown() throws Throwable {
        checkAggressiveIWC(IWC_3_LP_SCREEN_OFF_RADIO_DOWN, AGGRESSIVENESS_LEVEL_3,
                ScreenState.SCREEN_OFF, RadioState.RADIO_DOWN, true, false);
    }
}
