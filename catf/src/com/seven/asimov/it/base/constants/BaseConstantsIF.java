package com.seven.asimov.it.base.constants;

import android.os.Environment;

public interface BaseConstantsIF {
    String CRLF = "\r\n";
    String GET_IPTABLES_RULES = "iptables -t nat -L";
    String DNS_DISPATCHER_RULE = "udp dpt:domain";
    String HTTP_DISPATCHER_RULE = "tcp dpt:www";
    String PORTS = "ports";
    boolean KEEP_ALIVE = false;
    String VALID_RESPONSE = "tere";
    String INVALIDATED_RESPONSE = "eret";
    int TIMEOUT = 5 * 60 * 1000;
    int SMALL_TIMEOUT = 1 * 60 * 1000;
    int MAX_BODY_SIZE = 5 * 1024;
    int DEFAULT_READ_BUFFER = 16384;
    String SD_CARD = Environment.getExternalStorageDirectory().getPath();
    String TEST_RESOURCE_OWNER = "asimov_it";
}
