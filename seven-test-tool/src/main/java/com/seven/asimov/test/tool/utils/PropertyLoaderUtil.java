package com.seven.asimov.test.tool.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class PropertyLoaderUtil {
    private static final String TAG = PropertyLoaderUtil.class.getName();
    private static final String fileName = "brand.properties";
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static HashMap<String, String> properties = new HashMap<String, String>();

    private Context context;

    public PropertyLoaderUtil(Context context) {
        this.context = context;
    }

    public int loadProperties() {
        AssetManager assetManager = context.getAssets();
        int temp = 0;
        properties.clear();
        Properties p;
        InputStream in = null;
        try {
            in = assetManager.open("brandings" + FILE_SEPARATOR + fileName);
            p = new Properties();
            p.load(in);
            for (int i = 0; i < Fields.values().length; i++) {
                properties.put(Fields.values()[i].getField(), p.getProperty(Fields.values()[i].getField()));
            }
            Log.i(TAG, "size = " + properties.size());
        } catch (FileNotFoundException e) {
            temp = 1;
            e.printStackTrace();
        } catch (IOException e) {
            temp = 1;
            e.printStackTrace();
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return temp;
    }

    public static HashMap<String, String> getProperties() {
        return properties;
    }

    public static enum Fields {
        RAND_IDENTIFIER("brand_identifier"),
        PARENT_BRAND("parent_brand"),
        SYSTEM_RELAY_HOST("system.relay_host"),
        SYSTEM_CLIENT_RELAY_PORT("system.client.relay_port"),
        ANDROID_PACKAGE("android.package"),
        SUITE_NAME_FULL("suite_name_full"),
        SUITE_NAME("suite_name"),
        SUITE_NAME_7BIT("suite_name_7bit"),
        SUITE_NAME_WITHOUT_COMPANY_7BIT("suite_name_without_company_7bit"),
        CLIENT_UPGRADE_INTERVAL_MIN("client.upgrade_interval_min"),
        CLIENT_UPGRADE_URL("client.upgrade_url"),
        CLIENT_UPGRADE_DATA_ACTIVITY_TIMEOUT("client.upgrade_data_activity_timeout"),
        CLIENT_UPGRADE_POLL_ENABLED("client.upgrade_poll_enabled"),
        CLIENT_UPGRADE_IN_ROAMING("client.upgrade_in_roaming"),
        CLIENT_ANDROID_ENABLE_TRIGGER("client.android.enable_trigger"),
        CLIENT_TRIGGER_PORT("client.trigger_port"),
        CLIENT_TRIGGER_HEADER("client.trigger_header"),
        CLIENT_KEEPALIVE_INTERVAL("client.keepalive_interval"),
        CLIENT_KEEPALIVE_INTERVAL_STEP("client.keepalive_interval_step"),
        CLIENT_KEEPALIVE_INTERVAL_MIN("client.keepalive_interval_min"),
        CLIENT_KEEPALIVE_INTERVAL_MAX("client.keepalive_interval_max"),
        CLIENT_KEEPALIVE_TIMEOUT("client.keepalive_timeout"),
        CLIENT_INACTIVITY_TIMEOUT("client.inactivity_timeout"),
        CLIENT_INACTIVITY_DISCONNECT("client.inactivity_disconnect"),
        CLIENT_CONNECTION_USE_PROXY("client.connection_use_proxy"),
        CLIENT_CONNECTION_PROXY_SERVER("client.connection_proxy_server"),
        CLIENT_CONNECTION_PROXY_PORT("client.connection_proxy_port"),
        CLIENT_MSISDN_VALIDATION_ENABLED("client.msisdn_validation_enabled"),
        CLIENT_MSISDN_VALIDATION_PROTOCOL("client.msisdn_validation_protocol"),
        CLIENT_MSISDN_VALIDATION_URL("client.msisdn_validation_url"),
        CLIENT_MSISDN_VALIDATION_WAIT_TIME_MINUTES("client.msisdn_validation_wait_time_minutes"),
        CLIENT_ANDROID_PROD_BUILD("client.android.prod_build"),
        CLIENT_ANDROID_DEBUGGABLE("client.android.debuggable"),
        CLIENT_ANDROID_LOGICALY_CLOSED_TIMEOUT("client.android.logicaly_closed_timeout"),
        CLIENT_ANDROID_DORMANCY_TIMEOUT("client.android.dormancy_timeout"),
        CLIENT_ANDROID_PROXY_CONNECTION_INACTIVITY_TIMEOUT("client.android.proxy_connection_inactivity_timeout"),
        CLIENT_ANDROID_TCP_OUT_CONNECT_TIMEOUT("client.android.tcp_out_connect_timeout"),
        CLIENT_ANDROID_SERVICE_RESTART_ENABLED("client.android.service_restart.enabled"),
        CLIENT_ANDROID_UID("client.android.uid"),
        CLIENT_ANDROID_DEFAULT_DISPATCHERS_CFG("client.android.default_dispatchers_cfg"),
        CLIENT_BUILD_SKIP_PROGUARD("client.build.skip_proguard"),
        CLIENT_OPENCHANNEL_ROOTED_DEVICES_BUILD("client.openchannel.rooted_devices_build"),
        CLIENT_OPENCHANNEL_ROOTED_FAILOVER_ENABLED("client.openchannel.rooted_failover_enabled"),
        CLIENT_OPENCHANNEL_CACHE_TOTALSIZE("client.openchannel.cache.totalsize"),
        CLIENT_OPENCHANNEL_REDIRECTION_SERVER1_HOST("client.openchannel.redirection.server1.host"),
        CLIENT_OPENCHANNEL_REDIRECTION_SERVER1_PORT("client.openchannel.redirection.server1.port"),
        CLIENT_OPENCHANNEL_REDIRECTION_SERVER1_CAPABILITIES("client.openchannel.redirection.server1.capabilities"),
        CLIENT_OPENCHANNEL_REDIRECTION_SERVER1_FUNCTION("client.openchannel.redirection.server1.function"),
        CLIENT_OPENCHANNEL_REDIRECTION_SERVER2_HOST("client.openchannel.redirection.server2.host"),
        CLIENT_OPENCHANNEL_REDIRECTION_SERVER2_PORT("client.openchannel.redirection.server2.port"),
        CLIENT_OPENCHANNEL_REDIRECTION_SERVER2_CAPABILITIES("client.openchannel.redirection.server2.capabilities"),
        CLIENT_OPENCHANNEL_REDIRECTION_SERVER2_FUNCTION("client.openchannel.redirection.server2.function"),
        CLIENT_OPENCHANNEL_REDIRECTION_SERVER3_HOST("client.openchannel.redirection.server3.host"),
        CLIENT_OPENCHANNEL_REDIRECTION_SERVER3_PORT("client.openchannel.redirection.server3.port"),
        CLIENT_OPENCHANNEL_REDIRECTION_SERVER3_CAPABILITIES("client.openchannel.redirection.server3.capabilities"),
        CLIENT_OPENCHANNEL_REDIRECTION_SERVER3_FUNCTION("client.openchannel.redirection.server3.function"),
        CLIENT_OPENCHANNEL_REDIRECTION_FAILOVER_ENABLED("client.openchannel.redirection.failover.enabled"),
        CLIENT_OPENCHANNEL_REDIRECTION_FAILOVER_CONNECTION_ATTEMPT_INTERVAL("client.openchannel.redirection.failover.connection_attempt_interval"),
        CLIENT_OPENCHANNEL_ROAMING_WIFI_FAILOVER_ENABLED("client.openchannel.roaming_wifi_failover.enabled"),
        CLIENT_OPENCHANNEL_ROAMING_WIFI_FAILOVER_ACTIONS("client.openchannel.roaming_wifi_failover.actions"),
        CLIENT_OPENCHANNEL_MOBILE_NETWORKS_FAILOVER_ENABLED("client.openchannel.mobile_networks_failover.enabled"),
        CLIENT_OPENCHANNEL_MOBILE_NETWORKS_FAILOVER_ACTIONS("client.openchannel.mobile_networks_failover.actions"),
        CLIENT_OPENCHANNEL_MOBILE_NETWORKS_FAILOVER_ATTEMPT_INTERVAL("client.openchannel.mobile_networks_failover.attempt_interval"),
        CLIENT_OPENCHANNEL_MOBILE_NETWORKS_FAILOVER_RETRIES("client.openchannel.mobile_networks_failover.retries"),
        CLIENT_OPENCHANNEL_MOBILE_NETWORKS_FAILOVER_TIMEOUT("client.openchannel.mobile_networks_failover.timeout"),
        CLIENT_OPENCHANNEL_SPECIAL_IGNORE_BLACKLISTING("client.openchannel.special.ignore_blacklisting"),
        CLIENT_OPENCHANNEL_FIREWALL_BLOCK_TYPE("client.openchannel.firewall.block_type"),
        CLIENT_OPENCHANNEL_SPLITSSL_FORCED_PKI("client.openchannel.splitssl.forced_pki"),
        CLIENT_OPENCHANNEL_WCDMA_RADIO_LOGS("client.openchannel.wcdma_radio_logs"),
        CLIENT_OPENCHANNEL_WCDMA_RADIO_LOGS_T1("client.openchannel.wcdma_radio_logs.t1"),
        CLIENT_OPENCHANNEL_WCDMA_RADIO_LOGS_T2("client.openchannel.wcdma_radio_logs.t2"),
        CLIENT_OPENCHANNEL_SMS_BODY_LOGS("client.openchannel.sms_body_logs"),
        CLIENT_OPENCHANNEL_TRIAL_NO_PRENAT_ROUTING("client.openchannel.trial.no_prenat_routing"),
        CLIENT_OPENCHANNEL_TRIAL_IDENTIFICATION("client.openchannel.trial.identification"),
        CLIENT_OPENCHANNEL_TRIAL_COLLECT_LOGS("client.openchannel.trial.collect_logs"),
        CLIENT_OPENCHANNEL_TRIAL_TCPDUMP("client.openchannel.trial.tcpdump"),
        CLIENT_OPENCHANNEL_TRIAL_TCPDUMP_EXCLUDE_FILTER("client.openchannel.trial.tcpdump.exclude_filter"),
        CLIENT_OPENCHANNEL_TRIAL_TCPDUMP_PACKETS_LIMIT("client.openchannel.trial.tcpdump.packets_limit"),
        CLIENT_OPENCHANNEL_TRIAL_TCPDUMP_FILESIZE_LIMIT("client.openchannel.trial.tcpdump.filesize_limit"),
        CLIENT_OPENCHANNEL_TRIAL_TCPDUMP_MAX_FILES("client.openchannel.trial.tcpdump.max_files"),
        CLIENT_DEFAULT_LOG_LEVEL("client.default_log_level"),
        CLIENT_LOG_FILE_SIZE("client.log_file_size"),
        CLIENT_LOG_DIR_SIZE("client.log_dir_size"),
        CLIENT_OPENCHANNEL_LOG_HTTP("client.openchannel.log_http"),
        CLIENT_OPENCHANNEL_ENABLE_DISPATCHER_DEBUG_LOGS("client.openchannel.enable_dispatcher_debug_logs"),
        CLIENT_OPENCHANNEL_ENABLE_DISPATCHER_CSM_LOGS("client.openchannel.enable_dispatcher_csm_logs"),
        CLIENT_OPENCHANNEL_TRAFFIC_POLL_INTERVAL("client.openchannel.traffic_poll_interval"),
        CLIENT_OPENCHANNEL_TRAFFIC_LOG_INTERVAL("client.openchannel.traffic_log_interval"),
        CLIENT_OPENCHANNEL_DEBUG_TRAFFIC_COLLECTOR_LOGS("client.openchannel.debug.traffic_collector_logs"),
        CLIENT_OPENCHANNEL_HBR_TIMEOUT("client.openchannel.hbr_timeout");

        private String field;

        private Fields(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }
}
