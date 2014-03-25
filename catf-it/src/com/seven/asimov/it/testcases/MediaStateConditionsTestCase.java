package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.MediaUtil;
import com.seven.asimov.it.utils.PrepareResourceUtil;
import com.seven.asimov.it.utils.logcat.wrappers.ScriptLogWrapper;
import com.seven.asimov.it.utils.pms.PMSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.seven.asimov.it.base.constants.TFConstantsIF.*;

public class MediaStateConditionsTestCase extends TcpDumpTestCase {
    private static final Logger logger = LoggerFactory.getLogger(MediaStateConditionsTestCase.class.getSimpleName());
    protected final String SCRIPT_NAME = "script_media";
    protected final String ENTER_CONDITION = "@asimov@application@com.seven.asimov.it@scripts@script_media@conditions";
    protected final String EXIT_CONDITION = "@asimov@application@com.seven.asimov.it@scripts@script_media@exit_conditions";
    protected final String MEDIA_CONDITION = "media";
    protected final String SCREEN_CONDITION = "screen";
    protected final String TIMER_CONDITION = "timer";
    protected final String RADIO_CONDITION = "radio";
    protected final String EVENT_DATA = "F[0]C[5/on;1/30;3/on]EC[8/down;1/45;3/off]A[]EA[]";

    /**
     * Finds ScriptLogWrapper matching given requirements.
     *
     * @param wrappers    List of wrappers to search.
     * @param scriptName  Name of script to match.
     * @param scriptState Script state to match.
     * @param scriptEvent Script event to match.
     * @param eventData   Event data to match. Ignored if null.
     * @return Matched ScriptLogWrapper or null if none found.
     */
    protected ScriptLogWrapper mscFindScriptWrapper(List<ScriptLogWrapper> wrappers, String scriptName, int scriptState, int scriptEvent, String eventData) {
        logger.info("mscFindScriptWrapper: got " + (wrappers.size()) + " wrapper(s)");
        for (ScriptLogWrapper wrapper : wrappers) {
            logger.info("mscFindScriptWrapper: checking wrapper: " + wrapper);
            if ((wrapper.getAppName().equals(IT_PACKAGE_NAME)) &&
                    (wrapper.getScriptName().equals(scriptName)) &&
                    (wrapper.getState() == scriptState) &&
                    (wrapper.getEvent() == scriptEvent)) {
                if (eventData == null) {
                    logger.info("mscFindScriptWrapper: wrapper matched!" + wrapper);
                    return wrapper;
                } else if (mscCompareEventData(wrapper.getEventData(), eventData)) {
                    logger.info("mscFindScriptWrapper: wrapper matched!" + wrapper);
                    return wrapper;
                }
            }
        }
        return null;
    }

    /**
     * Compares two event data lines formatted in <a href="https://matrix.seven.com/display/Eng/Script+entities+state+data+format">Script entities state data format</a>.<br/>
     * Compares ignoring condition/action order in categories.
     *
     * @param eventData1 First event data to compare.
     * @param eventData2 Second event data to compare.
     */
    private boolean mscCompareEventData(String eventData1, String eventData2) {
        List<String> list1 = mscEventDataToList(eventData1);
        List<String> list2 = mscEventDataToList(eventData2);

        return (list1.containsAll(list2)) && (list2.containsAll(list1));
    }

    private List<String> mscEventDataToList(String eventData) {
        final String sPattern = "F\\[(.*)\\]C\\[(.*)\\]EC\\[(.*)\\]A\\[(.*)\\]EA\\[(.*)\\]";
        final Pattern pattern = Pattern.compile(sPattern);
        final Matcher matcher = pattern.matcher(eventData);
        List<String> list = new ArrayList<String>();
        if (matcher.find()) {
            mscAddToList(list, "F_", matcher.group(1));
            mscAddToList(list, "C_", matcher.group(2));
            mscAddToList(list, "EC_", matcher.group(3));
            mscAddToList(list, "A_", matcher.group(4));
            mscAddToList(list, "EA_", matcher.group(5));
        }
        return list;
    }

    private void mscAddToList(List<String> list, String prefix, String values) {
        String[] splitted = values.split(";");
        for (String pair : splitted) {
            list.add(prefix + pair);
        }
    }

    protected void oncePlay(String enterCond, String exitCond, String action, String url, int statusCode, boolean play) throws Exception {
        final int TEN_SEC = 10000;
        MediaUtil mediaUtil = MediaUtil.init();
        String uri = createTestResourceUri(url);
        HttpRequest request = createRequest().setUri(uri)
                .addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        try {
            String idEnter = PMSUtil.createPersonalScopeProperty(MEDIA_CONDITION, IN_CONDITIONS, enterCond, true);
            String idEXit = PMSUtil.createPersonalScopeProperty(MEDIA_CONDITION, OUT_CONDITIONS, exitCond, true);
            String actions = PMSUtil.createPersonalScopeProperty(MEDIA_CONDITION, ACTION_CONDITIONS, action, true);
            if (play) {
                mediaUtil.play();
                logSleeping(TEN_SEC);
                checkMiss(request, 1, statusCode, null);
                logSleeping(TEN_SEC);
                mediaUtil.stop();
            } else {
                logSleeping(TEN_SEC);
                checkMiss(request, 1, statusCode, null);
            }
            PMSUtil.deleteProperty(idEnter);
            PMSUtil.deleteProperty(idEXit);
            PMSUtil.deleteProperty(actions);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            mediaUtil.stop();
        }
    }

