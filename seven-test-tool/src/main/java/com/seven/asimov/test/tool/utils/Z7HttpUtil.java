package com.seven.asimov.test.tool.utils;

import com.seven.asimov.test.tool.constants.Constants;
import com.seven.asimov.test.tool.core.TestFactoryOptions;
import com.seven.asimov.test.tool.serialization.TestItem;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Z7HttpUtil {
    private static final Logger LOG = LoggerFactory.getLogger(Z7HttpUtil.class.getSimpleName());

    public static URI getUriFromHeaders(String headers) {

        // http://en.wikipedia.org/wiki/URI_scheme#Generic_syntax

        String uri = null;
        String host;
        String prefix = Constants.HTTP_PREFIX;
        Integer port = null;

        try {
            String[] headerList = headers.trim().split(Constants.HTTP_NEW_LINE);
            String[] statusLine = headerList[0].split(" ");
            if (statusLine.length > 1) {
                if (statusLine[1].toLowerCase().startsWith(Constants.HTTP_PREFIX)
                        || (statusLine[1].toLowerCase().startsWith(Constants.HTTPS_PREFIX))) {
                    uri = statusLine[1];
                }
            } else {
                uri = null;
            }
            for (int i = 1; i < headerList.length; i++) {
                String key = headerList[i].substring(0, headerList[i].indexOf(":"));
                String value = headerList[i].substring(key.length() + 1).trim();
                String[] headerParts = headerList[i].trim().split(":");
                if (headerParts[0].trim().equalsIgnoreCase("Host")) {
                    if (uri == null) {
                        if (value.contains(":")) {
                            host = value.substring(0, value.indexOf(":"));
                            try {
                                port = Integer.valueOf(value.substring(value.indexOf(":") + 1));
                            } catch (Exception e) {
                                LOG.error(e.toString());
                            }
                        } else {
                            host = value;
                        }
                        if (statusLine.length > 1) {
                            uri = prefix + host + ((port != null) ? ":" + port : StringUtils.EMPTY) + statusLine[1];
                        } else {
                            uri = prefix + host + ((port != null) ? ":" + port : StringUtils.EMPTY);
                        }

                    }
                    break;
                }
            }

            if (uri == null) {
                uri = sDefaultUri;
            }

            return new URI(uri);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Parameters parseParametersFromUri(URI uri) {
        Parameters params = new Parameters();

        String[] parts = null;
        if (uri.getQuery() != null) {
            parts = StringUtils.stripAll(uri.getQuery().split("&"));
        } else if (uri.getFragment() != null) {
            parts = StringUtils.stripAll(uri.getFragment().split("&"));
        }
        if (parts != null) {
            for (String part : parts) {
                int delimPos = part.indexOf("=");
                if (delimPos == -1) {
                    continue;
                }
                String key = part.substring(0, delimPos);
                String value = part.substring(delimPos + 1);
                params.put(key, value);
            }
        }
        return params;
    }

    public static URI getUriWithPort(URI uri) throws URISyntaxException {

        if (uri.getPort() == -1) {
            String prefix = null;
            String uriString = uri.toString();
            Integer port = Constants.PORT_HTTP;

            if (uriString.toLowerCase().startsWith(Constants.HTTP_PREFIX)) {
                prefix = Constants.HTTP_PREFIX;
            }
            if (uriString.toLowerCase().startsWith(Constants.HTTPS_PREFIX)) {
                prefix = Constants.HTTPS_PREFIX;
                port = Constants.PORT_HTTPS;
            }

            uri = new URI(prefix + uri.getHost() + ":" + port + uri.getRawPath()
                    + ((uri.getQuery() != null) ? "?" + uri.getRawQuery() : StringUtils.EMPTY)
                    + ((uri.getFragment() != null) ? "#" + uri.getRawFragment() : StringUtils.EMPTY));
        }
        return uri;
    }

    public static TestItem replaceUriFromHeaders(URI newUri, TestItem test) {

        // Check if need to change
        if (test.getUri().equals(newUri)) {
            return test;
        }

        Integer port = Constants.PORT_HTTP;

        if (newUri.getPort() == -1) {
            if (newUri.toString().toLowerCase().startsWith(Constants.HTTPS_PREFIX)) {
                port = Constants.PORT_HTTPS;
            }
        } else {
            port = newUri.getPort();
        }

        TestItem newTest = new TestItem();
        String[] headerList = test.getRequestHeaders().split(Constants.HTTP_NEW_LINE);
        String[] statusLine = headerList[0].split(" ");
        if (statusLine.length == 3) {

            if (statusLine[1].toLowerCase().startsWith(Constants.HTTP_PREFIX)
                    || (statusLine[1].toLowerCase().startsWith(Constants.HTTPS_PREFIX))) {
                headerList[0] = statusLine[0] + " " + newUri.toString() + " " + statusLine[2];
            } else {
                headerList[0] = statusLine[0] + " " + newUri.getPath()
                        + ((newUri.getQuery() != null) ? "?" + newUri.getRawQuery() : StringUtils.EMPTY)
                        + ((newUri.getFragment() != null) ? "#" + newUri.getRawFragment() : StringUtils.EMPTY) + " "
                        + statusLine[2];
            }
        } else {
            return test;
        }

        StringBuilder newHeaders = new StringBuilder();
        newHeaders.append(headerList[0]);

        for (int i = 1; i < headerList.length; i++) {
            newHeaders.append(Constants.HTTP_NEW_LINE);
            String[] headerParts = headerList[i].trim().split(":");
            if (headerParts[0].trim().equalsIgnoreCase("Host")) {
                newHeaders.append(headerParts[0]).append(": ")
                        .append(newUri.getHost())
                        .append((port != Constants.PORT_HTTP) ? ":" + port : StringUtils.EMPTY);
            } else {
                newHeaders.append(headerList[i]);
            }
        }
        newTest.setRequestHeaders(newHeaders.toString());
        newTest.setRequestContent(test.getRequestContent());
        newTest.setUri(newUri);
        return newTest;
    }

    public static String getHttpMethodFromHeaders(String headers) {
        try {
            String[] headerList = headers.trim().split(Constants.HTTP_NEW_LINE);
            String[] statusLine = headerList[0].split(" ");
            return statusLine[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static TestItem replaceHttpMethodFromHeaders(String newHttpMethod, TestItem test) {

        // Check if need to change
        if (test.getHttpMethod().equals(newHttpMethod)) {
            return test;
        }

        TestItem newTest = new TestItem();
        String[] headerList = test.getRequestHeaders().split(Constants.HTTP_NEW_LINE);
        String[] statusLine = headerList[0].split(" ");
        if (statusLine.length == 3) {
            headerList[0] = newHttpMethod + " " + statusLine[1] + " " + statusLine[2];
        } else {
            return test;
        }
        StringBuilder newHeaders = new StringBuilder();
        newHeaders.append(headerList[0]);

        for (int i = 1; i < headerList.length; i++) {
            if (!newHttpMethod.equals("POST") && !newHttpMethod.equals("PUT")
                    && !StringUtils.isEmpty(test.getRequestContent())) {
                String key = headerList[i].substring(0, headerList[i].indexOf(":"));
                if (key.equalsIgnoreCase("Content-Length")) {
                    continue;
                }
                if (headerList[i].equalsIgnoreCase("Transfer-Encoding: chunked")) {
                    continue;
                }
            }
            newHeaders.append(Constants.HTTP_NEW_LINE);
            newHeaders.append(headerList[i]);
        }
        String content;
        if ((newHttpMethod.equals("POST") && StringUtils.isEmpty(test.getRequestContent()))
                || newHttpMethod.equals("PUT") && StringUtils.isEmpty(test.getRequestContent())) {
            content = RandomStringUtils.random(1000, "a");
            newHeaders.append(Constants.HTTP_NEW_LINE);
            if (TestFactoryOptions.isRequestChunked()) {
                newHeaders.append("Transfer-Encoding: chunked");

                StringBuilder chunkedRequestContent = new StringBuilder();
                ArrayList<String> chunks = Util.chunk(content, 100);
                for (String chunk : chunks) {
                    chunkedRequestContent.append(Integer.toHexString(chunk.length()));
                    chunkedRequestContent.append(Constants.HTTP_NEW_LINE);
                    chunkedRequestContent.append(chunk);
                    chunkedRequestContent.append(Constants.HTTP_NEW_LINE);
                }
                chunkedRequestContent.append("0");
                chunkedRequestContent.append(Constants.HTTP_DOUBLE_NEW_LINE);

                content = chunkedRequestContent.toString();
            } else {
                newHeaders.append("Content-Length: ").append(content.length());
            }
            newTest.setRequestContent(content);
        } else {
            newTest.setRequestContent(StringUtils.EMPTY);
        }
        newTest.setRequestHeaders(newHeaders.toString());
        newTest.setHttpMethod(newHttpMethod);

        return newTest;
    }

    public static String getHostFromRequest(String request) {
        String host = null;
        String[] reqParts = request.trim().split(Constants.HTTP_DOUBLE_NEW_LINE);
        String[] headers = reqParts[0].trim().split(Constants.HTTP_NEW_LINE);
        String[] statusLine = headers[0].trim().toLowerCase().split(" ");
        if (statusLine[1].startsWith(Constants.HTTP_PREFIX)) {
            host = statusLine[1];
        }
        for (int i = 1; i < headers.length; i++) {
            String[] headerParts = headers[i].trim().split(":");
            if (headerParts[0].trim().equalsIgnoreCase("Host")) {
                host = headerParts[1].trim();
                break;
            }
        }
        return host;
    }

    public static Integer findHeadersLength(ByteArrayOutputStream messageArray) {

        Integer result = null;

        try {
            String headersString = messageArray.toString();
            int headersLength = headersString.indexOf(Constants.HTTP_DOUBLE_NEW_LINE);
            if (headersLength == -1) {
                return result;
            }
            result = headersLength + Constants.HTTP_DOUBLE_NEW_LINE.length();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static final String sDefaultHttpMethod = "GET";
    private static String sDefaultUri = "http://tln-dev-testrunner1.7sys.eu:80/yourresource";

    public static TestItem buildDefaultTest() {

        TestItem test = new TestItem();
        try {
            test.setReiterations(1);
            test.setRequestContent(StringUtils.EMPTY);
            test.setHttpMethod(sDefaultHttpMethod);

            URI uri = new URI(sDefaultUri);
            test.setUri(uri);

            StringBuilder requestHeaders = new StringBuilder();
            requestHeaders.append(sDefaultHttpMethod + " ")
                    .append(uri.getPath()).append(" ").append(Constants.HTTP_1_1_PROTOCOL);
            requestHeaders.append(Constants.HTTP_NEW_LINE);
            requestHeaders.append("Host: ").append(uri.getHost());
            requestHeaders.append(Constants.HTTP_NEW_LINE);
            if (!TestFactoryOptions.isRequestKeepAlive()) {
                requestHeaders.append("Connection: Close");
            } else {
                requestHeaders.append("Connection: Keep-Alive");
            }
            if (TestFactoryOptions.isRequestAcceptGzip()) {
                requestHeaders.append(Constants.HTTP_NEW_LINE);
                requestHeaders.append("Accept-Encoding: gzip");
            }
            if (TestFactoryOptions.isRequestAcceptDeflate()) {
                requestHeaders.append(Constants.HTTP_NEW_LINE);
                requestHeaders.append("Accept-Encoding: deflate");
            }
            if (TestFactoryOptions.isRequestAcceptCompress()) {
                requestHeaders.append(Constants.HTTP_NEW_LINE);
                requestHeaders.append("Accept-Encoding: compress");
            }

            test.setRequestHeaders(requestHeaders.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return test;
    }
}
