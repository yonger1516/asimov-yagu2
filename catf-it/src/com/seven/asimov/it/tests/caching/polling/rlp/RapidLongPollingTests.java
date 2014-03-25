package com.seven.asimov.it.tests.caching.polling.rlp;


import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.testcases.RlpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import org.apache.http.HttpStatus;

public class RapidLongPollingTests extends RlpTestCase {

    private static final String TAG = RapidLongPollingTests.class.getSimpleName();

    private final int DELTA_HIT_DELAY = 2 * 1000;

    private final String RESOURCE_URI_001 = "asimov_rapid_long_pol_001";
    private final String RESOURCE_URI_002 = "asimov_rapid_long_pol_002";
    private final String RESOURCE_URI_003 = "asimov_rapid_long_pol_003";
    private final String RESOURCE_URI_004 = "asimov_rapid_long_pol_004";
    private final String RESOURCE_URI_006 = "asimov_rapid_long_pol_006";
    private final String RESOURCE_URI_007 = "asimov_rapid_long_pol_007";
    private final String RESOURCE_URI_008 = "asimov_rapid_long_pol_008";
    private final String RESOURCE_URI_009 = "asimov_rapid_long_pol_009";
    private final String RESOURCE_URI_010 = "asimov_rapid_long_pol_010";


    /**
     * <p>OC detect RLP with correct delay in case when  D of 3rd  request < Average D</p>
     * <p>Pattern: [0,25,25,25]</p>
     * <p>Delays of responses: [24,8,14,10]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>(MISS;CSD)</li>
     * <li>(MISS; CSD)</li>
     * <li>(MISS //Average D=16, Average IT=0 => LP 1, RLP detected//; CSA)</li>
     * <li>Start Poll</li>
     * <li>(HIT // (Average D)*0.8 < delay of 3rd  response =14 < Average D => hit delay = 16//; N/A)</li>
     * </ol>
     *
     * @throws Throwable
     */
    @MediumTest
    public void test_001_RapidLongPoll() throws Throwable {

        /*the section need to be configured*/

        final int[] RI = {25, 25, 25, 0};
        final int[] D = {24, 8, 14, 0}; // expected 24 sec (the largest delay)
        final int[] RESPONSE_TIMEOUT = {29, 13, 19, 30};

        final int expectedHitResponseNumber = 3;
        final String TEST_CASE_RESOURCE_URI = RESOURCE_URI_001;
        final HttpResponse[] RESPONSE = checkMissHitResponses(RI, D, RESPONSE_TIMEOUT, TEST_CASE_RESOURCE_URI, expectedHitResponseNumber);

        long averageD = 0;
        long averageIT = 0;
        for (int i = 0; i < expectedHitResponseNumber - 1; i++) {
            averageD += RESPONSE[i].getDuration();
            averageIT += RI[i] * 1000 - RESPONSE[i].getDuration();
        }
        averageD /= expectedHitResponseNumber - 1;
        averageIT /= expectedHitResponseNumber - 1;

        long largestD = 0;
        for (int i = 0; i < expectedHitResponseNumber; i++) {
            if (RESPONSE[i].getDuration() > largestD) {
                largestD = RESPONSE[i].getDuration();
            }
        }
        final long hitResponseExpectedDelay = largestD;

        
        /*log information*/
        for (HttpResponse r : RESPONSE) {
            Log.i(TAG, "RESPONSE Duration = " + r.getDuration());
        }
        Log.i(TAG, "averageD (1,2) = " + averageD);
        Log.i(TAG, "averageIT (1,2) = " + averageIT);
        Log.i(TAG, "hitResponseExpectedDelay = " + hitResponseExpectedDelay);
        /*log information*/


        // this is needed to be sure, that exactly LP detected
        assertTrue("averageD (1,2) should be higher than averageIT (1,2), test-case was invalid", averageIT < averageD);
        assertTrue("averageD (1,2) should be higher than 7 seconds, test-case was invalid", 7 * 1000 < averageD);

        // check hit delay
        assertTrue("hit response delay: expected=" + hitResponseExpectedDelay + ", actual=" + RESPONSE[expectedHitResponseNumber].getDuration(),
                RESPONSE[expectedHitResponseNumber].getDuration() < (hitResponseExpectedDelay + DELTA_HIT_DELAY));
        assertTrue("hit response delay: expected=" + hitResponseExpectedDelay + ", actual=" + RESPONSE[expectedHitResponseNumber].getDuration(),
                RESPONSE[expectedHitResponseNumber].getDuration() > (hitResponseExpectedDelay - DELTA_HIT_DELAY));
        
        /*the section needs to be prepared*/
    }

