package com.seven.asimov.it.lib.connectivity;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.conn.*;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static com.seven.asimov.it.utils.conn.ConnEvent.ActionType;
import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode;
import static com.seven.asimov.it.utils.conn.ConnTestResult.StateCode.SEND_FAILED;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnState;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType;
import static com.seven.asimov.it.utils.conn.TcpConnection.ConnType.CLIENT;

/**
 * Based class for multi-connections test
 */
public class MultiConnectionsTest extends AbstractConnTest implements IConnListener {
    private static final String TAG = MultiConnectionsTest.class.getSimpleName();
    private static final int TEST_CONN_COUNT = 10;
    private static final int SET_CONN_INTERVAL = 500;// ms

    protected ConnSelector mSelector;
    protected Hashtable<Integer, HttpClient> mClients;
    protected ConcurrentHashMap<SelectableChannel, HttpClient> mClientChannels;
    protected List<ConnectionState> mLeakedClientConns, mLeakedServerConns;

    private int mClientExpectedFinCount = TEST_CONN_COUNT;
    private int mClientExpectedRstCount = TEST_CONN_COUNT;
    private int mServerExpectedRstCount = TEST_CONN_COUNT;
    private int mExpectedResponseCount = TEST_CONN_COUNT;
    private int mClientExpectedRstFinCount = TEST_CONN_COUNT;

    private static int sOcUid;
    private static boolean sInitialized;

    protected HttpServer mServer;
    protected int mClientReceivedRstCount, mServerReceivedRstCount, mRecevicedResponseCount;
    protected int mClientConnectedCount, mServerAcceptedCount;
    protected int mClientReceivedFinCount, mServerReceivedFinCount;

    protected SocketsMonitor mSocketsMonitor;
    private static Context context;

    public MultiConnectionsTest(ConnSelector selector, HttpServer server) {
        this(TAG, selector, server);
    }

    public MultiConnectionsTest(String name, ConnSelector selector, HttpServer server) {
        super(name);

        mSelector = selector;
        mServer = server;

        mClientChannels = new ConcurrentHashMap<SelectableChannel, HttpClient>();
        mClients = new Hashtable<Integer, HttpClient>();

        mLeakedClientConns = new ArrayList<ConnectionState>();
        mLeakedServerConns = new ArrayList<ConnectionState>();
    }

    public static void init(Context context) {
        if (!sInitialized) {
            sInitialized = true;
            MultiConnectionsTest.context = context;
            final PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            boolean found = false;
            for (ApplicationInfo packageInfo : packages) {
                if (packageInfo.packageName.equals(TFConstantsIF.OC_PACKAGE_NAME)) {
                    sOcUid = packageInfo.uid;
                    found = true;
                    break;
                }
            }

            if (found) {
                ConnLogger.info(TAG, "Successfully obtained OC client package UID: " + sOcUid);
            } else {
                ConnLogger.warn(TAG, "Failed to obtain OC client package UID");
            }
        }
    }

    protected HttpRequest buildSimpleRequest() {
        if (mServer == null) {
            ConnLogger.warn(TAG, "Test server is not set");
            return null;
        }

        return buildSimpleRequest(null, mServer.getCurrentAddress().getAddress().getHostAddress() + ":"
                + mServer.getCurrentAddress().getPort());
    }

    protected HttpClient getClient(int connId) {
        return mClients.get(connId);
    }

    protected HttpClient getClient(SelectableChannel channel) {
        return mClientChannels.get(channel);
    }

    protected void removeClient(HttpClient client) {
        mClientChannels.remove(client.getChannel());
        mClients.remove(client.getConnId());
    }

    protected Collection<HttpClient> getAllClients() {
        return mClients.values();
    }

    protected final boolean setupConnections(int connCount) {
        ConnLogger.debug(TAG, getName() + ": setupConnections:" + connCount);

        for (int i = 0; i < connCount; i++) {
            HttpClient client = setupConnection(false);
            if (client == null) {
                ConnLogger.error(TAG, this.getName() + ": setup connection[index=" + i + "] fail, break");
                return false;
            }

            sleepForOC();
        }

        return true;
    }

    protected void sleepForOC() {
        try {
            Thread.sleep(SET_CONN_INTERVAL); // sleep a while to let OC handle the request
        } catch (InterruptedException e) {
            //ignored
        }
    }

