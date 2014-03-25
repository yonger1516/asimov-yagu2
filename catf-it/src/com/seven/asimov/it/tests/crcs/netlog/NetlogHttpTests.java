package com.seven.asimov.it.tests.crcs.netlog;

import android.test.suitebuilder.annotation.SmallTest;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.TimeInfoTransaction;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.testcases.NetlogHttpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.PunycodeUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.pms.PMSUtil;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.net.URLEncoder;

/**
 * <h2>This class tests OCC's Netlog functionality for http requests.</h2>
 */
public class NetlogHttpTests extends NetlogHttpTestCase {

    private static final String RESOURCE_URI1 = "asimov_it_ga_client_server_check_miss_simple";
    private static final String RESOURCE_URI2 = "asimov_it_ga_client_server_check_miss_big_size";
    private static final String RESOURCE_URI3 = "asimov_it_ga_client_server_check_hit_rfc";
    private static final String RESOURCE_URI4 = "asimov_it_ga_client_server_check_random_size";
    private static final String RESOURCE_URI5 = "asimov_it_ga_hosts_app_names_check";
    private static final String RESOURCE_URI6 = "asimov_it_ga_response_status_check_";
    private static final String RESOURCE_URI7 = "asimov_it_ga_response_time_check_";
    private static final String RESOURCE_URI8 = "asimov_it_ga_content_length_little_size";
    private static final String RESOURCE_URI9 = "asimov_it_ga_content_length_big_size";
    private static final String RESOURCE_URI10 = "asimov_it_ga_without_content_length";
    private static final String RESOURCE_URI11 = "asimov_it_ga_header_length_check";
    private static final String RESOURCE_URI12 = "asimov_it_ga_response_time_check_lp_";
    private static final String RESOURCE_URI13 = "asimov_it_ga_content_type_";
    private static final String RESOURCE_URI14 = "asimov_it_ga_protocol_stack";

    private static final String STATUS_CODE_FIELD_NAME = "StatusCode";
    private static final String STATUS_CODE_ERROR_MESSAGE = " StatusCode from netlog is incorrect : ";
    private static final String CONTENT_LENGTH_FIELD_NAME = "ContentLength";
    private static final String CONTENT_LENGTH_ERROR_MESSAGE = "Content length from netlog is incorrect : ";
    private static final String HEADER_LENGTH_FIELD_NAME = "HeaderLength";
    private static final String HEADER_LENGTH_ERROR_MESSAGE = "Header length from netlog is incorrect : ";
    private static final String RESPONSE_TIME_FIELD_NAME = "ResponseTime";
    private static final String RESPONSE_TIME_ERROR_MESSAGE = "Response time from netlog is incorrect : ";
    private static final String CONTENT_TYPE_FIELD_NAME = "ContentType";
    private static final String CONTENT_TYPE_ERROR_MESSAGE = "Content type from netlog is incorrect : ";

    private static final String LOCAL_PROTOCOL_STACK_FIELD_NAME = "LocalProtocolStack";
    private static final String LOCAL_PROTOCOL_STACK_ERROR_MESSAGE = "Local Protocol Stack from netlog is incorrect : ";

    private static final String[] contentTypes = new String[]{"image/gif", "text/html; charset=ISO-8859-4", "application/pdf"};
    private static final String[] contentTypesChecked = new String[]{"image/gif", "text/html", "application/pdf"};

    private static final String[] filed1 = new String[]{"Host", "ApplicationName", "AppStatus"};
    private static final String[] filed2Host1 = new String[]{AsimovTestCase.TEST_RESOURCE_HOST, "com.seven.asimov.it", "foreground"};
    private static final String[] filed2Host2 = new String[]{"xn--d1abbgf6aiiy.xn--p1ai", "com.seven.asimov.it", "foreground"};
    private static final String[] filed3 = new String[]{"Host name from netlog incorrect : ", "Application name from netlog incorrect : ", "Application status incorrect : "};


    private final static String URI_PUNNY_CODE = "http://\u043F\u0440\u0435\u0437\u0438\u0434\u0435\u043D\u0442.\u0440\u0444";

    private static final int SLEEPING_TIME = 67 * 1000;

    private static final long MIN_RAPID_CACHING_PERIOD_GA = 5000L;

