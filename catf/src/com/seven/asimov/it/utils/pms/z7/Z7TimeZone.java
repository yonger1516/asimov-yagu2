package com.seven.asimov.it.utils.pms.z7;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Z7TimeZone {

    private short m_standardBias;   //E.g. -120 in Helsinki and 480 in RWC
    private short m_standardStartMonth;
    private short m_standardStartDay;
    private short m_standardStartDayOfWeek;
    private short m_standardStartHour;
    private short m_standardStartMinute;
    private short m_daylightBias; //E.g. -180 in Helsinki and 420 in RWC
    private short m_daylightStartMonth;
    private short m_daylightStartDay;
    private short m_daylightStartDayOfWeek;
    private short m_daylightStartHour;
    private short m_daylightStartMinute;
    private String m_timezoneName;

    public Z7TimeZone(short standardBias) {
        //super((int)standardBias * 60 * 1000, "Z7TimeZone");
        m_standardBias = standardBias;
        m_standardStartMonth = 0;
        m_standardStartDay = 0; // 'first'/'second'/ .... / 'last'
        m_standardStartDayOfWeek = 0;
        m_standardStartHour = 0;
        m_standardStartMinute = 0;
        m_daylightStartMonth = 0;
        m_daylightStartDay = 0;
        m_daylightStartDayOfWeek = 0;
        m_daylightStartHour = 0;
        m_daylightStartMinute = 0;
        m_daylightBias = (short) (standardBias - 60); // to match some standard values defined in //depot/z7/main/native/util/src/Z7DateTime.cpp
    }

    public Z7TimeZone(short standardBias, short standardStartMonth, short standardStartDay, short standardStartDayOfWeek,
                      short standardStartHour, short standardStartMinute, short daylightBias, short daylightStartMonth,
                      short daylightStartDay, short daylightStartDayOfWeek, short daylightStartHour, short daylightStartMinute) {
        m_standardBias = standardBias;
        m_standardStartMonth = standardStartMonth; //Z7 month range is 1 - 12 while CALENDAR.MONTH range is 0 - 11
        m_standardStartDay = standardStartDay;
        m_standardStartDayOfWeek = standardStartDayOfWeek;
        m_standardStartHour = standardStartHour;
        m_standardStartMinute = standardStartMinute;
        m_daylightStartMonth = daylightStartMonth;
        m_daylightStartDay = daylightStartDay;
        m_daylightStartDayOfWeek = daylightStartDayOfWeek; //Z7 DOW range is 0 - 6 while CALENDAR.DAY_OF_WEEK range is 1 - 7
        m_daylightStartHour = daylightStartHour;
        m_daylightStartMinute = daylightStartMinute;
        m_daylightBias = daylightBias;
        m_timezoneName = "Z7TimeZone";
    }

    // TODO: We should override the setter methods in SimpleTimeZone
    // to ensure that our internal variables stay up-to-date if the
    // time zone details are updated

    public short getStandardBias() {
        //return (short)(getRawOffset() / 60 / 1000);
        return m_standardBias;
    }

    public short getStandardStartMonth() {
        return m_standardStartMonth;
    }

    public short getStandardStartDay() {
        return m_standardStartDay;
    }

    public short getStandardStartDayOfWeek() {
        return m_standardStartDayOfWeek;
    }

    public short getStandardStartHour() {
        return m_standardStartHour;
    }

    public short getStandardStartMinute() {
        return m_standardStartMinute;
    }

    public short getDaylightBias() {
        //return (short)((short)(getRawOffset() / 60 / 1000) - (short)(getDSTSavings() / 60 / 1000));
        return m_daylightBias;
    }

    public short getDaylightStartMonth() {
        return m_daylightStartMonth;
    }

    public short getDaylightStartDay() {
        return m_daylightStartDay;
    }

    public short getDaylightStartDayOfWeek() {
        return m_daylightStartDayOfWeek;
    }

    public short getDaylightStartHour() {
        return m_daylightStartHour;
    }

    public short getDaylightStartMinute() {
        return m_daylightStartMinute;
    }


    public boolean isEqual(Z7TimeZone timeZone) {
        if (this.m_daylightBias - timeZone.getDaylightBias() != 0)
            return false;

        if (this.m_daylightStartDay != timeZone.getDaylightStartDay() ||
                this.m_daylightStartDayOfWeek != timeZone.getDaylightStartDayOfWeek() ||
                this.m_daylightStartHour != timeZone.getDaylightStartHour() ||
                this.m_daylightStartMinute != timeZone.getDaylightStartMinute() ||
                this.m_daylightStartMonth != timeZone.getDaylightStartMonth() ||
                this.m_standardBias != timeZone.getStandardBias() ||
                this.m_standardStartDay != timeZone.getStandardStartDay() ||
                this.m_standardStartDayOfWeek != timeZone.getStandardStartDayOfWeek() ||
                this.m_standardStartHour != timeZone.getStandardStartHour() ||
                this.m_standardStartMinute != timeZone.getStandardStartMinute() ||
                this.m_standardStartMonth != timeZone.getStandardStartMonth())
            return false;
        else
            return true;
    }

    //https://jira.seven.com/requests/browse/ZSEVEN-11321
    //compare timezone ignore start hour/minute for both standard&daylight.
    public boolean isEqualIgnoreStartHour(Z7TimeZone timeZone) {
        if (this.m_daylightBias - timeZone.getDaylightBias() != 0)
            return false;

        if (this.m_daylightStartDay != timeZone.getDaylightStartDay() ||
                this.m_daylightStartDayOfWeek != timeZone.getDaylightStartDayOfWeek() ||
                //this.m_daylightStartHour != timeZone.getDaylightStartHour() ||
                //this.m_daylightStartMinute != timeZone.getDaylightStartMinute() ||
                this.m_daylightStartMonth != timeZone.getDaylightStartMonth() ||
                this.m_standardBias != timeZone.getStandardBias() ||
                this.m_standardStartDay != timeZone.getStandardStartDay() ||
                this.m_standardStartDayOfWeek != timeZone.getStandardStartDayOfWeek() ||
                //this.m_standardStartHour != timeZone.getStandardStartHour() ||
                //this.m_standardStartMinute != timeZone.getStandardStartMinute() ||
                this.m_standardStartMonth != timeZone.getStandardStartMonth())
            return false;
        else
            return true;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(200);
        toString(buf);
        return buf.toString();
    }

    public void toString(StringBuffer buf) {
        buf.append("std: bias ").append(m_standardBias).append(", month ").append(m_standardStartMonth).append(", day ").append(m_standardStartDay).append(", dow ").append(m_standardStartDayOfWeek).append(", hour ").append(m_standardStartHour).append(", minute ").append(m_standardStartMinute);
        buf.append(", dst: bias ").append(m_daylightBias).append(", month ").append(m_daylightStartMonth).append(", day ").append(m_daylightStartDay).append(", dow ").append(m_daylightStartDayOfWeek).append(", hour ").append(m_daylightStartHour).append(",minute ").append(m_daylightStartMinute);
    }

    public boolean isInDST(int year, int month, int dayOfMonth, int dayOfWeek, int hour, int minute) {

        // Data must be interpreted as
        // (m_daylightStartDay) - ('m_daylightStartDayOfWeek') of (m_daylightStartDayOfMonth)
        // ('first'/'second'/'third'/'fourth'/'last') - ('Sun' to 'Sat') of ('Jan' to 'Dec')

        if (m_standardBias == m_daylightBias) // No DST
            return false;


        if (month > (m_daylightStartMonth - 1) && month < (m_standardStartMonth - 1)) {
            return true;
        } else if (month == (m_daylightStartMonth - 1)) {

            int dstStartDay = getDayOfMonth(m_daylightStartDay, m_daylightStartDayOfWeek,
                    m_daylightStartMonth - 1, year, getStandardTimeZone());

            if (dayOfMonth > dstStartDay) {
                return true;
            } else if (dayOfMonth == dstStartDay) {
                if (hour > m_daylightStartHour) {
                    return true;
                } else if (hour == m_daylightStartHour && minute >= m_daylightStartMinute) {
                    return true;
                }
            }
        } else if (month == (m_standardStartMonth - 1)) {
            int dstEndDay = getDayOfMonth(m_standardStartDay, m_standardStartDayOfWeek,
                    m_standardStartMonth - 1, year, getDSTTimeZone());

            if (dayOfMonth < dstEndDay) {
                return true;
            } else if (dayOfMonth == dstEndDay) {
                if (hour < m_standardStartHour) {
                    return true;
                } else if (hour == m_standardStartHour && minute < m_standardStartMinute) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInDST(Date onDate) {
        String standardTZ = getTimeZoneID(m_standardBias);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(standardTZ));
        cal.setTime(onDate);

        int month = cal.get(Calendar.MONTH);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int year = cal.get(Calendar.YEAR);

        return isInDST(year, month, dayOfMonth, dayOfWeek, hour, minute);
    }

    /**
     * Creates a String TimeZoneID from the Z7TimeZone object
     *
//     * @param z7t
     * @return (String) TimeZoneID - E.g.: "GMT-8:00", "GMT+5:30"
     */
    public String getCurrentTimeZoneID() {

        String tZoneID = getTimeZoneID(m_standardBias);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(tZoneID));
        if (isInDST(cal.getTime())) {
            tZoneID = getTimeZoneID(m_daylightBias);
        }

        return tZoneID;
    }

    private String getTimeZoneID(int bias) {
        int hours = bias / 60;
        int mins = bias % 60;

        String offsetType = "-";
        if (hours < 0) {
            hours = -(hours);
            mins = -(mins);
            offsetType = "+";
        }

        String tZoneID = "GMT" + offsetType + "" + hours + ":"
                + ((mins == 0) ? "00" : ("" + mins));

        return tZoneID;
    }

    public TimeZone getStandardTimeZone() {
        return TimeZone.getTimeZone(getTimeZoneID(m_standardBias));
    }

    public TimeZone getDSTTimeZone() {
        return TimeZone.getTimeZone(getTimeZoneID(m_daylightBias));
    }

    public TimeZone getTimeZone(Date onDate) {
        if (isInDST(onDate))
            return getDSTTimeZone();
        else
            return getStandardTimeZone();
    }

    private int getDayOfMonth(int dayInstanceNumber, int dayOfWeek, int month, int year, TimeZone tz) {
        int[] daysInMonth = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        daysInMonth[Calendar.FEBRUARY] = ((year % 100 == 0) ? (year % 400 == 0)
                : (year % 4 == 0)) ? 29 : 28; // adjust for Leap Year
        Calendar cal = Calendar.getInstance(getStandardTimeZone());
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.getTime();
        int dow = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int dayInstances[] = {0, 0, 0, 0, 0, 0, 0};
        int tmpDay = 0;
        dow = dow - 1;

        while (tmpDay < daysInMonth[month]) {
            tmpDay++;
            dow = (dow + 1) % 7;
            dayInstances[dow]++;
            if (dow == dayOfWeek &&
                    (dayInstances[dow] == dayInstanceNumber
                            || ((tmpDay + 7) > daysInMonth[month] && dayInstanceNumber == 5))
                    ) {
                // We found the correct day
                break;
            }
        }
        return tmpDay;
    }
}

