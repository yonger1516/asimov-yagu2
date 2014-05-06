package com.seven.asimov.it.base;

import android.content.Context;
import android.test.AndroidTestCase;
import com.seven.asimov.it.IntegrationTestRunnerGa;
import com.seven.asimov.it.base.HttpRequest.Builder;
import com.seven.asimov.it.base.constants.BaseConstantsIF;
import com.seven.asimov.it.base.interfaces.HttpUrlConnectionIF;
import com.seven.asimov.it.utils.IOUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.date.DateUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.conn.params.ConnManagerParamBean;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.security.cert.Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import static com.seven.asimov.it.base.constants.BaseConstantsIF.CRLF;

public class AsimovTestCase extends AndroidTestCase {

    public enum Body {
        BODY, NOBODY, BODY_HASH
    }

    private static long requestNumber = 1;

    protected static int TIMEOUT = 5 * 60 * 1000;
    protected static int SMALL_TIMEOUT = 1 * 60 * 1000;
    protected static final int MAX_BODY_SIZE = 5 * 1024;
    protected static final int DEFAULT_READ_BUFFER = 16384;
    protected static final int SLEEP_AFTER_HANDSHAKE = 0;
    protected static final int DEFAULT_PORT_VALUE = 0;
    public static long latency = 0;

    private static String testResourcePathStart = "";
    public static String TEST_RESOURCE_HOST = "tln-dev-testrunner1.7sys.eu";
    protected static String URI_SCHEME_HTTP = "http://";
    protected static String URI_SCHEME_HTTPS = "https://";
    protected static String TEST_RESOURCE_OWNER = "asimov_it";

    protected static Map<String, Long> lastRequestTime = new HashMap<String, Long>();

    private static final ReentrantLock mLock = new ReentrantLock();

    private static final Logger logger = LoggerFactory.getLogger(AsimovTestCase.class.getSimpleName());

    public enum ThreadStopMode {
        NONE,
        INTERRUPT,
        INTERRUPT_SOFTLY
    }

    public static enum HTTP_VERSION {
        HTTP11,
        HTTP10,
        HTTP21
    }

    public static final HTTP_VERSION VERSION = HTTP_VERSION.HTTP11;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    private static String getTestResourcePathStart() {
        String format = "_yyyy-MM-dd_HH-mm-ss_";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        UUID guid = UUID.randomUUID();
        return guid.toString() + sdf.format(new Date());
    }


    public static String createTestResourceUriNoPathChange(String path) {
        StringBuilder uri = new StringBuilder();
        uri.append(URI_SCHEME_HTTP).append(TEST_RESOURCE_HOST).append("/").append(path);
        return uri.toString();
    }

    public static String createTestResourceCustomUri(String host, String path) {
        StringBuilder uri = new StringBuilder();
        uri.append(URI_SCHEME_HTTP).append(host).append("/").append(createTestResourcePath(path));
        return uri.toString();
    }

    public static String createTestResourceCustomUri(String host, String pathEnd, boolean useSSL) {
        StringBuilder uri = new StringBuilder();
        uri.append(useSSL ? URI_SCHEME_HTTPS : URI_SCHEME_HTTP).append(host)
                .append("/").append(createTestResourcePath(pathEnd));
        return uri.toString();
    }

    public static String createTestResourceUri(String pathEnd) {
        return createTestResourceUri(pathEnd, false);
    }

    public static String createTestResourceUri(String pathEnd, boolean useSSL) {
        StringBuilder uri = new StringBuilder();
        uri.append(useSSL ? URI_SCHEME_HTTPS : URI_SCHEME_HTTP)
                .append(TEST_RESOURCE_HOST).append("/").append(createTestResourcePath(pathEnd));
        return uri.toString();
    }

    public static String createTestResourceUri(String pathEnd, boolean useSSL, int port) {
        StringBuilder uri = new StringBuilder();
        uri.append(useSSL ? URI_SCHEME_HTTPS : URI_SCHEME_HTTP)
                .append(TEST_RESOURCE_HOST).append(":").append(String.valueOf(port)).append("/")
                .append(createTestResourcePath(pathEnd));
        return uri.toString();
    }

    public static String createTestResourcePath(String pathEnd) {
        StringBuilder uri = new StringBuilder();
        uri.append(getTestResourcePathStart()).append("_").append(pathEnd);
        return uri.toString();
    }

    public static Builder createRequest() {
        return Builder.create();
    }

    public static HttpResponse sendRequest(HttpRequest request) throws MalformedURLException, IOException,
            URISyntaxException {
        return sendRequest(request, null, false, false, Body.BODY);
    }

    public static HttpResponse sendRequest(HttpRequest request, boolean keepAlive) throws MalformedURLException,
            IOException, URISyntaxException {
        return sendRequest(request, null, keepAlive, false, Body.BODY);
    }

    public static HttpResponse sendRequest(HttpRequest request, boolean keepAlive, int timeout)
            throws MalformedURLException, IOException, URISyntaxException {
        return sendRequest(request, null, keepAlive, false, Body.BODY, timeout, null);
    }

    public static HttpResponse sendRequest(HttpRequest request, boolean keepAlive, Body bodyType)
            throws MalformedURLException, IOException, URISyntaxException {
        return sendRequest(request, null, keepAlive, false, bodyType);
    }

    public static HttpResponse sendRequest(HttpRequest request, HttpUrlConnectionIF decorator)
            throws MalformedURLException, IOException, URISyntaxException {
        return sendRequest(request, decorator, false, false, Body.BODY);
    }

