package com.seven.asimov.it.utils.customservice;


import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import org.apache.http.HttpStatus;

import static com.seven.asimov.it.asserts.CATFAssert.assertEquals;
import static com.seven.asimov.it.base.AsimovTestCase.*;

public class CustomServiceUtil {
    private static final String CUSTOM_SERVICE_URI = "custom_service";


    /**
     * <P>Reserves Custom Service at first free port.</P>
     */
    public static CustomService reserveCustomService() throws Exception {
        HttpRequest request = createRequest().setMethod("GET").setUri(createTestResourceUri(CUSTOM_SERVICE_URI)).getRequest();
        request.addHeaderField(new HttpHeaderField("X-OC-ContentEncoding", "identity"));
        request.addHeaderField(new HttpHeaderField("X-OC-Reserve-Custom-Service", ""));
        HttpResponse resp = sendRequestParallel(request, false, true);
        assertEquals("Failed to reserve Custom Service", HttpStatus.SC_OK, resp.getStatusCode());
        int port = Integer.parseInt(resp.getHeaderField("X-OC-Custom-Service-Port"));
        String session = resp.getHeaderField("X-OC-Custom-Service-Session");

        return new CustomService(port, session, null);
    }

    /**
     * <P>Unreserves Custom Service. If service is running, also stops service.</P>
     *
     * @param customService {@link CustomService CustomService} to unreserve.
     */
    public static void unreserveCustomService(CustomService customService) {
        HttpRequest request = createRequest().setMethod("GET").setUri(createTestResourceUri(CUSTOM_SERVICE_URI)).getRequest();
        request.addHeaderField(new HttpHeaderField("X-OC-ContentEncoding", "identity"));
        request.addHeaderField(new HttpHeaderField("X-OC-Unreserve-Custom-Service", customService.getSession()));
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
    public static CustomService reserveAndStartCustomService(String serviceName) throws Exception {
        CustomService service = reserveCustomService();
        service = startCustomService(service, serviceName);
        return service;
    }

    /**
     * <P>Starts new or activates already reserved Custom Service. If null is passed to customService parameter
     * and Custom Service with same serviceName exists, it will be be activated.
     * To activate Custom Service on free port use  {@link #reserveCustomService() reserveCustomService}
     * before calling this method.</P>
     *
     * @param customService {@link CustomService CustomService} object. If null is passed, method starts new service at first free port.
     *                      If already reserved CustomService is passed then it activated.
     * @param serviceName   SSL configuration for new Custom Service. Default values is ",,,,,,".
     * @return
     * @throws Exception
     */
    public static CustomService startCustomService(CustomService customService, String serviceName) throws Exception {
        String session = (customService == null || customService.getSession() == null) ? "" : customService.getSession();
        HttpRequest request = createRequest().setMethod("GET").setUri(createTestResourceUri(CUSTOM_SERVICE_URI)).getRequest();
        request.addHeaderField(new HttpHeaderField("X-OC-ContentEncoding", "identity"));
        request.addHeaderField(new HttpHeaderField("X-OC-Start-Custom-Service", session + "," + serviceName));
        HttpResponse resp = sendRequestParallel(request, false, true);
        assertEquals("Start custom service returned code:" + resp.getStatusCode() + " expected:" + HttpStatus.SC_OK, HttpStatus.SC_OK, resp.getStatusCode());
        int port = Integer.parseInt(resp.getHeaderField("X-OC-Custom-Service-Port"));
        String respSession = resp.getHeaderField("X-OC-Custom-Service-Session");

        if (customService != null) {
            if (customService.getSession() != null)
                assertEquals("Failed to start Custom service - wrong session id", customService.getSession(), respSession);
            if (customService.getPort() > 0)
                assertEquals("Failed to start Custom Service - wrong port", customService.getPort(), port);
        }

        return new CustomService(port, respSession, null);
    }

    /**
     * <P>Signals that service is not needed anymore.
     * If Custom Service was reserved, it will be closed. If not - it will be closed only if nobody else uses it.</P>
     *
     * @param customService {@link CustomService CustomService} to close.
     */
    public static void stopCustomService(CustomService customService) {
        try {
            HttpRequest request = createRequest().setMethod("GET").setUri(createTestResourceUri(CUSTOM_SERVICE_URI)).getRequest();
            request.addHeaderField(new HttpHeaderField("X-OC-Stop-Custom-Service", customService.getSession()));
            sendRequestParallel(request, false, true);
        } catch (Exception e) {
            System.out.println("Exception stopping custom service");
            e.printStackTrace();
        }
    }
}
