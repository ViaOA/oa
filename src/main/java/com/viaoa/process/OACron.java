package com.viaoa.process;

import java.util.Arrays;
import java.util.Calendar;

import com.viaoa.hub.HubEvent;
import com.viaoa.util.OAArray;
import com.viaoa.util.OAConv;
import com.viaoa.util.OADate;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAString;


/**
 * Used to define and find the next time a Cron-like entry should be ran.

        *     *     *   *    *        command to be executed
        -     -     -   -    -
        |     |     |   |    |
        |     |     |   |    +----- day of week (0-6) (Sunday=0) - Java: 1-7 (Sunday=1)
        |     |     |   +---------- month (1-12) - Java: 0-11
        |     |     +-------------- day of month (1-31), also allows "last"
        |     +-------------------- hour (0 - 23)
        +-------------------------- min (0 - 59)

 Each field needs to be separate by space or tab.
 Field values can use a single number, "-" for a range, and commas to separate more then one.

 @see OACronProcessor# to register cron to be processed

 @author vvia
 */
public abstract class OACron {
    private OADateTime dtFrom;
    private String strMins;
    private String strHours;
    private String strDayOfMonth;
    private String strMonth;
    private String strDayOfWeek;

    // store sorted cron values (not Java)
    private int[] mins;
    private int[] hrs;
    private int[] monthDays;
    private int[] daysOfWeek;
    private int[] months;

    private boolean bIncludeLastDayOfMonth;

    private String description;
    private boolean bValid;


    public OACron(String strMins, String strHours, String strDayOfMonth, String strMonth, String strDayOfWeek) {
        this.strMins = strMins;
        this.strHours = strHours;
        this.strDayOfMonth = strDayOfMonth;
        this.strMonth = strMonth;
        this.strDayOfWeek = strDayOfWeek;

        bValid = true;
        mins = getInts(strMins);
        hrs = getInts(strHours);
        monthDays = getInts(strDayOfMonth, true);
        daysOfWeek = getInts(strDayOfWeek);
        months = getInts(strMonth);

        if (bValid) {
            for (int x : mins) {
                if (x < 0 || x > 59) bValid = false;
            }
            for (int x : hrs) {
                if (x < 0 || x > 23) bValid = false;
            }
            for (int x : monthDays) {
                if (x < 1 || x > 31) bValid = false;
            }
            for (int x : daysOfWeek) {
                if (x < 0 || x > 6) bValid = false;
            }
            for (int x : months) {
                if (x < 1 || x > 12) bValid = false;
            }
        }
    }

    /**
     * Called when it's time to process.
     */
    protected abstract void process() ;
    
    
    public int[] getMinutes() {
        return mins;
    }
    public int[] getHours() {
        return hrs;
    }
    public int[] getMonthDays() {
        return monthDays;
    }
    public int[] getDaysOfWeek() {
        return daysOfWeek;
    }
    public int[] getMonths() {
        return months;
    }
    public boolean getIncludeLastDayOfMonth() {
        return bIncludeLastDayOfMonth;
    }

    public boolean isValid() {
        return bValid;
    }

    public String getDescription() {
        if (description != null) return description;
        description = "";
        int x;

        if (months.length > 0) {
            if (description.length() > 0) description += "; and";
            else description += "when";
            description += " month is ";

            for (int i=0; i<months.length; i++) {
                x = months[i];
                if (i > 0) description += " or ";
                if (x < 1 || x > 12) description += "Invalid:"+x;
                else {
                    OADate d = new OADate(2017, x-1, 1);
                    description += d.toString("MMM");
                }
            }
        }

        if (monthDays.length > 0 || bIncludeLastDayOfMonth) {
            if (description.length() > 0) description += "; and";
            else description += "when";
            description += " day of month is ";
            for (int i=0; i<monthDays.length; i++) {
                x = monthDays[i];
                if (i > 0) description += " or ";
                if (x < 0 || x > 31) description += "Invalid:"+x;
                else description += ""+x;
            }
            if (bIncludeLastDayOfMonth) {
                if (monthDays.length > 0) description += " or ";
                description += "last day";
            }
        }

        if (daysOfWeek.length > 0) {
            if (description.length() > 0) description += "; and";
            else description += "when";
            description += " day of week is ";


            for (int i=0; i<daysOfWeek.length; i++) {
                x = daysOfWeek[i];
                if (i > 0) description += " or ";
                if (x < 0 || x > 6) description += "Invalid:"+x;
                else {
                    String s;
                    for (int j=0; j<7; j++) {
                        OADate d = new OADate(2017, 0, 1+j);
                        if (d.getDayOfWeek() != x+1) continue;
                        description += d.toString("EEE");
                        break;
                    }
                }
            }
        }

        if (hrs.length > 0) {
            if (description.length() > 0) description += "; and";
            else description += "when";
            description += " hour is ";
            for (int i=0; i<hrs.length; i++) {
                x = hrs[i];
                if (i > 0) description += " or ";
                if (x < 0 || x > 23) description += "Invalid:"+x;
                else description += ""+x;
            }
        }


        if (mins.length > 0) {
            if (description.length() > 0) description += "; and";
            else description += "when";
            description += " minute is ";
            for (int i=0; i<mins.length; i++) {
                x = mins[i];
                if (i > 0) description += " or ";
                if (x < 0 || x > 59) description += "Invalid:"+x;
                else description += ""+x;
            }
        }


        if (!isValid()) description = "INVALID: "+description;

        return description;
    }

