package com.seven.asimov.it.utils.customservice;

import com.seven.asimov.it.base.https.CertificateInfo;

/**
 * <P>Holds information about
 * <a href="https://matrix.seven.com/display/Eng/Dedicated+httpserver+-+dev-testrunner#Dedicatedhttpserver-dev-testrunner-CustomServices">Custom Service</a>.</P>
 */
public class CustomService {
    private int port;
    private String session;
    private CertificateInfo[] crtChain;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public CertificateInfo[] getCrtChain() {
        return crtChain;
    }

    public void setCrtChain(CertificateInfo[] crtChain) {
        this.crtChain = crtChain;
    }

    public CustomService(int port, String session, CertificateInfo[] crtChain) {
        this.port = port;
        this.session = session;
        this.crtChain = crtChain;
    }
}
