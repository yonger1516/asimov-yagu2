package com.seven.asimov.it.base.constants;

import com.seven.asimov.it.utils.PropertyLoadUtil;
import com.seven.asimov.it.utils.TelnetUtil;
import com.seven.asimov.it.utils.conn.ConnUtils;

import java.util.ArrayList;
import java.util.Arrays;

public interface TFConstantsIF {
    //TcpDumpConstants
    public static final Integer DNS_PORT = PropertyLoadUtil.getIntegerProperty("dns_port");
    public static final Integer DNS_LL_PORT = PropertyLoadUtil.getIntegerProperty("dns_ll_port");
    public static final Integer DNS_SERVER_PORT = PropertyLoadUtil.getIntegerProperty("dns_server_port");
    public static final Integer HTTP_PORT = PropertyLoadUtil.getIntegerProperty("http_port");
    public static final Integer HTTP_LL_PORT = PropertyLoadUtil.getIntegerProperty("http_ll_port");
    public static final Integer HTTPS_PORT = PropertyLoadUtil.getIntegerProperty("https_port");
    public static final Integer HTTPS_SPLIT_PORT = PropertyLoadUtil.getIntegerProperty("https_split_port");
    public static final Integer HTTPS_LL_PORT = PropertyLoadUtil.getIntegerProperty("https_ll_port");
    public static final Integer HTTP_TC_PORT_1 = PropertyLoadUtil.getIntegerProperty("http_tc_port_1");
    public static final Integer HTTP_TC_PORT_2 = PropertyLoadUtil.getIntegerProperty("http_tc_port_2");

    public static final Integer MIN_DISPATCHER_PORT = PropertyLoadUtil.getIntegerProperty("min_dispatcher_port");

    public static final Integer OC_BYPASS_PORT = PropertyLoadUtil.getIntegerProperty("oc_bypass_port");

    public static final Integer Z7TP_RELAY_PORT = PropertyLoadUtil.getIntegerProperty("z7tp_relay_port");

    public static final Integer BYTE_SIZE = PropertyLoadUtil.getIntegerProperty("byte_size");
    public static final Integer UINT16_SIZE = PropertyLoadUtil.getIntegerProperty("uint16_size");
    public static final Integer UINT32_SIZE = PropertyLoadUtil.getIntegerProperty("uint32_size");
    public static final Integer BITS_IN_BYTE = PropertyLoadUtil.getIntegerProperty("bits_in_byte");

    public static final Integer TCP_PROTO_ID = PropertyLoadUtil.getIntegerProperty("tcp_proto_id");
    public static final Integer UDP_PROTO_ID = PropertyLoadUtil.getIntegerProperty("udp_proto_id");

    public static final Integer SLL_LENGTH = PropertyLoadUtil.getIntegerProperty("sll_length");
    public static final Integer IP_HEADER_LENGTH = PropertyLoadUtil.getIntegerProperty("ip_header_length");

    public static final Integer LOOPBACK_ADDRESS_TYPE = PropertyLoadUtil.getIntegerProperty("loopback_address_type");

    public static final Integer PACKET_TYPE_TO_US = PropertyLoadUtil.getIntegerProperty("packet_type_to_us");
    public static final Integer PACKET_TYPE_FROM_US = PropertyLoadUtil.getIntegerProperty("packet_type_from_us");

    public static final Integer NETLOG_VERSION = PropertyLoadUtil.getIntegerProperty("netlog_version");

    public static final String GET_METHOD = "GET";
    public static final String POST_METHOD = "POST";

    //ServerLogsDownloader
    public static final String SERVER_LOG_NAMES_PART_ONE = PropertyLoadUtil.getProperty("server_log_names_part_one");
    public static final String SERVER_LOG_NAMES_PART_TWO = PropertyLoadUtil.getProperty("server_log_names_part_two");
    public static final String ENG002_APS_PART_ONE = PropertyLoadUtil.getProperty("eng002_aps_part_one");
    public static final String ENG002_APS_PART_TWO = PropertyLoadUtil.getProperty("eng002_aps_part_two");
    public static final String ENG004_APS_PART_ONE = PropertyLoadUtil.getProperty("eng004_aps_part_one");
    public static final String ENG004_APS_PART_TWO = PropertyLoadUtil.getProperty("eng004_aps_part_two");
    public static final String CUSTOM_SERVER_APS_PART_ONE = PropertyLoadUtil.getProperty("custom_server_aps_part_one");
    public static final String CUSTOM_SERVER_APS_PART_TWO = PropertyLoadUtil.getProperty("custom_server_aps_part_two");
    public static final String HKI_ADM02_HOST = PropertyLoadUtil.getProperty("hki_adm02.host");
    public static final String SERVER_LOG_DOWNLOAD_ENABLED = PropertyLoadUtil.getProperty("server_log_download_enabled");

