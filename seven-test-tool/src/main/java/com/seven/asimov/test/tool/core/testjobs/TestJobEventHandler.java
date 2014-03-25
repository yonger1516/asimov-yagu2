package com.seven.asimov.test.tool.core.testjobs;

import android.content.Context;
import android.content.Intent;
import com.seven.asimov.test.tool.activity.AdminTab;
import com.seven.asimov.test.tool.activity.MainTab;
import com.seven.asimov.test.tool.activity.TestsTab;
import com.seven.asimov.test.tool.constants.Constants;
import com.seven.asimov.test.tool.core.HttpMessageLogger;
import com.seven.asimov.test.tool.core.Pipeline;
import com.seven.asimov.test.tool.core.Response;
import com.seven.asimov.test.tool.core.TestFactory;
import com.seven.asimov.test.tool.serialization.TestItem;
import com.seven.asimov.test.tool.serialization.TestSuite;
import com.seven.asimov.test.tool.serialization.TestSuiteList;
import com.seven.asimov.test.tool.utils.Util;
import com.seven.asimov.test.tool.utils.Z7HttpUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TestJobEventHandler.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class TestJobEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TestJobEventHandler.class.getSimpleName());

    private Map<Integer, TestJob> mTestJobMap;
    private int mCurrentRequestId;
    private int mRequestCounter;
    private int mResponseCounter;

    private HttpMessageLogger mHttpMessageLogger;
    private final Context mContext;
    private final int mConnection;

    public TestJobEventHandler(Context context, int connection) {

        this.mContext = context;
        this.mConnection = connection;

        mTestJobMap = new ConcurrentHashMap<Integer, TestJob>();
        mCurrentRequestId = 0;
        mRequestCounter = 0;
        mResponseCounter = 0;

        mHttpMessageLogger = new HttpMessageLogger(connection);
    }

    public HttpMessageLogger getHttpMessageLogger() {
        return mHttpMessageLogger;
    }

    public TestJob get(int id) {
        return mTestJobMap.get(id);
    }

    public void add(TestJob testJob) {
        mTestJobMap.put(mRequestCounter, testJob);
        mRequestCounter++;
    }

    public void remove(Integer id) {
        mTestJobMap.remove(id);
    }

    public TestJob getFreshTestJob() {

        TestJob result = null;

        for (int i = mCurrentRequestId; i < mRequestCounter; i++) {
            TestJob testJob = mTestJobMap.get(i);
            if (testJob.getState() == TestJobEvent.EMPTY) {
                return testJob;
            }
        }
        return result;
    }

    public TestJob getFirstResponseTestJob() {

        TestJob result = get(getFirstResponseId());
        return result;
    }

    public Integer getFirstResponseId() {
        return mResponseCounter;
    }

    public int size() {
        return mTestJobMap.size();
    }

    public synchronized void handleEvent(long threadId, String connectionStatus, String error, TestJobEvent event) {

        LOG.debug(Util.getLogPrefix(threadId, mConnection) + "Event: " + event.name());

        TestJobToast toast;
        TestJobType tjType;

        switch (event) {
            case PIPELINE_STARTED:
                // mState = event;
                break;

            // Requests
            case REQUEST_STARTED:

                mTestJobMap.get(mCurrentRequestId).setState(event);

                break;
            case REQUEST_HEADER:

                mTestJobMap.get(mCurrentRequestId).setState(event);

                break;
            case REQUEST_COMPLETED:

                mTestJobMap.get(mCurrentRequestId).setState(event);
                // Log request
                mHttpMessageLogger.getRequestQueue().add(mTestJobMap.get(mCurrentRequestId).getRequest());
                mHttpMessageLogger.log(event);

                mCurrentRequestId++;

                break;

            // Responses
            case RESPONSE_STARTED:

                mTestJobMap.get(mResponseCounter).setState(event);

                break;
            case RESPONSE_HEADER_STARTED:

                mTestJobMap.get(mResponseCounter).setState(event);

                if (mTestJobMap.get(mResponseCounter).getRequest().isDispatchOnFirstByte()) {

                    if (mTestJobMap.get(mResponseCounter).getType() == TestJobType.BUTTON_LOOPED_REQUESTS) {

                        TestJob newTestJob = new TestJob(mTestJobMap.get(mResponseCounter).getType());
                        TestFactory.addTestJob(newTestJob);

                    }
                }
                break;
            case RESPONSE_HEADER_COMPLETED:

                mTestJobMap.get(mResponseCounter).setState(event);

                tjType = mTestJobMap.get(mResponseCounter).getType();

                if (tjType == TestJobType.SERVICE_REQUEST_LOADTEST || tjType == TestJobType.SERVICE_REQUEST_LOADTESTS) {

                    Response response = mTestJobMap.get(mResponseCounter).getResponse();

                    // Update progress bar
                    toast = new TestJobToast(mTestJobMap.get(mResponseCounter).getType(), mConnection);
                    toast.setMessage("Max:" + response.getContentLength());
                    broadcastToUi(toast);
                }

                break;
            case RESPONSE_BODY_STARTED:

                mTestJobMap.get(mResponseCounter).setState(event);

                break;
            case RESPONSE_BODY_READING:

                mTestJobMap.get(mResponseCounter).setState(event);

                tjType = mTestJobMap.get(mResponseCounter).getType();

                if (tjType == TestJobType.SERVICE_REQUEST_LOADTEST || tjType == TestJobType.SERVICE_REQUEST_LOADTESTS) {

                    Response response = mTestJobMap.get(mResponseCounter).getResponse();

                    // Update progress bar
                    toast = new TestJobToast(mTestJobMap.get(mResponseCounter).getType(), mConnection);
                    toast.setMessage("Progress:" + response.getContentLength());
                    broadcastToUi(toast);
                }

                break;
            case RESPONSE_BODY_COMPLETED:

                mTestJobMap.get(mResponseCounter).setState(event);

                break;
            case RESPONSE_COMPLETED:

                mTestJobMap.get(mResponseCounter).setState(TestJobEvent.EMPTY);

                // Log response
                mHttpMessageLogger.getResponseQueue().add(mTestJobMap.get(mResponseCounter).getResponse());
                mHttpMessageLogger.log(event);

                Response response = mTestJobMap.get(mResponseCounter).getResponse();

                tjType = mTestJobMap.get(mResponseCounter).getType();

                switch (tjType) {

                    default:
                        break;

                    case BUTTON_SINGLE_REQUEST:

                        if (!response.isExpectContinue()) {
                            TestJobStates.setJobState(tjType, TestJobState.NOT_RUNNING);
                        }

                        toast = new TestJobToast(mTestJobMap.get(mResponseCounter).getType(), mConnection);
                        toast.setHttpStatus(mTestJobMap.get(mResponseCounter).getResponse().getStatusLine());
                        broadcastToUi(toast);

                        break;

                    case BUTTON_PERIODIC_REQUESTS:

                        toast = new TestJobToast(mTestJobMap.get(mResponseCounter).getType(), mConnection);
                        toast.setHttpStatus(mTestJobMap.get(mResponseCounter).getResponse().getStatusLine());
                        broadcastToUi(toast);

                        break;

                    case BUTTON_LOOPED_REQUESTS:

                        if (!mTestJobMap.get(mResponseCounter).getRequest().isDispatchOnFirstByte()) {
                            TestJob newTestJob = new TestJob(mTestJobMap.get(mResponseCounter).getType());
                            TestFactory.addTestJob(newTestJob);
                        }

                        toast = new TestJobToast(mTestJobMap.get(mResponseCounter).getType(), mConnection);
                        toast.setHttpStatus(mTestJobMap.get(mResponseCounter).getResponse().getStatusLine());
                        broadcastToUi(toast);

                        break;

                    case AUTOMATED_TEST:

                        toast = new TestJobToast(mTestJobMap.get(mResponseCounter).getType(), mConnection);
                        toast.setHttpStatus(mTestJobMap.get(mResponseCounter).getResponse().getStatusLine());

                        toast.setValidated(mTestJobMap.get(mResponseCounter).validate(event, mResponseCounter));

                        broadcastToUi(toast);

                        break;

                    case SERVICE_REQUEST_LOADTEST:

                        toast = new TestJobToast(mTestJobMap.get(mResponseCounter).getType(), mConnection);
                        toast.setMessage("Received:" + mTestJobMap.get(mResponseCounter).getResponse().getStatusLine());
                        broadcastToUi(toast);

                        try {
                            TestSuite testSuite = (TestSuite) Util.deserializeXMLToObject(TestSuite.class,
                                    mTestJobMap.get(mResponseCounter).getResponse().getContentArray().toByteArray());
                            for (TestItem test : testSuite.getTestItems()) {
                                if (!test.getRequestHeaders().contains(Constants.HTTP_NEW_LINE)) {
                                    test.setRequestHeaders(test.getRequestHeaders().replace(Constants.HTTP_NEW_LINE_CHROME,
                                            Constants.HTTP_NEW_LINE));
                                }
                                test.setHttpMethod(Z7HttpUtil.getHttpMethodFromHeaders(test.getRequestHeaders()));
                                test.setUri(Z7HttpUtil.getUriFromHeaders(test.getRequestHeaders()));
                            }
                            TestFactory.setTestSuite(testSuite);
                            TestFactory.setFreshTestSuiteLoadedFromServer(true);
                        } catch (Exception e) {
                            LOG.error(Util.getLogPrefix(threadId, mConnection) + event + e.getMessage());
                        }

                        toast = new TestJobToast(mTestJobMap.get(mResponseCounter).getType(), mConnection);
                        toast.setHttpStatus(mTestJobMap.get(mResponseCounter).getResponse().getStatusLine());
                        broadcastToUi(toast);

                        break;

                    case SERVICE_REQUEST_LOADTESTS:

                        toast = new TestJobToast(mTestJobMap.get(mResponseCounter).getType(), mConnection);
                        toast.setMessage("Received:" + mTestJobMap.get(mResponseCounter).getResponse().getStatusLine());
                        broadcastToUi(toast);

                        try {
                            TestSuiteList testSuiteList = (TestSuiteList) Util.deserializeXMLToObject(TestSuiteList.class,
                                    mTestJobMap.get(mResponseCounter).getResponse().getContentArray().toByteArray());
                            TestsTab.getTestSuiteList().clear();
                            for (int i = 0; i < testSuiteList.getTestSuites().size(); i++) {
                                TestsTab.getTestSuiteList().put(i, testSuiteList.getTestSuites().get(i));
                            }

                        } catch (Exception e) {
                            LOG.error(Util.getLogPrefix(threadId, mConnection) + event + e.getMessage());
                        }

                        toast = new TestJobToast(mTestJobMap.get(mResponseCounter).getType(), mConnection);
                        toast.setHttpStatus(mTestJobMap.get(mResponseCounter).getResponse().getStatusLine());
                        broadcastToUi(toast);

                        break;
                }

                // Remove TestJob from queue or reset state
                if (!response.isExpectContinue()) {
                    mTestJobMap.remove(mResponseCounter);
                    mResponseCounter++;
                } else {
                    mTestJobMap.get(mResponseCounter).setState(TestJobEvent.REQUEST_COMPLETED);
                }

                break;
            case PIPELINE_SOCKET_CLOSED:

                if (mTestJobMap.size() != 0) {

                    // for (Map.Entry<Integer, TestJob> e : mTestJobMap.entrySet()) {
                    // Integer key = e.getKey();
                    // TestJob testJob = e.getValue();
                    // }

                    Iterator<Integer> iter = mTestJobMap.keySet().iterator();

                    while (iter.hasNext()) {

                        Integer key = iter.next();

                        TestJob testJob = mTestJobMap.get(key);

                        if (testJob.getState().getValue() < TestJobEvent.RESPONSE_COMPLETED.getValue()) {

                            // Log response
                            mHttpMessageLogger.getResponseQueue().add(mTestJobMap.get(key).getResponse());
                            mHttpMessageLogger.log(event);

                            if (testJob.getType() == TestJobType.BUTTON_SINGLE_REQUEST) {
                                TestJobStates.setJobState(testJob.getType(), TestJobState.NOT_RUNNING);
                            } else if (testJob.getType() == TestJobType.BUTTON_LOOPED_REQUESTS) {
                                TestJobStates.setJobState(testJob.getType(), TestJobState.NOT_RUNNING);
                            }

                            toast = new TestJobToast(testJob.getType(), mConnection);
                            toast.setConnectionStatus(connectionStatus);
                            toast.setError(error);
                            broadcastToUi(toast);
                        }
                        mTestJobMap.remove(key);
                    }
                } else {
                    toast = new TestJobToast(TestJobType.EOF, mConnection);
                    toast.setConnectionStatus(connectionStatus);
                    toast.setError(error);
                    broadcastToUi(toast);
                }

                mHttpMessageLogger.shutDown();

                break;
            case PIPELINE_COMPLETED:

                Iterator<Integer> iter = mTestJobMap.keySet().iterator();

                while (iter.hasNext()) {

                    Integer key = iter.next();

                    TestJob testJob = mTestJobMap.get(key);

                    if (testJob.getState() != TestJobEvent.RESPONSE_COMPLETED) {

                        if (testJob.getType() == TestJobType.BUTTON_SINGLE_REQUEST) {
                            TestJobStates.setJobState(testJob.getType(), TestJobState.NOT_RUNNING);
                        } else if (testJob.getType() == TestJobType.BUTTON_LOOPED_REQUESTS) {
                            TestJobStates.setJobState(testJob.getType(), TestJobState.NOT_RUNNING);
                        }

                        toast = new TestJobToast(testJob.getType(), mConnection);
                        toast.setConnectionStatus(connectionStatus);
                        toast.setError(error);
                        broadcastToUi(toast);
                    }

                    mTestJobMap.remove(key);
                }

                mHttpMessageLogger.shutDown();

                break;
            default:
                break;
        }
    }

    // Display methods
    private void broadcastToUi(TestJobToast toast) {

        LOG.debug("broadcastToUi()");

        Intent intent;
        if (toast.getTestJobType() == null) {
            intent = new Intent(MainTab.ACTION_DISPLAY);
        } else {
            switch (toast.getTestJobType()) {
                default:
                    intent = new Intent(MainTab.ACTION_DISPLAY);
                    break;
                case SERVICE_REQUEST_LOADTEST:
                    intent = new Intent(TestsTab.ACTION_DISPLAY);
                    break;
                case SERVICE_REQUEST_LOADTESTS:
                    intent = new Intent(TestsTab.ACTION_DISPLAY);
                    break;
                case SERVICE_REQUEST_LOADSCOPES:
                    intent = new Intent(TestsTab.ACTION_DISPLAY);
                    break;
                case SERVICE_PROXY_REQUEST_CHANGERELAY:
                    intent = new Intent(AdminTab.ACTION_DISPLAY);
                    break;
                case SERVICE_PROXY_REQUEST_CHANGEPROXY:
                    intent = new Intent(AdminTab.ACTION_DISPLAY);
                    break;
                case SERVICE_PROXY_REQUEST_INVALIDATEALL:
                    intent = new Intent(AdminTab.ACTION_DISPLAY);
                    break;
            }
        }
        intent.putExtra(Pipeline.MESSAGE, StringUtils.EMPTY);
        intent.putExtra("Toast", toast);
        broadcastIntent(intent);
    }

    private void broadcastIntent(Intent intent) {
        // Log.v(LOG, "displayIntent()");
        mContext.sendBroadcast(intent);
    }

}
