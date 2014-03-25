package com.seven.asimov.it.utils;

import com.seven.asimov.it.base.constants.TFConstantsIF;
import junit.framework.Assert;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DNS Util
 */
public class DnsUtil {
    private static final Logger logger = LoggerFactory.getLogger(DnsUtil.class.getSimpleName());

    /**
     * <p>Resolves the remote host</p>
     *
     * @param host - the name of the remote host
     * @throws IOException
     */

    public static void resolveHost(String host) throws IOException {
        resolveHost(host, TFConstantsIF.DEFAULT_DNS_SERVER);
    }

    /**
     * <p>Resolves the remote host</p>
     *
     * @param host      the name of the remote host
     * @param dnsServer the name of the remote DNS server
     * @throws IOException
     */

    public static void resolveHost(String host, String dnsServer) throws IOException {
        SimpleResolver resolver = new SimpleResolver(dnsServer);
        resolver.setTimeout(90);
        Message msgIpv4 = resolver.send(createDnsQuery(host, Type.A));
        logger.debug("Resolve host: received message ipv4:" + msgIpv4.toString());
        Message msgIpv6 = resolver.send(createDnsQuery(host, Type.AAAA));
        logger.debug("Resolve host: received message ipv6:" + msgIpv6.toString());
    }

    /**
     * <p>Creates the DNS Query and return the DNS Message
     * The message is the basic unit of communication between
     * the client and server of a DNS operation</p>
     *
     * @param host    the name of the remote host
     * @param dnsType the DNS Types. For example, Type.A - for IPv4, Type.AAAA for IPv6
     * @return the DNS Message
     */
    public static Message createDnsQuery(String host, int dnsType) {
        Message dnsMessage = null;
        host += ".";
        try {
            Name name = Name.fromString(host);
            dnsMessage = Message.newQuery(Record.newRecord(name, dnsType, DClass.IN));
        } catch (TextParseException tpe) {
            logger.debug(ExceptionUtils.getStackTrace(tpe));
        } catch (Exception e) {
            logger.debug(ExceptionUtils.getStackTrace(e));
        }
        return dnsMessage;
    }

    public void cleanOCDnsCache() throws Exception{
        final String occProcess = "occ";
        final String asimovProcess = "com.seven.asimov";
        Integer occPID;
        Integer asimovPID;
        Map<String,Integer> ocProcesses = OCUtil.getOcProcesses(true);
        occPID=ocProcesses.get(occProcess);
        asimovPID=ocProcesses.get(asimovProcess);

        Assert.assertTrue("prepareDnsTestRun can't find " + occProcess + " process", occPID != null);
        Assert.assertTrue("prepareDnsTestRun can't find " + asimovProcess + " process", asimovPID != null);

        List<String> command = new ArrayList<String>();
        command.add("rm /data/misc/openchannel/oc_engine.db");
        ShellUtil.execWithCompleteResult(command, true);

        command.clear();
        command.add("kill " + occPID);
        ShellUtil.execWithCompleteResult(command, true);

        command.clear();
        command.add("kill " + asimovPID);
        ShellUtil.execWithCompleteResult(command, true);

        TestUtil.sleep(30 * 1000);   //Wait for OC to recover and recreate DB; Maybe need to check if all processes UP?
        for(int i = 0; i < 3; i++){
            ocProcesses = OCUtil.getOcProcesses(true);
            occPID = ocProcesses.get(occProcess);
            asimovPID = ocProcesses.get(asimovProcess);
            if((occPID != null) && (asimovPID != null))
                break;

            TestUtil.sleep(15 * 1000);
        }
        occPID = ocProcesses.get(occProcess);
        asimovPID = ocProcesses.get(asimovProcess);
        Assert.assertTrue("prepareDnsTestRun can't find " + occProcess + " process  after db removal", occPID != null);
        Assert.assertTrue("prepareDnsTestRun can't find " + asimovProcess + " process  after db removal", asimovPID != null);
    }
}