    public OADateTime findNext(OADateTime dtFrom) {
        if (!isValid()) return null;
        if (dtFrom == null) dtFrom = new OADateTime();
        this.dtFrom = dtFrom;


        OADateTime dtFound = findNextMonth();
        return dtFound;
    }


    private OADateTime findNextMonth() {
        OADateTime dtFound = null;

        final int fromMonth = dtFrom.getMonth();
        if (months == null || months.length == 0) { // any month
            months = new int[12];
            for (int i=0; i<12; i++) months[i] = i+1; // cron month is 1-12, java is 0-11
        }

        OADateTime dtCheck = new OADateTime(dtFrom.getTime());
        for (int i=0; ;i++) {
            if (i > 0) dtCheck = dtCheck.addYears(1);
            for (int m : months) {
                m--; // cron month is 1-12, java is 0-11

                if (i == 0 && m < dtFrom.getMonth()) continue;

                dtCheck.setMonth(m);
                dtCheck.setDay(1);
                dtCheck.clearTime();
                if (dtFound != null && dtFound.before(dtCheck)) continue;
                OADateTime dtTo = dtCheck.addMonths(1);

                if (dtCheck.before(dtFrom)) dtCheck = new OADateTime(dtFrom);

                dtFound = findNextMonthDay(dtFound, dtTo, dtCheck);
            }
            if (dtFound != null) break;
        }
        if (dtFound != null) {
            dtFound.setSecond(0); // also clears ms
            // dtFound.setMilliSecond(0);
        }
        return dtFound;
    }
    private OADateTime findNextMonthDay(OADateTime dtFound, OADateTime dtTo, OADateTime dtCheck) {
        if (!bIncludeLastDayOfMonth && (monthDays == null || monthDays.length == 0)) {
            dtFound = findClosestDayOfWeek(dtFound, dtTo, dtCheck);
        }
        else {
            OADateTime dtx = new OADateTime(dtFrom);
            dtx.clearTime();

            int max = dtCheck.getDaysInMonth();
            for (int i=0; i<=monthDays.length; i++) {
                int md;
                if (i == monthDays.length) {
                    if (!bIncludeLastDayOfMonth) continue;
                    md = max;
                }
                else md = monthDays[i];

                if (md > max) continue;

                dtCheck.setDay(md);
                if (dtCheck.before(dtx)) continue;
                if (dtFound != null && dtFound.before(dtCheck)) continue;

                OADateTime dtTo2 = dtCheck.addDays(1);
                dtTo2.clearTime();

                dtFound = findClosestDayOfWeek(dtFound, dtTo2, dtCheck);
            }
        }
        return dtFound;
    }