    //E2ETestConstants
    public static final String REST_SERVER_ADDRESS = PropertyLoadUtil.getProperty("rest.server.address");
    public static final Integer REST_SERVER_PORT = PropertyLoadUtil.getIntegerProperty("rest.server.port");
    /**
     * The Rest Serve port which is listen by the SMPP server.
     */
    public static final Integer REST_SERVER_SMS_PORT = PropertyLoadUtil.getIntegerProperty("rest.server.sms.port");

    //RestHelper
    public static final String EXTERNAL_IP = PropertyLoadUtil.getRelayHost();
    public static final Integer PMS_SERVER_PORT = PropertyLoadUtil.getIntegerProperty("system.pms.server.port");
    public static final Integer RELAY_PORT = PropertyLoadUtil.getIntegerProperty("system.client.relay_port");
    public static final String GA_VENDOR = PropertyLoadUtil.getProperty("ga_vendor");
    public static final String EMULATOR_NAME = PropertyLoadUtil.getProperty("emulator_name");
    public static final String TC_REDIRECTION_PORT = PropertyLoadUtil.getProperty("client.openchannel.redirection.server1.port");
    public static final String HTTPS_REDIRECTION_PORT = PropertyLoadUtil.getProperty("client.openchannel.redirection.server2.port");

    public static String mPmsServerIp = EXTERNAL_IP;
    public static Integer mPmsServerPort = PMS_SERVER_PORT;
    public static String mEmulatorVendor = GA_VENDOR;

    //OpenChannelHelper
    public static final String TARGET_NAME = "com.seven.asimov";

    //SmokeHelper
    public static final Integer DEFAULT_RELAY_PORT = 7735;
    public static final Integer RELAY_REDIRECTION = 8765;
    public static final Integer ELSE_RELAY_REDIRECTION = 7888;
    public static final Integer SSL_RELAY_REDIRECTION = 7433;

    //TODO: add some mechanism to review current dispatcher port (might be changed by config)
    public static final Integer DEFAULT_DNS_SERVER_PORT = PropertyLoadUtil.getDnsDispatcherPort();
    public static final Integer DEFUALT_HTTP_SERVER_PORT = PropertyLoadUtil.getHttpDispatcherPort();

    public static final Integer MSISDN_VALIDATION_STATE = PropertyLoadUtil.getIntegerProperty("client.msisdn_validation_enabled");
    public static final String MSISDN_VALIDATION_PHONENUMBER = PropertyLoadUtil.getProperty("system.msisdn_validation_phonenumber");
    // TODO uncomment after some decision about constants
    public static final String OC_INTEGRATION_TESTS_RESULTS_DIR = PropertyLoadUtil.getProperty("tests_results_dir");

    //ConnUtils
    public static final String OC_PACKAGE_NAME = PropertyLoadUtil.getProperty("oc_package_name");
    public static final String OC_HTTP_PROXY_ADDRESS = ConnUtils.getLocalIpAddress().getHostAddress();
    public static final Integer OC_HTTP_PROXY_PORT = PropertyLoadUtil.getIntegerProperty("oc_http_proxy_port");
    public static final String OC_PROCESS_NAME = PropertyLoadUtil.getProperty("oc_process_name");

    public static final String CRLF = "\r\n";
    public static final String HEADER_CONNECTION = PropertyLoadUtil.getProperty("header_connection");
    public static final String HEADER_CONNECTION_CLOSE = PropertyLoadUtil.getProperty("header_connection_close");
    ;
    public static final String HEADER_CONNECTION_KEEP_ALIVE = PropertyLoadUtil.getProperty("header_connection_keep_alive");
    public static final String HEADER_CONTENT_LENGTH = PropertyLoadUtil.getProperty("header_content_length");
    public static final String SOCKET_EXCEPTION_RST_MESSAGE = PropertyLoadUtil.getProperty("socket_exception_rst_message");

    //DnsTestConstants
    public static final String TEMP_FAILOVER = PropertyLoadUtil.getProperty("temp_failover");
    public static final String INDEFINITE_FAILOVER = PropertyLoadUtil.getProperty("indefinite_failover");

    public static final Integer DNS_DEFAULT_PORT = PropertyLoadUtil.getIntegerProperty("dns_default_port");
    public static final Integer OVERRIDE_DNS_SERVER_PORT = PropertyLoadUtil.getIntegerProperty("override_dns_server_port");
    public static final Integer OVERRIDE_DNS_TTL = PropertyLoadUtil.getIntegerProperty("override_dns_ttl");