    @SmallTest
    public void test_000_Init() throws Exception {
        //ClientUtil.reinstallOCC("/sdcard/apks/asimov-signed-r488981-eng004_nozip_ga_test_it_rooted_wo_msisdn.apk");
        //TestUtils.sleep(30 * 1000);
        PMSUtil.createPersonalScopeProperty("report_net_proto_stack", "@asimov@reporting@analysis", "1", true);
        TestUtil.sleep(90 * 1000);
    }

    /**
     * <h3>Verify that OC calculates client_in, client_out, server_in, server_out correctly.</h3>
     * actions:
     * <ol>
     *     <li>send 1 http request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     */
    //@Execute
    @SmallTest
    public void test_001_ClientServerCheckMissSimple() throws Exception {
        request1 = createBasicNetLogRequest(RESOURCE_URI1, false, 80);
        timeInfoTransaction1 = checkMiss(request1);
        logSleeping(TIME_NETLOG_STARTED_AWAITING);
        addDataCheck(timeInfoTransaction1);
    }

    /**
     * <h3>Verify that OC calculates client_in, client_out, server_in, server_out correctly.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to resource that returns ResponseContentSize=524038 to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     */
    //@Execute
    @SmallTest
    public void test_002_ClientServerCheckMissBigSize() throws Exception {
        size = 524038;
        request1 = createNetLogRequest(RESOURCE_URI2, size, "a");
        timeInfoTransaction1 = checkMiss(request1);
        logSleeping(TIME_NETLOG_STARTED_AWAITING);
        addDataCheck(timeInfoTransaction1);
    }

    /**
     * <h3>Verify that OC calculates client_in, client_out, server_in, server_out correctly.</h3>
     * actions:
     * <ol>
     *     <li>check miss for request to resource that returns ResponseContentSize=25613 to testrunner</li>
     *     <li>heck hit for the same request</li>
     *     <li>send request with HeaderField X-OC-ResponseContentSize=25613 to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data for both requests</li>
     * </ol>
     * @throws Exception
     */
    //@Execute
    @SmallTest
    public void test_003_ClientServerCheckHitRfc() throws Exception {
        size = 25613;
        request1 = createNetLogRequest(RESOURCE_URI3, size, "a");
        timeInfoTransaction1 = checkMiss(request1);
        TestUtil.sleep(SLEEPING_TIME);
        timeInfoTransaction2 = checkHit(request1);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);

