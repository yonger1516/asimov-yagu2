package com.seven.asimov.test.tool.core;

import android.content.Context;
import android.util.Log;
import com.seven.asimov.test.tool.core.testjobs.TestJob;
import com.seven.asimov.test.tool.utils.Util;

import java.lang.Thread.State;
import java.util.ArrayList;

/**
 * PipelineFactory.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public abstract class PipelineFactory {

    private static final String LOG = "PipelineFactory";

    private static ArrayList<Pipeline> sPipelines = new ArrayList<Pipeline>();
    private static ArrayList<Pipeline> sNewSocketPipelines = new ArrayList<Pipeline>();
    private static Pipeline sServicePipeline;

    public static void sendRequestToPipeline(TestJob testJob, Context context) {
        Log.v(LOG, "sendRequestToPipeline()");

        int myPipelineId = 0;

        // Check if we need to open new pipeline
        if (testJob.getRequest().getNewSocket() == 0) {

            // Check if we there is a pipeline exists with same host and port
            boolean needToAddNewPipeline = true;
            String newHostAndPort = testJob.getRequest().getUri().getHost() + ":"
                    + testJob.getRequest().getUri().getPort();

            if (sPipelines.size() != 0) {
                for (int i = 0; i < sPipelines.size(); i++) {
                    if (newHostAndPort.equals(sPipelines.get(i).getHostAndPort())) {
                        myPipelineId = i;
                        needToAddNewPipeline = false;
                        break;
                    }
                }
            }

            while (true) {

                if (needToAddNewPipeline) {
                    sPipelines.add(new Pipeline(context, TestFactory.getConnectionCounter() + 1));
                    TestFactory.setConnectionCounter(TestFactory.getConnectionCounter() + 1);
                    myPipelineId = sPipelines.size() - 1;
                }

                // Start, resume, restart threads
                State threadState = sPipelines.get(myPipelineId).getState();
                boolean isShutDown = sPipelines.get(myPipelineId).isShutDown();

                Log.d(LOG,
                        Util.getLogPrefix(sPipelines.get(myPipelineId).getId(), sPipelines.get(myPipelineId)
                                .getConnection())
                                + "state: " + threadState.name());

                if (threadState == State.NEW) {
                    sPipelines.get(myPipelineId).getTestJobQueue().add(testJob);
                    sPipelines.get(myPipelineId).start();
                    break;
                } else if (threadState == State.WAITING && !isShutDown) {
                    sPipelines.get(myPipelineId).getTestJobQueue().add(testJob);
                    // Notify thread
                    synchronized (sPipelines.get(myPipelineId).getMainLock()) {
                        sPipelines.get(myPipelineId).getMainLock().notify();
                    }
                    break;
                } else if (threadState == State.RUNNABLE && !isShutDown) {
                    sPipelines.get(myPipelineId).getTestJobQueue().add(testJob);
                    break;
                } else if (threadState == State.TERMINATED || isShutDown) {
                    sPipelines.remove(myPipelineId);
                    needToAddNewPipeline = true;
                    continue;
                }
            }
            // Log.d(LOG, Utils.getLogPrefix(sPipelines.get(0).getId(), sPipelines.get(0).getConnection()) + "Queue: "
            // + sPipelines.get(0).getTestJobQueue().size());
        } else {
            for (int i = 0; i < testJob.getRequest().getNewSocket(); i++) {
                sNewSocketPipelines.add(new Pipeline(context, TestFactory.getConnectionCounter()));
                Log.d(LOG, Util.getLogPrefix(sNewSocketPipelines.get(0).getId(), TestFactory.getConnectionCounter())
                        + "New socket by direct command! (" + (i + 1) + " of " + testJob.getRequest().getNewSocket()
                        + ")");
                TestFactory.setConnectionCounter(TestFactory.getConnectionCounter() + 1);
                sNewSocketPipelines.get(0).getTestJobQueue().add(testJob);
                sNewSocketPipelines.get(0).start();
                sNewSocketPipelines.remove(0);
            }
        }
    }

    public static ArrayList<Pipeline> getActivePipelines() {
        ArrayList<Pipeline> result = new ArrayList<Pipeline>();
        for (Pipeline pipeline : sPipelines) {
            State threadState = pipeline.getState();
            if (threadState != State.TERMINATED) {
                result.add(pipeline);
            }
        }
        return result;
    }

    public static void sendServiceRequest(TestJob testJob, Context context) {
        Log.v(LOG, "sendServiceRequest()");
        if (sServicePipeline == null) {
            sServicePipeline = new Pipeline(context, TestFactory.getConnectionCounter() + 1);
            TestFactory.setConnectionCounter(TestFactory.getConnectionCounter() + 1);
        }
        // Start thread
        State threadState = sServicePipeline.getState();
        Log.d(LOG, "Thread[" + sServicePipeline.getId() + "] State: " + threadState.name());
        if (threadState == State.NEW) {
            sServicePipeline.getTestJobQueue().add(testJob);
            sServicePipeline.start();
            return;
        }
        // Restart thread
        if (threadState == State.TERMINATED) {
            sServicePipeline = new Pipeline(context, TestFactory.getConnectionCounter());
            TestFactory.setConnectionCounter(TestFactory.getConnectionCounter() + 1);
            sServicePipeline.getTestJobQueue().add(testJob);
            sServicePipeline.start();
            return;
        }
    }
}