    /**
     * <p>OC detect RLP with correct delay in case when  D of 3rd  request > Average D</p>
     * <p>A test resource is needed for this test case that returns the same responses for all requests</p>
     * <p>Pattern: [0,20,12,26]</p>
     * <p>Delays of responses: [19,11,17]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>(MISS;CSD)</li>
     * <li>(MISS; CSD)</li>
     * <li>(MISS //Average D=15, Average IT=0 => LP 1, RLP detected//; CSA)</li>
     * <li>Start Poll</li>
     * <li>(HIT //delay of 3rd response = 17 > Average D => hit delay = 17//; N/A)</li>
     * </ol>
     *
     * @throws Throwable
     */
    @MediumTest
    public void test_002_RapidLongPoll() throws Throwable {

        final int[] RI = {20, 12, 26, 0};
        final int[] D = {19, 11, 17, 0};   // 19 is the largest delay
        final int[] RESPONSE_TIMEOUT = {24, 16, 22, 30};
        final int expectedHitResponseNumber = 3;
        final String TEST_CASE_RESOURCE_URI = RESOURCE_URI_002;

        final HttpResponse[] RESPONSE = checkMissHitResponses(RI, D, RESPONSE_TIMEOUT, TEST_CASE_RESOURCE_URI, expectedHitResponseNumber);

        long averageD = 0;
        long averageIT = 0;
        for (int i = 0; i < expectedHitResponseNumber - 1; i++) {
            averageD += RESPONSE[i].getDuration();
            averageIT += RI[i] * 1000 - RESPONSE[i].getDuration();
        }
        averageD /= expectedHitResponseNumber - 1;
        averageIT /= expectedHitResponseNumber - 1;

        long largestD = 0;
        for (int i = 0; i < expectedHitResponseNumber; i++) {
            if (RESPONSE[i].getDuration() > largestD) {
                largestD = RESPONSE[i].getDuration();
            }
        }
        final long hitResponseExpectedDelay = largestD;

        
        /*log information*/
        for (HttpResponse r : RESPONSE) {
            Log.i(TAG, "RESPONSE Duration = " + r.getDuration());
        }
        Log.i(TAG, "averageD (1,2) = " + averageD);
        Log.i(TAG, "averageIt (1,2) = " + averageIT);
        Log.i(TAG, "hitResponseExpectedDelay = " + hitResponseExpectedDelay);
        /*log information*/


        // this is needed to be sure, that exactly LP detected
        assertTrue("averageD (1,2) should be higher than averageIT (1,2), test-case was invalid", averageIT < averageD);
        assertTrue("averageD (1,2) should be higher than 7 seconds, test-case was invalid", 7 * 1000 < averageD);

        // check hit delay
        assertTrue("hit response delay: expected=" + hitResponseExpectedDelay + ", actual=" + RESPONSE[expectedHitResponseNumber].getDuration(),
                RESPONSE[expectedHitResponseNumber].getDuration() < (hitResponseExpectedDelay + DELTA_HIT_DELAY));
        assertTrue("hit response delay: expected=" + hitResponseExpectedDelay + ", actual=" + RESPONSE[expectedHitResponseNumber].getDuration(),
                RESPONSE[expectedHitResponseNumber].getDuration() > (hitResponseExpectedDelay - DELTA_HIT_DELAY));
        
        /*the section needs to be prepared*/
    }

