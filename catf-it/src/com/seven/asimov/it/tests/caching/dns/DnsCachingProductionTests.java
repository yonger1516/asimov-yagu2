package com.seven.asimov.it.tests.caching.dns;


import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.testcases.DnsTestCase;
import com.seven.asimov.it.utils.TestUtil;
import com.seven.asimov.it.utils.tcpdump.TcpDumpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.security.Security;
import java.util.HashSet;
import java.util.Set;

public class DnsCachingProductionTests extends DnsTestCase {
    private static final Logger logger = LoggerFactory.getLogger(DnsCachingProductionTests.class.getSimpleName());


    /**
     * TODO @Ignore by ASMV-21650
     *
     * @throws Exception
     * @see ...
     */
    @Ignore
    @LargeTest
    public void test_001_DnsCachingProduction() throws Throwable {
        System.setProperty("networkaddress.cache.ttl", "1");
        Security.setProperty("networkaddress.cache.ttl", "1");
        System.setProperty("networkaddress.cache.negative.ttl", "1");
        Security.setProperty("networkaddress.cache.negative.ttl", "1");

        logger.info("Started");

        tcpDump = TcpDumpUtil.getInstance(getContext());
        try {
            Set<String> hosts = resolveAndGetHosts(HOSTS1);
            String message = "Resolved first queue of hosts(HOSTS1)  first time:  " + hosts;
            System.out.println(message);
            logger.info(message);
            assertEquals("First queue resolved", HOSTS1.length, hosts.size());

            TestUtil.sleep(605000);
            hosts = resolveAndGetHosts(HOSTS1);
            message = "Resolved first queue of hosts(HOSTS1)  second time:  " + hosts;
            System.out.println(message);
            logger.info(message);
            assertEquals("First queue resolved", 0, hosts.size());

            hosts = resolveAndGetHosts(HOSTS2);
            message = "Resolved second queue of hosts(HOSTS2)  first time:  " + hosts;
            System.out.println(message);
            logger.info(message);
            assertEquals("Second queue resolved", HOSTS2.length, hosts.size());

            TestUtil.sleep(605000);

            hosts = resolveAndGetHosts(HOSTS2);
            message = "Resolved second queue of hosts(HOSTS2)  second time:  " + hosts;
            System.out.println(message);
            logger.info(message);
            assertEquals("Second queue resolved", 0, hosts.size());

            hosts = resolveAndGetHosts(HOSTS1);
            message = "Resolved first queue of hosts(HOSTS1)  third time:  " + hosts;
            System.out.println(message);
            logger.info(message);
            assertEquals("First queue resolved", 0, hosts.size());
            System.out.print(false);
        } finally {
            tcpDump.stop();
        }
    }

    private Set<String> resolveAndGetHosts(String[] hosts) throws IOException {
        tcpDump.start();
        resolveAddresses(hosts);
        tcpDump.stop();
        Set<String> result = new HashSet<String>();
        for (String host : tcpDump.getResolvedHosts(hosts)) {
            if (!host.contains("carrier004.toc.seven.com")) {
                result.add(host);
            }
        }
        return result;
    }

    private void resolveAddresses(String[] hosts) {
        for (int i = 0; i < hosts.length; i++) {
            try {
                Thread.sleep(4000);
                InetAddress.getByName(hosts[i].replace("http://www.", ""));
            } catch (Throwable e) {
                e.printStackTrace();
                i++;
                continue;
            }
        }
    }

    public static final String[] HOSTS1 = {"http://www.facebook.com", "http://www.youtube.com",
            "http://www.yahoo.com", "http://www.live.com", "http://www.msn.com", "http://www.wikipedia.org",
            "http://www.blogspot.com", "http://www.baidu.com", "http://www.microsoft.com", "http://www.qq.com",
            "http://www.bing.com", "http://www.ask.com", "http://www.adobe.com", "http://www.taobao.com",
            "http://www.twitter.com", "http://www.youku.com"};

    public static final String[] HOSTS2 = {"http://www.soso.com", "http://www.wordpress.com", "http://www.sohu.com",
            "http://www.hao123.com", "http://www.windows.com", "http://www.163.com", "http://www.tudou.com",
            "http://www.amazon.com", "http://www.apple.com", "http://www.ebay.com", "http://www.4399.com",
            "http://www.yahoo.co.jp", "http://www.linkedin.com", "http://www.go.com", "http://www.tmall.com",
            "http://www.paypal.com", "http://www.sogou.com", "http://www.ifeng.com", "http://www.aol.com",
            "http://www.xunlei.com", "http://www.craigslist.org", "http://www.orkut.com", "http://www.56.com",
            "http://www.orkut.com.br", "http://www.about.com", "http://www.skype.com", "http://www.7k7k.com",
            "http://www.dailymotion.com"};
}

