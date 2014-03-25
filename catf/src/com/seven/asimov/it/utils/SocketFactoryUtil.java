package com.seven.asimov.it.utils;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SocketFactoryUtil {
    private static final Logger logger = LoggerFactory.getLogger(SocketFactoryUtil.class.getSimpleName());

    public static class CustomSocketFactory extends SSLSocketFactory {

        private String[] supportedCipherSuites;
        private SSLContext sslContext;
        private SSLSocketFactory factory;

        public CustomSocketFactory() {
            TrustManager trustAll = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{trustAll}, null);
            } catch (NoSuchAlgorithmException e) {
                logger.error("Provider not support TLS protocol!" + ExceptionUtils.getStackTrace(e));
            } catch (KeyManagementException e) {
                logger.error("Failed to init SocketFactory!" + ExceptionUtils.getStackTrace(e));
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
}
