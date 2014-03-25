package com.seven.asimov.it.testcases;


import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.sessionBasicTasks.RequestMD5Task;
import com.seven.asimov.it.utils.logcat.tasks.sessionBasicTasks.ResponseMD5Task;
import com.seven.asimov.it.utils.logcat.wrappers.RequestMD5Wrapper;
import com.seven.asimov.it.utils.logcat.wrappers.ResponseMD5Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NormalizationTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(NormalizationTestCase.class.getSimpleName());

    protected void compareMD5(String resource, HttpHeaderField[] headersForTheFirstRequest, HttpHeaderField[] headersForTheSecondRequest, boolean equalRequestMD5, boolean equalResponseMD5) throws Exception {
        final String uri = createTestResourceUri(resource);
        final HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-Encoding", "identity").getRequest();
        RequestMD5Task requestMD5Task = new RequestMD5Task();
        ResponseMD5Task responseMD5Task = new ResponseMD5Task();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), requestMD5Task, responseMD5Task);
        logcatUtil.start();
        logSleeping(5 * 1000);
        HttpRequest curentRequest = request.copy();

        for (int i = 0; headersForTheFirstRequest != null && i < headersForTheFirstRequest.length; i++) {
            curentRequest.addHeaderField(headersForTheFirstRequest[i]);
        }

        checkMiss(curentRequest, 1, (int) MIN_RMP_PERIOD);
        curentRequest = request.copy();

        for (int i = 0; headersForTheSecondRequest != null && i < headersForTheSecondRequest.length; i++) {
            curentRequest.addHeaderField(headersForTheSecondRequest[i]);
        }

        checkMiss(curentRequest, 2, (int) MIN_RMP_PERIOD);
        logSleeping(5 * 1000);
        logcatUtil.stop();
        checkRequestMD5(requestMD5Task, equalRequestMD5, 2);
        checkResponseMD5(responseMD5Task, equalResponseMD5, 2);
    }

    private void checkResponseMD5(ResponseMD5Task responseMD5Task, boolean equalResponseMD5, int amount) {
        List<ResponseMD5Wrapper> responseEntries = responseMD5Task.getLogEntries();
        logger.debug("Size of response entries: " + Integer.toString(responseEntries.size()));
        assertTrue(String.format("Should be reported %d MD5 of responses", amount), responseEntries.size() == amount);
        if (equalResponseMD5) {
            for (int i = 0; i < responseEntries.size() - 1; i++) {
                assertTrue("Responses should have the same MD5", responseEntries.get(i).getMD5().equals(responseEntries.get(i + 1).getMD5()));
            }

        } else {
            for (int i = 0; i < responseEntries.size() - 1; i++) {
                assertTrue("Responses should have different MD5", !responseEntries.get(i).getMD5().equals(responseEntries.get(i + 1).getMD5()));
            }
        }
    }

    private void checkRequestMD5(RequestMD5Task requestMD5Task, boolean equalRequestMD5, int amount) {
        List<RequestMD5Wrapper> requestEntries = requestMD5Task.getLogEntries();
        logger.debug("Size of request entries: " + Integer.toString(requestEntries.size()));
        assertTrue(String.format("Should be reported %d MD5 of requests", amount), requestEntries.size() == amount);
        if (equalRequestMD5) {
            for (int i = 0; i < requestEntries.size() - 1; i++) {
                assertTrue("Requests should have the same MD5", requestEntries.get(i).getMD5().equals(requestEntries.get(i + 1).getMD5()));
            }
        } else {
            for (int i = 0; i < requestEntries.size() - 1; i++) {
                assertTrue("Requests should have different MD5", !requestEntries.get(i).getMD5().equals(requestEntries.get(i + 1).getMD5()));
            }
        }
    }
}