    protected void twicePlay(String enterCond, String exitCond, String action, String enterCond2, String exitCond2, String action2, String url,
                             int statusCode, int statusCode2, boolean play, boolean play2) throws Exception {
        final int TEN_SEC = 10000;
        MediaUtil mediaUtil = MediaUtil.init();
        String uri = createTestResourceUri(url);
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        try {
            String idEnter = PMSUtil.createPersonalScopeProperty(MEDIA_CONDITION, IN_CONDITIONS, enterCond, true);
            String idEXit = PMSUtil.createPersonalScopeProperty(MEDIA_CONDITION, OUT_CONDITIONS, exitCond, true);
            String actions = PMSUtil.createPersonalScopeProperty(MEDIA_CONDITION, ACTION_CONDITIONS, action, true);
            if (play) {
                mediaUtil.play();
                logSleeping(TEN_SEC);
                checkMiss(request, 1, statusCode, null);
                logSleeping(TEN_SEC);
                mediaUtil.stop();
            } else {
                logSleeping(TEN_SEC);
                checkMiss(request, 1, statusCode, null);
            }
            PMSUtil.deleteProperty(idEnter);
            PMSUtil.deleteProperty(idEXit);
            PMSUtil.deleteProperty(actions);
            String idEnterCh = PMSUtil.createPersonalScopeProperty(MEDIA_CONDITION, IN_CONDITIONS, enterCond2, true);
            String idEXitCh = PMSUtil.createPersonalScopeProperty(MEDIA_CONDITION, OUT_CONDITIONS, exitCond2, true);
            String actionsCh = PMSUtil.createPersonalScopeProperty(MEDIA_CONDITION, ACTION_CONDITIONS, action2, true);
            if (play2) {
                mediaUtil.play();
                logSleeping(TEN_SEC);
                checkMiss(request, 2, statusCode2, null);
                logSleeping(TEN_SEC);
                mediaUtil.stop();
            } else {
                logSleeping(TEN_SEC);
                checkMiss(request, 2, statusCode2, null);
            }
            PMSUtil.deleteProperty(idEnterCh);
            PMSUtil.deleteProperty(idEXitCh);
            PMSUtil.deleteProperty(actionsCh);
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            mediaUtil.stop();
        }
    }

    protected void playAndCheck(String enterCond, String exitCond, String action, String url, int statusCode) throws Exception {
        final int TEN_SEC = 10000;
        MediaUtil mediaUtil = MediaUtil.init();
        String uri = createTestResourceUri(url);
        HttpRequest request = createRequest().setUri(uri).addHeaderField("X-OC-ContentEncoding", "identity").getRequest();
        try {
            mediaUtil.play();
            logSleeping(TEN_SEC);
            checkMiss(request, 1, 200, null);
            logSleeping(TEN_SEC);
            String idEnter = PMSUtil.createPersonalScopeProperty(MEDIA_CONDITION, IN_CONDITIONS, enterCond, true);
            String idEXit = PMSUtil.createPersonalScopeProperty(MEDIA_CONDITION, OUT_CONDITIONS, exitCond, true);
            String actions = PMSUtil.createPersonalScopeProperty(MEDIA_CONDITION, ACTION_CONDITIONS, action, true);
            logSleeping(TEN_SEC);
            checkMiss(request, 2, statusCode, null);
            logSleeping(TEN_SEC);
            PMSUtil.deleteProperty(idEnter);
            PMSUtil.deleteProperty(idEXit);
            PMSUtil.deleteProperty(actions);
            logSleeping(TEN_SEC);
            mediaUtil.stop();
        } finally {
            PrepareResourceUtil.invalidateResourceSafely(uri);
            mediaUtil.stop();
        }
    }

    protected void mediaInit() throws Exception {
        final int TEN_SEC = 10000;
        final String PATH = "@asimov@application";
        final String NAME = "com.seven.asimov.it";
        final String PATH_SECONDARY = "@asimov@application@com.seven.asimov.it";
        final String NAME_SECONDARY = "scripts";
        final String PATH_THIRD = "@asimov@application@com.seven.asimov.it@scripts";
        final String NAME_THIRD = "mediascript";
        final String MEDIA_SCRIPT_CONDITION_PATH = "@asimov@application@com.seven.asimov.it@scripts@mediascript";
        final String ENTER_CONDITIONS = "conditions";
        final String EXIT_CONDITIONS = "exit_conditions";
        final String ACTIONS = "actions";
        PMSUtil.createNameSpace(PATH, NAME);
        logSleeping(TEN_SEC);
        PMSUtil.createNameSpace(PATH_SECONDARY, NAME_SECONDARY);
        logSleeping(TEN_SEC);
        PMSUtil.createNameSpace(PATH_THIRD, NAME_THIRD);
        logSleeping(TEN_SEC);
        PMSUtil.createNameSpace(MEDIA_SCRIPT_CONDITION_PATH, ENTER_CONDITIONS);
        logSleeping(TEN_SEC);
        PMSUtil.createNameSpace(MEDIA_SCRIPT_CONDITION_PATH, ACTIONS);
        logSleeping(TEN_SEC);
        PMSUtil.createNameSpace(MEDIA_SCRIPT_CONDITION_PATH, EXIT_CONDITIONS);
        logSleeping(TEN_SEC);
    }
}
