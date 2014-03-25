package com.seven.asimov.test.tool.utils;

import com.seven.asimov.it.IntegrationTestRunnerGa;
import com.seven.asimov.it.base.NetStat;
import com.seven.asimov.it.utils.OCUtil;
import com.seven.asimov.it.utils.ShellUtil;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.date.DateUtil;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class SmokeTcpDumpUtil {
    private IntegrationTestRunnerGa intent = new IntegrationTestRunnerGa();
    public static String newline = System.getProperty("line.separator");
    private boolean isHttpsModeEnabled = false;
    private Process process;
    private String tcpdumpPath = intent.getTcpdumpPath();

    private static Map<Integer, Integer> mapNetworkStatistic = new HashMap<Integer, Integer>();
    private long startTime = 0L;

    private static List<SmokeTcpDumpUtil.Record> logArchiv = new ArrayList<Record>(1000);

    private List<Record> log = new ArrayList<Record>();
    private List<Record> logUdp = new ArrayList<Record>();
    private List<Record> logTcp = new ArrayList<Record>();
    private List<Record> resolveHostName = new ArrayList<Record>();
    private List<Record> tls = new ArrayList<Record>();
    private Map<TLS_MESSAGE_TYPE, String> tlsMessages = new LinkedHashMap<TLS_MESSAGE_TYPE, String>();
    private Map<TLS_HANDSHAKE_MESSAGE, String> tlsHandShakeMessages = new LinkedHashMap<TLS_HANDSHAKE_MESSAGE, String>();

    public static final int MILLSECONDS_IN_SECOND = 1000;
    public static final int MILLSECONDS_IN_MINUTE = 60 * MILLSECONDS_IN_SECOND;
    public static final int MILLSECONDS_IN_HOUR = 60 * MILLSECONDS_IN_MINUTE;
    public static final int MILLSECONDS_IN_DAY = 24 * MILLSECONDS_IN_HOUR;

    private static final int DNS = 53;
    private static int HTTP = 80;
    private static int HTTP_DIRECT = 8099;
    private static int HTTPS = 443;
    private static final int Z7TP = 7735;
    private static final int SSL_SPLIT_PROXY_PORT = 7443;
    private static Integer ASIMOV_PORT;

    public final String TN_IP = "208.87.205.219";

    protected final List<String> zipProxyHosts = new ArrayList<String>() {{
        add("208.87.206.211");  // c004
        add("208.87.206.216");  // c009
        add("10.2.3.150");      // demo
        add("10.2.1.165");      // han
    }};

    protected final List<Integer> zipProxyPorts = new ArrayList<Integer>() {{
        add(7888);              // c004, c009
        add(8765);              // han
        add(17888);             // dev
    }};

    private String zipProxyIp;
    private int zipProxyPort;

    public int getZipProxyPort() {
        return zipProxyPort;
    }

    public final String LOCAL_HOST = "10.0.2.15";
    public final String ASIMOV_IP = "127.0.0.1";

    private boolean isZipProxyEnabled = false;

    // Tcpdump flags
    public static final String SYN_FLAG = "S";
    public static final String RESET_FLAG = "R";
    public static final String FYN_FLAG = "F";

    private int tcpdumpProcessId;
    private String testName;

    public enum Flag {
        NONE, SYN, FIN, RST
    }

    public enum Protocol {
        UNKNOWN, TCP, UDP
    }

    public enum ProtocolUP {
        UNKNOWN, Z7TP, HTTP, DNS
    }

    public enum TLS_MESSAGE_TYPE {
        CHANGE_CIPHER_SPEC, ALERT, HANDSHAKE, APPLICATION_DATA // 14,15,16,17
    }

    public enum TLS_HANDSHAKE_MESSAGE {
        HELLO_REQUEST, CLIENT_HELO, SERVER_HELO, CERTIFICATE, SERVER_KEY_EXCHANGE, CERTIFICATE_REQUEST, SERVER_HELLO_DONE, CERTIFICATE_VERIFY, CLIENT_KEY_EXCHANGE, FINISHED
    }

    public static int getAsimovPort() {
        if (ASIMOV_PORT == null) {
            SmokeOpenChannelHelperUtil.ClientType oc = SmokeOpenChannelHelperUtil.getCurrentClientType();
            if (oc != null) {
                switch (oc) {
                    case GA:
                        ASIMOV_PORT = 8080;
                        // HTTPS = SSL_SPLIT_PROXY_PORT;
                        break;
                    case LA:
                        ASIMOV_PORT = 8072;
                        break;
                }
            } else {
                // Don't set
                System.out.println("Default OC client - LA");
                return 8072;
            }
        }
        return ASIMOV_PORT;
    }

    /**
     * Creates tcpdump
     *
     * @param testName - not null string
     */
    public SmokeTcpDumpUtil(String testName) {
        this.testName = testName;
        logArchiv.clear();
        initTlsMessages();
    }

    private void initTlsMessages() {
        tlsMessages.put(TLS_MESSAGE_TYPE.CHANGE_CIPHER_SPEC, "ChangeCipherSpec");
        tlsMessages.put(TLS_MESSAGE_TYPE.ALERT, "Alert");
        tlsMessages.put(TLS_MESSAGE_TYPE.APPLICATION_DATA, "Application data");
        tlsMessages.put(TLS_MESSAGE_TYPE.HANDSHAKE, "Handshake");
        tlsHandShakeMessages.put(TLS_HANDSHAKE_MESSAGE.HELLO_REQUEST, "HelloRequest");
        tlsHandShakeMessages.put(TLS_HANDSHAKE_MESSAGE.CLIENT_HELO, "ClientHello");
        tlsHandShakeMessages.put(TLS_HANDSHAKE_MESSAGE.SERVER_HELO, "ServerHello");
        tlsHandShakeMessages.put(TLS_HANDSHAKE_MESSAGE.CERTIFICATE, "Certificate");
        tlsHandShakeMessages.put(TLS_HANDSHAKE_MESSAGE.SERVER_KEY_EXCHANGE, "ServerKeyExchange");
        tlsHandShakeMessages.put(TLS_HANDSHAKE_MESSAGE.CERTIFICATE_REQUEST, "CertificateRequest");
        tlsHandShakeMessages.put(TLS_HANDSHAKE_MESSAGE.SERVER_HELLO_DONE, "ServerHeloDone");
        tlsHandShakeMessages.put(TLS_HANDSHAKE_MESSAGE.CERTIFICATE_VERIFY, "CertificateVerify");
        tlsHandShakeMessages.put(TLS_HANDSHAKE_MESSAGE.CLIENT_KEY_EXCHANGE, "ClientKeyExchange");
        tlsHandShakeMessages.put(TLS_HANDSHAKE_MESSAGE.FINISHED, "Finished");
    }

    /**
     * Start tcpdump process with output in log file
     */
    public void start() {
        start(false);
    }

    public void start(boolean isHttpsModeEnabled) {
//        Log.e("SmokeTcpDump", "Start TcpDump");
        reset();
        try {
            this.isHttpsModeEnabled = isHttpsModeEnabled;
            List<Integer> alreadyRunningTcpdumpIds = getRunningTcpdumpProcIds();
            String startCommand = Z7TestUtil.getTCPDUMP_PATH() + " -nvv" + (isHttpsModeEnabled ? "XX" : "")
                    + "tt -i any not tcp port 5555 >" + Z7TestUtil.pcapPath + "pcaps/" + testName + ".pcap &";
//            Log.e("SmokeTcpDump", startCommand);
            String[] a = {"su", "-c", startCommand};
            process = Runtime.getRuntime().exec(a);
            process.waitFor();
            TestUtil.sleep(3000);
            List<Integer> newRunningTcpdumpIds = getRunningTcpdumpProcIds();
            newRunningTcpdumpIds.removeAll(alreadyRunningTcpdumpIds);
            tcpdumpProcessId = newRunningTcpdumpIds.isEmpty() ? 0 : newRunningTcpdumpIds.get(0);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * Kill tcpdump process and parse tcpdump's log file.
     * <p/>
     * You Must set <b>output</b> - true if you want to call other tcpdump api, because tcp dump needs to parse written
     * pcap file. False must be used in finally statement to kill tcp dump.
     */
    public void stop(boolean output) {
//        Log.e("SmokeTcpDump", "Stop TcpDump");
        killTcpDump();
        if (output) {
            parseTcpDumpOutput();
            writeToFile();
            logArchiv.addAll(log);
        }
    }

    /**
     * Resets tcm dump state.
     */
    private void reset() {
        log.clear();
        logUdp.clear();
        logTcp.clear();
        resolveHostName.clear();
        tls.clear();
        tcpdumpProcessId = 0;
    }

    private void writeToFile() {
//        Log.e("SmokeTcpDump", "Start write file");
//        Log.e("SmokeTcpDump", SmokeOpenChannelHelper.getTcpDumpTargetDir());
        File root = new File(OCUtil.getTcpDumpTargetDir());
        boolean success = false;
        if (!root.exists()) {
            success = root.mkdir();
            if (!success) {
                System.out.print("Failed to create " + root.getAbsolutePath());
                return;
            }
        }
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(root.getAbsolutePath() + "/" + testName + ".log", true));
//            Log.e("SmokeTcpDump", "Log size = " + log.size());
            for (Record r : log) {
                out.write(DateUtil.format(new Date(r.getTime())));
                out.write(" ");
                out.write(r.getFromIp());
                out.write(" ");
                out.write(Integer.toString(r.getFromPort()));
                out.write(" ");
                out.write(r.getToIp());
                out.write(" ");
                out.write(Integer.toString(r.getToPort()));
                out.write(" ");
                out.write(r.getFlag());
                out.write(" ");
                out.write(r.getProtocol().toString());
                out.write(" ");
                out.write(Integer.toString(r.getLength()));
                out.write(" ");
                out.write(Integer.toString(r.getLengthBody()));
                out.write(newline);
                if (r.tlsType != null) {
                    out.write((r.tlsType).toString());
                    if (r.tlsHandshakeType != null) {
                        out.write((r.tlsHandshakeType).toString());
                    }
                    out.write(newline);
                }
            }
            out.write("-------------------------------------------------------\n");
        } catch (IOException e) {

        } finally {
            if (out != null) {
                try {
                    out.close();
                    out = null;
                } catch (IOException e) {
                }
            }
        }
    }

    private List<Integer> getRunningTcpdumpProcIds() {
        List<Integer> result = new ArrayList<Integer>();
        try {
            String[] a = {"su", "-c", "ps"};
            process = Runtime.getRuntime().exec(a);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = br.readLine();
            while (line != null) {
                if (line.contains(tcpdumpPath)) {
                    result.add(Integer.valueOf(line.trim().split("\\s+")[1]));
                }
                line = br.readLine();
            }
        } catch (Exception e) {
        }
        return result;
    }

    private void killTcpDump() {
        if (tcpdumpProcessId != 0) {
            ShellUtil.kill(tcpdumpProcessId);
            tcpdumpProcessId = 0;
        }
    }

    public void setZipProxyEnabled(boolean enabled) {
        isZipProxyEnabled = enabled;
    }

    private void parseTcpDumpOutput() {
//        Log.e("SmokeTcpDump", "Start parse TcpDump");
        BufferedReader br = null;
        try {
            Pattern p = Pattern.compile("[0-9]{10}.[0-9]{6} IP");
//            Log.e("SmokeTcpDump", pcapLocation + testName + ".pcap");
            InputStream is = new FileInputStream(new File(Z7TestUtil.pcapPath + "pcaps/" + testName + ".pcap"));
            br = new BufferedReader(new InputStreamReader(is));
            String line;
            if (!isHttpsModeEnabled) {
                while ((line = br.readLine()) != null) {
                    if (!p.matcher(line).find()) {
                        continue;
                    }
                    parseRecord(line + " " + br.readLine());
                }
            } else {
                line = br.readLine();
                while (line != null) {
                    Record rec = parseRecord(line + " " + br.readLine());
                    line = parseBody(br, rec);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {

                }
            }
        }
    }

    private String parseBody(BufferedReader br, Record rec) {
        String line = "";
        try {
            line = br.readLine();
            StringBuilder sb = new StringBuilder();
            Pattern p = Pattern.compile("[0-9|a|b|c|d|e|f]x[0-9|a|b|c|d|e|f]*: *(([0-9|a|b|c|d|e|f])* )*");
            while ((p.matcher(line)).find()) {
                sb.append(line);
                line = br.readLine();
            }
            List<TLS_MESSAGE_TYPE> tlsVal = new ArrayList<SmokeTcpDumpUtil.TLS_MESSAGE_TYPE>();
            List<TLS_HANDSHAKE_MESSAGE> tlsHsVal = new ArrayList<SmokeTcpDumpUtil.TLS_HANDSHAKE_MESSAGE>();
            String body = sb.toString();
            body = body.replaceAll("[^ ]{5,}", "");
            p = Pattern.compile("1[4|5|6|7] *03 *01");
            if ((p.matcher(body)).find()) {
                findTlsMessageTypes(body, TLS_MESSAGE_TYPE.CHANGE_CIPHER_SPEC, 14, tlsVal);
                findTlsMessageTypes(body, TLS_MESSAGE_TYPE.ALERT, 15, tlsVal);
                findTlsMessageTypes(body, TLS_MESSAGE_TYPE.HANDSHAKE, 16, tlsVal);
                findTlsMessageTypes(body, TLS_MESSAGE_TYPE.APPLICATION_DATA, 17, tlsVal);
                findTlsHSMessageTypes(body, tlsHsVal);
                rec.setTlsType(tlsVal);
                rec.setTlsHandshakeType(tlsHsVal);
                tls.add(rec);
            }
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                e.printStackTrace();
            }
        }

        return line;
    }

    private void findTlsMessageTypes(String body, TLS_MESSAGE_TYPE type, int messageIndex, List<TLS_MESSAGE_TYPE> tlsVal) {
        String pattern = "%d *03 *01";

        Pattern p = Pattern.compile(String.format(pattern, messageIndex));
        if (p.matcher(body).find()) {
            tlsVal.add(type);
        }
    }

    private void findTlsHSMessageTypes(String body, List<TLS_HANDSHAKE_MESSAGE> tlsVal) {
        String pattern = "16 ?03 ?01";
        String hsPattern = "16 *03 *01 *[0-9|a|b|c|d|e|f][0-9|a|b|c|d|e|f] *[0-9|a|b|c|d|e|f][0-9|a|b|c|d|e|f] *%s";
        Pattern p = Pattern.compile(pattern);
        if (p.matcher(body).find()) {
            String[] hsIdexes = {"00", "01", "02", "0b", "0c", "0d", "0e", "0f", "10", "14"};
            int i = 0;
            for (TLS_HANDSHAKE_MESSAGE mType : tlsHandShakeMessages.keySet()) {
                p = Pattern.compile(String.format(hsPattern, hsIdexes[i]));
                i++;
                if (p.matcher(body).find()) {
                    tlsVal.add(mType);
                }
            }
        }
    }

    private Protocol getProtocol(String proto) {
        if (proto.equals("TCP")) {
            return Protocol.TCP;
        } else if (proto.equals("UDP")) {
            return Protocol.UDP;
        } else {
            return Protocol.UNKNOWN;
        }
    }

    private Record parseRecord(String record) {
//        Log.e("SmokeTcpDump", "Start Parse Record");
        Record rec = new Record();
        try {
            String[] s = record.trim().replace(",", "").split("\\s+");
            if (record.contains("IP")) {
                if (!(s[0].contains(">>>") || s[0].contains("Your-IP") || s[0].contains("Client-IP"))) {
                    rec.setTime(parseTimeField(s[0]));
                    rec.setFromIp(getIp(s[17]));
                    rec.setFromPort(Integer.valueOf(getPort(s[17])));
                    rec.setToIp(getIp(s[19]));
                    rec.setToPort(Integer.valueOf(getPort(s[19])));
                    rec.setProtocol(getProtocol(s[13]));
                    // s[14] - protocol id 6 for TCP, 17 for UDP
                    rec.setFlag(rec.getProtocol().equals(Protocol.UDP) || s.length < 21 ? "" : s[21].replace("]", "")
                            .replace("[", "").replace(".", ""));
                    rec.setLength(Integer.parseInt(s[16].replace(")", "")));
                    rec.setLengthBody((rec.getProtocol() == Protocol.UDP) ? 0 : Integer.parseInt(s[s.length - 1]));
                    if (!isZipProxyEnabled && (zipProxyPorts.contains(rec.fromPort) || zipProxyPorts.contains(rec.toPort))) {
                        isZipProxyEnabled = true;
                        HTTP = zipProxyPort = zipProxyPorts.contains(rec.fromPort) ? rec.fromPort : rec.toPort;
                        if (zipProxyHosts.contains(rec.fromIp) || zipProxyHosts.contains(rec.toIp)) {
                            zipProxyIp = zipProxyHosts.contains(rec.fromIp) ? rec.fromIp : rec.toIp;
                        }
                    }

                    if ((isHttpsModeEnabled || (rec.getProtocol() == Protocol.UDP) || (rec.getFromPort() == Z7TP)
                            || (rec.toPort == Z7TP) || SYN_FLAG.equalsIgnoreCase(rec.getFlag())
                            || FYN_FLAG.equalsIgnoreCase(rec.getFlag()) || (RESET_FLAG.equalsIgnoreCase(rec.getFlag())))
                            && (rec.getToPort() != 5555 && rec.getFromPort() != 5555)) {

                        log.add(rec);
                        if (rec.getProtocol().equals(Protocol.TCP)) {
                            logTcp.add(rec);
                        } else {
                            if (rec.getProtocol().equals(Protocol.UDP)) {
                                if (rec.getToPort() == DNS && record.contains("A?")) {
                                    rec.setHost(s[s.length - 2]);
                                    resolveHostName.add(rec);
                                }
                                logUdp.add(rec);
                            }
                        }
                    }

                    if (startTime == 0L) {
                        startTime = rec.getTime();
                    }

                    int time = (int) (rec.getTime() - startTime) / 5000;
                    int value = 0;

                    try {
                        if (mapNetworkStatistic.containsKey(time)) {
                            value = mapNetworkStatistic.get(time);
                        }

                        mapNetworkStatistic.put(time, value + rec.getLengthBody());
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("parsing error " + record);
        }
        return rec;
    }

    public static Map<Integer, Integer> getMapNetworkStatistic() {
        return mapNetworkStatistic;
    }

    public Set<String> getResolvedHosts() {
        Set<String> result = new HashSet<String>();
        for (Record rec : resolveHostName) {
            result.add(rec.getHost());
        }
        return result;
    }

    private String getIp(String s) {
        return s.substring(0, s.lastIndexOf('.'));
    }

    private String getPort(String s) {
        return s.substring(s.lastIndexOf('.') + 1).replace(":", "");
    }

    /**
     * Return amount of HTTP sockets, that were opened.
     */

    public Set<NetStat.SocketInfo> getHttpSockets() {
        return getSocketsOnPort(0, Long.MAX_VALUE, HTTP);
    }

    public Set<NetStat.SocketInfo> getSocketsOnPort(int port) {
        return getSocketsOnPort(0, Long.MAX_VALUE, port);
    }

    /**
     * Return amount of HTTP sockets, that were opened in specified time period.
     */
    public Set<NetStat.SocketInfo> getSocketsOnPort(long startTimeStamp, long stopTimeStamp, int port) {
        port = changePortsIfProxy(port);
        Set<NetStat.SocketInfo> result = new HashSet<NetStat.SocketInfo>();
        for (Record rec : logTcp) {
            long ts = rec.getTime();
            if (startTimeStamp <= ts && ts <= stopTimeStamp) {
                if (rec.getToPort() == port) {
                    NetStat.SocketInfo recSocket = new NetStat.SocketInfo(rec.getFromIp(), rec.getFromPort(),
                            rec.getToIp(), rec.getToPort());
                    if (!result.contains(recSocket)) {
                        result.add(recSocket);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Return amount of DNS requests.
     */
    public int getDnsRequestsCount() {
        return getDnsRequestsCount(0, Long.MAX_VALUE);
    }

    /**
     * Return amount of DNS requests.
     */
    public int getDnsRequestsCount(long startTimeStamp, long stopTimeStamp) {
        int result = 0;
        for (Record curRecord : logUdp) {
            long ts = curRecord.getTime();
            if (startTimeStamp <= ts && ts <= stopTimeStamp) {
                if (curRecord.getToPort() == 53) {
                    result++;
                }
            }
        }
        return result;
    }

    /**
     * Return amount of DNS responses.
     */
    public int getDnsResponsesCount() {
        int result = 0;
        for (Record curRecord : logUdp) {
            if (curRecord.getFromPort() == DNS) {
                result++;
            }
        }
        return result;
    }

    /**
     * Return amount of HTTP sessions.
     */

    public int getHttpSessionsCount() {
        return getHttpSessionsCount(0, Long.MAX_VALUE);
    }

    public int getHttpSessionsCount(long startTimeStamp, long stopTimeStamp) {
        return getHttpSessionsCount(startTimeStamp, stopTimeStamp, HTTP);
    }

    public int getHttpSessionsCount(long startTimeStamp, long stopTimeStamp, int port) {
        int sucCon = 0;
        try {

            Set<Record> synRecords = new HashSet<Record>();

            for (Record rec : logTcp) {
                long ts = rec.getTime();
                if (startTimeStamp <= ts && ts <= stopTimeStamp) {
                    if (SYN_FLAG.equalsIgnoreCase(rec.getFlag())
                            && (rec.getToPort() == port || rec.getFromPort() == port)) {
                        synRecords.add(rec);
                    }
                }
            }
            Set<Record> copySynRecords = new HashSet<Record>(synRecords);
            for (Record rec : copySynRecords) {
                for (Record prevRec : synRecords) {
                    if (prevRec.getFromIp().equalsIgnoreCase(rec.getToIp()) && prevRec.getFromPort() == rec.getToPort()
                            && prevRec.getToIp().equalsIgnoreCase(rec.getFromIp())
                            && prevRec.getToPort() == rec.getFromPort()) {
                        sucCon++;
                        synRecords.remove(rec);
                        synRecords.remove(prevRec);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sucCon;
    }

    public int getSessionsCount(String ip, int port) {
        return getSessionsCount(ip, port, 0, Long.MAX_VALUE);
    }

    /**
     * Return amount of successful sessions on some IP and port
     */
    public int getSessionsCount(String ip, int port, long startTimeStamp, long stopTimeStamp) {
        ip = isZipProxyEnabled ? zipProxyIp : ip;
        port = changePortsIfProxy(port);
        if (ip == null) {
            System.out.println("ip is null");
            return 0;
        }
        int sucCon = 0;
        try {
            Set<Record> synRecords = new HashSet<Record>();
            for (Record rec : logTcp) {
                long ts = rec.getTime();
                if (startTimeStamp <= ts && ts <= stopTimeStamp) {
                    if (SYN_FLAG.equalsIgnoreCase(rec.getFlag())
                            && (rec.getToPort() == port || rec.getFromPort() == port)
                            && (rec.getFromIp().equalsIgnoreCase(ip) || ip.equalsIgnoreCase(rec.getToIp()))) {
                        synRecords.add(rec);
                    }
                }
            }
            Set<Record> copySynRecords = new HashSet<Record>(synRecords);
            for (Record rec : copySynRecords) {
                for (Record prevRec : synRecords) {
                    if (prevRec.getFromIp().equalsIgnoreCase(rec.getToIp()) && prevRec.getFromPort() == rec.getToPort()
                            && prevRec.getToIp().equalsIgnoreCase(rec.getFromIp())
                            && prevRec.getToPort() == rec.getFromPort()) {
                        sucCon++;
                        synRecords.remove(rec);
                        synRecords.remove(prevRec);
                        break;
                    }
                }
            }
        } catch (Exception e) {
        }
        return sucCon;
    }

    private int changePortsIfProxy(int port) {
        return isZipProxyEnabled && (port == 80 || port == 443) ? zipProxyPort : port;
    }

    /**
     * Check if there was session through OC from local port of sInfo socket
     */
    public boolean wasSessionThroughOC(NetStat.SocketInfo sInfo) {
        boolean result = false;
        try {
            Set<Record> synRecords = new HashSet<Record>();

            for (Record rec : logTcp) {
                if (SYN_FLAG.equalsIgnoreCase(rec.getFlag())
                        && ((rec.getToPort() == getAsimovPort() && ASIMOV_IP.equalsIgnoreCase(rec.getToIp())) || (rec
                        .getToPort() == sInfo.getLocalPort() && rec.getFromPort() == sInfo.getForeignPort() && rec
                        .getFromIp().equalsIgnoreCase(sInfo.getForeignAdress())))
                        && (rec.getFromPort() == sInfo.getLocalPort() || rec.getToPort() == sInfo.getLocalPort())) {
                    synRecords.add(rec);
                }
            }

            if (synRecords.size() == 2) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Return amount of successful HTTPS sessions from all tcpdump's logfile
     */
    public int getHttpsSessionsCount() {
        int sucCon = 0;
        try {
            getAsimovPort();
            Set<Record> synRecords = new HashSet<Record>();
            for (Record rec : logTcp) {
                if (SYN_FLAG.equalsIgnoreCase(rec.getFlag())
                        && (rec.getToPort() == HTTPS || rec.getFromPort() == HTTPS
                        || rec.getToPort() == SSL_SPLIT_PROXY_PORT || rec.getFromPort() == SSL_SPLIT_PROXY_PORT)) {
                    synRecords.add(rec);
                }
            }
            Set<Record> copySynRecords = new HashSet<Record>(synRecords);
            for (Record rec : copySynRecords) {
                for (Record prevRec : synRecords) {
                    if (prevRec.getFromIp().equalsIgnoreCase(rec.getToIp()) && prevRec.getFromPort() == rec.getToPort()
                            && prevRec.getToIp().equalsIgnoreCase(rec.getFromIp())
                            && prevRec.getToPort() == rec.getFromPort()) {
                        sucCon++;
                        synRecords.remove(rec);
                        synRecords.remove(prevRec);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sucCon;
    }

    public int getHttpsSessionsCount(long startTimeStamp, long stopTimeStamp) {
        int sucCon = 0;
        try {
            getAsimovPort();
            Set<Record> synRecords = new HashSet<Record>();
            for (Record rec : logTcp) {
                long ts = rec.getTime();
                if (startTimeStamp <= ts && ts <= stopTimeStamp) {
                    if (SYN_FLAG.equalsIgnoreCase(rec.getFlag())
                            && (rec.getToPort() == HTTPS || rec.getFromPort() == HTTPS || rec.getToPort() == SSL_SPLIT_PROXY_PORT || rec.getFromPort() == SSL_SPLIT_PROXY_PORT)) {
                        synRecords.add(rec);
                    }
                }
            }
            Set<Record> copySynRecords = new HashSet<Record>(synRecords);
            for (Record rec : copySynRecords) {
                for (Record prevRec : synRecords) {
                    if (prevRec.getFromIp().equalsIgnoreCase(rec.getToIp()) && prevRec.getFromPort() == rec.getToPort() && prevRec.getToIp().equalsIgnoreCase(rec.getFromIp())
                            && prevRec.getToPort() == rec.getFromPort()) {
                        sucCon++;
                        synRecords.remove(rec);
                        synRecords.remove(prevRec);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sucCon;
    }

    /**
     * Return amount of successful Z7TP sessions from all tcpdump's logfile
     */
    public int getZ7TPSessionsCount() {
        int sucCon = 0;
        Record prevRec = null;
        for (Record rec : logTcp) {
            if (!SYN_FLAG.equalsIgnoreCase(rec.getFlag()) || (rec.getToPort() != Z7TP && rec.getFromPort() != Z7TP)) {
                continue;
            }
            if (prevRec == null) {
                prevRec = rec;
                continue;
            } else {
                if (prevRec.getFromIp().equalsIgnoreCase(rec.getToIp()) && prevRec.getFromPort() == rec.getToPort()
                        && prevRec.getToIp().equalsIgnoreCase(rec.getFromIp())
                        && prevRec.getToPort() == rec.getFromPort()) {
                    if (prevRec.getToPort() == Z7TP || rec.getToPort() == Z7TP) {
                        sucCon++;
                    }
                }
                prevRec = rec;
            }
        }
        return sucCon;
    }

    private long parseTimeField(String s) {
        String time = s.substring(0, 10) + s.substring(11, 14);
        return Long.valueOf(time);
    }

    /**
     * Indicates that there was some activity on standart DNS port (53)
     */
    public boolean wasDnsActivity() {
        return wasDnsActivity(0, Long.MAX_VALUE);
    }

    /**
     * Indicates that there were some activity on standart DNS port (53)
     */
    public boolean wasDnsActivity(long startTimeStamp, long stopTimeStamp) {
        boolean result = false;
        for (Record rec : logUdp) {
            long ts = rec.getTime();
            if (startTimeStamp <= ts && ts <= stopTimeStamp) {
                result = true;
                break;
            }
        }
        return result;

    }

    /**
     * Indicates that there were some activity on any TCP ports
     */
    public boolean wasTcpActivity(long startTimeStamp, long stopTimeStamp) {
        boolean result = false;
        for (Record rec : logTcp) {
            long ts = rec.getTime();
            if (startTimeStamp <= ts && ts <= stopTimeStamp) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Indicates that there was some activity on standart HTTP port (80)
     */
    public boolean wasHttpActivity() {
        return wasHttpActivity(0, Long.MAX_VALUE);
    }

    /**
     * Indicates that there was some activity on standart HTTP port (80)
     */
    public boolean wasHttpActivity(long startTimeStamp, long stopTimeStamp) {
        Set<Integer> asimovPorts = new HashSet<Integer>();
        for (Record rec : logTcp) {
            if (rec.getFromPort() == getAsimovPort()) {
                asimovPorts.add(rec.getToPort());
            }
            if (rec.getToPort() == getAsimovPort()) {
                asimovPorts.add(rec.getFromPort());
            }
        }
        boolean result = false;
        for (Record rec : logTcp) {
            long ts = rec.getTime();
            if (startTimeStamp <= ts && ts <= stopTimeStamp) {
                if ((rec.getFromPort() == HTTP && !asimovPorts.contains(rec.getToPort()))
                        || (rec.getToPort() == HTTP && !asimovPorts.contains(rec.getFromPort()))) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Indicates that there was some activity on TH
     */
    public boolean wasZ7TPActivity() {
        return wasZ7TPActivity(0, Long.MAX_VALUE);
    }

    /**
     * Indicates that there was some activity on TH
     */
    public boolean wasZ7TPActivity(long startTimeStamp, long stopTimeStamp) {
        boolean result = false;
        for (Record rec : logTcp) {
            long ts = rec.getTime();
            if (startTimeStamp <= ts && ts <= stopTimeStamp) {
                if (rec.getFromPort() == Z7TP || rec.getToPort() == Z7TP) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Indicates that there was some activity on standart HTTPS port (443)
     */
    public boolean wasHttpsActivity() {
        return wasHttpsActivity(0, Long.MAX_VALUE);
    }

    /**
     * Indicates that there was some activity standart on HTTPS port (443)
     */
    public boolean wasHttpsActivity(long startTimeStamp, long stopTimeStamp) {
        Set<Integer> asimovPorts = new HashSet<Integer>();
        for (Record rec : logTcp) {
            if (rec.getFromPort() == getAsimovPort()) {
                asimovPorts.add(rec.getToPort());
            }
            if (rec.getToPort() == getAsimovPort()) {
                asimovPorts.add(rec.getFromPort());
            }
        }
        boolean result = false;
        for (Record rec : logTcp) {
            long ts = rec.getTime();
            if (startTimeStamp <= ts && ts <= stopTimeStamp) {
                if ((rec.getFromPort() == HTTPS && !asimovPorts.contains(rec.getToPort()))
                        || (rec.getToPort() == HTTPS && !asimovPorts.contains(rec.getFromPort()))) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * returns number of bytes send with UDP protocol
     */
    public int getUdpTraffic() {
        int result = 0;
        for (Record rec : logUdp) {
            result += rec.getLength();
        }
        return result;
    }

    /**
     * returns number of bytes send with TCP protocol
     */
    public int getTcpTraffic() {
        int result = 0;
        for (Record rec : logTcp) {
            result += rec.getLength();
        }
        return result;
    }

    public int getZ7TPtrafficIn() {
        return getZ7TPtrafficIn(0, Long.MAX_VALUE);
    }

    public int getZ7TPtrafficIn(long startTimeStamp, long stopTimeStamp) {
        int result = 0;
        for (Record rec : logTcp) {
            try {
                long ts = rec.getTime();
                if (startTimeStamp <= ts && ts <= stopTimeStamp) {
                    if (rec.getFromPort() == Z7TP) {
                        result += rec.getLengthBody();
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }
        return result;
    }

    public int getZ7TPtrafficOut() {
        return getZ7TPtrafficOut(0, Long.MAX_VALUE);
    }

    public int getZ7TPtrafficOut(long startTimeStamp, long stopTimeStamp) {
        int result = 0;

        for (Record rec : logTcp) {
            try {
                long ts = rec.getTime();
                if (startTimeStamp <= ts && ts <= stopTimeStamp) {
                    if (rec.getToPort() == Z7TP) {
                        result += rec.getLengthBody();
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }
        return result;
    }

    public int getClosedHttpSessionsCount() {
        int result = 0;
        for (Record rec : logTcp) {
            if (RESET_FLAG.equalsIgnoreCase(rec.getFlag())) {
                result++;
            }
        }
        return result;
    }

    public static List<SmokeTcpDumpUtil.Record> getLogArchiv() {
        return logArchiv;
    }

    public static class Record {
        private long time;
        private String fromIp;
        private int fromPort;
        private String toIp;
        private int toPort;
        private String flag;
        private Protocol protocol;
        private ProtocolUP protocolUP;
        private int length;
        private int lengthBody;
        private String host;
        private List<TLS_MESSAGE_TYPE> tlsType;
        private List<TLS_HANDSHAKE_MESSAGE> tlsHandshakeType;

        @Override
        public boolean equals(Object o) {
            boolean result = false;
            if (o instanceof Record) {
                Record obj = (Record) o;
                if (this.getFlag().equalsIgnoreCase(obj.getFlag()) && this.getProtocol().equals(obj.getProtocol())
                        && this.getToPort() == obj.getToPort() && this.getFromPort() == obj.getFromPort()
                        && this.getFromIp().equalsIgnoreCase(obj.getFromIp())
                        && this.getToIp().equalsIgnoreCase(obj.getToIp())) {
                    result = true;
                }
            }
            return result;
        }

        @Override
        public int hashCode() {
            String[] locadd = getFromIp().trim().split("\\.");
            String[] foradd = getToIp().trim().split("\\.");
            return getFromPort() + getToPort() + Integer.valueOf(locadd[0]) + Integer.valueOf(locadd[1])
                    + Integer.valueOf(locadd[2]) + Integer.valueOf(locadd[3]) + Integer.valueOf(foradd[0])
                    + Integer.valueOf(foradd[1]) + Integer.valueOf(foradd[2]) + Integer.valueOf(foradd[3]);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Record : time=");
            sb.append(Long.toString(time));
            sb.append(" fromIP=");
            sb.append(fromIp);
            sb.append(" fromPort=");
            sb.append(String.valueOf(fromPort));
            sb.append(" toIp=");
            sb.append(toIp);
            sb.append(" toPort=");
            sb.append(String.valueOf(toPort));
            sb.append(" flag=");
            sb.append(flag);
            sb.append(" protocol=");
            sb.append(protocol);
            sb.append(" length=");
            sb.append(String.valueOf(length));
            sb.append(" lengthBody=");
            sb.append(String.valueOf(lengthBody));
            if (tlsType != null) {
                sb.append(tlsType);
            }
            if (tlsHandshakeType != null) {
                sb.append(tlsHandshakeType);
            }
            return sb.toString();
        }

        public ProtocolUP getProtocolUP() {
            return protocolUP;
        }

        public void setProtocolUP(ProtocolUP protocolUP) {
            this.protocolUP = protocolUP;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getFromIp() {
            return fromIp;
        }

        public void setFromIp(String fromIp) {
            this.fromIp = fromIp;
        }

        public int getFromPort() {
            return fromPort;
        }

        public void setFromPort(int fromPort) {
            this.fromPort = fromPort;
        }

        public String getToIp() {
            return toIp;
        }

        public void setToIp(String toIp) {
            this.toIp = toIp;
        }

        public int getToPort() {
            return toPort;
        }

        public void setToPort(int toPort) {
            this.toPort = toPort;
        }

        public String getFlag() {
            return flag;
        }

        public void setFlag(String flag) {
            this.flag = flag;
        }

        public Protocol getProtocol() {
            return protocol;
        }

        public void setProtocol(Protocol protocol) {
            this.protocol = protocol;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public int getLengthBody() {
            return lengthBody;
        }

        public void setLengthBody(int lengthBody) {
            this.lengthBody = lengthBody;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public List<TLS_HANDSHAKE_MESSAGE> getTlsHandshakeType() {
            return tlsHandshakeType;
        }

        public void setTlsHandshakeType(List<TLS_HANDSHAKE_MESSAGE> tlsHandshakeType) {
            this.tlsHandshakeType = tlsHandshakeType;
        }

        public List<TLS_MESSAGE_TYPE> getTlsType() {
            return tlsType;
        }

        public void setTlsType(List<TLS_MESSAGE_TYPE> tlsType) {
            this.tlsType = tlsType;
        }
    }
}