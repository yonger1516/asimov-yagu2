package com.seven.asimov.it.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import com.seven.asimov.it.R;
import com.seven.asimov.it.base.AsimovTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.seven.asimov.it.base.constants.BaseConstantsIF.*;

public final class PropertyLoadUtil {
    private static final Logger logger = LoggerFactory.getLogger(PropertyLoadUtil.class.getSimpleName());

    private static Properties brandingProperties;
    private static Properties defaultBrandingProperties;
    private static Properties catfProperties;
    private static Context context;

    public static String ocVersion;
    public static int ipVersion;
    public static String ipv4_testrunner;
    public static String ipv6_testrunner;
    public static String testRunner;

    private PropertyLoadUtil() {
    }

    public static void init(Context cont) {
        context = cont;
        initBrandingProperties();
        initDefaultBrandingProperties();
        initCATFProperties();
        initOCVersion();
        logAllProperties();
    }

    private static void logAllProperties() {
        logger.trace("------------- brandingProperties -----------------");
        logProperties(brandingProperties);
        logger.trace("------------- defaultBrandingProperties -----------------");
        logProperties(defaultBrandingProperties);
        logger.trace("------------- catfProperties -----------------");
        logProperties(catfProperties);
        logger.trace("--------------------------------------------------");
    }

    private static void logProperties(Properties props) {
        for(Map.Entry e: props.entrySet()) {
            logger.trace(e.getKey() + "=" + e.getValue());        }
    }

    public static String getProperty(String propertyName) {
        logger.trace("getPropert " + propertyName);
        String value = getBrandingProperty(propertyName);
        if (value == null) {
            if (propertyName.equals("min_dispatcher_port")) {
                value = getDefaultBrandingProperty("client.openchannel.dispatchers.initial.listen.port");
            } else {
                value = getDefaultBrandingProperty(propertyName);
            }
            if (value == null) {
                value = getCatfProperty(propertyName);
            }
        }
        return value;
    }

    public static Integer getIntegerProperty(String propertyName) {
        logger.trace("getIntegerProperty " + propertyName);
        try {
            return Integer.valueOf(getProperty(propertyName));
        } catch (NumberFormatException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public static Boolean getBooleanProperty(String propertyName) {
        try {
            return Boolean.valueOf(getProperty(propertyName));
        } catch (NumberFormatException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public static String getRelayHost() {
        for (int i = 0; i < 3; i++) {
            try {
                InetAddress[] addresses = InetAddress.getAllByName(getProperty("system.relay_host"));
                for (InetAddress addr : addresses) {
                    if (addr instanceof Inet4Address)
                        return addr.getHostAddress();
                }
            } catch (UnknownHostException e) {
                logger.error(e.toString());
            }
        }
        return "";
    }

    public static int getDnsDispatcherPort() {
        int id = 65001;
        List<String> commands = new ArrayList<String>();
        commands.add(GET_IPTABLES_RULES);
        List<String> str = ShellUtil.execWithCompleteResultWithListOutput(commands, true);
        for (String aStr : str) {
            if (aStr.contains(DNS_DISPATCHER_RULE)) {
                id = Integer.valueOf(aStr.substring(aStr.indexOf(PORTS) + 6));
                return id;
            }
        }
        logger.trace("Dns dispatcher port = " + id);
        return id;
    }

    public static int getHttpDispatcherPort() {
        int id = 65001;
        List<String> commands = new ArrayList<String>();
        commands.add(GET_IPTABLES_RULES);
        List<String> str = ShellUtil.execWithCompleteResultWithListOutput(commands, true);
        for (String aStr : str) {
            if (aStr.contains(HTTP_DISPATCHER_RULE)) {
                id = Integer.valueOf(aStr.substring(aStr.indexOf(PORTS) + 6));
                return id;
            }
        }
        logger.trace("Http dispatcher port = " + id);
        return id;
    }

    public static int getIpVersion() {
        logger.trace("ipVersion=" + ipVersion);
        return ipVersion;
    }

    public static String getTestrunner() {
        if (!testRunner.equals("")) {
            return testRunner;
        }
        if (ipVersion == 4) {
            return ipv4_testrunner;
        }
        if (ipVersion == 6) {
            return ipv6_testrunner;
        }
        return "";
    }

    private static void initBrandingProperties() {
        if (brandingProperties != null) {
            brandingProperties.clear();
        }
        Resources res = context.getResources();

        String brandName = res.getString(R.string.branding);
        logger.info(brandName);
        brandingProperties = getPropertiesFromAssets("brandings" + File.separator + brandName + ".target");
    }

    private static void initDefaultBrandingProperties() {
        if (defaultBrandingProperties != null) {
            defaultBrandingProperties.clear();
        }
        Resources res = context.getResources();

        String brandName = res.getString(R.string.branding_property);
        logger.info(brandName);
        defaultBrandingProperties = getPropertiesFromAssets("brandings" + File.separator + brandName);
    }

    private static void initCATFProperties() {
        if (catfProperties != null) {
            catfProperties.clear();
        }
        catfProperties = getPropertiesFromAssets("tf_property.xml");
    }

    public static void initIPAndTestRunner() {
        logger.trace("initIPAndTestRunner");
        ipVersion = Integer.parseInt(catfProperties.getProperty("ip_version"));
        logger.trace("initIPAndTestRunner ipVersion=" + ipVersion);
        testRunner = catfProperties.getProperty("testrunner");
        ipv4_testrunner = catfProperties.getProperty("ipv4_testrunner");
        ipv6_testrunner = catfProperties.getProperty("ipv6_testrunner");
        AsimovTestCase.TEST_RESOURCE_HOST = getTestrunner();

        logger.trace(ipVersion + " " + testRunner);
    }

    private static void initOCVersion() {
        ocVersion =getCatfProperty("oc_version");
    }

    private static Properties getPropertiesFromAssets(String file) {
        AssetManager assetManager = context.getAssets();
        Properties props = null;
        try {
            InputStream inputStream = assetManager.open(file);
            props = new Properties();
            if (file.endsWith(".xml")) {
                props.loadFromXML(inputStream);
            } else {
                props.load(inputStream);
            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return props;
    }

    private static String getBrandingProperty(String propertyName) {
        logger.trace("getBrandingProperty " + propertyName);
        return brandingProperties.getProperty(propertyName);
    }

    private static String getDefaultBrandingProperty(String propertyName) {
        logger.trace("getDefaultBrandingProperty " + propertyName);
        return defaultBrandingProperties.getProperty(propertyName);
    }

    private static String getCatfProperty(String propertyName) {
        return catfProperties.getProperty(propertyName);
    }
}