    /**
     * <p>OC re-detect RLP from LP in case when delay of response begins to decrease</p>
     * <p>A test resource is needed for this test case that returns the same responses for all requests</p>
     * <p>Pattern: [0,67,20,20,20]</p>
     * <p>Delays of responses: [65,15,15,15]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>(MISS;CSD) construct RR for possible LP PM</li>
     * <li>(MISS; CSA) response delay 15 doesnt match the current pattern </li>
     * <li>(NRV;HITH)</li>
     * <li>(NRV //Average D=31, Average IT=0 => LP 1, RLP detected, delay of 3rd response = 15 = (Average D)for_RLP=15//; CSA)</li>
     * <li>Start Poll</li>
     * <li>(HIT // => hit delay =15//; N/A)</li>
     * </ol>
     *
     * @throws Throwable
     */
    @MediumTest
    public void test_003_RapidLongPoll() throws Throwable {

        final int[] RI = {67, 20, 20, 20, 0};
        final int[] D = {65, 15, 15, 15, 0};// expected 15 sec
        final int[] RESPONSE_TIMEOUT = {75, 20, 20, 20, 20};
        final int expectedHitResponseNumber = 4;
        final String TEST_CASE_RESOURCE_URI = RESOURCE_URI_003;

        final HttpResponse[] RESPONSE = checkMissHitResponses(RI, D, RESPONSE_TIMEOUT, TEST_CASE_RESOURCE_URI, expectedHitResponseNumber);

        long averageD = 0;
        long averageIT = 0;
        for (int i = 0; i < expectedHitResponseNumber - 1; i++) {
            averageD += RESPONSE[i].getDuration();
            averageIT += RI[i] * 1000 - RESPONSE[i].getDuration();
        }
        averageD /= expectedHitResponseNumber - 1;
        averageIT /= expectedHitResponseNumber - 1;

        long largestD = 0;
        for (int i = 1; i < expectedHitResponseNumber; i++) {
            if (RESPONSE[i].getDuration() > largestD) {
                largestD = RESPONSE[i].getDuration();
            }
        }
        final long hitResponseExpectedDelay = largestD;
        
        /*log information*/
        for (HttpResponse r : RESPONSE) {
            Log.i(TAG, "RESPONSE Duration = " + r.getDuration());
        }
        Log.i(TAG, "averageD (1,2,3) = " + averageD);
        Log.i(TAG, "averageIT (1,2,3) = " + averageIT);
        Log.i(TAG, "hitResponseExpectedDelay = " + hitResponseExpectedDelay);
        /*log information*/


        // this is needed to be sure, that exactly LP detected
        assertTrue("averageD (1,2,3) should be higher than averageIT (1,2,3), test-case was invalid", averageIT < averageD);
        assertTrue("averageD (1,2,3) should be higher than 7 seconds, test-case was invalid", 7 * 1000 < averageD);

        // check hit delay
        assertTrue("hit response delay: expected=" + hitResponseExpectedDelay + ", actual=" + RESPONSE[expectedHitResponseNumber].getDuration(),
                RESPONSE[expectedHitResponseNumber].getDuration() < (hitResponseExpectedDelay + DELTA_HIT_DELAY));
        assertTrue("hit response delay: expected=" + hitResponseExpectedDelay + ", actual=" + RESPONSE[expectedHitResponseNumber].getDuration(),
                RESPONSE[expectedHitResponseNumber].getDuration() > (hitResponseExpectedDelay - DELTA_HIT_DELAY));
        
        /*the section needs to be prepared*/
    }

