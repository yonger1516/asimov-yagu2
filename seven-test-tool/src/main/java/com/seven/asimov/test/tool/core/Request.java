package com.seven.asimov.test.tool.core;

import com.seven.asimov.test.tool.constants.Constants;
import com.seven.asimov.test.tool.core.testjobs.TestJobType;
import com.seven.asimov.test.tool.serialization.TestItem;
import com.seven.asimov.test.tool.utils.Parameters;
import com.seven.asimov.test.tool.utils.Util;
import com.seven.asimov.test.tool.utils.Z7HttpUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Request handler.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class Request {
    private static final Logger LOG = LoggerFactory.getLogger(Request.class.getSimpleName());

    private String mRequestString;

    private URI mUri;

    private String mProxy;
    private Integer mProxyPort;

    // private boolean mSip;
    private boolean mKeepAlive = true;
    private int mSocketReadTimeout = Constants.DEFAULT_SOCKET_TIMEOUT;
    private Integer mNextReadTimeout;
    private int mNewSocket;
    private Integer mDelaySocketWrite;
    private boolean mDispatchOnFirstByte;
    private boolean mRejectClientCert;
    private boolean mRejectServerCert;
    private int mCounter;
    private Parameters mParameters;
    private boolean mServiceRequest;

    private TestJobType mTestJobType;

    // Oauth2
    private static String sOauth2AccessToken;

    public static void setOauth2AccessToken(String oauth2AccessToken) {
        Request.sOauth2AccessToken = oauth2AccessToken;
    }

    public String getRequestString() {
        return mRequestString;
    }

    public URI getUri() {
        return mUri;
    }

    public boolean isDispatchOnFirstByte() {
        return mDispatchOnFirstByte;
    }

    public void setDispatchOnFirstByte(boolean dispatchOnFirstByte) {
        this.mDispatchOnFirstByte = dispatchOnFirstByte;
    }

    public boolean isRejectClientCert() {
        return mRejectClientCert;
    }

    public boolean isRejectServerCert() {
        return mRejectServerCert;
    }

    public Integer getSocketReadTimeout() {
        return mSocketReadTimeout;
    }

    public void setNextReadTimeout(Integer nextReadTimeout) {
        this.mNextReadTimeout = nextReadTimeout;
    }

    public Integer getNextReadTimeout() {
        return mNextReadTimeout;
    }

    public int getNewSocket() {
        return mNewSocket;
    }

    public Integer getDelaySocketWrite() {
        return mDelaySocketWrite;
    }

    public boolean isKeepAlive() {
        return mKeepAlive;
    }

    public boolean isServiceRequest() {
        return mServiceRequest;
    }

    public void setCounter(int counter) {
        this.mCounter = counter;
    }

    public int getCounter() {
        return mCounter;
    }

    public String getProxy() {
        return mProxy;
    }

    public Integer getProxyPort() {
        return mProxyPort;
    }

    public void setTestJobType(TestJobType testJobType) {
        this.mTestJobType = testJobType;
    }

    public TestJobType getTestJobTy() {
        return mTestJobType;
    }

    public Request() {
    }

    public static final String OAUTH2_ACCESS_TOKEN = "access_token";

    public void init(TestItem test) throws Exception {

        mRequestString = test.getRequestHeaders() + Constants.HTTP_DOUBLE_NEW_LINE
                + ((test.requestContent != null) ? test.getRequestContent() : StringUtils.EMPTY);
        mUri = Z7HttpUtil.getUriWithPort(Z7HttpUtil.getUriFromHeaders(test.getRequestHeaders()));

        mProxy = test.getProxy();
        mProxyPort = test.getProxyPort();

        mParameters = Z7HttpUtil.parseParametersFromUri(mUri);

        // Take custom parameters from query
        for (String key : mParameters.keySet()) {
            if (key.equals(OAUTH2_ACCESS_TOKEN)) {
                if (sOauth2AccessToken != null) {
                    mRequestString = mRequestString.replace(mParameters.get(OAUTH2_ACCESS_TOKEN), sOauth2AccessToken);
                    mUri = new URI(mUri.toString().replace(mParameters.get(OAUTH2_ACCESS_TOKEN), sOauth2AccessToken));
                }
                continue;
            }
        }

        getServiceHeaders(test.getRequestHeaders());
    }

    public void initService(String uri) throws Exception {

        StringBuffer host = new StringBuffer();
        String path = null;

        mKeepAlive = false;
        mServiceRequest = true;

        if (uri.toLowerCase().startsWith(Constants.HTTP_PREFIX)) {
            uri = uri.substring((Constants.HTTP_PREFIX).length());
        }
        if (uri.indexOf("/") != -1) {
            host.append(uri.substring(0, uri.indexOf("/")));
            path = uri.substring(host.length());
        } else {
            host.append(uri);
            path = "/";
        }

        // Add service port
        host.append(":8099");

        String httpMethod = "GET";
        StringBuffer result = new StringBuffer();
        result.append(httpMethod + " " + path + " " + Constants.HTTP_1_1_PROTOCOL);
        result.append(Constants.HTTP_NEW_LINE);
        result.append("Host: " + host);
        result.append(Constants.HTTP_NEW_LINE);
        result.append("Connection: Close");

        mUri = Z7HttpUtil.getUriFromHeaders(result.toString());

        result.append(Constants.HTTP_DOUBLE_NEW_LINE);

        mRequestString = result.toString();
    }

    public void initProxyService(String uri) throws Exception {

        String host = null;
        String path = null;

        if (uri.toLowerCase().startsWith("http://")) {
            uri = uri.substring(("http://").length());
        }
        if (uri.indexOf("/") != -1) {
            host = uri.substring(0, uri.indexOf("/"));
            path = uri.substring(host.length());
        } else {
            host = uri;
            path = "/";
        }

        String httpMethod = "GET";

        StringBuffer result = new StringBuffer();
        result.append(httpMethod + " " + path + " " + Constants.HTTP_1_1_PROTOCOL);
        result.append(Constants.HTTP_NEW_LINE);
        result.append("Host: " + host);
        result.append(Constants.HTTP_NEW_LINE);
        result.append("Connection: Close");

        mUri = Z7HttpUtil.getUriFromHeaders(result.toString());

        mUri = Z7HttpUtil.getUriWithPort(mUri);

        result.append(Constants.HTTP_DOUBLE_NEW_LINE);

        mRequestString = result.toString();
    }

    public void getServiceHeaders(String requestHeaders) throws Exception {
        String[] headersLines = requestHeaders.split(Constants.HTTP_NEW_LINE, -1);
        for (int i = 1; i < headersLines.length; i++) {
            String header = headersLines[i];
            try {
                if (StringUtils.isEmpty(header) || Util.isHeaderExtraLine(header)) {
                    continue;
                }
                ServiceHeaders key = ServiceHeaders.fromStringIgnoreCase(header.substring(0, header.indexOf(":")));
                if (key == null) {
                    continue;
                }
                String value = header.substring(header.indexOf(":") + 1).trim();
                switch (key) {
                    default:
                        break;
                    case DISPATCH_ON_FIRST_BYTE:
                        mDispatchOnFirstByte = Boolean.valueOf(value);
                        break;
                    case NEW_SOCKET:
                        mNewSocket = Integer.valueOf(value);
                        break;
                    case REJECT_CLIENT_CERT:
                        mRejectClientCert = Boolean.valueOf(value);
                        break;
                    case REJECT_SERVER_CERT:
                        mRejectServerCert = Boolean.valueOf(value);
                        break;
                    case SOCKET_TIMEOUT:
                        mSocketReadTimeout = Integer.valueOf(value);
                        break;
                    case SOCKET_NEXT_TIMEOUT:
                        mNextReadTimeout = Integer.valueOf(value);
                        break;
                    case DELAY_SOCKET_WRITE:
                        mDelaySocketWrite = Integer.valueOf(value);
                        break;
                }
            } catch (Exception e) {
                LOG.error(e.toString());
            }
        }
    }

    public void buildRedirect(URI redirectUri) throws Exception {

        String httpMethod = "GET";
        StringBuffer result = new StringBuffer();
        result.append(httpMethod + " " + redirectUri.getRawPath()
                + ((redirectUri.getQuery() != null) ? "?" + redirectUri.getRawQuery() : StringUtils.EMPTY)
                + ((redirectUri.getFragment() != null) ? "#" + redirectUri.getRawFragment() : StringUtils.EMPTY) + " "
                + Constants.HTTP_1_1_PROTOCOL);
        result.append(Constants.HTTP_NEW_LINE);
        result.append("Host: " + redirectUri.getHost());

        mUri = redirectUri;

        result.append(Constants.HTTP_DOUBLE_NEW_LINE);

        mRequestString = result.toString();
    }
}
