package com.seven.asimov.it.utils.nanohttpd;

/**
 * Simple Http Server
 */
public class SimpleHttpServer extends NanoHTTPD {
    /**
     * <p>The default response message from the local server</p>
     */
    private String responseMessage = "Response from the local server\n";

    /**
     * <p>Constructs an HTTP server on given port.</p>
     * @param port     - the server port. Only root can open ports below 1024.
     *                 This is standard for Linux, not unique to Android.
     */
    public SimpleHttpServer(int port) {
        super(port);
    }

    /**
     * <p>Constructs an HTTP server on given hostname and port.</p>
     * @param hostname - the host name
     * @param port     - the server port. Only root can open ports below 1024.
     *                 This is standard for Linux, not unique to Android.
     */

    public SimpleHttpServer(String hostname, int port) {
        super(hostname, port);
    }

    /**
     * <p>Set the response message</p>
     * @param responseMessage The response message
     */
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String response = responseMessage;
        return new NanoHTTPD.Response(response);
    }
}
