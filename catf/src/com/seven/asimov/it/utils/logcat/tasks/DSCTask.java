package com.seven.asimov.it.utils.logcat.tasks;


import android.util.Log;
import com.seven.asimov.it.utils.logcat.wrappers.DSCWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DSCTask extends Task<DSCWrapper> {
    private static final String TAG = DSCTask.class.getSimpleName();

    public static enum DSCAction {
        STARTED(0), ENDED(1), APPLIED(2), INITIALIZED(3),EVENT(4),BOOTED(5),RESTARTED(6),ERROR(7);

        DSCAction(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        private int value;

        public static DSCAction getEnum(int value) {
            for (DSCAction v : values())
                if (value==v.value) return v;
            throw new IllegalArgumentException();
        }
    }

    public static enum DSCReason {
        STARTUP(1),ENABLED(2),ENABLED_BY_PROP(3),RESTART(4),RECONNECT(5),FLO_END(6),SHUTDOWN(7),DISABLED(8),DISABLED_BY_PROP(9),TIMEOUT(10),UNEXPECTED(11),FLO_START(12),DISCONNECT(13),RESTART_FLO_ACTIVE(14);

        DSCReason(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        private int value;

        public static DSCReason getEnum(int value) {
            for (DSCReason v : values())
                if (value==v.value) return v;
            throw new IllegalArgumentException();
        }
    }

    private String sendingDSCRegex =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*Sending DSC message: sec=(\\d*), usec=(\\d*), disp_id=(%s), action=(%s), reason=(%s)";
    private String receivedDSCRegex =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*Received DSC message \\(ts=(\\d*)\\.(\\d*), action=(%s), dispatcher_id=(%s), reason=(%s)\\)";

    private String dscRegex;
    private Pattern dscPattern;
    private boolean sending;

    public DSCTask(boolean sending, Integer dispID, Integer action,Integer reason) {
        this.sending=sending;
        if(sending)
            dscRegex = String.format(sendingDSCRegex, dispID==null?"\\d*":dispID, action==null?"\\d*":action,reason==null?"\\d*":reason);
        else
            dscRegex = String.format(receivedDSCRegex, action==null?"\\d*":action, dispID==null?"\\d*":dispID,reason==null?"\\d*":reason);
        Log.v(TAG, "dscRegex=" + dscRegex);
        dscPattern = Pattern.compile(dscRegex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public DSCWrapper parseLine(String line) {
        Matcher matcher = dscPattern.matcher(line);
        if (matcher.find()) {
            DSCWrapper wrapper = new DSCWrapper();
            setTimestampToWrapper(wrapper, matcher);
            if(sending){
                wrapper.setDispID(Integer.parseInt(matcher.group(5)));
                wrapper.setAction(Integer.parseInt(matcher.group(6)));
                wrapper.setReason(Integer.parseInt(matcher.group(7)));
            }else{
                wrapper.setAction(Integer.parseInt(matcher.group(5)));
                wrapper.setDispID(Integer.parseInt(matcher.group(6)));
                wrapper.setReason(Integer.parseInt(matcher.group(7)));
            }

            return wrapper;
        }
        return null;
    }

}