    public static HttpResponse sendRequest(HttpRequest request, boolean keepAlive, boolean bypassProxy)
            throws MalformedURLException, IOException, URISyntaxException {
        return sendRequest(request, null, keepAlive, bypassProxy, Body.BODY);
    }

    public static HttpResponse sendRequest(HttpRequest request, HttpUrlConnectionIF decorator,
                                           boolean keepAlive, boolean bypassProxy, Body bodyType) throws MalformedURLException, IOException,
            URISyntaxException {
        return sendRequest(request, decorator, keepAlive, bypassProxy, bodyType, TIMEOUT, null);
    }


    public static HttpResponse sendRequest(HttpRequest request, HttpUrlConnectionIF decorator,
                                           boolean keepAlive, boolean bypassProxy, Body bodyType, int timeOut, ThreadLocker locker)
            throws MalformedURLException, IOException, URISyntaxException {
        return sendRequest(request, decorator, keepAlive, bypassProxy, bodyType, timeOut, locker, true);
    }

    public static HttpResponse sendRequestWithoutLogging(HttpRequest request)
            throws MalformedURLException, IOException, URISyntaxException {
        return sendRequest(request, null, false, true, Body.BODY, TIMEOUT, null, false);
    }

    public static HttpResponse sendRequest(HttpRequest request, HttpUrlConnectionIF decorator,
                                           boolean keepAlive, boolean bypassProxy, Body bodyType, int timeOut,
                                           ThreadLocker locker, boolean enableLogging)
            throws MalformedURLException, IOException, URISyntaxException {
        DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        Date serverDate = null;
        Calendar actualDate = null;
        latency = 0;

        HttpURLConnection conn = null;
        InputStream connIn = null;
        OutputStream connOut = null;
        try {
            String replacedUri = null;
            if (bypassProxy) {
                // To send request directly, change protocol to http and port to 8099 (this port is ignored by OC)
                URI uri = new URI(request.getUri());
                replacedUri = new URI("http", uri.getUserInfo(), uri.getHost(), 8099, uri.getPath(), uri.getQuery(),
                        uri.getFragment()).toString();
            }

            if (enableLogging) {
                logger.info("Sending request nr. ");
                logger.info(String.valueOf(requestNumber));
                logger.info(bypassProxy ? " directly:" : " through OC:");
            }

            long reqNum = requestNumber;
            ++requestNumber;

            if (enableLogging) {
                if (replacedUri != null) logger.info("URI is replaced to: " + replacedUri);
                logger.info(request.getFullRequest());
            }

            long start = System.currentTimeMillis();
            HttpURLConnection.setFollowRedirects(request.getFollowRedirects());

            conn = (HttpURLConnection) new URL(replacedUri == null ? request.getUri() : replacedUri).openConnection();

            for (HttpHeaderField field : request.getHeaderFields()) {
                conn.addRequestProperty(field.getName(), field.getValue());
            }

            conn.setConnectTimeout(timeOut);
            conn.setReadTimeout(timeOut);

            conn.setRequestMethod(request.getMethod());
            conn.addRequestProperty("Connection", keepAlive ? "keep-alive" : "close");

            if (request.getChunkSize() > 0) {
                conn.setChunkedStreamingMode(request.getChunkSize());
            }

            if (decorator != null) {
                decorator.decorate(conn);
            }

            if (request.getBody() != null) {
                conn.setDoOutput(true);
                conn.addRequestProperty("Content-Type", "text/html");
                connOut = conn.getOutputStream();
                connOut.write(request.getBody().getBytes());
                connOut.flush();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int bodyLength = -1;
            byte[] bodyHash = null;
            boolean bodySkipped = (bodyType != Body.BODY);
            Certificate[] certificates = null;
            String sslCipherSuite = null;

            try {
                conn.getResponseCode();
                if (locker != null)
                    locker.unlock();
                connIn = conn.getInputStream();
                actualDate = Calendar.getInstance();
                if (bodyType == Body.BODY) {
                    IOUtil.transfer(connIn, baos, new byte[1024]);
                } else if (bodyType == Body.BODY_HASH) {
                    StreamedHash sh = new StreamedHash();
                    bodyLength = 0;
                    byte[] buffer = new byte[1024 * 5];
                    while (true) {
                        int read = connIn.read(buffer);
                        if (read < 0)
                            break;
                        bodyLength += read;
                        sh.append(buffer, 0, read);
                    }
                    bodyHash = sh.getHash();
                } else {
                    // just a read
                    bodyLength = 0;
                    byte[] buffer = new byte[1024 * 5];
                    while (true) {
                        int read = connIn.read(buffer);
                        if (read < 0)
                            break;
                        bodyLength += read;
                    }
                }

                if (conn instanceof HttpsURLConnection) {
                    HttpsURLConnection httpsConn = (HttpsURLConnection) conn;
                    certificates = httpsConn.getServerCertificates();
                    sslCipherSuite = httpsConn.getCipherSuite();
                }

            } catch (IOException e) {
                logger.error(ExceptionUtils.getFullStackTrace(e));
            }

            boolean isImageBody = false;
            if (conn.getContentType() != null && conn.getContentType().contains("image")) {
                isImageBody = true;
            }

            HttpResponse response = HttpResponse.Builder.create().setStatusCode(conn.getResponseCode())
                    .setStatusLine(conn.getHeaderField(0)).setBody(isImageBody ? "Image body" : baos.toString())
                    .setDuration(System.currentTimeMillis() - start).setBodyHash(bodyHash).setBodyLength(bodyLength)
                    .setBodySkipped(bodySkipped).setCertificates(certificates).setCipherSuite(sslCipherSuite)
                    .getResponse();

            response.setStartTime(start);

            for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
                for (String value : entry.getValue()) {
                    /*
                    * if("Content-Length".equals(entry.getKey()) && bodyType == Body.NOBODY){ value = "0"; }
                    */
                    response.addHeaderField(new HttpHeaderField(entry.getKey(), value));
                    if (entry.getKey() != null && value != null && entry.getKey().equals("Date")) {
                        try {
                            serverDate = dateFormat.parse(value);
                        } catch (ParseException e) {
                            e.getStackTrace();
                        }
                    }
                }
            }
            if (enableLogging) {
                logger.info("Response for request nr. " + reqNum + "  " + request.getUri() + " received:");
                logger.info((response.getBody().length() < 1024) ? response.getFullResponse() : response
                        .getShortResponse());
            }

            if (serverDate != null && actualDate != null) {
                latency = (actualDate.getTime().getTime() - serverDate.getTime()) / 1000;
                logger.info("Latency = " + latency);
            }
            return response;
        } finally {
            IOUtil.safeClose(connOut);
            IOUtil.safeClose(connIn);
            IOUtil.safeClose(conn);
        }
    }

