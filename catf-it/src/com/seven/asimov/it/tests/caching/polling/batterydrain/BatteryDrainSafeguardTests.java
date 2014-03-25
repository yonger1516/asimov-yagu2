package com.seven.asimov.it.tests.caching.polling.batterydrain;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.annotation.DeviceOnly;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.pms.PMSUtil;

public class BatteryDrainSafeguardTests extends TcpDumpTestCase {

    private String rmpTtlPropertyId;
    private int initialHitDelay;

    /**
     * <p>Summary: OC client behavior in case IN socket on first step of delay increasing</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests and socket
     * should be closed in 13 sec after 7th  request</p>
     * <p>Pattern [0,15,15,15,15,15,15,15,15,15,15,15,15]</p>
     * <p>Delay [10,10,10,10,10,10,10,10,10,10,10,10,10]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>RLP should be detected after 3-rd request and polling should start after receiving response.</li>
     * <li>4th-6th requests should be HITed with hit delay = 10 sec.</li>
     * <li>In socket error should appear in 13sec after 7th request.</li>
     * <li>8th - 13th requests should be HITed with hit delay = 10 sec.</li>
     * </ol>
     *
     * @throws Exception
     */
    @DeviceOnly
    @LargeTest
    public void test_001_batterySafe() throws Exception {
        increaseRmpTtl();
        String uri = createTestResourceUri("test_battery_safeguard_001");
        changeResponseDelay(uri, 10);
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-EncodingTests", "identity").getRequest();
        try {
            HttpResponse response;
            for (int i = 1; i <= 14; i++) {
                if (i <= 3) {
                    response = checkMiss(request, i);
                } else if (i == 7) {
                    response = checkHit(request, i, -1, null, false, 13 * 1000);
                } else {
                    response = checkHit(request, i);
                    if (i == 4) {
                        setUpInitialDelay(response);
                    } else {
                        checkResponseDelayNotChanged(i, response);
                    }
                }
                if (i < 14) logSleeping(15 * 1000 - response.getDuration());
            }
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
            resetRmpTtl();
        }
    }

