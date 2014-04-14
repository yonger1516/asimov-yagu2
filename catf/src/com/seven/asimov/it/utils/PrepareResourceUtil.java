package com.seven.asimov.it.utils;

import com.seven.asimov.it.asserts.CATFAssert;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.constants.BaseConstantsIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.UUID;

import static com.seven.asimov.it.base.AsimovTestCase.createRequest;
import static com.seven.asimov.it.base.AsimovTestCase.sendRequest2;
import static com.seven.asimov.it.base.constants.BaseConstantsIF.INVALIDATED_RESPONSE;
import static com.seven.asimov.it.base.constants.BaseConstantsIF.VALID_RESPONSE;
import static junit.framework.Assert.assertEquals;

public final class PrepareResourceUtil {

    private static final Logger logger = LoggerFactory.getLogger(PrepareResourceUtil.class.getSimpleName());

    private PrepareResourceUtil() {
    }

    /**
     * Prepares test resource for test by sending request with special header to change content.
     * This request will be send bypassing the OC to not affect following tests.
     *
     * @param uri
     * @param invalidate
     * @throws Exception
     */
    public static HttpResponse prepareResource(String uri, boolean invalidate, long responseDelay) throws Exception {
        logger.info("Preparing test resource...");
        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ChangeResponseContent", invalidate ? INVALIDATED_RESPONSE : VALID_RESPONSE)
                .addHeaderField("X-OC-ChangeSleep", String.valueOf(responseDelay))
                .addHeaderField("X-OC-BelongsTo", BaseConstantsIF.TEST_RESOURCE_OWNER).getRequest();
        HttpResponse response = AsimovTestCase.sendRequest(request, false, true);
        CATFAssert.assertEquals("Response body is correct ", invalidate ? INVALIDATED_RESPONSE : VALID_RESPONSE, response.getBody());
        return response;
    }

    public static HttpResponse prepareResource(String uri, boolean invalidate) throws Exception {
        return prepareResource(uri, invalidate, 0);
    }

    public static HttpResponse prepareResourceParallel(String uri, boolean invalidate, long responseDelay) throws Exception {
        logger.info("Preparing test resource...");
        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ChangeResponseContent", invalidate ? INVALIDATED_RESPONSE : VALID_RESPONSE)
                .addHeaderField("X-OC-ChangeSleep", String.valueOf(responseDelay))
                .addHeaderField("X-OC-BelongsTo", BaseConstantsIF.TEST_RESOURCE_OWNER).getRequest();
        HttpResponse response = AsimovTestCase.sendRequestParallel(request, false, true);
        CATFAssert.assertEquals("Response body is correct ", invalidate ? INVALIDATED_RESPONSE : VALID_RESPONSE, response.getBody());
        return response;
    }

    public static HttpResponse prepareResourceParallel(String uri, boolean invalidate) throws Exception {
        return prepareResourceParallel(uri, invalidate, 0);
    }

    public static HttpResponse prepareDiffResource(String uri, String body) throws Exception {
        logger.info("Set : <" + uri + "> response body to: < " + body + ">");
        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ChangeResponseContent", body).getRequest();
        HttpResponse response = AsimovTestCase.sendRequest(request, false, true);
        CATFAssert.assertEquals("Expected body = " + body + " but was " + response.getBody(), body, response.getBody());
        return response;
    }

    public static HttpResponse prepareResource200(String uri, boolean invalidate, String rawHeaders) throws Exception {
        logger.info("Preparing test resource...");
        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ChangeResponseContent", invalidate ? INVALIDATED_RESPONSE : VALID_RESPONSE)
                .addHeaderField("X-OC-ChangeResponseStatus", "200")
                .addHeaderField("X-OC-AddRawHeadersPermanently", rawHeaders)
                .addHeaderField("X-OC-ChangeSleep", "0")
                .addHeaderField("X-OC-BelongsTo", BaseConstantsIF.TEST_RESOURCE_OWNER).getRequest();
        HttpResponse response = sendRequest2(request, false, true);
        CATFAssert.assertEquals("Prepare resource ", invalidate ? INVALIDATED_RESPONSE : VALID_RESPONSE, response.getBody());
        return response;
    }

    public static HttpResponse prepareResource304(String uri, String rawHeaders) throws Exception {
        logger.info("Preparing test resource...");
        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ChangeResponseContent", "")
                .addHeaderField("X-OC-ChangeResponseStatus", "304")
                .addHeaderField("X-OC-AddRawHeadersPermanently", rawHeaders)
                .addHeaderField("X-OC-ChangeSleep", "0")
                .addHeaderField("X-OC-BelongsTo", BaseConstantsIF.TEST_RESOURCE_OWNER).getRequest();
        HttpResponse response = sendRequest2(request, false, true);
        assertEquals("", response.getBody());
        return response;
    }

    public static void invalidateResourceSafely(String uri) {
        UUID guid = UUID.randomUUID();
        guid.toString();
        invalidateResourceSafely(uri, guid.toString(), false);
    }

    public static void invalidateResourceSafelyParallel(String uri) {
        UUID guid = UUID.randomUUID();
        guid.toString();
        invalidateResourceSafely(uri, guid.toString(), true);
    }

    public static void invalidateResourceSafely(String uri, String content, boolean parallel) {
        try {

            String headers = "Fake-OC-Invalidating-Header: " + TestUtil.generationRandomString(15);
            String encoded = URLEncoder.encode(Base64.encodeToString(headers.getBytes(), Base64.DEFAULT));

            HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ChangeResponseContent", content)
                    .addHeaderField("X-OC-BelongsTo", BaseConstantsIF.TEST_RESOURCE_OWNER)
                    .addHeaderField("X-OC-AddRawHeadersPermanently", encoded)
                    .addHeaderField("X-OC-Sleep", 0 + "")
                    .addHeaderField("X-OC-Freeze", "true")
                    .getRequest();
            if (parallel) {
                AsimovTestCase.sendRequestParallel(request, null, false, true, AsimovTestCase.Body.NOBODY);
            } else {
                AsimovTestCase.sendRequest(request, null, false, true, AsimovTestCase.Body.NOBODY);
            }
        } catch (Exception e) {
            logger.error("Invalidating resource, exception ignored");
            e.printStackTrace();
        }
    }

    public static void prepareResourceWithDelayedChange(String uri, int delay) throws Exception {
        logger.info("Preparing test resource...");
        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ChangeContentAfterSec", Integer.toString(delay))
                .getRequest();
        AsimovTestCase.sendRequest(request, false, true);
    }

    public static void prepareResourceWithDelay(String uri, int delay) {
        logger.info("Setting up resource...");
        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-Stateless-Sleep", "true")
                .addHeaderField("X-OC-ChangeSleep", Integer.toString(delay))
                .getRequest();
        sendRequest2(request, false, true);
    }

    public static void invalidateLongPoll(String uri) {
        try {
            UUID guid = UUID.randomUUID();
            String longPollInvalidateBody = guid.toString();
            HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-Freeze", "true")
                    .addHeaderField("X-OC-Sleep", "5")
                    .addHeaderField("X-OC-Stateless-Sleep", "true")
                    .addHeaderField("X-OC-ChangeResponseContent", longPollInvalidateBody)
                    .getRequest();
            AsimovTestCase.sendRequest(request, null, false, true, AsimovTestCase.Body.BODY);
        } catch (Exception e) {
            logger.error("Invalidating resource, exception ignored");
            e.printStackTrace();
        }
    }

}