    public static HttpResponse sendRequest(byte[] request, String host) {
        return sendRequest(request, host, null, TIMEOUT, DEFAULT_READ_BUFFER, DEFAULT_PORT_VALUE);
    }

    public static HttpResponse sendRequest(byte[] request, String host, boolean smallReadTimeout) {
        return sendRequest(request, host, null, smallReadTimeout ? SMALL_TIMEOUT : TIMEOUT, DEFAULT_READ_BUFFER, DEFAULT_PORT_VALUE);
    }

    public static HttpResponse sendRequest(byte[] request, int readTimeout) {
        return sendRequest(request, TEST_RESOURCE_HOST, null, readTimeout, DEFAULT_READ_BUFFER, DEFAULT_PORT_VALUE);
    }

    public static HttpResponse sendRequest(byte[] request, String host, javax.net.ssl.SSLSocketFactory sslSocketFactory) {
        return sendRequest(request, host, sslSocketFactory, TIMEOUT, DEFAULT_READ_BUFFER, DEFAULT_PORT_VALUE);
    }

    public static HttpResponse sendRequest(byte[] request, String host,
                                           javax.net.ssl.SSLSocketFactory sslSocketFactory, int readTimeout) {
        return sendRequest(request, host, sslSocketFactory, readTimeout, DEFAULT_READ_BUFFER, DEFAULT_PORT_VALUE);
    }

    public static HttpResponse sendRequest(byte[] request, String host,
                                           javax.net.ssl.SSLSocketFactory sslSocketFactory, int readTimeout, int readBuffer, int port) {
        Socket socket = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        InputStream in = null;

        try {
            logger.info("Sending request nr. ");
            logger.info(String.valueOf(requestNumber));
            long reqNum = requestNumber;
            ++requestNumber;
            if (sslSocketFactory != null) {
                logger.info(" using SSL");
            }
            logger.info(" through OC:");
            logger.info(new String(request));
            long start = System.currentTimeMillis();

            if (sslSocketFactory == null) {
                socket = (port == DEFAULT_PORT_VALUE ? new Socket(host, 80) : new Socket(host, port));
            } else {
                socket = (port == DEFAULT_PORT_VALUE ? sslSocketFactory.createSocket(host, 443) : sslSocketFactory.createSocket(host, port));
            }

            socket.setReceiveBufferSize(readBuffer);
            socket.setSoTimeout(readTimeout);

            dos = new DataOutputStream(socket.getOutputStream());
            dos.write(request);
            dos.flush();

            in = socket.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[readBuffer];
            int len;

            // OC sends RST instead of FIN, that causes SocketException. So, handle it as end of input.
            try {
                while ((len = in.read(buf)) >= 0) {
                    baos.write(buf, 0, len);
                }
            } catch (SocketException e) {
                logger.info("SocketException, ignored. message: " + e.getMessage());
            } catch (SocketTimeoutException e) {
                logger.info("SocketTimeoutException, ignored. message: " + e.getMessage());
            }

            StringBuilder sb = new StringBuilder();
            sb.append(baos.toString());

            long duration = System.currentTimeMillis() - start;

            HttpResponse response = buildResponse(sb);
            response.setDuration(duration);

            logger.info("Response for request nr. " + reqNum + " received:");
            logger.info(response.getFullResponse());

            return response;
        } catch (UnknownHostException e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } catch (IOException e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            IOUtil.safeClose(in);
            IOUtil.safeClose(dis);
            IOUtil.safeClose(dos);
            IOUtil.safeClose(socket);
        }
        HttpResponse badResponse = new HttpResponse();
        badResponse.setStatusCode(-1);
        badResponse.setRawContent("");
        return badResponse;
    }

    protected static HttpResponse buildResponse(StringBuilder sb) {
        HttpResponse.Builder builder = HttpResponse.Builder.create();

        builder.setRawContent(sb.toString());
        builder.setStatusLine(getStatusLine(sb.toString()));
        builder.setStatusCode(getResponseCode(sb.toString()));
        builder.setBody(getBody(sb.toString()));
        extractHeaderFields(builder, sb.toString());
        return builder.getResponse();
    }

