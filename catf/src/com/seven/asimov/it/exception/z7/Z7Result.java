package com.seven.asimov.it.exception.z7;

public class Z7Result {

    // Z7Result groups
    public static final int Z7_RESULT_GROUP_GENERAL = 0x0001;
    public static final int Z7_RESULT_GROUP_UTIL = 0x0002;
    public static final int Z7_RESULT_GROUP_IO = 0x0003;
    public static final int Z7_RESULT_GROUP_NET = 0x0004;
    public static final int Z7_RESULT_GROUP_TRANSPORT = 0x0005;
    public static final int Z7_RESULT_GROUP_SYNC = 0x0006;
    public static final int Z7_RESULT_GROUP_PERSIST = 0x0007;
    public static final int Z7_RESULT_GROUP_FILE = 0x0008;
    public static final int Z7_RESULT_GROUP_SETTINGS = 0x0009;
    public static final int Z7_RESULT_GROUP_LANG = 0x000a;
    public static final int Z7_RESULT_GROUP_CLIENT = 0x000b;

    // Macro for defining Z7Result errors
    private static int Z7_MAKE_ERROR(int group, int code) {
        return (0xC0000000 | ((group) << 16) | (code));
    }

    // Macro for defining Z7Result status
    private static int Z7_MAKE_STATUS(int group, int code) {
        return (((group) << 16) | (code));
    }

    // Macro for retrieving the group of an error
    public static int Z7_RESULT_GROUP(Z7Result error) {
        return ((error.m_value & 0x3FFF0000) >> 16);
    }

    // Z7Result values
    public static final Z7Result Z7_OK = new Z7Result(0);
    public static final Z7Result Z7_S_NOTHING_TO_DO = new Z7Result(Z7_MAKE_STATUS(Z7_RESULT_GROUP_GENERAL, 0x0001));
    public static final Z7Result Z7_S_NOT_FINISHED = new Z7Result(Z7_MAKE_STATUS(Z7_RESULT_GROUP_GENERAL, 0x0002));
    public static final Z7Result Z7_S_QUEUED = new Z7Result(Z7_MAKE_STATUS(Z7_RESULT_GROUP_GENERAL, 0x0003));
    public static final Z7Result Z7_S_CONSUMED = new Z7Result(Z7_MAKE_STATUS(Z7_RESULT_GROUP_GENERAL, 0x0004));
    public static final Z7Result Z7_S_WAITING = new Z7Result(Z7_MAKE_STATUS(Z7_RESULT_GROUP_GENERAL, 0x0005));
    public static final Z7Result Z7_S_END_OF_STREAM = new Z7Result(Z7_MAKE_STATUS(Z7_RESULT_GROUP_IO, 0x0001));
    public static final Z7Result Z7_S_PARTIAL_SUCCESS = new Z7Result(Z7_MAKE_STATUS(Z7_RESULT_GROUP_IO, 0x0002));

