package com.seven.asimov.test.tool.core;

import com.seven.asimov.test.tool.constants.Constants;
import com.seven.asimov.test.tool.utils.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Response.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class Response {
    private static final Logger LOG = LoggerFactory.getLogger(Response.class.getSimpleName());

    private Request mRequest;
    private int mBytesReceived;
    private ByteArrayOutputStream mRawArray = new ByteArrayOutputStream();
    private ByteArrayOutputStream mContentArray = new ByteArrayOutputStream();
    private Parameters mHeaders = new Parameters();
    private Integer mHeadersLength;
    private String mHeadersString;
    private Integer mHttpStatus;
    private String mStatusLine;
    private String mCharset;
    private boolean mKeepAlive = true;
    private boolean mExpectContinue;
    private boolean mChunked;
    private boolean mGzip;
    private boolean mDeflate;
    private boolean mCompress;
    private int mContentLength = 0;
    private URI mRedirectUri;
    private String mBody = StringUtils.EMPTY;
    private String mMd5 = StringUtils.EMPTY;
    private String mMd5content = StringUtils.EMPTY;
    private Integer mMetaTimeout;
    private Parameters mUriParameters;

    public Request getRequest() {
        return mRequest;
    }

    public Integer getHeadersLength() {
        return mHeadersLength;
    }

    public void setBytesReceived(int bytesReceived) {
        this.mBytesReceived = bytesReceived;
    }

    public int getBytesReceived() {
        return mBytesReceived;
    }

    public ByteArrayOutputStream getRawArray() {
        return mRawArray;
    }

    public ByteArrayOutputStream getContentArray() {
        return mContentArray;
    }

    public void setContentArray(ByteArrayOutputStream contentArray) {
        this.mContentArray = contentArray;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.mHttpStatus = httpStatus;
    }

    public Integer getHttpStatus() {
        return mHttpStatus;
    }

    public void setStatusLine(String statusLine) {
        this.mStatusLine = statusLine;
    }

    public String getStatusLine() {
        return mStatusLine;
    }

    public Integer getStatusCode() {
        Integer result = null;
        if (mStatusLine != null) {
            String[] parts = mStatusLine.split(" ", 3);
            if (parts.length == 3) {
                return Integer.parseInt(parts[1]);
            }
        }
        return result;
    }

    public void setCharset(String charset) {
        this.mCharset = charset;
    }

    public String getCharset() {
        return mCharset;
    }

    public void setKeepAlive(boolean isKeepAlive) {
        this.mKeepAlive = isKeepAlive;
    }

    public boolean isKeepAlive() {
        return mKeepAlive;
    }

    public boolean isExpectContinue() {
        return mExpectContinue;
    }

    public void setChunked(boolean isChunked) {
        this.mChunked = isChunked;
    }

    public boolean isChunked() {
        return mChunked;
    }

    public void setGzip(boolean isGzip) {
        this.mGzip = isGzip;
    }

    public boolean isGzip() {
        return mGzip;
    }

    public void setDeflate(boolean isDeflate) {
        this.mDeflate = isDeflate;
    }

    public boolean isDeflate() {
        return mDeflate;
    }

    public void setCompress(boolean isCompress) {
        this.mCompress = isCompress;
    }

    public boolean isCompress() {
        return mCompress;
    }

    public int getContentLength() {
        return mContentLength;
    }

    public void setRedirectUri(URI redirectUri) {
        this.mRedirectUri = redirectUri;
    }

    public URI getRedirectUri() throws Exception {
        if (mRedirectUri == null) {
            return null;
        }
        return Z7HttpUtil.getUriWithPort(mRedirectUri);
    }

    public void setBody(String body) {
        this.mBody = body;
    }

    public String getBody() {
        return mBody;
    }

    public void setMd5(String md5) {
        this.mMd5 = md5;
    }

    public String getMd5() {
        return mMd5;
    }

    public void setMd5content(String md5content) {
        this.mMd5content = md5content;
    }

    public String getMd5content() {
        return mMd5content;
    }

    public void setMetaTimeout(Integer metaTimeout) {
        this.mMetaTimeout = metaTimeout;
    }

    public Integer getMetaTimeout() {
        return mMetaTimeout;
    }

    public Response(Request request) {
        mRequest = request;
    }

    public Boolean parseHeaders() {

        if (mHeadersString == null) {
            mHeadersString = mRawArray.toString();
            mHeadersLength = mRawArray.size();
        }

        try {

            String[] headersLines = mHeadersString.split(Constants.HTTP_NEW_LINE, -1);

            StringBuffer serviceHeaderBuffer = null;
            String key = null;

            for (int i = 0; i < headersLines.length; i++) {
                String headerLine = headersLines[i];

                if (StringUtils.isEmpty(headerLine)) {
                    continue;
                }

                try {

                    if (i == 0) {
                        String[] headerLineParts = headerLine.split(" ");
                        if (headerLineParts.length > 2) {
                            mHttpStatus = Integer.valueOf(headerLineParts[1]);
                            mStatusLine = headersLines[i];
                        }
                        continue;
                    }

                    String value = null;

                    // Check if Multiline header, if yes, use previous key
                    Parameter p = null;
                    if (Util.isHeaderExtraLine(headerLine)) {
                        value = StringUtils.strip(headerLine, " \t");
                        p = new Parameter(key, value);
                        p.setMultiLineHeader(true);
                        p.setMultiLineHeaderPrefix(headerLine.substring(0, headerLine.length() - value.length()));
                        mHeaders.add(p);
                    } else {
                        key = headerLine.substring(0, headerLine.indexOf(":"));
                        value = headerLine.substring(key.length() + 1).trim();
                        mHeaders.put(key, value);
                    }

                    if (key.equalsIgnoreCase("Content-Type")) {
                        String[] headerparts = value.split(";");
                        if (headerparts.length != 0) {
                            for (String string : headerparts) {
                                if (string.toLowerCase().indexOf("charset=") != -1) {
                                    mCharset = string.substring("charset=".length());
                                }
                            }
                        }
                        continue;
                    }
                    if (key.equalsIgnoreCase("Connection")) {
                        if (value.equalsIgnoreCase("Close")) {
                            mKeepAlive = false;
                        }
                        continue;
                    }
                    if (key.equalsIgnoreCase("Transfer-Encoding")) {
                        if (value.equalsIgnoreCase("chunked")) {
                            mChunked = true;
                        }
                        continue;
                    }
                    if (key.equalsIgnoreCase("Content-Encoding")) {
                        if (value.equalsIgnoreCase("gzip")) {
                            mGzip = true;
                            continue;
                        }
                        if (value.equalsIgnoreCase("deflate")) {
                            mDeflate = true;
                            continue;
                        }
                        if (value.equalsIgnoreCase("compress")) {
                            mCompress = true;
                            continue;
                        }
                    }
                    if (key.equalsIgnoreCase("Content-Length")) {
                        mContentLength = Integer.valueOf(value);
                        continue;
                    }
                    if (key.equalsIgnoreCase("Location")) {
                        mRedirectUri = new URI(value);
                        continue;
                    }
                    if (key.contains("X-Z7-Test")) {
                        if (serviceHeaderBuffer == null) {
                            serviceHeaderBuffer = new StringBuffer();
                        }
                        serviceHeaderBuffer.append(headerLine + Constants.HTTP_NEW_LINE);
                        continue;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            if (mHttpStatus == Constants.HTTP_STATUS_100) {
                mExpectContinue = true;
            }
            if (mCharset == null) {
                mCharset = "UTF-8";
            }
            // 4.4 Message Length
            //
            // The transfer-length of a message is the length of the
            // message-body as
            // it appears in the message; that is, after any
            // transfer-codings have
            // been applied. When a message-body is included with a message,
            // the
            // transfer-length of that body is determined by one of the
            // following
            // (in order of precedence):
            //
            // 1.Any response message which "MUST NOT" include a
            // message-body (such
            // as the 1xx, 204, and 304 responses and any response to a HEAD
            // request) is always terminated by the first empty line after
            // the
            // header fields, regardless of the entity-header fields present
            // in
            // the message.

            // if (mHttpStatus == Constants.HTTP_STATUS_100 || mHttpStatus == Constants.HTTP_STATUS_101
            // || mHttpStatus == Constants.HTTP_STATUS_204 || mHttpStatus == Constants.HTTP_STATUS_304
            // || mRequest.getHttpMethod().equalsIgnoreCase("HEAD")) {
            // return true;
            // }
        } catch (Exception e) {
            LOG.error(e.toString());
            return false;
        }
        return true;
    }

    public void parseBody() throws Exception {
        // Find redirect from Meta or JS
        if (!StringUtils.isEmpty(mBody)) {

            while (true) {

                Pattern pattern = Pattern.compile("<meta (.*) content=[\"'](\\d+){1};[^;]*url=((.)*)[\"']/>",
                        Pattern.CASE_INSENSITIVE);
                Matcher match = pattern.matcher(mBody);

                if (match.find() && match.groupCount() == 4) {
                    mMetaTimeout = Integer.parseInt(match.group(2));
                    mRedirectUri = Z7HttpUtil.getUriWithPort(new URI(match.group(3)));
                    break;
                }

                pattern = Pattern.compile("window.location[\\s]=[\\s][\"']((.)*)[\"']", Pattern.CASE_INSENSITIVE);
                match = pattern.matcher(mBody);

                if (match.find() && match.groupCount() == 2) {
                    mRedirectUri = Z7HttpUtil.getUriWithPort(new URI(match.group(1)));
                    break;
                }

                break;
            }
        }
        mMd5 = (MD5Util.getMD5(mHttpStatus.toString() + mBody)).toUpperCase();
        mMd5content = (MD5Util.getMD5(mBody)).toUpperCase();
    }

    public void handleParameters() {
        // OAuth2
        if (mRedirectUri != null) {
            mUriParameters = Z7HttpUtil.parseParametersFromUri(mRedirectUri);
            if (StringUtils.isNotEmpty(mUriParameters.get(Request.OAUTH2_ACCESS_TOKEN))) {
                LOG.info("Setting new OAuth2 access token: " + mUriParameters.get(Request.OAUTH2_ACCESS_TOKEN));
                Request.setOauth2AccessToken(mUriParameters.get(Request.OAUTH2_ACCESS_TOKEN));
                // remove fragment from uri
                int index = mRedirectUri.toString().indexOf("#");
                if (index != -1) {
                    try {
                        mRedirectUri = new URI(mRedirectUri.toString().substring(0, index) + "?access_token="
                                + mUriParameters.get(Request.OAUTH2_ACCESS_TOKEN));
                    } catch (URISyntaxException e) {
                    }
                }
            }
        }
    }

    public boolean isRedirect() {
        if (TestFactoryOptions.isHandleRedirect()) {
            if (mRedirectUri != null) {
                return true;
            }
        }
        return false;
    }
}