    private static void extractHeaderFields(HttpResponse.Builder builder, String response) {
        if (response == null || response.indexOf(BaseConstantsIF.CRLF) == -1) {
            return;
        }

        int index1 = response.indexOf(CRLF);
        int index2 = response.indexOf(CRLF + CRLF, index1 + 1);

        if (index2 == -1) {
            return;
        }

        String headers = response.substring(index1 + 1, index2);

        StringTokenizer st = new StringTokenizer(headers, CRLF);

        while (st.hasMoreTokens()) {
            String header = st.nextToken();
            int i = header.indexOf(": ");
            if (i == -1)
                continue;
            builder.addHeaderField(header.substring(0, i), header.substring(i + 2));
        }
    }

    private static String getStatusLine(String response) {
        if (response == null || response.indexOf(CRLF) == -1) {
            return "";
        }
        return response.substring(0, response.indexOf(CRLF));
    }

    private static int getResponseCode(String response) {
        if (response == null) {
            return -1;
        }
        response = response.trim();
        int mark = response.indexOf(" ") + 1; //$NON-NLS-1$
        if (mark == 0) {
            return -1;
        }
        int last = mark + 3;
        if (last > response.length()) {
            last = response.length();
        }
        return Integer.parseInt(response.substring(mark, last));
    }

    private static String getBody(String response) {
        if (response == null || response.indexOf(CRLF + CRLF) == -1) {
            return "";
        }
        return response.substring(response.indexOf(CRLF + CRLF) + 4);
    }

    public static void resetRequestNumber() {
        requestNumber = 1;
    }

    public static HttpResponse sendRequestParallel(HttpRequest request) throws Exception {
        return sendRequestParallel(request, null, false, false, Body.BODY);
    }

    public static HttpResponse sendRequestParallel(HttpRequest request, boolean keepAlive) throws Exception {
        return sendRequestParallel(request, null, keepAlive, false, Body.BODY);
    }

    public static HttpResponse sendRequestParallel(HttpRequest request, boolean keepAlive, Body bodyType)
            throws Exception {
        return sendRequestParallel(request, null, keepAlive, false, bodyType);
    }

    public static HttpResponse sendRequestParallel(HttpRequest request, boolean keepAlive, boolean bypassProxy)
            throws Exception {
        return sendRequestParallel(request, null, keepAlive, bypassProxy, Body.BODY);
    }

    public static HttpResponse sendRequestParallel(HttpRequest request,
                                                   org.apache.http.conn.ssl.SSLSocketFactory apacheSslSocketFactory,
                                                   boolean keepAlive, boolean bypassProxy, Body bodyType) throws Exception {
        return sendRequestParallel(request, apacheSslSocketFactory, keepAlive, bypassProxy, bodyType, SMALL_TIMEOUT);
    }

    public static HttpResponse sendRequestParallel(HttpRequest request,
                                                   SSLSocketFactory apacheSslSocketFactory, boolean keepAlive, boolean bypassProxy,
                                                   Body bodyType, int timeOut) throws MalformedURLException, IOException,
            URISyntaxException, Exception {
        // HttpClient client = new DefaultHttpClient();
        HttpRequestBase httpReq;
        String method = request.getMethod();

        InputStream connIn = null;
        OutputStream connOut = null;
        try {
            URI replacedUri = null;
            if (bypassProxy) {
                // To send request directly, change protocol to http and port to 8099 (this port is ignored by OC)
                URI uri = new URI(request.getUri());
                replacedUri = new URI("http", uri.getUserInfo(), uri.getHost(), 8099, uri.getPath(), uri.getQuery(),
                        uri.getFragment());
            }
            long reqNum = 0;
            synchronized (mLock) {
                logger.info("Sending request nr. ");
                logger.info(String.valueOf(requestNumber));
                reqNum = requestNumber;
                ++requestNumber;
                String uri = replacedUri != null ? replacedUri.toString() : request.getUri();
                logger.info("Req uri: " + uri);
            }
            logger.info(bypassProxy ? " directly:" : " through OC:");
            if (replacedUri != null) {
                logger.info("URI is replaced to: " + replacedUri);
            }
            logger.info(request.getFullRequest());

            URI uri = replacedUri != null ? replacedUri : new URI(request.getUri());
            if (method.equals("GET")) {
                httpReq = new HttpGet(uri);
            } else if (method.equals("POST")) {
                HttpPost post = new HttpPost(uri);
                if (request.getBody() != null) {
                    StringEntity entity = new StringEntity(request.getBody());
                    post.setEntity(entity);
                    post.addHeader("Content-Type", "text/html");
                }
                httpReq = post;
            } else if (method.equals("DELETE")) {
                httpReq = new HttpDelete(uri);
            } else if (method.equals("PUT")) {
                HttpPut put = new HttpPut(uri);
                if (request.getBody() != null) {
                    StringEntity entity = new StringEntity(request.getBody());
                    put.setEntity(entity);
                    put.addHeader("Content-Type", "text/html");
                }
                httpReq = put;
            } else if (method.equals("TRACE")) {
                httpReq = new HttpTrace(uri);
            } else if (method.equals("HEAD")) {
                httpReq = new HttpHead(uri);
            } else if (method.equals("PUT")) {
                httpReq = new HttpPut(uri);
            } else if (method.equals("OPTIONS")) {
                httpReq = new HttpOptions(uri);
            } else {
                // unsupported method
                throw new Exception(method + " is unsupported");
            }

            long start = System.currentTimeMillis();

            // HttpURLConnection.setFollowRedirects(request.getFollowRedirects());

            for (HttpHeaderField field : request.getHeaderFields()) {
                httpReq.setHeader(field.getName(), field.getValue());
            }

            httpReq.setHeader("Connection", keepAlive ? "keep-alive" : "close");

            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schemeRegistry.register(new Scheme("httpbypass", PlainSocketFactory.getSocketFactory(), 8099));
            SSLSocketFactory socketFactory = apacheSslSocketFactory != null ? apacheSslSocketFactory : SSLSocketFactory
                    .getSocketFactory();
            schemeRegistry.register(new Scheme("https", socketFactory, 443));

            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, timeOut);
            HttpConnectionParams.setSoTimeout(params, timeOut);
            ConnManagerParams.setMaxTotalConnections(params, 60);
            ConnManagerParamBean connParams = new ConnManagerParamBean(params);
            connParams.setConnectionsPerRoute(new ConnPerRouteBean(20));

            ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);