    /**
     * <p>OC re-detect RLP from LP in case when delay of response = 0</p>
     * <p>A test resource is needed for this test case that returns the same responses for all requests</p>
     * <p>Pattern: [0,67,20,20,20]</p>
     * <p>Delays of responses: [65,0,0,0,0]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>(MISS;CSD) construct RR for possible LP PM</li>
     * <li>(MISS; CSA) response delay 0 doesnt match the current pattern </li>
     * <li>(NRV;HITH)</li>
     * <li>(NRV //Average D=21, Average IT=14 => LP 1, RLP detected, delay of 3rd response = 0 = (Average D)for_RLP =0//; CSA)</li>
     * <li>Start Poll</li>
     * <li>(HIT // => hit delay =0//; N/A)</li>
     * </ol>
     *
     * @throws Throwable
     */
    @SmallTest
    public void test_004_RapidLongPoll() throws Throwable {

        final int[] RI = {67, 20, 20, 20, 0};
        final int[] D = {65, 0, 0, 0, 0};   // expected 0 sec (upgrading RMP to RLP)
        final int[] RESPONSE_TIMEOUT = {75, 10, 10, 10, 75};
        final int expectedHitResponseNumber = 4;
        final String TEST_CASE_RESOURCE_URI = RESOURCE_URI_004;

        final HttpResponse[] RESPONSE = checkMissHitResponses(RI, D, RESPONSE_TIMEOUT, TEST_CASE_RESOURCE_URI, expectedHitResponseNumber);

        long averageD = 0;
        long averageIT = 0;
        for (int i = 0; i < expectedHitResponseNumber - 1; i++) {
            averageD += RESPONSE[i].getDuration();
            averageIT += RI[i] * 1000 - RESPONSE[i].getDuration();
        }
        averageD /= expectedHitResponseNumber - 1;
        averageIT /= expectedHitResponseNumber - 1;

        final long hitResponseExpectedDelay = RESPONSE[0].getDuration();

        
        /*log information*/
        for (HttpResponse r : RESPONSE) {
            Log.i(TAG, "RESPONSE Duration = " + r.getDuration());
        }
        Log.i(TAG, "averageD = " + averageD);
        Log.i(TAG, "averageIT = " + averageIT);
        Log.i(TAG, "hitResponseExpectedDelay = " + hitResponseExpectedDelay);
        /*log information*/

        // check hit delay
        assertTrue("hit response delay: expected=" + hitResponseExpectedDelay + ", actual=" + RESPONSE[expectedHitResponseNumber].getDuration(),
                RESPONSE[expectedHitResponseNumber].getDuration() < (hitResponseExpectedDelay + DELTA_HIT_DELAY));
        assertTrue("hit response delay: expected=" + hitResponseExpectedDelay + ", actual=" + RESPONSE[expectedHitResponseNumber].getDuration(),
                RESPONSE[expectedHitResponseNumber].getDuration() > (hitResponseExpectedDelay - DELTA_HIT_DELAY));
        
        /*the section needs to be prepared*/
    }

