package com.seven.asimov.test.tool.constants;

import java.util.ArrayList;

public interface TestInformation {
    //=============================Smoke Tests=============================//
    public static final ArrayList<String> SMOKE_TESTS = new ArrayList<String>() {{
        add("Sends request every 10 second during 3 minute, checking that OC work stable");
        add("Sends request over SSL every 10 second during 3 minute, checking that OC work stable");
        add("Send request 1, get response from network, caching response by RFC, sleep on 200 milliseconds\n" +
                "Send request 2, get response from cache");
        add("Send request 1, get response from network, sleep on 67 seconds\n" +
                "Send request 2, get response from network, sleep on 67 seconds\n" +
                "Send request 3, get response from network, start polling, sleep on 67 seconds\n" +
                "Send request 4, get response from cache");
        add("Send request 1, get response from network, sleep on 45 seconds\n" +
                "Send request 2, get response from network, sleep on 45 seconds\n" +
                "Send request 3, get response from network, start polling, sleep on 45 seconds\n" +
                "Send request 4, get response from cache");
        add("Send request 1, get response from network, sleep on 120 seconds\n" +
                "Send request 2, get response from network, start polling sleep on 120 seconds\n" +
                "Send request 3, get response from cache, sleep on 120 seconds\n" +
                "Send request 4, get response from cache");
//    add("Send request 1, get response from network, sleep on 100 seconds\n" +
//            "Send request 2, get response from network, sleep on 31 seconds\n" +
//            "Send request 3, get response from network, sleep on 100 seconds\n" +
//            "Send request 4, get response from network, sleep on 31 seconds\n" +
//            "Send request 5, get response from network, start polling, sleep on 100 seconds\n" +
//            "Send request 6, get response from cache");
        add("Send request 1 with ETAG in headers, get response from network, caching response by RFC, sleep on 30 seconds\n" +
                "Send request 2, get response from cache, sleep on 30 seconds\n" +
                "Send request 3, get response from cache, sleep on 30 seconds\n" +
                "Send request 4, get response from cache, sleep on 30 seconds\n" +
                "Resource expired by RFC\n" +
                "Send request 5, get response from network, caching response by RFC, sleep on 30 seconds\n" +
                "Send request 6, get response from cache, sleep on 30 seconds\n" +
                "Send request 7, get response from cache");
        add("Send request 1, get response from network, sleep on 30 seconds\n" +
                "Send request 2, get response from network, sleep on 30 seconds\n" +
                "Send request 3, get response from network, start polling, sleep on 30 seconds\n" +
                "Send request 4, get response from cache, sleep on 30 seconds\n" +
                "Invalidate resource, get invalidate without cache\n" +
                "Send request 5, get response from cache with new body");
        add("Send request 1, get response from network, sleep on 30 seconds\n" +
                "Send request 2, get response from network, sleep on 30 seconds\n" +
                "Send request 3, get response from network, start polling, sleep on 30 seconds\n" +
                "Send request 4, get response from cache, sleep on 30 seconds\n" +
                "Invalidate resource, simulate sms with invalidate without cache\n" +
                "Send request 5, get response from network");
        add("Send request 1 with not normalize uri, get response from network, sleep on 5 seconds\n" +
                "Send request 2 with not normalize uri, get response from network, sleep on 5 seconds\n" +
                "Send request 3 with not normalize uri, get response from network, start polling, sleep on 5 seconds\n" +
                "Send request 4 with not normalize uri, get response from cache");
        add("Send request 1, get response from network, caching response by RFC, sleep on 200 milliseconds\n" +
                "Send request 2, get response from cache");
        add("Send request 1, get response from network, sleep on 45 seconds\n" +
                "Send request 2, get response from network, sleep on 45 seconds\n" +
                "Send request 3, get response from network, start polling, sleep on 45 seconds\n" +
                "Send request 4, get response from cache");
        add("Send 1 POST request with body 1024 bytes. Check for correct response");
    }};
    //=====================================================================//
//=============================Certf Tests=============================//
    public static final ArrayList<String> ENV_TESTS = new ArrayList<String>() {{
        add("Install OC and check that it successfully started");
        add("Checks that after OC successfully registered with server MSISDN validation will send");
        add("Checks that after success MSISDN validation OC Client send request for policy update and getting them");
        add("Send 55 same requests to different URIs, get 55 responses from server\n" +
                "Sleep on 200 millisecond after every response\n" +
                "Check that after hundredth CRCS record OC send it to server\n" +
                "Check that OC successfully remove CRCS records from database after sending");
        add("Send request 1, get response from network, caching response by RFC, sleep on 200 milliseconds\n" +
                "Send request 2, get response from cache");
        add("Send request 1, get response from network, caching response by RFC, sleep on 200 milliseconds\n" +
                "Send request 2, get response from cache");
        add("Send request 1 with not normalize uri, get response from network, sleep on 65 seconds\n" +
                "Send request 2 with not normalize uri, get response from network, sleep on 65 seconds\n" +
                "Send request 3 with not normalize uri, get response from network, start polling, sleep on 65 seconds\n" +
                "Send request 4 with not normalize uri, get response from cache");
        add("Send request 1 with not normalize uri, get response from network, sleep on 65 seconds\n" +
                "Send request 2 with not normalize uri, get response from network, sleep on 65 seconds\n" +
                "Send request 3 with not normalize uri, get response from network, start polling, sleep on 65 seconds\n" +
                "Send request 4 with not normalize uri, get response from cache");
        add("Send request 1, get response from network, sleep on 30 seconds\n" +
                "Send request 2, get response from network, sleep on 30 seconds\n" +
                "Send request 3, get response from network, start polling, sleep on 30 seconds\n" +
                "Send request 4, get response from cache, sleep on 30 seconds\n" +
                "Invalidate resource, get invalidate without cache\n" +
                "Send request 5, get response from cache with new body");
        add("Send request 1, get response from network, sleep on 30 seconds\n" +
                "Send request 2, get response from network, sleep on 30 seconds\n" +
                "Send request 3, get response from network, start polling, sleep on 30 seconds\n" +
                "Send request 4, get response from cache, sleep on 30 seconds\n" +
                "Invalidate resource, simulate sms with invalidate without cache\n" +
                "Send request 5, get response from network");
    }};
    //=====================================================================//
//=============================Sanity Tests============================//
    public static final ArrayList<String> SANITY_TESTS = new ArrayList<String>() {{
        add("Send HTTP request with headers: Content-lenght: 1024 Connection: Keep-Alive\n" +
                "When response is receiving, kill process of http\n" +
                "Error should be received\nValid response shouldnt be received");
        add("Send HTTP request #1 with header: Connection: close\n" +
                "While socket is opened, send HTTP request #2 with header: Content-lenght: 512\n" +
                "Response for request #1 should be received\n" +
                "Response for request #2 should be received");
        add("end HTTP request #1 with header: Cache-control:public Content-lenght: 128\n" +
                "Send HTTP request #2 with header: Cache-control: max-stale\n" +
                "Response for request #1 should be received from network, cached by RFC\n" +
                "Response for request #2 should be received from network, cached by RFC");
        add("Add com.seven.asimov.test.tool.it to ssl branch of PMS\n" +
                "Send HTTPS request with headers: Connection: Keep-Alive\n" +
                "Response for request should be received. SSL handshake should be done correctly\n" +
                "NetLog with proxy_ssl_local_hs operation type should be observed in logcat for com.seven.asimov.test.tool.it");
        add("Be sure that com.seven.asimov.test.tool is not added to ssl branch of PMS\n" +
                "Send 4 HTTPS requests\nAll NetLog records for com.seven.asimov.test.tool should have proxy_stream operation type in logcat");
        add("Mobile network is avaliable\n" +
                "Set such policy: asimov@interseption@bypass_list = com.seven.asimov.test.tool\n" +
                "Send HTTP request #1\nSwitch to Wi-Fi\nSend HTTP request #2\n" +
                "Policy should be received and applied\nResponses for both requests should be received\n" +
                "Both HTTP transactions should be bypassed by OC");
        add("Policy should be configured:\n" +
                "asimov@enabled=0\n" +
                "asimov@interception@ochttpd@interception ports: 443\n" +
                "asimov@interception@ochttpd@type: 1\n" +
                "asimov@interception@ochttpd@z_order:0\n" +
                "asimov@interception@ocshttpd@interception ports: 80\n" +
                "asimov@interception@ocshttpd@type: 1\n" +
                "asimov@interception@ocshttpd@z_order:0\n" +
                "Wait 1 minute\n" +
                "Configure policy rule: asimov@enabled=1\nSend HTTP request");
        add("Active interface is 3G\nPolicy should be configured: client.openchannel.roaming_wifi_failover.enabled=1 " +
                "client.openchannel.roaming_wifi_failover.actions=1\nDisable input traffic from relay\nImmediately turn on wifi\n" +
                "After one negative attempt to connect to Relay, client should start failover\nSuch records should be in logcat");
        add("Active interface is 3G\nPolicy should be configured:\n" +
                "client.openchannel.roaming_wifi_failover.enabled=1\n" +
                "client.openchannel.roaming_wifi_failover.actions=2\nDisable input traffic from relay\nImmediately turn on wifi\n" +
                "Enable input traffic from relay\nAfter one negative attempt to connect to Relay, client should start failover");
        add("Policy should be configured: asimov@http@cache_invalidate_aggressiveness=1\n" +
                "Screen is switched off\nSend 4 HTTP requests with delay 30 sec between each other\n" +
                "Kill OC and dispatchers processes\nWait for OC to be restarted\nMake 1 request for the same resource\n" +
                "Responses for requests 1st – 3rd should be received from network\n" +
                "Response for the 4th request should be received from cache\n" +
                "fter OCE restart, all polling models CE should be loaded from database\n" +
                "Response for the 5th request should be received from cache");
        add("Send 3 HTTP requests with parameters: Pattern [0,70,70] Delay [65,65,65]\n" +
                "Set such policy: asimov@transparent = 1\n" +
                "LP should be detected after 2nd  request and polling should start after receiving response\n" +
                "Response for the 3rd request should be received from cache\n" +
                "Policy should be received and applied\nStop poll should be sent to the server, RR should be deactivated");
        add("Policy should be configured: asimov@http@cache_invalidate_aggressiveness=1\n" +
                "Screen is switched off\nCreate 1st thread. Start polling with pattern [0,34,34]\n" +
                "Create 2nd  thread. Start polling with pattern [0,68,68]\n" +
                "Create 3rd   thread. Start polling with pattern [0,68,68], Delay [61,61,61]\n" +
                "After receiving response for 7th RMP request, change resources for all test suites\n" +
                "Turn screen ON in 15 sec after 10th RMP request\n" +
                "Set such policy: asimov@transparent = 1 after receiving response for 14th  RMP request");
    }};
//=====================================================================//
}
