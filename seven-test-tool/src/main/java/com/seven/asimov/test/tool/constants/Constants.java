package com.seven.asimov.test.tool.constants;

/**
 * Constants handler.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public interface Constants {

    /**
     * End Of File = -1.
     */
    int EOF = -1;
    /**
     * End Of File for chunked = -2.
     */
    int CHUNKED_EOF = -2;

    /**
     * Default socket timeout = 24 hours
     */
    int DEFAULT_SOCKET_TIMEOUT = 86400000;

    /**
     * HTTP port.
     */
    int PORT_HTTP = 80;
    /**
     * HTTPS port.
     */
    int PORT_HTTPS = 443;

    /**
     * Amount of milliseconds in 1 second.
     */
    int MILL_IN_SEC = 1000;
    /**
     * Amount of seconds in 1 minute.
     */
    int SEC_IN_MIN = 60;

    // HTTP constants
    /**
     * "\n".
     */
    byte LF = 10;
    /**
     * "\r".
     */
    byte CR = 13;
    /**
     * HTTP new line "\n".
     */
    String HTTP_NEW_LINE_CHROME = "\n";
    /**
     * HTTP new line "\r\n".
     */
    String HTTP_NEW_LINE = "\r\n";
    /**
     * HTTP double new line "\r\n\r\n".
     */
    String HTTP_DOUBLE_NEW_LINE = "\r\n\r\n";
    /**
     * HTTP prefix = "http://".
     */
    String HTTP_PREFIX = "http://";
    /**
     * HTTPS prefix= "https://".
     */
    String HTTPS_PREFIX = "https://";
    // HTTP Status code strings
    /**
     * HTTP Status: 100 Continue.
     */
    String HTTP_STATUS_STRING_100 = "100 Continue";
    /**
     * HTTP Status: 100 Continue.
     */
    int HTTP_STATUS_100 = 100;

    /**
     * HTTP Status: 101 Switching Protocols.
     */
    String HTTP_STATUS_STRING_101 = "101 Switching Protocols";
    /**
     * HTTP Status: 101 Switching Protocols.
     */
    int HTTP_STATUS_101 = 101;

    /**
     * HTTP Status: 200 OK.
     */
    String HTTP_STATUS_STRING_200 = "200 OK";
    /**
     * HTTP Status: 200 OK.
     */
    int HTTP_STATUS_200 = 200;

    /**
     * HTTP Status: 201 Created.
     */
    String HTTP_STATUS_STRING_201 = "201 Created";
    /**
     * HTTP Status: 201 Created.
     */
    int HTTP_STATUS_201 = 201;

    /**
     * HTTP Status: 202 Accepted.
     */
    String HTTP_STATUS_STRING_202 = "202 Accepted";
    /**
     * HTTP Status: 202 Accepted.
     */
    int HTTP_STATUS_202 = 202;

    /**
     * HTTP Status: 203 Non-Authoritative Information.
     */
    String HTTP_STATUS_STRING_203 = "203 Non-Authoritative Information";
    /**
     * HTTP Status: 203 Non-Authoritative Information.
     */
    int HTTP_STATUS_203 = 203;

    /**
     * HTTP Status: 204 No Content.
     */
    String HTTP_STATUS_STRING_204 = "204 No Content";
    /**
     * HTTP Status: 204 No Content.
     */
    int HTTP_STATUS_204 = 204;

    /**
     * HTTP Status: 205 Reset Content.
     */
    String HTTP_STATUS_STRING_205 = "205 Reset Content";
    /**
     * HTTP Status: 205 Reset Content.
     */
    int HTTP_STATUS_205 = 205;

    /**
     * HTTP Status: 206 Partial Content.
     */
    String HTTP_STATUS_STRING_206 = "206 Partial Content";
    /**
     * HTTP Status: 206 Partial Content.
     */
    int HTTP_STATUS_206 = 206;

    /**
     * HTTP Status: 300 Multiple Choices.
     */
    String HTTP_STATUS_STRING_300 = "300 Multiple Choices";
    /**
     * HTTP Status: 300 Multiple Choices.
     */
    int HTTP_STATUS_300 = 300;

    /**
     * HTTP Status: 301 Moved Permanently.
     */
    String HTTP_STATUS_STRING_301 = "301 Moved Permanently";
    /**
     * HTTP Status: 301 Moved Permanently.
     */
    int HTTP_STATUS_301 = 301;

    /**
     * HTTP Status: 302 Found.
     */
    String HTTP_STATUS_STRING_302 = "302 Found";
    /**
     * HTTP Status: 302 Found.
     */
    int HTTP_STATUS_302 = 302;

    /**
     * HTTP Status: 303 See Other.
     */
    String HTTP_STATUS_STRING_303 = "303 See Other";
    /**
     * HTTP Status: 303 See Other.
     */
    int HTTP_STATUS_303 = 303;

    /**
     * HTTP Status: 304 Not Modified.
     */
    String HTTP_STATUS_STRING_304 = "304 Not Modified";
    /**
     * HTTP Status: 304 Not Modified.
     */
    int HTTP_STATUS_304 = 304;

    /**
     * HTTP Status: 305 Use Proxy.
     */
    String HTTP_STATUS_STRING_305 = "305 Use Proxy";
    /**
     * HTTP Status: 305 Use Proxy.
     */
    int HTTP_STATUS_305 = 305;

    /**
     * HTTP Status: 307 Temporary Redirect.
     */
    String HTTP_STATUS_STRING_307 = "307 Temporary Redirect";
    /**
     * HTTP Status: 307 Temporary Redirect.
     */
    int HTTP_STATUS_307 = 307;

    /**
     * HTTP Status: 400 Bad Request.
     */
    String HTTP_STATUS_STRING_400 = "400 Bad Request";
    /**
     * HTTP Status: 400 Bad Request.
     */
    int HTTP_STATUS_400 = 400;

    /**
     * HTTP Status: 401 Unauthorized.
     */
    String HTTP_STATUS_STRING_401 = "401 Unauthorized";
    /**
     * HTTP Status: 401 Unauthorized.
     */
    int HTTP_STATUS_401 = 401;

    /**
     * HTTP Status: 402 Payment Required.
     */
    String HTTP_STATUS_STRING_402 = "402 Payment Required";
    /**
     * HTTP Status: 402 Payment Required.
     */
    int HTTP_STATUS_402 = 402;

    /**
     * HTTP Status: 403 Forbidden.
     */
    String HTTP_STATUS_STRING_403 = "403 Forbidden";
    /**
     * HTTP Status: 403 Forbidden.
     */
    int HTTP_STATUS_403 = 403;

    /**
     * HTTP Status: 404 Not Found.
     */
    String HTTP_STATUS_STRING_404 = "404 Not Found";
    /**
     * HTTP Status: 404 Not Found.
     */
    int HTTP_STATUS_404 = 404;

    /**
     * HTTP Status: 405 Method Not Allowed.
     */
    String HTTP_STATUS_STRING_405 = "405 Method Not Allowed";
    /**
     * HTTP Status: 405 Method Not Allowed.
     */
    int HTTP_STATUS_405 = 405;

    /**
     * HTTP Status: 406 Not Acceptable.
     */
    String HTTP_STATUS_STRING_406 = "406 Not Acceptable";
    /**
     * HTTP Status: 406 Not Acceptable.
     */
    int HTTP_STATUS_406 = 406;

    /**
     * HTTP Status: 407 Proxy Authentication Required.
     */
    String HTTP_STATUS_STRING_407 = "407 Proxy Authentication Required";
    /**
     * HTTP Status: 407 Proxy Authentication Required.
     */
    int HTTP_STATUS_407 = 407;

    /**
     * HTTP Status: 408 Request Timeout.
     */
    String HTTP_STATUS_STRING_408 = "408 Request Timeout";
    /**
     * HTTP Status: 408 Request Timeout.
     */
    int HTTP_STATUS_408 = 408;

    /**
     * HTTP Status: 409 Conflict.
     */
    String HTTP_STATUS_STRING_409 = "409 Conflict";
    /**
     * HTTP Status: 409 Conflict.
     */
    int HTTP_STATUS_409 = 409;

    /**
     * HTTP Status: 410 Gone.
     */
    String HTTP_STATUS_STRING_410 = "410 Gone";
    /**
     * HTTP Status: 410 Gone.
     */
    int HTTP_STATUS_410 = 410;

    /**
     * HTTP Status: 411 Length Required.
     */
    String HTTP_STATUS_STRING_411 = "411 Length Required";
    /**
     * HTTP Status: 411 Length Required.
     */
    int HTTP_STATUS_411 = 411;

    /**
     * HTTP Status: 412 Precondition Failed.
     */
    String HTTP_STATUS_STRING_412 = "412 Precondition Failed";
    /**
     * HTTP Status: 412 Precondition Failed.
     */
    int HTTP_STATUS_412 = 412;

    /**
     * HTTP Status: 413 Request Entity Too Large.
     */
    String HTTP_STATUS_STRING_413 = "413 Request Entity Too Large";
    /**
     * HTTP Status: 413 Request Entity Too Large.
     */
    int HTTP_STATUS_413 = 413;

    /**
     * HTTP Status: 414 Request-URI Too Long.
     */
    String HTTP_STATUS_STRING_414 = "414 Request-URI Too Long";
    /**
     * HTTP Status: 414 Request-URI Too Long.
     */
    int HTTP_STATUS_414 = 414;

    /**
     * HTTP Status: 415 Unsupported Media Type.
     */
    String HTTP_STATUS_STRING_415 = "415 Unsupported Media Type";
    /**
     * HTTP Status: 415 Unsupported Media Type.
     */
    int HTTP_STATUS_415 = 415;

    /**
     * HTTP Status: 416 Requested Range Not Satisfiable.
     */
    String HTTP_STATUS_STRING_416 = "416 Requested Range Not Satisfiable";
    /**
     * HTTP Status: 416 Requested Range Not Satisfiable.
     */
    int HTTP_STATUS_416 = 416;

    /**
     * HTTP Status: 417 Expectation Failed.
     */
    String HTTP_STATUS_STRING_417 = "417 Expectation Failed";
    /**
     * HTTP Status: 417 Expectation Failed.
     */
    int HTTP_STATUS_417 = 417;

    /**
     * HTTP Status: 500 Internal Server Error.
     */
    String HTTP_STATUS_STRING_500 = "500 Internal Server Error";
    /**
     * HTTP Status: 500 Internal Server Error.
     */
    int HTTP_STATUS_500 = 500;

    /**
     * HTTP Status: 501 Not Implemented.
     */
    String HTTP_STATUS_STRING_501 = "501 Not Implemented";
    /**
     * HTTP Status: 501 Not Implemented.
     */
    int HTTP_STATUS_501 = 501;

    /**
     * HTTP Status: 502 Bad Gateway.
     */
    String HTTP_STATUS_STRING_502 = "502 Bad Gateway";
    /**
     * HTTP Status: 502 Bad Gateway.
     */
    int HTTP_STATUS_502 = 502;

    /**
     * HTTP Status: 503 Service Unavailable.
     */
    String HTTP_STATUS_STRING_503 = "503 Service Unavailable";
    /**
     * HTTP Status: 503 Service Unavailable.
     */
    int HTTP_STATUS_503 = 503;

    /**
     * HTTP Status: 504 Gateway Timeout.
     */
    String HTTP_STATUS_STRING_504 = "504 Gateway Timeout";
    /**
     * HTTP Status: 504 Gateway Timeout.
     */
    int HTTP_STATUS_504 = 504;

    /**
     * HTTP Status: 505 HTTP Version Not Supported.
     */
    String HTTP_STATUS_STRING_505 = "505 HTTP Version Not Supported";
    /**
     * HTTP Status: 505 HTTP Version Not Supported.
     */
    int HTTP_STATUS_505 = 505;

    // Protocols
    /**
     * HTTP 1.0 protocol = "HTTP/1.0".
     */
    String HTTP_1_0_PROTOCOL = "HTTP/1.0";
    /**
     * HTTP 1.1 protocol = "HTTP/1.1".
     */
    String HTTP_1_1_PROTOCOL = "HTTP/1.1";
    /**
     * SIP protocol.
     */
    String SIP_PROTOCOL = "SIP/2.0";
    /**
     * 7Test tool protocol.
     */
    String OC_PROTOCOL = "OC/1.0";

    /**
     * HTTP white space.
     */
    String WHITESPACE = " ";

    // Logging
    /**
     * Will not log in logger responses than over this size.
     */
    int SERVER_RESPONSE_LOGGING_LIMIT = 10240;

    /**
     * Http methods (GET, POST, etc).
     */
    public enum HTTP_METHODS {
        GET {
            public String toString() {
                return "GET";
            }
        },
        POST {
            public String toString() {
                return "POST";
            }
        },
        PUT {
            public String toString() {
                return "PUT";
            }
        },
        DELETE {
            public String toString() {
                return "DELETE";
            }
        },
        HEAD {
            public String toString() {
                return "HEAD";
            }
        },
        TRACE {
            public String toString() {
                return "TRACE";
            }
        },
        OPTIONS {
            public String toString() {
                return "OPTIONS";
            }
        }
    }
}
