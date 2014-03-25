package com.seven.asimov.test.tool.core;

import com.seven.asimov.test.tool.core.testjobs.TestJob;
import com.seven.asimov.test.tool.core.testjobs.TestJobEvent;
import com.seven.asimov.test.tool.core.testjobs.TestJobState;
import com.seven.asimov.test.tool.core.testjobs.TestJobStates;
import com.seven.asimov.test.tool.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * HttpMessageLogger thread.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public class HttpMessageLogger extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(HttpMessageLogger.class.getSimpleName());

    public static final int MESSAGE_SIZE_HEX_LOGGING_LIMIT = 1024;
    public static final int MESSAGE_SIZE_HEX_LOGGING_LINE_LENGTH = 16;

    private final int mDefaultInputBufferSize = 8192;
    private int mInputBufferSize = mDefaultInputBufferSize;

    private Object mLock = new Object();
    private int mConnection;

    private ArrayList<TestJobEvent> mEventQueue = new ArrayList<TestJobEvent>();
    private ArrayList<Request> mRequestQueue = new ArrayList<Request>();
    private ArrayList<Response> mResponseQueue = new ArrayList<Response>();

    private boolean mShutDown;

    public HttpMessageLogger(int connection) {
        this.mConnection = connection;
    }

    public ArrayList<Request> getRequestQueue() {
        return mRequestQueue;
    }

    public ArrayList<Response> getResponseQueue() {
        return mResponseQueue;
    }

    public Object getLock() {
        return mLock;
    }

    public ArrayList<TestJobEvent> getEventQueue() {
        return mEventQueue;
    }

    public void shutDown() {

        mShutDown = true;

        State threadState = getState();
        if (threadState == State.WAITING) {
            synchronized (getLock()) {
                getLock().notify();
            }
        }
    }

    public void log(TestJobEvent event) {

        getEventQueue().add(event);

        State threadState = getState();
        if (threadState == State.NEW) {
            this.start();
        } else if (threadState == State.WAITING) {
            synchronized (getLock()) {
                getLock().notify();
            }
        }
    }

    public void run() {
        try {
            while (true) {
                if (mShutDown) {
                    break;
                }
                if (mEventQueue.size() == 0) {
                    synchronized (mLock) {
                        mLock.wait();
                    }
                }
                if (mEventQueue.size() != 0) {
                    switch (mEventQueue.get(0)) {
                        default:
                            break;
                        case REQUEST_COMPLETED:
                            while (mRequestQueue.size() != 0) {
                                LOG.debug(Util.getLogPrefix(this.getId(), mConnection, mRequestQueue.get(0).getCounter())
                                        + "Request:");
                                LOG.info(mRequestQueue.get(0).getRequestString());
                                mRequestQueue.remove(0);
                            }
                            mEventQueue.remove(0);
                            break;
                        case RESPONSE_COMPLETED:
                            while (mResponseQueue.size() != 0) {

                                Response response = mResponseQueue.get(0);

                                LOG.debug(Util.getLogPrefix(this.getId(), mConnection, response.getRequest().getCounter())
                                        + "Totally bytes received: " + response.getBytesReceived() + " bytes");
                                if (response.getRawArray().size() <= MESSAGE_SIZE_HEX_LOGGING_LIMIT) {

                                    byte[] bytes = response.getRawArray().toByteArray();
                                    Util.logHexDump(LOG, bytes, MESSAGE_SIZE_HEX_LOGGING_LINE_LENGTH);

                                    // String visualBytes = StringUtil.EMPTY;
                                    // int row = 0;
                                    // for (int i = 0; i < response.getRawArray().size(); i++) {
                                    // byte b = response.getRawArray().toByteArray()[i];
                                    // visualBytes += String.valueOf(b) + " ";
                                    // row++;
                                    // if (row == mByteLoggingRows) {
                                    // LOG.debug(Util.getLogPrefix(this.getId(), mConnection, response.getRequest()
                                    // .getCounter())
                                    // + visualBytes);
                                    // visualBytes = StringUtil.EMPTY;
                                    // row = 0;
                                    // continue;
                                    // }
                                    // if (i == (response.getRawArray().size() - 1)) {
                                    // LOG.debug(Util.getLogPrefix(this.getId(), mConnection, response.getRequest()
                                    // .getCounter())
                                    // + visualBytes);
                                    // }
                                    // }
                                } else {
                                    LOG.debug(Util.getLogPrefix(this.getId(), mConnection, response.getRequest()
                                            .getCounter())
                                            + "Will not log bytes, too many of them (limit="
                                            + MESSAGE_SIZE_HEX_LOGGING_LIMIT
                                            + " bytes)");
                                }
                                if (response.getContentArray() != null) {
                                    decodeBody();
                                    if (!response.getRequest().isServiceRequest()) {
                                        response.parseBody();
                                    }
                                }

                                response.handleParameters();

                                LOG.debug(Util.getLogPrefix(this.getId(), mConnection, response.getRequest().getCounter())
                                        + "Response:");
                                LOG.info(response.getRawArray().toString());

                                // Configure redirect if needed
                                if (response.isRedirect()) {
                                    if (response.getRedirectUri() != null) {

                                        Request newRequest = new Request();
                                        try {
                                            newRequest.buildRedirect(response.getRedirectUri());

                                            LOG.debug(Util.getLogPrefix(this.getId(), mConnection) + "Redirecting to "
                                                    + response.getRedirectUri());

                                            if (response.getMetaTimeout() != null) {
                                                LOG.debug(Util.getLogPrefix(this.getId(), mConnection)
                                                        + "Meta redirect, need to sleep " + response.getMetaTimeout()
                                                        / 1000 + " sec...");
                                                sleep(response.getMetaTimeout());
                                            }

                                            TestJobStates.setJobState(response.getRequest().getTestJobTy(),
                                                    TestJobState.IS_RUNNING);

                                            TestJob newTestJob = new TestJob(response.getRequest().getTestJobTy());
                                            newTestJob.setRequest(newRequest);

                                            TestFactory.addTestJob(newTestJob);

                                        } catch (Exception e) {
                                            LOG.error(Util.getLogPrefix(this.getId(), mConnection) + e.getMessage());
                                        }
                                    }
                                }

                                mResponseQueue.remove(0);
                            }
                            mEventQueue.remove(0);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(Util.getLogPrefix(this.getId(), mConnection) + e.getMessage());
        } finally {
            LOG.debug(Util.getLogPrefix(this.getId(), mConnection) + "Shutting down HttpMessageLogger: "
                    + this.getName());
            this.interrupt();
        }
    }

    private void decodeBody() throws Exception {
        // Decode content if needed
        while (true) {
            if (mResponseQueue.get(0).isGzip()) {
                LOG.debug(Util.getLogPrefix(this.getId(), mConnection, mResponseQueue.get(0).getRequest().getCounter())
                        + "Decoding gzip type content...");
                ByteArrayInputStream in = new ByteArrayInputStream(mResponseQueue.get(0).getContentArray()
                        .toByteArray());
                mResponseQueue.get(0).getContentArray().reset();
                GZIPInputStream gzip = new GZIPInputStream(in);
                byte[] isbuffer = new byte[mInputBufferSize];
                while (true) {
                    int n = gzip.read(isbuffer, 0, isbuffer.length);
                    if (n != -1) {
                        if (n == mInputBufferSize) {
                            mInputBufferSize = mInputBufferSize * 2;
                        }
                        mResponseQueue.get(0).getContentArray().write(isbuffer, 0, n);
                    } else {
                        LOG.debug(Util.getLogPrefix(this.getId(), mConnection, mResponseQueue.get(0).getRequest()
                                .getCounter())
                                + "Gzip decoded");
                        break;
                    }
                }
                // Close streams
                in.close();
                gzip.close();
                break;
            }
            if (mResponseQueue.get(0).isDeflate()) {
                LOG.debug(Util.getLogPrefix(this.getId(), mConnection, mResponseQueue.get(0).getRequest().getCounter())
                        + "Decoding deflate type content...");
                ByteArrayInputStream in = new ByteArrayInputStream(mResponseQueue.get(0).getContentArray()
                        .toByteArray());
                mResponseQueue.get(0).getContentArray().reset();
                InflaterInputStream inflate = new InflaterInputStream(in);
                byte[] isbuffer = new byte[mInputBufferSize];
                while (true) {
                    int n = inflate.read(isbuffer, 0, isbuffer.length);
                    if (n != -1) {
                        if (n == mInputBufferSize) {
                            mInputBufferSize = mInputBufferSize * 2;
                        }
                        mResponseQueue.get(0).getContentArray().write(isbuffer, 0, n);
                    } else {
                        LOG.debug(Util.getLogPrefix(this.getId(), mConnection, mResponseQueue.get(0).getRequest()
                                .getCounter())
                                + "Deflate decoded");
                        break;
                    }
                }
                // Close streams
                in.close();
                inflate.close();
                break;
            }
            if (mResponseQueue.get(0).isCompress()) {
                LOG.debug("Decoding compress type content...");
                ByteArrayInputStream in = new ByteArrayInputStream(mResponseQueue.get(0).getContentArray()
                        .toByteArray());
                mResponseQueue.get(0).getContentArray().reset();
                ZipInputStream zip = new ZipInputStream(in);
                // Get the first entry
                ZipEntry entry = zip.getNextEntry();
                LOG.debug(Util.getLogPrefix(this.getId(), mConnection, mResponseQueue.get(0).getRequest().getCounter())
                        + "ZipEntry Name:" + entry.getName());
                byte[] isbuffer = new byte[mInputBufferSize];
                while (true) {
                    int n = zip.read(isbuffer, 0, isbuffer.length);
                    if (n != -1) {
                        if (n == mInputBufferSize) {
                            mInputBufferSize = mInputBufferSize * 2;
                        }
                        mResponseQueue.get(0).getContentArray().write(isbuffer, 0, n);
                    } else {
                        LOG.debug(Util.getLogPrefix(this.getId(), mConnection, mResponseQueue.get(0).getRequest()
                                .getCounter())
                                + "Compress decoded");
                        break;
                    }
                }
                // Close streams
                in.close();
                zip.close();
                break;
            }
            break;
        }
        mResponseQueue.get(0).setBody(new String(mResponseQueue.get(0).getContentArray().toByteArray()));
        if (mResponseQueue.get(0).isChunked() || mResponseQueue.get(0).isGzip() || mResponseQueue.get(0).isDeflate()
                || mResponseQueue.get(0).isCompress()) {
            LOG.debug(Util.getLogPrefix(this.getId(), mConnection, mResponseQueue.get(0).getRequest().getCounter())
                    + "Decoded content: " + mResponseQueue.get(0).getBody());
        }
    }
}
