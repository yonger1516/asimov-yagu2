package com.seven.asimov.it.tests.caching.rfc.compliant;


import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.testcases.HttpHeaderStrategyCachingGATestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.date.DateUtil;
import org.apache.http.HttpStatus;

import java.net.URLEncoder;
import java.util.Date;

import static com.seven.asimov.it.base.constants.TFConstantsIF.CRLF;

public class HttpHeaderStrategyCachingGATests extends HttpHeaderStrategyCachingGATestCase {

    /**
     * #1 Req: no; Resp: Age:230, Date
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_001_NoCacheByResponseAge() throws Exception {

        String addHeaders = "Age: 230";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache1", true, "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #5 Req: no; Resp: Age:1, Date, CC max-age=30
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_002_CacheByResponseMaxAge() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=30";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));
        HttpRequest request = createGetRequest("asimov_it_http_cache5", true, "X-OC-AddHeader:" + addHeadersEncoded);
        long start = System.currentTimeMillis();
        // this request shall be cached

        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;

            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }


    /**
     * #5b Req: no; Resp: Age:1, Date, CC max-age=-5
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_003_NoCacheByResponseMaxAgeIsNegative() throws Exception {

        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=-5";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache5b", true, "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #6 Req: CC max-age=30; Resp: Age:1, Date
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_004_CacheByRequestMaxAge() throws Exception {
        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache6", true, "Cache-Control:max-age=30",
                "X-OC-AddHeader:" + addHeadersEncoded);
        HttpResponse httpResponse;
        // this request shall be cached
        int responseId = 0;
        httpResponse = checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            assertTrue("Latency on your network is too high to run this test", latency < 10);
            logSleeping(10 * MILLIS_IN_SECOND - httpResponse.getDuration() - latency);
            // this request shall be returned from cache
            httpResponse = checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #6b Req: CC max-age=incorrect Resp: Age:1, Date
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_005_NoCacheByRequestMaxAgeIsIncorrect() throws Exception {

        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache6b", true, "Cache-Control:max-age=incorrect",
                "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #7 Req: CC max-age=0; Resp: Age:1, Date, CC max-age=30
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_006_NoCacheByRequestMaxAgeIsZeroOverridesResponseMaxAge() throws Exception {

        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=30";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache7", true, "Cache-Control:max-age=0",
                "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #8 Req: CC max-stale=10; Resp: Age:1, Date, Expires=Date+20sec should cache and respond from cache until
     * (freshness + max-stale) > current_age
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_007_CacheByRequestMaxStaleExceedsResponseExpires() throws Exception {
        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache8", true, "Cache-Control:max-stale=10",
                "X-OC-AddHeader_Expires:20", "X-OC-AddHeader:" + addHeadersEncoded);
        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #8a Req: CC max-stale=20; Resp: Age:1, Date, max-age=10
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_008_CacheByRequestMaxStaleResponseMaxAge() throws Exception {

        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=10";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache8a", true, "Cache-Control:max-stale=20",
                "X-OC-AddHeader:" + addHeadersEncoded);
        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;

            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #8b Req: CC max-stale=10; Resp: Date, Expires=Date+20sec should cache and respond from cache until (freshness +
     * max-stale) > current_age
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_009_CacheByRequestMaxStaleExceedsResponseExpiresNoAge() throws Exception {
        HttpRequest request = createGetRequest("asimov_it_http_cache8b", true, "Cache-Control:max-stale=10",
                "X-OC-AddHeader_Expires:20");

        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #8c Req: CC max-stale=30; Resp: Age: 1, Date, Expires=Date should cache and respond from cache until (freshness +
     * max-stale) > current_age
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_010_CacheByRequestMaxStaleResponseExpiresHasPassed() throws Exception {

        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache8c", true, "Cache-Control:max-stale=30",
                "X-OC-AddHeader_Expires:0", "X-OC-AddHeader:" + addHeadersEncoded);

        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #8d Req: CC max-stale=20, max-age=10; Resp: Age:1, Date
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_011_CacheByRequestMaxAgeExceedsMaxStale() throws Exception {

        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache8d", true,
                "Cache-Control:max-stale=20, max-age=10", "X-OC-AddHeader:" + addHeadersEncoded);

        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;

            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }


    /**
     * #8f Req: CC max-stale=30; Resp: Age: 1, Date cache since (freshness + max-stale) > current_age
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_012_CacheByRequestMaxStaleResponseDate() throws Exception {

        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache8f", true, "Cache-Control:max-stale=30",
                "X-OC-AddHeader:" + addHeadersEncoded);
        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #8g Req: CC max-stale=30; Resp: Age: 30, Date, Expires=Date does not cache since (freshness + max-stale) <=
     * current_age
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_013_NoCacheByFreshnessWithMaxStaleLessThanCurrentAge() throws Exception {

        String addHeaders = "Age: 30";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache8g", true, "Cache-Control:max-stale=30",
                "X-OC-AddHeader_Expires:0", "X-OC-AddHeader:" + addHeadersEncoded);

        assertMissMiss2(request);
    }

    /**
     * #8h Req: CC max-age=10, max-stale=20; Resp: Age:40, Date Expect: does not cache since (max-age + max-stale) <=
     * current_age
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_014_NoCacheByRequestMaxAgeMaxStaleLessThanCurrentAge() throws Exception {

        String addHeaders = "Age: 40";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache8h", true,
                "Cache-Control:max-stale=20, max-age=10", "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #8j Req: CC max-stale=20; Resp: Age:40, Date, CC max-age=10 Expect: does not cache since (max-age + max-stale) <=
     * current_age
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_015_NoCacheByRequestMaxStaleResponseMaxAgeLessThanCurrentAge() throws Exception {

        String addHeaders = "Age: 40" + CRLF + "Cache-Control: max-age=10";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache8j", true, "Cache-Control:max-stale=20",
                "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    public void test_016_CacheByRequestMaxStaleResponseExpires() throws Exception {

        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache8l", true, "Cache-Control:max-stale",
                "X-OC-AddHeader_Expires:0", "X-OC-AddHeader:" + addHeadersEncoded);

        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            logSleeping(10 * MILLIS_IN_SECOND);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
    }

    /**
     * #8m Req: CC max-stale; Resp: Age:20, Date, CC max-age=20
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-10s-[HIT]
     */
    public void test_017_CacheByRequestMaxStaleNoValue() throws Exception {

        String addHeaders = "Age: 20" + CRLF + "Cache-Control: max-age=20";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache8m", true, "Cache-Control:max-stale",
                "X-OC-AddHeader:" + addHeadersEncoded);
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 3; i++) {
            logSleeping(10 * MILLIS_IN_SECOND);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
    }

    /**
     * #8n Req: CC max-stale=300; Resp: CC must-revalidate, Age:1, Date, Expires=Date
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_018_NoCacheByResponseMustRevalidateOverridesMaxStale() throws Exception {

        String addHeaders = "Age: 1" + CRLF + "Cache-Control: must-revalidate";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache8n", true, "Cache-Control:max-stale=300",
                "X-OC-AddHeader_Expires:0", "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #8o Req: CC max-stale=300; Resp: CC proxy-revalidate, Age:1, Date, Expires=Date
     * <p/>
     * [MISS]-[HIT]
     * according to new logic, proxy-revalidate directive is ignored by GA
     */
    public void test_019_NoCacheByResponseProxyRevalidateOverridesMaxStale() throws Exception {

        String addHeaders = "Age: 1" + CRLF + "Cache-Control: proxy-revalidate";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache8o", true, "Cache-Control:max-stale=300",
                "X-OC-AddHeader_Expires:0", "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissHit2(request);
    }

    /**
     * #8p Req: CC max-stale=30; Resp: Age:1, Date, CC max-age=0 Expect: caches and responds from cache until (max-age +
     * max-stale) > current_age
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_020_CacheByRequestMaxStaleExtendsResponseMaxAgeZero() throws Exception {

        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=0";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache8p", true, "Cache-Control:max-stale=30",
                "X-OC-AddHeader:" + addHeadersEncoded);

        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;

            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #8r Req: CC max-stale=30, max-age=0; Resp: Age:1, Date Expect: caches and responds from cache until (max-age +
     * max-stale) > current_age
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_021_CacheByRequestMaxStaleExtendsMaxAgeZero() throws Exception {

        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache8r", true, "Cache-Control:max-stale=30, max-age=0",
                "X-OC-AddHeader:" + addHeadersEncoded);

        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);
        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #8s Req: CC max-stale=90; Resp: Date, Expires=Date-1min Expect: caches and responds from cache until (freshness +
     * max-stale) > current_age
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_022_CacheByRequestMaxStaleExtendsResponseExpires() throws Exception {

        HttpRequest request = createGetRequest("asimov_it_http_cache8s", true, "Cache-Control:max-stale=90",
                "X-OC-AddHeader_Expires:-60");
        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #8t Req: CC max-stale=90; Resp: Date, Expires=Date-3min
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_023_NoCacheByRequestMaxStaleDoesNotExceedResponseExpires() throws Exception {
        HttpRequest request = createGetRequest("asimov_it_http_cache8t", true, "Cache-Control:max-stale=90",
                "X-OC-AddHeader_Expires:-180");
        assertMissMiss2(request);
    }

    /**
     * #8u Req: CC max-stale=30; Resp: Age:1, Date, Expires: -1
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_024_NoCacheByResponseExpiresIsIncorrect() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Expires: -1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache8u", true, "Cache-Control:max-stale=30",
                "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #9 Req: CC max-age=35; Resp: Age:1, Date, CC max-age=60
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-16s-[MISS/CACHE]
     */
    public void test_025_CacheByRequestMaxAgeOverridesResponseMaxAge() throws Exception {

        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=60";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache9", true, "Cache-Control:max-age=35",
                "X-OC-AddHeader:" + addHeadersEncoded);
        long start = System.currentTimeMillis();

        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(16 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #9a Req: CC max-age=60; Resp: Age:1, Date, CC max-age=35
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-16s-[MISS/CACHE]
     */
    public void test_026_CacheByResponseMaxAgeOverridesRequestMaxAge() throws Exception {

        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=35";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache9a", true, "Cache-Control:max-age=60",
                "X-OC-AddHeader:" + addHeadersEncoded);
        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(16 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #10 Req: CC min-fresh=15; Resp: Age:1, Date, CC max-age=45
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_027_CacheByRequestMinFreshResponseMaxAge() throws Exception {

        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=45";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache10", true, "Cache-Control:min-fresh=15",
                "X-OC-AddHeader:" + addHeadersEncoded);
        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #10b Req: CC min-fresh=250; Resp: Age:100, Date, CC max-age=260
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_028_NoCacheByRequestMinFreshResponseAgeMaxAge() throws Exception {
        String addHeaders = "Age: 100" + CRLF + "Cache-Control: max-age=260";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache10b", true, "Cache-Control:min-fresh=250",
                "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #10c Req: CC max-age=45, min-fresh=15; Resp: Date, CC max-age=60
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_029_CacheByRequestMaxAgeMinFreshOverridesResponseMaxAge() throws Exception {
        String addHeaders = "Cache-Control: max-age=60";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache10c", true,
                "Cache-Control:max-age=45, min-fresh=15", "X-OC-AddHeader:" + addHeadersEncoded);

        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #10d Req: CC max-age=200, min-fresh=110; Resp: Age: 100, Date, CC max-age=260
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_030_NoCacheByRequestMaxAgeDoesNotMatchMinFresh() throws Exception {
        String addHeaders = "Age: 100" + CRLF + "Cache-Control: max-age=260";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache10d", true,
                "Cache-Control:max-age=200, min-fresh=110", "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #10e Req: CC min-fresh=30; Resp: Age:1, Date, Expires=Date+1min
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_031_CacheByResponseAgeExpiresMatchesRequestMinFresh() throws Exception {

        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache10e", true, "Cache-Control:min-fresh=30",
                "X-OC-AddHeader_Expires:60", "X-OC-AddHeader:" + addHeadersEncoded);

        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #10f Req: CC min-fresh=130; Resp: Age:1, Date, Expires=Date
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_032_NoCacheByResponseExpiresDoesNotMatchMinFresh() throws Exception {

        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache10f", true, "Cache-Control:min-fresh=130",
                "X-OC-AddHeader_Expires:0", "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #10i Req: CC min-fresh=0; Resp: Age:1, Date, CC max-age=30
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_033_CacheByResponseMaxAgeMatchesMinFresh() throws Exception {

        String addHeaders = "Age: 1" + CRLF + "Cache-Control:max-age=30";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache10i", true, "Cache-Control:min-fresh=0",
                "X-OC-AddHeader:" + addHeadersEncoded);
        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #10j_1 Req: CC min-fresh=300; Resp: Age:1, Date
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_034_NoCacheByResponseNotFreshDoesNotMatchMinFresh() throws Exception {
        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache10j_1", true, "Cache-Control:min-fresh=300",
                "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #10j_2 Req: CC min-fresh=-10; Resp: Age:1, Date
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_035_NoCacheByResponseNotFreshMinFreshIsNegative() throws Exception {
        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache10j_2", true, "Cache-Control:min-fresh=-30",
                "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #10j_3 Req: CC min-fresh; Resp: Age:1, Date
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_036_NoCacheByResponseNotFreshMinFreshHasNoValue() throws Exception {
        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache10j_3", true, "Cache-Control:min-fresh",
                "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #10j_4 Req: CC min-fresh=no; Resp: Age:1, Date
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_037_NoCacheByResponseNotFreshMinFreshHasIncorrectValue() throws Exception {
        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache10j_4", true, "Cache-Control:min-fresh=no",
                "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #10k Req: CC max-age=260, min-fresh=90; Resp: Date, CC max-age=90
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_038_NoCacheByResponseMaxAgeDoesNotMatchMinFresh() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Cache-Control:max-age=90";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache10k", true,
                "Cache-Control:max-age=260, min-fresh=90", "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #11 Req: CC public; Resp: Age:1, Date, CC max-age=30
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_039_CacheByResponseMaxAgeIgnoresRequestCCPublic() throws Exception {

        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=30";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache11", true, "Cache-Control:public",
                "X-OC-AddHeader:" + addHeadersEncoded);

        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #11b Req: CC personal; Resp: Age:1, Date, CC max-age=30
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_040_CacheByResponseMaxAgeIgnoresRequestCCPersonal() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=30";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache11b", true, "Cache-Control:personal",
                "X-OC-AddHeader:" + addHeadersEncoded);
        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #11c Req: CC no-cache; Resp: Age:1, Date, CC max-age=260 Expect: should not cache
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_041_NoCacheByRequestNoCache() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=260";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache11c", true, "Cache-Control:no-cache",
                "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #11d Req: CC no-store; Resp: Age:1, Date, CC max-age=260 Expect: should not cache
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_042_NoCacheByRequestNoStore() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=260";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache11d", true, "Cache-Control:no-store",
                "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #11r Req: no; Resp: Age:1, Date, Expires:, CC must-revalidate Expect: should not cache
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_043_NoCacheByResponseDateTZIsIncorrect() throws Exception {
        long now = System.currentTimeMillis();

        String addHeaders = "Age: 1" + CRLF + "Date: " + DateUtil.format(new Date(now), DateUtil.EET_TZ) + CRLF
                + "Cache-Control: must-revalidate" + CRLF + "Expires:";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache11r", false, "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #11i Req: no; Resp: Age:1, Date, Expires:, CC must-revalidate Expect: should not cache
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_044_NoCacheByResponseMustRevalidate() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Cache-Control: must-revalidate" + CRLF + "Expires:";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache11i", true, "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #11j Req: no; Resp: Age:1, Date, Expires:, CC proxy-revalidate Expect: should not cache
     * <p/>
     * [MISS]-[HIT]
     * according to new logic, proxy-revalidate directive is ignored by GA
     */
    public void test_045_NoCacheByResponseProxyRevalidate() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Cache-Control: proxy-revalidate";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache11j", true, "X-OC-AddHeader_Expires:100",
                "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissHit2(request);
    }

    /**
     * #11g Req: no; Resp: Age:1, Date, CC max-age=30, public;
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_046_CacheByResponseMaxAgePublic() throws Exception {

        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=30, public";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache11g", true, "X-OC-AddHeader:" + addHeadersEncoded);
        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #11h Req: no; Resp: Age:1, Date, CC max-age=30, personal;
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_047_CacheByResponseMaxAgePersonal() throws Exception {

        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=30, personal";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache11h", true, "X-OC-AddHeader:" + addHeadersEncoded);
        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #11k Req: no; Resp: Age:1, Date, CC max-age=0
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_048_NoCacheByResponseMaxAgeIsZero() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=0";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache11k", true, "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #12 Req: no; Resp: Age:1, Date, CC max-age=0, Expires:Date+1h
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_049_NoCacheByResponseMaxAgeOverridesExpires() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=0";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12", true, "X-OC-AddHeader_Expires:3600",
                "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #12a Req: no; Resp: Age:1, Date, Expires:Date+1h (1st format: Sat, 08 Oct 2011 10:52:00 GMT)
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-10s-[HIT]
     */
    public void test_050_CacheByResponseExpires() throws Exception {

        long now = System.currentTimeMillis();

        String addHeaders = "Age: 1" + CRLF + "Date: " + DateUtil.format(new Date(now), DateUtil.RFC1123_DATE_FORMAT)
                + CRLF + "Expires: " + DateUtil.format(new Date(now + 3600000), DateUtil.RFC1123_DATE_FORMAT);

        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12a", false, "X-OC-AddHeader:" + addHeadersEncoded);
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 3; i++) {
            logSleeping(10 * MILLIS_IN_SECOND);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
    }

    /**
     * #12b Req: no; Resp: Age:1, Date, Expires:old date (1st format)
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_051_NoCacheByResponseExpires() throws Exception {
        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12b", true, "X-OC-AddHeader_Expires:-3600",
                "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #12c Req: no; Resp: Age:1, Date, Expires:incorrect format (Sat, 08 Oct 2011 18:45:00 EEST)
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_052_NoCacheByResponseExpiresTZInIncorrectFormat() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Expires: Sat, 08 Oct 2011 18:45:00 EEST";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12c", true, "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #12c_1 Req: no; Resp: Age:1, Date, Expires:incorrect format (Mi, 08 Okt 2011 18:45:00 GMT)
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_053_NoCacheByResponseExpiresLocaleInIncorrectFormat() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Expires: Mi, 08 Okt 2011 18:45:00 GMT";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12c_1", true, "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #12d Req: no; Resp: Age:1, Date: incorrect format (e.g. Sat, 08 Oct 2011 16:16:00 EEST), Expires: incorrect
     * format (e.g. Sat, 08 Oct 2011 18:16:00 EEST)
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_054_NoCacheByResponseExpiresAndDateTZInIncorrectFormat() throws Exception {
        long now = System.currentTimeMillis();

        String addHeaders = "Age: 1" + CRLF + "Expires: "
                + DateUtil.format(new Date(now + (2 * 3600)), DateUtil.EET_TZ) + CRLF + "Date: "
                + DateUtil.format(new Date(now), DateUtil.EET_TZ);
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12d", true, "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #12e Req: no; Resp: Age:1, Date, Expires:incorrect format (08-10-2011 16:00:00 GMT)
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_055_NoCacheByResponseExpiresInIncorrectFormat() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Expires: 08-10-2011 16:00:00 GMT";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12e", true, "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #12f Req: no; Resp: Age:1, Date, Expires:incorrect format (0)
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_056_NoCacheByResponseExpiresIsZero() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Expires: 0";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12f", true, "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #12g Req: no; Resp: Age:1, Date, Expires:incorrect format (-1)
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_057_NoCacheByResponseExpiresIsNegative() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Expires: -1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12g", true, "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #12h Req: no; Resp: Age:1, Date, Expires:incorrect format (already expired)
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_058_NoCacheByResponseExpiresIsText() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Expires: already expired";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12h", true, "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #12i Req: no; Resp: Age:1, Date, Expires:Date+1h, CC no-store;
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_059_NoCacheByResponseNoStoreOverridesExpires() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Cache-Control: no-store";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12i", true, "X-OC-AddHeader_Expires:3600",
                "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #12j Req: CC no-store; Resp: Age:1, Date, Expires:Date+1h
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_060_NoCacheByRequestNoStoreOverridesResponseExpires() throws Exception {
        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12j", true, "Cache-Control:no-store",
                "X-OC-AddHeader_Expires:3600", "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #12k Req: no; Resp: Age:1, Date, Expires:Date+1h, CC no-cache;
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_061_NoCacheByResponseNoCacheOverridesExpires() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Cache-Control: no-cache";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12k", true, "X-OC-AddHeader_Expires:3600",
                "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #12l Req: no; Resp: Age:1, Date, Expires:Date+1h, CC no-cache;
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_062_NoCacheByResponseNoCacheFields() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Cache-Control: no-cache=\"X-OC-AddHeader_Date\"";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12l", true, "X-OC-AddHeader_Expires:3600",
                "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #12m Req: CC no-cache="X-OC-AddHeader_Date"; Resp: Age:1, Date, Expires:Date+1h;
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_063_NoCacheByRequestNoCacheFields() throws Exception {
        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12m", true,
                "Cache-Control:no-cache=\"X-OC-AddHeader_Date\"", "X-OC-AddHeader_Expires:3600", "X-OC-AddHeader:"
                + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #12n Req: no; Resp: Age:1, Date, Expires:Date+1h (2nd format: Wednesday, 10-Oct-12 10:40:00 GMT)
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-10s-[HIT]
     */
    public void test_064_CacheByResponseExpiresRFC850Format() throws Exception {
        long now = System.currentTimeMillis();

        String addHeaders = "Age: 1" + CRLF + "Date: " + DateUtil.format(new Date(now), DateUtil.RFC1123_DATE_FORMAT)
                + CRLF + "Expires: " + DateUtil.format(new Date(now + 3600000), "EEEE, dd-MMM-yy HH:mm:ss z");

        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12n", false, "X-OC-AddHeader:" + addHeadersEncoded);
        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 3; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
    }

    /**
     * #12nb Req: no; Resp: Age:1, Date, Expires:Date+1h (3rd format: Wed Nov 10 10:40:00 2012)
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-10s-[HIT]
     */
    public void test_065_CacheByResponseExpiresInAsctimeFormat() throws Exception {
        long now = System.currentTimeMillis();

        String addHeaders = "Age: 1" + CRLF + "Date: " + DateUtil.format(new Date(now), DateUtil.RFC1123_DATE_FORMAT)
                + CRLF + "Expires: " + DateUtil.formatAscTime(new Date(now + 3600000));

        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12nb", false, "X-OC-AddHeader:" + addHeadersEncoded);
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 3; i++) {
            logSleeping(10 * MILLIS_IN_SECOND);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
    }

    /**
     * #12o Req: no; Resp: Age:1, Date: far in the future (Sat, 08 Oct 3000 16:16:00 GMT)
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_066_NoCacheByResponseDateFarInFutureAndNotEnoughHeaders() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Date: Sat, 08 Oct 3000 16:16:00 GMT";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12o", false, "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #12r Req: no; Resp: Age:1, Date, Expires: far in the past
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_067_NoCacheByResponseExpiresFarInThePast() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Expires: Sat, 08 Oct 1000 16:16:00 GMT";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache12r", true, "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #13 Req: Pragma: no-cache; Resp: Age:1, Date, Expires:Date+1h
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_068_NoCacheByPragmaNoCacheIgnoresResponseExpires() throws Exception {
        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache13", true, "Pragma:no-cache",
                "X-OC-AddHeader_Expires:3600", "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #13b Req: Pragma: no-cache; Resp: Age:1, Date, CC max-age=260
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_069_NoCacheByPragmaNoCacheIgnoresResponseMaxAge() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Cache-Control:max-age=260";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache13b", true, "Pragma:no-cache", "X-OC-AddHeader:"
                + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #13i Req: CC max-age=300, Pragma: no-cache; Resp: Age:1, Date, Expires: Date+30s
     * <p/>
     * [MISS/MISS]-[MISS/MISS]
     */
    public void test_070_NoCacheByRequestPragmaNoCacheIgnoresResponseExpiresAndRequestMaxAge() throws Exception {
        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache13i", true, "Cache-Control:max-age=300",
                "Pragma:no-cache", "X-OC-AddHeader_Expires:30", "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #13j Req: CC max-age=30, Pragma: no-cache; Resp: Age:1, Date
     * <p/>
     * [MISS/MISS]-[MISS/MISS]
     */
    public void test_071_NoCacheByRequestPragmaNoCacheIgnoresMaxAge() throws Exception {
        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache13j", true, "Cache-Control:max-age=30",
                "Pragma:no-cache", "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #14b Req: no; Resp: Age:1, Date, CC max-age=260, Expires:Date
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-10s-[HIT]
     */
    public void test_072_CacheByResponseMaxAgeIgnoresDateExpires() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=260";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache14b", true, "X-OC-AddHeader_Expires:0",
                "X-OC-AddHeader:" + addHeadersEncoded);
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 3; i++) {
            logSleeping(10 * MILLIS_IN_SECOND);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
            ;
        }
    }

    /**
     * #16 Req: CC no-cache; Resp: Age:1, Date, Expires:Date+1h
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_073_NoCacheByRequestNoCacheOverridesExpires() throws Exception {
        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache16", true, "Cache-Control:no-cache",
                "X-OC-AddHeader_Expires:3600", "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #16b Req: CC no-cache="X-OC-AddHeader_Date"; Resp: Age:1, Date, CC max-age=260
     * <p/>
     * [MISS]-[MISS]
     */
    public void test_074_NoCacheByRequestNoCacheWithValueOverridesResponseMaxAge() throws Exception {
        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=260";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache16b", true,
                "Cache-Control:no-cache=\"X-OC-AddHeader_Date\"", "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * #17 Req: no; Resp: Age:0, Date, CC max-age=30
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_075_CacheByResponseMaxAgeExceedsAge() throws Exception {
        String addHeaders = "Age: 0" + CRLF + "Cache-Control: max-age=30";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache17", true, "X-OC-AddHeader:" + addHeadersEncoded);
        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #17b Req: no; Resp: Age:incorrect, Date, CC max-age=30
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_076_NoCacheByResponseMaxAgeIgnoreIncorrectAge() throws Exception {
        String addHeaders = "Age: incorrect" + CRLF + "Cache-Control: max-age=30";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache17b", true, "X-OC-AddHeader:" + addHeadersEncoded);
        long start = System.currentTimeMillis();

        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(16 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
    }

    /**
     * #17h Req: no; Resp: Date, CC max-age=30 Expect: caches and responds
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_077_CacheByResponseMaxAgeNoAge() throws Exception {
        String addHeaders = "Cache-Control: max-age=30";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache17h", true, "X-OC-AddHeader:" + addHeadersEncoded);
        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId);
        ;
    }

    /**
     * #17i Req: no; Resp: Age:999999999999999999999999999999999999999999, Date, CC max-age=260
     * <p/>
     * [MISS]-[MISS]
     */
    @LargeTest
    public void test_078_NoCacheByAgeExceedsMaxInt() throws Exception {
        String addHeaders = "Age: 999999999999999999999999999999999999999999" + CRLF + "Cache-Control: max-age=260";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createGetRequest("asimov_it_http_cache17i", true, "X-OC-AddHeader:" + addHeadersEncoded);
        assertMissMiss2(request);
    }

    /**
     * hs_freshness_post3 Req: no; Resp: Age:1, Date, CC max-age=30 Expect: caches for max-age period
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[HIT]
     */
    public void test_079_CacheByMethodPostResponseMaxAge() throws Exception {

        String addHeaders = "Age: 1" + CRLF + "Cache-Control: max-age=60";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createPostRequest("asimov_it_http_cache_post3", true, "X-OC-AddHeader:"
                + addHeadersEncoded, "Content-Length:0");
        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);

            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);

        // this request shall be served from cache
        checkHit(request, ++responseId);
    }

    /**
     * hs_freshness_post4 Req: no; Resp: Age:1, Date, Expires:Date+30s Expect: caches for Expires period
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[HIT]
     */
    public void test_080_CacheByMethodPostResponseExpires() throws Exception {

        String addHeaders = "Age: 1";
        String addHeadersEncoded = URLEncoder.encode(Base64.encodeToString(addHeaders.getBytes(), Base64.DEFAULT));

        HttpRequest request = createPostRequest("asimov_it_http_cache_post4", true, "X-OC-AddHeader_Expires:60",
                "X-OC-AddHeader:" + addHeadersEncoded, "Content-Length:0");

        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from cache
        checkHit(request, ++responseId);
    }

    /**
     * hs_freshness_post_ma410 Req: no; Resp: Age:1, Date, CC max-age=30
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-15s-[MISS/CACHE]
     */
    public void test_081_CacheByMethodPostCode410ResponseMaxAge() throws Exception {
        final String path = "asimov_it_http_cache_post_ma410";
        PrepareResourceUtil.prepareResource(createTestResourceUri(path), false);

        long now = System.currentTimeMillis();
        String expected = "HTTP/1.1 410 Gone" + CRLF + "Connection: close" + CRLF + "Age: 1" + CRLF + "Date: "
                + DateUtil.format(new Date(now)) + CRLF + "Cache-Control: max-age=30" + CRLF + "Content-Length: 0"
                + CRLF + CRLF;

        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));

        HttpRequest request = createRequest(METHOD_POST, createTestResourcePath(path), false, "X-OC-Raw:" + encoded, "Content-Length:0");

        long start = System.currentTimeMillis();
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId, HttpStatus.SC_GONE, null);

        for (int i = 0; i < 2; i++) {
            long delay = System.currentTimeMillis() - start;
            logSleeping((i + 1) * 10 * MILLIS_IN_SECOND - delay);
            // this request shall be returned from cache
            checkHit(request, ++responseId, HttpStatus.SC_GONE, null);
        }
        logSleeping(15 * MILLIS_IN_SECOND);
        // this request shall be served from network
        checkMiss(request, ++responseId, HttpStatus.SC_GONE, null);

        // there should be a network activity
    }

    /**
     * hs_freshness_post_exp410 Req: no; Resp: Age:1, Date, Expires: Date+1d
     * <p/>
     * [MISS/CACHE]-10s-[HIT]-10s-[HIT]-10s-[HIT]
     */
    //   Has bug - The code -1 is received instead of 200 at the first request
    public void test_082_CacheByMethodPostCode410ResponseExpires() throws Exception {
        final String path = "asimov_it_http_cache_post_exp410";
        PrepareResourceUtil.prepareResource(createTestResourceUri(path), false);

        long now = System.currentTimeMillis();
        String expected = "HTTP/1.1 410 Gone" + CRLF + "Connection: close" + CRLF + "Age: 1" + CRLF + "Date: "
                + DateUtil.format(new Date(now)) + CRLF + "Expires: " + DateUtil.format(new Date(now + 86400))
                + CRLF + "Content-Length: 0" + CRLF + CRLF;

        String encoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
        HttpRequest request = createRequest(METHOD_POST, createTestResourcePath(path), false, "X-OC-Raw:" + encoded, "Content-Length: 0");
        // this request shall be cached
        int responseId = 0;
        checkMiss(request, ++responseId, HttpStatus.SC_GONE, null);

        for (int i = 0; i < 3; i++) {
            logSleeping(10 * MILLIS_IN_SECOND);
            // this request shall be returned from cache
            checkHit(request, ++responseId, HttpStatus.SC_GONE, null);
        }
    }
}
