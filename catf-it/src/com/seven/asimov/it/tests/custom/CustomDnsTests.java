package com.seven.asimov.it.tests.custom;

import android.test.suitebuilder.annotation.LargeTest;
import com.seven.asimov.it.annotation.Ignore;
import com.seven.asimov.it.base.TcpDumpTestCase;

import java.util.ArrayList;
import java.util.List;

@Ignore
public class CustomDnsTests extends TcpDumpTestCase {
    private String host1 = "www.bianews.com";
    private String host2 = "www.bedetheque.com";
    private String host3 = "www.bdgroup.biz";
    private String host4 = "www.bdialog.net";
    private String host5 = "www.bdm-agro.ru";
    private String host6 = "www.centrmag.ru";
    private String host7 = "www.beloezoloto.ru";
    private String host8 = "www.biafishenol.ru";
    private String host9 = "www.bialystokonline.pl";
    private String host10 = "www.bianet.org";

    private String host11 = "www.baravik.org";
    private String host12 = "www.biathlon.ca";
    private String host13 = "www.bibagames.com.ua";
    private String host14 = "www.bibb.de";
    private String host15 = "www.bible.org";
    private String host16 = "www.bibleapp.com";
    private String host17 = "www.biblicalfoundations.org";
    private String host18 = "www.bicycleretailer.com";
    private String host19 = "www.bidla.net";
    private String host20 = "www.bidorbuy.co.za";

    private List<String> hosts1queue = new ArrayList<String>() {{
        add(host1);
        add(host2);
        add(host3);
        add(host4);
        add(host5);

        add(host6);
        add(host7);
        add(host8);
        add(host9);
        add(host10);

        add(host11);
        add(host12);
        add(host13);
        add(host14);
        add(host15);

        add(host16);
        add(host17);
        add(host18);
        add(host19);
        add(host20);

        add("www.becar.ru");
        add("edno23.com");
        add("www.betfor.fis.ru");
        add("www.bethany-ukraine.org");
        add("www.tpor.ru");
    }};

    private static final int SLEEP = 5 * 1000;

    @LargeTest
    public void testDNS() throws Throwable {

        for (String host : hosts1queue) {
            try {
                resolveHost(host);
                Thread.sleep(SLEEP);

                resolveHost(host);
                Thread.sleep(SLEEP);
            } catch (Throwable e) {
            }
        }
    }
}
