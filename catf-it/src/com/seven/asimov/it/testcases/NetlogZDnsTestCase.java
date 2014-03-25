package com.seven.asimov.it.testcases;

import android.util.Log;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.base.TimeInfoTransaction;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.NetlogTask;
import com.seven.asimov.it.utils.logcat.wrappers.NetlogEntry;
import com.seven.asimov.it.utils.tcpdump.DnsSession;
import com.seven.asimov.it.utils.tcpdump.Packet;
import com.seven.asimov.it.utils.tcpdump.TcpDumpUtil;
import junit.framework.AssertionFailedError;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;

import java.io.BufferedReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: imiflig
 * Date: 1/30/14
 * Time: 3:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class NetlogZDnsTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(NetlogZDnsTestCase.class.getSimpleName());

    private static final int DNS_CLIENT_PORT = 6789;
    protected TimeInfoTransaction timeInfoTransaction;
    protected LogcatUtil logcatUtil;

    protected NetlogTask netlogTask;

    protected void executeGeneralCheck_NetlogZDns(String hostName, String fieldName, String controlValue, String message, List<NetlogEntry> entries){

        NetlogEntry entry = null;
        for (NetlogEntry entryInner : entries){
            if (hostName != null && hostName.equals(entryInner.getHost())){
                entry = entryInner;
            }
        }

        if (fieldName == null)
            throw new RuntimeException("Incorrect parameter.");
        if (entry != null){
            Class clazz = NetlogEntry.class;
            String testValue = null;
            for (Method method : clazz.getDeclaredMethods()){
                if (method.getName().contains("get") &&
                        method.getName().contains(fieldName)){
                    try {
                        Object result = method.invoke(entry,null);
                        testValue = result.toString();
                        break;
                    }catch (IllegalAccessException e){
                        e.printStackTrace();
                    }catch (InvocationTargetException e){
                        e.printStackTrace();
                    }
                }
            }
            assertEquals(message, controlValue , testValue);
        }else {
            throw new AssertionFailedError("Can't find netlog for host : " + hostName);
        }
    }

    protected void executeDataCheck_NetlogZDns(String host, List<NetlogEntry> entries, boolean isLocalDnsServer){

        int clientInNetlog = 0;
        int clientOutNetlog = 0;
        int serverInNetlog = 0;
        int serverOutNetlog = 0;

        long clientInTcpdump = 0;
        long clientOutTcpdump = 0;
        long serverInTcpdump = 0;
        long serverOutTcpdump = 0;

        logger.trace("StartTime=" + timeInfoTransaction.getTimeStart() / 1000 + " TimeEnd=" + timeInfoTransaction.getTimeEnd() / 1000);
        List<DnsSession> dnsSessions =  tcpDump.getDnsSessions(host,timeInfoTransaction.getTimeStart(),timeInfoTransaction.getTimeEnd());

        if (!isLocalDnsServer){
            for (DnsSession session :dnsSessions){
                logger.trace(session.toString());
                logger.trace("session.getUpstreamPayloadLengthLocal()=" + session.getUpstreamPayloadLengthLocal());
                logger.trace("session.getDownstreamPayloadLengthLocal()=" + session.getDownstreamPayloadLengthLocal());
                logger.trace("session.getDownstreamPayloadLengthNetwork()=" + session.getDownstreamPayloadLengthNetwork());
                logger.trace("session.getUpstreamPayloadLengthNetwork()=" + session.getUpstreamPayloadLengthNetwork());
                clientInTcpdump += session.getUpstreamPayloadLengthLocal();
                clientOutTcpdump += session.getDownstreamPayloadLengthLocal();
                serverInTcpdump += session.getDownstreamPayloadLengthNetwork();
                serverOutTcpdump += session.getUpstreamPayloadLengthNetwork();
            }
        }else{
            for (DnsSession session : dnsSessions){
                for (Packet packet : session.getPackets()){
                    if (packet.getSourcePort() == DNS_CLIENT_PORT){
                        clientInTcpdump += packet.getPayloadLength();
                    }
                    if (packet.getDestinationPort() == DNS_CLIENT_PORT){
                        clientOutTcpdump += packet.getPayloadLength();
                    }
                    if (packet.getSourcePort() == TFConstantsIF.DNS_SERVER_PORT && packet.getDestinationPort() != DNS_CLIENT_PORT){
                        serverInTcpdump += packet.getPayloadLength();
                    }
                    if (packet.getDestinationPort() == TFConstantsIF.DNS_SERVER_PORT){
                        serverOutTcpdump += packet.getPayloadLength();
                    }
                }
            }
        }

        for (NetlogEntry entry : entries){
            logger.trace("Entry=" + entry);
            if (host != null && host.equals(entry.getHost())){
                clientInNetlog += entry.getClient_in();
                clientOutNetlog += entry.getClient_out();
                serverInNetlog += entry.getServer_in();
                serverOutNetlog += entry.getServer_out();
            }
        }

        logger.trace("ClientIn extracted from netlogs for host = {} equals : {}", host, clientInNetlog);
        logger.trace("ClientOut extracted from netlogs for host = {} equals : {}", host, clientOutNetlog);
        logger.trace("ServerIn extracted from netlogs for host = {} equals : {}", host, serverInNetlog);
        logger.trace("ServerOut extracted from netlogs for host = " + host + " equals : " + serverOutNetlog);

        logger.trace("ClientIn calculated by tcpdump  for host = " + host + " equals : " + clientInTcpdump);
        logger.trace("ClientOut calculated by tcpdump for host = " + host + " equals : " + clientOutTcpdump);
        logger.trace("ServerIn calculated by tcpdump for host = " + host + " equals : " + serverInTcpdump);
        logger.trace("ServerOut calculated by tcpdump for host = " + host + " equals : " + serverOutTcpdump);

        assertEquals("ClientIn form tcpdump not equals clientIn from netlog. ", clientInTcpdump , clientInNetlog);
        assertEquals("ClientOut form tcpdump not equals clientOut from netlog. " , clientOutTcpdump, clientOutNetlog);
        assertEquals("ServerIn form tcpdump not equals serverIn from netlog. ", serverInTcpdump ,serverInNetlog);
        assertEquals("ServerOut form tcpdump not equals serverOut from netlog. ", serverOutTcpdump ,serverOutNetlog );
    }

    protected NetlogTask getNetlogTask(String host) throws Exception{

        tcpDump = TcpDumpUtil.getInstance(getContext());
        tcpDump.start();

        NetlogTask netlogTask = new NetlogTask();
        LogcatUtil logcatUtil = new LogcatUtil(getContext(),netlogTask);
        logcatUtil.start();

        timeInfoTransaction = new TimeInfoTransaction();
        timeInfoTransaction.setTimeStart(System.currentTimeMillis());

        try {
            InetAddress address = InetAddress.getByName(host);
            logger.trace("Resolved address : {}", address.getAddress().toString());
        } catch (UnknownHostException uhe){
            logger.error(ExceptionUtils.getStackTrace(uhe));
        } finally {
            logSleeping(20*1000);
            timeInfoTransaction.setTimeEnd(System.currentTimeMillis());
            logcatUtil.stop();
            tcpDump.stop();
        }
        return netlogTask;
    }


}