        addDataCheck(timeInfoTransaction1);
        addDataCheck(timeInfoTransaction2, true);
    }

    /**
     * <h3>Verify that OC calculates client_in, client_out, server_in, server_out correctly.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to resource that returns random value for ResponseContentSize to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     */
    //@Ignore
    @SmallTest
    public void test_004_ClientServerCheckRandomSize() throws Exception {
        maxSize = 1024 * 1024;
        size = random.nextInt(maxSize) + 1;
        request1 = createNetLogRequest(RESOURCE_URI4, size, "a");
        timeInfoTransaction1 = checkMiss(request1);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);
        addDataCheck(timeInfoTransaction1);
    }

    /**
     * <h3>Verify that OC works correctly with Host Name, Application Name, Application Status.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     *     <li>send 1 request on real resource which host names have cyrillic symbols</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out, Host Name, Application Name, Application Status correspond to tcpdump data for both requests</li>
     * </ol>
     * @throws Exception
     */
    //@Execute
    @SmallTest
    public void test_005_HostsAppNamesStatusesNetlog() throws Exception {
        final String uri2_pc = PunycodeUtil.cyrillicToPunicode(URI_PUNNY_CODE);
        request1 = createNetLogRequest(createTestResourceUri(RESOURCE_URI5));
        request2 = createNetLogRequest(uri2_pc);
        timeInfoTransaction1 = checkMiss(request1);
        TestUtil.sleep(SLEEPING_TIME);
        timeInfoTransaction2 = checkMiss(request2);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);

        for (int i = 0; i < filed1.length; i++) {
            addGeneralCheck(timeInfoTransaction1, filed1[i], filed2Host1[i], filed3[i]);
        }

        for (int i = 0; i < filed1.length; i++) {
            addGeneralCheck(timeInfoTransaction2, filed1[i], filed2Host2[i], filed3[i]);
        }
    }

    /**
     * <h3>Verify OC works correctly with Status Code 100.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to resource that returns ResponseStatus=100 to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Throwable
     */
    //@Ignore
    @SmallTest
    public void test_006_ResponseStatus_SC_CONTINUE_Check() throws Throwable {
        timeInfoTransaction1 = checkMiss(RESOURCE_URI6, HttpStatus.SC_CONTINUE, true, TIME_NETLOG_STARTED_AWAITING);
        addGeneralCheck(timeInfoTransaction1, STATUS_CODE_FIELD_NAME, Integer.toString(HttpStatus.SC_CONTINUE), STATUS_CODE_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with Status Code 200.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to resource that returns ResponseStatus=200 to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Throwable
     */
    //@Execute
    @SmallTest
    public void test_007_ResponseStatus_SC_OK_Check() throws Throwable {
        timeInfoTransaction1 = checkMiss(RESOURCE_URI6, HttpStatus.SC_OK, TIME_NETLOG_STARTED_AWAITING);
        addGeneralCheck(timeInfoTransaction1, STATUS_CODE_FIELD_NAME, Integer.toString(HttpStatus.SC_OK), STATUS_CODE_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with Status Code 206.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to resource that returns ResponseStatus=206 to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Throwable
     */
    //@Execute
    @SmallTest
    public void test_008_ResponseStatus_SC_PARTIAL_CONTENT_Check() throws Throwable {
        timeInfoTransaction1 = checkMiss(RESOURCE_URI6, HttpStatus.SC_PARTIAL_CONTENT, TIME_NETLOG_STARTED_AWAITING);
        addGeneralCheck(timeInfoTransaction1, STATUS_CODE_FIELD_NAME, Integer.toString(HttpStatus.SC_PARTIAL_CONTENT), STATUS_CODE_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with Status Code 301.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to resource that returns ResponseStatus=301 to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Throwable
     */
    //@Execute
    @SmallTest
    public void test_009_ResponseStatus_SC_MOVED_PERMANENTLY_Check() throws Throwable {
        timeInfoTransaction1 = checkMiss(RESOURCE_URI6, HttpStatus.SC_MOVED_PERMANENTLY, TIME_NETLOG_STARTED_AWAITING);
        addGeneralCheck(timeInfoTransaction1, STATUS_CODE_FIELD_NAME, Integer.toString(HttpStatus.SC_MOVED_PERMANENTLY), STATUS_CODE_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with Status Code 302.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to resource that returns ResponseStatus=302 to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Throwable
     */
    //@Execute
    @SmallTest
    public void test_010_ResponseStatus_SC_MOVED_TEMPORARILY_Check() throws Throwable {
        timeInfoTransaction1 = checkMiss(RESOURCE_URI6, HttpStatus.SC_MOVED_TEMPORARILY, TIME_NETLOG_STARTED_AWAITING);
        addGeneralCheck(timeInfoTransaction1, STATUS_CODE_FIELD_NAME, Integer.toString(HttpStatus.SC_MOVED_TEMPORARILY), STATUS_CODE_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with Status Code 304.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to resource that returns ResponseStatus=304 to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Throwable
     */
    //@Ignore
    @SmallTest
    public void test_011_ResponseStatus_SC_NOT_MODIFIED_Check() throws Throwable {
        timeInfoTransaction1 = checkMiss(RESOURCE_URI6, HttpStatus.SC_NOT_MODIFIED, true, TIME_NETLOG_STARTED_AWAITING);
        addGeneralCheck(timeInfoTransaction1, STATUS_CODE_FIELD_NAME, Integer.toString(HttpStatus.SC_NOT_MODIFIED), STATUS_CODE_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with Status Code 400.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to resource that returns ResponseStatus=400 to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    public void test_012_ResponseStatus_SC_BAD_REQUEST_Check() throws Throwable {
        timeInfoTransaction1 = checkMiss(RESOURCE_URI6, HttpStatus.SC_BAD_REQUEST, TIME_NETLOG_STARTED_AWAITING);
        addGeneralCheck(timeInfoTransaction1, STATUS_CODE_FIELD_NAME, Integer.toString(HttpStatus.SC_BAD_REQUEST), STATUS_CODE_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with Status Code 403.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to resource that returns ResponseStatus=403 to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    public void test_013_ResponseStatus_SC_FORBIDDEN_Check() throws Throwable {
        timeInfoTransaction1 = checkMiss(RESOURCE_URI6, HttpStatus.SC_FORBIDDEN, TIME_NETLOG_STARTED_AWAITING);
        addGeneralCheck(timeInfoTransaction1, STATUS_CODE_FIELD_NAME, Integer.toString(HttpStatus.SC_FORBIDDEN), STATUS_CODE_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with Status Code 500.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to resource that returns ResponseStatus=500 to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Throwable
     */
    //@Execute
    @SmallTest
    public void test_014_ResponseStatus_SC_INTERNAL_SERVER_ERROR_Check() throws Throwable {
        timeInfoTransaction1 = checkMiss(RESOURCE_URI6, HttpStatus.SC_INTERNAL_SERVER_ERROR, TIME_NETLOG_STARTED_AWAITING);
        addGeneralCheck(timeInfoTransaction1, STATUS_CODE_FIELD_NAME, Integer.toString(HttpStatus.SC_INTERNAL_SERVER_ERROR), STATUS_CODE_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with Content Length.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to resource that returns ContentLength=4 to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    public void test_016_ContentLengthLittleSize() throws Throwable {
        final String expectedResult = "4";
        request1 = createNetLogRequest(createTestResourceUri(RESOURCE_URI8));
        timeInfoTransaction1 = checkMiss(request1);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);
        addGeneralCheck(timeInfoTransaction1, CONTENT_LENGTH_FIELD_NAME, expectedResult, CONTENT_LENGTH_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with Content Length.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to resource that returns ContentLength=123456 to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    public void test_017_ContentLengthBigSize() throws Throwable {
        size = 123456;
        request2 = createNetLogRequest(RESOURCE_URI9, size, "b");
        timeInfoTransaction1 = checkMiss(request2);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);
        addGeneralCheck(timeInfoTransaction1, CONTENT_LENGTH_FIELD_NAME, Integer.toString(size), CONTENT_LENGTH_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify that OC calculates client_in, client_out, server_in, server_out correctly.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to resource that returns ResponseContentSize=524038 to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    public void test_018_ResponseWithoutContentLength() throws Throwable {
        final String expectedResult = "4";
        request1 = createNetLogRequestWithRemoveContentLength(RESOURCE_URI10);
        timeInfoTransaction1 = checkMiss(request1);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);
        addGeneralCheck(timeInfoTransaction1, CONTENT_LENGTH_FIELD_NAME, expectedResult, CONTENT_LENGTH_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify that OC calculates client_in, client_out, server_in, server_out, HeaderLength correctly.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out, HeaderLength correspond to tcpdump data</li>
     * </ol>
     * @throws Throwable
     *
     */
    //@Execute
    @SmallTest
    public void test_019_HeaderLength() throws Throwable {
        request1 = createNetLogRequest(createTestResourceUri(RESOURCE_URI11));

        timeInfoTransaction1 = checkMiss(request1);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);

        tcpDump.stop();
        addGeneralCheck(timeInfoTransaction1, HEADER_LENGTH_FIELD_NAME, getExpectedHeaderLength(timeInfoTransaction1), HEADER_LENGTH_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out with HTTP and none HTTP traffic.</h3>
     * actions:
     * <ol>
     *     <li>send 4 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     */
    //@Execute
    @SmallTest
    public void test_020_IncorrectHttpVersion() throws Exception {
        for (TimeInfoTransaction trasaction : prepareTransactions(MIN_RAPID_CACHING_PERIOD_GA)) {
            addDataCheck(trasaction);
        }
    }

    /**
     * <h3>Verify OC works correctly with response time 1-2 seconds.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out arrives in less than 2 sec after request</li>
     * </ol>
     * @throws Throwable
     */
    //@Ignore
    //@Execute
    @SmallTest
    public void test_021_ResponseTimeSmallMiss() throws Throwable {
        final int delta = 2;
        timeInfoTransaction1 = checkMissSleep(RESOURCE_URI7, 1, 1);
        addDataInrangeCheck(timeInfoTransaction1, RESPONSE_TIME_FIELD_NAME, timeInfoTransaction1.getResponseTime() - delta,
                timeInfoTransaction1.getResponseTime() + delta, RESPONSE_TIME_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with response time 30 seconds.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out arrives between 30 and 32 sec after request</li>
     * </ol>
     * @throws Throwable
     */
    //@Ignore
    //@Execute
    @SmallTest
    public void test_022_ResponseTimeMiddleMiss() throws Throwable {
        final int delta = 2;
        timeInfoTransaction1 = checkMissSleep(RESOURCE_URI7, 30, 1);
        addDataInrangeCheck(timeInfoTransaction1, RESPONSE_TIME_FIELD_NAME, timeInfoTransaction1.getResponseTime() - delta,
                timeInfoTransaction1.getResponseTime() + delta, RESPONSE_TIME_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with response time 175 seconds.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out arrives between 175 and 177 sec after request</li>
     * </ol>
     * @throws Throwable
     */
    //@Ignore
    //@Execute
    @SmallTest
    public void test_023_ResponseTimeBigMiss() throws Throwable {
        final int delta = 2;
        timeInfoTransaction1 = checkMissSleep(RESOURCE_URI7, 175, 1);
        addDataInrangeCheck(timeInfoTransaction1, RESPONSE_TIME_FIELD_NAME, timeInfoTransaction1.getResponseTime() - delta,
                timeInfoTransaction1.getResponseTime() + delta, RESPONSE_TIME_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with response time 70 seconds.</h3>
     * actions:
     * <ol>
     *     <li>send 5 request with response time 70 seconds to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>checkMiss for 1,2, and 3 requests</li>
     *     <li>checkHit for 4 and 5 requests</li>
     *     <li>check last 2 requests that netlog data for client_in, client_out, server_in, server_out arrives between 70 and 73 sec after request</li>
     * </ol>
     * @throws Throwable
     */
    //@Ignore
    //@Execute
    @SmallTest
    public void test_024_ResponseTimeLpHit() throws Throwable {
        final int delta = 3;
        final int responseSleep = 70;
        uri = createTestResourceUri(RESOURCE_URI12 + Integer.toString(responseSleep));
        request1 = createRequest().setUri(uri)
                .setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-Sleep", Integer.toString(responseSleep)).getRequest();
        checkMiss(request1);
        TestUtil.sleep(1000);
        checkMiss(request1);
        TestUtil.sleep(1000);
        timeInfoTransaction1 = checkMiss(request1);
        TestUtil.sleep(1000);
        timeInfoTransaction2 = checkHit(request1);
        new Thread() {
            public void run() {
                TestUtil.sleep(10 * 1000);
                PrepareResourceUtil.invalidateResourceSafely(uri);
            }
        }.start();
        TestUtil.sleep(1000);
        checkHit(request1);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);

        addDataInrangeCheck(timeInfoTransaction1, RESPONSE_TIME_FIELD_NAME, timeInfoTransaction1.getResponseTime() - delta,
                timeInfoTransaction1.getResponseTime() + delta, RESPONSE_TIME_ERROR_MESSAGE);
        addDataInrangeCheck(timeInfoTransaction2, RESPONSE_TIME_FIELD_NAME, timeInfoTransaction2.getResponseTime() - delta,
                timeInfoTransaction2.getResponseTime() + delta, RESPONSE_TIME_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with response time for force HIT.</h3>
     * actions:
     * <ol>
     *     <li>send 5 request with response time 70 seconds to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>checkMiss for 1,2, and 3 requests</li>
     *     <li>checkHit for 4 and 5 requests</li>
     *     <li>check last request that netlog data for client_in, client_out, server_in, server_out arrives between 70 and 73 sec after request</li>
     * </ol>
     * @throws Throwable
     */
    //@Ignore
    //@Execute
    @SmallTest
    public void test_025_ResponseTimeLpForceHit() throws Throwable {
        final int delta = 3;
        final int responseSleep = 70;
        uri = createTestResourceUri(RESOURCE_URI12 + "_FH_" + Integer.toString(responseSleep));
        request1 = createRequest().setUri(uri)
                .setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-Sleep", Integer.toString(responseSleep)).getRequest();
        checkMiss(request1);
        TestUtil.sleep(1000);
        checkMiss(request1);
        TestUtil.sleep(1000);
        checkMiss(request1);
        TestUtil.sleep(1000);
        checkHit(request1);
        new Thread() {
            public void run() {
                TestUtil.sleep(10 * 1000);
                PrepareResourceUtil.invalidateResourceSafely(uri);
            }
        }.start();
        TestUtil.sleep(1000);
        timeInfoTransaction1 = checkHit(request1);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);
        addDataInrangeCheck(timeInfoTransaction1, RESPONSE_TIME_FIELD_NAME, timeInfoTransaction1.getResponseTime() - delta,
                timeInfoTransaction1.getResponseTime() + delta, RESPONSE_TIME_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with image/gif Content Type.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog content type is correct</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    public void test_026_ContentTypeImageGif() throws Throwable {
        uri = createTestResourceUri(RESOURCE_URI13 + contentTypes[0].substring(0, 3));
        final String expected = "HTTP/1.1 200 OK" + TFConstantsIF.CRLF + "Connection: close" + TFConstantsIF.CRLF +
                "Content-Length: 7" + TFConstantsIF.CRLF + "Content-Type: " + contentTypes[0] + TFConstantsIF.CRLF +
                TFConstantsIF.CRLF + "get1b..";
        final String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        request1 = createRequest().setUri(uri).setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-Raw", encoded).getRequest();
        timeInfoTransaction1 = checkMiss(request1);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);

        addGeneralCheck(timeInfoTransaction1, CONTENT_TYPE_FIELD_NAME, contentTypesChecked[0], CONTENT_TYPE_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with text/html Content Type.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog content type is correct</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    public void test_027_ContentTypeTextHtml() throws Throwable {
        uri = createTestResourceUri(RESOURCE_URI13 + contentTypes[1].substring(0, 3));
        final String expected = "HTTP/1.1 200 OK" + TFConstantsIF.CRLF + "Connection: close" + TFConstantsIF.CRLF +
                "Content-Length: 7" + TFConstantsIF.CRLF + "Content-Type: " + contentTypes[1] + TFConstantsIF.CRLF +
                TFConstantsIF.CRLF + "get1b..";
        final String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        request1 = createRequest().setUri(uri).setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-Raw", encoded).getRequest();
        timeInfoTransaction1 = checkMiss(request1);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);

        addGeneralCheck(timeInfoTransaction1, CONTENT_TYPE_FIELD_NAME, contentTypesChecked[1], CONTENT_TYPE_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with application/pdf Content Type.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog content type is correct</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    public void test_028_ContentTypeApplicationPdf() throws Throwable {
        uri = createTestResourceUri(RESOURCE_URI13 + contentTypes[2].substring(0, 3));
        final String expected = "HTTP/1.1 200 OK" + TFConstantsIF.CRLF + "Connection: close" + TFConstantsIF.CRLF +
                "Content-Length: 7" + TFConstantsIF.CRLF + "Content-Type: " + contentTypes[2] + TFConstantsIF.CRLF +
                TFConstantsIF.CRLF + "get1b..";
        final String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        request1 = createRequest().setUri(uri).setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-Raw", encoded).getRequest();
        timeInfoTransaction1 = checkMiss(request1);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);

        addGeneralCheck(timeInfoTransaction1, CONTENT_TYPE_FIELD_NAME, contentTypesChecked[2], CONTENT_TYPE_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with Network Protocol with tcp/http response.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog content type is correct</li>
     * </ol>
     * @throws Throwable
     *
     */
    //@Execute
    @SmallTest
    public void test_029_NetworkProtocolStackHttpMiss() throws Throwable {
        request1 = createRequest().setUri(createTestResourceUri(RESOURCE_URI14))
                .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-AddHeader_Last-Modified", "-86400")
                .addHeaderField("X-OC-AddHeader_Date", "GMT")
                .addHeaderField("X-OC-ResponseContentSize", "4,a").getRequest();

        timeInfoTransaction1 = checkMiss(request1);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);
        tcpDump.stop();
        checkNetworkSession_test29(timeInfoTransaction1);
    }

    /**
     * <h3>Verify OC works correctly with Network Protocol with tcp/http response.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog content type is correct</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    public void test_030_NetworkProtocolStackHttpHit() throws Throwable {
        request1 = createRequest().setUri(createTestResourceUri(RESOURCE_URI14 + "_HIT"))
                .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-AddHeader_Last-Modified", "-86400")
                .addHeaderField("X-OC-AddHeader_Date", "GMT")
                .addHeaderField("X-OC-ResponseContentSize", "4,a").getRequest();

        checkMiss(request1);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);
        timeInfoTransaction1 = checkHit(request1);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);

        addGeneralCheck(timeInfoTransaction1, NETWORK_PROTOCOL_STACK_FIELD_NAME, "-/-/-/-", NETWORK_PROTOCOL_STACK_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with Protocol with tcp/http response.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog content type is -/-/http/tcp</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    public void test_031_LocalProtoсolStackHttpMiss() throws Throwable {
        request1 = createRequest().setUri(createTestResourceUri(RESOURCE_URI14))
                .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-AddHeader_Last-Modified", "-86400")
                .addHeaderField("X-OC-AddHeader_Date", "GMT")
                .addHeaderField("X-OC-ResponseContentSize", "4,b").getRequest();

        timeInfoTransaction1 = checkMiss(request1);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);

        addGeneralCheck(timeInfoTransaction1, LOCAL_PROTOCOL_STACK_FIELD_NAME, "-/-/http/tcp", LOCAL_PROTOCOL_STACK_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with Protocol with tcp/http response.</h3>
     * actions:
     * <ol>
     *     <li>send 2 requests to testrunner</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog content type is -/-/http/tcp</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    public void test_032_LocalProtoсolStackHttpHit() throws Throwable {
        request1 = createRequest().setUri(createTestResourceUri(RESOURCE_URI14 + "_HIT"))
                .setMethod(HttpGet.METHOD_NAME).addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-AddHeader_Last-Modified", "-86400")
                .addHeaderField("X-OC-AddHeader_Date", "GMT")
                .addHeaderField("X-OC-ResponseContentSize", "4,b").getRequest();

        checkMiss(request1);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);
        timeInfoTransaction1 = checkMiss(request1);
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);

        addGeneralCheck(timeInfoTransaction1, LOCAL_PROTOCOL_STACK_FIELD_NAME, "-/-/http/tcp", LOCAL_PROTOCOL_STACK_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with Protocol with request version 1.0 and response version 1.1.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner with request version 1.0 and response version 1.1</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog content type is -/http/tc/tcp for network session and  -/-/http/tcp for local session</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    public void test_033_LocalProtokolStackHttpDifferentVersion() throws Throwable {
        uri = "ga_asimov_it_proxy_past_version";
        final String expected = "HTTP/1.0 200 OK" + TFConstantsIF.CRLF + "Connection: close" + TFConstantsIF.CRLF +
                "Content-length: 4" + TFConstantsIF.CRLF + TFConstantsIF.CRLF + "body";

        final String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
        final String request = "GET /" + uri + " HTTP/1.1" + TFConstantsIF.CRLF + "Connection: close" +
                TFConstantsIF.CRLF + "Host: " + AsimovTestCase.TEST_RESOURCE_HOST + TFConstantsIF.CRLF + "X-OC-Raw: " +
                encoded + TFConstantsIF.CRLF + TFConstantsIF.CRLF;

        timeInfoTransaction1 = checkMiss(request.getBytes());
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);

        addGeneralCheck(timeInfoTransaction1, LOCAL_PROTOCOL_STACK_FIELD_NAME, "-/-/http/tcp", LOCAL_PROTOCOL_STACK_ERROR_MESSAGE);
    }

    /**
     * <h3>Verify OC works correctly with Network Protocol with request version 1.0 and response version 1.1.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner with request version 1.0 and response version 1.1</li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog content type is -/-/http/tcp</li>
     * </ol>
     * @throws Throwable
     */
    @SmallTest
    //@Execute
    public void test_034_NetworkProtokolStackHttpDifferentVersion() throws Throwable {
        uri = "ga_asimov_it_proxy_past_version2";
        final String expected = "HTTP/1.0 200 OK" + TFConstantsIF.CRLF + "Connection: close" + TFConstantsIF.CRLF +
                "Content-length: 4" + TFConstantsIF.CRLF + TFConstantsIF.CRLF + "body";

        final String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
        final String request = "GET /" + uri + " HTTP/1.1" + TFConstantsIF.CRLF + "Connection: close" +
                TFConstantsIF.CRLF + "Host: tln-dev-testrunner1.7sys.eu" + TFConstantsIF.CRLF + "X-OC-Raw: " +
                encoded + TFConstantsIF.CRLF + TFConstantsIF.CRLF;

        timeInfoTransaction1 = checkMiss(request.getBytes());
        TestUtil.sleep(TIME_NETLOG_STARTED_AWAITING);

        tcpDump.stop();
        checkNetworkSession_test34(timeInfoTransaction1);
    }
}