    protected void clientSendFin(int connId) {
        HttpClient client = mClients.get(connId);
        clientSendFin(client);
    }

    protected void clientSendFin(HttpClient client) {
        if (client != null) {
            try {
                ConnLogger.debug(TAG, appendLogPrefix(client.getConnId(), "client sends fin"));
                client.sendFin();
            } catch (IOException e) {
                ConnLogger.error(TAG, appendLogPrefix(client.getConnId(), "Send Fin fail"), e);
                this.fail(client.getConnId(), CLIENT, SEND_FAILED);
            }
        } else {
            ConnLogger.error(TAG, "Client is null, can't send Fin");
        }
    }

    protected void clientSendRst(int connId) {
        HttpClient client = mClients.get(connId);
        clientSendRst(client);
    }

    protected void clientSendRst(HttpClient client) {
        if (client != null) {
            try {
                ConnLogger.debug(TAG, appendLogPrefix(client.getConnId(), "client sends Rst"));
                client.sendRst();
            } catch (IOException e) {
                ConnLogger.error(TAG, appendLogPrefix(client.getConnId(), "Send Rst fail"), e);
                this.fail(client.getConnId(), CLIENT, SEND_FAILED);
            }
        } else {
            ConnLogger.error(TAG, "Client is null, can't send Rst");
        }
    }

    protected boolean onStart() {
        return setupConnections(TEST_CONN_COUNT);
    }

    protected HttpClient setupConnection(boolean ignoreConnId) {
        return setupConnection(ignoreConnId, TFConstantsIF.OC_HTTP_PROXY_ADDRESS, TFConstantsIF.OC_HTTP_PROXY_PORT);
    }

    protected HttpClient setupConnection(boolean ignoreConnId, String host, int port) {
        if (this.getStatus() != Status.RUNNING) {
            ConnLogger.error(TAG, this.getName() + ": is not running, break the muti-conn testing");
            return null;
        }

        HttpClient client;
        try {
            client = new HttpClient(mSelector, ignoreConnId);
        } catch (IOException e1) {
            ConnLogger.error(TAG, "Failed to create HttpClient", e1);

            this.fail(ConnTestResult.CONN_ID_NEW, CLIENT, StateCode.CONNECT_FAILED);
            return null;
        }

        client.setListener(this);
        mClientChannels.put(client.getChannel(), client);
        mClients.put(client.getConnId(), client);

        try {
            this.addResult(client.getConnId(), CLIENT, StateCode.CONNECTING);

            client.connect(host, port);
        } catch (IOException e) {
            ConnLogger.error(
                    TAG,
                    appendLogPrefix(client.getConnId(), "failed to connect tcp server on port:"
                            + TFConstantsIF.OC_HTTP_PROXY_PORT), e);
            this.fail(client.getConnId(), ConnType.CLIENT, StateCode.CONNECT_FAILED);
            return null;
        }

        return client;
    }

    protected String appendLogPrefix(int connId, String message) {
        return getName() + ":: Conn #" + connId + ": " + message;
    }

    public String getConnsDescripton() {
        StringBuilder description = new StringBuilder("[");
        description.append(getName()).append("] Test status: ").append(getStatus());

        if (this.getResults().getReason() != null) {
            description.append("\n Reason: ");
            description.append(this.getResults().getReason());
        }

        description.append("\n Conns: \n");
        description.append(getOpenedConns());

        return description.toString();
    }

    protected String getOpenedConns() {
        StringBuilder description = new StringBuilder();
        for (ConnectionState conn : mLeakedClientConns) {
            description.append("       Client connection[").append("id=").append(conn.conn.getConnId())
                    .append("] is not closed, state:").append(conn.state).append("\n");
        }

        for (ConnectionState conn : mLeakedServerConns) {
            description.append("       Server connection[").append("id=").append(conn.conn.getConnId())
                    .append("] is not closed, state:").append(conn.state).append("\n");
        }

        description.append(" Leaked client conns:").append(mLeakedClientConns.size()).append(", leaked server conns:")
                .append(mLeakedServerConns.size()).append(", mRecevicedResponseCount:").append(mRecevicedResponseCount)
                .append(", mClientReceivedRstCount:").append(mClientReceivedRstCount)
                .append(", mServerReceivedRstCount:").append(mServerReceivedRstCount).append("\n");

        return description.toString();
    }

