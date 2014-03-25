package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.SmokeUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.ArrayList;

public class SmokeTestCase extends TcpDumpTestCase {

    private static final Logger logger = LoggerFactory.getLogger(SmokeTestCase.class.getSimpleName());

    protected boolean testing = true;
    protected final long SLEEP_TIME = 10 * 1000;
    protected final long TEST_TIME = 60 * 1000 * 5;
    protected String host = "";
    protected ArrayList<String> processes = new ArrayList<String>();

    protected HttpRequest request;
    protected HttpResponse response;
    protected String propertyId;
    protected static final String ROAMING_WIFI_FAILOVER_ENABLED = "client.openchannel.roaming_wifi_failover.enabled";
    protected static final String ROAMING_WIFI_FAILOVER_ACTIONS = "client.openchannel.roaming_wifi_failover.actions";
    protected static final String POLICY_PATH = "@asimov@normalization@header";
    protected static final String FULL_POLICY_PATH = "@asimov@normalization@header@com.seven.asimov.it";
    protected static final String REST_FAILOVERS_PATH = "@asimov@failovers@roaming_wifi";
    protected static final String RESPONSE_HEADER_RULES = "response_header_rules";
    protected static final String RESPONSE_HEADER_RULES_VALUE = ((int) (Math.random() * 1000)) + "SVRNAME:.*\\r\\n";
    protected final static String PATH = "@asimov@application@com.seven.asimov.it";
    protected final static String NAME = "ssl";

    private String base64Encode(String expected) {
        return URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
    }

    /**
     * @param uri       - test built uri
     * @param headers   - headers response
     * @param keepAlive - keepAlive
     * @param delay     - delay between requests
     * @throws Throwable
     */
    public void executeServerRevalidation(String uri, String headers, boolean keepAlive, long delay) throws Throwable {
        HttpResponse response;
        String encodedRawHeadersDef = base64Encode(headers);
        final HttpRequest request = SmokeUtil.buildDefaultRequest(uri).getRequest();
        PrepareResourceUtil.prepareResource200(uri, false, encodedRawHeadersDef);

        try {
            response = checkMiss(request, 1, "tere", keepAlive, TIMEOUT);
            logSleeping(delay - response.getDuration());

            response = checkHit(request, 2, "tere", keepAlive, TIMEOUT);
            logSleeping(delay - response.getDuration());

            response = checkHit(request, 3, "tere", keepAlive, TIMEOUT);
            logSleeping(delay - response.getDuration());

            response = checkHit(request, 4, "tere", keepAlive, TIMEOUT);
            logSleeping(delay - response.getDuration());

            response = checkMiss(request, 5, "tere", keepAlive, TIMEOUT);
            logSleeping(delay - response.getDuration());

            response = checkHit(request, 6, "tere", keepAlive, TIMEOUT);
            logSleeping(delay - response.getDuration());

            checkHit(request, 7, "tere", keepAlive, TIMEOUT);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
        }
    }
}