    //DEFAULT_DNS_SERVER_PORT was set in DnsTestConstants as 53 but already exists in TFConstants
    //public static final Integer  DEFAULT_DNS_SERVER_PORT = PropertyLoadUtil.getIntegerProperty("default_dns_server_port");

    public static final Integer DEFAULT_DNS_TTL = PropertyLoadUtil.getIntegerProperty("default_dns_ttl");
    public static final String DEFAULT_DNS_SERVER = PropertyLoadUtil.getProperty("default_dns_server");
    public static final String PARENT_CHAIN = PropertyLoadUtil.getProperty("parent_chain");
    public static final String PROP_ASIMOV_ENABLED = "openchannel_enabled";
    public static final ArrayList<String> getOutputChainCmd = new ArrayList<String>(Arrays.asList(new String[]{"/system/bin/iptables", "-t", "nat", "-L", "OUTPUT", "-n"}));

    //OCCertificateConstants
    // Open Channel certificate exception control parameters
    public static final Integer OC_IGNORE_ALL_CERTIFICATE_EXCEPTIONS = PropertyLoadUtil.getIntegerProperty("oc_ignore_all_certificate_exceptions");
    public static final Integer OC_CHECK_ALL_CERTIFICATE_EXCEPTIONS = PropertyLoadUtil.getIntegerProperty("oc_check_all_certificate_exceptions"); //this is the default value
    public static final Integer OC_IGNORE_CERTIFICATE_AUTHORITY_EXCEPTIONS = PropertyLoadUtil.getIntegerProperty("oc_ignore_certificate_authority_exceptions");
    public static final Integer OC_IGNORE_CERTIFICATE_NOT_YET_VALID_EXCEPTION = PropertyLoadUtil.getIntegerProperty("oc_ignore_certificate_not_yet_valid_exception");
    public static final Integer OC_IGNORE_CERTIFICATE_EXPIRED_EXCEPTION = PropertyLoadUtil.getIntegerProperty("oc_ignore_certificate_expired_exception");

    //AsimovTestCase
    public static long MIN_CACHING_PERIOD = PropertyLoadUtil.getIntegerProperty("min_caching_period");

    //Media state constants
    public static final String MEDIA_SCRIPT_CONDITION_PATH = PropertyLoadUtil.getProperty("media_script_condition_path");
    public static final String IN_CONDITIONS = PropertyLoadUtil.getProperty("in_conditions");
    public static final String OUT_CONDITIONS = PropertyLoadUtil.getProperty("out_conditions");
    public static final String ACTION_CONDITIONS = PropertyLoadUtil.getProperty("actions_conditions");
    public static final String ENTER_CONDITIONS = PropertyLoadUtil.getProperty("enter_conditions");
    public static final String EXIT_CONDITIONS = PropertyLoadUtil.getProperty("exit_conditions");
    public static final String ACTIONS = PropertyLoadUtil.getProperty("actions");
    public static final String MEDIA_CONDITION = PropertyLoadUtil.getProperty("media_condition");
    public static final String ON = PropertyLoadUtil.getProperty("on");
    public static final String OFF = PropertyLoadUtil.getProperty("off");

    //Policy
    public static final String MIN_ENTRIES_PROPERTY_NAME = PropertyLoadUtil.getProperty("min_entries_property_name");
    public static final String MIN_ENTRIES_PROPERTY_PATH = PropertyLoadUtil.getProperty("min_entries_property_path");
    public static final String MIN_ENTRIES_PROPERTY_VALUE_5 = PropertyLoadUtil.getProperty("min_entries_property_value_5");
    public static final String MIN_ENTRIES_PROPERTY_VALUE_10 = PropertyLoadUtil.getProperty("min_entries_property_value_10");
    public static final String MIN_ENTRIES_PROPERTY_VALUE_25 = PropertyLoadUtil.getProperty("min_entries_property_value_25");
    public static final String MIN_ENTRIES_PROPERTY_VALUE_40 = PropertyLoadUtil.getProperty("min_entries_property_value_40");

    public static final String FACEBOOK_PACKAGE_NAME = PropertyLoadUtil.getProperty("facebook_package_name");
    public static final String IT_PACKAGE_NAME = PropertyLoadUtil.getProperty("it_package_name");
    public static final String HTTPS_BLACKLIST_PATH = PropertyLoadUtil.getProperty("https_blacklist_path");
    public final static int RADIO_UP_DELAY = 4 * 1000;
    public final static String OFO_AGGRESSIVENESS = "out_of_order_aggressiveness";
    public static final String SUPPORTED_SERVER_VERSIONS = PropertyLoadUtil.getProperty("oc_supported_server_versions");
    public static final String EXPECTED_SERVER_VERSION = PropertyLoadUtil.getProperty("oc_server_version");
    public static final String CURRENT_SERVER_VERSION = TelnetUtil.findOutServerVersion(TFConstantsIF.mPmsServerIp, TFConstantsIF.DEFAULT_RELAY_PORT);
    public final static String DNS_POLICY_PATH = "@asimov@dns";
    public final static String CACHE_ENABLED = "cache_enabled";
    public final static String DNS_PACKET_TIMEOUT = "dns_packet_timeout";
    public final static String DEFAULT_CACHE_TTL = "default_cache_ttl";

