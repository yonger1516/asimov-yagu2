package com.seven.asimov.it.utils.logcat.tasks.saTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.SAUpdateReceivedWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: rfu
 * Date: 4/30/14
 * Time: 2:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class SAUpdateReceivedTask extends Task<SAUpdateReceivedWrapper> {
    private static final Logger logger= LoggerFactory.getLogger(SAUpdateReceivedTask.class);

    private static final String SA_RECEIVED_REGEXP = "([A-Z]?)/Asimov::Java::.* (201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*com.seven.transport.AbstractZ7TransportMultiplexer.*<--- Received a generic update delivery system GUDS";
    private static final Pattern SAReceivedPattern = Pattern.compile(SA_RECEIVED_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected SAUpdateReceivedWrapper parseLine(String line) {

        Matcher matcher = SAReceivedPattern.matcher(line);
        if (matcher.find()) {
            SAUpdateReceivedWrapper wrapper = new SAUpdateReceivedWrapper();
            wrapper.setLogLevel(matcher.group(1));
            setTimestampToWrapper(wrapper, matcher, 2, 3);

            logger.debug("Matched wrapper:"+wrapper);
            return wrapper;
        }
        return null;
    }

}
