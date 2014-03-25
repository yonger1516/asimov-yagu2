package com.seven.asimov.it.tests.caching.heuristic;

import com.seven.asimov.it.testcases.HeuristicCachingTestCase;
import org.junit.Ignore;

public class HeuristicPatternMatcherTests extends HeuristicCachingTestCase {

    /**
     * <h1>Summary: OC should start polling after heuristic pattern detection</h1>
     * <p>Steps:</p>
     * <p>Create  test suites with 4 different requests and with the same responses:</p>
     * <ol>
     * <li>First request: http, GET, to host1, URL should differ only by such parameters param1= 123, param2=345</li>
     * <li>Second request: http, GET, to host1, URL should differ only by such parameters param1=567, param2=789</li>
     * </ol>
     * <p>Expected result:</p>
     * <ol>
     * <li>RR should be constructed after 2nd response</li>
     * <li>RMP should be detected after 3rd request and polling should start after receiving response.</li>
     * <li>4th request should be HITed.</li>
     * </ol>
     * <p/>
     * TestLab: HP_001
     *
     * IGNORED due to ASMV-21463
     * @throws Exception
     */
    @Ignore
    public void test_001_HeuristicPatternDetection() throws Exception {
        heuristicPatternMatcher("test_heuristic_pattern_detection", true, true, true, true, responseDelay, sleepTime);
    }

    /**
     * <h1>Summary: OC should not detect pattern heuristically in case different request owner </h1>
     * <p>Steps:</p>
     * <p>Create  test suites with 4 different requests and with the same responses:</p>
     * <ol>
     * <li>First request: http, GET, to host1, URL should differ only by such parameters param1= 123, param2=345</li>
     * <li>Second request: https, GET, to host1, URL should differ only by such parameters param1=567, param2=789</li>
     * </ol>
     * <p>Expected result:</p>
     * <ol>
     * <li>RR should not be constructed after 2nd response</li>
     * <li>RMP should not be detected after 3rd request and polling should not start after receiving response.</li>
     * <li>4th request should be MISSed.</li>
     * </ol>
     * <p/>
     * TestLab: HP_002
     *
     * @throws Exception
     */
    public void test_002_DifferentRequestOwner() throws Exception {
        heuristicPatternMatcher("test_different_request_owner", false, true, true, false, responseDelay, sleepTime);
    }

    /**
     * <h1>Summary: OC should not detect pattern heuristically in case different request method</h1>
     * <p>Steps:</p>
     * <p>Create  test suites with 4 different requests and with the same responses:</p>
     * <ol>
     * <li>First request: http, GET, to host1, URL should differ only by such parameters param1= 123, param2=345</li>
     * <li>Second request: http, POST, to host1, URL should differ only by such parameters param1=567, param2=789</li>
     * </ol>
     * <p>Expected result:</p>
     * <ol>
     * <li>RR should not be constructed after 2nd response</li>
     * <li>RMP should not be detected after 3rd request and polling should not start after receiving response.</li>
     * <li>4th request should be MISSed.</li>
     * </ol>
     * <p/>
     * TestLab: HP_003
     *
     * @throws Exception
     */
    public void test_003_DifferentRequestMethod() throws Exception {
        heuristicPatternMatcher("test_different_request_method", true, false, true, false, responseDelay, sleepTime);
    }

    /**
     * <h1>Summary: OC should not detect pattern heuristically in case different request host</h1>
     * <p>Steps:</p>
     * <p>Create  test suites with 4 different requests and with the same responses:</p>
     * <ol>
     * <li>First request: http, GET, to host1, URL should differ only by such parameters param1= 123, param2=345</li>
     * <li>Second request: http, GET, to host2, URL should differ only by such parameters param1=567, param2=789</li>
     * </ol>
     * <p>Expected result:</p>
     * <ol>
     * <li>RR should not be constructed after 2nd response</li>
     * <li>RMP should not be detected after 3rd request and polling should not start after receiving response.</li>
     * <li>4th request should be MISSed.</li>
     * </ol>
     * <p/>
     * TestLab: HP_004
     *
     * @throws Exception
     */
    public void test_004_DifferentRequestHost() throws Exception {
        heuristicPatternMatcher("test_different_request_host", true, true, false, false, responseDelay, sleepTime);
    }
}