    /**
     * <p>OC detect RLP with correct delay in case when  D of 3rd  request = Average D</p>
     * <p>A test resource is needed for this test case that returns the same responses for all requests</p>
     * <p>Pattern: [0,20,20,20]</p>
     * <p>Delays of responses: [15,15,15,15]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>(MISS;CSD)</li>
     * <li>(MISS; CSD)</li>
     * <li>(MISS //Average D=15, Average IT=5 => LP 1, RLP detected//; CSA)</li>
     * <li>Start Poll</li>
     * <li>(HIT //delay of 3rd response = 15 = Average D => hit delay = 15//; N/A)</li>
     * </ol>
     *
     * @throws Throwable
     */
    @MediumTest
    public void test_006_RapidLongPoll() throws Throwable {

        final int[] RI = {20, 20, 20, 0};
        final int[] D = {15, 15, 15, 0}; // expected 15 sec
        final int[] RESPONSE_TIMEOUT = {25, 25, 25, 25};
        final int expectedHitResponseNumber = 3;
        final String TEST_CASE_RESOURCE_URI = RESOURCE_URI_006;

        final HttpResponse[] RESPONSE = checkMissHitResponses(RI, D, RESPONSE_TIMEOUT, TEST_CASE_RESOURCE_URI, expectedHitResponseNumber);

        long averageD = 0;
        long averageIT = 0;
        for (int i = 0; i < expectedHitResponseNumber - 1; i++) {
            averageD += RESPONSE[i].getDuration();
            averageIT += RI[i] * 1000 - RESPONSE[i].getDuration();
        }
        averageD /= expectedHitResponseNumber - 1;
        averageIT /= expectedHitResponseNumber - 1;

        long largestD = 0;
        for (int i = 0; i < expectedHitResponseNumber; i++) {
            if (RESPONSE[i].getDuration() > largestD) {
                largestD = RESPONSE[i].getDuration();
            }
        }
        final long hitResponseExpectedDelay = largestD;
        
        /*log information*/
        for (HttpResponse r : RESPONSE) {
            Log.i(TAG, "RESPONSE Duration = " + r.getDuration());
        }
        Log.i(TAG, "averageD (1,2) = " + averageD);
        Log.i(TAG, "averageIT (1,2) = " + averageIT);
        Log.i(TAG, "hitResponseExpectedDelay = " + hitResponseExpectedDelay);
        /*log information*/


        // this is needed to be sure, that exactly RLP detected
        assertTrue("averageD (1,2) should be higher than averageIT (1,2), test-case was invalid", averageIT < averageD);
        assertTrue("averageD (1,2) should be higher than 7 seconds, test-case was invalid", 7 * 1000 < averageD);

        // check hit delay
        assertTrue("hit response delay: expected=" + hitResponseExpectedDelay + ", actual=" + RESPONSE[expectedHitResponseNumber].getDuration(),
                RESPONSE[expectedHitResponseNumber].getDuration() < (hitResponseExpectedDelay + DELTA_HIT_DELAY));
        assertTrue("hit response delay: expected=" + hitResponseExpectedDelay + ", actual=" + RESPONSE[expectedHitResponseNumber].getDuration(),
                RESPONSE[expectedHitResponseNumber].getDuration() > (hitResponseExpectedDelay - DELTA_HIT_DELAY));
        
        /*the section needs to be prepared*/
    }

    /**
     * <p>OC correctly detect LP</p>
     * <p>A test resource is needed for this test case that returns the same responses for all requests</p>
     * <p>Pattern: [0,20,20,65,65]</p>
     * <p>Delays of responses: [15,15,64,64,64]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>(MISS;CSD)</li>
     * <li>(MISS; CSD)</li>
     * <li>(MISS //Average D=15, Average IT=0 => LP 1,  RLP detected//; CSA)  response delay 64 doesnt match the current pattern </li>
     * <li>(MISS //Polling class: 4 detected;CSD)</li>
     * <li>Start Poll</li>
     * <li>(HIT // hit delay = 64//; N/A)</li>
     * </ol>
     *
     * @throws Throwable
     */
    @MediumTest
    public void test_007_RapidLongPoll() throws Throwable {

        final int[] RI = {20, 20, 65, 65, 0};
        final int[] D = {15, 15, 64, 64, 64}; // expected 64 (LP)
        final int[] RESPONSE_TIMEOUT = {25, 25, 75, 75, 75};
        final int expectedHitResponseNumber = 4;

        final String TEST_CASE_RESOURCE_URI = RESOURCE_URI_007;

        final HttpResponse[] RESPONSE = checkMissHitResponses(RI, D, RESPONSE_TIMEOUT, TEST_CASE_RESOURCE_URI, expectedHitResponseNumber);

        final long hitResponseExpectedDelay = RESPONSE[expectedHitResponseNumber - 1].getDuration();

        
        /*log information*/
        for (HttpResponse r : RESPONSE) {
            Log.i(TAG, "RESPONSE Duration = " + r.getDuration());
        }
        Log.i(TAG, "hitResponseExpectedDelay = " + hitResponseExpectedDelay);
        /*log information*/

        // check hit delay
        assertTrue("hit response delay: expected=" + hitResponseExpectedDelay + ", actual=" + RESPONSE[expectedHitResponseNumber].getDuration(),
                RESPONSE[expectedHitResponseNumber].getDuration() < (hitResponseExpectedDelay + DELTA_HIT_DELAY));
        assertTrue("hit response delay: expected=" + hitResponseExpectedDelay + ", actual=" + RESPONSE[expectedHitResponseNumber].getDuration(),
                RESPONSE[expectedHitResponseNumber].getDuration() > (hitResponseExpectedDelay - DELTA_HIT_DELAY));
        
        /*the section needs to be prepared*/
    }

