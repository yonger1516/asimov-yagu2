package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.ShellUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.logcat.tasks.httpsTasks.FcFcnTask;
import com.seven.asimov.it.utils.logcat.tasks.httpsTasks.FclLHSRTask;
import com.seven.asimov.it.utils.logcat.tasks.httpsTasks.FclPostponed;
import com.seven.asimov.it.utils.logcat.tasks.httpsTasks.HttpsFCLTask;
import com.seven.asimov.it.utils.logcat.wrappers.FcFcnWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.FclLHSRWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.FclPostponedWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.StreamWrapper;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpsHandshakeTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(HttpsHandshakeTestCase.class.getSimpleName());
    private static CustomSocketFactory socketFactory = new CustomSocketFactory();
    protected final String SUITE = "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA";
    protected static final int MIN_PERIOD = 90 * 1000;

    public Integer getMaxRejectNumber() {
        return TFConstantsIF.MAX_REJECT_NUMBER;
    }

    public Integer getBlacklistPeriod() {
        return TFConstantsIF.BLACKLIST_PERIOD;
    }

    protected boolean isVerdictFCN(HttpsFCLTask httpsFCLTask, FcFcnTask fcFcnTask) {
        int verdict = 0;
        if (!httpsFCLTask.getLogEntries().isEmpty() && !fcFcnTask.getLogEntries().isEmpty()) {
            for (FcFcnWrapper entry : fcFcnTask.getLogEntries()) {
                if (entry.getVerdict().equals("FCN")) {
                    verdict++;
                }
            }
        }
        return verdict > 0;
    }

    protected boolean checkLocalHsRes(FclLHSRTask fclLHSRTask, String localHsRes) {
        return checkLocalHsRes(fclLHSRTask, localHsRes, 1);
    }

    protected boolean checkFclPostponed(FclPostponed fclPostponed, int nFclPostponed) {
        int result = 0;
        if (!fclPostponed.getLogEntries().isEmpty()) {
            for (FclPostponedWrapper entry : fclPostponed.getLogEntries()) {
                result++;
            }
        }
        return result == nFclPostponed;
    }

    protected boolean checkLocalHsRes(FclLHSRTask fclLHSRTask, String localHsRes, int count) {
        int result = 0;
        if (!fclLHSRTask.getLogEntries().isEmpty()) {
            for (FclLHSRWrapper entry : fclLHSRTask.getLogEntries()) {
                if (entry.getLocalHsRes().contains(localHsRes)) {
                    result++;
                }
            }
        }
        return result >= count;
    }

    protected boolean isTrafficInStream(List<StreamWrapper> streamEntries) {
        boolean presentNAQ = false;
        boolean presentNAR = false;
        if (streamEntries != null) {
            for (StreamWrapper entry : streamEntries) {
                logger.info(entry.getTask() + " - size");
                logger.info(entry.getTask());
                if (entry.getTask().equals("NAQ")) {
                    presentNAQ = true;
                }
                if (entry.getTask().equals("NAR")) {
                    presentNAR = true;
                }
            }
        }
        return presentNAQ && presentNAR;
    }

    private Runnable getHttpsRequestThread(final String resourceUrl) throws Throwable {
        return new Runnable() {
            @Override
            public void run() {
                HttpRequest request = createRequest().setUri(resourceUrl)
                        .setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
                try {
                    sendHttpsRequest(request);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

            }
        };
    }

    protected void simultaneousRejectedHttpsConnection(int nHttpsConnection, String resourceUrl) throws Throwable {
        ExecutorService executorService = Executors.newFixedThreadPool(nHttpsConnection);
        for (int i = 0; i < nHttpsConnection; i++) {
            executorService.submit(getRejectedHttpsConnectionThread(resourceUrl));
        }
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);
    }

    protected void sendSimultaneousHttpsRequest(int normalRequestNumber, int rejectedRequestNumber, String resourceUrl) throws Throwable {
        ExecutorService executorService = Executors.newFixedThreadPool(normalRequestNumber + rejectedRequestNumber);
        for (int i = 0; i < normalRequestNumber; i++) {
            executorService.submit(getHttpsRequestThread(resourceUrl));
        }
        TestUtil.sleep(1000);
        for (int i = 0; i < rejectedRequestNumber; i++) {
            executorService.submit(getRejectedHttpsConnectionThread(resourceUrl));
        }
        executorService.shutdown();
        if (!executorService.awaitTermination(90, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }
    }

    private Runnable getRejectedHttpsConnectionThread(final String resourceUrl) {
        return new Runnable() {
            @Override
            public void run() {
                HttpsURLConnection connection = null;
                InputStream inputStream = null;
                try {
                    logger.info("Generate https connections");
                    logger.info("GET " + resourceUrl);
                    socketFactory.setSupportedCipherSuites(new String[]{SUITE});
                    URL url = new URL(resourceUrl);
                    connection = (HttpsURLConnection) url.openConnection();
                    connection.setSSLSocketFactory(socketFactory);
                    connection.setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return true;
                        }
                    });
                    connection.setReadTimeout(15 * 1000);
                    inputStream = connection.getInputStream();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Throwable e) {
                    logger.info("Connection closed");
                } finally {
                    if (connection != null) connection.disconnect();
                    if (inputStream != null) try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    protected void sendHttpsRequest(int number, String resourceUrl) throws Throwable {
        for (int i = 0; i < number; i++) {
            HttpRequest request = createRequest().setUri(resourceUrl)
                    .setMethod("GET").addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
            sendHttpsRequest(request);
        }
        Thread.sleep(10 * 1000);
    }

    protected void sendRejectedHttpsRequest(int number, String resourceUrl) throws Throwable {
        for (int i = 0; i < number; i++) {
            try {
                sendRejectedHttpsRequest(SUITE, resourceUrl);
            } catch (Throwable e) {
                logger.info("Connection closed");
            }
        }
        Thread.sleep(10 * 1000);
    }

    protected void sendRejectedHttpsRequest(String suite, String resourceUrl) throws Throwable {
        HttpsURLConnection connection = null;
        InputStream inputStream = null;
        try {
            logger.info("Generate https connections");
            logger.info("GET " + resourceUrl);
            socketFactory.setSupportedCipherSuites(new String[]{suite});
            URL url = new URL(resourceUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(socketFactory);
            connection.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
            connection.setReadTimeout(15 * 1000);
            inputStream = connection.getInputStream();
        } finally {
            if (connection != null) connection.disconnect();
            if (inputStream != null) inputStream.close();
        }
    }

    private static class CustomSocketFactory extends SSLSocketFactory {
        private String[] supportedCipherSuites;
        private SSLContext sslContext;
        private SSLSocketFactory factory;

        public CustomSocketFactory() {
            TrustManager trustFalse = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    throw new CertificateException();
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    throw new CertificateException();
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{trustFalse}, null);
            } catch (NoSuchAlgorithmException e) {
                logger.info("Provider not support TLS protocol!" + ExceptionUtils.getStackTrace(e));
            } catch (KeyManagementException e) {
                logger.info("Failed to init SocketFactory!" + ExceptionUtils.getStackTrace(e));
            }
            factory = sslContext.getSocketFactory();
            this.supportedCipherSuites = factory.getSupportedCipherSuites();
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return supportedCipherSuites;
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return supportedCipherSuites;
        }

        @Override
        public Socket createSocket(Socket socket1, String s, int i, boolean b) throws IOException {
            Socket socket = factory.createSocket(socket1, s, i, b);
            ((SSLSocket) socket).setEnabledCipherSuites(supportedCipherSuites);
            return socket;
        }

        @Override
        public Socket createSocket(String s, int i) throws IOException {
            Socket socket = factory.createSocket(s, i);
            ((SSLSocket) socket).setEnabledCipherSuites(supportedCipherSuites);
            return socket;
        }

        @Override
        public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException {
            Socket socket = factory.createSocket(s, i, inetAddress, i1);
            ((SSLSocket) socket).setEnabledCipherSuites(supportedCipherSuites);
            return socket;
        }

        @Override
        public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
            Socket socket = factory.createSocket(inetAddress, i);
            ((SSLSocket) socket).setEnabledCipherSuites(supportedCipherSuites);
            return socket;
        }

        @Override
        public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException {
            Socket socket = factory.createSocket(inetAddress, i, inetAddress1, i1);
            ((SSLSocket) socket).setEnabledCipherSuites(supportedCipherSuites);
            return socket;
        }

        public void setSupportedCipherSuites(String[] cipherSuites) {
            this.supportedCipherSuites = cipherSuites;
        }
    }

    protected Thread rebootOpenChannel() throws Exception {
        return new Thread() {
            @Override
            public void run() {
                Integer pid1;
                Integer pid2;
                Map<String, Integer> processes = OCUtil.getRunningProcesses(true);
                if (processes.get("occ") != null || processes.get("com.seven.asimov") != null) {
                    pid1 = processes.get("occ");
                    logger.info("Process occ = " + pid1);
                    String[] killPid1 = {"su", "-c", "kill " + pid1};
                    pid2 = processes.get("com.seven.asimov");
                    logger.info("Process com.seven.asimov = " + pid2);
                    String[] killPid2 = {"su", "-c", "kill " + pid2};

                    try {
                        Runtime.getRuntime().exec(killPid1);
                        Runtime.getRuntime().exec(killPid2);
                    } catch (IOException io) {
                        logger.info("Killing process is failed due to : " + ExceptionUtils.getStackTrace(io));
                    }
                }
            }
        };
    }

    protected void resetDB() throws Exception {
        List<String> command = new ArrayList<String>();
        command.add("rm -r /data/misc/openchannel/oc_engine.db");
        ShellUtil.execWithCompleteResult(command, true);
    }
}
