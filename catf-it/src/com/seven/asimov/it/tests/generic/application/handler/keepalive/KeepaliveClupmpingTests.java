package com.seven.asimov.it.tests.generic.application.handler.keepalive;


import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.testcases.KeepAliveConditionTestCase;

/**
 * NetworkType condition tests
 * <p/>
 * <p/>
 * IGNORED due to need verification
 * ASMV-22669
 */
@Ignore
public class KeepaliveClupmpingTests extends KeepAliveConditionTestCase {


    /**
     * Basic detection of KA
     * <p/>
     * 1. Start logcat
     * 2. Login into Skype
     * 3. Wait for a while
     * <p/>
     * 1. After each transaction in stream 2 TDR task should be constructed.
     * 2. Amount of data transferred should be calculated separately for both direction - upstream and downstream.
     * 3. Event history as part of application profile should become filling on each TDR message with such fields:
     * CSM id, TRX id, type of message processed should be TRD, delay in seconds for
     * the answer should be 0, bytes from\to, request time, KA state (If the packet?s size <=50 bytes
     * and CSM?s age > 100 seconds KA state should be "KA", if not - NOT KA"), KA weight (should be increased by
     * 1 for each KA candidate detected in a row, if the sequence breaks, ?weight KA? should be droped to 0).
     *
     * @throws Throwable
     */
    public void test_002_KAoptimization() throws Throwable {
        tryToConnect(4, true);
        keepaliveConditionTest(null, OPTIMIZATION, false, Radio.RADIO_DOWN, Screen.SCREEN_ON, MINUTE, 0, false);
    }

    /**
     * Basic detection of KA
     * <p/>
     * 1. Start logcat
     * 3. Set such policies: asimov@application@skype.com@keepalive_clumping@*@delay=10
     * 2. Login into Skype
     * 3. Wait for a while
     * 4. Observe client logs
     * <p/>
     * 1.1. Basic detection of KA should be done and realtime detection mechanism should be activated.
     * 2. Event history as part of application profile should become filling on each NAQ/NSQ message with such fields:
     * CSM id, TRX id, type of message processed should be TRD, delay in seconds for
     * the answer should be 0, bytes from\to, request time, KA state (If the packet?s size <=50 bytes
     * and CSM?s age > 100 seconds KA state should be "KA", if not - NOT KA"), KA weight (should be increased by
     * 1 for each KA candidate detected in a row, if the sequence breaks, ?weight KA? should be droped to 0).
     *
     * @throws Throwable
     */
    public void test_004_KAoptimization() throws Throwable {
        managedProperties.add(new Property("delay", "@asimov@application@com.seven.asimov.it@scripts@keepalive_clumping@*", "10"));
        keepaliveConditionTest(managedProperties, OPTIMIZATION, false, Radio.RADIO_DOWN, Screen.SCREEN_ON, MINUTE, 4, true);
    }

    /**
     * Real-time KA detection with present keep-alive clumping tolerance in case radio down on  NAQ and NSQ
     * <p/>
     * 1. Start logcat
     * 3. Set such policies: asimov@application@skype.com@keepalive_clumping@*@delay=10
     * 2. Login into Skype
     * 3. Wait for a while
     * 4. Observe client logs
     * <p/>
     * 1. Policy should be received and applied
     * 2. Basic detection of KA should be done and realtime detection mechanism should be activated.
     * 3. Event history as part of application profile should become filling.
     * 4. Delay in seconds for the answer for NAQ should be 10 and NSQ should be 0 for each KA candidate detected in a row.
     * 5. Engine should send NAR in 10 sec. Engine should send NSR immediate
     *
     * @throws Throwable
     */
    @Ignore //the same to previous
    public void test_005_KAoptimization() throws Throwable {
        tryToConnect(4, false);
//        keepaliveConditionTest(null, OPTIMIZATION, !FIRST_START, Radio.RADIO_DOWN, Screen.SCREEN_OFF, MINUTE);
    }

    /**
     * The use of keep-alive clumping tolerance in case Real-time KA detection was done in case radio down on NAQ and NSQ
     * <p/>
     * 1. Start logcat
     * 2. Login into Skype
     * 3. Wait for a while
     * 4. Set such policies: asimov@application@skype.com@keepalive_clumping@*@delay=10
     * 4. Observe client logs
     * <p/>
     * 1. Basic detection of KA should be done and realtime detection mechanism should be activated.
     * 2. Event history as part of application profile should become filling.
     * 3. Delay in seconds for the answer for NAQ and NSQ should be 0 for each KA candidate detected in a row.
     * 4. Engine should send NAR and NSR immediately
     * 5. Policy should be received and applied.
     * 6. Delay in seconds for the answer for NAQ should be 10 for each KA candidate detected in a row. Delay in seconds for the answer for NSQ should be 0 for each KA candidate detected in a row
     * 7. Engine should send NAR in 10 sec. Engine should send NSR immediately
     *
     * @throws Throwable
     */
    public void test_006_KAoptimization() throws Throwable {
        tryToConnect(2, false);
        managedProperties.add(new Property("delay", "@asimov@application@com.seven.asimov.it@scripts@keepalive_clumping@*", "10"));
        keepaliveConditionTest(managedProperties, OPTIMIZATION, false, Radio.RADIO_DOWN, Screen.SCREEN_ON, MINUTE, 0, false);
    }

