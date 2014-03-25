package com.seven.asimov.test.tool.validation;

import com.seven.asimov.test.tool.core.TestFactory;
import com.seven.asimov.test.tool.core.testjobs.TestJob;
import com.seven.asimov.test.tool.core.testjobs.TestJobEvent;
import com.seven.asimov.test.tool.core.testjobs.TestJobState;
import com.seven.asimov.test.tool.core.testjobs.TestJobStates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ValidationFactory.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public abstract class ValidationFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationFactory.class.getSimpleName());

    public static Boolean validate(TestJob tj, TestJobEvent tjEvent, int counter, VerificationPattern vp) {

        // Stop test if verification pattern is missing
        if (vp == null) {
            TestJobStates.setJobState(tj.getTestSuiteName(), TestJobState.NOT_RUNNING);
            TestFactory.removeTestJobs(tj.getTestSuiteName());
        }

        Boolean result = null;

        switch (tjEvent) {
            default:
                break;
            case RESPONSE_COMPLETED:

                VerificationItem vi = vp.getResponseVerificationItem(counter + 1);

                if (vi != null) {

                    result = false;

                    VerificationTags vTag = vi.getTag();

                    switch (vTag) {
                        default:
                            break;
                        case VALIDATE_RESP_SIZE:
                            switch (vi.getOperator()) {
                                default:
                                    break;
                                case EQUAL_TO:
                                    if (vi.getSize() == tj.getResponse().getBytesReceived()) {
                                        result = true;
                                    }
                                    break;
                                case NOT_EQUAL_TO:
                                    if (vi.getSize() != tj.getResponse().getBytesReceived()) {
                                        result = true;
                                    }
                                    break;
                                case GREATER_THAN:
                                    if (vi.getSize() > tj.getResponse().getBytesReceived()) {
                                        result = true;
                                    }
                                    break;
                                case GREATER_THAN_OR_EQUAL_TO:
                                    if (vi.getSize() >= tj.getResponse().getBytesReceived()) {
                                        result = true;
                                    }
                                    break;
                                case LESS_THAN:
                                    if (vi.getSize() < tj.getResponse().getBytesReceived()) {
                                        result = true;
                                    }
                                    break;
                                case LESS_THAN_OR_EQUAL_TO:
                                    if (vi.getSize() <= tj.getResponse().getBytesReceived()) {
                                        result = true;
                                    }
                                    break;
                                case PRECISION_WITHIN:
                                    if (vi.getSize() >= tj.getResponse().getBytesReceived()) {
                                        result = true;
                                    }
                                    break;
                                case PRECISION_AS_HIGH_AS:
                        /* TODO */
                                    break;
                                case PRECISION_AS_LOW_AS:
                        /* TODO */
                                    break;
                            }
                            if (result) {
                                LOG.info(String.format("{0}, excpected: {1}, actual: {2}. PASSED!", vTag, vi.getSize(), tj
                                        .getResponse().getBytesReceived()));
                            } else {
                                LOG.error(String.format("{0}, excpected: {1}, actual: {2}. FAILED!", vTag, vi.getSize(), tj
                                        .getResponse().getBytesReceived()));
                            }
                            break;
                        case VALIDATE_RESP_STATUS_CODE:
                            switch (vi.getOperator()) {
                                default:
                                    break;
                                case EQUAL_TO:
                                    if (vi.getStatusCode() == tj.getResponse().getStatusCode()) {
                                        result = true;
                                    }
                                    break;
                                case NOT_EQUAL_TO:
                                    if (vi.getStatusCode() != tj.getResponse().getStatusCode()) {
                                        result = true;
                                    }
                                    break;
                                case GREATER_THAN:
                                    if (vi.getStatusCode() > tj.getResponse().getStatusCode()) {
                                        result = true;
                                    }
                                    break;
                                case GREATER_THAN_OR_EQUAL_TO:
                                    if (vi.getStatusCode() >= tj.getResponse().getStatusCode()) {
                                        result = true;
                                    }
                                    break;
                                case LESS_THAN:
                                    if (vi.getStatusCode() < tj.getResponse().getStatusCode()) {
                                        result = true;
                                    }
                                    break;
                                case LESS_THAN_OR_EQUAL_TO:
                                    if (vi.getStatusCode() <= tj.getResponse().getStatusCode()) {
                                        result = true;
                                    }
                                    break;
                                case PRECISION_WITHIN:
                        /* TODO */
                                    break;
                                case PRECISION_AS_HIGH_AS:
                        /* TODO */
                                    break;
                                case PRECISION_AS_LOW_AS:
                        /* TODO */
                                    break;
                            }
                            if (result) {
                                LOG.info(String.format("{0}, excpected: {1}, actual: {2}. PASSED!", vTag, vi.getStatusCode(),
                                        tj.getResponse().getStatusCode()));
                            } else {
                                LOG.error(String.format("{0}, excpected: {1}, actual: {2}. FAILED!", vTag, vi.getStatusCode(),
                                        tj.getResponse().getStatusCode()));
                            }
                            break;
                        case VALIDATE_RESP_HEADERS:
                            switch (vi.getOperator()) {
                                default:
                                    break;
                                case EQUAL_TO:
                        /* TODO */
                                    break;
                                case NOT_EQUAL_TO:
                        /* TODO */
                                    break;
                                case CONTAINS:
                        /* TODO */
                                    break;
                                case CONTAINS_EXACT_NUMBER_OF_INSTANCES:
                        /* TODO */
                                    break;
                                case BEGINS_WITH:
                        /* TODO */
                                    break;
                                case ENDS_WITH:
                        /* TODO */
                                    break;
                            }
                    /* TODO Log result */
                            // if (result) {
                            // LOG.info(vTag + ": " + vi.getStatusCode() + " - PASSED!");
                            // } else {
                            // LOG.error(vTag + ": " + vi.getStatusCode() + " - FAILED! Actual result: "
                            // + tj.getResponse().getStatusCode());
                            // }
                            break;

                        case VALIDATE_RESP_BODY:
                            switch (vi.getOperator()) {
                                default:
                                    break;
                                case EQUAL_TO:
                        /* TODO */
                                    break;
                                case NOT_EQUAL_TO:
                        /* TODO */
                                    break;
                                case CONTAINS:
                        /* TODO */
                                    break;
                                case CONTAINS_EXACT_NUMBER_OF_INSTANCES:
                        /* TODO */
                                    break;
                                case BEGINS_WITH:
                        /* TODO */
                                    break;
                                case ENDS_WITH:
                        /* TODO */
                                    break;
                    /* TODO Log result */
                                // if (result) {
                                // LOG.info(vTag + ": " + vi.getStatusCode() + " - PASSED!");
                                // } else {
                                // LOG.error(vTag + ": " + vi.getStatusCode() + " - FAILED! Actual result: "
                                // + tj.getResponse().getStatusCode());
                                // }
                            }
                            break;
                        case VALIDATE_RESP_BODY_SIZE:
                            switch (vi.getOperator()) {
                                default:
                                    break;
                                case EQUAL_TO:
                        /* TODO */
                                    break;
                                case NOT_EQUAL_TO:
                        /* TODO */
                                    break;
                                case GREATER_THAN:
                        /* TODO */
                                    break;
                                case GREATER_THAN_OR_EQUAL_TO:
                        /* TODO */
                                    break;
                                case LESS_THAN:
                        /* TODO */
                                    break;
                                case LESS_THAN_OR_EQUAL_TO:
                        /* TODO */
                                    break;
                                case PRECISION_WITHIN:
                        /* TODO */
                                    break;
                                case PRECISION_AS_HIGH_AS:
                        /* TODO */
                                    break;
                                case PRECISION_AS_LOW_AS:
                        /* TODO */
                                    break;
                            }
                    /* TODO Log result */
                            // if (result) {
                            // LOG.info(vTag + ": " + vi.getStatusCode() + " - PASSED!");
                            // } else {
                            // LOG.error(vTag + ": " + vi.getStatusCode() + " - FAILED! Actual result: "
                            // + tj.getResponse().getStatusCode());
                            // }
                            break;

                        case VALIDATE_RESP_TIME:
                            switch (vi.getOperator()) {
                                default:
                                    break;
                                case EQUAL_TO:
                        /* TODO */
                                    break;
                                case NOT_EQUAL_TO:
                        /* TODO */
                                    break;
                                case GREATER_THAN:
                        /* TODO */
                                    break;
                                case GREATER_THAN_OR_EQUAL_TO:
                        /* TODO */
                                    break;
                                case LESS_THAN:
                        /* TODO */
                                    break;
                                case LESS_THAN_OR_EQUAL_TO:
                        /* TODO */
                                    break;
                                case PRECISION_WITHIN:
                        /* TODO */
                                    break;
                                case PRECISION_AS_HIGH_AS:
                        /* TODO */
                                    break;
                                case PRECISION_AS_LOW_AS:
                        /* TODO */
                                    break;
                            }
                    /* TODO Log result */
                            // if (result) {
                            // LOG.info(vTag + ": " + vi.getStatusCode() + " - PASSED!");
                            // } else {
                            // LOG.error(vTag + ": " + vi.getStatusCode() + " - FAILED! Actual result: "
                            // + tj.getResponse().getStatusCode());
                            // }
                            break;

                        case VALIDATE_STATUS:
                            switch (vi.getOperator()) {
                                default:
                                    break;
                                case SCREEN_ON:
                        /* TODO */
                                    break;
                                case SCREEN_OFF:
                        /* TODO */
                                    break;
                                case WIFI_ON:
                        /* TODO */
                                    break;
                                case WIFI_OFF:
                        /* TODO */
                                    break;
                                case AIRPLANE_ON:
                        /* TODO */
                                    break;
                                case AIRPLANE_OFF:
                        /* TODO */
                                    break;
                                case TCPDUMP_ON:
                        /* TODO */
                                    break;
                                case TCPDUMP_OFF:
                        /* TODO */
                                    break;
                            }
                    /* TODO Log result */
                            // if (result) {
                            // LOG.info(vTag + ": " + vi.getStatusCode() + " - PASSED!");
                            // } else {
                            // LOG.error(vTag + ": " + vi.getStatusCode() + " - FAILED! Actual result: "
                            // + tj.getResponse().getStatusCode());
                            // }
                            break;
                    }
                }

                // TODO Need to move test suite stop some other place
                // Stop test suite
                TestJobStates.setJobState(tj.getTestSuiteName(), TestJobState.NOT_RUNNING);
                TestFactory.removeTestJobs(tj.getTestSuiteName());

                break;
        }

        return result;
    }
}
