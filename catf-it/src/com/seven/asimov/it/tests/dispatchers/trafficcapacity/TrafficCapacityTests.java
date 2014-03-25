package com.seven.asimov.it.tests.dispatchers.trafficcapacity;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.testcases.TrafficCapacityTestCase;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.TestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrafficCapacityTests extends TrafficCapacityTestCase {

    private static final Logger logger = LoggerFactory.getLogger(TrafficCapacityTests.class.getSimpleName());

    public void test_001_HttpTrafficCapacity() throws Throwable {
        long minResponseDuration = -1;
        String massage = "";
        String resource = "asimov_http_traffic_capacity";
        String uri = createTestResourceUri(resource);
        HttpRequest request;
        HttpResponse response;
        try {
            for (int i = 0; i < ITERATION; i++) {
                request = createRequest().setUri(uri).setMethod("GET")
                        .addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("Random", TestUtil.generationRandomString())
                        .getRequest();
                response = sendHttpMiss(i + 1, request);
                if (minResponseDuration < 0) {
                    minResponseDuration = response.getDuration();
                } else if (minResponseDuration > response.getDuration()) {
                    minResponseDuration = response.getDuration();
                }
            }
            if (OCUtil.isOpenChannelRunning())
                massage = "Min http response duration with OC = " + minResponseDuration;
            else
                massage = "Min http response duration without OC = " + minResponseDuration;
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            logger.info(massage);
        }
    }

    public void test_002_HttpsTrafficCapacity() throws Throwable {
        long minResponseDuration = -1;
        String massage = "";
        String resource = "asimov_http_traffic_capacity";
        String uri = createTestResourceUri(resource, true);
        HttpRequest request;
        HttpResponse response;
        try {
            for (int i = 0; i < ITERATION; i++) {
                request = createRequest().setUri(uri).setMethod("GET")
                        .addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("Random", TestUtil.generationRandomString())
                        .getRequest();
                response = sendHttpsMiss(i + 1, request);
                if (minResponseDuration < 0) {
                    minResponseDuration = response.getDuration();
                } else if (minResponseDuration > response.getDuration()) {
                    minResponseDuration = response.getDuration();
                }
            }
            if (OCUtil.isOpenChannelRunning())
                massage = "Min https response duration with OC = " + minResponseDuration;
            else
                massage = "Min https response duration without OC = " + minResponseDuration;
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            logger.info(massage);
        }
    }

    public void test_003_HttpSizeTrafficCapacity() throws Throwable {
        long minResponseDuration = -1;
        long speed = 0;
        String massage = "";
        String resource = "asimov_http_traffic_capacity";
        String uri = createTestResourceUri(resource);
        HttpRequest request;
        HttpResponse response;
        try {
            for (int i = 0; i < ITERATION; i++) {
                request = createRequest().setUri(uri).setMethod("GET")
                        .addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("X-OC-ResponseContentSize", SIZE + ",c")
                        .addHeaderField("Random", TestUtil.generationRandomString())
                        .getRequest();
                response = sendHttpMiss(i + 1, request);
                if (minResponseDuration < 0) {
                    minResponseDuration = response.getDuration();
                } else if (minResponseDuration > response.getDuration()) {
                    minResponseDuration = response.getDuration();
                }
            }
            speed = SIZE / minResponseDuration;
            if (OCUtil.isOpenChannelRunning())
                massage = "Min http speed with OC = " + speed;
            else
                massage = "Min http speed without OC = " + speed;
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            logger.info(massage);
        }
    }

    public void test_004_HttpsSizeTrafficCapacity() throws Throwable {
        long minResponseDuration = -1;
        long speed = 0;
        String massage = "";
        String resource = "asimov_http_traffic_capacity";
        String uri = createTestResourceUri(resource, true);
        HttpRequest request;
        HttpResponse response;
        try {
            for (int i = 0; i < ITERATION; i++) {
                request = createRequest().setUri(uri).setMethod("GET")
                        .addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("X-OC-ResponseContentSize", SIZE + ",c")
                        .addHeaderField("Random", TestUtil.generationRandomString())
                        .getRequest();
                response = sendHttpsMiss(i + 1, request);
                if (minResponseDuration < 0) {
                    minResponseDuration = response.getDuration();
                } else if (minResponseDuration > response.getDuration()) {
                    minResponseDuration = response.getDuration();
                }
            }
            speed = SIZE / minResponseDuration;
            if (OCUtil.isOpenChannelRunning())
                massage = "Min https speed with OC = " + speed;
            else
                massage = "Min https speed without OC = " + speed;
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            logger.info(massage);
        }
    }

}