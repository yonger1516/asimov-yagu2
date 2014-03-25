package com.seven.asimov.it.base.constants;

/**
 * Z7 Transport Constant values
 * <p/>
 * C++ DEPENDENCY: native/transport/common/src/Z7Transport.h
 * Many of the macro values, such as PROVISION_*, must be the same between these two files.
 */
public interface Z7TransportConstantsIF {

    public static final byte POLLING_PATTERN_COMPONENT_RT = (byte) 0x10;
    public static final int Z7_TRANSPORT_POLICY_CONTROL_FUNCTION_HOSTID = 19;

    // Default relay port
    // TODO: move this to port 735 as the default across the board
    public static final int Z7_TRANSPORT_RELAY_PORT = 7735;
    public static final int Z7_TRANSPORT_RELAY_PORT_INTERNAL = 7736;   // for cluster (inter-relay) communication
    public static final int Z7_TRANSPORT_RELAY_PORT_PROXY = 7737;       // for relay proxying/streaming communication

    // Supported versions
    public static final byte Z7_TRANSPORT_VERSION = 1;
    public static final byte Z7_TRANSPORT_VERSION_MIN = 1;
    public static final byte Z7_TRANSPORT_VERSION_MAX = 1;

    // Magic bytes
    public static final short Z7_TRANSPORT_MAGIC_BYTES = 0x7a37;
    public static final short Z7_TRANSPORT_KEEPALIVE_BYTES = (short) 0xdada; // -23258 ;  // 0xdada as a signed 16bit quantity

    // Opcodes
    public static final byte Z7_TRANSPORT_OPCODE_DATA = 0x00;
    public static final byte Z7_TRANSPORT_OPCODE_ACK = 0x01;
    public static final byte Z7_TRANSPORT_OPCODE_NACK = 0x02;
    public static final byte Z7_TRANSPORT_OPCODE_PING = 0x03;
    public static final byte Z7_TRANSPORT_OPCODE_PACK = 0x04;
    public static final byte Z7_TRANSPORT_OPCODE_STATUS = 0x05;
    public static final byte Z7_TRANSPORT_OPCODE_RESOLVE = 0x06;
    public static final byte Z7_TRANSPORT_OPCODE_SIGNALING = 0x07;        // for inter-relay signaling between
    // internal relay services
    public static final byte Z7_TRANSPORT_MAX_OPCODE = 0x07;


    // Flags for data packet
    public static final short Z7_TRANSPORT_DATA_FLAG_ACK_REQUESTED = 0x0001;
    public static final short Z7_TRANSPORT_DATA_FLAG_CONTAINS_FRAGMENT = 0x0002;
    public static final short Z7_TRANSPORT_DATA_FLAG_DONT_FRAGMENT = 0x0004;
    public static final short Z7_TRANSPORT_DATA_FLAG_ORDERED = 0x0008;
    public static final short Z7_TRANSPORT_DATA_FLAG_REQUEST = 0x0010;
    public static final short Z7_TRANSPORT_DATA_FLAG_RESPONSE = 0x0020;
    public static final short Z7_TRANSPORT_DATA_FLAG_ENABLE_TRIGGER = 0x0040;
    public static final short Z7_TRANSPORT_DATA_FLAG_MORE_PENDING_DATA = 0x0080;   // If this is an RPC response and there is more data
    // still pending.  This requires packet ordering or
    // you could get the final packet before an earlier
    // packet and miss some data.  All additional ordering
    // and data reassembly of multiple response pieces
    // is left to the application layer for now.
    public static final short Z7_TRANSPORT_DATA_FLAG_ENABLE_URGENT_TRIGGER = 0x0100;
    public static final short Z7_TRANSPORT_DATA_FLAG_ALLOW_ROUTING_THROUGH_TRIGGER_CHANNEL = 0x0200;

    // Flags for status packet
    public static final short Z7_TRANSPORT_STATUS_FLAG_ACK_REQUESTED = 0x0001;
    public static final short Z7_TRANSPORT_STATUS_FLAG_PACKET_LIST = 0x0002;
    public static final short Z7_TRANSPORT_STATUS_FLAG_RESPONSE_REQUESTED = 0x0004;
    public static final short Z7_TRANSPORT_STATUS_FLAG_IS_RESPONSE = 0x0008;
    public static final short Z7_TRANSPORT_STATUS_FLAG_DATA_AVAILABLE = 0x0010;
    public static final short Z7_TRANSPORT_STATUS_FLAG_PAUSE = 0x0020;

    // Flags for ping packet
    public static final short Z7_TRANSPORT_PING_FLAG_IS_RESPONSE = 0x0001;
    public static final short Z7_TRANSPORT_PING_FLAG_RECORD_PACKET_ROUTE = 0x0002;
    public static final short Z7_TRANSPORT_PING_FLAG_ENABLE_TRIGGER = 0x0004;
    public static final short Z7_TRANSPORT_PING_FLAG_EBABLE_URGENT_TRIGGER = 0x0008;
    public static final short Z7_TRANSPORT_PING_FLAG_RESPONSE_BY_RELAY = 0x0010;


    // Data priorities
    public static final byte Z7_TRANSPORT_DATA_PRIORITY_LOW = 0;
    public static final byte Z7_TRANSPORT_DATA_PRIORITY_NORMAL = 1;
    public static final byte Z7_TRANSPORT_DATA_PRIORITY_HIGH = 2;

    // Instance id pre-defines
    public static final byte Z7_TRANSPORT_INSTANCE_ID_DEFAULT_ANY = 0;
    public static final byte Z7_TRANSPORT_INSTANCE_ID_ALL = (byte) 255;

    // NOC types
    public static final byte Z7ADDRESS_NOC_TYPE_INFRASTRUCTURE = 0;
    public static final byte Z7ADDRESS_NOC_TYPE_OPERATOR = 2;

    // Codec types
    public static final short Z7CODECTYPE_MASK = 0xe0; // 11100000
    public static final short Z7CODECTYPE_COMPRESSION = 0x20; // type = 1
    public static final short Z7CODECTYPE_ENCRYPTION = 0x40; // type = 2
    public static final short Z7CODECTYPE_DIGEST = 0x60; // type = 3

