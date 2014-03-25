package com.seven.asimov.it.tests.dispatchers.transparent;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.testcases.TransparentTestCase;
import com.seven.asimov.it.utils.date.DateUtil;

import java.util.Date;

import static com.seven.asimov.it.base.constants.TFConstantsIF.CRLF;
public class TransparentTests extends TransparentTestCase {

    /**
     * Pattern [0,35,35,35,35,35]
     * 1. RMP should be detected after 3-rd request and polling should start after receiving response.
     * 2. 4th request should be HITed
     * 3. Policy should be received and applied.
     * 4. Stop poll should be sent to the server, RR should be deactivated and CE should be removed
     *
     * @throws Throwable
     */

    public void testSwitchingToTheTransparentMode_001() throws Throwable {
        final String uri = createTestResourceUri("switching_to_the_transparent_mode_rmp");
        final HttpRequest request = createRequest().setUri(uri)
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        funcForSwithchingTransparentMode(uri, null, null, request, 25 * 1000, 0, 3, RMP_PERIOD);
    }

    /**
     * Pattern [0,35,35,35,35,35]
     * Delay [21, 21, 21, 21, 21,21]
     * 1. RLP should be detected after 3-rd request and polling should start after receiving response.
     * 2. 4th request should be HITed
     * 3. Policy should be received and applied.
     * 4. Stop poll should be sent to the server, RR should be deactivated and CE should be removed
     *
     * @throws Throwable
     */
    public void testSwitchingToTheTransparentMode_002() throws Throwable {
        final String uri = createTestResourceUri("switching_to_the_transparent_mode_rlp");
        final HttpRequest request = createRequest().setUri(uri)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "21").getRequest();

        funcForSwithchingTransparentMode(uri, null, null, request, 25 * 1000, 0, 3, RLP_PERIOD);
    }

    /**
     * Pattern [0,70,70,70,70]
     * Delays[65,65,65,65,65]
     * 1. LP should be detected after 2nd request and polling should start after receiving response.
     * 2. 3rd request should be HITed
     * 3. Policy should be received and applied.
     * 4. 3rd  request should be force HITed
     * 5. Stop poll should be sent to the server, RR should be deactivated and CE should be removed
     *
     * @throws Throwable
     */
    public void testSwitchingToTheTransparentMode_003() throws Throwable {
        final String uri = createTestResourceUri("switching_to_the_transparent_mode_lp");

        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-Sleep", "65").getRequest();

        funcForSwithchingTransparentMode(uri, null, null, request, 45 * 1000, 0, 2, LP_PERIOD);
    }

    /**
     * Pattern [0, 70, 70, 70, 70]
     *
     * 1. RI-based pattern should be detected after 3-rd request and polling should start after receiving response.
     * 2. 4th request should be HITed
     * 3. Policy should be received and applied.
     * 4. Stop poll should be sent to the server, RR should be deactivated and CE should be removed
     * @throws Throwable
     */
    public void testSwitchingToTheTransparentMode_004() throws Throwable {
        String uri = createTestResourceUri("switching_to_the_transparent_mode_ri");
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        funcForSwithchingTransparentMode(uri, null, null, request, 45 * 1000, 0, 2, RI_PERIOD);
    }

    /**
     * Pattern [0, 40, 40, 40]
     *
     * 1. RI-based pattern should be detected after 3-rd request and polling should start after receiving response.
     * 2. 4th request should be HITed
     * 3. Policy should be received and applied.
     * 4. Stop poll should be sent to the server, RR should be deactivated and CE should be removed
     * 5. Policy should be received and applied.
     * 6. Next response after this should be cached by RFC
     *
     * @throws Throwable
     */
    public void testSwitchingToTheTransparentMode_005() throws Throwable {
        String uri = createTestResourceUri("switching_to_the_transparent_mode_removed_ce");

        String rawHeadersDef = "Content-Encoding: identity" + CRLF
                + "Cache-Control: max-age=" + 30 + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4";
        String encodedRawHeadersDef = base64Encode(rawHeadersDef);

        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        funcForSwithchingTransparentMode(uri, encodedRawHeadersDef, null, request, 45 * 1000, 45 * 1000, 3, 40 * 1000);
    }

    /**
     * A test resource is needed for this test case that returns the same response for all requests, status code for 1st , 5th  responses  should be 200OK, 2nd ? 4th  304 Not Modified for other responses  and all responses should have such headers:
     * Cache-Control: max-age=30
     * Etag: 123
     * Pattern [0, 40, 40, 40, 40, 40]
     *
     * 1. RI-based pattern should be detected after 3-rd request and polling should start after receiving response.
     * 2. 4th request should be HITed
     * 3. Policy should be received and applied.
     * 4. Stop poll should be sent to the server, RR should be deactivated and CE should be removed
     * 5. Policy should be received and applied.
     * 5. Next response after this should be cached by RFC
     * @throws Throwable
     */
    public void testSwitchingToTheTransparentMode_006() throws Throwable {

        final String uri = createTestResourceUri("switching_to_the_transparent_mode_removed_ce_with_etag");

        String rawHeadersDef = "ETag: " + "123" + CRLF
                + "Content-Encoding: identity" + CRLF
                + "Cache-Control: max-age=" + 30 + CRLF
                + "Accept-Ranges: bytes" + CRLF
                + "Content-Length: 4";

        long startTime = System.currentTimeMillis() + 10000;
        String date2 = DateUtil.format(new Date(startTime + 120000));
        String expires = DateUtil.format(new Date(startTime + 100000));

        String expectedNotModified = "Date: " + date2 + CRLF
                + "Expires: " + expires + CRLF
                + "Last-Modified: " + "Wed, 11 Jan 1984 08:00:00 GMT" + CRLF
                + "ETag: " + "123" + CRLF
                + "Cache-Control: max-age=" + 30;

        final String encodedRawHeadersDef = base64Encode(rawHeadersDef);
        final String encodedRawHeadersDef304 = base64Encode(expectedNotModified);

        final HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        funcForSwithchingTransparentMode(uri, encodedRawHeadersDef, encodedRawHeadersDef304, request, 45 * 1000, 45 * 1000, 0, 0);
    }

    //TODO test_007 and test_008 should be refactored and added after revalidation will be enabled
}
