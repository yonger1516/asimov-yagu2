package com.seven.asimov.it.tests.dispatchers.bypath;

import com.seven.asimov.it.base.CustomService;
import com.seven.asimov.it.testcases.BypassPortTestCase;
import com.seven.asimov.it.utils.IpTablesUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;

public class BypassPortTests extends BypassPortTestCase {

        /**
         * Preparing directory to operate with configuration
         *
         * @throws Throwable
         */
        public void test_000_BYPATH() throws Throwable {
            IpTablesUtil.execute(new String[]{"su", "-c", "chmod -R 777 /data/"});
        }

        /**
         * Steps
         * 1.Configure PMS rule:
         * asimov@interception@bypass_list: com.seven.asimov.it;80
         * 2. Send 1 HTTP and 1 HTTPS requests
         * 3. Verify that HTTPS request optimize by OC and HTTP request bypath OC
         *
         * @throws Throwable
         */
        public void test_001_BYPATH() throws Throwable {

            String value = "com.seven.asimov.it;80";

            boolean[] checks = {true, false, true, true};

            String httpUri = createTestResourceUri("test_001_bypath_http", false);
            String httpsUri = createTestResourceUri("test_001_bypath_https", true);

            PrepareResourceUtil.prepareResource(httpUri, false);
            PrepareResourceUtil.prepareResource(httpsUri, false);
            requests.clear();
            requests.put(80, createRequest().setUri(httpUri).getRequest());
            requests.put(443, createRequest().setUri(httpsUri).getRequest());

            bypathPortTest(requests, value, 0, 1, 0, checks);

            PrepareResourceUtil.invalidateResourceSafely(httpUri);
            PrepareResourceUtil.invalidateResourceSafely(httpsUri);
            requests.clear();
        }

        /**
         * Steps
         * 1.Configure PMS rule:
         * asimov@interception@bypass_list: com.seven.asimov.it;80,443
         * 2. Send 1 HTTP and 1 HTTPS requests
         * 3. Verify that both requests bypath OC
         *
         * @throws Throwable
         */
        public void test_002_BYPATH() throws Throwable {

            String value = "com.seven.asimov.it;80,443";

            boolean[] checks = {true, false, true, true};

            String httpUri =  createTestResourceUri("test_002_bypath_http", false);
            String httpsUri = createTestResourceUri("test_002_bypath_https", true);

            PrepareResourceUtil.prepareResource(httpUri, false);
            PrepareResourceUtil.prepareResource(httpsUri, false);

            requests.put(80, createRequest().setUri(httpUri).getRequest());
            requests.put(443, createRequest().setUri(httpsUri).getRequest());

            bypathPortTest(requests, value, 0, 0, 0, checks);

            PrepareResourceUtil.invalidateResourceSafely(httpUri);
            PrepareResourceUtil.invalidateResourceSafely(httpsUri);
            requests.clear();
            services.clear();
        }

        /**
         * Steps:
         * 1. Prepare custom 5 ports on testrunner
         * 2. Include 4 of them to policy asimov@interception@bypass_list: com.seven.asimov.it;*ports*
         * 3. Add policy to server and send 5 requests to testrunner for 5 ports
         * <p/>
         * Expected results:
         * 1. Policy should be received and applied
         * 2. 4 requests for ports which were included to policy value should bypath OC
         * 3. another one request should be optimized by OC
         *
         * @throws Throwable
         */
        public void test_003_BYPATH() throws Throwable {

            services = prepareCustomPortsForTest(services, 5);
            for (CustomService cs : services) {
                requests.put(cs.port, createRequest().setUri(createTestResourceUri("test_003_bypath_http_" + cs.port, true, cs.port)).addHeaderField("X-OC-Reserve-Custom-Service", cs.session).getRequest());
            }

            boolean[] checks = {true, false, true, true};
            bypathPortTest(requests, createPolicyValue(services), 0, 0, 1, checks);

            prepareCustomPortsForTest(services, true);
            services.clear();
            requests.clear();
        }

        /**
         * Steps:
         * 1. Add policy asimov@interception@bypass_list: com.seven.asimov.it;80
         * 2. Remove asimov@interception@bypass_list: com.seven.asimov.it;80
         * 3. Send http request to testrunner
         * <p/>
         * Expected results:
         * 1. Policy should be received and applied
         * 2. Policy should be removed sucessfully
         *
         * @throws Throwable
         */
        public void test_005_BYPATH() throws Throwable {
            String value = "com.seven.asimov.it;80";

            String httpUri = createTestResourceUri("test_005_bypath_http", false);

            PrepareResourceUtil.prepareResource(httpUri, false);

            boolean[] checks = {true, true, false, false};

            requests.put(80, createRequest().setUri(httpUri).getRequest());

            bypathPortTest(requests, value, 1, 0, 0, checks);

            PrepareResourceUtil.invalidateResourceSafely(httpUri);
            services.clear();
            requests.clear();
        }

        /**
         * Steps:
         * 1. Add policy  asimov@interception@bypass_list: com.seven.asimov.it;11400:11500
         * 2. Check that policy was added and applied and remove it
         * 3. Prepare custom 5 ports on testrunner
         * 4. Include 4 of them to policy asimov@interception@bypass_list: com.seven.asimov.it;*ports*
         * 5. Add policy to server and send 5 requests to testrunner for 5 ports
         * <p/>
         * Expected results:
         * 1. Policy should be received and applied
         * 2. 4 requests for ports which were included to policy value should bypath OC
         * 3. another one request should be optimized by OC
         *
         * @throws Throwable
         */
        public void test_007_BYPATH() throws Throwable {

            assertTrue("Pre-requisites were failed", setupInitialValue());

            services = prepareCustomPortsForTest(services, 5);
            for (CustomService cs : services) {
                requests.put(cs.port, createRequest().setUri(createTestResourceUri("test_007_bypath_http_" + cs.port, true, cs.port)).addHeaderField("X-OC-Reserve-Custom-Service", cs.session).getRequest());
            }

            boolean[] checks = {true, false, true, true};
            bypathPortTest(requests, createPolicyValue(services), 0, 0, 1, checks);

            prepareCustomPortsForTest(services, true);
            services.clear();
            requests.clear();
        }

        /**
         * Steps:
         * 1. Configure PMS rule: asimov@interception@bypass_list:  com.seven.asimov.it;68000
         * <p/>
         * Expected results:
         * 1. Policy should be received but not applied
         * 2. The rule for the app shouldn't be added into ZBASECHAINE-PRIOR section of mangle table both for ipv4 and ipv6 iptables
         * 3. The rule shouldn't be observed in bypath list in /data/misc/openchannel/dispatchers.cfg
         *
         * @throws Throwable
         */
        public void test_008_BYPATH() throws Throwable {
            String value = "com.seven.asimov.it;68000";

            boolean[] checks = {true, false, false, false};

            bypathPortTest(null, value, 0, 0, 0, checks);
        }

        /**
         * Steps:
         * 1. Configure PMS rule: asimov@interception@bypass_list:  com.seven.asimov.it;*
         * <p/>
         * Expected results:
         * 1. Policy should be received but not applied
         * 2. The rule for the app shouldn't be added into ZBASECHAINE-PRIOR section of mangle table both for ipv4 and ipv6 iptables
         * 3. The rule shouldn't be observed in bypath list in /data/misc/openchannel/dispatchers.cfg
         */
        public void test_009_BYPATH() throws Throwable {
            String value = "com.seven.asimov.it;*";

            boolean[] checks = {true, false, false, false};

            bypathPortTest(null, value, 0 , 0, 0, checks);
        }
}