    // Codecs
    public static final short Z7CODEC_ZLIB_COMPRESSION = Z7CODECTYPE_COMPRESSION;    // scheme = 0
    public static final short Z7CODEC_AES_ENCRYPTION = Z7CODECTYPE_ENCRYPTION;     // scheme = 0
    public static final short Z7CODEC_HMAC_MD5 = Z7CODECTYPE_DIGEST;         // scheme = 0
    public static final short Z7CODEC_HMAC_SHA1 = Z7CODECTYPE_DIGEST | 0x1;   // scheme = 1

    // Relay client types
    public static final short Z7_CLIENTTYPE_RELAY = 0;
    public static final short Z7_CLIENTTYPE_DEVICE = 1;
    public static final short Z7_CLIENTTYPE_CONNECTOR = 2;
    public static final short Z7_CLIENTTYPE_TEMPORARY = 3;
    public static final short Z7_CLIENTTYPE_PREDEFINED = 4;

    // Scope constants for enterprises
    public static final short Z7_ENTERPRISE_SCOPE_WE = 0;
    public static final short Z7_ENTERPRISE_SCOPE_EESE = 1;
    public static final short Z7_ENTERPRISE_SCOPE_ISP = 2;
    public static final short Z7_ENTERPRISE_SCOPE_OWA = 3;
    public static final short Z7_ENTERPRISE_SCOPE_VOICEMAIL = 4;
    public static final short Z7_ENTERPRISE_SCOPE_IM = 5;
    public static final short Z7_ENTERPRISE_SCOPE_PING = 6;
    public static final short Z7_ENTERPRISE_SCOPE_FEED = 7;
    public static final short Z7_ENTERPRISE_SCOPE_EAS = 8;
    public static final short Z7_ENTERPRISE_SCOPE_TH = 9;

    // Sizes
    public static final int Z7_TRANSPORT_MAGIC_SIZE = 2;
    public static final int Z7_TRANSPORT_HEADER_MIN_SIZE = 25;
    public static final int Z7_TRANSPORT_ADDRESS_SIZE = 8;
    public static final int Z7_TRANSPORT_NACK_BODY_SIZE = 11;
    public static final int Z7_TRANSPORT_MAX_PACKET_SIZE = 17 * 1024 * 1024; // 17MB
    public static final int Z7_TRANSPORT_DATA_PACKET_HEADER_SIZE = 4;
    public static final int Z7_TRANSPORT_MAX_DATA_PACKET_SIZE_FOR_SMS_DELIVERY = 60;

    // Transport optional headers
    public static final int Z7_TRANSPORT_HEADER_LOGICAL_CONNECTION_TIMEOUT = 0;

    // Predefined service addresses
    public static final int Z7_TRANSPORT_DIRECTORY_SERVICE_HOSTID = 1;
    public static final int Z7_TRANSPORT_NOTIFICATION_SERVER_HOSTID = 2;
    public static final int Z7_TRANSPORT_MANAGEMENTCONSOLE_HOSTID = 3;
    public static final int Z7_TRANSPORT_CONSUMEREDITION_HOSTID = 4;
    public static final int Z7_TRANSPORT_ENTEPRISEEDITION_HOSTID = 5;
    public static final int Z7_TRANSPORT_MONITOR_SERVICE_HOSTID = 6;
    public static final int Z7_TRANSPORT_RESOURCE_SERVICE_HOSTID = 7; // these are
    public static final int Z7_TRANSPORT_UPGRADE_SERVICE_HOSTID = 7; // the same
    public static final int Z7_TRANSPORT_SYSTEM_EMAIL_SERVICE_HOSTID = 7; // host
    public static final int Z7_TRANSPORT_STATISTICS_SERVICE_HOSTID = 7; //
    public static final int Z7_TRANSPORT_BILLING_SERVICE_HOSTID = 7; //
    public static final int Z7_TRANSPORT_CLIENT_REPORTING_CAPTURE_SERVICE_HOSTID = 7;
    public static final int Z7_TRANSPORT_PROFILE_POLICY_MANAGEMENT_SERVICE_HOSTID = 7; // Profile & Policy Management Service (PMS)
    public static final int Z7_TRANSPORT_DIAGNOSTICS_SERVICE_HOSTID = 8;
    public static final int Z7_TRANSPORT_CONNECTOR_SERVICE_HOSTID = 9;
    public static final int Z7_TRANSPORT_CONNECTOR_SERVICE_SOAPAPI_HOSTID = 10;
    public static final int Z7_TRANSPORT_CONNECTOR_SERVICE_HTTPAPI_HOSTID = 11;
    public static final int Z7_TRANSPORT_CONNECTOR_SERVICE_MSISDNVALIDATIONAPI_HOSTID = 12;
    public static final int Z7_TRANSPORT_STREAM_CONTROL_SERVICE_HOSTID = 13;
    public static final int Z7_TRANSPORT_IM_SERVICE_HOSTID = 14;
    public static final int Z7_TRANSPORT_PING_SERVICE_HOSTID = 15;
    public static final int Z7_TRANSPORT_FEED_SERVICE_HOSTID = 16;
    public static final int Z7_TRANSPORT_TRAFFIC_HARMONIZER_SERVICE_HOSTID = 17;
    public static final int Z7_TRANSPORT_SECURE_STORAGE_SERVICE_HOSTID = 17; // the same host as Traffic Harmonizer
    public static final int Z7_TRANSPORT_LAST_PREDEFINED_SERVICE_HOSTID = 18;

