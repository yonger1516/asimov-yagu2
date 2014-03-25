package com.seven.asimov.test.tool.core;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * EasyTrustManager.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public class EasyTrustManager implements X509TrustManager {

    private Request mRequest;

    // a custom TrustManager that accepts all certs, lifted
    // shamelessly from
    // http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e537

    public EasyTrustManager(Request request) {
        this.mRequest = request;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (mRequest.isRejectClientCert()) {
            throw new CertificateException("Rejecting client certificate by direct command!");
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (mRequest.isRejectServerCert()) {
            throw new CertificateException("Rejecting server certificate by direct command!");
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

}