    /**
     * <p>Summary: OC client behavior in case IN socket on some step of delay increasing</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests and socket
     * should be closed in 35 sec after 10th ? 13th   requests.</p>
     * <p>Pattern [0,15,15,15,15,15,15,25,35,40,40,40,40]</p>
     * <p>Delay [10,10,10,10,10,10,10,10,10,10,10,10,10]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>RLP should be detected after 3-rd request and polling should start after receiving response.</li>
     * <li>4th-6th requests should be HITed with hit delay = 10 sec.</li>
     * <li>7th request should be HITed with hit delay = 20sec.</li>
     * <li>8th request should be HITed with hit delay = 30sec.</li>
     * <li>In socket error should appear in 35sec after 9th request.</li>
     * <li>10th - 12th requests should be HITed with hit delay = 30sec.</li>
     * </ol>
     *
     * @throws Exception
     */
    @DeviceOnly
    @LargeTest
    public void test_002_batterySafe() throws Exception {
        String uri = createTestResourceUri("test_battery_safeguard_002");
        changeResponseDelay(uri, 10);
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-EncodingTests", "identity").getRequest();
        try {
            increaseRmpTtl();
            logSleeping(10 * 1000);
            HttpResponse response;
            for (int i = 1; i <= 13; i++) {
                if (i <= 3) {
                    response = checkMiss(request, i);
                    logSleeping(15 * 1000 - response.getDuration());
                } else if (i <= 6) {
                    response = checkHit(request, i);
                    if (i == 4) {
                        setUpInitialDelay(response);
                    } else {
                        checkResponseDelayNotChanged(i, response);
                    }
                    logSleeping(15 * 1000 - response.getDuration());
                } else if (i == 7) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 10 * 1000);
                    logSleeping(25 * 1000 - response.getDuration());
                } else if (i == 8) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 20 * 1000);
                    logSleeping(35 * 1000 - response.getDuration());
                } else if (i == 9) {
                    response = checkHit(request, i, -1, null, false, 35 * 1000);
                    logSleeping(40 * 1000 - response.getDuration());
                } else {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 20 * 1000);
                    if (i < 13) logSleeping(40 * 1000 - response.getDuration());
                }
            }
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
            resetRmpTtl();
        }
    }

    /**
     * <p>Summary: OC client behavior in case two IN sockets in row</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests and socket
     * should be closed in 35 sec after 9th , in 25 sec after  10th  request.</p>
     * <p>Pattern [0,15,15,15,15,15,15,25,35,40,30,20,20,20]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>RLP should be detected after 3-rd request and polling should start after receiving response.</li>
     * <li>4th-6th requests should be HITed with hit delay = 10 sec.</li>
     * <li>7th request should be HITed with hit delay = 20sec.</li>
     * <li>8th request should be HITed with hit delay = 30sec.</li>
     * <li>In socket error should appear in 35sec after 9th request.</li>
     * <li>In socket error should appear in 25sec after 10th request.</li>
     * <li>Deactivating RLP patern</li>
     * <li>11th request should be MISSed and after response RLP should be detected</li>
     * <li>12th request should be HITed with hit delay = 30sec.</li>
     * </ol>
     *
     * @throws Exception
     */
    @DeviceOnly
    @LargeTest
    public void test_003_batterySafe() throws Exception {
        String uri = createTestResourceUri("test_battery_safeguard_003");
        changeResponseDelay(uri, 10);
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-EncodingTests", "identity").getRequest();
        try {
            increaseRmpTtl();
            logSleeping(10 * 1000);
            HttpResponse response;
            for (int i = 1; i <= 12; i++) {
                if (i <= 3) {
                    response = checkMiss(request, i);
                    logSleeping(15 * 1000 - response.getDuration());
                } else if (i <= 6) {
                    response = checkHit(request, i);
                    if (i == 4) {
                        setUpInitialDelay(response);
                    } else {
                        checkResponseDelayNotChanged(i, response);
                    }
                    logSleeping(15 * 1000 - response.getDuration());
                } else if (i == 7) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 10 * 1000);
                    logSleeping(25 * 1000 - response.getDuration());
                } else if (i == 8) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 20 * 1000);
                    logSleeping(35 * 1000 - response.getDuration());
                } else if (i == 9) {
                    response = checkHit(request, i, -1, null, false, 35 * 1000);
                    logSleeping(40 * 1000 - response.getDuration());
                } else if (i == 10) {
                    response = checkHit(request, i, -1, null, false, 25 * 1000);
                    logSleeping(30 * 1000 - response.getDuration());
                } else if (i == 11) {
                    response = checkMiss(request, i);
                    logSleeping(20 * 1000 - response.getDuration());
                } else if (i == 12) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 20 * 1000);
                }
            }
        } finally {
            resetRmpTtl();
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    /**
     * <p>OC client converts  RLP pattern to LP due to delay increasing</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests</p>
     * <p>Pattern [0,50,50,50,50,50,50,60,70,70]</p>
     * <p>Delay [45,45,45,45,45,45,45,45,65,65]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>RLP should be detected after 3-rd request and polling should start after receiving response.</li>
     * <li>4th-6th requests should be HITed with hit delay = 45 sec.</li>
     * <li>7th request should be HITed with hit delay = 55 sec.</li>
     * <li>8th request should be HITed with hit delay = 65 sec.</li>
     * <li>9th request should be MISSed, Long poll should be detected after it and polling should start after receiving response.</li>
     * <li>10th request should be HITed with hit delay = 65 sec.</li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_004_batterySafe() throws Exception {
        final String uri = createTestResourceUri("test_battery_safeguard_004");
        changeResponseDelay(uri, 45);
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-EncodingTests", "identity")
                .addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();
        try {
            increaseRmpTtl("190");
            HttpResponse response;
            for (int i = 1; i <= 10; i++) {
                if (i <= 3) {
                    response = checkMiss(request, i);
                    logSleeping(50 * 1000 - response.getDuration());
                } else if (i <= 6) {
                    response = checkHit(request, i);
                    if (i == 4) {
                        setUpInitialDelay(response);
                    } else {
                        checkResponseDelayNotChanged(i, response);
                    }
                    logSleeping(50 * 1000 - response.getDuration());
                } else if (i == 7) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 10 * 1000);
                    logSleeping(60 * 1000 - response.getDuration());
                } else if (i == 8) {
                    long start = System.currentTimeMillis();
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 20 * 1000);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                changeResponseDelay(uri, 65);
                            } catch (Exception ignored) {
                            }
                        }
                    }).start();
                    long end = System.currentTimeMillis();
                    logSleeping(70 * 1000 - (end - start));
                } else if (i == 9) {
                    response = checkMiss(request, i);
                    logSleeping(70 * 1000 - response.getDuration());
                } else {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 20 * 1000);
                }
            }
        } finally {
            resetRmpTtl();
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    /**
     * <p>OC client converts  RI pattern to RLP due to delay increasing</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests</p>
     * <p>Pattern [0,65,65,65,2,2,2,12,25,20]</p>
     * <p>Delay [10,10,10,10,10,10,10,10,10,10]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>RI should be detected after 3-rd request and polling should start after receiving response.</li>
     * <li>4th-6th requests should be HITed with hit delay = 0 sec.</li>
     * <li>7th request should be HITed with hit delay = 10 sec.</li>
     * <li>8th request should be HITed with hit delay = 20 sec.</li>
     * <li>9th request should be out of order, RLP should be detected after it and polling should start after receiving response.</li>
     * <li>10th request should be HITed with hit delay = 20 sec.</li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_005_batterySafe() throws Exception {
        String uri = createTestResourceUri("test_battery_safeguard_005");
        changeResponseDelay(uri, 10);
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-EncodingTests", "identity").getRequest();
        try {
            HttpResponse response;
            for (int i = 1; i <= 10; i++) {
                if (i <= 3) {
                    response = checkMiss(request, i);
                    logSleeping(65 * 1000 - response.getDuration());
                } else if (i <= 6) {
                    response = checkHit(request, i);
                    if (i == 4) {
                        setUpInitialDelay(response);
                    } else {
                        checkResponseDelayNotChanged(i, response);
                    }
                    logSleeping(2 * 1000 - response.getDuration());
                } else if (i == 7) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 10 * 1000);
                    logSleeping(12 * 1000 - response.getDuration());
                } else if (i == 8) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 20 * 1000);
                    logSleeping(25 * 1000 - response.getDuration());
                } else if (i == 9) {
                    response = checkMiss(request, i);
                    logSleeping(20 * 1000 - response.getDuration());
                } else {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 20 * 1000);
                }
            }
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    /**
     * <p>OC client converts  RMP pattern to RLP due to delay increasing</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests</p>
     * <p>Pattern [0,30,30,30,2,2,2,15,25,35,45,55,55]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>RMP should be detected after 3-rd request and polling should start after receiving response.</li>
     * <li>4th-6th requests should be HITed with hit delay = 0 sec.</li>
     * <li>7h request should be HITed with hit delay = 10 sec.</li>
     * <li>8th request should be HITed with hit delay = 20 sec.</li>
     * <li>9th request should be HITed with hit delay = 30 sec</li>
     * <li>10th request should be HITed with hit delay = 40 sec.</li>
     * <li>11th request should be HITed with hit delay = 50 sec.</li>
     * <li>12th request should be MISSed, RLP should be detected after it and polling should start after receiving response.</li>
     * <li>13th request should be HITed with hit delay = 50 sec.</li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_006_batterySafe() throws Exception {
        String uri = createTestResourceUri("test_battery_safeguard_006");
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-EncodingTests", "identity").getRequest();
        try {
            HttpResponse response;
            for (int i = 1; i <= 13; i++) {
                if (i <= 3) {
                    response = checkMiss(request, i);
                    logSleeping(30 * 1000 - response.getDuration());
                } else if (i <= 6) {
                    response = checkHit(request, i);
                    if (i == 4) {
                        setUpInitialDelay(response);
                    } else {
                        checkResponseDelayNotChanged(i, response);
                    }
                    logSleeping(2 * 1000 - response.getDuration());
                } else if (i == 7) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 10 * 1000);
                    logSleeping(15 * 1000 - response.getDuration());
                } else if (i == 8) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 20 * 1000);
                    logSleeping(25 * 1000 - response.getDuration());
                } else if (i == 9) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 30 * 1000);
                    logSleeping(35 * 1000 - response.getDuration());
                } else if (i == 10) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 40 * 1000);
                    logSleeping(45 * 1000 - response.getDuration());
                } else if (i == 11) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 50 * 1000);
                    logSleeping(55 * 1000 - response.getDuration());
                } else if (i == 12) {
                    response = checkMiss(request, i);
                    logSleeping(55 * 1000 - response.getDuration());
                } else {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 50 * 1000);
                }
            }
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    /**
     * <p>OC client converts  RI pattern to LP due to delay increasing</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests</p>
     * <p>Pattern [0,65,65,65,2,2,2,60,65,65,65,65,65,75,85,95]</p>
     * <p>Delay [10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>RI should be detected after 3-rd request and polling should start after receiving response.</li>
     * <li>4th-6th requests should be HITed with hit delay = 0 sec.</li>
     * <li>7th request should be HITed with hit delay = 10 sec.</li>
     * <li>8th request should be HITed with hit delay = 20 sec.</li>
     * <li>9th request should be HITed with hit delay = 30 sec</li>
     * <li>10th request should be HITed with hit delay = 40 sec</li>
     * <li>11th request should be HITed with hit delay = 50 sec</li>
     * <li>12th request should be HITed with hit delay = 60 sec</li>
     * <li>13th request should be HITed with hit delay = 70 sec</li>
     * <li>14th request should be HITed with hit delay = 80 sec</li>
     * <li>15th request should be HITed with hit delay = 90 sec, LP should be detected and polling should start.</li>
     * <li>16th request should be HITed with hit delay = 90 sec</li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_007_batterySafe() throws Exception {
        String uri = createTestResourceUri("test_battery_safeguard_007");
        changeResponseDelay(uri, 10);
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-EncodingTests", "identity").getRequest();
        try {
            HttpResponse response;
            for (int i = 1; i <= 16; i++) {
                if (i <= 3) {
                    response = checkMiss(request, i);
                    logSleeping(65 * 1000 - response.getDuration());
                } else if (i <= 6) {
                    response = checkHit(request, i);
                    if (i == 4) {
                        setUpInitialDelay(response);
                    } else {
                        checkResponseDelayNotChanged(i, response);
                    }
                    logSleeping(2 * 1000 - response.getDuration());
                } else if (i == 7) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 10 * 1000);
                    logSleeping(60 * 1000 - response.getDuration());
                } else if (i >= 8 && i <= 12) {
                    response = checkHit(request, i);
                    switch (i) {
                        case 8:
                            checkResponseDelayChanged(i, response, 20 * 1000);
                            break;
                        case 9:
                            checkResponseDelayChanged(i, response, 30 * 1000);
                            break;
                        case 10:
                            checkResponseDelayChanged(i, response, 40 * 1000);
                            break;
                        case 11:
                            checkResponseDelayChanged(i, response, 50 * 1000);
                            break;
                        case 12:
                            checkResponseDelayChanged(i, response, 60 * 1000);
                            break;
                    }
                    logSleeping(65 * 1000 - response.getDuration());
                } else if (i == 13) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 70 * 1000);
                    logSleeping(75 * 1000 - response.getDuration());
                } else if (i == 14) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 80 * 1000);
                    logSleeping(85 * 1000 - response.getDuration());
                } else if (i == 15) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 90 * 1000);
                    logSleeping(95 * 1000 - response.getDuration());
                } else {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 90 * 1000);
                }
            }
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    /**
     * <p>OC client detects delay increasing applicability for RMP in case average IT for first 3 HITed requests  less then 15sec</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests</p>
     * <p>Pattern [0,30,30,30,2,2,30,40]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>RMP should be detected after 3-rd request and polling should start after receiving response.</li>
     * <li>4th-6th requests should be HITed with hit delay = 0 sec.</li>
     * <li>7th request should be HITed with hit delay = 10 sec.</li>
     * <li>8th request should be HITed with hit delay = 20 sec.</li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_008_batterySafe() throws Exception {
        String uri = createTestResourceUri("test_battery_safeguard_008");
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-EncodingTests", "identity").getRequest();
        try {
            HttpResponse response;
            for (int i = 1; i <= 8; i++) {
                if (i <= 3) {
                    response = checkMiss(request, i);
                    logSleeping(30 * 1000 - response.getDuration());
                } else if (i == 4) {
                    response = checkHit(request, i);
                    setUpInitialDelay(response);
                    logSleeping(2 * 1000 - response.getDuration());
                } else if (i == 5) {
                    response = checkHit(request, i);
                    checkResponseDelayNotChanged(i, response);
                    logSleeping(2 * 1000 - response.getDuration());
                } else if (i == 6) {
                    response = checkHit(request, i);
                    checkResponseDelayNotChanged(i, response);
                    logSleeping(30 * 1000 - response.getDuration());
                } else if (i == 7) {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 10 * 1000);
                    logSleeping(40 * 1000 - response.getDuration());
                } else {
                    response = checkHit(request, i);
                    checkResponseDelayChanged(i, response, 20 * 1000);
                }
            }
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    /**
     * <p>OC client doesnt detect delay increasing applicability for RI in case average IT for first 3 HITed requests
     * greater then 15sec</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests</p>
     * <p>Pattern [0,61,61,61,5,61,61]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>RI should be detected after 3-rd request and polling should start after receiving response.</li>
     * <li>4th-7th requests should be HITed with hit delay = 0 sec.</li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_009_batterySafe() throws Exception {
        String uri = createTestResourceUri("test_battery_safeguard_009");
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-EncodingTests", "identity").getRequest();
        try {
            HttpResponse response;
            for (int i = 1; i <= 7; i++) {
                if (i <= 3) {
                    response = checkMiss(request, i);
                    logSleeping(60 * 1000 - response.getDuration());
                } else if (i == 4) {
                    response = checkHit(request, i);
                    setUpInitialDelay(response);
                    logSleeping(5 * 1000 - response.getDuration());
                } else {
                    response = checkHit(request, i);
                    checkResponseDelayNotChanged(i, response);
                    if (i < 7) logSleeping(60 * 1000 - response.getDuration());
                }
            }
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    /**
     * <p>OC client doesnt detect delay increasing applicability for RMP in case average IT for first 3 HITed requests greater then 15sec</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests</p>
     * <p>Pattern [0,30,30,16,16,16,16]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>RMP should be detected after 3-rd request and polling should start after receiving response.</li>
     * <li>4th-7th requests should be HITed with hit delay = 0 sec.</li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_010_batterySafe() throws Exception {
        String uri = createTestResourceUri("test_battery_safeguard_010");
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-EncodingTests", "identity").getRequest();
        try {
            HttpResponse response;
            for (int i = 1; i <= 7; i++) {
                if (i <= 2) {
                    response = checkMiss(request, i);
                    logSleeping(30 * 1000 - response.getDuration());
                } else if (i == 3) {
                    response = checkMiss(request, i);
                    logSleeping(16 * 1000 - response.getDuration());
                } else {
                    response = checkHit(request, i);
                    if (i == 4) {
                        setUpInitialDelay(response);
                    } else {
                        checkResponseDelayNotChanged(i, response);
                    }
                    if (i < 7) logSleeping(16 * 1000 - response.getDuration());
                }
            }
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }

    /**
     * <p>OC client detects delay increasing applicability for RMP in case average IT for some 3 HITed requests less then 15sec</p>
     * <p>A test resource is needed for this test case that returns the same response for all requests</p>
     * <p>Pattern [0,30,30,30,30,30,14,14,14]</p>
     * <p>The test is implemented as follows:</p>
     * <ol>
     * <li>RMP should be detected after 3-rd request and polling should start after receiving response.</li>
     * <li>4th-8th requests should be HITed with hit delay = 0 sec.</li>
     * <li>9th request should be HITed with hit delay = 10 sec.</li>
     * </ol>
     *
     * @throws Exception
     */
    @LargeTest
    public void test_011_batterySafe() throws Exception {
        String uri = createTestResourceUri("test_battery_safeguard_011");
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-EncodingTests", "identity").getRequest();
        try {
            HttpResponse response;
            for (int i = 1; i <= 9; i++) {
                if (i <= 3) {
                    response = checkMiss(request, i);
                    logSleeping(30 * 1000 - response.getDuration());
                } else if (i == 4) {
                    response = checkHit(request, i);
                    setUpInitialDelay(response);
                    logSleeping(30 * 1000 - response.getDuration());
                } else if (i == 5) {
                    response = checkHit(request, i);
                    checkResponseDelayNotChanged(i, response);
                    logSleeping(30 * 1000 - response.getDuration());
                } else {
                    response = checkHit(request, i);
                    if (i < 9) {
                        checkResponseDelayNotChanged(i, response);
                        logSleeping(14 * 1000 - response.getDuration());
                    } else {
                        checkResponseDelayChanged(i, response, 10 * 1000);

                    }
                }
            }
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }


    private void changeResponseDelay(String uri, int delay) throws Exception {
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-EncodingTests", "identity")
                .addHeaderField("X-OC-ChangeSleep", Integer.toString(delay))
                .addHeaderField("X-OC-Stateless-Sleep", "true").getRequest();
        sendRequest(request, false, true);
    }

    private void increaseRmpTtl() {
        increaseRmpTtl("850");
    }

    private void increaseRmpTtl(String value) {
        rmpTtlPropertyId = PMSUtil.createPersonalScopeProperty(
                "rapid_polling_min_validity_period_in_seconds", "@asimov@http", value, true);
    }

    private void resetRmpTtl() {
        if (rmpTtlPropertyId == null) return;
        PMSUtil.deleteProperty(rmpTtlPropertyId);
    }

    private void checkResponseDelayChanged(int responseId, HttpResponse response, int diff) {
        int delta = 1000;
        int hitDelay = roundHitDelay(response);
        assertTrue("Response " + responseId + " delay should be changed" +
                "Expected : " + (initialHitDelay + diff) + " ,  but was : " + hitDelay,
                hitDelay >= (initialHitDelay + diff - delta) && hitDelay <= (initialHitDelay + diff + delta));
    }

    private void checkResponseDelayNotChanged(int responseId, HttpResponse response) {
        int delta = 1000;
        int hitDelay = roundHitDelay(response);
        assertTrue("Response " + responseId + " delay should be equal to initial hit delay" +
                "Expected : " + initialHitDelay + " ,  but was : " + hitDelay,
                hitDelay >= (initialHitDelay - delta) && hitDelay <= (initialHitDelay + delta));
    }

    private int roundHitDelay(HttpResponse response) {
        return ((int) response.getD() / 1000) * 1000;
    }

    private void setUpInitialDelay(HttpResponse response) {
        initialHitDelay = roundHitDelay(response);
    }

}