    // Unauthenticated.* methods
    public static final short CMD_UNAUTH_CHALLENGE = 1;
    public static final short CMD_UNAUTH_CHALLENGERESPONSE = 2;
    // Deprecated. CMD_UNAUTH_REGISTRATIONINVALID is used instead (error code set to Z7_ERR_ENDPOINT_NOT_FOUND)
    // public static final short CMD_UNAUTH_REREGISTER                     = 3;
    public static final short CMD_UNAUTH_REGISTERDIFFIEHELLMAN = 4;
    public static final short CMD_UNAUTH_REGISTERDIFFIEHELLMANRESPONSE = 5;
    public static final short CMD_UNAUTH_REGISTERPREDEFINEDSERVICE = 6;
    public static final short CMD_UNAUTH_REGISTERPREDEFINEDSERVICERESPONSE = 7;
    public static final short CMD_UNAUTH_REGISTRATIONINVALID = 8;
    public static final short CMD_UNAUTH_REGISTERRELAY = 9;
    public static final short CMD_UNAUTH_CHALLENGEINVALID = 10;
    public static final short CMD_UNAUTH_CHALLENGEVALID = 11;


    // DirectoryService.* methods
    public static final short CMD_DIRECTORYSERVICE_GETAVAILABLECONNECTORS = 1;
    public static final short CMD_DIRECTORYSERVICE_GETAVAILABLECONNECTORSRESPONSE = 2;
    public static final short CMD_DIRECTORYSERVICE_LOOKUPSERVICE = 3;
    public static final short CMD_DIRECTORYSERVICE_LOOKUPSERVICERESPONSE = 4;
    public static final short CMD_DIRECTORYSERVICE_REGISTERCONNECTOR = 5;
    public static final short CMD_DIRECTORYSERVICE_REGISTERCONNECTORRESPONSE = 6;
    public static final short CMD_DIRECTORYSERVICE_ADDSTAGEDACCOUNT = 7;
    public static final short CMD_DIRECTORYSERVICE_ADDSTAGEDACCOUNTRESPONSE = 8;
    public static final short CMD_DIRECTORYSERVICE_UPDATEACCOUNT = 9;
    public static final short CMD_DIRECTORYSERVICE_UPDATEACCOUNTRESPONSE = 10;
    public static final short CMD_DIRECTORYSERVICE_DELETEACCOUNT = 11;
    public static final short CMD_DIRECTORYSERVICE_DELETEACCOUNTRESPONSE = 12;
    public static final short CMD_DIRECTORYSERVICE_UNREGISTERCONNECTOR = 13;
    public static final short CMD_DIRECTORYSERVICE_UNREGISTERCONNECTORRESPONSE = 14;
    public static final short CMD_DIRECTORYSERVICE_SENDMARKETINGMAIL = 15;
    public static final short CMD_DIRECTORYSERVICE_SENDMARKETINGMAILRESPONSE = 16;
    public static final short CMD_DIRECTORYSERVICE_ADDISPSERVER = 17;
    public static final short CMD_DIRECTORYSERVICE_ADDISPSERVERRESPONSE = 18;
    public static final short CMD_DIRECTORYSERVICE_DELETESTAGEDACCOUNT = 19;
    public static final short CMD_DIRECTORYSERVICE_DELETESTAGEDACCOUNTRESPONSE = 20;
    public static final short CMD_DIRECTORYSERVICE_UPDATESTAGEDACCOUNT = 21;
    public static final short CMD_DIRECTORYSERVICE_UPDATESTAGEDACCOUNTRESPONSE = 22;
    public static final short CMD_DIRECTORYSERVICE_VALIDATEURL = 23;
    public static final short CMD_DIRECTORYSERVICE_VALIDATEURLRESPONSE = 24;
    public static final short CMD_DIRECTORYSERVICE_GETCONNECTORSUBSCRIPTIONINFO = 25;
    public static final short CMD_DIRECTORYSERVICE_GETCONNECTORSUBSCRIPTIONINFORESPONSE = 26;
    public static final short CMD_DIRECTORYSERVICE_RECOVERCLIENTLIST = 27;
    public static final short CMD_DIRECTORYSERVICE_RECOVERCLIENTLISTRESPONSE = 28;
    public static final short CMD_DIRECTORYSERVICE_GETNACKCODEDETAILS = 29;
    public static final short CMD_DIRECTORYSERVICE_GETNACKCODEDETAILSRESPONSE = 30;
    public static final short CMD_DIRECTORYSERVICE_UPDATECONNECTORINFO = 31;
    public static final short CMD_DIRECTORYSERVICE_UPDATECONNECTORINFORESPONSE = 32;
    public static final short CMD_DIRECTORYSERVICE_GETINSTANCEBRANDSETTINGS = 33;
    public static final short CMD_DIRECTORYSERVICE_GETINSTANCEBRANDSETTINGSRESPONSE = 34;
    public static final short CMD_DIRECTORYSERVICE_GETMIGRATIONINFO = 35;
    public static final short CMD_DIRECTORYSERVICE_GETMIGRATIONINFORESPONSE = 36;


    // Provision.* methods
    public static final short CMD_PROVISION_REGISTERDIFFIEHELLMAN = 1;
    public static final short CMD_PROVISION_REGISTERDIFFIEHELLMANRESPONSE = 2;
    public static final short CMD_PROVISION_UPDATEPROFILE = 3;
    public static final short CMD_PROVISION_WIPE = 4;
    public static final short CMD_PROVISION_INVALIDATE_ENDPOINT_INFO = 5;
    // 5, 6
    public static final short CMD_PROVISION_UPDATECONNECTORSUBSCRIPTION = 7;
    public static final short CMD_PROVISION_SETMSISDN = 8;
    public static final short CMD_PROVISION_RENEWENCRYPTIONKEY = 9;
    public static final short CMD_PROVISION_RENEWENCRYPTIONKEYRESPONSE = 10;
    public static final short CMD_PROVISION_VALIDATEENCRYPTIONKEY = 11;
    public static final short CMD_PROVISION_VALIDATEENCRYPTIONKEYRESPONSE = 12;
    public static final short CMD_PROVISION_FULLDATARECOVERY = 13;

    // Authenticate.* methods
    public static final short CMD_AUTHENTICATE_LOGIN = 1;
    public static final short CMD_AUTHENTICATE_LOGINSTATUS = 2;
    public static final short CMD_AUTHENTICATE_LOGOUT = 3;
    public static final short CMD_AUTHENTICATE_RELOGINREQUIRED = 4;

