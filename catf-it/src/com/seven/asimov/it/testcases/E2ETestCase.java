package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class E2ETestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(E2ETestCase.class.getSimpleName());
    protected static final List<String> properties = new ArrayList<String>();
    protected static String z7TpId;
    protected static final String TEST_START_NOTIFICATION_PATH_END = "/rest/testStart";
    protected static final String TEST_END_NOTIFICATION_PATH_END = "/rest/testFinish";
    protected static final String POLL_REQUEST_PATH_END = "/rest/poll";
    protected static final String MSISDN_REQUEST_PATH_END = "/rest/msisdnvalidate";
    protected static final String CHECK_ENDPOINT_AND_POLICY_DATA_HASH_END = "/rest/policyretrieve";
    protected static final String POLICY_SET_PATH_END = "/rest/policyset";
    protected static final String CRCS_REPORT_PATH_END = "/rest/crcsreport";
    protected static final String FIREWALL_PATH_END = "/rest/firewall";
    protected static final String REST_BATCH_PATH_END = "/rest/batch";
    protected static final String POLL_REQUEST_BODY_PATTERN = "<PollRequest><caseID>%s</caseID>" +
            "<operationType>%s</operationType><subscriptionID>%s</subscriptionID>" +
            "<z7TP>%s</z7TP><resourceKey>%s</resourceKey></PollRequest>";
    protected static final String UKKO_POLL_REQUEST_BODY_PATTERN = "<PollRequest><caseID>%s</caseID>" +
            "<operationType>UKKO_PO_ON</operationType><z7TP>%s</z7TP><resourceKey>%s</resourceKey>" +
            "<pollCount>%s</pollCount></PollRequest>";
    protected static final String TESTRUNNER_POLL_REQUEST_BODY_PATTERN = "<PollRequest><caseID>%s</caseID>" +
            "<operationType>TR_PO_ON</operationType><path>%s</path>" +
            "<pollCount>%s</pollCount><pollInterval>%s</pollInterval></PollRequest>";
    protected static final String TEST_START_END_REQUEST_BODY_PATTERN = "<Request><testName>%s</testName>" +
            "<time>%d</time></Request>";
    protected static final String MSISDN_REQUEST_PATTERN =
            "<MSISDNValidateRequest><caseID>%s</caseID><z7TP>%s</z7TP><isValidate>true</isValidate><newMSISDN>%s</newMSISDN></MSISDNValidateRequest>";
    protected static final String CHECK_ENDPOINT_AND_POLICY_DATA_HASH_PATTERN = "<PolicyRetriveRequest><caseID>%s</caseID>" +
            "<z7TP>%s</z7TP><policyDataHash>%s</policyDataHash></PolicyRetriveRequest>";
    protected static final String POLICY_CREATE_SCHEDULING_BODY_PATTERN = "<PolicyCheckRequest><caseID>%s</caseID><operationType>%s</operationType><z7TP>%s</z7TP>" +
            "<policyRequestBody>%s</policyRequestBody><PMServerIP>%s</PMServerIP><PMServerPort>%s</PMServerPort><delay>%s</delay></PolicyCheckRequest>";
    protected static final String CRCS_REPORTING_BODY_PATTERN = "<CrcsReportRequest><caseID>%s</caseID><z7TP>%s</z7TP><sizeOfData>%s</sizeOfData><firewall>%b</firewall><power>%b</power><system>%b</system><traffic>%b</traffic><radio>%b</radio><service>%b</service></CrcsReportRequest>";
    protected static final String FIREWALL_QUERY_PATTERN = "<FirewallQueryRequest><caseID>%s</caseID><checkType>%s</checkType><pcfDelta>%s</pcfDelta></FirewallQueryRequest>";
    protected static final String TH_REQUEST_OPERATION_TYPE = "TH_SP_RQ";
    protected static final String TH_RESPONSE_OPERATION_TYPE = "TH_SP_RP";
    protected static final String CASSANDRA_SUBSCR_OPERATION_TYPE = "CS_PS_RC";
    protected static final String SET_POLICY_WITH_DELAY_OPERATION_TYPE = "SP_WD";
    protected static final String POLICY_SENT_TO_PMS_OPERATION_TYPE = "PS";
    protected static final String SUCCESS = "success";

    protected void pingHost(String host) {
        try {
            Process ping = Runtime.getRuntime().exec("ping -s1 -c1 -W3 -n " + host);
            ping.waitFor();
        } catch (Exception e) {
            logger.debug("pingHost Exception:\n" + ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * <h1>Notify Rest for tests start</h1>
     * This rest service API is used to notify Rest for tests start.
     * <br /><br />
     * <b>URL:</b> http://hostname/rest/testStart
     * <br />
     * <b>HTTP Method:</b> Post
     * <br />
     * <b>Request Parameters:</b>
     * <ul>
     * <li>testName: test name</li>
     * <li>time: current time</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <Request> }<br />
     * {@code <testName>E2E-Polling</caseID> }<br />
     * {@code <time>1368797598642</time> }<br />
     * {@code </Request> }<br />
     * <br />
     * <b>Response Parameters:</b>
     * <ul>
     * <li>result:  success or fail</li>
     * <li>message:  if fail, the test server will provide some information</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <Response> }<br />
     * {@code <result>success</result> }<br />
     * {@code <message></message> }<br />
     * {@code </Response> }<br />
     *
     * @param suiteId Subscription Id
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    protected void notifyRestForTestsStart(String suiteId) throws Exception {
        String requestBody = String.format(TEST_START_END_REQUEST_BODY_PATTERN, suiteId, System.currentTimeMillis());
        sendPostRequestToRest(TEST_START_NOTIFICATION_PATH_END, requestBody);
    }

    /**
     * <h1>Notify Rest for tests start</h1>
     * This rest service API is used to notify Rest for tests finish.
     * <br /><br />
     * <b>URL:</b> http://hostname/rest/testFinish
     * <br />
     * <b>HTTP Method:</b> Post
     * <br />
     * <b>Request Parameters:</b>
     * <ul>
     * <li>testName: test name</li>
     * <li>time: current time</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <Request> }<br />
     * {@code <testName>E2E-Polling</caseID> }<br />
     * {@code <time>1368797598642</time> }<br />
     * {@code </Request> }<br />
     * <br />
     * <b>Response Parameters:</b>
     * <ul>
     * <li>result:  success or fail</li>
     * <li>message:  if fail, the test server will provide some information</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <Response> }<br />
     * {@code <result>success</result> }<br />
     * {@code <message></message> }<br />
     * {@code </Response> }<br />
     *
     * @param suiteId Subscription Id
     * @throws IOException
     * @throws URISyntaxException
     */
    protected void notifyRestForTestEnd(String suiteId) throws Exception {
        String requestBody = String.format(TEST_START_END_REQUEST_BODY_PATTERN, suiteId, System.currentTimeMillis());
        sendPostRequestToRest(TEST_END_NOTIFICATION_PATH_END, requestBody);
    }

    /**
     * <h1>Check polling via Test Runner</h1>
     * This rest service API is used to check the poll times and interval is right on UKKO.
     * <br /><br />
     * <b>URL:</b> http://hostname/rest/poll
     * <br />
     * <b>HTTP Method:</b> Post
     * <br />
     * <b>Request Parameters:</b>
     * <ul>
     * <li>caseID: identify the case</li>
     * <li>operationType: TR_PO_ON</li>
     * <li>resourceKey: MD5 calculation according to HTTP request, identify the resource</li>
     * <li>pollCount: the expected poll times, since there may be more than one HTTP request record</li>
     * <li>pollInterval(second): the expected poll interval, the test server will add some buffer to check poll interval, it can’t exactly match</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <PollRequest> }<br />
     * {@code <caseID>e2e-001</caseID> }<br />
     * {@code <operationType>TR_PO_ON</operationType> }<br />
     * {@code <resourceKey> E2848C5C185F3AF5BEED043833C7DFA7 </resourceKey> }<br />
     * {@code <pollCount>2</pollCount> }<br />
     * {@code <pollInterval>35</pollInterval> }<br />
     * {@code </PollRequest> }<br />
     * <br />
     * <b>Response Parameters:</b>
     * <ul>
     * <li>result:  success or fail</li>
     * <li>message:  if fail, the test server will provide some information</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <Response> }<br />
     * {@code <result>success</result> }<br />
     * {@code <message></message> }<br />
     * {@code </Response> }<br />
     *
     * @param pollInterval Polling Interval
     * @param pollCount    Polling Quantity
     * @param path         the path which was polled by UKKO
     * @throws IOException
     * @throws URISyntaxException
     */
    protected void checkTestrunnerPolling(int pollCount, String pollInterval, String path)
            throws IOException, URISyntaxException {
        String requestBody = String.format(TESTRUNNER_POLL_REQUEST_BODY_PATTERN, getName(), path.substring(35), Integer.toString(pollCount),
                pollInterval);
        String response = sendPostRequestToRest(POLL_REQUEST_PATH_END, requestBody);
        boolean isSuccess = response != null && response.toLowerCase().contains(SUCCESS);
        assertTrue("Testrunner should be polled " + pollCount + " times with interval " + pollInterval + ". Response from Rest API : " + response, isSuccess);
    }

    /**
     * <h1>checkEndpointAndPolicyDataHash</h1>
     * This rest service API is used to check that endpoint was created (after install OC Client) and server policy tree hash equals local policy tree hash.
     * <br /><br />
     * <b>URL:</b> http://hostname/rest/policyretrieve
     * <br />
     * <b>HTTP Method:</b> Post
     * <br />
     * <b>Request Parameters:</b>
     * <ul>
     * <li>caseID: identify the case</li>
     * <li>policyDataHash: local policy tree hash</li>
     * <li>z7TP: client z7TP address</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <PolicyRetriveRequest> }<br />
     * {@code <caseID>e2e-001</caseID> }<br />
     * {@code <z7TP>275a</z7TP> }<br />
     * {@code <policyDataHash>-12101128006113</policyDataHash> }<br />
     * {@code </PolicyRetriveRequest> }<br />
     * <br />
     * <b>Response Parameters:</b>
     * <ul>
     * <li>result:  success or fail</li>
     * <li>message:  if fail, the test server will provide some information</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <Response> }<br />
     * {@code <result>success</result> }<br />
     * {@code <message></message> }<br />
     * {@code </Response> }<br />
     *
     * @param policyDataHash policy three hash (local or server)
     * @throws IOException
     * @throws URISyntaxException
     */
    protected void checkEndpointAndPolicyDataHash(long policyDataHash)
            throws IOException, URISyntaxException {
        String requestBody = String.format(CHECK_ENDPOINT_AND_POLICY_DATA_HASH_PATTERN, getName(), z7TpId, policyDataHash);
        String response = sendPostRequestToRest(CHECK_ENDPOINT_AND_POLICY_DATA_HASH_END, requestBody);
        boolean isSuccess = response != null && response.toLowerCase().contains(SUCCESS);
        assertTrue("REST check failed. Local policy data hash should equals to server policy data hash. Local value is " + policyDataHash, isSuccess);
    }

    /**
     * <h1>checkMsisdnInServer</h1>
     * This rest service API is used to check MSISDN validation on server side.
     * <br /><br />
     * <b>URL:</b> http://hostname/rest/msisdnvalidate
     * <br />
     * <b>HTTP Method:</b> Post
     * <br />
     * <b>Request Parameters:</b>
     * <ul>
     * <li>caseID: identify the case</li>
     * <li>policyDataHash: local policy tree hash</li>
     * <li>z7TP: client z7TP address</li>
     * <li>isValidate: is validation enabled</li>
     * <li>newMSISDN: client MSISDN value</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <MSISDNValidateRequest> }<br />
     * {@code <caseID>e2e-001</caseID> }<br />
     * {@code <z7TP>275a</z7TP> }<br />
     * {@code <isValidate>true</isValidate> }<br />
     * {@code <newMSISDN>255017540922611</newMSISDN> }<br />
     * {@code </MSISDNValidateRequest> }<br />
     * <br />
     * <b>Response Parameters:</b>
     * <ul>
     * <li>result:  success or fail</li>
     * <li>message:  if fail, the test server will provide some information</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <Response> }<br />
     * {@code <result>success</result> }<br />
     * {@code <message></message> }<br />
     * {@code </Response> }<br />
     *
     * @param msisdn msisdn value
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    protected void checkMsisdnInServer(String msisdn) throws IOException, URISyntaxException {
        String requestBody = String.format(MSISDN_REQUEST_PATTERN, getName(), z7TpId, msisdn);
        String response = sendPostRequestToRest(MSISDN_REQUEST_PATH_END, requestBody);
        boolean isSuccess = response != null && response.toLowerCase().contains(SUCCESS);
        assertTrue("REST check failed. MSISDN validation in server side", isSuccess);
    }

    /**
     * <h1>Check poll starting on UKKO</h1>
     * This rest service API is used to check the poll is running on UKKO.
     * <br /><br />
     * <b>URL:</b> http://hostname/rest/poll
     * <br />
     * <b>HTTP Method:</b> Post
     * <br />
     * <b>Request Parameters:</b>
     * <ul>
     * <li>caseID: identify the case</li>
     * <li>operationType: UKKO_PO_ON</li>
     * <li>resourceKey: MD5 calculation according to HTTP request, identify the resource</li>
     * <li>pollCount: the expected poll times, since there are more than one record in UKKO log</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <PollRequest> }<br />
     * {@code <caseID>e2e-001</caseID> }<br />
     * {@code <operationType>UKKO_PO_ON</operationType> }<br />
     * {@code <resourceKey> E2848C5C185F3AF5BEED043833C7DFA7 </resourceKey> }<br />
     * {@code <pollCount>2</ pollCount> }<br />
     * {@code </PollRequest> }<br />
     * <br />
     * <b>Response Parameters:</b>
     * <ul>
     * <li>result:  success or fail</li>
     * <li>message:  if fail, the test server will provide some information</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <Response> }<br />
     * {@code <result>success</result> }<br />
     * {@code <message></message> }<br />
     * {@code </Response> }<br />
     *
     * @param resourceKey Resource Key
     * @param pollCount   Count of polls
     * @throws IOException
     * @throws URISyntaxException
     */
    protected void checkUkkoPolling(String resourceKey, int pollCount)
            throws IOException, URISyntaxException {
        String requestBody = String.format(UKKO_POLL_REQUEST_BODY_PATTERN, getName(), z7TpId, resourceKey, Integer.toString(pollCount));
        String response = sendPostRequestToRest(POLL_REQUEST_PATH_END, requestBody);
        boolean isSuccess = response != null && response.toLowerCase().contains(SUCCESS);
        assertTrue("Ukko should poll resource " + pollCount + " times", isSuccess);
    }

    /**
     * <h1>Check Start Poll Request on TH</h1>
     * This rest service API is used to check the poll is started on TH via TH log.
     * <br /><br />
     * <b>URL:</b> http://hostname/rest/poll
     * <br />
     * <b>HTTP Method:</b> Post
     * <br />
     * <b>Request Parameters:</b>
     * <ul>
     * <li>caseID: identify the case</li>
     * <li>operationType: TH_SP_RQ (Traffic Harmonizer Start Poll Request to check whether TH started poll)</li>
     * <li>subscriptionID: identify the subscription</li>
     * <li>z7TP: Z7 address of test client</li>
     * <li>resourceKey: MD5 calculation according to HTTP request, identify the resource</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <PollRequest> }<br />
     * {@code <caseID>e2e-001</caseID> }<br />
     * {@code <operationType>TH_SP_RQ</operationType> }<br />
     * {@code <subscriptionID>1000</subscriptionID> }<br />
     * {@code <z7TP>435</z7TP> }<br />
     * {@code <resourceKey> E2848C5C185F3AF5BEED043833C7DFA7 </resourceKey> }<br />
     * {@code </PollRequest> }<br />
     * <br />
     * <b>Response Parameters:</b>
     * <ul>
     * <li>result:  success or fail</li>
     * <li>message:  if fail, the test server will provide some information</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <Response> }<br />
     * {@code <result>success</result> }<br />
     * {@code <message></message> }<br />
     * {@code </Response> }<br />
     *
     * @param subscriptionId Subscription Id
     * @param resourceKey    Resource Key
     * @throws IOException
     * @throws URISyntaxException
     */
    protected void checkThPollRequest(String subscriptionId, String resourceKey)
            throws IOException, URISyntaxException {
        String requestBody = String.format(POLL_REQUEST_BODY_PATTERN, getName(), TH_REQUEST_OPERATION_TYPE, subscriptionId,
                z7TpId, resourceKey);
        String response = sendPostRequestToRest(POLL_REQUEST_PATH_END, requestBody);
        boolean isSuccess = response != null && response.toLowerCase().contains(SUCCESS);
        assertTrue("Polling request should be send successfully to TH", isSuccess);
    }

    /**
     * <h1>Check Subscription&Resource in Cassandra</h1>
     * This rest service API is used to check the subscription is added to resource in Cassandra.
     * <br /><br />
     * <b>URL:</b> http://hostname/rest/poll
     * <br />
     * <b>HTTP Method:</b> Post
     * <br />
     * <b>Request Parameters:</b>
     * <ul>
     * <li>caseID: identify the case</li>
     * <li>operationType: CS_PS_RC</li>
     * <li>subscriptionID: identify the subscription</li>
     * <li>z7TP: Z7 address of test client</li>
     * <li>resourceKey: MD5 calculation according to HTTP request, identify the resource</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <PollRequest> }<br />
     * {@code <caseID>e2e-001</caseID> }<br />
     * {@code <operationType>CS_PS_RC</operationType> }<br />
     * {@code <subscriptionID>1000</subscriptionID> }<br />
     * {@code <z7TP>435</z7TP> }<br />
     * {@code <resourceKey> E2848C5C185F3AF5BEED043833C7DFA7 </resourceKey> }<br />
     * {@code </PollRequest> }<br />
     * <br />
     * <b>Response Parameters:</b>
     * <ul>
     * <li>result:  success or fail</li>
     * <li>message:  if fail, the test server will provide some information</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <Response> }<br />
     * {@code <result>success</result> }<br />
     * {@code <message></message> }<br />
     * {@code </Response> }<br />
     *
     * @param subscriptionId Subscription Id
     * @param resourceKey    Resource Key
     * @throws IOException
     * @throws URISyntaxException
     */
    protected void checkSubscriptionInCassandra(String subscriptionId, String resourceKey)
            throws IOException, URISyntaxException {
        String requestBody = String.format(POLL_REQUEST_BODY_PATTERN, getName(), CASSANDRA_SUBSCR_OPERATION_TYPE,
                subscriptionId, z7TpId, resourceKey);
        String response = sendPostRequestToRest(POLL_REQUEST_PATH_END, requestBody);
        boolean isSuccess = response != null && response.toLowerCase().contains(SUCCESS);
        assertTrue("Subscription should be added to Cassandra", isSuccess);
    }

    /**
     * <h1>Check Start Poll Response on TH</h1>
     * This rest service API is used to check TH send poll response to client via TH log.
     * <br /><br />
     * <b>URL:</b> http://hostname/rest/poll
     * <br />
     * <b>HTTP Method:</b> Post
     * <br />
     * <b>Request Parameters:</b>
     * <ul>
     * <li>caseID: identify the case</li>
     * <li>operationType: TH_SP_RP (Traffic Harmonizer Start Poll Response to check whether TH started poll)</li>
     * <li>subscriptionID: identify the subscription</li>
     * <li>z7TP: Z7 address of test client</li>
     * <li>resourceKey: MD5 calculation according to HTTP request, identify the resource</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <PollRequest> }<br />
     * {@code <caseID>e2e-001</caseID> }<br />
     * {@code <operationType>TH_SP_RP</operationType> }<br />
     * {@code <subscriptionID>1000</subscriptionID> }<br />
     * {@code <z7TP>435</z7TP> }<br />
     * {@code <resourceKey> E2848C5C185F3AF5BEED043833C7DFA7 </resourceKey> }<br />
     * {@code </PollRequest> }<br />
     * <br />
     * <b>Response Parameters:</b>
     * <ul>
     * <li>result:  success or fail</li>
     * <li>message:  if fail, the test server will provide some information</li>
     * </ul>
     * <b>Example:</b>
     * <br />
     * {@code <Response> }<br />
     * {@code <result>success</result> }<br />
     * {@code <message></message> }<br />
     * {@code </Response> }<br />
     *
     * @param subscriptionId Subscription Id
     * @param resourceKey    Resource Key
     * @throws IOException
     * @throws URISyntaxException
     */
    protected void checkThPollResponse(String subscriptionId, String resourceKey)
            throws IOException, URISyntaxException {
        String requestBody = String.format(POLL_REQUEST_BODY_PATTERN, getName(), TH_RESPONSE_OPERATION_TYPE,
                subscriptionId, z7TpId, resourceKey);
        String response = sendPostRequestToRest(POLL_REQUEST_PATH_END, requestBody);
        boolean isSuccess = response != null && response.toLowerCase().contains(SUCCESS);
        assertTrue("Polling response should be received successfully from TH", isSuccess);
    }

    /**
     * The method checks that polling is started.
     * Process the next checks:
     * <ul>
     * <li>{@link E2EPollingTestCase#checkThPollRequest}</li>
     * <li>{@link E2EPollingTestCase#checkThPollResponse}</li>
     * <li>{@link E2EPollingTestCase#checkSubscriptionInCassandra}</li>
     * </ul>
     *
     * @param subscriptionId subscription ID value
     * @param resourceKey    Resource key value
     * @throws IOException
     * @throws URISyntaxException
     */
    protected void checkPollingStarted(String subscriptionId, String resourceKey)
            throws IOException, URISyntaxException {
        checkThPollRequest(subscriptionId, resourceKey);
        checkThPollResponse(subscriptionId, resourceKey);
        checkSubscriptionInCassandra(subscriptionId, resourceKey);
    }

    protected static String sendPostRequestToRest(String pathEnd, String body) throws IOException, URISyntaxException {
        String uri = "http://" + TFConstantsIF.EXTERNAL_IP + ":" + TFConstantsIF.REST_SERVER_PORT + pathEnd;
        HttpRequest request = HttpRequest.Builder.create().setMethod("POST")
                .addHeaderField("Content-type", "application/xml")
                .setUri(uri)
                .setBody(body)
                .getRequest();
        return AsimovTestCase.sendRequest(request).getBody();
    }
}
