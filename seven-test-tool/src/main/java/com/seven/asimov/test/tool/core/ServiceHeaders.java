package com.seven.asimov.test.tool.core;

/**
 * The Enum ServiceHeaders.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public enum ServiceHeaders {

    /**
     * "X-OC-DispatchOnFirstByte".
     */
    DISPATCH_ON_FIRST_BYTE("X-OC-DispatchOnFirstByte"),
    /**
     * "X-OC-NewSocket".
     */
    NEW_SOCKET("X-OC-NewSocket"),
    /**
     * "X-OC-Reject-Client-Cert".
     */
    REJECT_CLIENT_CERT("X-OC-Reject-Client-Cert"),
    /**
     * "X-OC-Reject-Server-Cert".
     */
    REJECT_SERVER_CERT("X-OC-Reject-Server-Cert"),
    /**
     * "X-OC-Socket-NextTimeout".
     */
    SOCKET_TIMEOUT("X-OC-Socket-Timeout"),
    /**
     * "X-OC-Socket-NextTimeout".
     */
    SOCKET_NEXT_TIMEOUT("X-OC-Socket-NextTimeout"),
    /**
     * "X-OC-Delay-SocketWrite".
     */
    DELAY_SOCKET_WRITE("X-OC-Delay-SocketWrite");

    private String mValue;

    public void setValue(String value) {
        this.mValue = value;
    }

    public String getValue() {
        return mValue;
    }

    ServiceHeaders(String value) {
        this.mValue = value;
    }

    static ServiceHeaders fromStringIgnoreCase(String header) {
        for (ServiceHeaders sh : ServiceHeaders.values()) {
            if (sh.getValue().equalsIgnoreCase(header)) {
                return sh;
            }
        }
        return null;
    }
}