    // DataTransfer.* methods
    public static final short CMD_DATA_TRANSFER_DOWNLOAD_INFO = 1;
    public static final short CMD_DATA_TRANSFER_DOWNLOAD_INFO_RESPONSE = 2;
    public static final short CMD_DATA_TRANSFER_DOWNLOAD_INFO_ERROR = 3;
    public static final short CMD_DATA_TRANSFER_DOWNLOAD_CHUNK = 4;
    public static final short CMD_DATA_TRANSFER_DOWNLOAD_CHUNK_RESPONSE = 5;
    public static final short CMD_DATA_TRANSFER_DOWNLOAD_CHUNK_ERROR = 6;
    public static final short CMD_DATA_TRANSFER_DOWNLOAD_CANCEL = 7;
    public static final short CMD_DATA_TRANSFER_DOWNLOAD_CANCEL_RESPONSE = 8;
    public static final short CMD_DATA_TRANSFER_DOWNLOAD_CANCEL_ERROR = 9;
    public static final short CMD_DATA_TRANSFER_UPLOAD_INFO = 10;
    public static final short CMD_DATA_TRANSFER_UPLOAD_INFO_RESPONSE = 11;
    public static final short CMD_DATA_TRANSFER_UPLOAD_INFO_ERROR = 12;
    public static final short CMD_DATA_TRANSFER_UPLOAD_CHUNK = 13;
    public static final short CMD_DATA_TRANSFER_UPLOAD_CHUNK_RESPONSE = 14;
    public static final short CMD_DATA_TRANSFER_UPLOAD_CHUNK_ERROR = 15;
    public static final short CMD_DATA_TRANSFER_UPLOAD_CANCEL = 16;
    public static final short CMD_DATA_TRANSFER_UPLOAD_CANCEL_RESPONSE = 17;
    public static final short CMD_DATA_TRANSFER_UPLOAD_CANCEL_ERROR = 18;

    // InstantMessaging.* methods
    public static final short CMD_IM_STATE_RESET_NOTIFICATION = 1;
    public static final short CMD_IM_OPERATION_MODE_CHANGE_REQUEST = 8;
    public static final short CMD_IM_OPERATION_MODE_CHANGE_RESPONSE = 9;
    public static final short CMD_IM_GATEWAY_STATUS_NOTIFICATION = 10;
    public static final short CMD_IM_PRESENCE_UPDATE_REQUEST = 11;
    public static final short CMD_IM_PRESENCE_UPDATE_RESPONSE = 12;
    public static final short CMD_IM_INSTANT_MESSAGE_REQUEST = 13;
    public static final short CMD_IM_INSTANT_MESSAGE_RESPONSE = 14;
    public static final short CMD_IM_ROSTER_UPDATE_REQUEST = 15;
    public static final short CMD_IM_ROSTER_UPDATE_RESPONSE = 16;
    public static final short CMD_IM_AVATAR_DATA_REQUEST = 17;
    public static final short CMD_IM_AVATAR_DATA_RESPONSE = 18;
    public static final short CMD_IM_PERSONAL_INFO_UPDATE_REQUEST = 19;
    public static final short CMD_IM_PERSONAL_INFO_UPDATE_RESPONSE = 20;

    // Feed.* methods
    public static final short CMD_FEED_SUBSCRIBE = 1001;
    public static final short CMD_FEED_SEARCH = 1002;
    public static final short CMD_FEED_UNSUBSCRIBE = 1003;

    // Http push cash.* methods
    public static final short CMD_TH_START_POLL = 1001;
    public static final short CMD_TH_STOP_POLL = 1002;
    public static final short CMD_TH_INVALIDATE_CACHE = 1003;
    public static final short CMD_TH_GET_CACHED_DATA = 1004;
    public static final short CMD_TH_STOP_CACHING_DATA = 1005;
    public static final short CMD_TH_MIXED_LIST = 1006;
    public static final short CMD_TH_SET_CLUMPING = 1007;
    public static final short CMD_TH_FLUSH_CLUMPING = 1008;
    public static final short CMD_TH_GENERATE_CERTIFICATE_KEY = 1009;
    public static final short CMD_TH_EXPUNGE_DEVICE_CERTIFICATE = 1010;
    public static final short CMD_TH_OAUTH2_TOKEN_REFRESH = 1011;
    public static final short CMD_TH_ACK_CACHED_DATA = 1012;
    public static final short CMD_TH_RESUME_POLL = 1013;
    public static final short CMD_TH_GENERATE_CREDENTIAL = 1014;


    // bit masks for Z7_KEY_TH_POLL_REQUEST_POLLING_CLASS:
    public static final byte POLLING_PATTERN_COMPONENT_RI = (byte) 0x01;
    public static final byte POLLING_PATTERN_COMPONENT_IT = (byte) 0x02;
    public static final byte POLLING_PATTERN_COMPONENT_TO = (byte) 0x04;
    //ASMV-6323 Added two new polling classes for long poll that can time out
    public static final byte POLLING_PATTERN_COMPONENT_LP = (byte) 0x08;

    // Ping.* methods
    public static final short CMD_PING_REGISTER = 1;
    public static final short CMD_PING_UNREGISTER = 2;
    public static final short CMD_PING_MESSAG = 3;
    public static final short CMD_PING_CLUSTER = 4;

    // HTTP Proxy authentication methods
    public static final short CMD_HTTP_PROXY_AUTHENTICATION_REQUEST = 1;
    public static final short CMD_HTTP_PROXY_AUTHENTICATION_RESPONSE = 2;

    // MonitorService.* methods
    public static final short CMD_MONITORSERVICE_GETRELAYINFO = 1;
    public static final short CMD_MONITORSERVICE_GETRELAYINFORESPONSE = 2;

    // ResourceService.* methods
    public static final short CMD_RESOURCESERVICE_GETRESOURCEINFO = 1;
    public static final short CMD_RESOURCESERVICE_GETRESOURCEINFO_RESPONSE = 2;
    public static final short CMD_RESOURCESERVICE_GETRESOURCEDATA = 3;
    public static final short CMD_RESOURCESERVICE_GETRESOURCEDATA_RESPONSE = 4;

