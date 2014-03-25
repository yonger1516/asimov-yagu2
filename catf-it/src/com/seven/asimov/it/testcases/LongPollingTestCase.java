package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;

import java.util.UUID;

public class LongPollingTestCase extends TcpDumpTestCase {
    public static void invalidateLongPoll(String uri) {
        try {
            UUID guid = UUID.randomUUID();
            String longPollInvalidateBody = guid.toString();
            HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                    .addHeaderField("X-OC-ContentEncoding", "identity")
                    .addHeaderField("X-OC-Freeze", "true")
                    .addHeaderField("X-OC-Sleep", "5")
                    .addHeaderField("X-OC-Stateless-Sleep", "true")
                    .addHeaderField("X-OC-ChangeResponseContent", longPollInvalidateBody)
                    .getRequest();
            sendRequest(request, null, false, true, Body.BODY);
        } catch (Exception e) {
            System.out.println("Invalidating resource, exception ignored");
            e.printStackTrace();
        }
    }
}
