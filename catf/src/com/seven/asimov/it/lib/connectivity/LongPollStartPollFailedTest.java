package com.seven.asimov.it.lib.connectivity;

import android.content.Context;
import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.conn.ConnLogger;
import com.seven.asimov.it.utils.conn.ConnSelector;
import com.seven.asimov.it.utils.conn.ConnUtils;
import com.seven.asimov.it.utils.conn.HttpServer;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.UUID;

import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode;
import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.CONNECTED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.CLIENT;

/**
 * <p>
 * Pre-requisite: OC startups and works fine.
 * </p>
 * Steps:
 * <ul>
 * <li>1. App setup connection with OC, then App sends request to OC.
 * <li>2. App sends the same request every 30 seconds. 5 times totally.
 * <li>3. OC starts poll but failed because the Host cann't be accessed by Z7 server.
 * <li>4. Check connection in logs.
 * </ul>
 * Expected result:
 * <ul>
 * <li>1. OC should setup OC<>Host connection after request received.
 * <li>2. OC should forwards the first 3 requests to Host but not the 4th and 5th request.
 * <li>3. OC should keep App<>OC and OC<>Host connection when start poll failed.
 * <li>4. OC should serve the 4th and 5th request from cache.
 * <li>5. App should receive total 5 responses, all are same.
 * </ul>
 */
public class LongPollStartPollFailedTest extends SimpleTest {
    private static final String TAG = LongPollStartPollFailedTest.class.getSimpleName();
    private int mReqNoSentByClient, mReqNoReceivedByServer;
    private int mRespNoSentByServer, mRespNoReceivedByClient;
    private Context mContext;

    private static final String messageId = getMessageId();

    static public String getMessageId() {
        UUID guid = UUID.randomUUID();
        String requestPath = guid.toString();
        return requestPath;
    }

    public LongPollStartPollFailedTest(ConnSelector selector, HttpServer server, Context context) {
        super(TAG, selector, server);
        mReqNoSentByClient = 0;
        mReqNoReceivedByServer = 0;
        mRespNoSentByServer = 0;
        mRespNoReceivedByClient = 0;
        mContext = context;
    }

    @Override
    protected boolean init() {
        if (!super.init())
            return false;
        return true;
    }

    /**
     * Get a poll uri with idle time
     */
    private String getPollUri(int idleTime) {
        return "/v1/pushchannel/" + messageId + "&idle=" + idleTime;
    }

    @Override
    public void onConnected(SelectableChannel channel) {
        if (mClient.getChannel().equals(channel)) {
            ConnLogger.debug(TAG, getName() + ": client connected");
            this.addResult(mClient.getConnId(), CLIENT, CONNECTED);
            ConnUtils.block7TP();
            sendRequest();
        }
    }

    @Override
    public void onRequestReceived(int connId, HttpRequest request) {
        // check connId
        if (mServer.getConnection(connId) == null || mClient.getConnId() != connId) {
            fail("server received request on unexpected connId = " + connId);
            return;
        }

        mReqNoReceivedByServer++;

        // check request no
        switch (mReqNoSentByClient) {
            case 1:
            case 2:
                if (mReqNoSentByClient != mReqNoReceivedByServer) {
                    fail("server received unexpected request - reqNoSentByClient(" + mReqNoSentByClient
                            + ") != reqNoReceivedByServer(" + mReqNoReceivedByServer + ")");
                    return;
                }
                break;
            case 3:
                if (mReqNoReceivedByServer != 2) {
                    fail("server received unexpected response - reqNoReceivedByServer(" + mReqNoReceivedByServer + ") != 2");
                    return;
                }
                break;
            case 4:
                if (mReqNoReceivedByServer != 3) {
                    fail("server received unexpected response - reqNoReceivedByServer(" + mReqNoReceivedByServer + ") != 3");
                    return;
                }
                break;
            case 5:
                if (mReqNoReceivedByServer != 3) {
                    fail("server received unexpected response - reqNoReceivedByServer(" + mReqNoReceivedByServer + ") != 3");
                    return;
                }
                break;
            default:
                fail("client received unexpected response - reqNoReceivedByServer(" + mReqNoReceivedByServer);
                return;
        }

        // check request line
        String Uri = request.getUri();
        int index = Uri.indexOf("idle=");
        if (index == -1) {
            fail("server received unexptected request - no idle in uri");
            return;
        }

        // send response
        long idle = (Integer.parseInt(Uri.substring(index + 5)) - 5) * 1000;
        try {
            Thread.sleep(idle);
            HttpResponse response = buildSimpleResponse(request);
            response.setBody(messageId);
            response.addHeaderField(new HttpHeaderField("Content-Length", Integer.toString(messageId.length())));
            mServer.sendResponse(connId, response);
            mRespNoSentByServer++;
        } catch (InterruptedException e) {
            // ignore
        } catch (IOException e) {
            ConnLogger.error(TAG, getName() + ": Send data fail", e);
            fail(connId, ConnType.SERVER, StateCode.SEND_FAILED);
        }
    }

