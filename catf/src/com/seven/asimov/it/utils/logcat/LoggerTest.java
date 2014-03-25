package com.seven.asimov.it.utils.logcat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: imiflig
 * Date: 1/9/14
 * Time: 4:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoggerTest {
    private static final Logger logger = LoggerFactory.getLogger(LoggerTest.class.getSimpleName());
    //OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL
    public static void test() {
        logger.error("-------------");
        logger.trace("trace message");
        logger.debug("debug message");
        logger.info("info message");
        logger.warn("warn message");
        logger.error("error message");
        //logger.error(e.getMessage());
    }
}
