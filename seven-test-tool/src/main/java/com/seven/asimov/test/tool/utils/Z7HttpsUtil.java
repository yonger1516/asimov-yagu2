package com.seven.asimov.test.tool.utils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * @author Dmitri Melnikov (dmelnikov@seven.com)
 */
public abstract class Z7HttpsUtil {
    private static final Logger LOG = LoggerFactory.getLogger(Z7HttpsUtil.class.getSimpleName());

    private static final String DEFAULT_SSL_CONTEXT_NAME = "Default";
    private static final String FALLBACK_SSL_CONTEXT_NAME = "TLS";
    private static final SSLContext DEFAULT_SSL_CONTEXT;

    static {
        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance(DEFAULT_SSL_CONTEXT_NAME);
        } catch (NoSuchAlgorithmException e) {
            LOG.warn("Could not initialize default SSL context (Android 2.2?), trying fallback", e);
            try {
                ctx = SSLContext.getInstance(FALLBACK_SSL_CONTEXT_NAME);
                ctx.init(null, null, null);
            } catch (NoSuchAlgorithmException ex) {
                LOG.error("Could not initialize fallback SSL context, SSL will not work", ex);
            } catch (KeyManagementException ex) {
                LOG.error("Could not initialize fallback SSL context, SSL will not work", ex);
            }
        } finally {
            DEFAULT_SSL_CONTEXT = ctx;
        }
    }

    private static final Map<String, String[]> SUPPORTED_CIPHER_CACHE = new HashMap<String, String[]>();
    private static final Map<String, String[]> DEFAULT_CIPHER_CACHE = new HashMap<String, String[]>();
    private static String[] sSupportedProtocolCache = null;

    public static synchronized String[] getSupportedCiphers(String... protocols) {
        if (DEFAULT_SSL_CONTEXT == null) {
            return new String[0];
        }

        Set<String> ciphers = new TreeSet<String>(); // TreeSet gives us free sorting
        for (String p : protocols) {
            if (!SUPPORTED_CIPHER_CACHE.containsKey(p)) {
                String[] sortedCiphers = DEFAULT_SSL_CONTEXT.getSocketFactory().getSupportedCipherSuites();
                Arrays.sort(sortedCiphers);
                SUPPORTED_CIPHER_CACHE.put(p, sortedCiphers);
                LOG.debug("Got list of supported ciphers: %s", Arrays.asList(sortedCiphers));
            }

            ciphers.addAll(Arrays.asList(SUPPORTED_CIPHER_CACHE.get(p)));
        }

        return ciphers.toArray(new String[0]);
    }

    public static synchronized String[] getDefaultCiphers(String protocol) {
        if (DEFAULT_SSL_CONTEXT == null) {
            return new String[0];
        }

        if (!DEFAULT_CIPHER_CACHE.containsKey(protocol)) {
            String[] sortedCiphers = DEFAULT_SSL_CONTEXT.getSocketFactory().getDefaultCipherSuites();
            Arrays.sort(sortedCiphers);
            DEFAULT_CIPHER_CACHE.put(protocol, sortedCiphers);
            LOG.debug("Got list of default ciphers: %s", Arrays.asList(sortedCiphers));
        }

        return DEFAULT_CIPHER_CACHE.get(protocol);
    }

    public static synchronized String[] getSupportedProtocols() {
        if (DEFAULT_SSL_CONTEXT == null) {
            return new String[0];
        }

        if (sSupportedProtocolCache == null) {
            String[] sortedProtocols = DEFAULT_SSL_CONTEXT.createSSLEngine().getSupportedProtocols();
            Arrays.sort(sortedProtocols);
            sSupportedProtocolCache = sortedProtocols;
            LOG.debug("Got list of supported protocols: %s", Arrays.asList(sortedProtocols));
        }

        return sSupportedProtocolCache;
    }

    public static synchronized String[] getDefaultProtocols() {
        if (DEFAULT_SSL_CONTEXT == null) {
            return new String[0];
        }

        if (sSupportedProtocolCache == null) {
            String[] sortedProtocols = DEFAULT_SSL_CONTEXT.createSSLEngine().getEnabledProtocols();
            Arrays.sort(sortedProtocols);
            sSupportedProtocolCache = sortedProtocols;
            LOG.debug("Got list of default protocols: %s", Arrays.asList(sortedProtocols));
        }

        return sSupportedProtocolCache;
    }

    public static String convertCiphersMapToString(Map<String, Set<String>> ciphers) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Set<String>> p : ciphers.entrySet()) {
            sb.append(p.getKey())
                    .append(":")
                    .append(StringUtils.join(p.getValue().toArray(), ","))
                    .append(";");
        }

        return sb.toString();
    }

    public static Map<String, Set<String>> convertCiphersStringToMap(String str) {
        if (str == null) {
            return null;
        }

        Map<String, Set<String>> ciphers = new HashMap<String, Set<String>>();
        for (String p : str.split("\\s*;\\s*")) {
            String[] proto = p.split("\\s*:\\s*");
            Collection<String> set = new HashSet<String>(Arrays.asList(proto[1].split("\\s*,\\s*")));
            ciphers.put(proto[0], new HashSet<String>(set));
        }

        return ciphers;
    }

    /*
     * shamelessly copied from OC client
     */
    public static void logCertificates(Certificate[] certs) {
        int certIdx = 0;
        for (Certificate c : certs) {
            if (c instanceof X509Certificate) {
                // Certificate subject & issuer.
                X509Certificate xCert = ((X509Certificate) c);
                String subject = null, issuer = null;
                Principal subjectDN = xCert.getSubjectDN(),
                        issuerDN = xCert.getIssuerDN();

                Date start = xCert.getNotBefore();
                Date end = xCert.getNotAfter();

                if (subjectDN != null)
                    subject = subjectDN.getName();
                if (issuerDN != null)
                    issuer = issuerDN.getName();
                // Certificate fingerprint.
                byte[] fingerprint = null; // OK to pass null to toHexString, will return ""
                try {
                    MessageDigest md = MessageDigest.getInstance("SHA1");
                    md.update(xCert.getEncoded());
                    fingerprint = md.digest();
                } catch (CertificateEncodingException e) {
                    LOG.trace("Can't encode certificate");
                } catch (NoSuchAlgorithmException e) {
                    LOG.trace("No SHA1 Algorithm");
                }
                // Log it.
                LOG.trace(" %4d: Subject: %s", certIdx, subject);
                LOG.trace("     : Issuer:  %s", issuer);
                LOG.trace("     : Start: %s", (start != null) ? start.toString() : "null");
                LOG.trace("     : End  : %s", (end != null) ? end.toString() : "null");
                LOG.trace("     : SHA1 fingerprint: %s", Util.toHexString(fingerprint));
            } else {
                LOG.trace(" %4d: <unknown> type: %s", c.getType());
            }
            ++certIdx;
        }
    }
}