    // UpgradeService.* methods
    public static final short CMD_UPGRADESERVICE_CHECKUPDATEAVAILABILITY = 1;
    public static final short CMD_UPGRADESERVICE_CHECKUPDATEAVAILABILITY_RESPONSE = 2;

    // SystemEmailService.* methods
    public static final short CMD_SYSTEMEMAILSERVICE_SENDSYSTEMEMAIL = 1;
    public static final short CMD_SYSTEMEMAILSERVICE_SENDSYSTEMEMAIL_RESPONSE = 2;
    public static final short CMD_SYSTEMEMAILSERVICE_SENDSYSTEMSMS = 3;
    public static final short CMD_SYSTEMEMAILSERVICE_SENDSYSTEMSMS_RESPONSE = 4;

    // StatisticsService.* methods
    public static final short CMD_STATISTICSSERVICE_SENDSTATISTICS = 1;
    public static final short CMD_STATISTICSSERVICE_SENDSTATISTICS_RESPONSE = 2;

    // BillingService.* methods
    public static final short CMD_BILLINGSERVICE_DISPLAYAOC = 1;
    public static final short CMD_BILLINGSERVICE_DISPLAYAOC_RESPONSE = 2;
    public static final short CMD_BILLINGSERVICE_PURCHASE = 3;
    public static final short CMD_BILLINGSERVICE_PURCHASE_RESPONSE = 4;
    public static final short CMD_BILLINGSERVICE_PURCHASEAOC = 5;
    public static final short CMD_BILLINGSERVICE_PURCHASEAOC_RESPONSE = 6;

    // Client reporting capture service.* methods
    public static final short CMD_CRCS_DUMP_REPORT = 1;
    public static final short CMD_CRCS_DUMP_REPORT_RESPONSE = 2;

    // Profile&PolicyManagementService.* methods
    public static final short CMD_PMS_GET = 1;
    public static final short CMD_PMS_GET_RESPONSE = 2;
    public static final short CMD_PMS_NOTIFY = 3;
    public static final short CMD_PMS_CACHE_INVALIDATE = 4;
    public static final short CMD_PMS_CACHE_INVALIDATE_RESPONSE = 5;
    public static final short CMD_PMS_POLICY_DATA = 6;
    public static final short CMD_PMS_POLICY_MODIFY = 7;
    public static final short CMD_PMS_POLICY_MODIFY_RESPONSE = 8;

    // NOC.* methods
    public static final short CMD_NOC_REFRESHBLACKLIST = 1;
    public static final short CMD_NOC_REMOVEENDPOINT = 2;
    public static final short CMD_NOC_REMOVEENDPOINTCACHE = 3;
    public static final short CMD_NOC_UPDATEENDPOINTCACHE = 4;


    // Connector.* methods
    public static final short CMD_CONNECTOR_ISP_NOTIFICATION = 1;
    public static final short CMD_CONNECTOR_SCHEDULE_WIPE_DEVICE_NOTIFICATION = 2;
    public static final short CMD_CONNECTOR_REMOVE_ENDPOINT_CACHE = 3;

    // NotificationChannel (not related to NotificationService)
    public static final short CMD_NOTIFICATION_CHANNEL_POWER_SAVE_MODE_ENTERED = 1;
    public static final short CMD_NOTIFICATION_CHANNEL_POWER_SAVE_MODE_EXITED = 2;

    // NotificationService.* methods
    public static final short CMD_NOTIFICATIONSERVICE_TRIGGER = 0;     // NF trigger
    public static final short CMD_NOTIFICATIONSERVICE_MESSAGE = 1;     // NF user-message
    public static final short CMD_NOTIFICATIONSERVICE_INTERNAL = 2;     // NF internal message

    // Topology notifications commands
    public static final short CMD_TOPOLOGY_ENDPOINT_CONNECTED = 1;     // Endpoint connected

    // Throttling info commands
    public static final short CMD_THROTTLING_INFO_MESSAGE = 1;     // Throttling info message

    // Secure storage commands
    public static final short CMD_SECURE_STORAGE_PUT = 1;
    public static final short CMD_SECURE_STORAGE_GET = 2;
    public static final short CMD_SECURE_STORAGE_DELETE = 3;

    // Contacts.* methods
    public static final short CMD_CONTACT_SEARCH_BASIC_QUERY_COMMAND_ID = 0;
    public static final short CMD_CONTACT_SEARCH_DETAILS_COMMAND_ID = 1;

    // NOC.* methods
    public static final short CMD_NOC_NODESTATUS_UPDATE = 1;

    // Billing related commands
    public static final int BILLING_DISPLAY_AOC = 0;
    public static final int BILLING_PURCHASE_RESULT = 1;
    public static final int BILLING_PURCHASE_AOC_TEXT = 2;
    public static final int BILLING_DISPLAY_AOC_TEXT = 3;
    public static final int BILLING_ERROR = 4;

    // Provisioning arguments. Shared namespace across Unauthenticated.*, DirectoryService.*, Provision.*
    public static final int PROVISION_ERROR = 1;
    public static final int PROVISION_CHALLENGE = 2;
    public static final int PROVISION_CHALLENGERESPONSE = 3;
    public static final int PROVISION_PUBLICKEY = 6;
    public static final int PROVISION_EPHEMERAL_PUBLICKEY = 7;
    public static final int PROVISION_SEED = 8;
    public static final int PROVISION_SCOPE = 10;
    public static final int PROVISION_NAME = 11;
    public static final int PROVISION_EMAIL = 12;
    public static final int PROVISION_TYPE = 13;
    public static final int PROVISION_LICENSEKEY = 14;
    public static final int PROVISION_PASSWORD = 15;
    public static final int PROVISION_NOC_ID = 16;
    public static final int PROVISION_HOST_ID = 17;
    public static final int PROVISION_MAXDEVICES = 18;
    public static final int PROVISION_PROFILE = 19;
    public static final int PROVISION_USERNAME = 20;
    public static final int PROVISION_SERVICES = 21;
    public static final int PROVISION_ENTERPRISE = 22;
    public static final int PROVISION_INSTANCES = 23;
    public static final int PROVISION_URL = 24;
    public static final int PROVISION_REQUIRESRESPONSE = 25;
    public static final int PROVISION_MSISDN = 26;
    public static final int PROVISION_STAGED_ACCOUNTS = 27;
    public static final int PROVISION_STAGEDID = 28;
    public static final int PROVISION_LOCALE = 29;
    public static final int PROVISION_INSTANCEID = 30;
    public static final int PROVISION_STATUS = 31;
    public static final int PROVISION_PREFERRED_LANGUAGE = 32;
    public static final int PROVISION_ISP_AGGREGATE = 33;
    public static final int PROVISION_ISP_SERVER_ID = 34;

