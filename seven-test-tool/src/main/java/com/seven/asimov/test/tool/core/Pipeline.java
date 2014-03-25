package com.seven.asimov.test.tool.core;

import android.content.Context;
import com.seven.asimov.test.tool.constants.Constants;
import com.seven.asimov.test.tool.core.testjobs.TestJob;
import com.seven.asimov.test.tool.core.testjobs.TestJobEvent;
import com.seven.asimov.test.tool.core.testjobs.TestJobEventHandler;
import com.seven.asimov.test.tool.utils.Util;
import com.seven.asimov.test.tool.utils.Z7HttpUtil;
import com.seven.asimov.test.tool.utils.Z7HttpsUtil;
import com.seven.asimov.test.tool.utils.streaming.ChunkedInputStream;
import com.seven.asimov.test.tool.utils.streaming.MyInputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.net.*;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.security.cert.Certificate;
import java.util.Arrays;

/**
 * Pipeline Thread.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class Pipeline extends Thread {

    public static final String MESSAGE = "pipelineMessage";

    private static final Logger LOG = LoggerFactory.getLogger(Pipeline.class.getSimpleName());

    private TestJobEventHandler mEventHandler;
    private ResponseHandler mResponseHandler;

    private SocketChannel mSocketChannel;
    private Socket mSocket;

    private URI mUri;

    public String getHostAndPort() {
        if (mUri != null) {
            return mUri.getHost() + ":" + mUri.getPort();
        }
        return null;
    }

    private final int mConnection;

    private boolean mShutDown;

    public void setShutDown(boolean shutDown) {
        this.mShutDown = shutDown;
    }

    public boolean isShutDown() {
        return mShutDown;
    }

    private String mError;
    private Exception mException;
    private String mConnectionStatus;

    private Object mMainLock = new Object();

    public Object getMainLock() {
        return mMainLock;
    }

    public Pipeline(Context context, int connection) {
        this.mEventHandler = new TestJobEventHandler(context, connection);
        this.mConnection = connection;
    }

    public TestJobEventHandler getTestJobQueue() {
        return mEventHandler;
    }

    public int getConnection() {
        return mConnection;
    }

    public Socket getSocket() {
        return mSocket;
    }

    public void run() {

        Request request = null;
        int counter = 1;

        try {

            // LOG.debug(Util.getLogPrefix(this.getId(), mConnection) + "Pipeline " + this.getName() + " started...");

            mEventHandler.handleEvent(this.getId(), mConnectionStatus, mError, TestJobEvent.PIPELINE_STARTED);

            while (true) {

                request = null;
                LOG.debug(Util.getLogPrefix(this.getId(), mConnection) + "Queue size: " + mEventHandler.size());

                TestJob newTestJob;

                synchronized (mMainLock) {

                    // Find fresh TestJobs in queue
                    newTestJob = mEventHandler.getFreshTestJob();

                    if (newTestJob != null) {

                        // LOG.debug(Util.getLogPrefix(this.getId(), mConnection, counter) + "New request received!");

                        mEventHandler
                                .handleEvent(this.getId(), mConnectionStatus, mError, TestJobEvent.REQUEST_STARTED);
                        request = newTestJob.getRequest();
                        mUri = request.getUri();
                        request.setTestJobType(newTestJob.getType());
                        request.setCounter(counter);
                        counter++;

                    } else {

                        LOG.debug(Util.getLogPrefix(this.getId(), mConnection) + "Waiting new request...");
                        mMainLock.wait();

                        if (mShutDown) {
                            break;
                        }

                        continue;

                    }
                }

                // Handle request
                if (mSocketChannel == null) {
                    mSocketChannel = SocketChannel.open();

                    LOG.debug(Util.getLogPrefix(this.getId(), mConnection, request.getCounter()) + "Connecting to {"
                            + request.getUri().getAuthority() + "}...");

                    InetSocketAddress destination;

                    // Check if we need to use proxy
                    if (StringUtils.isEmpty(request.getProxy())) {
                        destination = new InetSocketAddress(request.getUri().getHost(), request.getUri().getPort());
                    } else {
                        // Use proxy
                        destination = new InetSocketAddress(request.getProxy(),
                                ((request.getProxyPort() != null) ? request.getProxyPort() : Constants.PORT_HTTP));
                    }

                    LOG.debug(Util.getLogPrefix(this.getId(), mConnection, request.getCounter())
                            + "Host is resolved: " + destination);
                    if (request.getUri().getPort() != Constants.PORT_HTTPS) {
                        mSocketChannel.connect(destination);
                        mSocket = mSocketChannel.socket();
                    } else {
                        LOG.debug(Util.getLogPrefix(this.getId(), mConnection, request.getCounter())
                                + "Creating TLS engine...");

                        EasyTrustManager easyTrustManager = new EasyTrustManager(request);

                        String[] sslProtocols = HttpsOptions.getProtocolsAsArray();
                        String[] sslCiphers = HttpsOptions.getCiphersAsArray(sslProtocols);

                        LOG.debug("Enabled protocols: %s", Arrays.toString(sslProtocols));
                        LOG.debug("Enabled ciphers: %s", Arrays.toString(sslCiphers));

                        SSLContext sslCtx = SSLContext.getInstance(sslProtocols[0]);
                        sslCtx.init(null, new TrustManager[]{easyTrustManager}, null);
                        SSLSocketFactory ssf = sslCtx.getSocketFactory();
                        InetAddress addr = destination.getAddress();
                        mSocket = ssf.createSocket(addr, Constants.PORT_HTTPS);
                        SSLSocket sslSocket = (SSLSocket) mSocket;
                        sslSocket.setEnabledCipherSuites(sslCiphers);
                        sslSocket.setEnabledProtocols(sslProtocols);
                        sslSocket.startHandshake();
                        String proto = sslSocket.getSession().getProtocol();
                        String cipher = sslSocket.getSession().getCipherSuite();
                        Certificate[] certs = sslSocket.getSession().getPeerCertificates();
                        LOG.debug("SSL handshake completed: protocol=%s, cipher=%s, server certificate:", proto, cipher);
                        Z7HttpsUtil.logCertificates(certs);
                    }
                }

                LOG.debug(Util.getLogPrefix(this.getId(), mConnection, request.getCounter())
                        + "Setting socket read timeout: " + request.getSocketReadTimeout() + " mSec");
                mSocket.setSoTimeout(request.getSocketReadTimeout());

                // Start reading socket
                if (mResponseHandler == null) {
                    mResponseHandler = new ResponseHandler();
                    mResponseHandler.start();
                }

                // Write to socket
                byte[] requestArray = request.getRequestString().getBytes("UTF-8");
                LOG.debug(Util.getLogPrefix(this.getId(), mConnection, request.getCounter()) + "Sending request...");

                mEventHandler.handleEvent(this.getId(), mConnectionStatus, mError, TestJobEvent.REQUEST_HEADER);

                if (mShutDown) {
                    break;
                }

                if (request.getDelaySocketWrite() != null) {
                    LOG.debug(Util.getLogPrefix(this.getId(), mConnection, request.getCounter()) + "Delay: "
                            + request.getDelaySocketWrite() + " mSec...");
                    sleep(request.getDelaySocketWrite());
                }

                mSocket.getOutputStream().write(requestArray, 0, requestArray.length);
                mSocket.getOutputStream().flush();

                mEventHandler.handleEvent(this.getId(), mConnectionStatus, mError, TestJobEvent.REQUEST_COMPLETED);

                if (mShutDown) {
                    break;
                }
            }
        } catch (UnknownHostException e) {
            mError = "UnknownHostException: " + e.getMessage();
            mException = e;
        } catch (UnresolvedAddressException e) {
            mError = "UnresolvedAddressException: " + request.getUri().getHost();
            mException = e;
        } catch (NullPointerException e) {
            mError = "NullPointerException";
            mException = e;
        } catch (IllegalArgumentException e) {
            mError = "IllegalArgumentException: " + e.getMessage();
            mException = e;
        } catch (SocketTimeoutException e) {
            mError = "SocketTimeoutException: " + e.getMessage();
        } catch (SocketException e) {
            mError = "SocketException: " + e.getMessage();
            mException = e;
        } catch (SSLHandshakeException e) {
            mError = e.getMessage();
        } catch (Exception e) {
            mError = e.getMessage();
            mException = e;
        } finally {
            if (mError != null) {
                if (mException != null) {
                    LOG.error(Util.getLogPrefix(this.getId(), mConnection) + mError, mException);
                    mException = null;
                } else {
                    LOG.warn(Util.getLogPrefix(this.getId(), mConnection) + mError);
                }
            }
            mEventHandler.handleEvent(this.getId(), mConnectionStatus, mError, TestJobEvent.PIPELINE_COMPLETED);
            LOG.debug(Util.getLogPrefix(this.getId(), mConnection) + "Shutting down Pipeline: " + this.getName());
            this.interrupt();
        }
    }

    private static final Logger LOG2 = LoggerFactory.getLogger(ResponseHandler.class.getSimpleName());

    /**
     * ResponseHandler handler.
     *
     * @author Maksim Selivanov (mselivanov@seven.com)
     */
    public class ResponseHandler extends Thread {

        public ResponseHandler() {
        }

        private Response mResponse;
        private MyInputStream mIStream;

        private ByteArrayOutputStream mRawArray = new ByteArrayOutputStream();

        public static final int BODY_SIZE_LIMIT = 100240;
        private final int mDefaultInputBufferSize = 8192;
        private int mInputBufferSize = mDefaultInputBufferSize;

        public void run() {

            LOG2.debug(Util.getLogPrefix(this.getId(), mConnection) + "ResponseHandler " + this.getName()
                    + " started...");
            try {

                mIStream = new MyInputStream(mSocket.getInputStream());

                LOG2.debug(Util.getLogPrefix(this.getId(), mConnection) + "Reading socket...");

                while (true) {

                    byte[] buffer = new byte[mInputBufferSize];
                    int bytesRead;

                    try {

                        bytesRead = mIStream.read(buffer, 0, buffer.length);

                        if (mResponse != null) {
                            if (mResponse.isChunked() && bytesRead == Constants.EOF) {
                                if (((ChunkedInputStream) mIStream).isComplete()) {
                                    mEventHandler.handleEvent(this.getId(), mConnectionStatus, mError,
                                            TestJobEvent.RESPONSE_BODY_COMPLETED);
                                    bytesRead = Constants.CHUNKED_EOF;
                                }
                            }
                        }

                        switch (bytesRead) {
                            default:

                                if (bytesRead == Constants.CHUNKED_EOF) {
                                    bytesRead = 0;
                                }
                                // Try to accelerate read
                                if (bytesRead == mInputBufferSize) {
                                    mInputBufferSize = mInputBufferSize * 2;
                                } else {
                                    mInputBufferSize = mDefaultInputBufferSize;
                                }

                                if (mEventHandler.getFirstResponseTestJob().getState().getValue() < TestJobEvent.RESPONSE_STARTED
                                        .getValue()) {
                                    mResponse = new Response(mEventHandler.getFirstResponseTestJob().getRequest());
                                    mEventHandler.handleEvent(this.getId(), mConnectionStatus, mError,
                                            TestJobEvent.RESPONSE_STARTED);
                                }

                                if (!mResponse.isChunked()) {
                                    mRawArray.write(buffer, 0, bytesRead);
                                } else {
                                    mRawArray.write(((ChunkedInputStream) mIStream).getByteArray().toByteArray());
                                    ((ChunkedInputStream) mIStream).getByteArray().reset();
                                }

                                if (mEventHandler.getFirstResponseTestJob().getState().getValue() < TestJobEvent.RESPONSE_HEADER_STARTED
                                        .getValue()) {
                                    mEventHandler.handleEvent(this.getId(), mConnectionStatus, mError,
                                            TestJobEvent.RESPONSE_HEADER_STARTED);
                                }

                                if (mIStream.getRealBytesRead() != 0) {
                                    LOG2.debug(Util.getLogPrefix(this.getId(), mConnection, mResponse.getRequest()
                                            .getCounter())
                                            + "Reading response...Bytes read: " + bytesRead);
                                }

                                if (mEventHandler.getFirstResponseTestJob().getState().getValue() < TestJobEvent.RESPONSE_HEADER_COMPLETED
                                        .getValue()) {

                                    Integer headersLength = Z7HttpUtil.findHeadersLength(mRawArray);

                                    if (headersLength != null) {
                                        mResponse.getRawArray().write(mRawArray.toByteArray(), 0, headersLength);

                                        mResponse.setBytesReceived(mResponse.getBytesReceived() + headersLength);

                                        mResponse.parseHeaders();

                                        mEventHandler.getFirstResponseTestJob().setResponse(mResponse);

                                        mEventHandler.handleEvent(this.getId(), mConnectionStatus, mError,
                                                TestJobEvent.RESPONSE_HEADER_COMPLETED);

                                    } else {
                                        // Continue to read headers
                                        continue;
                                    }
                                }

                                if (mEventHandler.getFirstResponseTestJob().getState().getValue() < TestJobEvent.RESPONSE_BODY_STARTED
                                        .getValue()) {

                                    // Check if request has a body
                                    if ((mResponse.getContentLength() != 0) || mResponse.isChunked()) {
                                        mEventHandler.handleEvent(this.getId(), mConnectionStatus, mError,
                                                TestJobEvent.RESPONSE_BODY_STARTED);

                                        // write body to buffer
                                        mIStream.setBufferedInputStream(mRawArray.toByteArray(), mResponse.getRawArray()
                                                .size(), mRawArray.size() - mResponse.getRawArray().size());
                                        mRawArray.reset();

                                        if (mResponse.isChunked()) {
                                            mIStream = new ChunkedInputStream(mIStream);
                                        }

                                        continue;
                                    } else {
                                        // Check if there are multiple responses in rawArray
                                        // If response is not chunked and does not have Content-Length, don't write to
                                        // buffer
                                        if (mRawArray.size() > mResponse.getHeadersLength()
                                                && (mResponse.isChunked() || (mResponse.getContentLength() != 0))) {
                                            mIStream.setBufferedInputStream(mRawArray.toByteArray(),
                                                    mResponse.getHeadersLength(),
                                                    mRawArray.size() - mResponse.getHeadersLength());
                                        }
                                        mEventHandler.handleEvent(this.getId(), mConnectionStatus, mError,
                                                TestJobEvent.RESPONSE_BODY_COMPLETED);
                                    }
                                }

                                if (mEventHandler.getFirstResponseTestJob().getState().getValue() < TestJobEvent.RESPONSE_BODY_COMPLETED
                                        .getValue()) {

                                    if (!mResponse.isChunked()) {
                                        if (mResponse.getContentLength() < bytesRead) {
                                            mResponse.getContentArray().write(buffer, 0, mResponse.getContentLength());
                                            mResponse.setBytesReceived(mResponse.getBytesReceived()
                                                    + mResponse.getContentLength());
                                            // write extra to buffer
                                            mIStream.setBufferedInputStream(buffer, mResponse.getContentLength(), bytesRead
                                                    - mResponse.getContentLength());
                                        } else {
                                            mResponse.getContentArray().write(buffer, 0, bytesRead);
                                            mResponse.setBytesReceived(mResponse.getBytesReceived() + bytesRead);
                                        }

                                        LOG2.debug(Util.getLogPrefix(this.getId(), mConnection, mResponse.getRequest()
                                                .getCounter())
                                                + "Bytes read so far: " + mResponse.getBytesReceived());

                                        if (mResponse.getBytesReceived() == mResponse.getHeadersLength()
                                                + mResponse.getContentLength()) {
                                            mEventHandler.handleEvent(this.getId(), mConnectionStatus, mError,
                                                    TestJobEvent.RESPONSE_BODY_COMPLETED);
                                        } else {

                                            mEventHandler.getFirstResponseTestJob().setResponse(mResponse);
                                            mEventHandler.handleEvent(this.getId(), mConnectionStatus, mError,
                                                    TestJobEvent.RESPONSE_BODY_READING);

                                            continue;
                                        }
                                    }
                                }

                                if (mEventHandler.getFirstResponseTestJob().getState().getValue() < TestJobEvent.RESPONSE_BODY_COMPLETED
                                        .getValue()) {
                                    continue;
                                }

                                if (mIStream instanceof ChunkedInputStream) {
                                    mResponse.getRawArray().write(mRawArray.toByteArray(), 0,
                                            ((ChunkedInputStream) mIStream).getBytesRead());
                                    mIStream = new MyInputStream(((ChunkedInputStream) mIStream).getMyInputStream());
                                } else {
                                    mResponse.getRawArray().write(mRawArray.toByteArray(), 0, mResponse.getContentLength());
                                }

                                mRawArray.reset();

                                mEventHandler.getFirstResponseTestJob().setResponse(mResponse);
                                mEventHandler.handleEvent(this.getId(), mConnectionStatus, mError,
                                        TestJobEvent.RESPONSE_COMPLETED);

                                // Check if SoTimeout needs to be changed
                                if (mResponse.getRequest().getNextReadTimeout() != null) {
                                    LOG2.debug(Util.getLogPrefix(this.getId(), mConnection, mResponse.getRequest()
                                            .getCounter())
                                            + "Setting socket read timeout: "
                                            + mResponse.getRequest().getNextReadTimeout()
                                            + " mSec");
                                    mSocket.setSoTimeout(mResponse.getRequest().getNextReadTimeout());
                                }

                                mResponse = null;

                                continue;

                            case 0:
                                throw new SocketTimeoutException();
                            case Constants.EOF:
                                throw new SocketException("EOF received");
                        }

                    } catch (SocketException e) {
                        // LOG2.warn(Util.getLogPrefix(this.getId(), mConnection) + e.getMessage());
                        mConnectionStatus = e.getMessage();
                        break;
                    } catch (AsynchronousCloseException e) {
                        // LOG2.warn(Util.getLogPrefix(this.getId(), mConnection) + "AsynchronousCloseException");
                        mConnectionStatus = "AsynchronousCloseException: " + e.getMessage();
                        break;
                    } catch (SocketTimeoutException e) {
                        // LOG2.warn(Util.getLogPrefix(this.getId(), mConnection) + "SocketTimeoutException: "
                        // + mSocket.getSoTimeout() + " mSec");
                        mConnectionStatus = "SocketTimeoutException: " + mSocket.getSoTimeout() + " mSec";
                        break;
                    } catch (Exception e) {
                        LOG2.error(Util.getLogPrefix(this.getId(), mConnection) + e.getMessage());
                        mConnectionStatus = e.getMessage();
                        break;
                    }
                }
            } catch (NullPointerException e) {
                mError = "NullPointerException";
            } catch (IllegalArgumentException e) {
                mError = "IllegalArgumentException: " + e.getMessage();
            } catch (Exception e) {
                mError = e.getMessage();
            } finally {

                safeSocketClose();

                mShutDown = true;

                mEventHandler.handleEvent(this.getId(), mConnectionStatus, mError, TestJobEvent.PIPELINE_SOCKET_CLOSED);

                if (mError != null) {
                    LOG2.error(Util.getLogPrefix(this.getId(), mConnection) + mError);
                }
                if (mConnectionStatus != null) {
                    LOG2.warn(Util.getLogPrefix(this.getId(), mConnection) + mConnectionStatus);
                }
                LOG2.debug(Util.getLogPrefix(this.getId(), mConnection) + "Connection closed");
                LOG2.debug(Util.getLogPrefix(this.getId(), mConnection) + "Shutting down ResponseHandler: "
                        + this.getName());
                mIStream = null;
                mResponse = null;

                // Notify main thread about to shutdown
                synchronized (mMainLock) {
                    mMainLock.notify();
                }
                this.interrupt();
            }
        }
    }

    private void safeSocketClose() {
        try {
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
            if (mSocketChannel != null) {
                mSocketChannel.close();
                mSocketChannel = null;
            }
        } catch (Exception ex) {
            LOG.debug(Util.getLogPrefix(this.getId(), mConnection) + "safeSocketClose(): " + ex.getMessage());
        }
    }
}
