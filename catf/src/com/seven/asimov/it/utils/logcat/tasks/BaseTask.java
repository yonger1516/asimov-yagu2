package com.seven.asimov.it.utils.logcat.tasks;

import com.seven.asimov.it.utils.logcat.wrappers.BaseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This task should be used if only the presence of entries specified by regexp
 * should be checked in the log, that is the content of entries does not matter.
 */
public class BaseTask extends Task<BaseWrapper> {
    private static final Logger logger = LoggerFactory.getLogger(BaseTask.class.getSimpleName());
    private static final String DATE_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*:[0-9]*:[0-9]*\\.[0-9]*).([A-Z]*).*";

    private final Pattern dateTaskPattern;

    /**
     * Create base task with using date regexp before base regexp (that is, DATE_REGEXP + baseRegexp)
     * @param regexp Base regexp to be matched
     * @return Instance of BaseTask class
     */
    public static BaseTask getInstance(String regexp) {
        return new BaseTask(DATE_REGEXP + regexp);
    }

    /**
     * @param regexp Base regexp to be matched
     * @param useDateRegexp Whether to use date regexp before base regexp,
     *                      in case of 'true' it will be DATE_REGEXP + baseRegexp
     * @return Instance of BaseTask class
     */
    public static BaseTask getInstance(String regexp, boolean useDateRegexp) {
        if (useDateRegexp)
            return new BaseTask(DATE_REGEXP + regexp);
        else
            return new BaseTask(regexp);
    }

    private BaseTask(String regexp) {
        dateTaskPattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
        logger.info("Created BaseTask with pattern: " + dateTaskPattern);
    }

    @Override
    protected BaseWrapper parseLine(String line) {
        Matcher matcher1 = dateTaskPattern.matcher(line);
        if (matcher1.find()) {
            BaseWrapper baseWrapper = new BaseWrapper();
            setTimestampToWrapper(baseWrapper, matcher1);
            return baseWrapper;
        }
        return null;
    }
}