    //TrafficConditions
    public static final String TRAFFIC_PROPERTY_NAME = PropertyLoadUtil.getProperty("traffic_property_name");
    public static final String TRAFFIC_IN_CONDITION_PROPERTY_PATH_TEMPLATE = PropertyLoadUtil.getProperty("traffic_in_condition_property_path_template");
    public static final String TRAFFIC_OUT_CONDITION_PROPERTY_PATH_TEMPLATE = PropertyLoadUtil.getProperty("traffic_out_condition_property_path_template");
    public static final String TRAFFIC_IN_ACTION_PROPERTY_PATH_TEMPLATE = PropertyLoadUtil.getProperty("traffic_in_action_property_path_template");
    public static final String TRAFFIC_OUT_ACTION_PROPERTY_PATH_TEMPLATE = PropertyLoadUtil.getProperty("traffic_out_action_property_path_template");

    public final static Integer MOBILE_NETWORKS_FAILOVER_ATTEMPT_INTERVAL = PropertyLoadUtil.getIntegerProperty("client.openchannel.mobile_networks_failover.attempt_interval");
    public final static Integer MOBILE_NETWORKS_FAILOVER_RETRIES = PropertyLoadUtil.getIntegerProperty("client.openchannel.mobile_networks_failover.retries");

    //Max fake certificate reject number
    public final static Integer MAX_REJECT_NUMBER = PropertyLoadUtil.getIntegerProperty("client.openchannel.fakecert.max_reject_number");
    // Runtime blacklisting period (in hours)
    // If value is set to 0, application will be put to the black list till OC reboot.
    public final static Integer BLACKLIST_PERIOD = PropertyLoadUtil.getIntegerProperty("client.openchannel.fakecert.blacklisting_period");

    public final static String PATH = "/data/misc/openchannel/xtables-multi";
    public final static String IPTABLES_PATH = PATH + " iptables";
    public final static String IP6TABLES_PATH = PATH + " ip6tables";
    public final static String DISPATCHERS_CFG_PATH = "/data/misc/openchannel/dispatchers.cfg";

    public final static int WAIT_FOR_POLICY_UPDATE = 25 * 1000;
    public final static int MINUTE = 60 * 1000;

    public static final int IP4_VERSION = 4;
    public static final int IP6_VERSION = 6;
    public static final int IP_VERSION = PropertyLoadUtil.getIpVersion();
    //    public static final String TEST_RESOURCE_HOST = AsimovTestCase.TEST_RESOURCE_HOST;//PropertyLoadUtil.getTestrunner(PropertyLoadUtil.getProperty("ipv4_testrunner"), PropertyLoadUtil.getProperty("ipv6_testrunner"));
    public static final int DESTINATION_PORT_HTTP = PropertyLoadUtil.getIntegerProperty("dst.port.http");
    public static final int DESTINATION_PORT_HTTPS = PropertyLoadUtil.getIntegerProperty("dst.port.https");
    public static final int DESTINATION_PORT_DNS = PropertyLoadUtil.getIntegerProperty("dst.port.dns");

    // Log level
    public static final int DISPATCHERS_LOG_LEVEL = PropertyLoadUtil.getIntegerProperty("client.dispatchers_default_log_level");

    public final static int UPGRADE_INTERVAL = PropertyLoadUtil.getIntegerProperty("client.upgrade_interval_min");

    public final static int TPROXY = PropertyLoadUtil.getIntegerProperty("client.openchannel.tproxy_enabled");

    //Tcpkill
    public static final String PID_REGEXP = "([a-zA-Z]*)\\ *([0-9]*)\\ *([0-9]*)\\ *.*(%s)";

    public final static String LOGCAT_UTIL_SWAP = PropertyLoadUtil.getProperty("logcat_util.swap");
    public final static String LOGCAT_UTIL_DEBUG = PropertyLoadUtil.getProperty("logcat_util.debug");

    public boolean START_CHECKS = PropertyLoadUtil.getBooleanProperty("start_checks");
    public boolean POLICY_WIFI = PropertyLoadUtil.getBooleanProperty("policy_wifi");

}