    public static final int PROVISION_INFORMATION = 35;
    public static final int PROVISION_CONNECTORS = 36;
    public static final int PROVISION_CONNECTOR = 37;
    public static final int PROVISION_CONNECTOR_ID = 38;
    public static final int PROVISION_PAGE = 39;
    public static final int PROVISION_ICON_ID = 40;
    public static final int PROVISION_ICON_LAST_MODIFIED = 41;
    public static final int PROVISION_RESOURCES = 42;
    public static final int PROVISION_RESOURCE_ID = 43;
    public static final int PROVISION_RESOURCE_LAST_MODIFIED = 44;
    //public static final int PROVISION_ISP_SERVER_SELECTION_STYLE       = 45;
    public static final int PROVISION_ISP_SERVERS = 46;
    public static final int PROVISION_ISP_SERVER = 47;
    //public static final int PROVISION_CUSTOM_PROVIDERS_ALLOWED         = 48;
    public static final int PROVISION_ISP_BRAND_ID = 49;
    public static final int PROVISION_ISP_TYPE = 50;
    public static final int PROVISION_SILENT_RELOGIN = 51;
    public static final int PROVISION_ISP_SELECTION_VALUE = 52;
    public static final int PROVISION_AVAILABLE_SERVICES = 53;
    public static final int PROVISION_ACCOUNT_ID = 54;
    public static final int PROVISION_SUBSCRIPTION_LIMIT = 55;
    public static final int PROVISION_SUBSCRIPTION_EXPDATE = 56;
    public static final int PROVISION_ISP_CERT_REQUIRED = 57;
    public static final int PROVISION_ISP_ID = 58;
    public static final int PROVISION_SELF_SIGNED_CERT = 59;
    public static final int PROVISION_STAGED_SERVICE_INFO = 60;
    public static final int PROVISION_PERIODIC_REFRESH = 61;
    public static final int PROVISION_BLACKLIST_ENTRIES = 62;
    public static final int PROVISION_SUBCONNECTORS = 63;
    public static final int PROVISION_URL_TOU = 64;
    public static final int PROVISION_URL_PRIVACY = 65;
    public static final int PROVISION_EXCLUSIVE_ACCOUNT = 66;
    public static final int PROVISION_CONTENT_PARAMS = 67;
    public static final int PROVISION_ISP_EMAIL_NEEDED = 68;
    public static final int PROVISION_ISP_EMAIL_DOMAIN = 69;
    public static final int PROVISION_PASSWORD_SET = 70;
    public static final int PROVISION_CLIENT_ADDRESSES = 71;
    public static final int PROVISION_TOKEN = 72;
    public static final int PROVISION_SEQUENCE_ID = 73;
    public static final int PROVISION_ACCOUNT_NICKNAME = 74;
    public static final int PROVISION_SUBSCRIPTION_LIMIT_UPSELL = 75;
    public static final int PROVISION_DATA_RECOVERY_SYNC_CONTENT_ID = 76;
    public static final int PROVISION_ACCOUNT_ENCRYPTION_KEY = 77;
    public static final int PROVISION_PREVIOUSLY_ENABLED_SERVICES = 78;
    public static final int PROVISION_NACK_CODE = 79;
    public static final int PROVISION_NACK_DETAIL = 80;
    public static final int PROVISION_SUPPORT_OTHER = 81;
    //https://jira.seven.com/requests/browse/ZSEVEN-16945
    public static final int PROVISION_IMAP_SELF_SIGNED_CERT_IMPLEMENTED = 82;
    public static final int PROVISION_COMPANY_NAME = 83;
    public static final int PROVISION_STREET_ADDRESS = 84;
    public static final int PROVISION_CONTACT_NAME = 85;
    public static final int PROVISION_ALLOW_FAILURE = 86;
    public static final int PROVISION_REQUESTED_SCOPES = 87;
    public static final int PROVISION_CUSTOM_DATA = 88;
    public static final int PROVISION_PASSWORD_STORE_LOCALLY = 89;
    public static final int PROVISION_SERVER_TIME_UTC = 90;
    public static final int PROVISION_PING_DEVICE_TYPE = 91;
    public static final int PROVISION_PING_DEVICE_ID = 92;
    public static final int PROVISION_PING_UNREGISTER_PENDING_STATUS = 93;
    public static final int PROVISION_PING_UNREGISTER_TASK_ID = 94;
    public static final int PROVISION_SUBSCRIPTION_MDM = 95;
    public static final int PROVISION_VERSION = 96;
    public static final int PROVISION_SUPPORTED_SERVICES = 97;
    public static final int PROVISION_STATUS_DROPDOWN = 98;
    public static final int PROVISION_RESET_CONNECTION = 99;
    public static final int PROVISION_RESOURCE_CONTENT_TYPE = 100;
    //public static final int PROVISION_GROUP_ID = 101;
    //public static final int PROVISION_GROUP_DISPLAYNAME = 102;
    //public static final int PROVISION_GROUP_DEFAULTDOMAIN = 103;   

    public static final int UMA_REQUEST_MSISDN = 1;
    public static final int UMA_REQUEST_IMSI = 2;

