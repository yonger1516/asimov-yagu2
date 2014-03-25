package com.seven.asimov.it.utils.logcat.wrappers;

public class NetlogEntry extends CrcsEntry implements Comparable<NetlogEntry> {

    private static final String TAG = NetlogEntry.class.getSimpleName();

    private int loport;
    private int netport;
    private int client_in;
    private int client_out;
    private int server_in;
    private int server_out;
    private int cache_in;
    private int cache_out;
    private String host;
    private long hostId;
    private String applicationName;
    private long applicationNameId;
    private AppStatusType appStatus;
    private OperationType opType;
    private ProtocolType protocolType;
    private InterfaceType interfaceType;
    private int responseTime;
    private long requestId;
    private int statusCode;
    private String errorCode;
    private String contentType;
    private int headerLength;
    private int contentLength;
    private String responseHash;
    private String analysis;
    private int optimization;
    private int dstPort;
    private String originalIp;
    private String localProtocolStack;
    private String networkProtocolStack;
    private int version;

    public int getLoport() {
        return loport;
    }

    public void setLoport(int loport) {
        this.loport = loport;
    }

    public int getNetport() {
        return netport;
    }

    public void setNetport(int netport) {
        this.netport = netport;
    }

    public int getClient_in() {
        return client_in;
    }

    public void setClient_in(int client_in) {
        this.client_in = client_in;
    }

    public int getClient_out() {
        return client_out;
    }

    public void setClient_out(int client_out) {
        this.client_out = client_out;
    }

    public int getServer_in() {
        return server_in;
    }

    public void setServer_in(int server_in) {
        this.server_in = server_in;
    }

    public int getServer_out() {
        return server_out;
    }

    public void setServer_out(int server_out) {
        this.server_out = server_out;
    }

    public int getCache_in() {
        return cache_in;
    }

    public void setCache_in(int cache_in) {
        this.cache_in = cache_in;
    }

    public int getCache_out() {
        return cache_out;
    }

    public void setCache_out(int cache_out) {
        this.cache_out = cache_out;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public String getResponseHash() {
        return responseHash;
    }

    public void setResponseHash(String responseHash) {
        this.responseHash = responseHash;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public int getOptimization() {
        return optimization;
    }

    public void setOprimization(int optimization) {
        this.optimization = optimization;
    }

    public int getDstPort() {
        return dstPort;
    }

    public void setDstPort(int dstPort) {
        this.dstPort = dstPort;
    }

    public long getHostId() {
        return hostId;
    }

    public void setHostId(long hostId) {
        this.hostId = hostId;
    }

    public long getApplicationNameId() {
        return applicationNameId;
    }

    public void setApplicationNameId(long applicationNameId) {
        this.applicationNameId = applicationNameId;
    }

    public AppStatusType getAppStatus() {
        return appStatus;
    }

    public void setAppStatus(AppStatusType appStatus) {
        this.appStatus = appStatus;
    }

    public int compareTo(NetlogEntry entry) {
        if (getTimestamp() == entry.getTimestamp()) {
            return 0;
        }
        return getTimestamp() > entry.getTimestamp() ? 1 : -1;
    }

    public OperationType getOpType() {
        return opType;
    }

    public void setOpType(OperationType opType) {
        this.opType = opType;
    }

    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    public InterfaceType getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(InterfaceType interfaceType) {
        this.interfaceType = interfaceType;
    }

    public String getOriginalIp() {
        return originalIp;
    }

    public void setOriginalIp(String originalIp) {
        this.originalIp = originalIp;
    }

    public String getLocalProtocolStack() {
        return localProtocolStack;
    }

    public void setLocalProtocolStack(String localProtocolStack) {
        this.localProtocolStack = localProtocolStack;
    }

    public String getNetworkProtocolStack() {
        return networkProtocolStack;
    }

    public void setNetworkProtocolStack(String networkProtocolStack) {
        this.networkProtocolStack = networkProtocolStack;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return super.toString() + " [Id=" + getId() + " LogId=" + getLogId() + " LoPort=" + loport + " NetPort=" + netport  +
                " Time=" + getTimestamp() + " Client_In=" + client_in +
                " Client_Out=" + client_out + " Server_In=" + server_in + " Server_Out=" + server_out +
                " Host=" + host + " AppName=" + applicationName + " NetworkProtocolStack=" + networkProtocolStack +
                " ResponseTime" + responseTime + " RequestId=" + requestId + " StatusCode=" + statusCode +
                " ErrorCode=" + errorCode + " ContentType=" + contentType + " HeaderLength=" + headerLength +
                " ContentLength=" + contentLength + " ResponseHash=" + responseHash +
                " Analisys=" + analysis + " Optimization=" + optimization + " DstPort=" + dstPort + "]";
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}