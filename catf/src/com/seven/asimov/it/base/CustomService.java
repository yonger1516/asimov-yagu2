package com.seven.asimov.it.base;

import com.seven.asimov.it.base.https.CertificateInfo;
import org.apache.http.HttpStatus;

public class CustomService extends TcpDumpTestCase{
    public int port;
    public String session;
    public CertificateInfo[] crtChain;
    public String host;

    public CustomService(int port, String session, CertificateInfo[] crtChain, String host) {
        this.port = port;
        this.session = session;
        this.crtChain = crtChain;
        this.host = host;
    }

    public static final int CUSTOM_SERVICE_PORT_0 = 11400;
    private static final String SERVE_URI = "asimov_it_serve";
    public static final String[] CUSTOM_SERVICE_HOSTS = {
            "213.180.28.160",
            "213.180.28.161",
            "213.180.28.162",
            "213.180.28.163",
            "213.180.28.164",
            "213.180.28.165",
            "213.180.28.166",
            "213.180.28.167",
            "213.180.28.168",
            "213.180.28.169",
            "213.180.28.170"
    };

    /**
     * <P>Reserves Custom Service at first free port.</P>
     */
    public static CustomService reserveCustomSerivceParallel() throws Exception {
        HttpRequest request = createRequest().setMethod("GET").setUri(createTestResourceUri(SERVE_URI)).getRequest();
        request.addHeaderField(new HttpHeaderField("X-OC-ContentEncoding", "identity"));
        request.addHeaderField(new HttpHeaderField("X-OC-Reserve-Custom-Service", ""));
        HttpResponse resp = sendRequestParallel(request, false, true);
        assertEquals(HttpStatus.SC_OK, resp.getStatusCode());
        int port = Integer.parseInt(resp.getHeaderField("X-OC-Custom-Service-Port"));
        String session = resp.getHeaderField("X-OC-Custom-Service-Session");
        String host = null;
        if (port >= CUSTOM_SERVICE_PORT_0 && port < CUSTOM_SERVICE_PORT_0 + CUSTOM_SERVICE_HOSTS.length) {
            host = CUSTOM_SERVICE_HOSTS[port - CUSTOM_SERVICE_PORT_0];
        }
        return new CustomService(port, session, null, host);
    }

    /**
     * <P>Unreserves Custom Service. If service is running, also stops service.</P>
     *
     * @param customService {@link CustomService CustomService} to unreserve.
     */
    public static void unreserveCustomSerivceParallel(CustomService customService) {
        HttpRequest request = createRequest().setMethod("GET").setUri(createTestResourceUri(SERVE_URI)).getRequest();
        request.addHeaderField(new HttpHeaderField("X-OC-ContentEncoding", "identity"));
        request.addHeaderField(new HttpHeaderField("X-OC-Unreserve-Custom-Service", customService.session));
        try {
            sendRequestParallel(request, false, true);
        } catch (Exception e) {
            System.out.println("Exception unreserving custom service");
            e.printStackTrace();
        }
    }

    /**
     * <P>Method reserves and starts Custom Service. Each new service is started on first free port.</P>
     *
     * @param serviceName Defines required ssl protocol parameters. Default value is ",,,,,,".
     * @return Returns newly created {@link CustomService CustomService} object.
     * @throws Exception
     */
    public static CustomService reserveAndStartCustomSerivceParallel(String serviceName) throws Exception {
        CustomService service = reserveCustomSerivceParallel();
        service = startCustomSerivceParallel(service, serviceName);
        return service;
    }

    /**
     * <P>Starts new or activates already reserved Custom Service. If null is passed to customService parameter
     * and Custom Service with same serviceName exists, it will be be activated.
     * To activate Custom Service on free port use  {@link #reserveCustomSerivceParallel() reserveCustomSerivceParallel}
     * before calling this method.</P>
     *
     * @param customService {@link CustomService CustomService} object. If null is passed, method starts new service at first free port.
     *                      If already reserved CustomService is passed then it activated.
     * @param serviceName   SSL configuration for new Custom Service. Default values is ",,,,,,".
     * @return
     * @throws Exception
     */
    public static CustomService startCustomSerivceParallel(CustomService customService, String serviceName) throws Exception {
        String session = (customService == null || customService.session == null) ? "" : customService.session;
        HttpRequest request = createRequest().setMethod("GET").setUri(createTestResourceUri(SERVE_URI)).getRequest();
        request.addHeaderField(new HttpHeaderField("X-OC-ContentEncoding", "identity"));
        request.addHeaderField(new HttpHeaderField("X-OC-Start-Custom-Service", session + "," + serviceName));
        HttpResponse resp = sendRequestParallel(request, false, true);
        assertEquals("Start custom service returned code:" + resp.getStatusCode() + " expected:" + HttpStatus.SC_OK, HttpStatus.SC_OK, resp.getStatusCode());
        int port = Integer.parseInt(resp.getHeaderField("X-OC-Custom-Service-Port"));
        String respSession = resp.getHeaderField("X-OC-Custom-Service-Session");
        String host = null;
        if (port >= CUSTOM_SERVICE_PORT_0 && port < CUSTOM_SERVICE_PORT_0 + CUSTOM_SERVICE_HOSTS.length) {
            host = CUSTOM_SERVICE_HOSTS[port - CUSTOM_SERVICE_PORT_0];
        }
        if (customService != null) {
            if (customService.session != null) assertEquals(customService.session, respSession);
            if (customService.port > 0) assertEquals(customService.port, port);
        }
        //return new CustomService(port, respSession, CertUtils.parseCertificateInfo(resp.getBody()), host);
        return new CustomService(port, respSession, null, host);
    }


    /**
     * <P>Signals that service is not needed anymore.
     * If Custom Service was reserved, it will be closed. If not - it will be closed only if nobody else uses it.</P>
     *
     * @param customService {@link CustomService CustomService} to close.
     */
    public static void stopCustomSerivceSafelyParallel(CustomService customService) {
        try {
            HttpRequest request = createRequest().setMethod("GET").setUri(createTestResourceUri(SERVE_URI)).getRequest();
            request.addHeaderField(new HttpHeaderField("X-OC-Stop-Custom-Service", customService.session));
            sendRequestParallel(request, false, true);
        } catch (Exception e) {
            System.out.println("Exception stopping custom service");
            e.printStackTrace();
        }
    }
}