    public static final int UMA_ACCOUNT_SCOPE = 1;
    public static final int UMA_OWA_URL = 2;
    public static final int UMA_IS_SELF_CERTIFICATED = 3;
    public static final int UMA_PREMIUM_BRAND_ID = 4;
    public static final int UMA_OTHER_INCOMING_SERVER = 5;
    public static final int UMA_OTHER_INCOMING_PORT = 6;
    public static final int UMA_OTHER_INCOMING_ISSSL = 7;
    public static final int UMA_OTHER_OUTGOING_SERVER = 8;
    public static final int UMA_OTHER_OUTGOING_PORT = 9;
    public static final int UMA_OTHER_OUTGOING_ISSSL = 10;
    public static final int UMA_OTHER_OUTGOING_ISAUTH = 11;
    public static final int UMA_OTHER_OUTGOING_SMTP_USERNAME = 12;
    public static final int UMA_MAIL_ENABLED = 13;
    public static final int UMA_CALENDAR_ENABLED = 14;
    public static final int UMA_CONTACT_ENABLED = 15;
    public static final int UMA_MAIL_SYNC_WINDOW = 16;
    public static final int UMA_MAIL_TRUNCATE_LIMIT = 17;
    public static final int UMA_MAIL_FOLDER_SYNC_MODE = 18;
    public static final int UMA_MAIL_SELECTED_FOLDERS = 19;
    public static final int UMA_CALENDAR_SYNC_WINDOW = 20;
    public static final int UMA_CALENDAR_TRUNCATE_LIMIT = 21;
    public static final int UMA_CONTACT_TRUNCATE_LIMIT = 22;
    public static final int UMA_USER_NAME = 23;
    public static final int UMA_MIGRATION_INFO = 24;

    // Values for GetAvailableConnectors ISP_TYPE
    public static final int ISP_TYPE_NORMAL = 1;
    public static final int ISP_TYPE_CUSTOM_POP = 2;
    public static final int ISP_TYPE_CUSTOM_IMAP = 3;
    public static final int ISP_TYPE_OWA = 4;
    public static final int ISP_TYPE_EX2K7 = 5;
    public static final int ISP_TYPE_OTHER = 6;

    // Values for OWA notifications
    public static final int NOTIFICATION_ACCOUNT_TYPE = 1;
    public static final int NOTIFICATION_SUBID_LIST = 2;
    public static final int NOTIFICATION_FOLDER_ID = 3;

    // Values for Ex2k7 notifications
    public static final int NOTIFICATION_URI = 1;
    public static final int NOTIFICATION_POST_DATA = 2;

    // Values for GetAvailableConnectors PROVISION_ISP_SERVER_SELECTION_STYLE
    public static final int SELECTION_STYLE_NONE = 0;
    public static final int SELECTION_STYLE_SHOW_IN_LIST = 1;
    public static final int SELECTION_STYLE_SEARCH = 2;
    public static final int SELECTION_STYLE_LIST_MAIL_DOMAIN = 3;
    public static final int SELECTION_STYLE_LIST_MAIL_DOMAIN_OTHER = 4;
    public static final int SELECTION_STYLE_SEARCH_DOMAIN = 5;

    // Values for GetNackCodeReason

    public static final int NACK_REASON_NONE = 0;
    public static final int NACK_REASON_SUBSCRIPTION_EXPIRED = 1;
    public static final int NACK_REASON_NUM_OF_ACCOUNTS_EXCEEDED = 2;
    public static final int NACK_REASON_NO_SUBSCRIPTION_FOUND = 3;
    public static final int NACK_REASON_NO_SUB_TRIALS_NOT_ENABLED = 4;
    public static final int NACK_REASON_ACCOUNT_REMOVED = 5;
    public static final int NACK_REASON_UNKNOWN = 6;


    // Values for TH notifications
    public static final byte TH_NOTIFICATION_TYPE_INVALIDATE_WITH_CACHE = 1;
    public static final byte TH_NOTIFICATION_TYPE_INVALIDATE_WITHOUT_CACHE = 2;
    public static final byte TH_NOTIFICATION_TYPE_STOP_POLLING = 3;

    // Resource service constants
    public static final int RESOURCE_FIELD_ERROR_KEY = 0;
    // Fields in the request / response object
    public static final int RESOURCE_FIELD_VERSION_KEY = 1;
    public static final int RESOURCE_FIELD_MAX_RESPONSE_SIZE_KEY = 2;
    public static final int RESOURCE_FIELD_RESOURCES_KEY = 3;
    // Fields in the resource object contained within the request/response
    public static final int RESOURCE_FIELD_UNIQUE_ID_KEY = 101;
    public static final int RESOURCE_FIELD_BASE_ID_KEY = 102;
    public static final int RESOURCE_FIELD_PROVISIONING_ID_KEY = 103;
    public static final int RESOURCE_FIELD_LOCALE_ID_KEY = 104;
    public static final int RESOURCE_FIELD_CONTENT_TYPE_KEY = 105;
    public static final int RESOURCE_FIELD_TIMESTAMP_KEY = 106;
    public static final int RESOURCE_FIELD_DATA_SIZE_KEY = 107;
    public static final int RESOURCE_FIELD_DATA_KEY = 108;

    // Upgrade service constants
    // Version keys
    public static final int CLIENT_UPGRADE_VERSION_MAJOR_KEY = 0;
    public static final int CLIENT_UPGRADE_VERSION_BUILD_MAJOR_KEY = 1;
    public static final int CLIENT_UPGRADE_VERSION_BUILD_MINOR_KEY = 2;
    public static final int CLIENT_UPGRADE_VERSION_BRANCH_IDENTIFIER_KEY = 3;
    // Request
    public static final int UPGRADE_REQUEST_VERSION_KEY = 0;
    public static final int UPGRADE_REQUEST_ENDPOINT_BINARY_VERSION_KEY = 1;
    public static final int UPGRADE_REQUEST_ENDPOINT_BRAND_VERSION_KEY = 2;
    public static final int UPGRADE_REQUEST_ENDPOINT_PLATFORM_UID_KEY = 3;
    public static final int UPGRADE_REQUEST_ENDPOINT_PROVISIONING_ID_KEY = 4;
    public static final int UPGRADE_REQUEST_ENDPOINT_LOCALE_KEY = 5;
    public static final int UPGRADE_REQUEST_SILENT_KEY = 6;
    // Response
    public static final int UPGRADE_RESPONSE_ERROR_KEY = 0;
    public static final int UPGRADE_RESPONSE_AVAILABLE_BINARY_VERSION_KEY = 1;
    public static final int UPGRADE_RESPONSE_AVAILABLE_BRAND_VERSION_KEY = 2;
    public static final int UPGRADE_RESPONSE_UPDATE_TYPE_KEY = 3;
    public static final int UPGRADE_RESPONSE_UPDATE_RESOURCES_KEY = 4;
    public static final int UPGRADE_RESPONSE_TOTAL_SIZE_KEY = 5;

