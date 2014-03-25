package com.seven.asimov.test.tool.utils;

import android.content.Context;
import android.content.res.AssetManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrandingLoaderUtil {
    private static final Logger LOG = LoggerFactory.getLogger(BrandingLoaderUtil.class.getSimpleName());

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final String SYSTEM_RELAY_HOST = "system.relay_host";
    private static final String SYSTEM_CLIENT_RELAY_PORT = "system.client.relay_port";
    private static final String CLIENT_OC_REDIRECTION_SERVER1_HOST = "client.openchannel.redirection.server1.host";
    private static final String CLIENT_OC_REDIRECTION_SERVER1_PORT = "client.openchannel.redirection.server1.port";
    private static final String ANDROID_PACKAGE_NAME = "android.package";

    private static HashMap<String, String> properties = PropertyLoaderUtil.getProperties();

    public static ArrayList<String> incorrectParameters = new ArrayList<String>();
    public static ArrayList<String> error = new ArrayList<String>();

    List<Target> targets = new ArrayList<Target>(
            Arrays.asList(Target.C004_GA_TEST_IT_TARGET, Target.C009_GA_TEST_TARGET, Target.C004_NOZIP_GA_TEST_IT_TARGET,
                    Target.CV_BASELINE_3G1_GA_TARGET,
                    Target.CV_BASELINE_3G_GA_TARGET, Target.CV_BASELINE_WIFI_GA_TARGET, Target.CV_DEV_3G_GA_TARGET,
                    Target.CV_DEV_3G_GA_TARGET, Target.CV_DEV_3G_NOZIP_GA_TARGET, Target.CV_DEV_QA_WIFI_FAILOWER_GA_TARGET,
                    Target.CV_DEV_WIFI_FAILOWER_GA_TARGET, Target.CV_DEV_WIFI_GA_TARGET, Target.CV_DEV_WIFI_GA_ROOTED_TARGET,
                    Target.CV_DEV_WIFI_NOZIP_GA_TARGET, Target.CV_DEV_WIFI_TCV2_GA_TARGET, Target.CV_DEV_WIFI_TRIAL_GA_TARGET,
                    Target.CV_QA_3GL_GA_TARGET, Target.CV_QA_3G_GA_TARGET, Target.CV_QA_WIFI_FAILOWER_GA_TARGET,
                    Target.CV_QA_WIFI_GA_TARGET, Target.CV_QA_WIFI, Target.DEMO001_ARM027_AP4A_GA_TEST_GSM_ROOTED_TARGET,
                    Target.DEMO008_GA_TRIAL_ROOTED_TARGET, Target.DEMO011_GA_TRIAL_ROOTED_CYPRESS_TARGET,
                    Target.DEMO013_ARM035_AP4A_GA_TRIAL_ROOTED_TARGET, Target.DEMO016_GA_TRIAL_ROOTED_TARGET, Target.DEMO017_GA_TRIAL_ROOTED_TARGET,
                    Target.DEMO001_ARM027_AP4A_SMS_SQUID_SPLITSSL_TC_ROOTED_TEMP_TARGET,
                    Target.DEMO013_ARM035_AP4A_GA_TRIAL_ROOTED_NOPROXY_TARGET,
                    Target.DEMO006_GA_TRIAL_ROOTED, Target.DEMO012_TRIAL_ROOTED,
                    Target.DEMO018_GA_TRIAL_ROOTED,
                    Target.DEMO999_GA_TRIAL_ROOTED));

    private static List<Branding> brandings = new ArrayList<Branding>();

    private Context context;

    public BrandingLoaderUtil(Context context) {
        this.context = context;
    }

    public void loadBrandings() {
        for (Target target : targets) {
            Branding branding = loadBranding(target);
            brandings.add(branding);
        }
    }

    private Branding loadBranding(Target brandingFileName) {
        Branding result = new Branding();
        AssetManager assetManager = context.getAssets();
        try {
            InputStream in = assetManager.open("brandings" + FILE_SEPARATOR + brandingFileName.getName());
            Properties prop = new Properties();
            prop.load(in);

            result.setName(brandingFileName);
            result.setSystemRelayHost((String) prop.get(SYSTEM_RELAY_HOST));
            if (prop.get(CLIENT_OC_REDIRECTION_SERVER1_HOST) != null)
                result.setClientRedirectionServer1Host((String) prop.get(CLIENT_OC_REDIRECTION_SERVER1_HOST));
            else
                result.setClientRedirectionServer1Host((String) prop.get(SYSTEM_RELAY_HOST));
            try {
                if (prop.get(ANDROID_PACKAGE_NAME) != null) {
                    result.setAndroidPackageName(prop.getProperty(ANDROID_PACKAGE_NAME));
                } else {
                    result.setAndroidPackageName("com.seven.asimov");
                }
                result.setSystemClientRelayPort(Integer.parseInt((String) prop.get(SYSTEM_CLIENT_RELAY_PORT)));
                result.setClientRedirectionServer1Port(0);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            LOG.info(result.toString());
        } catch (IOException e) {
            LOG.error("Can't load branding. Exception : " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public static int addNewBranding(String path) {
        int temp = 0;
        Branding branding = loadNewBranding(path);
        LOG.info(branding.toString());
        if (incorrectParameters.isEmpty()) {
            if (error.isEmpty()) {
                for (Branding aBranding : brandings) {
                    if (branding.toString().equals(aBranding.toString())) {
                        temp = 1;
                    }
                }
            } else temp = 3;
        } else temp = 2;
        switch (temp) {
            case 0:
                branding.setPath(path);
                brandings.add(branding);
                return temp;
            default:
                return temp;
        }
    }

    private static Branding loadNewBranding(String path) {
        Branding result = new Branding();
        File file = new File(path);
        InetAddress[] address;
        if (file.exists() && file.getName().contains(".target")) {
            try {
                InputStream in = new FileInputStream(file);
                Properties prop = new Properties();
                prop.load(in);

                result.setBrandingName(file.getName());
                String host = "";
                Pattern p = Pattern.compile("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}");
                Matcher m = p.matcher(host);
                if (prop.get(SYSTEM_RELAY_HOST) != null) {
                    host = (String) prop.get(SYSTEM_RELAY_HOST);
                    LOG.info(host);
                    if (!m.matches()) {
                        address = InetAddress.getAllByName(host);
                        host = address[0].toString().substring(address[0].toString().indexOf("/") + 1);
                    }
                    result.setSystemRelayHost(host);
                } else {
                    result.setSystemRelayHost(null);
                    incorrectParameters.add(SYSTEM_RELAY_HOST);
                }
                if (prop.get(CLIENT_OC_REDIRECTION_SERVER1_HOST) != null) {
                    host = (String) prop.get(CLIENT_OC_REDIRECTION_SERVER1_HOST);
                    m = p.matcher(host);
                    if (!m.matches()) {
                        address = InetAddress.getAllByName(host);
                        host = address[0].toString().substring(address[0].toString().indexOf("/") + 1);
                    }
                    result.setClientRedirectionServer1Host(host);
                } else {
                    if (result.getSystemRelayHost() != null)
                        result.setClientRedirectionServer1Host(result.getSystemRelayHost());
                    else {
                        result.setClientRedirectionServer1Host(null);
                        incorrectParameters.add(CLIENT_OC_REDIRECTION_SERVER1_HOST);
                    }
                }
                try {
                    if (prop.get(SYSTEM_CLIENT_RELAY_PORT) != null) {
                        result.setSystemClientRelayPort(Integer.parseInt((String) prop.get(SYSTEM_CLIENT_RELAY_PORT)));
                    } else {
                        result.setSystemClientRelayPort(Integer.parseInt(properties.get(SYSTEM_CLIENT_RELAY_PORT)));
                    }
                    if (prop.get(ANDROID_PACKAGE_NAME) != null) {
                        result.setAndroidPackageName(prop.getProperty(ANDROID_PACKAGE_NAME));
                    } else {
                        result.setAndroidPackageName(properties.get(ANDROID_PACKAGE_NAME));
                    }
                    result.setClientRedirectionServer1Port(0);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
                LOG.info(result.toString());
            } catch (IOException e) {
                LOG.error("Can't load branding. Exception : " + e.getMessage());
                error.add(e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }

    public static List<Branding> getBrandings() {
        return brandings;
    }

    public static boolean setNewBranding(Branding branding) {
        boolean result = true;
        for (Branding b : brandings) {
            if (branding.toString().equals(b.toString())) {
                result = false;
            }
        }
        if (result) {
            brandings.add(branding);
        }
        return result;
    }


    public static class Branding {

        private Target target;
        private String brandingName;
        private String systemRelayHost;
        private String clientRedirectionServer1Host;
        private String androidPackageName;
        private int systemClientRelayPort;
        private int clientAndroidProxyPort;
        private int clientRedirectionServer1Port;
        private String path;

        public Target getName() {
            return target;
        }

        public void setName(Target target) {
            this.target = target;
        }

        public String getBrandingName() {
            return brandingName;
        }

        public void setBrandingName(String brandingName) {
            this.brandingName = brandingName;
        }

        public String getSystemRelayHost() {
            return systemRelayHost;
        }

        public void setSystemRelayHost(String systemRelayHost) {
            this.systemRelayHost = systemRelayHost;
        }

        public int getSystemClientRelayPort() {
            return systemClientRelayPort;
        }

        public void setSystemClientRelayPort(int systemClientRelayPort) {
            this.systemClientRelayPort = systemClientRelayPort;
        }

        public int getClientAndroidProxyPort() {
            return clientAndroidProxyPort;
        }

        public void setClientAndroidProxyPort(int clientAndroidProxyPort) {
            this.clientAndroidProxyPort = clientAndroidProxyPort;
        }

        public String getClientRedirectionServer1Host() {
            return clientRedirectionServer1Host;
        }

        public void setClientRedirectionServer1Host(String clientRedirectionServer1Host) {
            this.clientRedirectionServer1Host = clientRedirectionServer1Host;
        }

        public int getClientRedirectionServer1Port() {
            return clientRedirectionServer1Port;
        }

        public void setClientRedirectionServer1Port(int clientRedirectionServer1Port) {
            this.clientRedirectionServer1Port = clientRedirectionServer1Port;
        }

        public String getAndroidPackageName() {
            return androidPackageName;
        }

        public void setAndroidPackageName(String androidPackageName) {
            this.androidPackageName = androidPackageName;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Target getTarget() {
            return target;
        }

        public void setTarget(Target target) {
            this.target = target;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append("Branding info : ");
            if (brandingName == null)
                result.append(target.getName());
            else result.append(brandingName);
            result.append(LINE_SEPARATOR);
            result.append(SYSTEM_RELAY_HOST);
            result.append(":");
            result.append(systemRelayHost);
            result.append(LINE_SEPARATOR);
            result.append(SYSTEM_CLIENT_RELAY_PORT);
            result.append(":");
            result.append(systemClientRelayPort);
            result.append(LINE_SEPARATOR);
            result.append(ANDROID_PACKAGE_NAME);
            result.append(":");
            result.append(androidPackageName);
//            result.append(CLIENT_ANDROID_PROXY_PORT);
//            result.append(":");
//            result.append(clientAndroidProxyPort);
            result.append(LINE_SEPARATOR);
            result.append(CLIENT_OC_REDIRECTION_SERVER1_HOST);
            result.append(":");
            result.append(clientRedirectionServer1Host);
            result.append(LINE_SEPARATOR);
            result.append(CLIENT_OC_REDIRECTION_SERVER1_PORT);
            result.append(":");
            result.append(clientRedirectionServer1Port);
            result.append(LINE_SEPARATOR);
            return result.toString();
        }
    }

    public enum Target {
        UNKNOWN("unknown"),
        C004_GA_TEST_IT_TARGET("c004_ga_test_it.target"),
        C004_NOZIP_GA_TEST_IT_TARGET("c004_nozip_ga_test_it.target"),
        C009_GA_TEST_TARGET("c009_ga_test.target"),
        CV_BASELINE_3G1_GA_TARGET("cv_baseline_3g1_ga.target"),
        CV_BASELINE_3G_GA_TARGET("cv_baseline_3g_ga.target"),
        CV_BASELINE_WIFI_GA_TARGET("cv_baseline_wifi_ga.target"),
        CV_DEV_3G_GA_TARGET("cv_dev_3g_ga.target"),
        CV_DEV_3G_NOZIP_GA_TARGET("cv_dev_3g_nozip_ga.target"),
        CV_DEV_QA_WIFI_FAILOWER_GA_TARGET("cv_dev_qa_wifi_failover_ga.target"),
        CV_DEV_WIFI_FAILOWER_GA_TARGET("cv_dev_wifi_failover_ga.target"),
        CV_DEV_WIFI_GA_TARGET("cv_dev_wifi_ga.target"),
        CV_DEV_WIFI_GA_ROOTED_TARGET("cv_dev_wifi_ga_rooted.target"),
        CV_DEV_WIFI_NOZIP_GA_TARGET("cv_dev_wifi_nozip_ga.target"),
        CV_DEV_WIFI_TCV2_GA_TARGET("cv_dev_wifi_tcv2_ga.target"),
        CV_DEV_WIFI_TRIAL_GA_TARGET("cv_dev_wifi_trial_ga.target"),
        CV_QA_3GL_GA_TARGET("cv_qa_3g1_ga.target"),
        CV_QA_3G_GA_TARGET("cv_qa_3g_ga.target"),
        CV_QA_WIFI_FAILOWER_GA_TARGET("cv_qa_wifi_failover_ga.target"),
        CV_QA_WIFI_GA_TARGET("cv_qa_wifi_ga.target"),
        CV_QA_WIFI("cv_qa_wifi_ga_it.target"),
        DEMO001_ARM027_AP4A_GA_TEST_GSM_ROOTED_TARGET("demo001-arm027-ap4a_ga_test_gsm_rooted.target"),
        DEMO008_GA_TRIAL_ROOTED_TARGET("demo008_ga_trial_rooted.target"),
        DEMO011_GA_TRIAL_ROOTED_CYPRESS_TARGET("demo011_ga_trial_rooted_cypress.target"),
        DEMO013_ARM035_AP4A_GA_TRIAL_ROOTED_TARGET("demo013-arm035-ap4a_ga_trial_rooted.target"),
        DEMO016_GA_TRIAL_ROOTED_TARGET("demo016-ga-trial-rooted.target"),
        DEMO017_GA_TRIAL_ROOTED_TARGET("demo017_ga_trial_rooted.target"),
        DEMO001_ARM027_AP4A_SMS_SQUID_SPLITSSL_TC_ROOTED_TEMP_TARGET("demo001-arm027-ap4a_SMS_squid_splitssl_tc_rooted_temp.target"),
        DEMO013_ARM035_AP4A_GA_TRIAL_ROOTED_NOPROXY_TARGET("demo013-arm035-ap4a_ga_trial_rooted_noproxy.target"),
        DEMO006_GA_TRIAL_ROOTED("demo006-ga-trial-rooted.target"),
        DEMO012_TRIAL_ROOTED("demo012_samsung_trial.target"),
        DEMO018_GA_TRIAL_ROOTED("demo018_ga_trial_rooted_full_logging.target"),
        DEMO999_GA_TRIAL_ROOTED("demo999_rooted.target");

        private String name;

        Target(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
