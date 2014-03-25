package com.seven.asimov.it.tests.stream.nondetailed;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
     /*
        No Pre-requests
     */

    /**
     * 1. DCSM should wait for incoming from an application data for at most a configurable timeout (currently equal to CTR timeout) that is counted from the CSM's creation time.
     * 2. If any data arrived, DCSM should detect the protocol as usually.
     * 3. If no data arrived, DCSM has to initiate OUT connection according to the CTR's destination data pair and switch into STREAM mode of operation.
     **/
public class ServerInitiatedProtocolsDetection extends TcpDumpTestCase {
        /**
         * Steps
         * <p/>
         * 1. Send reguest with delay after handshake equal 10 sec
         * 2. Send request to the same resource
         * Observe the logcat
         * <p/>
         * Results
         * 1. DCSM has to initiate OUT connection according to the CTR's destination data pair and switch into STREAM mode of operation. Response shouldn`t be cached
         * 2. Check miss
         **/
        public void test_001_Stream() throws Throwable {

            final String RESOURCE_URI = "asimov_it_test_001_SP_PROXYING";
            String uri = createTestResourceUri(RESOURCE_URI);

            HttpRequest request = createRequest().setUri(uri).addHeaderField("Cache-Control" ,"max-age=500").getRequest();
            PrepareResourceUtil.prepareResource(uri, false);

            //miss
            sendRequest2(request, 10);
            //R1.2 miss
            checkMiss(request, 2, VALID_RESPONSE);
        }
        /**
         * <p>
         * Steps
         * <p/>
         * <p>
         * 1. Send reguest
         * 2. Send request to the same resource
         * Observe the logcat
         * <p/>
         * Results
         * 1. DCSM should detect the protocol as usually.
         * 2. Check hit
         **/
        public void test_002_Stream() throws Throwable {

            final String RESOURCE_URI = "asimov_it_test_002_SP_PROXYING";
            String uri = createTestResourceUri(RESOURCE_URI);

            HttpRequest request = createRequest().setUri(uri).addHeaderField("Cache-Control" ,"max-age=500").getRequest();
            PrepareResourceUtil.prepareResource(uri, false);

            //miss, cached by RFC
            sendRequest2(request, 0);
            // R1.2 hit
            checkHit(request, 2, VALID_RESPONSE);
        }

}