    public static final Z7Result Z7_E_OUT_OF_MEMORY = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0001));
    public static final Z7Result Z7_E_INVALID_PARAMETER = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0002));
    public static final Z7Result Z7_E_UNSUPPORTED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0003));
    public static final Z7Result Z7_E_FAIL = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0004)); // Generic error - should be used very sparingly
    public static final Z7Result Z7_E_NOT_FOUND = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0005));
    public static final Z7Result Z7_E_COULD_NOT_LOCK = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0006));
    public static final Z7Result Z7_E_INVALID_STATE = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0007));
    public static final Z7Result Z7_E_INVALID_DATA = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0008));
    public static final Z7Result Z7_E_CANCELED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0009));
    public static final Z7Result Z7_E_OVERFLOW = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x000a));
    public static final Z7Result Z7_E_DEPENDENCY = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x000b));
    public static final Z7Result Z7_E_NOT_READY = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x000c));
    public static final Z7Result Z7_E_BUFFER_TOO_SMALL = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x000d));
    public static final Z7Result Z7_E_TIMEOUT = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x000e));
    public static final Z7Result Z7_E_ALREADY_EXISTS = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x000f));
    public static final Z7Result Z7_E_TOO_MANY_ITEMS = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0010));
    public static final Z7Result Z7_E_TEMPORARY_FAILURE = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0011));
    public static final Z7Result Z7_E_VERSION_MISMATCH = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0012));
    public static final Z7Result Z7_E_PASSWORD_REQUIRED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0013));
    public static final Z7Result Z7_E_FILE_TOO_LARGE = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0014));
    public static final Z7Result Z7_E_NO_MAILBOX = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0015));
    public static final Z7Result Z7_E_NO_CONTENT_HANDLER = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0016));
    public static final Z7Result Z7_E_SERVICE_IS_EXCLUSIVE = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0017));
    public static final Z7Result Z7_E_OUT_OF_STORAGE = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0018));
    public static final Z7Result Z7_E_ACCOUNT_NAME_EXISTS = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0019));
    public static final Z7Result Z7_E_TOO_MANY_ACCOUNTS = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x001a));
    public static final Z7Result Z7_E_DUPLICATED_ACCOUNT = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x001b));
    public static final Z7Result Z7_E_TOO_MANY_ATTEMPTS = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x001c));
    public static final Z7Result Z7_E_CONTACT_BAD_EMAIL = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x001d));
    public static final Z7Result Z7_E_CONTACT_ALREADY_EXISTS = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x001e));
    public static final Z7Result Z7_E_CONTACT_LIMIT_REACHED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x001f));
    public static final Z7Result Z7_E_ACCESS_DENIED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0020));
    public static final Z7Result Z7_E_RETRY_IMMEDIATELY = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0021)); // Operation failed now but should be retried as soon as possible
    public static final Z7Result Z7_E_MAILBOX_FULL = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0022));
    public static final Z7Result Z7_E_PAUSE_QUIETTIME = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0023));
    public static final Z7Result Z7_E_UNEXPECTED_RESPONSE = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0024));
    public static final Z7Result Z7_E_RESTART_REQUIRED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_GENERAL, 0x0025));

    public static final Z7Result Z7_E_UTILITY_ERROR = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_UTIL, 0x0001));
    public static final Z7Result Z7_E_DATE_ERROR = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_UTIL, 0x0002));
    public static final Z7Result Z7_E_SERIALIZE_FAILURE = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_UTIL, 0x0003));
    public static final Z7Result Z7_E_DESERIALIZE_FAILURE = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_UTIL, 0x0004));
    public static final Z7Result Z7_E_ENCRYPTION_FAILED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_UTIL, 0x0005));
    public static final Z7Result Z7_E_COMPRESSION_FAILED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_UTIL, 0x0006));

    public static final Z7Result Z7_E_END_OF_STREAM = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_IO, 0x0001));
    public static final Z7Result Z7_E_CONNECTION_RESET = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_IO, 0x0002));
    public static final Z7Result Z7_E_BUFFER_FULL = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_IO, 0x0003));
    public static final Z7Result Z7_E_STREAM_FINALIZED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_IO, 0x0004));
    public static final Z7Result Z7_E_NOT_ENOUGH_DATA = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_IO, 0x0005));
    public static final Z7Result Z7_E_BUSY = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_IO, 0x0006));
    public static final Z7Result Z7_E_NO_CONNECTION = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_IO, 0x0007));

    public static final Z7Result Z7_E_INVALID_ADDRESS = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_NET, 0x0001));
    public static final Z7Result Z7_E_SOCKET_CREATION_FAILED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_NET, 0x0002));
    public static final Z7Result Z7_E_SOCKET_BIND_FAILED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_NET, 0x0003));
    public static final Z7Result Z7_E_SOCKET_CONNECT_FAILED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_NET, 0x0004));
    public static final Z7Result Z7_E_SOCKET_NOT_CONNECTED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_NET, 0x0005));
    public static final Z7Result Z7_E_SOCKET_SHUTDOWN_FAILED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_NET, 0x0006));
    public static final Z7Result Z7_E_SOCKET_SEND_FAILED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_NET, 0x0007));
    public static final Z7Result Z7_E_SOCKET_RECEIVE_FAILED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_NET, 0x0008));
    public static final Z7Result Z7_E_SOCKET_CLOSE_FAILED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_NET, 0x0009));
    public static final Z7Result Z7_E_SOCKET_LISTEN_FAILED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_NET, 0x000a));
    public static final Z7Result Z7_E_SOCKET_ACCEPT_FAILED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_NET, 0x000b));
    public static final Z7Result Z7_E_SOCKET_ERROR = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_NET, 0x000c));
    public static final Z7Result Z7_E_NET_LINK_ESTABLISH_ERROR = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_NET, 0x000d));
    public static final Z7Result Z7_E_SOCKET_CALL_ALREADY_PENDING = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_NET, 0x000e));
    public static final Z7Result Z7_E_HOSTNAME_RESOLUTION_FAILED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_NET, 0x000f));
    public static final Z7Result Z7_E_NET_INVALID_ACCESS_POINT = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_NET, 0x0010));
    public static final Z7Result Z7_E_SOCKET_CLOSED_BY_REMOTE_PEER = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_NET, 0x0011));

    public static final Z7Result Z7_E_INVALID_MAGIC_BYTES = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_TRANSPORT, 0x0001));
    public static final Z7Result Z7_E_UNSUPPORTED_CODEC = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_TRANSPORT, 0x0002));
    public static final Z7Result Z7_E_CODEC_CREATION_FAILURE = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_TRANSPORT, 0x0003));
    public static final Z7Result Z7_E_TRANS_UNSUPPORTED_FEATURE = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_TRANSPORT, 0x0004));
    // Z7_E_PACKET_NACKED used to be in this slot, but it should no longer be used.
    // Use Z7_IS_TRANSPORT_NACK(), Z7_GET_TRANSPORT_NACK_REASON_CODE() and
    // Z7_MAKE_TRANSPORT_NACK_ERROR() instead.
    public static final Z7Result Z7_E_PACKET_TIMED_OUT = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_TRANSPORT, 0x0006));
    public static final Z7Result Z7_E_TRANSPORT_NACK_CODE_SPACE = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_TRANSPORT, 0xff00));

    public static final Z7Result Z7_S_SYNC_ITEM_ADDED = new Z7Result(Z7_MAKE_STATUS(Z7_RESULT_GROUP_SYNC, 0x0001));
    public static final Z7Result Z7_S_SYNC_ITEM_UPDATED = new Z7Result(Z7_MAKE_STATUS(Z7_RESULT_GROUP_SYNC, 0x0002));
    public static final Z7Result Z7_E_SYNC_INVALID_UPDATE = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_SYNC, 0x0001));
    public static final Z7Result Z7_E_SYNC_OLD_UDPATE = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_SYNC, 0x0002));
    public static final Z7Result Z7_E_SYNC_SINGLE_ITEM_TOO_LARGE = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_SYNC, 0x0003));
    public static final Z7Result Z7_E_SYNC_CONFLICT_DETECTED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_SYNC, 0x0004));
    public static final Z7Result Z7_E_SYNC_CONFLICT_RESOLVED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_SYNC, 0x0005));

    /**
     * general persistence error
     */
    public static final Z7Result Z7_E_PERSISTENCE_ERROR = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_PERSIST, 0x0001));
    public static final Z7Result Z7_E_PERSISTENCE_CORRUPTED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_PERSIST, 0x0002));
    public static final Z7Result Z7_E_INDEX_REBUILD_REQUIRED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_PERSIST, 0x0003));
    /** Implementation specific error codes. These have higher values than the
     *  more generic error codes to avoid conflicting values in the C++ error
     *  codes */
    /**
     * duplicated org
     */
    public static final Z7Result Z7_E_ORG_DUP_RES = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_PERSIST, 0x0101));
    /**
     * org parent id foreign constraint violation
     */
    public static final Z7Result Z7_E_ORG_PARENT_ID_RES = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_PERSIST, 0x0102));
    /**
     * duplicated enterprise name
     */
    public static final Z7Result Z7_E_ENT_DUP_NAME_RES = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_PERSIST, 0x0103));
    /**
     * enterprise org id foreign constraint violation
     */
    public static final Z7Result Z7_E_ENT_ORG_ID_RES = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_PERSIST, 0x0104));
    /**
     * duplicated enterprise license
     */
    public static final Z7Result Z7_E_ENT_DUP_LICENSE_RES = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_PERSIST, 0x0105));
    /**
     * duplicated admin
     */
    public static final Z7Result Z7_E_ADMIN_DUP_RES = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_PERSIST, 0x0106));
    /**
     * admin org id foreign constraint violation
     */
    public static final Z7Result Z7_E_ADMIN_ORG_ID_RES = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_PERSIST, 0x0107));
    /**
     * duplicated user account
     */
    public static final Z7Result Z7_E_USER_DUP_RES = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_PERSIST, 0x0108));
    /**
     * user enterprise id foreign constraint violation
     */
    public static final Z7Result Z7_E_USER_ENT_ID_RES = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_PERSIST, 0x0109));
    /**
     * duplicated staged account
     */
    public static final Z7Result Z7_E_STAGED_DUP_RES = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_PERSIST, 0x010a));
    /**
     * not enough privilege
     */
    public static final Z7Result Z7_E_NOT_PRIVILEGED_RES = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_PERSIST, 0x010b));

    public static final Z7Result Z7_E_FILE_ERROR = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_FILE, 0x0001));
    public static final Z7Result Z7_E_FILE_EOF = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_FILE, 0x0002));
    public static final Z7Result Z7_E_FILE_NOT_FOUND = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_FILE, 0x0003));
    public static final Z7Result Z7_E_FILE_ALREADY_EXISTS = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_FILE, 0x0004));
    public static final Z7Result Z7_E_FILE_DISK_FULL = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_FILE, 0x0005));
    public static final Z7Result Z7_E_FILE_BAD_FILE_NAME = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_FILE, 0x0006));
    public static final Z7Result Z7_E_FILE_ACCESS_DENIED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_FILE, 0x0007));
    public static final Z7Result Z7_E_FILE_NO_MORE_FILES = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_FILE, 0x0008));
    public static final Z7Result Z7_E_FILE_SHARING_VIOLATION = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_FILE, 0x0009));
    public static final Z7Result Z7_E_FILE_TOO_MANY_OPEN_FILES = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_FILE, 0x000a));
    public static final Z7Result Z7_E_FILE_DEVICE_NOT_READY = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_FILE, 0x000b));
    public static final Z7Result Z7_E_FILE_DEVICE_ERROR = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_FILE, 0x000b));

    // Settings system errors
    public static final Z7Result Z7_E_VALUE_TOO_LOW = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_SETTINGS, 0x0001));
    public static final Z7Result Z7_E_VALUE_TOO_HIGH = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_SETTINGS, 0x0002));
    public static final Z7Result Z7_E_VALUE_REJECTED = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_SETTINGS, 0x0003));
    public static final Z7Result Z7_E_VALUE_DEFAULT_OVERRIDDEN = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_SETTINGS, 0x0004));

    // Language / internatialization errors
    public static final Z7Result Z7_E_LANG_UNSUPPORTED_ENCODING = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_LANG, 0x0001));
    public static final Z7Result Z7_E_LANG_INVALID_CHARACTER_DATA = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_LANG, 0x0002));

    // Client general errors
    public static final Z7Result Z7_E_NEED_TO_WAIT_FOR_CONNECTION = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_CLIENT, 0x0001));
    public static final Z7Result Z7_E_MAX_TASK_DELAY_COUNT = new Z7Result(Z7_MAKE_ERROR(Z7_RESULT_GROUP_CLIENT, 0x0002));

    // implementation
    public int m_value;

    public Z7Result(int value) {
        m_value = value;
    }

    public int compareTo(Object obj) {
        return m_value - ((Z7Result) obj).m_value;
    }

    public String toString() {
        return Integer.toHexString(m_value);
    }

    public int getValue() {
        return m_value;
    }

    /**
     * Macro for checking failure.
     */
    public static final boolean Z7_FAILED(Z7Result x) {
        return ((x.m_value) & 0xC0000000) != 0;
    }

    /**
     * Macro for checking success.
     */
    public static final boolean Z7_SUCCEEDED(Z7Result x) {
        return ((x.m_value) & 0xC0000000) == 0;
    }

    /**
     * Macro for checking if the error code represents a 7TP nack.
     */
    public static final boolean Z7_IS_TRANSPORT_NACK(Z7Result x) {
        return ((Z7_RESULT_GROUP(x) == Z7_RESULT_GROUP_TRANSPORT) && (((x.m_value) & Z7_E_TRANSPORT_NACK_CODE_SPACE.m_value) == Z7_E_TRANSPORT_NACK_CODE_SPACE.m_value));
    }

    /**
     * Macro for getting the reason code from error codes representing 7TP nacks.
     */
    public static final byte Z7_GET_TRANSPORT_NACK_REASON_CODE(Z7Result x) {
        return (byte) ((x.m_value) & 0xff);
    }

    /**
     * Macro for creating error codes representin 7TP nacks.
     */
    public static Z7Result Z7_MAKE_TRANSPORT_NACK_ERROR(int reasonCode) {
        return new Z7Result((Z7_E_TRANSPORT_NACK_CODE_SPACE.m_value | ((reasonCode) & 0xff)));
    }

    public static boolean Z7_IS_TEMPORARY_DATA_ERROR(Z7Result x) {
        return (Z7_FAILED(x) && (Z7_RESULT_GROUP(x) == Z7_RESULT_GROUP_TRANSPORT || x == Z7_E_TEMPORARY_FAILURE || x == Z7_E_NOT_READY));
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Z7Result)) {
            return false;
        } else {
            if (((Z7Result) obj).m_value == this.m_value) {
                return true;
            } else {
                return false;
            }
        }
    }
}

