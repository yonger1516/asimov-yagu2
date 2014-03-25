package com.seven.asimov.test.tool.core;

import com.seven.asimov.test.tool.preferences.SharedPrefs;
import com.seven.asimov.test.tool.utils.Z7HttpsUtil;

import java.util.*;

public abstract class HttpsOptions {
    private static Set<String> sHttpsProtocols = null;
    private static Map<String, Set<String>> sHttpsCiphers = null;

    private static void initProtocolList() {
        if (sHttpsProtocols == null) {
            String[] prefProtocols = SharedPrefs.getHttpsProtocols();
            if (prefProtocols == null) {
                sHttpsProtocols = new HashSet<String>(Arrays.asList(Z7HttpsUtil.getDefaultProtocols()));
            } else {
                sHttpsProtocols = new HashSet<String>(Arrays.asList(prefProtocols));
            }
        }
    }

    public static void setProtocols(String... protocols) {
        sHttpsProtocols = new HashSet<String>(Arrays.asList(protocols));
    }

    public static void addProtocols(String... protocols) {
        initProtocolList();
        for (String p : protocols) {
            sHttpsProtocols.add(p);
        }
    }

    public static void removeProtocols(String... protocols) {
        initProtocolList();
        for (String p : protocols) {
            sHttpsProtocols.remove(p);
        }
    }

    public static Set<String> getProtocolsAsSet() {
        initProtocolList();
        return sHttpsProtocols;
    }

    public static String[] getProtocolsAsArray() {
        initProtocolList();
        return sHttpsProtocols.toArray(new String[0]);
    }

    public static boolean isProtocolEnabled(String protocol) {
        for (String p : getProtocolsAsSet()) {
            if (p.equals(protocol)) {
                return true;
            }
        }
        return false;
    }

    public static void persistProtocol() {
        SharedPrefs.saveHttpsProtocols(getProtocolsAsArray());
    }

    private static void initCipherList() {
        if (sHttpsCiphers == null) {
            Map<String, Set<String>> storedCiphers = SharedPrefs.getHttpsCiphers();
            if (storedCiphers != null) {
                sHttpsCiphers = storedCiphers;
            } else {
                sHttpsCiphers = new HashMap<String, Set<String>>();
            }
        }
    }

    public static void persistCiphers() {
        initCipherList();
        SharedPrefs.saveHttpsCiphers(sHttpsCiphers);
    }

    public static void setCiphers(String protocol, String... ciphers) {
        initCipherList();
        sHttpsCiphers.put(protocol, new HashSet<String>(Arrays.asList(ciphers)));
    }

    public static void addCiphers(String protocol, String... ciphers) {
        initCipherList();
        if (!sHttpsCiphers.containsKey(protocol)) {
            String[] defaultCiphers = Z7HttpsUtil.getDefaultCiphers(protocol);
            sHttpsCiphers.put(protocol, new HashSet<String>(Arrays.asList(defaultCiphers)));
        }
        sHttpsCiphers.get(protocol).addAll(Arrays.asList(ciphers));
    }

    public static void removeCiphers(String protocol, String... ciphers) {
        initCipherList();
        if (!sHttpsCiphers.containsKey(protocol)) {
            String[] defaultCiphers = Z7HttpsUtil.getDefaultCiphers(protocol);
            sHttpsCiphers.put(protocol, new HashSet<String>(Arrays.asList(defaultCiphers)));
        }

        for (String c : ciphers) {
            sHttpsCiphers.get(protocol).remove(c);
        }
    }

    public static String[] getCiphersAsArray(String... protocols) {
        return getCiphersAsSet(protocols).toArray(new String[0]);
    }

    public static Set<String> getCiphersAsSet(String... protocols) {
        initCipherList();
        Set<String> ciphers = new HashSet<String>();
        for (String p : protocols) {
            if (sHttpsCiphers.containsKey(p)) {
                ciphers.addAll(sHttpsCiphers.get(p));
            } else {
                ciphers.addAll(Arrays.asList(Z7HttpsUtil.getDefaultCiphers(p)));
            }
        }
        return ciphers;
    }
}
