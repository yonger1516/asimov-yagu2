package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.tests.connectivity.ciphersuites.ZHttpsCipherSuiteTests;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.SocketFactoryUtil;
import com.seven.asimov.it.utils.TestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class ConnectivityCipherSuiteTestCase extends TcpDumpTestCase {

    private static final Logger logger = LoggerFactory.getLogger(ConnectivityCipherSuiteTestCase.class.getSimpleName());
    private static SocketFactoryUtil.CustomSocketFactory socketFactory = new SocketFactoryUtil.CustomSocketFactory();

    protected void testZHttpsCipherSuite(ZHttpsCipherSuiteTests.CipherSuite suite) throws Throwable {
        BufferedReader br = null;
        HttpsURLConnection connection = null;
        try {
            TestUtil.sleep(30 * 1000);
            socketFactory.setSupportedCipherSuites(new String[]{suite.name()});
            String resourceUrl = "https://" + AsimovTestCase.TEST_RESOURCE_HOST + "/asimov_it_cv_suite_" + suite.name();
            URL url = new URL(resourceUrl);
            PrepareResourceUtil.prepareResource(resourceUrl, false);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(30 * 1000);
            connection.setSSLSocketFactory(socketFactory);
            connection.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
            connection.connect();
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                body.append(line).append("\n");
            }
            logger.info("Cipher suite: " + connection.getCipherSuite());
            logger.info("Response body: " + body.toString());
            assertEquals("Cipher suite " + suite.name() + " applied:", suite.name(), connection.getCipherSuite());
            assertEquals("Received valid body", VALID_RESPONSE, body.toString().trim());
        } finally {
            if (br != null) br.close();
            if (connection != null) connection.disconnect();
        }
    }
}
