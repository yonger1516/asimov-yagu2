package com.seven.asimov.it.utils.logcat.tasks;


import com.seven.asimov.it.utils.logcat.wrappers.GenericWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GenericTask is configurable task.
 * This task requires regex with named groups. Only values matched byt named groups will be stored in GenericWrapper.
 * Group names must follow next format:
 * (?P<groupName>regex)
 * where groupName is name of group and regex is regular expression used to match group.
 * Here is example of adding named groups to standard regex.
 * Consider having next regex:
 * (201[2-9]/[0-9]*\/[0-9]\* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*status=([A-Z_]*).*type=([A-Z_]*).*token=([0-9]*).*added to the queue
 * to be able to retrieve status, type and token groups values, those groups have to be named:
 * (201[2-9]/[0-9]*\/[0-9]\* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*status=(?P<crcsTaskStatus>[A-Z_]*).*type=(?P<crcsTaskType>[A-Z_]*).*token=(?P<crcsTaskToken>[0-9]*).*added to the queue
 * Name for DATE and GMT groups is not required. Time will be automatically retrieved and set to wrapper from regex groups 1 and 2.
 * Given regex will produce wrapper with 3 values.
 * To retrieve value of some named group from GenericWrapper just use one of get* methods (getString,getShot,getInteger,getLong),
 * use group name as parameter when calling one of these methods.
 * If given group name is not found or value can't be converted to given type get* methods will return null.
 * <p/>
 * GenericTask has 3 constructors:
 * public GenericTask(String regex) - accepts one regex with named groups.
 * public GenericTask(String regex, Object... formatParams) - same as above, but applies String.format
 * to first parameter and uses vararg as second parameter to String.format.
 * public GenericTask(String[] regexStrings) - accepts array of regular expressions with named groups. This should be used if multiple variants of logging
 * exists for same thing. For example there are two possible log lines which differ only by some constant part or have groups swapped, etc.
 * Do not use this constructor for completely different regexes, this is not effective, because it will require to determine which regex was matched.
 * Be sure to check for null returned from GenericWrapper get* methods, especially if there is different named group count in regexes.
 * <p/>
 * Just after constructing GeneralTask can be used to match log lines.
 * But if some concretised lines needs to be mathched then all of named groups can be parametrized.
 * <p/>
 * public void parametrizeGroup(String groupName, String parameter) - method used to parametrize named groups.
 * <p/>
 * Just call this method, passing name of group and expected value.
 * This method will overwrite all text between ( and ) for given named group.
 * For example consider next named group in regex (part of regex with named group is shown):
 * .*status=(?P<crcsTaskStatus>[A-Z_]*).*
 * after call:
 * parametrizeGroup("crcsTaskStatus","SENT");
 * Regex will be transformed to:
 * .*status=(SENT).*
 * So only lines with status=SENT can be matched.
 * All named groups or just some of them can be parametrized. Also completely not parametrized regex can be used to match as much log lines as regex allows.
 * <p/>
 * public void resetParameters() is used to remove parametrization.
 */
public class GenericTask extends Task<GenericWrapper> {
    private static final Logger logger = LoggerFactory.getLogger(GenericTask.class.getSimpleName());
    private List<NamedRegex> regexps;
    private Map<String, String> groupParameters = new TreeMap<String, String>();

    /**
     * Constructs {@link com.seven.asimov.it.utils.logcat.tasks.GenericTask} with one regular expression.
     *
     * @param regex Regular expression to use from parsing.
     */
    public GenericTask(String regex) {
        this(new String[]{regex});
    }

    /**
     * Constructs {@link com.seven.asimov.it.utils.logcat.tasks.GenericTask} with one regular expression formatted with the help of String.format .
     *
     * @param regex        Regular expression to use from parsing.
     * @param formatParams Params passed to format method along with Regular expression pattern passed as first parameter.
     */
    public GenericTask(String regex, Object... formatParams) {
        this(new String[]{String.format(regex, formatParams)});
    }

    /**
     * Constructs {@link com.seven.asimov.it.utils.logcat.tasks.GenericTask} with multiple regular expressions.
     *
     * @param regexStrings Regular expression to use from parsing.
     */
    public GenericTask(String[] regexStrings) {
        this(regexStrings, null);
    }

    /**
     * Constructs {@link com.seven.asimov.it.utils.logcat.tasks.GenericTask} with multiple regular expressions. Second parameter allows parametrization of regex groups.
     *
     * @param regexStrings Regular expression to use from parsing.
     * @param groupValues  Map of regex groups parametrization values.
     */
    protected GenericTask(String[] regexStrings, Map<String, String> groupValues) {
        regexps = new LinkedList<NamedRegex>();
        for (String regex : regexStrings)
            regexps.add(new NamedRegex(regex, groupValues));
    }

    /**
     * Set parametrization for one group.
     *
     * @param groupName Name of parametrized group.
     * @param parameter Parametrization value.
     */
    public void parametrizeGroup(String groupName, String parameter) {
        groupParameters.put(groupName, parameter);
        applyParametrizing();
    }

    /**
     * Remove all parametrization for groups.
     */
    public void resetParameters() {
        groupParameters.clear();
        applyParametrizing();
    }

    /**
     * Sets current parametrization values to regular expressions groups.
     */
    private void applyParametrizing() {
        for (NamedRegex regexp : regexps) {
            regexp.parametrize(groupParameters);
        }
    }

    protected GenericWrapper parseLine(String line) {
        GenericWrapper wrapper;
        Matcher matcher;
        for (NamedRegex regexp : regexps) {
            matcher = regexp.getPattern().matcher(line);
            if (matcher.find()) {
                wrapper = new GenericWrapper();
                setTimestampToWrapper(wrapper, matcher);
                for (String group : regexp.getGroupNames())
                    wrapper.putValue(group, matcher.group(regexp.getGroupNumber(group)));
                return wrapper;
            }
        }
        return null;
    }

    static class NamedRegex {
        Map<String, Integer> groupNumbers = new TreeMap<String, Integer>();
        String regexPattern;
        Pattern pattern;

        public Pattern getPattern() {
            return pattern;
        }

        public Set<String> getGroupNames() {
            return groupNumbers.keySet();
        }

        public Integer getGroupNumber(String groupName) {
            return groupNumbers.get(groupName);
        }

        public NamedRegex(String regex, Map<String, String> groupParameters) {
            regexPattern = regex;
            parametrize(groupParameters);
        }

        public void parametrize(Map<String, String> groupParameters) {
            String regex = extractNamedGroups(regexPattern, groupParameters, groupNumbers);
            pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            logger.info("Parametrized regex: " + regex);
        }

        private static String extractGroupName(StringBuilder regex, int readPos) {
            Pattern groupNamePattern = Pattern.compile("\\(\\?P\\<(.*?)\\>");
            Matcher matcher = groupNamePattern.matcher(regex);
            if (!matcher.find(readPos))
                return null;
            else if (matcher.start() != readPos)
                return null;

            String groupName = matcher.group(1);
            regex.delete(matcher.start() + 1, matcher.end());
            return groupName;
        }

        private static int findGroupStart(StringBuilder regex, int currentPos) {
            char prevChar = 0;
            while (currentPos < regex.length()) {
                char currentChar = regex.charAt(currentPos);
                if ((currentChar == '(') && (prevChar != '\\'))
                    return currentPos;

                prevChar = currentChar;
                currentPos++;
            }
            return -1;
        }

        private static int findGroupEnd(StringBuilder regex, int currentPos) {
            char prevChar = 0;
            int depthLevel = 0;
            while (currentPos < regex.length()) {
                char currentChar = regex.charAt(currentPos);
                switch (currentChar) {
                    case '(':
                        if (prevChar != '\\')
                            depthLevel++;
                        break;
                    case ')':
                        if (prevChar != '\\')
                            depthLevel--;
                        if (depthLevel == 0)
                            return currentPos;
                        break;
                }
                prevChar = currentChar;
                currentPos++;
            }
            return -1;
        }

        private static String extractNamedGroups(String regex, Map<String, String> groupValues, Map<String, Integer> namedGroups) {
            StringBuilder editableRegex = new StringBuilder(regex);
            extractNamedGroups(editableRegex, groupValues, namedGroups);
            for (String key : namedGroups.keySet())
                logger.info("Found named group :" + key + " index: " + namedGroups.get(key));
            return editableRegex.toString();
        }

        private static void extractNamedGroups(StringBuilder regex, Map<String, String> groupValues, Map<String, Integer> namedGroups) {
            int currentGroup = 0;
            int groupStart = -1;
            String groupName;
            while ((groupStart = findGroupStart(regex, groupStart + 1)) >= 0) {
                currentGroup++;
                groupName = extractGroupName(regex, groupStart);
                if (groupName != null) {
                    namedGroups.put(groupName, currentGroup);
                    if ((groupValues != null) && (groupValues.containsKey(groupName))) {
                        int groupEnd = findGroupEnd(regex, groupStart);
                        regex.replace(groupStart + 1, groupEnd, groupValues.get(groupName));
                    }
                }
            }
        }
    }
}