            HttpClient client = new DefaultHttpClient(cm, params);

            org.apache.http.HttpResponse httpResponse = client.execute(httpReq);
            HttpEntity entity = httpResponse.getEntity();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            if (entity != null) {
                connIn = entity.getContent();
                byte[] tmp = new byte[2048];
                if (bodyType == Body.BODY) {
                    try {
                        IOUtil.transfer(connIn, baos, new byte[1024]);
                    } catch (IOException e) {
                        logger.error(ExceptionUtils.getFullStackTrace(e));
                        // Ignored
                    }
                } else {
                    while (connIn.read(tmp) != -1) {
                    }
                }
            }

            HttpResponse response = HttpResponse.Builder.create()
                    .setStatusCode(httpResponse.getStatusLine().getStatusCode())
                    .setStatusLine(httpResponse.getStatusLine().toString()).setBody(baos.toString())
                    .setDuration(System.currentTimeMillis() - start).getResponse();

            Header[] allHeaders = httpResponse.getAllHeaders();

            for (Header header : allHeaders) {
                response.addHeaderField(new HttpHeaderField(header.getName(), header.getValue()));
            }

            if (entity != null) {
                entity.consumeContent();
            }
            synchronized (mLock) {
                logger.info("Response for request nr. " + reqNum + " received:");
                logger.info(response.getFullResponse());
            }

