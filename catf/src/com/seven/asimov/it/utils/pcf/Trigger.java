package com.seven.asimov.it.utils.pcf;

import android.util.Log;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class Trigger {
    private static final String TAG = Trigger.class.getSimpleName();
    private Boolean applyAlways;
    private Boolean timeOfDay;
    private Integer timeOfDayFrom;
    private Integer timeOfDayTo;
    private Boolean periodic;
    private Integer periodLength;
    private Integer periodBlockLength;
    private Boolean screen;
    private Integer screenOffPeriod;
    private Boolean radio;

    public static Trigger toTriggerFromJson(String json) {
        try {
            JSONObject trigger = new JSONObject(json);

            Boolean applyAlways = trigger.getBoolean("applyAlways");
            Boolean timeOfDay = trigger.getBoolean("timeOfDay");
            Integer timeOfDayFrom = trigger.getInt("timeOfDayFrom");
            Integer timeOfDayTo = trigger.getInt("timeOfDayTo");
            Boolean periodic = trigger.getBoolean("periodic");
            Integer periodLength = trigger.getInt("periodLength");
            Integer periodBlockLength = trigger.getInt("periodBlockLength");
            Boolean screen = trigger.getBoolean("screen");
            Integer screenOffPeriod = trigger.getInt("screenOffPeriod");
            Boolean radio = trigger.getBoolean("radio");

            return new Trigger(screen, screenOffPeriod, radio, periodBlockLength, periodLength, periodic, timeOfDayTo, timeOfDayFrom, timeOfDay, applyAlways);
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create create UserGroup from json");
        //TODO should be custom exception
        return null;
    }

    public Trigger() {
        applyAlways = true;
    }

    public JSONObject toJson() {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put("applyAlways", applyAlways);
            resultJson.put("timeOfDay", timeOfDay);
            resultJson.put("timeOfDayFrom", timeOfDayFrom);
            resultJson.put("timeOfDayTo", timeOfDayTo);
            resultJson.put("periodic", periodic);
            resultJson.put("periodLength", periodLength);
            resultJson.put("periodBlockLength", periodBlockLength);
            resultJson.put("screen", screen);
            resultJson.put("screenOffPeriod", screenOffPeriod);
            resultJson.put("radio", radio);
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        return resultJson;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (getClass() != obj.getClass()) return false;

        Trigger other = (Trigger) obj;

        return new EqualsBuilder()
                .append(applyAlways, other.isApplyAlways())
                .append(timeOfDay, other.isTimeOfDay())
                .append(timeOfDayFrom, other.getTimeOfDayFrom())
                .append(timeOfDayTo, other.getTimeOfDayTo())
                .append(periodic, other.isPeriodic())
                .append(periodLength, other.getPeriodLength())
                .append(periodBlockLength, other.getPeriodBlockLength())
                .append(screen, other.isScreen())
                .append(screenOffPeriod, other.getScreenOffPeriod())
                .append(radio, other.isRadio()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(applyAlways)
                .append(timeOfDay)
                .append(timeOfDayFrom)
                .append(timeOfDayTo)
                .append(periodic)
                .append(periodLength)
                .append(periodBlockLength)
                .append(screen)
                .append(screenOffPeriod)
                .append(radio).toHashCode();
    }

    public Trigger(Boolean screen, Integer screenOffPeriod, Boolean radio, Integer periodBlockLength, Integer periodLength, Boolean periodic, Integer timeOfDayTo, Integer timeOfDayFrom, Boolean timeOfDay, Boolean applyAlways) {
        this.screen = screen;
        this.screenOffPeriod = screenOffPeriod;
        this.radio = radio;
        this.periodBlockLength = periodBlockLength;
        this.periodLength = periodLength;
        this.periodic = periodic;
        this.timeOfDayTo = timeOfDayTo;
        this.timeOfDayFrom = timeOfDayFrom;
        this.timeOfDay = timeOfDay;
        this.applyAlways = applyAlways;
    }

    public Boolean isApplyAlways() {
        return applyAlways;
    }

    public Boolean isTimeOfDay() {
        return timeOfDay;
    }

    public Integer getTimeOfDayFrom() {
        return timeOfDayFrom;
    }

    public Integer getTimeOfDayTo() {
        return timeOfDayTo;
    }

    public Boolean isPeriodic() {
        return periodic;
    }

    public Integer getPeriodLength() {
        return periodLength;
    }

    public Integer getPeriodBlockLength() {
        return periodBlockLength;
    }

    public Boolean isScreen() {
        return screen;
    }

    public Integer getScreenOffPeriod() {
        return screenOffPeriod;
    }

    public Boolean isRadio() {
        return radio;
    }
}