    /**
     * Real-time KA detection with present keep-alive clumping tolerance in case radio radio state changes from down to up on  NAQ
     * <p/>
     * 1. Start logcat
     * 3. Set such policies: asimov@application@skype.com@keepalive_clumping@*@delay=10
     * 2. Login into Skype
     * 3. Wait for a while
     * 4. Observe client logs
     * <p/>
     * 1. Policy should be received and applied
     * 2. Basic detection of KA should be done and realtime detection mechanism should be activated.
     * 3. Event history as part of application profile should become filling.
     * 4. Delay in seconds for the answer for NAQ should be 10
     * 5. Engine should schedule NAR for 10 sec.
     * 6. Radio state should changes to UP.
     * 7. Engine should send NAR immediately after this.
     *
     * @throws Throwable
     */
    public void test_007_KAoptimization() throws Throwable {
        tryToConnect(3, false);
        managedProperties.add(new Property("delay", "@asimov@application@com.seven.asimov.it@scripts@keepalive_clumping@*", "10"));
        keepaliveConditionTest(managedProperties, OPTIMIZATION, false, Radio.RADIO_UP, Screen.SCREEN_ON, MINUTE, 0, false);
    }

    /**
     * Real-time KA detection with preset keep-alive clumping tolerance for background  in case radio down on NAQ and NSQ
     * <p/>
     * 1. Start logcat
     * 2. Set such policies: asimov@application@skype.com@keepalive_clumping@bg@delay=10
     * 3. Login into Skype
     * 4. Turn off the screen
     * 5. Wait for a while
     * 6. Observe client logs
     * <p/>
     * 1. Policy should be received and applied
     * 2. Basic detection of KA should be done and realtime detection mechanism should be activated.
     * 3. Event history as part of application profile should become filling.
     * 4. Delay in seconds for the answer for NAQ should be 10 and NSQ should be 0 for each KA candidate detected in a row.
     * 5. Engine should send NAR in 10 sec. Engine should send NSR immediately
     *
     * @throws Throwable
     */
    public void test_010_KAoptimization() throws Throwable {
        managedProperties.add(new Property("delay", "@asimov@application@com.seven.asimov.it@scripts@keepalive_clumping@bg", "10"));
        keepaliveConditionTest(managedProperties, OPTIMIZATION, false, Radio.RADIO_DOWN, Screen.SCREEN_OFF, 0, 0, false);
    }


    /**
     * Real-time KA detection with present keep-alive clumping tolerance for background  in case radio up on NAQ and NSQ
     * <p/>
     * 1. Start logcat
     * 2. Set such policies: asimov@application@skype.com@keepalive_clumping@bg@delay=10
     * 3. Login into Skype
     * 4. Turn off the screen
     * 5. Wait for a while
     * 6. Observe client logs
     * <p/>
     * 1. Policy should be received and applied
     * 2. Basic detection of KA should be done and realtime detection mechanism should be activated.
     * 3. Event history as part of application profile should become filling.
     * 4. Delay in seconds for the answer for NAQ and  NSQ should be 0 for each KA candidate detected in a row.
     * 5. Engine should send NAR and NSR immediately
     *
     * @throws Throwable
     */
    public void test_011_KAoptimization() throws Throwable {
        managedProperties.add(new Property("delay", "@asimov@application@com.seven.asimov.it@scripts@keepalive_clumping@bg", "10"));
        keepaliveConditionTest(managedProperties, OPTIMIZATION, false, Radio.RADIO_UP, Screen.SCREEN_OFF, 0, 0, false);
    }

    /**
     * Real-time KA detection with present keep-alive clumping tolerance in case detection of NOT KA
     * 1. Start logcat
     * 2. Set such policies: asimov@application@skype.com@keepalive_clumping@*@delay=10
     * 3. Login into Skype
     * 4. Wait for a while
     * 5. Observe client logs
     * <p/>
     * 1. Basic detection of KA should be done and realtime detection mechanism should be activated.
     * 2. Large packet (>50b) should be detected.
     * 3. Event history as part of application profile should be filled on NAQ message with such fields:
     * CSM id, TRX id, type of message processed should be NAQ, delay in seconds for the answer for NAQ should be 0, bytes from\to, request time, KA state should be NOT KA", KA weight should be droped to 0.
     * 4. Engine should update App Profile with KA detection status.
     * 5. Delay in seconds for the answer for NAQ and  NSQ should be 0 for NOT KA packet
     * 6. Engine should send NAR and NSR immediately
     *
     * @throws Throwable
     */
    public void test_012_KAoptimization() throws Throwable {
        managedProperties.add(new Property("delay", "@asimov@application@com.seven.asimov.it@scripts@keepalive_clumping@*", "10"));
        keepaliveConditionTest(managedProperties, OPTIMIZATION, false, Radio.RADIO_DOWN, Screen.SCREEN_ON, MIN_PERIOD, 2, false);
    }

