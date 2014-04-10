package com.seven.asimov.it.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: rfu
 * Date: 4/10/14
 * Time: 10:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectivityUtil {
    private static final Logger logger= LoggerFactory.getLogger(ConnectivityUtil.class.getSimpleName());
    private static final String BYTES_FROM="bytes from";

    public static boolean ping(String host) {
        String cmd = "ping -c 4 " + host;

        StringBuffer sb=new StringBuffer();
        try {

            logger.debug(cmd);
            Process p = Runtime.getRuntime().exec(cmd);
            InputStream is = p.getInputStream();


            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while (null != (line = br.readLine())) {
                sb.append(line);
            }

            logger.trace(sb.toString());

            if (sb.toString().contains(BYTES_FROM)){
                return true;
            }else{
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return false;
        }
    }
}
