package com.seven.asimov.it.tests.connectivity.occa;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.testcases.OCCATestCase;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.httpsTasks.FCIMessageTask;
import com.seven.asimov.it.utils.pms.Policy;


public class OCCATests extends OCCATestCase {
    private static final String TAG = OCCATests.class.getSimpleName();
    String policyPNName = "private_networks";
    String policyPNPath = "@asimov@pn";
    String policyWhiteListName = "enabled";
    String policyWhiteListPath = "@asimov@application@com.seven.asimov.it@ssl";

    /*
    <p>Pre-requests:</p>
    <ol>
    <li> Install OC. </li>
    <li> Push one apk-file on the sdcard with name: asimov-signed.apk (application, that installed on device) </li>
    </ol>
    */

    /**
     * <p>Summary: OC shouldnt optimize traffic for blacklisted applications</p>
     * <p>FCL task for first request should receive verdict FCN. Then traffic to this host should go in stream.</p>
     */

    public void test_001_OCCA() throws Exception {
        Policy[] policiesToAdd = new Policy[]{new Policy(policyWhiteListName, "0", policyWhiteListPath, true)};
        checkStreamMode(policiesToAdd);
    }

    /**
     * <p>Summary: OC should not optimize traffic for private IP</p>
     * <p>FCL task for first request should receive verdict FCN. Then traffic to this host should go in stream.</p>
     */

    public void test_002_OCCA() throws Exception {
        Policy[] policiesToAdd = new Policy[]{new Policy(policyPNName, "192.130.77.186:", policyPNPath, true), new Policy(policyWhiteListName, "0", policyWhiteListPath, true)};
        checkStreamMode(policiesToAdd);
    }

    /**
     * <p>Summary: FCL task should not receive verdict FCN when FC was already generated for current resource</p>
     * <p>Expected results</p>
     * <ol>
     * <li>Only first FCL task should receive verdict FCN.
     * <li>FC should be generated(CCR 0 ? signed by root certificate) and stored.
     * <li>FCL tasks for 2 and 3 request should receive verdict FCP
     * </ol>
     */

    public void test_003_OCCA() throws Exception {
        checkFCLVerdict("https://github.com", 0);
    }

    /**
     * <p>Summary: FCL task should not receive verdict FCN when FC was
     * already generated for current resource (untrusted remote certificate)</p>
     * <p>Expected results</p>
     * <ol>
     * <li>Only first FCL task should receive verdict FCN.</li>
     * <li>FC should be generated(CCR 1 ? signed by fake root certificate) and stored.</li>
     * <li>2nd and 3rd  FCL tasks should receive verdict FCP.</li>
     * </ol>
     */

    public void test_004_OCCA() throws Exception {
        checkFCLVerdict(createTestResourceUri("test_004_OCCA", true), 1);

    }

    /**
     * <p>Summary: OC behavior in case of simultaneous processing of FCL for the same resource</p>
     * <p>Expected results</p>
     * <ol>
     * <li>First FCL task should receive verdict FCN. </li>
     * <li>Verdict for second FCL task should be postponed till CCV for first request.</li>
     * <li>Second FCL task should receive verdict FCP after success FC generation.</li>
     * </ol>
     */

    public void test_005_OCCA() throws Exception {
        checkPostponing("https://developers.facebook.com");
    }

    /**
     * <p>OC behavior in case of simultaneous processing of FCL for the same resource (untrusted remote certificate)</p>
     * <p>Expected results</p>
     * <ol>
     * <li>First FCL task should receive verdict FCN. </li>
     * <li>Verdict for second FCL task should be postponed till CCV for first request.</li>
     * <li>Second FCL task should receive verdict FCP after success FC generation.</li>
     * </ol>
     */

    public void test_006_OCCA() throws Exception {
        AsimovTestCase.TEST_RESOURCE_HOST = "hki-qa-testrunner1.7sys.eu";
        checkPostponing(createTestResourceUri("test_006_OCCA", true));
    }

    /**
     * <p>OC behavior in case of expired remote certificate</p>
     * <p>Expected results</p>
     * <ol>
     * <li> First FCL task should receive verdict FCN.</li>
     * <li> FC should be stored.</li>
     * <li> Expired FC should be deleted.</li>
     * <li> Second FCL task should receive verdict FCN.</li>
     * </ol>
     */

    public void test_007_OCCA() throws Exception {
        checkExpiration("https://github.com");
    }

    /**
     * <p>OC behavior in case of expiration of FC (untrusted remote certificate)</p>
     * <p>Expected results</p>
     * <ol>
     * <li> First FCL task should receive verdict FCN.</li>
     * <li> FC should be stored.</li>
     * <li> Expired FC should be deleted.</li>
     * <li> Second FCL task should receive verdict FCN.</li>
     * </ol>
     */

    public void test_008_OCCA() throws Exception {
        checkExpiration(createTestResourceUri("test_008_OCCA", true));
    }

    /**
     * <p>Summary: OC CA should successfully generated and stored in case of rooted device</p>
     * <p>Expected results</p>
     * <ol>
     * <li> After installing of OC FCI message should be seen in logcat. OC CA should be stored in TrustStore. </li>
     * </ol>
     */

    public void test_009_OCCA() throws Exception {
        FCIMessageTask fciMessageTask = new FCIMessageTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(), fciMessageTask);
        logcatUtil.start();
        installOC();
        logcatUtil.stop();
        assertTrue("Occ should receive FCI message from OCEngine", fciMessageTask.getLogEntries().size()!=0);
    }

}
