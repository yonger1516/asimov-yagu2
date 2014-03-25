package com.seven.asimov.it.utils;

import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequestMd5CalculatorUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestMd5CalculatorUtil.class.getSimpleName());
    private HttpRequest request;
    private List<Long> pollingIntervals;


    public HttpRequestMd5CalculatorUtil(HttpRequest request, List<Long> pollingIntervals) {
        this.request = request;
        if (pollingIntervals.get(0) >= 30 || pollingIntervals.get(0) == 0) {
            this.pollingIntervals = pollingIntervals;
        } else {
            this.pollingIntervals = new ArrayList<Long>(Arrays.asList(30L));
        }
    }

    public String getRequestMd5() {
        System.out.println(">>> Request Line: " + getRequestLine());
        System.out.println(">>> Headers: " + getHeaders());
        System.out.println(">>> URI + RI: " + request.getUri() + pollingIntervals);
        Md5CalculatorUtil cal = new Md5CalculatorUtil();
        cal.update(request.getUri() + pollingIntervals);
        if (getRequestLine() != null) {
            cal.update(getRequestLine());
        }
        if (getHeaders() != null) {
            cal.update(getHeaders());
        }
//        if (getExceptionHandlingParam() != OCCertificateConstants.OC_CHECK_ALL_CERTIFICATE_EXCEPTIONS &&
//                getExceptionHandlingParam() != OCCertificateConstants.OC_IGNORE_ALL_CERTIFICATE_EXCEPTIONS) {
//            cal.update(getExceptionHandlingParam() + getCertHash());
//        }
        return cal.md5();
    }

    public String getRequestLine() {
        String uriRegexp = "http[s]*:\\/\\/[\\.\\w\\d:\\-]+([\\/\\w\\d\\-]+)";
        Pattern pattern = Pattern.compile(uriRegexp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(request.getUri());
        String pathEnd = null;
        if (matcher.find()) pathEnd = matcher.group(1);
        return request.getMethod() + " " + pathEnd + " " + "HTTP/1.1";

    }

    public String getHost() {
        String uriRegexp = "http[s]*:\\/\\/([\\.\\w\\d:\\-]+)[\\/\\w\\d\\-]+";
        Pattern pattern = Pattern.compile(uriRegexp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(request.getUri());
        String host = null;
        if (matcher.find()) host = matcher.group(1);
        return host;
    }

    public String getHeaders() {
        StringBuilder headers = new StringBuilder();
        headers.append(getRequestLine()).append("\n"); // very clever
        headers.append(getRequestLine()).append("\r\n");
        headers.append("Host: ").append(getHost()).append("\r\n");
        for (HttpHeaderField header : request.getHeaderFields()) {
            headers.append(header.getName()).append(": ").append(header.getValue()).append("\r\n");
        }
        headers.append("Connection: close").append("\r\n");
        headers.append("\r\n");
        return headers.toString();
    }

    private String getBody() {
        return request.getBody();
    }

    private String getCertHash() {
        return null;
    }

    private int getExceptionHandlingParam() {
        return TFConstantsIF.OC_CHECK_ALL_CERTIFICATE_EXCEPTIONS;
    }
}