    /**
     * <p>OC correctly detect Polling class 3</p>
     * <p>A test resource is needed for this test case that returns the same responses for all requests</p>
     * <p>Pattern: [0,65,65,65]</p>
     * <p>Delays of responses: [15,15,15,15]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>(MISS;CSD)</li>
     * <li>(MISS; CSD)</li>
     * <li>(MISS //Average D=15, Average IT=50 => LP 0,  Polling class: 3 detected//; CSA)</li>
     * <li>Start Poll</li>
     * <li>(HIT // hit delay = 0//; N/A)</li>
     * </ol>
     *
     * @throws Throwable
     */
    @MediumTest
    public void test_008_RapidLongPoll() throws Throwable {

        final int[] RI = {65, 65, 65, 0};
        final int[] D = {15, 15, 15, 15}; // expected 0 (RI based pattern)
        final int[] RESPONSE_TIMEOUT = {25, 25, 25, 25};
        int expectedHitResponseNumber = 3;
        final String TEST_CASE_RESOURCE_URI = RESOURCE_URI_008;

        final HttpResponse[] RESPONSE = checkMissHitResponses(RI, D, RESPONSE_TIMEOUT, TEST_CASE_RESOURCE_URI, expectedHitResponseNumber);

        long averageD = 0;
        long averageIT = 0;
        for (int i = 0; i < expectedHitResponseNumber - 1; i++) {
            averageD += RESPONSE[i].getDuration();
            averageIT += RI[i] * 1000 - RESPONSE[i].getDuration();
        }
        averageD /= expectedHitResponseNumber - 1;
        averageIT /= expectedHitResponseNumber - 1;

        final long hitResponseExpectedDelay = 0;

        
        /*log information*/
        for (HttpResponse r : RESPONSE) {
            Log.i(TAG, "RESPONSE Duration = " + r.getDuration());
        }
        Log.i(TAG, "averageD = " + averageD);
        Log.i(TAG, "averageIT = " + averageIT);
        Log.i(TAG, "hitResponseExpectedDelay = " + hitResponseExpectedDelay);
        /*log information*/

        assertTrue("averageIT should be higher than averageD, test-case was invalid", averageIT > averageD);
        assertTrue("averageD should be higher than 7 seconds, test-case was invalid", 7 * 1000 < averageD);

        // check hit delay
        assertTrue("hit response delay: expected=" + hitResponseExpectedDelay + ", actual=" + RESPONSE[expectedHitResponseNumber].getDuration(),
                RESPONSE[expectedHitResponseNumber].getDuration() < (hitResponseExpectedDelay + DELTA_HIT_DELAY));
        assertTrue("hit response delay: expected=" + hitResponseExpectedDelay + ", actual=" + RESPONSE[expectedHitResponseNumber].getDuration(),
                RESPONSE[expectedHitResponseNumber].getDuration() > (hitResponseExpectedDelay - DELTA_HIT_DELAY));
        
        /*the section needs to be prepared*/
    }

