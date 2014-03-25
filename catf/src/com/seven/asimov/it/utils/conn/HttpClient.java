package com.seven.asimov.it.utils.conn;

import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.conn.TcpConnection.ConnType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * The <code>HttpClient</code> instance represents a HTTP client which sends and receives HTTP request/response.
 *
 * @author Daniel Xie (dxie@seven.com)
 */
public class HttpClient extends TcpClient {
    private static final String TAG = "HttpClient";
    static public String OC_HEADER_FIELD_CONN_ID = "X-OC-ConnId";
    static public String OC_HEADER_FIELD_REQ_ID = "X-OC-ReqId";

    // http requests
    protected ArrayList<HttpRequest> mRequests;

    protected int mReqId;
    protected int mExpectedReqId;
    protected boolean mIgnoreConnId;

    protected HttpResponse currentResp = null;

    /**
     * Create a HTTP client with persistent connection
     *
     * @param selector
     * @param version
     * @throws IOException
     */
    public HttpClient(ConnSelector selector) throws IOException {
        this(selector, false);
    }

    public HttpClient(ConnSelector selector, boolean ignoreConnId) throws IOException {
        super(selector);
        mRequests = new ArrayList<HttpRequest>();

        mIgnoreConnId = ignoreConnId;
    }

    public int getRequestId() {
        return mExpectedReqId;
    }

    public void sendRequest(HttpRequest request) throws IOException {
        sendRequest(request, false);
    }

    public void sendRequest(HttpRequest request, boolean noReqId) throws IOException {
        request.setSendTime(System.currentTimeMillis());

        if (!mIgnoreConnId) {
            // set connection id : X-OC-ConnId
            HttpHeaderField field = new HttpHeaderField(OC_HEADER_FIELD_CONN_ID, String.valueOf(getConnId()));
            request.addHeaderField(field);
        }

        mReqId++;
        if (!noReqId) {
            // set request id : X-OC-ReqId
            HttpHeaderField field = new HttpHeaderField(OC_HEADER_FIELD_REQ_ID, String.valueOf(mReqId));
            request.addHeaderField(field);
        }
        currentResp = null;
        // send it out
        sendData(ConnUtils.stringToByteBuffer(request.getFullRequest()));

        // add it into mRequests
        mRequests.add(request);
        if (mReqId == 1) {
            mExpectedReqId = 1;
        }
    }

    /**
     * Callback from test case's onDataReceived(channel, buffer)
     *
     * @param byteBuffer
     */

    public void onDataReceived(ByteBuffer buffer) {
        TcpConnection conn = super.getConnection();
        if (!hasRequestPending()) {
            conn.onHttpException(getConnId(), ConnType.CLIENT, new Exception("no more request"));
        } else {
            ByteBuffer data = conn.getReceiveBuffer();
            data.mark();
            data.put(buffer).reset();
            HttpResponse response = ConnUtils.parseHttpResponse(data);
            while (response != null) {
                HttpRequest request = this.mRequests.remove(0); // Get response, remove the request
                if (request != null) {
                    response.setDuration(System.currentTimeMillis() - request.getSendTime());
                } else {
                    ConnLogger.debug(TAG, "Can't find request for current response");
                }

                if (mIgnoreConnId) {
                    response = processResponse(conn, data, response);
                } else {
                    String connId = response.getHeaderField(OC_HEADER_FIELD_CONN_ID);
                    if (connId == null) {
                        conn.onHttpException(getConnId(), ConnType.CLIENT, new Exception("no conn id"));
                        break;
                    } else if (Integer.parseInt(connId) != getConnId()) {
                        conn.onHttpException(getConnId(), ConnType.CLIENT, new Exception("conn id is not correct"));
                        break;
                    } else {
                        response = processResponse(conn, data, response);
                    }
                }
            }
        }
    }

    protected HttpResponse processResponse(TcpConnection conn, ByteBuffer data, HttpResponse response) {
        mExpectedReqId++;
        conn.onResponseReceived(getConnId(), response);
        response = ConnUtils.parseHttpResponse(data);
        return response;
    }

    public boolean hasRequestPending() {
        return mExpectedReqId <= mReqId;
    }
}