    /**
     * Real-time KA detection with present keep-alive clumping tolerance in case detection of unused is 0 by default
     * <p/>
     * 1. Start logcat
     * 2. Set such policies: asimov@application@skype.com@keepalive_clumping@unused@delay=10
     * 3. Login into Skype
     * 4. Turn off the screen
     * 5. Wait for a while
     * 6. Observe client logs
     * <p/>
     * 1. Policy should be received and applied 3. Event history as part of application profile should become filling.
     * 2. Basic detection of KA should be done and realtime detection mechanism should be activated.
     * 3. Event history as part of application profile should become filling.
     * 4. Delay in seconds for the answer for NAQ should be 0 and NSQ should be 0 for each KA candidate detected in a row.
     * 5. Engine should send NAR and NSR immediately
     *
     * @throws Throwable
     */
    public void test_013_KAoptimization() throws Throwable {
        managedProperties.add(new Property("delay", "@asimov@application@com.seven.asimov.it@scripts@keepalive_clumping@unused", "10"));
        keepaliveConditionTest(managedProperties, OPTIMIZATION, false, Radio.RADIO_UP, Screen.SCREEN_OFF, 0, 0, false);
    }

    /**
     * Real-time KA detection with present keep-alive clumping tolerance in case detection of unused is 1
     * <p/>
     * 1. Start logcat
     * 2. Set such policies:
     * asimov@application@skype.com@days_for_unused_status=1
     * asimov@application@skype.com@keepalive_clumping@unused@delay=10
     * 3. Login into Skype
     * 4. Turn off the screen
     * 5. Wait for 1 day
     * <p/>
     * 1. Policy should be received and applied as keepalive clumping unused delay
     * 2. Basic detection of KA should be done and realtime detection mechanism should be activated.
     * 3. Event history as part of application profile should become filling.
     * 4. Engine should send NAR and NSR immediately
     * 5. After 1 day application should be considered as unused
     * 4. Delay in seconds for the answer for NAQ should be 10 and NSQ should be 0 for each KA candidate detected in a row.
     * 5. Engine should send NAR in 10 sec. Engine should send NSR immediately
     *
     * @throws Throwable
     */
    public void test_014_KAoptimization() throws Throwable {
        managedProperties.add(new Property("days_for_unused_status", "@asimov@application@com.seven.asimov.it", "1"));
        managedProperties.add(new Property("delay", "@asimov@application@com.seven.asimov.it@scripts@keepalive_clumping@bg", "10"));
        keepaliveConditionTest(managedProperties, OPTIMIZATION, false, Radio.RADIO_UP, Screen.SCREEN_OFF, DAY, 0, false);
    }

    /**
     * Real-time KA detection with present keep-alive clumping tolerance in case detection of unused is 1
     * <p/>
     * 1. Start logcat
     * 2. Set such policies:
     * asimov@application@skype.com@days_for_unused_status=1
     * asimov@application@skype.com@keepalive_clumping@unused@delay=10
     * 3. Login into Skype
     * 4. Turn off the screen
     * 5. Wait for 1 day
     * 6. After some time turn screen on
     * <p/>
     * 1. Policy should be received and applied as keepalive clumping unused delay
     * 2. Basic detection of KA should be done and realtime detection mechanism should be activated.
     * 3. Event history as part of application profile should become filling.
     * 4. Engine should send NAR and NSR immediately
     * 5. After 1 day application should be considered as unused
     * 4. Delay in seconds for the answer for NAQ should be 10 and NSQ should be 0 for each KA candidate detected in a row.
     * 5. Engine should send NAR in 10 sec. Engine should send NSR immediately
     *
     * @throws Throwable
     */
    public void test_015_KAoptimization() throws Throwable {
        managedProperties.add(new Property("days_for_unused_status", "@asimov@application@com.seven.asimov.it", "1"));
        managedProperties.add(new Property("delay", "@asimov@application@com.seven.asimov.it@scripts@keepalive_clumping@bg", "10"));
        keepaliveConditionTest(managedProperties, OPTIMIZATION, false, Radio.RADIO_UP, Screen.SCREEN_ON, DAY, 0, false);
    }
}