    /**
     * <p>OC correctly detect RMP</p>
     * <p>A test resource is needed for this test case that returns the same responses for all requests</p>
     * <p>Pattern: [0,35,35,35]</p>
     * <p>Delays of responses: [10,10,10,10]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>(MISS;CSD)</li>
     * <li>(MISS; CSD)</li>
     * <li>(MISS //Average D=10, Average IT=15 => LP 0, RMP detected//; CSA)</li>
     * <li>Start Poll</li>
     * <li>(HIT // hit delay = 0//; N/A)</li>
     * </ol>
     *
     * @throws Throwable
     */
    @MediumTest
    public void test_009_RapidLongPoll() throws Throwable {

        final int[] RI = {35, 35, 35, 0};
        final int[] D = {10, 10, 10, 10}; // expected 0 sec (RMP)
        final int[] RESPONSE_TIMEOUT = {20, 20, 20, 20};
        final int expectedHitResponseNumber = 3;
        final String TEST_CASE_RESOURCE_URI = RESOURCE_URI_009;

        final HttpResponse[] RESPONSE = checkMissHitResponses(RI, D, RESPONSE_TIMEOUT, TEST_CASE_RESOURCE_URI, expectedHitResponseNumber);

        long averageD = 0;
        long averageIT = 0;
        for (int i = 0; i < expectedHitResponseNumber - 1; i++) {
            averageD += RESPONSE[i].getDuration();
            averageIT += RI[i] * 1000 - RESPONSE[i].getDuration();
        }
        averageD /= expectedHitResponseNumber - 1;
        averageIT /= expectedHitResponseNumber - 1;

        final long hitResponseExpectedDelay = 0;

        
        /*log information*/
        for (HttpResponse r : RESPONSE) {
            Log.i(TAG, "RESPONSE Duration = " + r.getDuration());
        }
        Log.i(TAG, "averageD = " + averageD);
        Log.i(TAG, "averageIT = " + averageIT);
        Log.i(TAG, "hitResponseExpectedDelay = " + hitResponseExpectedDelay);
        /*log information*/

        assertTrue("averageIT should be higher than averageD, test-case was invalid", averageIT > averageD);
        assertTrue("averageD should be higher than 7 seconds, test-case was invalid", 7 * 1000 < averageD);

        // check hit delay
        assertTrue("hit response delay: expected=" + hitResponseExpectedDelay + ", actual=" + RESPONSE[expectedHitResponseNumber].getDuration(),
                RESPONSE[expectedHitResponseNumber].getDuration() < (hitResponseExpectedDelay + DELTA_HIT_DELAY));
        assertTrue("hit response delay: expected=" + hitResponseExpectedDelay + ", actual=" + RESPONSE[expectedHitResponseNumber].getDuration(),
                RESPONSE[expectedHitResponseNumber].getDuration() > (hitResponseExpectedDelay - DELTA_HIT_DELAY));
        
        /*the section needs to be prepared*/
    }


    /**
     * <p>If there are more than 2 error trx in a row for RLP, OC should deactivate RR</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests and socket should be closed in 20 sec after 4th and 5th  requests</p>
     * <p>Pattern [0,45,45,45,45,45]</p>
     * <p>Delay [30,30,30,30,30,30]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>1st - 3rd requests should be MISSed</li>
     * <li>4th request should be HITed</li>
     * <li>In socket error should appear in 20 sec after 5th  request</li>
     * <li>In socket error should appear in 20 sec after 6th  request</li>
     * <li>RR should be deactivated</li>
     * </ol>
     *
     * @throws Throwable
     */
    @MediumTest
    public void test_010_RapidLongPoll() throws Throwable {
        String uri = createTestResourceUri(RESOURCE_URI_010);
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-Encoding", "identity").getRequest();
        int requestId = 0;
        int sleepTime = 45 * 1000;
        try {
            PrepareResourceUtil.prepareResource(uri, false, 30);
            HttpResponse response;
            //R1-R3 miss
            for (int i = 0; i < 3; i++) {
                response = checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
                logSleeping(sleepTime - response.getDuration());
            }
            //R4 hit
            response = checkHit(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
            logSleeping(sleepTime - response.getDuration());
            //R5 verdict hit, but in socket
            response = checkHit(request, ++requestId, -1, null, false, 20 * 1000);
            logSleeping(sleepTime - response.getDuration());
            //R6 verdict hit, but in socket
            response = checkHit(request, ++requestId, -1, null, false, 20 * 1000);
            logSleeping(sleepTime - response.getDuration());
            //R7 miss because RR deactivated
            checkMiss(request, ++requestId, HttpStatus.SC_OK, VALID_RESPONSE);
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

}