    //Diagnistics constants
    public static final int DIAGNOSTICS_LIST_AVAILABLE_CONTENT = 0;
    public static final int DIAGNOSTICS_LIST_OF_LOGS = 1;
    public static final int DIAGNOSTICS_LOG_FILE = 2;
    public static final int DIAGNOSTICS_SETTINGS = 3;
    public static final int DIAGNOSTICS_DIAGNOSTICS = 4;
    public static final int DIAGNOSTICS_REGISTRY_DUMP = 5;
    public static final int DIAGNOSTICS_FULL_LOG_COLLECTION = 6;

    public static final int DIAGNOSTICS_AVAILABLE_CONTENT_DATA_LIST = 0;
    public static final int DIAGNOSTICS_LIST_OF_LOGS_FILE_LIST = 0;
    public static final int DIAGNOSTICS_RESPONSE_FILE_NAME = 0;
    public static final int DIAGNOSTICS_RESPONSE_FILE_SIZE = 1;
    public static final int DIAGNOSTICS_RESPONSE_FILE_DATE = 2;
    public static final int DIAGNOSTICS_RESPONSE_CONTENT_TYPE = 3;
    public static final int DIAGNOSTICS_REQUEST_TYPE = 0;
    public static final int DIAGNOSTICS_REQUEST_FILENAME = 1;


    // NACK packet reason codes
    public static final byte Z7_TRANSPORT_NACK_UNSUPPORTED_PROTOCOL_VERSION = (byte) 0x00;
    public static final byte Z7_TRANSPORT_NACK_INVALID_OPCODE = (byte) 0x01;
    public static final byte Z7_TRANSPORT_NACK_DESTINATION_HOST_NOT_CONNECTED = (byte) 0x10;
    public static final byte Z7_TRANSPORT_NACK_INVALID_SOURCE = (byte) 0x11;
    public static final byte Z7_TRANSPORT_NACK_INVALID_DESTINATION = (byte) 0x12;
    public static final byte Z7_TRANSPORT_NACK_NO_ROUTE_TO_DESTINATION = (byte) 0x13;
    public static final byte Z7_TRANSPORT_NACK_RECEIVE_BUFFER_FULL = (byte) 0x14;
    public static final byte Z7_TRANSPORT_NACK_NETWORK_CONGESTION = (byte) 0x15;
    public static final byte Z7_TRANSPORT_NACK_PACKET_TOO_LARGE = (byte) 0x16;
    public static final byte Z7_TRANSPORT_NACK_ROUTE_TO_DESTINATION_UNAVAILABLE = (byte) 0x17;
    public static final byte Z7_TRANSPORT_NACK_CONTENT_HANDLER_UNAVAILABLE = (byte) 0x18;
    public static final byte Z7_TRANSPORT_NACK_RELOGIN_REQUIRED = (byte) 0x19;
    public static final byte Z7_TRANSPORT_NACK_UNKNOWN_ENDPOINT = (byte) 0x1A;
    public static final byte Z7_TRANSPORT_NACK_PAUSED = (byte) 0x1B;
    public static final byte Z7_TRANSPORT_NACK_UNSUPPORTED_COMMAND = (byte) 0x1C;
    public static final byte Z7_TRANSPORT_NACK_FRAGMENT_REASSEMBLY_UNSUPPORTED = (byte) 0x20;
    public static final byte Z7_TRANSPORT_NACK_FRAGMENT_TIMEOUT = (byte) 0x21;
    public static final byte Z7_TRANSPORT_NACK_INSUFFICIENT_MEMORY_FOR_REASSEMBLY = (byte) 0x22;
    public static final byte Z7_TRANSPORT_NACK_INVALID_FRAGMENT_OFFSET = (byte) 0x23;
    public static final byte Z7_TRANSPORT_NACK_UNSUPPORTED_CODEC_IN_CHAIN = (byte) 0x30;
    public static final byte Z7_TRANSPORT_NACK_CODEC_PROCESSING_ERROR = (byte) 0x31;
    public static final byte Z7_TRANSPORT_NACK_INVALID_CODEC_DESCRIPTOR = (byte) 0x32;
    public static final byte Z7_TRANSPORT_NACK_PACKET_OUT_OF_ORDER = (byte) 0x40;
    public static final byte Z7_TRANSPORT_NACK_PACKET_MAX_SIZE_REACHED = (byte) 0x41;
    public static final byte Z7_TRANSPORT_NACK_ENDPOINT_UPGRADE_REQUIRED = (byte) 0x42;
    public static final byte Z7_TRANSPORT_NACK_ROUTING_NOT_ALLOWED = (byte) 0x43;
    public static final byte Z7_TRANSPORT_NACK_ACK_ROUTING_NOT_ALLOWED = (byte) 0x44;
    public static final byte Z7_TRANSPORT_NACK_KEY_RENEWAL_REQUIRED = (byte) 0x45;
    public static final byte Z7_TRANSPORT_NACK_SERVER_BUSY = (byte) 0x46;
    public static final byte Z7_TRANSPORT_NACK_SILENT_RELOGIN_REQUIRED = (byte) 0x47;
    public static final byte Z7_TRANSPORT_NACK_THROTTLING_EXCEEDED = (byte) 0x48;
    public static final byte Z7_TRANSPORT_NACK_UNSUPPORTED_CLIENT_VERSION = (byte) 0x49;

    public static final byte Z7_TRANSPORT_NACK_APPLICATION_PACKET_ERROR = (byte) 0xFF;
}
