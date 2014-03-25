package com.seven.asimov.test.tool.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.utils.ShellUtil;
import com.seven.asimov.test.tool.R;
import com.seven.asimov.test.tool.core.exceptions.TFPropertyFormatException;
import com.seven.asimov.test.tool.core.exceptions.TFPropertyNotFoundException;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;


/**
 * Created with IntelliJ IDEA.
 * Date: 4/10/13
 * Time: 8:18 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Z7TFProperties {
    private static Properties brandingProperties;
    private static Properties testProperties;
    private static Properties defaultBrandingProperties;
    private static Context context;
    public static int ocVersion;
    public static int ipVersion;
    public static String ipv4_testrunner;
    public static String ipv6_testrunner;
    public static String testRunner;
    public static final String TAG = "TFProperties :: ";
    public static final String GET_IPTABLES_RULES = "iptables -t nat -L";
    public static final String DNS_DISPATCHER_RULE = "udp dpt:domain";
    public static final String HTTP_DISPATCHER_RULE = "tcp dpt:www";
    public static final String PORTS = "ports";
    public static final String BRANDINGS_DIRECTORY = "brandings";
    public static final int BAYSHORE = 2;

    public static void init(Context cont) {
        init(cont, null);
    }

    public static void init(Context cont, String path) {
        context = cont;
        initBrandingProperties(path);
        initTestProperties();
        initDefaultBrandingProperties();
        initOCVersion();
        logAllProperties();
    }

    private static void logAllProperties() {
        Log.d(TAG, "------------- testProperties -----------------");
        logProperties(testProperties);
        Log.d(TAG, "------------- defaultBrandingProperties -----------------");
        logProperties(defaultBrandingProperties);
        Log.d(TAG, "------------- brandingProperties -----------------");
        logProperties(brandingProperties);
        Log.d(TAG, "--------------------------------------------------");
    }

    private static void logProperties(Properties props) {
        for (Entry e : props.entrySet()) {
            Log.d("", e.getKey() + "=" + e.getValue());
        }
    }

    private static void initBrandingProperties(String path) {
        if (brandingProperties != null) {
            brandingProperties.clear();
        }
        Log.i("TAG", "initBrandingProperties");
        Resources res = context.getResources();

        String brandName = path != null ? path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(".")) :
                res.getString(R.string.branding);
        Log.i("TAG", brandName);
        brandingProperties = getPropertiesFromAssets(BRANDINGS_DIRECTORY + File.separator + "default.target");
        for (Entry<Object, Object> entry : path != null ? getPropertiesFromSDCard(path).entrySet() :
                getPropertiesFromAssets(BRANDINGS_DIRECTORY + File.separator + brandName + ".target").entrySet()) {
            brandingProperties.put(entry.getKey(), entry.getValue());
        }
        String defBrand = "brand.properties";
        for (Entry<Object, Object> entry : getPropertiesFromAssets(BRANDINGS_DIRECTORY + File.separator + defBrand).entrySet()) {
            if (brandingProperties.getProperty((String) entry.getKey()) == null) {
                brandingProperties.put(entry.getKey(), entry.getValue());
            }
        }

    }

    private static void initOCVersion() {
        AssetManager assetManager = context.getAssets();
        InputStream in = null;
        try {
            in = assetManager.open("tf_property.xml");
            testProperties = new Properties();
            testProperties.loadFromXML(in);
            ocVersion = Integer.parseInt(testProperties.getProperty("oc_version"));
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void initIPAndTestRunner() {
        AssetManager assetManager = context.getAssets();
        InputStream in = null;
        try {
            in = assetManager.open("tf_property.xml");
            Properties prop = new Properties();
            prop.loadFromXML(in);
            ipVersion = Integer.parseInt(prop.getProperty("ip_version"));
            testRunner = prop.getProperty("testrunner");
            ipv4_testrunner = prop.getProperty("ipv4_testrunner");
            ipv6_testrunner = prop.getProperty("ipv6_testrunner");
            AsimovTestCase.TEST_RESOURCE_HOST = getTestrunner();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.v(TAG, ipVersion + " " + testRunner);
    }

    private static void initTestProperties() {
        //testProperties = getPropertiesFromAssets("test.property");
    }

    private static void initDefaultBrandingProperties() {
        Log.i("TAG", "initDefaultBrandingProperties");
        defaultBrandingProperties = getPropertiesFromAssets(BRANDINGS_DIRECTORY + File.separator + "brand.properties");
    }

    private static Properties getPropertiesFromAssets(String file) {
        AssetManager assetManager = context.getAssets();
        Properties props = null;
        try {
            InputStream inputStream = assetManager.open(file);
            props = new Properties();
            props.load(inputStream);
        } catch (FileNotFoundException fex) {
            Log.e(TAG, ExceptionUtils.getStackTrace(fex));
            throw new AssertionError("File asimov-client-tf/assets/" + fex + " is not found. Please, check this file in asimov-client-tf/assets/" + BRANDINGS_DIRECTORY + " directory.");
        } catch (IOException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
            throw new AssertionError(e);
        }
        return props;
    }

    private static Properties getPropertiesFromSDCard(String path) {
        File file = new File(path);
        Properties props;
        try {
            InputStream in = new FileInputStream(file);
            props = new Properties();
            props.load(in);

        } catch (FileNotFoundException fex) {
            Log.e(TAG, ExceptionUtils.getStackTrace(fex));
            throw new AssertionError("File asimov-client-tf/assets/" + fex + " is not found. Please, check this file in asimov-client-tf/assets/" + BRANDINGS_DIRECTORY + " directory.");
        } catch (IOException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
            throw new AssertionError(e);
        }
        return props;
    }

    private static String getBrandingProperty(String propertyName) {
        return brandingProperties.getProperty(propertyName);
    }

    private static String getTestProperty(String propertyName) {
        return testProperties.getProperty(propertyName);
    }

    private static String getDefaultBrandingProperty(String propertyName) {
        return defaultBrandingProperties.getProperty(propertyName);
    }

    public static String getProperty(String propertyName) {
        String testValue = getTestProperty(propertyName);
        String defaultBrandingValue = null;
        String brandValue = null;
        if (testValue == null) { // no property in test.property
            brandValue = getBrandingProperty(propertyName);
            if (brandValue == null) {
                if (ocVersion == BAYSHORE) {
                    processPropertyException(new TFPropertyNotFoundException(propertyName));
                } else {
                    if (propertyName.equals("min_dispatcher_port")) {
                        defaultBrandingValue = getDefaultBrandingProperty("client.openchannel.dispatchers.initial.listen.port");
                    } else {
                        defaultBrandingValue = getDefaultBrandingProperty(propertyName);
                    }
                    if (defaultBrandingValue == null) {
                        processPropertyException(new TFPropertyNotFoundException(propertyName));
                    } else {
                        brandValue = defaultBrandingValue;
                    }
                }
            }
        } else {
            brandValue = getBrandingProperty(testValue);
            if (brandValue == null) {
                if (ocVersion == BAYSHORE) {
                    brandValue = defaultBrandingValue;
                } else {
                    if (propertyName.equals("min_dispatcher_port")) {
                        defaultBrandingValue = getDefaultBrandingProperty("client.openchannel.dispatchers.initial.listen.port");
                    } else {
                        defaultBrandingValue = getDefaultBrandingProperty(propertyName);
                    }
                    if (defaultBrandingValue == null) {
                        brandValue = testValue;
                    } else {
                        brandValue = defaultBrandingValue;
                    }
                }
            }
        }
        return brandValue;
    }
//
//    public static Integer getIntegerProperty(String propertyName) {
//        try {
//            return Integer.valueOf(getProperty(propertyName));
//        } catch (NumberFormatException nfe) {
//            processPropertyException(new TFPropertyFormatException(propertyName, getProperty(propertyName), "integer"));
//            return null;
//        }
//    }

    /**
     * Checks if all essential constants are available and correct.
     * It adds warnings to logcat and if throws RuntimeException it will lead to testing process to fail.
     *
     * @param e exception to be processed
     */
    private static void processPropertyException(Exception e) {
        Log.w(TAG, e.getMessage());
        if (e instanceof TFPropertyNotFoundException) {
            TFPropertyNotFoundException pnfe = (TFPropertyNotFoundException) e;
            //add critical checks here
            /* Example
            Log.v(TAG, "1:client.msisdn_validation_enabled 2:" + pnfe.getPropertyName() + " equals:" + "client.msisdn_validation_enabled".equals(pnfe.getPropertyName()));
            if ("client.msisdn_validation_enabled".equals(pnfe.getPropertyName())) {
                Log.v(TAG, "throw new RuntimeException");
                throw new RuntimeException(e.getMessage());
            }
            */
        }
        if (e instanceof TFPropertyFormatException) {
            TFPropertyFormatException pfe = (TFPropertyFormatException) e;
            //add critical checks here
            /* Example
            Log.v(TAG, "1:client.msisdn_validation_enabled 2:" + pfe.getPropertyName() + " equals:" + "client.msisdn_validation_enabled".equals(pfe.getPropertyName()));
            if ("client.msisdn_validation_enabled".equals(pfe.getPropertyName())) {
                Log.v(TAG, "throw new RuntimeException");
                throw new RuntimeException(e.getMessage());
            }
            */
        }
    }

    //TODO: improve method, to allow it to get ANY dispatcher's port.
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
        Log.v(TAG, "Dns dispatcher port = " + id);
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
        Log.v(TAG, "Http dispatcher port = " + id);
        return id;
    }

    public static int getOcVersion() {
        return ocVersion;
    }

    public static void setOcVersion(int ocVersion) {
        Z7TFProperties.ocVersion = ocVersion;
    }

    public static int getIpVersion() {
        return ipVersion;
    }

    public static String getTestrunner() {
        if (!testRunner.equals(""))
            return testRunner;

        if (ipVersion == 4)
            return ipv4_testrunner;
        if (ipVersion == 6)
            return ipv6_testrunner;

        return "";
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
                Log.e(TAG, e.toString());
            }
        }
        return "";
    }
}