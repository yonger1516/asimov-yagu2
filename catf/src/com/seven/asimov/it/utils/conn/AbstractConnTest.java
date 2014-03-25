package com.seven.asimov.it.utils.conn;

import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.conn.ConnTestResult.StateCode;
import com.seven.asimov.it.utils.conn.TcpConnection.ConnType;

import java.util.List;
import java.util.UUID;

public abstract class AbstractConnTest {

    private static final String TAG = AbstractConnTest.class.getSimpleName();

    public enum Status {
        RUNNING, SUCCEEDED, FAILED, INTERRUPTED, TIMEDOUT;
    }

    private ConnTestResult mResult = new ConnTestResult();
    private Object mStatusLock = new Object();
    private Status mStatus;
    private String mName;

    protected AbstractConnTest(String name) {
        mStatus = Status.RUNNING;
        mName = name;
    }

    public final void run(long timeout) {
        try {
            if (!init()) {
                ConnLogger.error(TAG, this.getName() + ": init fail");
                mResult.addRecord(ConnTestResult.CONN_ID_NEW, ConnType.CLIENT, StateCode.NONE);
                return;
            }

            long waittime = timeout;
            long startTime = System.currentTimeMillis();
            ConnLogger.debug(TAG, "Starting " + mName + ", timeout:" + timeout);

            while (mStatus == Status.RUNNING) {
                synchronized (mStatusLock) {
                    try {
                        mStatusLock.wait(waittime);
                        waittime = timeout - (System.currentTimeMillis() - startTime);
                        if (waittime <= 0) {
                            mStatus = Status.TIMEDOUT;
                            break;
                        }
                    } catch (InterruptedException e) {
                        mStatus = Status.INTERRUPTED;
                    }
                }
            }
        } finally {
            onDone(mStatus);
        }
    }

    public String getName() {
        return mName;
    }

    public String getResultDescription() {
        return "[" + getName() + "] Test status:" + mStatus + "\n" + mResult.toString();
    }

    public boolean isPassed() {
        return mStatus == Status.SUCCEEDED;
    }

    // -----------------------------------------------------------------------------------
    protected abstract boolean init();

    protected final void pass(int connId, ConnType type, StateCode code) {
        addResult(connId, type, code);
        pass();
    }

    protected void pass() {
        notify(Status.SUCCEEDED);
    }

    protected final void fail(int connId, ConnType type, StateCode code) {
        addResult(connId, type, code);
        fail();
    }

    protected final void fail(String reason) {
        mResult.setReason(reason);
        ConnLogger.error(TAG, getName() + "failed because:" + reason);
        fail();
    }

    protected void fail() {
        Thread.dumpStack();
        notify(Status.FAILED);
    }

    protected final void notify(Status status) {
        synchronized (mStatusLock) {
            mStatus = status;
            mStatusLock.notify();
        }
    }

    protected final Status getStatus() {
        return mStatus;
    }

    protected final ConnTestResult getResults() {
        return mResult;
    }

    protected final void addResult(int connId, ConnType type, StateCode code) {
        mResult.addRecord(connId, type, code);
    }

    protected void onDone(Status status) {
        ConnLogger.debug(TAG, mName + " stopped, status:" + status);
        mResult.addRecord(ConnTestResult.CONN_ID_NEW, ConnType.CLIENT, StateCode.DONE);
    }

    /**
     * Create new unique request url to avoid OC cache in the third request
     *
     * @return
     */
    protected static String getRequestPath() {
        UUID guid = UUID.randomUUID();
        String requestPath = guid.toString();
        return requestPath;
    }

    protected static HttpRequest buildSimpleRequest(String path, String host) {
        return buildSimpleRequest(path, host, true);
    }

    protected static HttpRequest buildSimpleRequest(String path, String host, boolean keepAlive) {
        HttpRequest.Builder builder = HttpRequest.Builder.create();
        builder.setMethod("GET");
        if (path == null) {
            builder.setUri("/" + getRequestPath());
        } else {
            builder.setUri(path);
        }

        if (host != null) {
            builder.addHeaderField("Host", host);
        }

        if (keepAlive) {
            builder.addHeaderField(TFConstantsIF.HEADER_CONNECTION, TFConstantsIF.HEADER_CONNECTION_KEEP_ALIVE);
        } else {
            builder.addHeaderField(TFConstantsIF.HEADER_CONNECTION, TFConstantsIF.HEADER_CONNECTION_CLOSE);
        }
        ConnLogger.debug(TAG, "Request=" + builder.getRequest());
        return builder.getRequest();
    }

    protected HttpResponse buildSimpleResponse(HttpRequest request) {
        return buildSimpleResponse(request, true);
    }

    protected HttpResponse buildSimpleResponse(HttpRequest request, boolean keepAlive) {
        return buildSimpleResponse(request, keepAlive, true);
    }

    protected HttpResponse buildSimpleResponse(HttpRequest request, boolean keepAlive, boolean addContentLength) {
        HttpResponse.Builder builder = HttpResponse.Builder.create();
        builder.setStatusLine("HTTP/1.1 200 OK");
        // copy headers from request except "Host"
        List<HttpHeaderField> headers = request.getHeaderFields();
        for (HttpHeaderField header : headers) {
            if (header.getName().equals("Host") || header.getName().equals(TFConstantsIF.HEADER_CONNECTION)
                    || header.getName().equals("Content-Length")) {
                continue;
            } else {
                builder.addHeaderField(header.getName(), header.getValue());
            }
        }

        if (keepAlive) {
            builder.addHeaderField(TFConstantsIF.HEADER_CONNECTION, TFConstantsIF.HEADER_CONNECTION_KEEP_ALIVE);
        } else {
            builder.addHeaderField(TFConstantsIF.HEADER_CONNECTION, TFConstantsIF.HEADER_CONNECTION_CLOSE);
        }

        if (addContentLength) {
            ConnLogger.debug(TAG, "Adding content length to the response");
            builder.addHeaderField(TFConstantsIF.HEADER_CONTENT_LENGTH, "4");
            builder.setBody("tere");
        }
        return builder.getResponse();
    }
}
