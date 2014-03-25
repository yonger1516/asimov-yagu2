package com.seven.asimov.it.tests.dispatchers.proxy;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.base.Base64;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.date.DateUtil;
import org.apache.http.HttpStatus;

import java.net.URLEncoder;
import java.util.Date;

import static com.seven.asimov.it.base.constants.TFConstantsIF.CRLF;

public class ResourceWithDiffResponses  extends TcpDumpTestCase {

    /**
     * test for ASMV-7378
     *
     * @throws Exception
     */
    @LargeTest
    public void testResourseWithDiffResponses() throws Exception {
        final String RESOURCE_URI = "ASMV_7378";
        final String uri = createTestResourceUri(RESOURCE_URI);
        PrepareResourceUtil.prepareResource(uri, false, 70);

        try {
            String expectedPart = "%s" + CRLF + "Date: " + "%s" + CRLF + "Content-Length: "
                    + "ASMV_7378".getBytes().length + CRLF + "Connection: close" + CRLF + CRLF + "ASMV_7378";
            String statusNotFound = "HTTP/1.1 404 Not Found";
            String statusOK = "HTTP/1.1 200 OK";
            for (int i = 0; i < 12; i++) {
                boolean isOk = (i % 2 == 0);
                String expectedEncoded = URLEncoder.encode(Base64.encodeToString(
                        String.format(expectedPart, isOk ? statusOK : statusNotFound,
                                DateUtil.format(new Date(System.currentTimeMillis()))).getBytes(), Base64.DEFAULT));

                HttpRequest request = createRequest().setUri(uri).setMethod("GET")
                        .addHeaderField("X-OC-ContentEncoding", "identity").addHeaderField("X-OC-Sleep", "70")
                        .addHeaderField("X-OC-Raw", expectedEncoded).getRequest();
                checkMiss(request, i + 1, isOk ? HttpStatus.SC_OK : HttpStatus.SC_NOT_FOUND, null);
            }
        } finally {
            PrepareResourceUtil.prepareResource(uri, true);
        }
    }
}
