package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.NetlogTask;
import com.seven.asimov.it.utils.logcat.tasks.streamTasks.StreamTask;
import com.seven.asimov.it.utils.logcat.wrappers.NetlogEntry;
import com.seven.asimov.it.utils.logcat.wrappers.OperationType;
import com.seven.asimov.it.utils.logcat.wrappers.StreamWrapper;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import static com.seven.asimov.it.base.constants.TFConstantsIF.DISPATCHERS_LOG_LEVEL;
import static com.seven.asimov.it.base.constants.TFConstantsIF.MINUTE;

public class StreamTestCase extends TcpDumpTestCase {

    private boolean appStatusChanged = false;
    private boolean presentFCL = false;
    private boolean presentNSQ = false;
    private boolean presentNSR = false;
    private boolean presentNAQ = false;
    private boolean presentNAR = false;
    private boolean presentNSC = false;

    /**
     * @param httpRequest - correct or incorect HTTP requets
     * @param array       - request in array of bytes
     * @param host        - test resourse host
     * @param port        - port to open connection
     * @param checkFCL    - if HTTPS dispatcher is enabled and https request
     * @param ssl         - variable to prepare ssl socket
     * @param version      - variable to build correct or incorrect http request(if true, http request should have HTTP 1.1 version, so, it should be correct request)
     * @throws Throwable
     */
    protected void sendAnyRequestByChosenPort(HttpRequest httpRequest, byte[] array, String host, int port, boolean checkFCL, boolean ssl, HTTP_VERSION version) throws Throwable {

        int SLEEP_DELAY = MINUTE * 4;
        if(DISPATCHERS_LOG_LEVEL > 5) {
            SLEEP_DELAY = MINUTE * 9;
        }
        boolean checkResponce = false;
        boolean HTTP11 = (version == HTTP_VERSION.HTTP11 && !ssl);
        int TIMEOUT = 20 * 1000;
        StreamTask streamTask = new StreamTask();
        NetlogTask netlogTask = new NetlogTask();

        LogcatUtil logcatUtil = new LogcatUtil(getContext(), streamTask, netlogTask);
        logcatUtil.start();

        SSLSocketFactory mSSLSocketFactory = null;
        SSLContext mSslContext;
        if (port == 443 || ssl) {
            mSslContext = SSLContext.getInstance("SSL");
            TrustManager[] tm = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }};
            mSslContext.init(null, tm, null);
            mSSLSocketFactory = mSslContext.getSocketFactory();
        }
        try {
            if ((port == 80 || port == 443) && httpRequest != null) {
                sendRequest2(httpRequest, MINUTE, version);
                checkResponce = true;
            } else {
                if(array == null) {
                    array = httpRequest.getFullRequest().getBytes();
                }
                sendRequest(array, host, mSSLSocketFactory, TIMEOUT, 16384, port);
            }
            Thread.sleep(SLEEP_DELAY);
            logcatUtil.stop();
            setEntriesToCheck(netlogTask.getLogEntries(), streamTask.getLogEntries(), host);
            checkTasks(checkFCL, checkResponce, HTTP11);
        } finally {
            if(logcatUtil.isRunning()) {
                logcatUtil.stop();
            }
            resetValues();
        }

    }

    protected void setEntriesToCheck(List<NetlogEntry> netlogEntries, List<StreamWrapper> streamEntries, String host) throws UnknownHostException {

        if (netlogEntries != null) {
            for (NetlogEntry entry : netlogEntries) {
                if ((entry.getOpType().equals(OperationType.proxy_stream)||entry.getOpType().equals(InetAddress.getAllByName(AsimovTestCase.TEST_RESOURCE_HOST))) && entry.getHost().equals(host)) {
                    appStatusChanged = true;
                }
            }
        }
        if (streamEntries != null) {
            for (StreamWrapper entry : streamEntries) {
                if (entry.getTask().equals("FCL")) {
                    presentFCL = true;
                }
                if (entry.getTask().equals("NAQ")) {
                    presentNAQ = true;
                }
                if (entry.getTask().equals("NAR")) {
                    presentNAR = true;
                }
                if (entry.getTask().equals("NSQ")) {
                    presentNSQ = true;
                }
                if (entry.getTask().equals("NSR")) {
                    presentNSR = true;
                }
                if (entry.getTask().equals("NSC")) {
                    presentNSC = true;
                }
            }
        }
    }

    protected void resetValues() {
        appStatusChanged = false;
        presentFCL = false;
        presentNSQ = false;
        presentNSR = false;
        presentNAQ = false;
        presentNAR = false;
        presentNSC = false;
    }

    protected void checkTasks(boolean FCL, boolean responce, boolean negative) {

        if(negative) {
            assertFalse("The status of application has been changed for 'proxy_stream' but it's not correct", appStatusChanged);
        }  else {
            assertTrue("The status of application hasn't been changed for 'proxy_stream'", appStatusChanged);

            if (FCL) {
                assertTrue("FCL task should be observed in log but wasn't", presentFCL);
            }

            assertTrue("Dispatcher should send NAQ to controller", presentNAQ);
            assertTrue("Dispatcher should received NAR from controller", presentNAR);

            if (responce) {
                assertTrue("Dispatcher should send NSQ to controller", presentNSQ);
                assertTrue("Dispatcher should received NSR from controller", presentNSR);
            } else {
                assertTrue("Dispatcher should received NSC from controller", presentNSC);
            }
        }
    }
}
