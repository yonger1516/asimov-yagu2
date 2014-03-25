package com.seven.asimov.it.tests.connectivity.ciphersuites;

import com.seven.asimov.it.annotation.Execute;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.testcases.ConnectivityCipherSuiteTestCase;

public class ZHttpsCipherSuiteTests extends ConnectivityCipherSuiteTestCase {

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
        SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA,
        TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA,
        TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
        TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
        TLS_ECDHE_ECDSA_WITH_NULL_SHA,
        TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
        TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA,
        TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
        TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
        TLS_ECDHE_RSA_WITH_NULL_SHA,
        TLS_ECDHE_RSA_WITH_RC4_128_SHA,
        TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA,
        TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA,
        TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA,
        TLS_ECDH_ECDSA_WITH_NULL_SHA,
        TLS_ECDH_ECDSA_WITH_RC4_128_SHA,
        TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA,
        TLS_ECDH_RSA_WITH_AES_128_CBC_SHA,
        TLS_ECDH_RSA_WITH_AES_256_CBC_SHA,
        TLS_ECDH_RSA_WITH_NULL_SHA,
        TLS_ECDH_RSA_WITH_RC4_128_SHA,
        TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA,
        TLS_ECDH_anon_WITH_AES_128_CBC_SHA,
        TLS_ECDH_anon_WITH_AES_256_CBC_SHA,
        TLS_ECDH_anon_WITH_NULL_SHA,
        TLS_ECDH_anon_WITH_RC4_128_SHA
    }

    /**
     * /**
     * <p>Verify, that OC establish secure connection with SSL_RSA_WITH_RC4_128_MD5 Cipher suite
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Create socket
     * 2. Set cipher suite - SSL_RSA_WITH_RC4_128_MD5
     * 3. Send https request to test runner
     * 4. Get response from testRunner
     * 5.Check response body
     * </p>
     * <p>Expected reults:
     * 1. Server authentication was passed
     * 2. Key's exchange was passed
     * 3. Cipher suite was applied
     * </p>
     *
     * @throws Throwable
     */
    @Execute
    public void test_001_SSL_RSA_WITH_RC4_128_MD5() throws Throwable {
        testZHttpsCipherSuite(CipherSuite.SSL_RSA_WITH_RC4_128_MD5);
    }

    /**
     * /**
     * <p>Verify, that OC establish secure connection with SSL_RSA_WITH_RC4_128_SHA Cipher suite
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Create socket
     * 2. Set cipher suite - SSL_RSA_WITH_RC4_128_SHA
     * 3. Send https request to test runner
     * 4. Get response from testRunner
     * 5.Check response body
     * </p>
     * <p>Expected reults:
     * 1. Server authentication was passed
     * 2. Key's exchange was passed
     * 3. Cipher suite was applied
     * </p>
     *
     * @throws Throwable
     */
    @Execute
    public void test_002_SSL_RSA_WITH_RC4_128_SHA() throws Throwable {
        testZHttpsCipherSuite(CipherSuite.SSL_RSA_WITH_RC4_128_SHA);
    }

    /**
     * /**
     * <p>Verify, that OC establish secure connection with TLS_RSA_WITH_AES_128_CBC_SHA Cipher suite
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Create socket
     * 2. Set cipher suite - TLS_RSA_WITH_AES_128_CBC_SHA
     * 3. Send https request to test runner
     * 4. Get response from testRunner
     * 5.Check response body
     * </p>
     * <p>Expected reults:
     * 1. Server authentication was passed
     * 2. Key's exchange was passed
     * 3. Cipher suite was applied
     * </p>
     *
     * @throws Throwable
     */
    @Execute
    public void test_003_TLS_RSA_WITH_AES_128_CBC_SHA() throws Throwable {
        testZHttpsCipherSuite(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA);
    }

    /**
     * /**
     * <p>Verify, that OC establish secure connection with SSL_RSA_WITH_3DES_EDE_CBC_SHA Cipher suite
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Create socket
     * 2. Set cipher suite - SSL_RSA_WITH_3DES_EDE_CBC_SHA
     * 3. Send https request to test runner
     * 4. Get response from testRunner
     * 5.Check response body
     * </p>
     * <p>Expected reults:
     * 1. Server authentication was passed
     * 2. Key's exchange was passed
     * 3. Cipher suite was applied
     * </p>
     *
     * @throws Throwable
     */
    @Execute
    public void test_004_SSL_RSA_WITH_3DES_EDE_CBC_SHA() throws Throwable {
        testZHttpsCipherSuite(CipherSuite.SSL_RSA_WITH_3DES_EDE_CBC_SHA);
    }

    /**
     * /**
     * <p>Verify, that OC establish secure connection with SSL_RSA_WITH_DES_CBC_SHA Cipher suite
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Create socket
     * 2. Set cipher suite - SSL_RSA_WITH_DES_CBC_SHA
     * 3. Send https request to test runner
     * 4. Get response from testRunner
     * 5.Check response body
     * </p>
     * <p>Expected reults:
     * 1. Server authentication was passed
     * 2. Key's exchange was passed
     * 3. Cipher suite was applied
     * </p>
     *
     * @throws Throwable
     */
    @Execute
    public void test_006_SSL_RSA_WITH_DES_CBC_SHA() throws Throwable {
        testZHttpsCipherSuite(CipherSuite.SSL_RSA_WITH_DES_CBC_SHA);
    }

    /**
     * /**
     * <p>Verify, that OC establish secure connection with SSL_RSA_EXPORT_WITH_RC4_40_MD5 Cipher suite
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Create socket
     * 2. Set cipher suite - SSL_RSA_EXPORT_WITH_RC4_40_MD5
     * 3. Send https request to test runner
     * 4. Get response from testRunner
     * 5.Check response body
     * </p>
     * <p>Expected reults:
     * 1. Server authentication was passed
     * 2. Key's exchange was passed
     * 3. Cipher suite was applied
     * </p>
     *
     * @throws Throwable This cipher suite is not supported.
     */
    @Ignore
    //@Execute
    public void test_008_SSL_RSA_EXPORT_WITH_RC4_40_MD5() throws Throwable {
        testZHttpsCipherSuite(CipherSuite.SSL_RSA_EXPORT_WITH_RC4_40_MD5);
    }

    /**
     * /**
     * <p>Verify, that OC establish secure connection with SSL_RSA_EXPORT_WITH_DES40_CBC_SHA Cipher suite
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Create socket
     * 2. Set cipher suite - SSL_RSA_EXPORT_WITH_DES40_CBC_SHA
     * 3. Send https request to test runner
     * 4. Get response from testRunner
     * 5.Check response body
     * </p>
     * <p>Expected reults:
     * 1. Server authentication was passed
     * 2. Key's exchange was passed
     * 3. Cipher suite was applied
     * </p>
     *
     * @throws Throwable This cipher suite is not supported.
     */
    @Ignore
    //@Execute
    public void test_009_SSL_RSA_EXPORT_WITH_DES40_CBC_SHA() throws Throwable {
        testZHttpsCipherSuite(CipherSuite.SSL_RSA_EXPORT_WITH_DES40_CBC_SHA);
    }

    /**
     * /**
     * <p>Verify, that OC establish secure connection with TLS_RSA_WITH_AES_256_CBC_SHA Cipher suite
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Create socket
     * 2. Set cipher suite - TLS_RSA_WITH_AES_256_CBC_SHA
     * 3. Send https request to test runner
     * 4. Get response from testRunner
     * 5.Check response body
     * </p>
     * <p>Expected reults:
     * 1. Server authentication was passed
     * 2. Key's exchange was passed
     * 3. Cipher suite was applied
     * </p>
     *
     * @throws Throwable ASMV-21491 Investigate fails in Connectivity suite
     */
    @Execute
    public void test_011_TLS_RSA_WITH_AES_256_CBC_SHA() throws Throwable {
        testZHttpsCipherSuite(CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA);
    }

    /**
     * /**
     * <p>Verify, that OC establish secure connection with SSL_RSA_WITH_NULL_MD5 Cipher suite
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Create socket
     * 2. Set cipher suite - SSL_RSA_WITH_NULL_MD5
     * 3. Send https request to test runner
     * 4. Get response from testRunner
     * 5.Check response body
     * </p>
     * <p>Expected reults:
     * 1. Server authentication was passed
     * 2. Key's exchange was passed
     * 3. Cipher suite was applied
     * </p>
     *
     * @throws Throwable This cipher suite is not supported.
     */
    @Ignore
    //@Execute
    public void test_019_SSL_RSA_WITH_NULL_MD5() throws Throwable {
        testZHttpsCipherSuite(CipherSuite.SSL_RSA_WITH_NULL_MD5);
    }

    /**
     * /**
     * <p>Verify, that OC establish secure connection with SSL_RSA_WITH_NULL_SHA Cipher suite
     * </p>
     * <p>Pre-requisites:
     * 1. OC client is installed
     * </p>
     * <p>Steps:
     * 1. Create socket
     * 2. Set cipher suite - SSL_RSA_WITH_NULL_SHA
     * 3. Send https request to test runner
     * 4. Get response from testRunner
     * 5.Check response body
     * </p>
     * <p>Expected reults:
     * 1. Server authentication was passed
     * 2. Key's exchange was passed
     * 3. Cipher suite was applied
     * </p>
     *
     * @throws Throwable This cipher suite is not supported.
     */
    @Ignore
    //@Execute
    public void test_020_SSL_RSA_WITH_NULL_SHA() throws Throwable {
        testZHttpsCipherSuite(CipherSuite.SSL_RSA_WITH_NULL_SHA);
    }

}
