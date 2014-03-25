package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import java.util.Random;

public class HeuristicCachingTestCase extends TcpDumpTestCase {

    protected final int sleepTime = 50 * 1000;
    protected final int responseDelay = 40;
    protected int requestId;
    protected final static String RESOURCE = "heuristic_anti_cache_defeat";
    protected final static int FIRST_ASCII_LETTER_CODE = 65;
    protected final static int ASCII_LETTERS_RANGE = 25;
    protected final static int REQUEST_INTERVAL = 30 * 1000;
    protected final static Random random = new Random();

    protected final static HttpRequest request = createRequest()
            .setMethod("GET")
            .addHeaderField("X-OC-ContentEncoding", "identity")
            .getRequest();

    protected void heuristicPatternMatcher(String uri, boolean allHttp, boolean sameMethod, boolean sameHost,
                                           boolean fourthHit, long responseDelay, int sleepTime) throws Exception {
        String URI_SCHEME_HTTP = "http://";
        String URI_SCHEME_HTTPS = "https://";
        String host1 = "tln-dev-testrunner1.7sys.eu/";
        String host2 = "hki-dev-testrunner2.7sys.eu/";

        String resourcePath = createTestResourcePath(uri);

        String uri1 = URI_SCHEME_HTTP + host1 + addUriParameters(resourcePath);
        String uri2 = (allHttp ? URI_SCHEME_HTTP : URI_SCHEME_HTTPS) + (sameHost ? host1 : host2) + addUriParameters(resourcePath);
        String uri3 = URI_SCHEME_HTTP + host1 + addUriParameters(resourcePath);
        String uri4 = (allHttp ? URI_SCHEME_HTTP : URI_SCHEME_HTTPS) + (sameHost ? host1 : host2) + addUriParameters(resourcePath);

        HttpRequest request1 = createRequest().setUri(uri1).addHeaderField("X-OC-Encoding", "identity").getRequest();
        HttpRequest request2 = createRequest().setUri(uri2).addHeaderField("X-OC-Encoding", "identity")
                .setMethod(sameMethod ? HttpGet.METHOD_NAME : HttpPost.METHOD_NAME).getRequest();
        HttpRequest request3 = createRequest().setUri(uri3).addHeaderField("X-OC-Encoding", "identity").getRequest();
        HttpRequest request4 = createRequest().setUri(uri4).addHeaderField("X-OC-Encoding", "identity")
                .setMethod(sameMethod ? HttpGet.METHOD_NAME : HttpPost.METHOD_NAME).getRequest();
        try {
            PrepareResourceUtil.prepareResource(uri1, false, responseDelay);
            PrepareResourceUtil.prepareResource(uri2, false, responseDelay);
            PrepareResourceUtil.prepareResource(uri3, false, responseDelay);
            PrepareResourceUtil.prepareResource(uri4, false, responseDelay);

            checkMiss(request1, ++requestId, VALID_RESPONSE, sleepTime); //R.1
            checkMiss(request2, ++requestId, VALID_RESPONSE, sleepTime); //R.2
            checkMiss(request3, ++requestId, VALID_RESPONSE, sleepTime); //R.3
            if (fourthHit) {
                checkHit(request4, ++requestId, VALID_RESPONSE); //R.4
            } else {
                checkMiss(request4, ++requestId, VALID_RESPONSE); //R.4
            }
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri1);
            PrepareResourceUtil.invalidateResourceSafely(uri2);
            PrepareResourceUtil.invalidateResourceSafely(uri3);
            PrepareResourceUtil.invalidateResourceSafely(uri4);
        }
    }

    private String addUriParameters(String partUri) {
        StringBuilder uri = new StringBuilder();
        int param = (int) (Math.random() * 10000);
        uri.append(partUri).append("/some_file.db?param1=13").append("&param2=").append(param);
        return uri.toString();
    }

    protected void testHeuristicAntiCacheDefeatRecognition(String resourceUri, String resourcePattern, String addition) throws Exception {

        try {
            PrepareResourceUtil.prepareResource(resourceUri, false);
            for (int i = 1; i <= 6; i++) {
                request.setUri(String.format(resourcePattern, String.valueOf(random.nextInt(100))
                        + (char) (random.nextInt(ASCII_LETTERS_RANGE) + FIRST_ASCII_LETTER_CODE)));
                checkMiss(request, i);
                if (i < 6) {
                    TestUtil.sleep(REQUEST_INTERVAL);
                }
            }
        } finally {
            PrepareResourceUtil.prepareResource(resourceUri, true);
        }
    }
}