    // Java Sunday=1, cron Sunday=0
    private OADateTime findClosestDayOfWeek(OADateTime dtFound, OADateTime dtTo, OADateTime dtCheck) {
        if (daysOfWeek == null || daysOfWeek.length == 0) { // any dayOfWeek
            for (;;) {
                dtFound = findClosestHour(dtFound, dtTo, dtCheck);
                if (dtFound != null) break;
                dtCheck = dtCheck.addDays(1);
                dtCheck.clearTime();
                if (dtTo != null && dtTo.compareTo(dtCheck) <= 0) break;
            }
        }
        else {
            int fromDayOfWeek = dtCheck.getDayOfWeek();
            OADateTime dtHold = dtCheck;

            int fromWd = dtHold.getDayOfWeek();
            for (int i=0; i<2; i++) {
                for (int wd : daysOfWeek) {
                    wd++;  // cron day is 1 greater then java day

                    int diff;
                    if (i > 0) {
                        if (wd != fromWd) continue;
                        diff = 7;
                    }
                    else {
                        if (fromWd > wd) diff = (wd+7) - fromWd;
                        else diff = wd - fromWd;
                    }

                    dtCheck = dtHold.addDays(diff);
                    dtCheck.clearTime();

                    if (dtTo != null && dtTo.compareTo(dtCheck) <= 0) continue;

                    if (dtFound != null && dtFound.before(dtCheck)) continue;

                    dtFound = findClosestHour(dtFound, dtTo, dtCheck);
                }
                if (dtFound != null) break;
            }
        }
        return dtFound;
    }
    private OADateTime findClosestHour(OADateTime dtFound, OADateTime dtTo, OADateTime dtCheck) {
        if (hrs == null || hrs.length == 0) { // any hour
            dtFound = findClosestMinute(dtFound, dtTo, dtCheck);
            if (dtFound == null) {
                dtCheck = dtCheck.addHours(1);
                if (dtTo == null || dtTo.compareTo(dtCheck) > 0) {
                    dtFound = findClosestMinute(dtFound, dtTo, dtCheck);
                }
            }
        }
        else {
            dtCheck.setMinute(0);
            dtCheck.setSecond(0);
            dtCheck.setMilliSecond(0);
            OADateTime dtx = new OADateTime(dtFrom);
            dtx.clearTime();
            dtx.set24Hour(dtFrom.get24Hour());

            for (int hr : hrs) {
                dtCheck.set24Hour(hr);
                if (dtCheck.before(dtx)) continue;
                if (dtFound != null && dtFound.before(dtCheck)) continue;
                dtFound = findClosestMinute(dtFound, dtTo, dtCheck);
            }
        }
        return dtFound;
    }
    private OADateTime findClosestMinute(OADateTime dtFound, OADateTime dtTo, OADateTime dtCheck) {
        if (mins == null || mins.length == 0) {
            if (dtFound == null || dtCheck.before(dtFound)) {
                dtFound = new OADateTime(dtCheck);
                if (dtFound.equals(dtFrom)) dtFound = dtFound.addMinutes(1);
            }
            return dtFound;
        }
        else {
            for (int m : mins) {
                dtCheck.setMinute(m);
                if (dtCheck.compareTo(dtFrom) <= 0) continue;
                if (dtFound != null && dtFound.before(dtCheck)) continue;
                if (dtCheck.before(dtFrom)) continue;
                if (dtFound == null || dtCheck.before(dtFound)) {
                    dtFound = new OADateTime(dtCheck);
                }
            }
        }
        return dtFound;
    }


    /**
     * parses values from String
     * @param line
     * @return values to include or [0]=all
     */
    private int[] getInts(String line) {
        return getInts(line, false);
    }
    private int[] getInts(String line, boolean bAllowLast) {
        if (OAString.isEmpty(line)) return new int[0];
        if (line.indexOf('*') >= 0) return new int[0]; // all

        line = line.replace(',', ' ');
        line = line.replace(':', ' ');

        int[] ints = new int[0];
        for (String s : line.split("\\s+")) {
            String[] ss = s.split("\\-");
            if (ss == null || ss.length == 0) continue;
            s = ss[0];
            if (!OAString.isInteger(s)) {
                if (bAllowLast && s.equalsIgnoreCase("last")) {
                    bIncludeLastDayOfMonth = true;
                }
                else bValid = false;
                continue;
            }
            int x = OAConv.toInt(s);

            if (ss.length == 2) {
                if (!OAString.isInteger(ss[1])) {
                    bValid = false;
                    ints = OAArray.add(ints, x);
                    continue;
                }
                int x2 = OAConv.toInt(ss[1]);
                for (int i=x; i<=x2; i++) {
                    ints = OAArray.add(ints, i);
                }
            }
            else {
                ints = OAArray.add(ints, x);
            }
        }

        Arrays.sort(ints);
        return ints;
    }

}
