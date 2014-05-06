/*
package com.seven.asimov.it.tests.crcs.netlog;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.testcases.NetlogHttpsTestCase;

*/
/**
 * <h2>This class tests OCC's Netlog functionality for http requests.</h2>
 * ASMV-21417 Fix Netlog suite
 *//*

public class NetlogHttpsTests extends NetlogHttpsTestCase {

    public enum CipherSuite {
        SSL_RSA_WITH_RC4_128_MD5,
        SSL_RSA_WITH_RC4_128_SHA,
        TLS_RSA_WITH_AES_128_CBC_SHA,
        TLS_RSA_WITH_AES_256_CBC_SHA,
        TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
        TLS_DHE_RSA_WITH_AES_256_CBC_SHA,
        TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
        TLS_DHE_DSS_WITH_AES_256_CBC_SHA,
        SSL_RSA_WITH_3DES_EDE_CBC_SHA,
        SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA,
        SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA,
        SSL_RSA_WITH_DES_CBC_SHA,
        SSL_DHE_RSA_WITH_DES_CBC_SHA,
        SSL_DHE_DSS_WITH_DES_CBC_SHA,
        SSL_RSA_EXPORT_WITH_RC4_40_MD5,
        SSL_RSA_EXPORT_WITH_DES40_CBC_SHA,
        SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA,
        SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA,
        SSL_RSA_WITH_NULL_MD5,
        SSL_RSA_WITH_NULL_SHA,
        SSL_DH_anon_WITH_RC4_128_MD5,
        TLS_DH_anon_WITH_AES_128_CBC_SHA,
        TLS_DH_anon_WITH_AES_256_CBC_SHA,
        SSL_DH_anon_WITH_3DES_EDE_CBC_SHA,
        SSL_DH_anon_WITH_DES_CBC_SHA,
        SSL_DH_anon_EXPORT_WITH_RC4_40_MD5,
        SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA

//        Currently working
//        SSL_RSA_WITH_RC4_128_MD5,
//        SSL_RSA_WITH_RC4_128_SHA,
//        TLS_RSA_WITH_AES_128_CBC_SHA,
//        SSL_RSA_WITH_3DES_EDE_CBC_SHA,
//        SSL_RSA_WITH_DES_CBC_SHA,
//        TLS_RSA_WITH_AES_256_CBC_SHA
    }


    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_RSA_WITH_RC4_128_MD5 </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     *//*

    @LargeTest
    public void test_001_Netlog_SSL_RSA_WITH_RC4_128_MD5() throws Exception {
        testNetlogHttpsSuite(CipherSuite.SSL_RSA_WITH_RC4_128_MD5);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_RSA_WITH_RC4_128_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     *//*

    @LargeTest
    public void test_002_Netlog_SSL_RSA_WITH_RC4_128_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_RSA_WITH_RC4_128_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using TLS_RSA_WITH_AES_128_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     *//*

    @LargeTest
    public void test_003_Netlog_TLS_RSA_WITH_AES_128_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using TLS_RSA_WITH_AES_256_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     *//*

    @LargeTest
    public void test_004_Netlog_TLS_RSA_WITH_AES_256_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using TLS_DHE_RSA_WITH_AES_128_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_005_Netlog_TLS_DHE_RSA_WITH_AES_128_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using TLS_DHE_RSA_WITH_AES_256_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_006_Netlog_TLS_DHE_RSA_WITH_AES_256_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using TLS_DHE_DSS_WITH_AES_128_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_007_Netlog_TLS_DHE_DSS_WITH_AES_128_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using TLS_DHE_DSS_WITH_AES_256_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_008_Netlog_TLS_DHE_DSS_WITH_AES_256_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_RSA_WITH_3DES_EDE_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     *//*

    @LargeTest
    public void test_009_Netlog_SSL_RSA_WITH_3DES_EDE_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_RSA_WITH_3DES_EDE_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_010_Netlog_SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_011_Netlog_SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_RSA_WITH_DES_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     *//*

    @LargeTest
    public void test_012_Netlog_SSL_RSA_WITH_DES_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_RSA_WITH_DES_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_DHE_RSA_WITH_DES_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_013_Netlog_SSL_DHE_RSA_WITH_DES_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_DHE_RSA_WITH_DES_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_DHE_DSS_WITH_DES_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_014_Netlog_SSL_DHE_DSS_WITH_DES_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_DHE_DSS_WITH_DES_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_RSA_EXPORT_WITH_RC4_40_MD5 </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_015_Netlog_SSL_RSA_EXPORT_WITH_RC4_40_MD5() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_RSA_EXPORT_WITH_RC4_40_MD5);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_RSA_EXPORT_WITH_DES40_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_016_Netlog_SSL_RSA_EXPORT_WITH_DES40_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_RSA_EXPORT_WITH_DES40_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_017_Netlog_SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_018_Netlog_SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_019_Netlog_SSL_RSA_WITH_NULL_MD5() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_RSA_WITH_NULL_MD5);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_RSA_WITH_NULL_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_020_Netlog_SSL_RSA_WITH_NULL_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_RSA_WITH_NULL_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_DH_anon_WITH_RC4_128_MD5 </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_021_Netlog_SSL_DH_anon_WITH_RC4_128_MD5() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_DH_anon_WITH_RC4_128_MD5);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using TLS_DH_anon_WITH_AES_128_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_022_Netlog_TLS_DH_anon_WITH_AES_128_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.TLS_DH_anon_WITH_AES_128_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using TLS_DH_anon_WITH_AES_256_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_023_Netlog_TLS_DH_anon_WITH_AES_256_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.TLS_DH_anon_WITH_AES_256_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_DH_anon_WITH_3DES_EDE_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_024_Netlog_SSL_DH_anon_WITH_3DES_EDE_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_DH_anon_WITH_3DES_EDE_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_DH_anon_WITH_DES_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_025_Netlog_SSL_DH_anon_WITH_DES_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_DH_anon_WITH_DES_CBC_SHA);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_DH_anon_EXPORT_WITH_RC4_40_MD5 </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_026_Netlog_SSL_DH_anon_EXPORT_WITH_RC4_40_MD5() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_DH_anon_EXPORT_WITH_RC4_40_MD5);
    }

    */
/**
     * <h3>Verify OC works correctly and client_in, client_out, server_in, server_out.</h3>
     * actions:
     * <ol>
     *     <li>send 1 request to testrunner by connection with socket using SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA </li>
     * </ol>
     * checks:
     * <ol>
     *     <li>check that netlog data for client_in, client_out, server_in, server_out correspond to tcpdump data</li>
     * </ol>
     * @throws Exception
     * This cipher suite is not supported.
     *//*

    //@Execute
    @Ignore
    @LargeTest
    public void test_027_Netlog_SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA() throws Throwable {
        testNetlogHttpsSuite(CipherSuite.SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA);
    }
}
*/
