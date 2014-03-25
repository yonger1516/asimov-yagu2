package com.seven.asimov.it.tests.generic.application.handler.keepalive;


import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.testcases.KeepAliveConditionTestCase;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * NetworkType condition tests
 * ({ com.seven.asimov.it.complex.functional.conditions.tests.KeepaliveApplicationStateConditionTests#testKA_detectionCondition_001()  Test1)
 * ({ com.seven.asimov.it.complex.functional.conditions.tests.KeepaliveApplicationStateConditionTests#testKA_detectionCondition_002()  Test2)
 * ({ com.seven.asimov.it.complex.functional.conditions.tests.KeepaliveApplicationStateConditionTests#testKA_detectionCondition_003()  Test3)
 * ({ com.seven.asimov.it.complex.functional.conditions.tests.KeepaliveApplicationStateConditionTests#testKA_detectionCondition_004()  Test4)
 * IGNORED due to need verification
 * ASMV-22669
 */
@Ignore
public class KeepaliveApplicationStateConditionTests extends KeepAliveConditionTestCase {
    private static final Logger logger = LoggerFactory.getLogger(KeepaliveApplicationStateConditionTests.class.getSimpleName());

    /**
     * <p/>
     * Steps:
     * 1. Set such policies:
     * asimov@application@com.seven.asimov.it@scripts@script_keepalive@conditions@keepalive = OFF
     * asimov@application@com.seven.asimov.it@scripts@script_keepalive@keepalive = ON
     * The screen is OFF, radio down
     * <p/>
     * <p/>
     * <p> Expected results
     * 1. Policy should be received and applied
     * 2. Script state should be switched : Disabled -> Exited
     * 3. Engine should
     * - calculate filter id for current uid
     * - send FTM messages to dispatchers
     * 4. Realtime detection mechanism should be activated.
     * 5. Keepalive application state condition should be activated
     * 6. Engine should update App Profile with KA detection status.
     * <p/>
     *
     * @throws Throwable
     */
    public void test_001_KAdetectionCondition() throws Throwable {

        managedProperties.add(new Property("keepalive", "@asimov@application@com.seven.asimov.it@scripts@script_keepalive@conditions", "off", false));
        managedProperties.add(new Property("keepalive", "@asimov@application@com.seven.asimov.it@scripts@script_keepalive@exit_conditions", "on", true));

        keepaliveConditionTest(managedProperties, false, FIRST_START, Radio.RADIO_UP, Screen.SCREEN_OFF, 0, 0, false);
    }

    /**
     * <p/>
     * Detection of KA condition with value ?ON? and exit condition with value ?OFF?  in case basic detection of KA is not be done yet.
     * <p/>
     * <p/>
     * 1. Set such policies:
     * asimov@application@com.seven.asimov.it@scripts@script_keepalive@conditions@keepalive = ON
     * asimov@application@com.seven.asimov.it@scripts@script_keepalive@keepalive = OFF
     * <p/>
     * <p/>
     * 1. Policy should be received and applied
     * 2. Script state should be switched : Disabled -> Exited
     * 3. Realtime detection mechanism should be activated.
     * 4. Keepalive application state condition should be activated
     * 5. Engine should update App Profile with KA detection status.
     * <p/>
     *
     * @throws Throwable
     */
    public void test_002_KAdetectionCondition() throws Throwable {

        managedProperties.add(new Property("keepalive", "@asimov@application@com.seven.asimov.it@scripts@script_keepalive@conditions", "on"));
        managedProperties.add(new Property("keepalive", "@asimov@application@com.seven.asimov.it@scripts@script_keepalive@exit_conditions", "off"));
        keepaliveConditionTest(managedProperties, false, false, Radio.RADIO_UP, Screen.SCREEN_OFF, 0, 0, false);
    }

    /**
     * <p/>
     * Detection of KA condition with value ?ON? and exit condition with value ?OFF?  in case basic detection of KA is already done.
     * <p/>
     * <p/>
     * 2. Set such policies:
     * asimov@application@com.seven.asimov.it@scripts@script_keepalive@conditions@keepalive = ON
     * asimov@application@com.seven.asimov.it@scripts@script_keepalive@keepalive = OFF
     * <p/>
     * <p/>
     * 1. Policy should be received and applied
     * 2. Script state should be switched : Disabled -> Exited
     * 3. Realtime detection mechanism should be activated.
     * 4. Keepalive application state condition should be activated
     * 5. Engine should update App Profile with KA detection status.<p/>
     */
    public void test_003_KAdetectionCondition() throws Throwable {
        managedProperties.add(new Property("keepalive", "@asimov@application@com.seven.asimov.it@scripts@script_keepalive@conditions", "on"));
        managedProperties.add(new Property("keepalive", "@asimov@application@com.seven.asimov.it@scripts@script_keepalive@exit_conditions", "off"));
        keepaliveConditionTest(managedProperties, false, false, Radio.RADIO_UP, Screen.SCREEN_OFF, 0, 2, true);
    }

    /**
     * <p/>
     * Detection of KA condition with value ?OFF? and exit condition with value ?ON?  in case basic detection of KA is already done
     * <p/>
     * <p/>
     * 1. Send several big messages
     * 2. Set such policies:
     * asimov@application@com.seven.asimov.it@scripts@script_keepalive@conditions@keepalive = ON
     * asimov@application@com.seven.asimov.it@scripts@script_keepalive@keepalive = OFF
     * 3. Wait for a while
     * 4. Send some message
     * 5. Observe client logs
     * <p/>
     * <p/>
     * 1. Policy should be received and applied
     * 2. Script state should be switched : Disabled -> Exited
     * 3. Realtime detection mechanism should be activated.
     * 4. Keepalive application state condition should be activated
     * 5. Engine should update App Profile with KA detection status.<p/>
     */
    public void test_004_KAdetectionCondition() throws Throwable {
        tryToConnect(2, true);
        managedProperties.add(new Property("keepalive", "@asimov@application@com.seven.asimov.it@scripts@script_keepalive@conditions", "on"));
        managedProperties.add(new Property("keepalive", "@asimov@application@com.seven.asimov.it@scripts@script_keepalive@exit_conditions", "off"));
        keepaliveConditionTest(managedProperties, false, false, Radio.RADIO_UP, Screen.SCREEN_OFF, 0, 1, true);
    }

    @Override
    protected void runTest() throws Throwable {
        boolean isPassed;
        int numberOfAttempts = 0;
        List<String> counts = new ArrayList<String>();
        do {
            isPassed = true;
            numberOfAttempts++;
            try {
                super.runTest();
            } catch (AssertionFailedError assertionFailedError) {
                logger.debug("Test failed due to " + ExceptionUtils.getStackTrace(assertionFailedError));
                isPassed = false;
                counts.add("Test failed due to AssertionFailedError in " + numberOfAttempts + " attempt");
                Thread.sleep(2 * 60 * 1000);
            }
        } while (!isPassed && numberOfAttempts < 3);
        assertTrue("The test was failed three times ", counts.size() != 3);
    }
}