    protected final int getClientExpectedFinCount() {
        return mClientExpectedFinCount;
    }

    protected final void setClientExpectedFinCount(int expectedFinCount) {
        mClientExpectedFinCount = expectedFinCount;
    }

    protected final int getClientExpectedRstCount() {
        return mClientExpectedRstCount;
    }

    protected final void setClientExpectedRstCount(int expectedRstCount) {
        mClientExpectedRstCount = expectedRstCount;
    }

    protected int getServerExpectedRstCount() {
        return mServerExpectedRstCount;
    }

    protected final void setServerExpectedRstCount(int expectedRstCount) {
        mServerExpectedRstCount = expectedRstCount;
    }

    protected final int getExpectedResponseCount() {
        return mExpectedResponseCount;
    }

    protected final void setExpectedResponseCount(int expectedResponseCount) {
        mExpectedResponseCount = expectedResponseCount;
    }

    public boolean verifyClientExpectedRst(int clientReceivedRstCount) {
        return getClientExpectedRstCount() < 0 || clientReceivedRstCount == getClientExpectedRstCount();
    }

    // --------------------------------------------------------------------------------------

    @Override
    protected boolean init() {
        int hostPort = 80;
        if (mServer != null) {
            mServer.setListener(this);
            hostPort = mServer.getCurrentAddress().getPort();
        }

        mSocketsMonitor = new SocketsMonitor(hostPort, sOcUid);
        return onStart();
    }

    @Override
    protected void onDone(Status status) {
        ConnLogger.debug(TAG, this.getName() + ": stopped, status:" + status);
        List<SysSocketDescriptor> leakedSockets = mSocketsMonitor.getCurrentNewSockets();
        ConnLogger.debug(TAG,
                this.getName() + ": leakedSockets\n" + mSocketsMonitor.getSocketsDescription(leakedSockets));

        closeLeakClientConn();
        closeLeakServerConn();

        if (status == Status.TIMEDOUT) {

            if (!mLeakedClientConns.isEmpty()) {
                this.fail("Client sockets leak, conns:" + mLeakedClientConns.size());
            } else if (!mLeakedServerConns.isEmpty()) {
                this.fail("Server sockets leak, conns:" + mLeakedClientConns.size());
            } else if (!leakedSockets.isEmpty()) {
                this.fail("Sockets leak");
            } else if (!verifyClientExpectedRst(mClientReceivedRstCount)) {
                this.fail("Client received [" + mClientReceivedRstCount + "] rst, expected ["
                        + getClientExpectedRstCount() + "]");
            } else if (getServerExpectedRstCount() > 0 && mServerReceivedRstCount != getServerExpectedRstCount()) {
                this.fail("Server received [" + mServerReceivedRstCount + "] rst, expected ["
                        + getServerExpectedRstCount() + "]");
            } else if (getExpectedResponseCount() > 0 && mRecevicedResponseCount != getExpectedResponseCount()) {
                this.fail("Client received [" + mRecevicedResponseCount + "] reponses, expected ["
                        + getExpectedResponseCount() + "]");
            } else {
                this.pass(ConnTestResult.CONN_ID_NEW, CLIENT, StateCode.DONE);
            }
        }

        ConnLogger.debug(TAG, this.getName() + ": " + mSocketsMonitor.getCurrentNewSocketsDescription());

    }