    @Override
    public void onResponseReceived(int connId, HttpResponse response) {
        // check connId
        if (mClient.getConnId() != connId) {
            fail("client received response with unexpected connId = " + connId);
            return;
        }

        mRespNoReceivedByClient++;

        // check response body
        if (!messageId.equals(response.getBody())) {
            fail("client received unexpected response - body is not same");
            return;
        }

        // check response no
        switch (mRespNoReceivedByClient) {
            case 1:
            case 2:
                if (mRespNoSentByServer != mRespNoReceivedByClient) {
                    fail("client received unexpected response - respNoSentByServer(" + mRespNoSentByServer
                            + ") != respNoReceivedByClient(" + mRespNoReceivedByClient + ")");
                    return;
                }
                break;
            case 3:
                if (mRespNoSentByServer != 2) {
                    fail("server received unexpected response - respNoSentByServer(" + mRespNoSentByServer + ") != 2");
                    return;
                }
                break;
            case 4:
                if (mRespNoSentByServer != 3) {
                    fail("server received unexpected response - respNoSentByServer(" + mRespNoSentByServer + ") != 3");
                    return;
                }
                break;
            case 5:
                if (mRespNoSentByServer != 3) {
                    fail("server received unexpected response - respNoSentByServer(" + mRespNoSentByServer + ") != 3");
                    return;
                }
                break;
            default:
                fail("client received unexpected response - respNoReceivedByClient(" + mRespNoReceivedByClient);
                return;
        }

        // send the next request
        if (mRespNoReceivedByClient < 5) {
            sendRequest();
        } else {
            ConnUtils.unblock7TP();
            pass();
        }
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        super.onRstReceived(channel);
        if (isClientReceivedRst) {
            this.fail("client received RST");
        }
    }

    @Override
    public void onHttpException(int connId, ConnType type, Exception e) {
        ConnLogger.error(TAG, getName() + ": Http exception:\r\n" + e);
        fail("Response is not excepted");
    }

    @Override
    public void onDone(Status status) {
        try {
            ConnLogger.debug(TAG, getName() + ": Close client");
            mClient.close();
        } catch (IOException e) {
            // ignore
        }
        ConnUtils.unblock7TP();

        if (status == Status.TIMEDOUT) {
            if (mReqNoReceivedByServer >= 4) {
                fail("server received request which should be served from cache");
            } else if (mRespNoReceivedByClient < 5) {
                fail("client didn't receive 5th response");
            }
        }
    }

    private void sendRequest() {
        try {
            Thread.sleep(10000);
            HttpRequest request = buildSimpleRequest(getPollUri(90));
            mClient.sendRequest(request, true);
            ConnLogger.debug(TAG, getName() + ": client sends request:\r\n" + request.getFullRequest());
            mReqNoSentByClient++;
        } catch (IOException e) {
            ConnLogger.error(TAG, getName() + ": Send requests failed", e);
            this.fail(mClient.getConnId(), ConnType.CLIENT, StateCode.SEND_FAILED);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}