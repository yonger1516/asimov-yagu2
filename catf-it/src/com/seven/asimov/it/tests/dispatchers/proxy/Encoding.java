package com.seven.asimov.it.tests.dispatchers.proxy;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.PunycodeUtil;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.util.ArrayList;
import java.util.List;

import static com.seven.asimov.it.asserts.CATFAssert.assertStatusCode;

public class Encoding extends TcpDumpTestCase {

    public void testEncodingTR_UTF8_BasicMultilingualPlane() throws Throwable {
        String RESOURCE_URI_LATIN = "asimov_ga_it";
        String RESOURCE_URI_CYRILLIC = "asimov_ga_it_\u0410\u0411\u0412\u0413\u0414\u0415\u0430\u0431\u0432\u0433";
        String RESOURCE_URI_CYRILLIC_ADDITIONAL = "asimov_ga_it_\u04F0\u04D0\u0500\u0501\u04E0\u04E2\u04EA\u04F0\u2DE0\uA650";
        String RESOURCE_URI_HEBREW = "asimov_ga_it_\u0590\u05A0\u05B0\u05D0\u05D1\u05D2\u05E5\uFB10\uFB20\uFB3E";
        String RESOURCE_URI_ARABIC = "asimov_ga_it_\u0600\u06A0\u06BA\u0690\u0620\u0670\u068E0\u06FF\u0689\u069B";
        String RESOURCE_URI_DEVANAGARI = "asimov_ga_it_\u0901\u091A\u092E\u096B\u0949\u094A\u094B\u094C\u094D\u096F";

        String RESOURCE_URI_PC = PunycodeUtil.encode(RESOURCE_URI_LATIN);
        String RESOURCE_URI_CYRILLIC_PC = PunycodeUtil.encode(RESOURCE_URI_CYRILLIC);
        String RESOURCE_URI_CYRILLIC_ADDITIONAL_PC = PunycodeUtil.encode(RESOURCE_URI_CYRILLIC_ADDITIONAL);
        String RESOURCE_URI_HEBREW_PC = PunycodeUtil.encode(RESOURCE_URI_HEBREW);
        String RESOURCE_URI_ARABIC_PC = PunycodeUtil.encode(RESOURCE_URI_ARABIC);
        String RESOURCE_URI_DEVANAGARI_PC = PunycodeUtil.encode(RESOURCE_URI_DEVANAGARI);

        List<String> uris = new ArrayList<String>();
        uris.add(createTestResourceUri(RESOURCE_URI_PC));
        uris.add(createTestResourceUri(RESOURCE_URI_ARABIC_PC));
        uris.add(createTestResourceUri(RESOURCE_URI_CYRILLIC_PC));
        uris.add(createTestResourceUri(RESOURCE_URI_CYRILLIC_ADDITIONAL_PC));
        uris.add(createTestResourceUri(RESOURCE_URI_DEVANAGARI_PC));
        uris.add(createTestResourceUri(RESOURCE_URI_HEBREW_PC));

        for (String uri : uris)
            PrepareResourceUtil.prepareResource(uri, false);

        int responseId = 1;

        for (String uri : uris) {
            HttpRequest request = createRequest().setUri(uri).setMethod(HttpGet.METHOD_NAME)
                    .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

            checkMiss(request, responseId++);
        }
    }

    public void testEncodingRR_UTF8_BasicMultilingualPlane_Cyrillic() throws Throwable {

        String uri1 = "http://\u043F\u0440\u0435\u0437\u0438\u0434\u0435\u043D\u0442.\u0440\u0444";
        String uri2 = "http://\u043F\u0440\u0430\u0432\u0438\u0442\u0435\u043B\u044C\u0441\u0442\u0432\u043E.\u0440\u0444";
        String uri3 = "http://\u043A\u0446.\u0440\u0444";
        String uri4 = "http://\u0440\u0441\u043F\u043F.\u0440\u0444";
        String uri5 = "http://\u0434\u0438\u0430\u043B\u043E\u0433\u0443\u0440\u0430\u043B.\u0440\u0444";

        String uri1_pc = PunycodeUtil.cyrillicToPunicode(uri1);
        String uri2_pc = PunycodeUtil.cyrillicToPunicode(uri2);
        String uri3_pc = PunycodeUtil.cyrillicToPunicode(uri3);
        String uri4_pc = PunycodeUtil.cyrillicToPunicode(uri4);
        String uri5_pc = PunycodeUtil.cyrillicToPunicode(uri5);

        List<String> uris = new ArrayList<String>();
        uris.add(uri1_pc);
        uris.add(uri2_pc);
        uris.add(uri3_pc);
        uris.add(uri4_pc);
        uris.add(uri5_pc);

        int responseId = 1;
        for (String uri : uris) {
            HttpRequest request = createRequest().setUri(uri).setMethod(HttpGet.METHOD_NAME).getRequest();
            HttpResponse response = sendRequest2(request);
            if (uri.equalsIgnoreCase(uri3_pc))
                assertStatusCode(responseId++, HttpStatus.SC_MOVED_TEMPORARILY, response);
            else if (uri.equals(uri2_pc)) {
                assertStatusCode(responseId++, HttpStatus.SC_MOVED_PERMANENTLY, response);
            } else
                assertStatusCode(responseId++, HttpStatus.SC_OK, response);
            logSleeping(5 * 1000);
        }
    }

    public void testInvalidCodingTR_UTF8() throws Throwable {

        byte[] resource = new byte[]{(byte) 0xEA, (byte) 0x78, (byte) 0xE2, (byte) 0x6D, (byte) 0x70, (byte) 0x6C,
                (byte) 0xEB};
        String RESOURCE = "asimov_it_";
        String uri = createTestResourceUri(RESOURCE);
        HttpRequest request = createRequest().setUri(uri).setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        HttpResponse response = sendRequest2(request, false, false, SMALL_TIMEOUT);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());

        RESOURCE += new String(resource, "ISO-8859-1");
        uri = createTestResourceUri(RESOURCE);

        request = createRequest().setUri(uri).setMethod(HttpGet.METHOD_NAME)
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();

        response = sendRequest2(request, false, false, SMALL_TIMEOUT);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());

    }
}
