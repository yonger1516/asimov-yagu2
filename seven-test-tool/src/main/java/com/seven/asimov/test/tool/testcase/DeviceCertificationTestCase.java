package com.seven.asimov.test.tool.testcase;

import android.util.Log;
import com.seven.asimov.it.base.*;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DeviceCertificationTestCase extends TcpDumpTestCase {

    private static final String TAG = DeviceCertificationTestCase.class.getSimpleName();
    private static final String HTTPS_URI_SCHEME = "https://";

    private static Random random = new Random();

    private long sleepTime = 10 * 1000;
    private javax.net.ssl.SSLSocketFactory sslSocketFactory;

    protected static final String[] S = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q",
            "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
            "Z", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
    protected final long ONE_MIN = 60 * 1000;
    protected final int NORMAL_SIZE = 25600;
    protected final int MAX_SIZE = 524288;
    protected final int STREAM_SIZE = 10 * 1024;
    protected final int SLEEP_PERIOD = 15 * 1000;
    protected final int SLEEP_PERIOD_RFS = SLEEP_PERIOD / 3;
    protected final int TIME_OUT = 15 * 60 * 1000;
    protected final int[] SIZE = {1, 10, 100, 1000, 10000};
    protected final int[] resourceSize = {1, 256, 512, 1024};
    private final String RESOURCE_URI = "asimov_it_performance_cpu_mem";

    protected List<Throwable> exceptions = new ArrayList<Throwable>();

    protected void stabilitySimulation(String name, int[] stepTime, String uri, boolean isSizeLoad) throws Throwable {
        long startTime;
        String tag = "MemoryLeak" + name;
        int id = 1;
        PrepareResourceUtil.prepareResource(uri, false);
        startTime = System.currentTimeMillis();
        id = stepSimulation(tag, 1, id, startTime, stepTime[0], uri, isSizeLoad);
        startTime = System.currentTimeMillis();
        id = stepSimulation(tag, 2, id, startTime, stepTime[1], uri, isSizeLoad);
        startTime = System.currentTimeMillis();
        stepSimulation(tag, 3, id, startTime, stepTime[2], uri, isSizeLoad);
    }

    protected int stepSimulation(String tag, int stepID, int requestId, long startTime, int stepTime, String uri,
                                 boolean isSizeLoad) throws Throwable {
        HttpRequest request;
        HttpResponse response;
        int id = requestId;
        int temp = stepID == 2 ? 10 : 1;
        while (true) {
            for (int i = 1; i <= temp; i++) {
                try {
                    request = createRequest().setUri(uri).setMethod("GET")
                            .addHeaderField("X-OC-ContentEncoding", "identity")
                            .addHeaderField("Random", generationRandomHeader())
                            .getRequest();
                    if (isSizeLoad)
                        request.addHeaderField(new HttpHeaderField("X-OC-ResponseContentSize", (stepID != 2 ? NORMAL_SIZE : MAX_SIZE) + ",c"));
                    response = sendMiss(id++, request);
                    logSleeping(sleepTime / temp - response.getDuration());
                } catch (SocketTimeoutException ex) {
                    ex.printStackTrace();
                }
                if (System.currentTimeMillis() - startTime >= stepTime * ONE_MIN) {
                    break;
                }
            }
            if (System.currentTimeMillis() - startTime >= stepTime * ONE_MIN) {
                break;
            }
        }
        return id;
    }

    protected static String generationRandomHeader() {
        char[] sb = new char[30];
        for (int i = 0; i < 30; i++) {
            sb[i] = S[random.nextInt(S.length - 1)].charAt(0);
        }
        return String.valueOf(sb);
    }

    protected HttpResponse sendMiss(int requestId, HttpRequest request) throws IOException, URISyntaxException {
        return sendMiss(request, requestId, false, TIMEOUT);
    }

    protected HttpResponse sendMiss(HttpRequest request, int requestId, boolean keepAlive, int timeout)
            throws IOException, URISyntaxException {
        boolean isSslModeOn = false;
        String uri = request.getUri();
        if (uri.startsWith(HTTPS_URI_SCHEME)) isSslModeOn = true; // we are going to send HTTPS request
        logRequest(requestId, uri);
        HttpResponse response;
        long startTime = System.currentTimeMillis();
        Log.w(TAG, "Session start:" + startTime);
        try {
            if (isSslModeOn) {
                response = sendHttpsRequest(request, this);
            } else {
                response = sendRequest2(request, keepAlive, false, timeout);
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            response = new HttpResponse();
        }
        long endTime = System.currentTimeMillis();
        Log.w(TAG, "Session end:" + endTime);
        logResponse(requestId, ResponseLocation.NETWORK, response);
        return response;
    }

    protected void execute(int sizeResponseBody, boolean isSSL) throws Throwable {
        String uri1 = createTestResourceUri(RESOURCE_URI + "1_" + sizeResponseBody, isSSL);
        final HttpRequest request1 = createRequest()
                .setUri(uri1)
                .setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", sizeResponseBody + ",c").getRequest();

        String uri2 = createTestResourceUri(RESOURCE_URI + "2_" + sizeResponseBody, isSSL);
        final HttpRequest request2 = createRequest()
                .setUri(uri2)
                .setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", sizeResponseBody + ",c").getRequest();

        String uri3 = createTestResourceUri(RESOURCE_URI + "3_" + sizeResponseBody, isSSL);
        final HttpRequest request3 = createRequest()
                .setUri(uri3)
                .setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-ContentEncoding", "identity")
                .addHeaderField("X-OC-ResponseContentSize", sizeResponseBody + ",c").getRequest();

        TestCaseThread t1 = new TestCaseThread() {
            public void run() throws Throwable {
                sendRequests(request1, SLEEP_PERIOD, 4);
            }
        };

        TestCaseThread t2 = new TestCaseThread(10 * 1000) {
            public void run() throws Throwable {
                sendRequests(request2, SLEEP_PERIOD, 4);
            }
        };

        TestCaseThread t3 = new TestCaseThread(20 * 1000) {
            public void run() throws Throwable {
                sendRequests(request3, SLEEP_PERIOD, 4);
            }
        };

        try {
            executeThreads(TIME_OUT, t1, t2, t3);
        } finally {
            // invalidate resource to stop server polling
            PrepareResourceUtil.invalidateResourceSafely(uri1);
            PrepareResourceUtil.invalidateResourceSafely(uri2);
            PrepareResourceUtil.invalidateResourceSafely(uri3);
        }
    }

    protected void sendRequests(HttpRequest request, int sleepPeriod, int countRequests) throws Exception {
        HttpResponse response;
        for (int i = 0; i < countRequests; i++) {
            response = sendMiss(i + 1, request);
            logSleeping(sleepPeriod - response.getDuration());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void setUp() throws Exception {
        SSLContext mSslContext = SSLContext.getInstance("SSL");
        TrustManager[] tm = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }};
        mSslContext.init(null, tm, null);
        sslSocketFactory = mSslContext.getSocketFactory();
        Class c = Class.forName(org.apache.http.conn.ssl.SSLSocketFactory.class.getName());
        Constructor<SSLSocketFactory> con = c.getConstructor(javax.net.ssl.SSLSocketFactory.class);
        SSLSocketFactory apacheSslSocketFactory = con.newInstance(sslSocketFactory);
        apacheSslSocketFactory.setHostnameVerifier(new AllowAllHostnameVerifier());
    }

    @Override
    public void decorate(HttpURLConnection conn) throws IOException {
        if (conn instanceof HttpsURLConnection) {
            Log.i(TAG, "Https decorator applied");
            HttpsURLConnection httpsConnection = (HttpsURLConnection) conn;
            httpsConnection.setSSLSocketFactory(sslSocketFactory);
            httpsConnection.setHostnameVerifier(new AllowAllHostnameVerifier());
        }
    }
}
