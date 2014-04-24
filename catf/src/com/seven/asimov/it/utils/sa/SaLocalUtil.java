package com.seven.asimov.it.utils.sa;

import com.seven.asimov.it.base.constants.TFConstantsIF;
import com.seven.asimov.it.utils.ShellUtil;
import com.seven.asimov.it.utils.StringUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: rfu
 * Date: 4/17/14
 * Time: 2:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class SaLocalUtil {
    private static final Logger logger = LoggerFactory.getLogger(SaLocalUtil.class);
    private static String CONFIG_SCHEMA_FILE = "config.data";
    private static String CONFIG_SCHEMA_JSON;

    private static void loadString() throws Exception {
        if ("".equals(CONFIG_SCHEMA_JSON) || null == CONFIG_SCHEMA_JSON) {
            try {
                readJson();
            } catch (Exception e) {
                logger.error(ExceptionUtils.getFullStackTrace(e));
                throw new Exception(e);
            }
        }

    }

    protected void updatePolicy(String name, String value, String path) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
        loadString();
        String  newJson="";

        Pattern pattern=Pattern.compile("\""+name+"\":\\{\"[a-z]+\":[a-z]+\\},");
        Matcher m=pattern.matcher(CONFIG_SCHEMA_JSON);
        if (m.find()){
             String type=detectTypeByValue(value);
             newJson=CONFIG_SCHEMA_FILE.replace(m.group(0),"\""+name+"\":{\""+type+"\":"+value+"},");
        }else{
            addPolicy(name,value,path);
        }


        writeJson(newJson);

    }



    public void addPolicy(String name, String value, String path) {

    }


    private void writeJson(String newJson) {


    }


    private String detectTypeByValue(String value) {

        if ("true".equals(value)||"false".equals(value)){
            return "boolean";
        }else if (StringUtil.isNumberOfString(value)){
            return "int";
        } else{
            return "string";
        }
    }


    private static void readJson() throws Exception {

        CONFIG_SCHEMA_FILE = TFConstantsIF.OC_PRIMARY_DIR + "/" + CONFIG_SCHEMA_FILE;
        if (!ShellUtil.grantRWRightToFile(CONFIG_SCHEMA_FILE)) {
            throw new Exception("failed to get read/write privilege:" + CONFIG_SCHEMA_FILE);
        }

        BufferedReader br = new BufferedReader(new FileReader(CONFIG_SCHEMA_FILE));

        String line = "";
        while (null != (line = br.readLine())) {
            CONFIG_SCHEMA_JSON += line;
        }
    }


}