            return response;
        } finally {
            IOUtil.safeClose(connOut);
            IOUtil.safeClose(connIn);
        }
    }

    public static HttpResponse sendRequest2(HttpRequest request, int sleepAfterHandshake) {
        return sendRequest2(request, false, false, TIMEOUT, DEFAULT_READ_BUFFER, sleepAfterHandshake, VERSION);
    }

    public static HttpResponse sendRequest2(HttpRequest request) {
        return sendRequest2(request, false, false, TIMEOUT, DEFAULT_READ_BUFFER, SLEEP_AFTER_HANDSHAKE, VERSION);
    }

    public static HttpResponse sendRequest2(HttpRequest request, int TIMEOUT, HTTP_VERSION version) {
        return sendRequest2(request, false, false, TIMEOUT, DEFAULT_READ_BUFFER, SLEEP_AFTER_HANDSHAKE, version);
    }

    public static HttpResponse sendRequest2(HttpRequest request, boolean keepAlive, boolean bypassProxy) {
        return sendRequest2(request, keepAlive, bypassProxy, TIMEOUT, DEFAULT_READ_BUFFER, SLEEP_AFTER_HANDSHAKE, VERSION);
    }

    public static HttpResponse sendRequest2(HttpRequest request, boolean keepAlive, boolean bypassProxy, int timeout) {
        return sendRequest2(request, keepAlive, bypassProxy, timeout, DEFAULT_READ_BUFFER, SLEEP_AFTER_HANDSHAKE, VERSION);
    }

    public static HttpResponse sendRequest2(HttpRequest request, boolean keepAlive, boolean bypassProxy, int timeout, int readBuffer) {
        return sendRequest2(request, keepAlive, bypassProxy, timeout, readBuffer, SLEEP_AFTER_HANDSHAKE, VERSION);
    }

    public static HttpResponse sendRequest2(HttpRequest request, boolean keepAlive, boolean bypassProxy, int timeout, int readBuffer, int sleepAfterHandshake, HTTP_VERSION version) {

        DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        Date serverDate = null;
        Calendar actualDate = null;
        latency = 0;

        Socket socket = null;
        DataOutputStream dos = null;
        InputStream in = null;
        ByteArrayOutputStream baos;

        try {
            HttpRequest copy = request.copy();
            copy.addHeaderField(new HttpHeaderField("Connection", keepAlive ? "keep-alive" : "close"));

            logger.info("Sending request nr. ");
            logger.info(String.valueOf(requestNumber++));
            logger.info(bypassProxy ? " directly:" : " through OC:");
            String fullRequest;
            if (version == HTTP_VERSION.HTTP11) {
                fullRequest = copy.getFullRequest(true);
            } else if (version == HTTP_VERSION.HTTP10) {
                fullRequest = copy.getFullRequest(false);
            } else {
                fullRequest = copy.getIncorrectRequest();
            }
//            String fullRequest = copy.getFullRequest(HTTP11);
            logger.info(fullRequest);
            long start = System.currentTimeMillis();

            SocketAddress sockaddr = new InetSocketAddress(new URL(copy.getUri()).getHost(), bypassProxy ? 8099 : 80);
            socket = new Socket();
            socket.setReceiveBufferSize(readBuffer);
            socket.setSoTimeout(timeout);
            socket.setKeepAlive(keepAlive);
            socket.connect(sockaddr, timeout);

            TestUtil.sleep(sleepAfterHandshake * 1000);

            dos = new DataOutputStream(socket.getOutputStream());

            long startD = System.currentTimeMillis();

            dos.write(fullRequest.getBytes());
            dos.flush();

            long startTO = System.currentTimeMillis();

            in = socket.getInputStream();
            actualDate = Calendar.getInstance();
            baos = new ByteArrayOutputStream();

            // OC sends RST instead of FIN, that causes SocketException. So, handle it as end of input.
            int len;

            int headersLen = -1;
            int totalReceivedSize = 0;
            byte[] buffer = new byte[readBuffer];
            long startRT = 0;
            int contentLength = -1;
            List<HttpHeaderField> responseHeaders = null;
            boolean processedHeaders = false;

            try {
                while ((len = in.read(buffer)) >= 0) {
                    totalReceivedSize += len;

                    if (startRT == 0) {
                        startRT = System.currentTimeMillis();
                    }

                    if ((headersLen == -1) || ((headersLen != -1) && (baos.size() < MAX_BODY_SIZE))) {
                        baos.write(buffer, 0, len);
                    }

                    if (headersLen == -1) {
                        headersLen = checkBody(buffer);
                    }
                    if (headersLen != -1) {
                        int receivedBodyLen = totalReceivedSize - headersLen - 4 /* CRLFCRLF */;
                        if (responseHeaders == null && !processedHeaders) {
                            processedHeaders = true;
                            responseHeaders = extractHeaderFields(baos.toByteArray());
                            for (HttpHeaderField field : responseHeaders) {
                                if (field.getName().equalsIgnoreCase("Content-Length")) {

                                    try {
                                        contentLength = Integer.parseInt(field.getValue());
                                    } catch (NumberFormatException ignored) {

                                    }
                                }
                                if (field.getName() != null && field.getValue() != null && field.getName().equals("Date")) {
                                    try {
                                        serverDate = dateFormat.parse(field.getValue());
                                    } catch (ParseException e) {
                                        e.getStackTrace();
                                    }
                                }
                            }
                        }
                        if ((contentLength != -1) && (contentLength == receivedBodyLen)) {
                            break;
                        }
                    }
                }
            } catch (SocketException e) {
                logger.info("SocketException, ignored. message: " + e.getMessage());
            } catch (SocketTimeoutException e) {
                logger.info("SocketTimeoutException, ignored. message: " + e.getMessage());
            }
            logger.info("Size of response calculated by test application: " + totalReceivedSize);
            long endRT = System.currentTimeMillis();

            HttpResponse response = buildResponse(baos);
            response.setDuration(System.currentTimeMillis() - start);
            response.setStartTime(start);
            logger.info("Response received:" + copy.getUri());
            logger.info((response.getBody().length() < 1024) ? response.getFullResponse() : response
                    .getShortResponse());

            response.setSocketInfo(new NetStat.SocketInfo(socket.getLocalAddress().getHostAddress(), socket
                    .getLocalPort(), socket.getInetAddress().getHostAddress(), socket.getPort()));

            logger.info("SocketInfo :" + response.getSocketInfo().toString());

            response.setTO(startRT - startTO);
            response.setD(startRT - startD);
            response.setRT(endRT - startRT);
            if (lastRequestTime.containsKey(request.getMethod() + request.getUri()))
                response.setIT(endRT - lastRequestTime.get(request.getMethod() + request.getUri()));
            lastRequestTime.put(request.getMethod() + request.getUri(), startD);

            if (serverDate != null && actualDate != null) {
                latency = (actualDate.getTime().getTime() - serverDate.getTime()) / 1000;
                logger.info("Latency = " + latency);
            }

            return response;
        } catch (UnknownHostException e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } catch (IOException e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            baos = null;
            IOUtil.safeClose(in);
            IOUtil.safeClose(dos);
            IOUtil.safeClose(socket);
        }

        HttpResponse badResponse = new HttpResponse();
        badResponse.setStatusCode(-1);
        badResponse.setRawContent("");
        return badResponse;
    }

    public static int checkBody(byte[] buf) {
        for (int i = 0; i <= buf.length - 4; i++) {
            if ((buf[i] == 13) && (buf[i + 1] == 10) && (buf[i + 2] == 13) && (buf[i + 3] == 10)) {
                return i;
            }
        }
        return -1;
    }

    private static String getStatusLine(byte[] response) {
        if (response == null || response.length == 0) {
            return "";
        }
        for (int i = 0; i < response.length; i++) {
            if (response[i] == 10/* LF */) {
                if (response[i - 1] == 13/* CR */) {
                    return new String(response, 0, i - 1);
                }
            }
        }
        return "";
    }

    private static int getResponseCode(byte[] response) {
        if (response == null || response.length == 0) {
            return -1;
        }
        int i;

        // Trim
        for (i = 0; i < response.length; i++) {
            if (response[i] != ' ') {
                break;
            }
        }

        int j;
        for (j = i; j < response.length; j++) {
            if (response[j] == ' ') {
                break;
            }
        }
        int mark = j + 1; //$NON-NLS-1$
        if (mark == 1) {
            return -1;
        }

        int last = mark + 3;
        if (last > response.length) {
            last = response.length;
        }
        return Integer.parseInt(new String(response, mark, last - mark));
    }

    private static String getBody(byte[] response, boolean isChunked, boolean isGziped, boolean isDeflated) {
        if (response == null || response.length == 0) {
            return "";
        }

        int bodyStart = checkBody(response);
        if (bodyStart == -1) {
            return "";
        }
        // Skip CRLFCRLF
        bodyStart += 4;
        if (bodyStart >= response.length) {
            return "";
        }

        byte[] bodyBytes = new byte[response.length - bodyStart];
        System.arraycopy(response, bodyStart, bodyBytes, 0, response.length - bodyStart);

        if (isChunked) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(bodyBytes.length);
            int chunkStart = 0;
            for (int i = 1; i < bodyBytes.length; i++) {
                if (bodyBytes[i] == 10/* LF */) {
                    if (bodyBytes[i - 1] == 13/* CR */) {
                        String chunkLength = new String(bodyBytes, chunkStart, i - 1 - chunkStart);
                        if ("0".equals(chunkLength)) {
                            bodyBytes = baos.toByteArray();
                            break;
                        }
                        int length = Integer.parseInt(chunkLength, 16);

                        i++;
                        if (bodyBytes.length < i)
                            break;

                        if (bodyBytes.length < i + length) {
                            baos.write(bodyBytes, i, bodyBytes.length - i);
                            break;
                        } else {
                            baos.write(bodyBytes, i, length);
                        }
                        i += length;
                        chunkStart = i;
                        // In case or CRLF at the end of chunk
                        if (i < bodyBytes.length && bodyBytes[i] == 13/* CR */ && bodyBytes[i + 1] == 10/* LF */) {
                            i += 2;
                            chunkStart = i;
                        }
                    }
                }
            }
        }
        if (isGziped) {
            BufferedReader in = null;
            StringBuilder sb = new StringBuilder();
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(bodyBytes);
                GZIPInputStream gzis = new GZIPInputStream(bais);
                InputStreamReader reader = new InputStreamReader(gzis);
                in = new BufferedReader(reader);

                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                logger.error(ExceptionUtils.getFullStackTrace(e));
            } finally {
                IOUtil.safeClose(in);
            }
            return sb.toString();
        } else if (isDeflated) {
            Inflater decompressor = new Inflater();
            decompressor.setInput(bodyBytes);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(bodyBytes.length);
            byte[] buf = new byte[1024];
            try {
                while (!decompressor.finished()) {

                    int count = decompressor.inflate(buf);
                    bos.write(buf, 0, count);
                }
            } catch (DataFormatException e) {
            }
            return bos.toString();
        }
        return new String(bodyBytes);
    }

    public static List<HttpHeaderField> extractHeaderFields(byte[] response) {
        if (response == null || response.length == 0) {
            return null;
        }

        int index1 = -1;
        for (int i = 0; i < response.length; i++) {
            if (response[i] == 10/* LF */) {
                if (response[i - 1] == 13/* CR */) {
                    index1 = i;
                    break;
                }
            }
        }

        if (index1 == -1) {
            return null;
        }

        int index2 = checkBody(response);

        if (index2 == -1) {
            return null;
        }

        for (int i = 2; i < response.length; i++) {
            if (response[i] == 9 && response[i - 2] == 13 && response[i - 1] == 10) {
                response[i] = response[i - 2] = response[i - 1] = 32;
            }
        }

        String headers = new String(response, index1 + 1, index2 - index1 + 1);
        StringTokenizer st = new StringTokenizer(headers, CRLF);
        List<HttpHeaderField> headersList = new ArrayList<HttpHeaderField>();

        while (st.hasMoreTokens()) {
            String header = st.nextToken();
            int i = header.indexOf(": ");
            if (i == -1)
                continue;
            headersList.add(new HttpHeaderField(header.substring(0, i), header.substring(i + 2)));
        }
        return headersList;
    }

    public static HttpResponse buildResponse(ByteArrayOutputStream rawResponse) {
        HttpResponse.Builder builder = HttpResponse.Builder.create();

        byte[] response = rawResponse.toByteArray();
        // builder.setRawContent(sb);
        builder.setStatusLine(getStatusLine(response));
        builder.setStatusCode(getResponseCode(response));
        List<HttpHeaderField> headers = extractHeaderFields(response);
        boolean gzip = false;
        boolean deflate = false;
        boolean chunked = false;
        if (headers != null) {
            for (HttpHeaderField field : headers) {
                builder.addHeaderField(field);
                if (!chunked && field.getName().equalsIgnoreCase("Transfer-Encoding")) {
                    if ("chunked".equalsIgnoreCase(field.getValue())) {
                        chunked = true;
                    }
                } else if (!gzip && field.getName().equalsIgnoreCase("Content-Encoding")) {
                    if ("gzip".equalsIgnoreCase(field.getValue())) {
                        gzip = true;
                    } else if ("deflate".equalsIgnoreCase(field.getValue())) {
                        deflate = true;
                    }

                }
            }
            headers.clear();
        }
        builder.setBody(getBody(response, chunked, gzip, deflate));

        return builder.getResponse();
    }

    protected void executeThreads(TestCaseThread... threads) throws Throwable {
        executeThreads(ThreadStopMode.INTERRUPT, 0, threads);
    }

    protected void executeThreads(long timeout, TestCaseThread... threads) throws Throwable {
        executeThreads(ThreadStopMode.INTERRUPT, timeout, threads);
    }


    protected void executeThreads(ThreadStopMode stopMode, long timeOut, TestCaseThread... threads) throws Throwable {
        List<TestCaseThread> multipleThreads = new ArrayList<TestCaseThread>();
        for (TestCaseThread t : threads) {
            multipleThreads.add(t);
        }
        executeThreads(stopMode, timeOut, multipleThreads);
    }

    /**
     * Executes several threads. Waits until all threads are stopped.
     * Throws a Throwable if any occurs in some thread (if many -
     * the Throwable of the first listed thread will be thrown).
     *
     * @param stopMode - how to interrupt another threads when one is failed.
     * @param timeOut  - timeout of execution, in ms. 0 for default timeout in ThreadLocker.
     * @param threads
     * @throws Throwable
     */
    protected void executeThreads(final ThreadStopMode stopMode, long timeOut, final List<TestCaseThread> threads) throws Throwable {
        if (timeOut == 0)
            timeOut = TIMEOUT;

        final ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(4);

        Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread th, Throwable ex) {
                if (stopMode == ThreadStopMode.INTERRUPT) {
                    executorService.shutdownNow();
                } else if (stopMode == ThreadStopMode.INTERRUPT_SOFTLY) {
                    for (TestCaseThread testCaseThread : threads) {
                        testCaseThread.interruptSoftly();
                    }
                }
            }
        };
        for (final TestCaseThread testCaseThread : threads) {
            Thread wrapperThread = new Thread() {
                @Override
                public void run() {
                    Throwable t = testCaseThread.call();
                    if (t != null && getUncaughtExceptionHandler() != null)
                        getUncaughtExceptionHandler().uncaughtException(this, t);
                }
            };

            wrapperThread.setUncaughtExceptionHandler(h);
            executorService.schedule(wrapperThread, testCaseThread.getDelay(), TimeUnit.MILLISECONDS);
        }

        executorService.shutdown();
        executorService.awaitTermination(timeOut, TimeUnit.MILLISECONDS);
    }

    protected void createCustomResponse(String uri, String body, String statusCode) {

        String expected = "HTTP/1.0 " + statusCode + " OK" + CRLF + CRLF + body;
        String expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded)
                .getRequest();
        sendRequest2(request, false, true);
    }

    protected void createCustomResponse(String uri, String body, String statusCode, String etag) {
        String expected = "HTTP/1.0 " + statusCode + " OK" + CRLF + "Etag " + etag + CRLF + CRLF + body;
        String expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded)
                .getRequest();
        sendRequest2(request, false, true);
    }

    protected void createCustomResponse(String uri, String body, String statusCode, String etag, String cacheControl) {
        String expected = "HTTP/1.0 " + statusCode + " OK" + CRLF + "Etag: " + etag + "Cache-Control: " + cacheControl + CRLF + CRLF + body;
        String expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
        HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded)
                .getRequest();
        sendRequest2(request, false, true);
    }

    protected void createCustomResponse(String resourceUri, String statusCode, String age, String contentType, String pragma, String contentLength, String body) {
        long NOW = System.currentTimeMillis();

        String expected = "HTTP/1.0 " + statusCode + " OK" + CRLF + "Date: " + DateUtil.format(new Date(NOW)) + CRLF + "Age: " + age + ""
                + CRLF + "Content-Type: " + contentType + "" + CRLF + "Pragma: " + pragma + "" + CRLF
                + "Accept-Ranges: bytes" + CRLF + "Content-Length: " + contentLength + CRLF + CRLF + body;
        String expectedEncoded = URLEncoder.encode(Base64.encodeToString(expected.getBytes(), Base64.DEFAULT));
        HttpRequest request = createRequest().setUri(resourceUri).setMethod("GET")
                .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Raw", expectedEncoded)
                .getRequest();
        sendRequest2(request, false, true);
    }

    //================================================================================================

    /**
     * Added LogerUtil
     */

    protected void logRequest(int requestId, String uri) {
        String resourceFrom = (getResourceType(uri) == ResourceType.REAL_RESOURCE) ? " to real recource "
                : " to test runner server, resource ";
        String threadName = getShortThreadName();

        StringBuilder message = new StringBuilder(128);
        message.append("Thread ").append(threadName);
        message.append(": Sending request R").append(threadName);
        message.append(".").append(requestId);
        message.append(resourceFrom).append(uri);

        String allMessage = message.toString();
        logger.info(allMessage);

//        logger.log(requestId, "REQUEST", allMessage);
    }

    protected void logResponse(int responseId, ResponseLocation location, HttpResponse response) {
        String from = (location == ResponseLocation.NETWORK) ? "NETWORK" : "CACHE";
        String threadName = getShortThreadName();

        StringBuilder message = new StringBuilder();
        message.append("Thread ").append(threadName);
        message.append(": Response recieved on R").append(threadName);
        message.append(".").append(responseId);
        message.append(" request. Response from: ").append(from);

//        logger.info(new Date(response.getStartTime() + response.getDuration()), "RESPONSE", message.toString(), from);

        logger.info("TIMESTAMPS", String.format("TO = %d, D = %d, RT = %d, IT = %d, RI = %d",
                response.getTO(), response.getD(), response.getRT(), response.getIT(), response.getRI()));
    }

    public static ResourceType getResourceType(String uri) {
        if (uri != null) {
            return uri.contains(TEST_RESOURCE_HOST) ? ResourceType.TEST_RUNNER : ResourceType.REAL_RESOURCE;
        }
        return ResourceType.UNKNOWN;
    }

    protected enum ResourceType {
        REAL_RESOURCE, TEST_RUNNER, UNKNOWN
    }

    protected enum ResponseLocation {
        NETWORK, CACHE
    }

    public static String getShortThreadName() {
        String threadName = Thread.currentThread().getName();
        return threadName.substring(threadName.length() - 1);
    }

    protected void logSleeping(long duration) {
        TestUtil.sleep(duration);
    }

    public static Context getStaticContext() {
        return IntegrationTestRunnerGa.getStaticContext();
    }
}