    private void closeLeakClientConn() {
        for (HttpClient client : mClientChannels.values()) {
            if (client.getState() != ConnState.CLOSED) {
                ConnectionState connSate = new ConnectionState();
                connSate.conn = client.getConnection();
                connSate.state = client.getState();

                mLeakedClientConns.add(connSate);

                try {
                    client.close();
                    sleepForOC();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    // TODO: this is duplicated to closeLeakClientConn. Need to remove it.
    private void closeLeakServerConn() {
        if (mServer == null) {
            return;
        }

        for (Entry<SocketChannel, TcpConnection> connEntry : mServer.getServerConnection().getAllConnections()
                .entrySet()) {
            if (connEntry.getValue().getState() != ConnState.CLOSED) {
                ConnectionState connSate = new ConnectionState();
                connSate.conn = connEntry.getValue();
                connSate.state = connEntry.getValue().getState();

                mLeakedServerConns.add(connSate);

                try {
                    mServer.close(connEntry.getKey());
                    sleepForOC();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ========================================================================================
    @Override
    public void onAccepted(ServerSocketChannel serverChannel, SocketChannel newChannel) {
        mServerAcceptedCount++;
        // ConnLogger.debug(TAG, getName() + "  onAccepted, mServerAcceptedCount:"+ mServerAcceptedCount);
    }

    @Override
    public void onConnected(SelectableChannel channel) {
        HttpClient client = mClientChannels.get(channel);
        if (client == null) {
            ConnLogger.debug(TAG, getName() + ": Get unknown channel");
            return;
        }
        mClientConnectedCount++;

        // SocketChannel sChannel = (SocketChannel) channel;
        // ConnLogger.debug(TAG, getName() + ": client connected, local port:" + sChannel.socket().getLocalPort()
        // +", port:" + sChannel.socket().getPort()
        // +", mClientConnectedCount:" + mClientConnectedCount);
        try {
            this.addResult(client.getConnId(), CLIENT, StateCode.CONNECTED);
            HttpRequest simpleRequest = buildSimpleRequest();
            client.sendRequest(simpleRequest);

            // ConnLogger.debug(TAG, getName() + ": client sends request:\r\n" + simpleRequest.getFullRequest());
        } catch (IOException e) {
            ConnLogger.error(TAG, getName() + ": Send data fail", e);
            this.fail(client.getConnId(), CLIENT, StateCode.SEND_FAILED);
        }
    }

    @Override
    public void onRequestReceived(int connId, HttpRequest request) {
        if (mServer == null) {
            ConnLogger.error(TAG, "Test server is not set");
            return;
        }

        try {
            HttpResponse response = buildSimpleResponse(request);
            mServer.sendResponse(connId, response);
        } catch (IOException e) {
            ConnLogger.error(TAG, appendLogPrefix(connId, "Send data fail"), e);
            this.fail(connId, ConnType.SERVER, StateCode.SEND_FAILED);
        }
    }

    @Override
    public void onResponseReceived(int connId, HttpResponse response) {
        // ConnLogger.debug(TAG, appendLogPrefix(connId, "onResponseReceived"));
        mRecevicedResponseCount++;

        clientSendFin(connId);
    }

    @Override
    public void onFinReceived(SelectableChannel channel) {
        TcpClient client = mClientChannels.get(channel);
        if (client != null) {
            // ConnLogger.debug(TAG, appendLogPrefix(client.getConnId(), "client received fin"));
            mClientReceivedFinCount++;
            this.addResult(client.getConnId(), ConnType.CLIENT, StateCode.FIN_RECEIVED);
            return;
        }

        TcpConnection conn = getServerSideConn(channel);
        if (conn != null) {
            // ConnLogger.debug(TAG, appendLogPrefix(conn.getConnId(), "server received fin"));
            mServerReceivedFinCount++;

            this.addResult(conn.getConnId(), ConnType.SERVER, StateCode.FIN_RECEIVED);

            try {
                // ConnLogger.debug(TAG, appendLogPrefix(conn.getConnId(), "server sends fin"));
                mServer.sendFin(conn.getConnId());
            } catch (IOException e) {
                ConnLogger.error(TAG, getName() + ": Send FIN fail", e);
                this.fail(conn.getConnId(), ConnType.SERVER, StateCode.SEND_FAILED);
            }
        }
    }

    @Override
    public void onFinSent(SelectableChannel channel) {
        addResult(channel, StateCode.FIN_SENT);
    }

    @Override
    public void onRstSent(SelectableChannel channel) {
        addResult(channel, StateCode.RST_SENT);
    }

    // ============================================
    private TcpConnection getServerSideConn(SelectableChannel channel) {
        if (mServer == null) {
            return null;
        }

        return mServer.getConnection(channel);
    }

    @Override
    public void onRstReceived(SelectableChannel channel) {
        TcpClient client = mClientChannels.get(channel);
        if (client != null) {
            int connId = client.getConnId();
            this.addResult(connId, ConnType.CLIENT, StateCode.RST_RECEIVED);
            // ConnLogger.debug(TAG, getName() + ": Client onRstReceived, connId:" + connId);

            mClientChannels.remove(channel);
            mClientReceivedRstCount++;

            try {
                client.close();
                sleepForOC();
            } catch (IOException e) {
                ConnLogger.debug(TAG, getName() + ": Close client connection excepton", e);
            }

            return;
        }

        TcpConnection conn = getServerSideConn(channel);
        if (conn != null) {
            this.addResult(conn.getConnId(), ConnType.SERVER, StateCode.RST_RECEIVED);
            // ConnLogger.debug(TAG, getName() + ": Server onRstReceived, connId:" + conn.getConnId());

            mServer.getServerConnection().removeConnection(channel);
            mServerReceivedRstCount++;
            try {
                mServer.close(conn.getConnId());
            } catch (IOException e) {
                ConnLogger.debug(TAG, getName() + ": Close server connection excepton", e);
            }
        }

        if (verifyClientExpectedRst(mClientReceivedRstCount)
                && (getServerExpectedRstCount() < 0 || mServerReceivedRstCount == getServerExpectedRstCount())
                && (getExpectedResponseCount() < 0 || mRecevicedResponseCount == getExpectedResponseCount())) {
            this.pass();
        }
    }

    protected void addResult(SelectableChannel channel, StateCode resultCode) {
        ConnType connType = ConnType.CLIENT;
        int connId = ConnTestResult.CONN_ID_NEW;

        TcpClient client = mClientChannels.get(channel);
        if (client != null) {
            connId = client.getConnId();
        } else {
            TcpConnection conn = getServerSideConn(channel);
            if (conn != null) {
                connType = ConnType.SERVER;
                connId = conn.getConnId();
            }
        }

        this.addResult(connId, connType, resultCode);
    }

    // ---------------------------------------------------------------------------------------------
    @Override
    public void onDataReceived(SelectableChannel channel, ByteBuffer byteBuffer) {
        HttpClient client = mClientChannels.get(channel);
        if (client != null) {
            // ConnLogger.debug(TAG, getName() + ": client received response:\r\n" +
            // ConnUtils.ByteBufferToString(byteBuffer));
            this.addResult(client.getConnId(), ConnType.CLIENT, StateCode.DATA_RECEIVED);

            client.onDataReceived(byteBuffer);

            return;
        }

        TcpConnection conn = getServerSideConn(channel);
        if (conn != null) {
            // ConnLogger.debug(TAG, getName() + ": server received request:\r\n" +
            // ConnUtils.ByteBufferToString(byteBuffer));
            this.addResult(conn.getConnId(), ConnType.SERVER, StateCode.DATA_RECEIVED);

            mServer.onDataReceived(channel, byteBuffer);
        }
    }

    @Override
    public void onSocketException(SelectableChannel channel, IOException e, ActionType type) {
        //if ( e != null && e.getMessage() != null && e.getMessage().contains(ConnUtils.SOCKET_EXCEPTION_RST_MESSAGE)) {
        if (e != null && e.getClass() == SocketException.class) {
            // RST received
            onRstReceived(channel);
        } else {
            ConnLogger.debug(TAG, getName() + " SocketException[" + e.getMessage() + "] occurs, type:" + type, e);
            this.fail();
        }
    }

    @Override
    public void onWritable(SelectableChannel channel) {
        // ConnLogger.debug(TAG, getName() + "  onWritable, channel:"+ channel);
    }

    @Override
    public void onReadable(SelectableChannel channel) {
        // ConnLogger.debug(TAG, getName() + "  onReadable, channel:"+ channel);
    }

    @Override
    public void onDataSent(SelectableChannel channel) {
        // ConnLogger.debug(TAG, getName() + "  data sent, channel:"+ channel);
    }

    @Override
    public void onHttpException(int connId, ConnType type, Exception e) {
        ConnLogger.debug(TAG, getName() + " onHttpException, connId:" + connId, e);
    }

    public int getClientExpectedRstFinCount() {
        return mClientExpectedRstFinCount;
    }

    public void setClientExpectedRstFinCount(int mClientExpectedRstFinCount) {
        this.mClientExpectedRstFinCount = mClientExpectedRstFinCount;
    }

    protected class ConnectionState {
        public ConnectionState() {
        }

        public TcpConnection conn;
        public ConnState state;

        // NEW, BOUND, LISTENING, CONNECTING, CONNECTED, SHUTDOWN, CLOSE_WAIT, CLOSED;
        public String toString() {
            return "ConnState = " + state.toString() + " ";
        }
    }

    protected final void pass() {
        notify(Status.SUCCEEDED);
    }

    protected final void fail() {
        Thread.dumpStack();
        notify(Status.FAILED);
    }
}
