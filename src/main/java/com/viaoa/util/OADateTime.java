/*  Copyright 1999-2017 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.util;

import java.util.*;
import java.text.*;
import java.io.IOException;
import java.sql.Time;

/*
    Superclass of OADate and OATime that combines Calendar, Date and SimpleDateFormat.

    'Hms', 'Mdy'

    yyyyMMdd_HHmmss.SSS
             hhmmssa

    JSON
        yyyy-MM-dd'T'HH:mm:ssX
            Notice the X on the end. It will handle timezones in ISO 8601 standard
            see: http://stackoverflow.com/questions/19112357/java-simpledateformatyyyy-mm-ddthhmmssz-gives-timezone-as-ist
            ex: 2016-11-22T08:49:02-05
        yyyy-MM-dd'T'HH:mm:ssXX
            ex: 2016-11-22T08:50:12-0500
        yyyy-MM-dd'T'HH:mm:ssXXX
            ex: 2016-11-22T08:49:02-05:00

    javascript Date.toString()    EEE MMM dd yyyy '00:00:00' 'GMT'Z '('z')'

    <p>
    Formatting Symbols used for output display.

    SEE: https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
    <pre>
    G  era designator          (Text)              AD
    y  year                    (Number)            1996
    M  month in year           (Number)            1, 2, 3, 4 .. 10, 11, 12
    MM                         (Number)            01, 02, 03, 04 ... 10, 11, 12
    MMM                        (Text)              Jan, Feb, ... Dec
    MMMM                       (Text)              January, February, ... December
    d  day in month            (Number)            10
    h  hour in am/pm (1~12)    (Number)            12
    H  hour in day (0~23)      (Number)            0
    m  minute in hour          (Number)            30
    s  second in minute        (Number)            55
    S  millisecond             (Number)            978
    E  day in week             (Text)              Tues
    EE
    EEE
    EEEE  day in week          (Text)              Tuesday
    D  day in year             (Number)            189
    F  day of week in month    (Number)            2 (2nd Wed in July)
    w  week in year            (Number)            27
    W  week in month           (Number)            2
    a  am/pm marker            (Text)              PM
    k  hour in day (1~24)      (Number)            24
    K  hour in am/pm (0~11)    (Number)            0
    z  time zone               (Text)              PST
    zzzz                                           Pacific Standard Time
    X                          (hours)             -04
    XX                         (hrsMins)           -0400
    XXX                        (hrs:mins)          -04:00
    Z                          (hrsMins)           -0400
    ZZ   (same as using Z)                         -0400
    ZZZ  (same as using Z)                         -0400

    '  escape for text         (Delimiter)

    '' single quote            (Literal)           '

    Examples:
    "yyyy.MM.dd G 'at' HH:mm:ss z"    ->>  1996.07.10 AD at 15:08:56 PDT
    "EEE, MMM d, ''yy"                ->>  Wed, July 10, '96
    "h:mm a"                          ->>  12:08 PM
    "hh 'o''clock' a, zzzz"           ->>  12 o'clock PM, Pacific Daylight Time
    "K:mm a, z"                       ->>  0:00 PM, PST
    "yyyy.MMMMM.dd GGG hh:mm aaa"     ->>  1996.July.10 AD 12:08 PM
    "yyyy.MM.dd HH:mm:ss.SSS"


    "E dd M yyyy hh:mm:ss a z"          ->> Thu 30 3 2017 11:58:21 AM EDT
    "EE dd MM yyyy hh:mm:ss a zz"       ->> Thu 30 03 2017 11:58:52 AM EDT
    "EEE dd MMM yyyy hh:mm:ss a zzz"    ->> Thu 30 Mar 2017 11:59:35 AM EDT
    "EEEE dd MMMM yyyy hh:mm:ss a zzzz" ->> Thursday 30 March 2017 12:00:33 PM Eastern Daylight Time



    </pre>

    <br>
    Formatting is used to convert OADateTime to a String and also for parsing a String to create
    an OADateTime.
    <p><b>Note:</b>
    OADateTimes are not affected by timezone.  A date/time created on one system will be the same on
    another machine, even if the timezone is different.

    @see #setGlobalOutputFormat
    @see java.text.SimpleDateFormat
*/
public class OADateTime implements java.io.Serializable, Comparable {
    private static final long serialVersionUID = 1L;

    
    private final static String FORMAT_long = "yyyy/MM/dd hh:mm:ss.S a";
    
    
    protected long _time;
    protected TimeZone timeZone;

    protected String format;

    private static final TimeZone defaultTimeZone;

    private static SimpleDateFormat[] simpleDateFormats;
    private static int simpleDateFormatCounter;
    static {
        // used by getFormatter()
        simpleDateFormats = new SimpleDateFormat[12];  // keeps a pool of 12 that are shared in a "round robin" pool
    }

    /** default output format */
    protected static String staticOutputFormat;

    /** default parse formats */
    private static Vector vecDateTimeParseFormat;

    static {
        setLocale(Locale.getDefault());
        defaultTimeZone = TimeZone.getDefault();
    }

    // use a pool of GregorianCalendar since they are so heavy
    private static OAPool<GregorianCalendar> poolGregorianCalendar = new OAPool<GregorianCalendar>(GregorianCalendar.class, 20, 50) {
        @Override
        protected GregorianCalendar create() {
            GregorianCalendar cal = new GregorianCalendar();
            return cal;
        }

        @Override
        protected void removed(GregorianCalendar resource) {
        }
    };

    protected GregorianCalendar _getCal() {
        GregorianCalendar cal = poolGregorianCalendar.get();
        cal.setTimeInMillis(_time);

        TimeZone tz = timeZone!=null?timeZone:defaultTimeZone;
        if (cal.getTimeZone() != tz) cal.setTimeZone(tz);
        return cal;
    }

    protected void _releaseCal(GregorianCalendar cal) {
        poolGregorianCalendar.release(cal);
    }

    
    private static Locale locale;
    public static void setLocale(Locale loc) {
        locale = loc;
        vecDateTimeParseFormat = new Vector(15,10);
        String s = getFormat(DateFormat.SHORT, locale);
        boolean bMonthFirst = true;
        boolean bYearFirst = false;
        if (s != null && s.length() > 0) {
            char ch = s.charAt(0);
            if (ch != 'M') bMonthFirst = false;
            if (ch == 'y') bYearFirst = true;;
        }
        if (bMonthFirst) {
            staticOutputFormat = "MM/dd/yyyy hh:mma";
            // the "yy" formats must be before the "yyyy" formats because "yyyy" will convert "05/04/65" -> "05/04/0065"
            vecDateTimeParseFormat.addElement("MM/dd/yy hh:mm:ss.Sa");
            vecDateTimeParseFormat.addElement("MM/dd/yy hh:mm:ssa");
            vecDateTimeParseFormat.addElement("MM/dd/yy hh:mma");

            vecDateTimeParseFormat.addElement("MM/dd/yy hh:mm:ss.S a");
            vecDateTimeParseFormat.addElement("MM/dd/yy hh:mm:ss a");
            vecDateTimeParseFormat.addElement("MM/dd/yy hh:mm a");

            vecDateTimeParseFormat.addElement("MM/dd/yy HH:mm:ss.S");
            vecDateTimeParseFormat.addElement("MM/dd/yy HH:mm:ss");
            vecDateTimeParseFormat.addElement("MM/dd/yy HH:mm");

            vecDateTimeParseFormat.addElement("MM/dd/yyyy hh:mm:ss.Sa");
            vecDateTimeParseFormat.addElement("MM/dd/yyyy hh:mm:ssa");
            vecDateTimeParseFormat.addElement("MM/dd/yyyy hh:mma");

            vecDateTimeParseFormat.addElement("MM/dd/yyyy HH:mm:ss.S");
            vecDateTimeParseFormat.addElement("MM/dd/yyyy HH:mm:ss");
            vecDateTimeParseFormat.addElement("MM/dd/yyyy HH:mm");
        }
        else if (bYearFirst) {
            staticOutputFormat = "yyyy/MM/dd hh:mma";
            // the "yy" formats must be before the "yyyy" formats because "yyyy" will convert "05/04/65" -> "05/04/0065"
            vecDateTimeParseFormat.addElement("yy/MM/dd hh:mm:ss.Sa");
            vecDateTimeParseFormat.addElement("yy/MM/dd hh:mm:ssa");
            vecDateTimeParseFormat.addElement("yy/MM/dd hh:mma");

            vecDateTimeParseFormat.addElement("yy/MM/dd HH:mm:ss.S");
            vecDateTimeParseFormat.addElement("yy/MM/dd HH:mm:ss");
            vecDateTimeParseFormat.addElement("yy/MM/dd HH:mm");

            vecDateTimeParseFormat.addElement("yyyy/MM/dd hh:mm:ss.Sa");
            vecDateTimeParseFormat.addElement("yyyy/MM/dd hh:mm:ssa");
            vecDateTimeParseFormat.addElement("yyyy/MM/dd hh:mma");

            vecDateTimeParseFormat.addElement("yyyy/MM/dd HH:mm:ss.S");
            vecDateTimeParseFormat.addElement("yyyy/MM/dd HH:mm:ss");
            vecDateTimeParseFormat.addElement("yyyy/MM/dd HH:mm");
        }
        else {  // day first
            staticOutputFormat = "dd/MM/yyyy hh:mma";
            // the "yy" formats must be before the "yyyy" formats because "yyyy" will convert "05/04/65" -> "05/04/0065"
            vecDateTimeParseFormat.addElement("dd/MM/yy hh:mm:ss.Sa");
            vecDateTimeParseFormat.addElement("dd/MM/yy hh:mm:ssa");
            vecDateTimeParseFormat.addElement("dd/MM/yy hh:mma");

            vecDateTimeParseFormat.addElement("dd/MM/yy HH:mm:ss.S");
            vecDateTimeParseFormat.addElement("dd/MM/yy HH:mm:ss");
            vecDateTimeParseFormat.addElement("dd/MM/yy HH:mm");

            vecDateTimeParseFormat.addElement("dd/MM/yyyy hh:mm:ss.Sa");
            vecDateTimeParseFormat.addElement("dd/MM/yyyy hh:mm:ssa");
            vecDateTimeParseFormat.addElement("dd/MM/yyyy hh:mma");

            vecDateTimeParseFormat.addElement("dd/MM/yyyy HH:mm:ss.S");
            vecDateTimeParseFormat.addElement("dd/MM/yyyy HH:mm:ss");
            vecDateTimeParseFormat.addElement("dd/MM/yyyy HH:mm");
        }
        // SQL date formats
        vecDateTimeParseFormat.addElement("yyyy-MM-dd HH:mm:ss");
        vecDateTimeParseFormat.addElement("yyyy-MM-dd");

        vecDateTimeParseFormat.addElement(getFormat(DateFormat.SHORT));
        vecDateTimeParseFormat.addElement(getFormat(DateFormat.MEDIUM));
        vecDateTimeParseFormat.addElement(getFormat(DateFormat.LONG));
        vecDateTimeParseFormat.addElement(getFormat(DateFormat.DEFAULT));
    }

    /**
        Creates new datetime, using current date and time.
    */
    public OADateTime() {
        this._time = System.currentTimeMillis();
    }

    /**
        Creates new datetime, using time parameter.
    */
    public OADateTime(java.sql.Time time) {
        this._time = time.getTime();
    }
    /**
        Creates new datetime, using date parameter.
    */
    public OADateTime(Date date) {
        if (date == null) this._time = System.currentTimeMillis();
        else this._time = date.getTime();
    }
    /**
        Creates new datetime, using date parameter.
    */
    public OADateTime(long time) {
        this._time = time;
    }
    /**
        Creates new datetime, using timestamp parameter.
    */
    public OADateTime(java.sql.Timestamp date) {
        if (date == null) this._time = System.currentTimeMillis();
        else this._time = date.getTime();
    }
    /**
        Creates new datetime, using Calendar parameter.
    */
    public OADateTime(Calendar c) {
        if (c == null) this._time = System.currentTimeMillis();
        else this._time = c.getTimeInMillis();
        this.timeZone = c.getTimeZone();
    }

    /**
        Creates new datetime, using OADateTime parameter.
    */
    public OADateTime(OADateTime odt) {
        if (odt == null) this._time = System.currentTimeMillis();
        else this._time = odt.getTime();
        this.timeZone = odt.timeZone;
    }

    /**
        Creates new datetime, using String parameter.
        @see #valueOf(String)
    */
    public OADateTime(String strDate) {
        setCalendar(strDate);
    }

    /**
        Creates new datetime, using String parameter and format.
        @see #valueOf(String)
    */
    public OADateTime(String strDate, String format) {
        setCalendar(strDate, format);
    }

    /**
        Creates new datetime, using date and time.
    */
    public OADateTime(OADate d, OATime t) {
        if (d == null) d = new OADate();
        this._time = d.getTime();
        this.timeZone = d.timeZone;
        
        if (t != null) {
            setTime(t);
            //was: this._time += t._time;  // wrong: does not account for tz
        }
    }

    /**
        @param year full year (not year minus 1900 like Date)
        @param month 0-11
        @param day day of the month
     */
    public OADateTime(int year, int month, int day) {
        this(new Date(year-1900, month, day));
    }
    public OADateTime(int year, int month, int day, int hrs, int mins) {
        this(new Date(year-1900, month, day, hrs, mins));
    }
    public OADateTime(int year, int month, int day, int hrs, int mins, int secs) {
        this(new Date(year-1900, month, day, hrs, mins, secs));
    }
    /**
       @param year full year (not year minus 1900 like Date)
       @param month 0-11
       @param day day of the month
    */
    public OADateTime(int year, int month, int day, int hrs, int mins, int secs, int milsecs) {
        this(new Date(year-1900, month, day, hrs, mins, secs));
        this._time += milsecs;
    }

    // This will fix the bug in JDK and will keep date/times the same across different timezones.
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeInt(9999); // version
        stream.writeLong(_time);
    }
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        // might want to add TimeZone
        // in.defaultReadObject();
        int x = in.readInt();
        if (x == 9999) {
            _time = in.readLong();
        }
        else { // previous format
            int year = x;
            int month = in.readInt();
            int day = in.readInt();
            int hour = in.readInt();
            int minute = in.readInt();
            int second = in.readInt();
            int milisecond = in.readInt();

            Date d = new Date(year-1900, month, day, hour, minute, second);
            this._time = d.getTime();
            this._time += milisecond;
        }
    }

    private int getField(int fld) {
        int x;
        GregorianCalendar c = _getCal();
        try {
            x = c.get(fld);
        }
        finally {
            _releaseCal(c);
        }
        return x;
    }


    /**
        Returns a clone of the calendar used by this object.
    */
    public Calendar getCalendar() {
        Calendar cNew;
        GregorianCalendar c = _getCal();
        try {
            cNew = (Calendar) c.clone();
        }
        finally {
            poolGregorianCalendar.release(c);
        }
        return cNew;
    }


    // conversions
    protected void setCalendar(int year, int month, int day, int hrs, int mins, int secs, int milsecs) {
        long t = new Date(year-1900, month, day, hrs, mins, secs).getTime();
        this._time = t + milsecs;
    }
    protected void setCalendar(GregorianCalendar c) {
        if (c == null) this._time = System.currentTimeMillis();
        else {
            this._time = c.getTimeInMillis();
            this.timeZone = c.getTimeZone();
        }
    }

    protected void setCalendar(java.sql.Timestamp date) {
        if (date == null) this._time = System.currentTimeMillis();
        else this._time = date.getTime();
    }

    protected void setCalendar(Date date) {
        if (date == null) this._time = System.currentTimeMillis();
        else this._time = date.getTime();
    }

    protected void setCalendar(Time time) {
        if (time == null) this._time = System.currentTimeMillis();
        else this._time = time.getTime();
    }

    protected void setCalendar(OADateTime dt) {
        if (dt == null) this._time = System.currentTimeMillis();
        else {
            this._time = dt.getTime();
            this.timeZone = dt.timeZone;
        }
    }
    protected void setCalendar(String strDate) {
        if (strDate == null) this._time = System.currentTimeMillis();
        else {
            OADateTime dt = valueOf(strDate);
            if (dt == null) throw new IllegalArgumentException("OADateTime cant create date from String \""+strDate+"\"");
            setCalendar(dt);
        }
    }
    protected void setCalendar(String strDate, String fmt) {
        if (strDate == null) this._time = System.currentTimeMillis();
        else {
            OADateTime dt = valueOf(strDate, fmt);
            if (dt == null) throw new IllegalArgumentException("OADateTime cant create date from String \""+strDate+"\"");
            setCalendar(dt);
        }
    }




    /**
        Sets hour,minutes and seconds to zero.
    */
    public void clearTime() {
        if (timeZone == null || timeZone == defaultTimeZone) {
            Date dThis = new Date(_time);

            Date d = new Date(dThis.getYear(), dThis.getMonth(), dThis.getDate());
            _time = d.getTime();
            return;
        }
        GregorianCalendar c = _getCal();
        try {
            c.set(c.HOUR_OF_DAY, 0);
            c.set(c.MINUTE, 0);
            c.set(c.SECOND, 0);
            c.set(c.MILLISECOND, 0);
            _time = c.getTimeInMillis();
        }
        finally {
            _releaseCal(c);
        }
    }

    /**
        sets date to 1/1/1970  (time 0)
    */
    public void clearDate() {
        if (timeZone == null || timeZone == defaultTimeZone) {
            Date dThis = new Date(_time);
            long ms = getMilliSecond();
            Date d = new Date(70, 0, 1, dThis.getHours(), dThis.getMinutes(), dThis.getSeconds());
            _time = d.getTime();
            if (ms > 0) _time += ms;
            return;
        }
        GregorianCalendar c = _getCal();
        try {
            c.set(c.YEAR, 1970);
            c.set(c.MONTH, c.JANUARY);
            c.set(c.DATE, 1);
            _time = c.getTimeInMillis();
        }
        finally {
            _releaseCal(c);
        }
    }


    /**
        Sets time.
        @see #setTime(int, int, int, int) setTime
    */
    public void setTime(int hr, int m) {
        setTime(hr, m, 0, 0);
    }
    /**
        Sets time.
        @see #setTime(int, int, int, int) setTime
    */
    public void setTime(int hr, int m, int s) {
        setTime(hr, m, s, 0);
    }
    /**
        Sets hour,minutes,seconds and milliseconds.
    */
    public void setTime(int hr, int m, int s, int ms) {
        if (timeZone == null || timeZone == defaultTimeZone) {
            Date dThis = new Date(_time);
            Date d = new Date(dThis.getYear(), dThis.getMonth(), dThis.getDate(), hr, m, s);
            _time = d.getTime() + ms;
            return;
        }
        GregorianCalendar c = _getCal();
        try {
            c.set(c.HOUR_OF_DAY, hr);
            c.set(c.MINUTE, m);
            c.set(c.SECOND, s);
            c.set(c.MILLISECOND, ms);
            _time = c.getTimeInMillis();
        }
        finally {
            _releaseCal(c);
        }
    }

    public void setTime(OATime t) {
        if (t == null) {
            clearTime();
            return;
        }
        if (timeZone == null || timeZone == defaultTimeZone) {
            setTime(t.getHour(), t.getMinute(), t.getSecond(), t.getMilliSecond());
            return;
        }
        GregorianCalendar c = _getCal();
        try {
            c.set(c.HOUR_OF_DAY, t.get24Hour());
            c.set(c.MINUTE, t.getMinute());
            c.set(c.SECOND, t.getSecond());
            c.set(c.MILLISECOND, t.getMilliSecond());
            _time = c.getTimeInMillis();
        }
        finally {
            _releaseCal(c);
        }
    }


    /**
        Sets year (ex: 2017), month (0-11), and day (1-31).
    */
    public void setDate(int yr, int m, int d) {
        if (timeZone == null || timeZone == defaultTimeZone) {
            Date dThis = new Date(_time);
            long ms = getMilliSecond();
            Date dNew = new Date(yr-1900, m, d, dThis.getHours(), dThis.getMinutes(), dThis.getSeconds());
            _time = dNew.getTime();
            if (ms > 0) _time += ms;
            return;
        }
        GregorianCalendar c = _getCal();
        try {
            c.set(c.YEAR, yr);
            c.set(c.MONTH, m);
            c.set(c.DATE, d);
            _time = c.getTimeInMillis();
        }
        finally {
            _releaseCal(c);
        }
    }

    public void setDate(OADate d) {
        if (d == null) {
            clearDate();
            return;
        }
        setDate(d.getYear(), d.getMonth(), d.getDay());
    }

    /**
        Returns year.  This is the <i>real</i>, unlike java.util.Date, which is the date minus 1900.
    */
    public int getYear() {
        if (timeZone == null || timeZone == defaultTimeZone) {
            Date d = new Date(_time);
            return d.getYear() + 1900;
        }
        GregorianCalendar c = _getCal();
        int yr;
        try {
            yr = c.get(c.YEAR);
        }
        finally {
            _releaseCal(c);
        }
        return yr;
    }

    /**
        Sets the year.  This is the <i>real</i>, unlike java.util.Date, which is the date minus 1900.
    */
    public void setYear(int y) {
        if (timeZone == null || timeZone == defaultTimeZone) {
            long ms = getMilliSecond();
            Date dThis = new Date(_time);
            Date dNew = new Date(y-1900, dThis.getMonth(), dThis.getDate(), dThis.getHours(), dThis.getMinutes(), dThis.getSeconds());
            _time = dNew.getTime();
            if (ms > 0) _time += ms;
            return;
        }
        GregorianCalendar c = _getCal();
        try {
            c.set(c.YEAR, y);
            _time = c.getTimeInMillis();
        }
        finally {
            _releaseCal(c);
        }
    }
    /**
        Get month, values between 0-11.
        @return month as 0-11
    */
    public int getMonth() {
        if (timeZone == null || timeZone == defaultTimeZone) {
            Date d = new Date(_time);
            return d.getMonth();
        }
        GregorianCalendar c = _getCal();
        int m;
        try {
            m = c.get(c.MONTH);
        }
        finally {
            _releaseCal(c);
        }
        return m;
    }
    /**
        Set month, values between 0-11.
        @param month must be between <b>0-11</b>.
    */
    public void setMonth(int month) {
        if (timeZone == null || timeZone == defaultTimeZone) {
            long ms = getMilliSecond();
            Date dThis = new Date(_time);
            Date dNew = new Date(dThis.getYear(), month, dThis.getDate(), dThis.getHours(), dThis.getMinutes(), dThis.getSeconds());
            _time = dNew.getTime();
            if (ms > 0) _time += ms;
            return;
        }
        GregorianCalendar c = _getCal();
        try {
            c.set(c.MONTH, month);
            _time = c.getTimeInMillis();
        }
        finally {
            _releaseCal(c);
        }
    }
    /** @return day of month, 1-31.*/
    public int getDay() {
        if (timeZone == null || timeZone == defaultTimeZone) {
            Date d = new Date(_time);
            return d.getDate();
        }
        GregorianCalendar c = _getCal();
        int d;
        try {
            d = c.get(c.DATE);
        }
        finally {
            _releaseCal(c);
        }
        return d;
    }
    /** Set the day of month, 1-31. */
    public void setDay(int d) {
        if (timeZone == null || timeZone == defaultTimeZone) {
            long ms = getMilliSecond();
            Date dThis = new Date(_time);
            Date dNew = new Date(dThis.getYear(), dThis.getMonth(), d, dThis.getHours(), dThis.getMinutes(), dThis.getSeconds());
            _time = dNew.getTime();
            if (ms > 0) _time += ms;
            return;
        }
        GregorianCalendar c = _getCal();
        try {
            c.set(c.DAY_OF_MONTH, d);
            _time = c.getTimeInMillis();
        }
        finally {
            _releaseCal(c);
        }
    }


    /**
     * Change the tz and keep the same other values (day,month,hour,etc).
     *
     * Use convertTo(tz) to have values adjusted.
     */
    public void setTimeZone(TimeZone tz) {
        if (tz == timeZone) return;
        if (timeZone == null && tz == defaultTimeZone) return;

        long ms = getMilliSecond();

        // need to create a new cal, otherwise setting tz will adjust the other values (use convertTo(tz) instead)
        GregorianCalendar calNew = new GregorianCalendar(tz);

        GregorianCalendar c = _getCal();
        try {
            calNew.set(c.get(c.YEAR), c.get(c.MONTH), c.get(c.DAY_OF_MONTH), c.get(c.HOUR_OF_DAY), c.get(c.MINUTE), c.get(c.SECOND));
            calNew.set(Calendar.MILLISECOND, c.get(c.MILLISECOND));
        }
        finally {
            _releaseCal(c);
        }

        this._time = calNew.getTimeInMillis();
        this._time += ms;

        this.timeZone = tz;
    }


    public TimeZone getTimeZone() {
        return timeZone == null ? defaultTimeZone :timeZone;
    }


    /**
        Gets the hour of the day based on 24 hour clock.
        @return the Hour 0-23
        @see #get12Hour
        @see #get24Hour
        @see #getAM_PM
    */
    public int getHour() {
        if (timeZone == null || timeZone == defaultTimeZone) {
            Date d = new Date(_time);
            int hr = d.getHours();
            return hr;
        }
        GregorianCalendar c = _getCal();
        int hr;
        try {
            hr = c.get(c.HOUR_OF_DAY);  // 24 hr
        }
        finally {
            _releaseCal(c);
        }
        return hr;
    }
    /**
        Sets the hour of the day based on 24 hour clock.
        @param hr is the Hour 0-12
        @see #setAM_PM
        @see #set12Hour
        @see #set24Hour
    */
    public void setHour(int hr) {
        if (timeZone == null || timeZone == defaultTimeZone) {
            long ms = getMilliSecond();
            Date dThis = new Date(_time);
            Date dNew = new Date(dThis.getYear(), dThis.getMonth(), dThis.getDate(), hr, dThis.getMinutes(), dThis.getSeconds());
            _time = dNew.getTime();
            if (ms > 0) _time += ms;
            return;
        }
        GregorianCalendar c = _getCal();
        try {
            c.set(c.HOUR_OF_DAY, hr);
            _time = c.getTimeInMillis();
        }
        finally {
            _releaseCal(c);
        }
    }

    public int get12Hour() {
        if (timeZone == null || timeZone == defaultTimeZone) {
            Date d = new Date(_time);
            int hr = d.getHours();
            if (hr >= 12) hr -= 12;
            return hr;
        }
        GregorianCalendar c = _getCal();
        int hr;
        try {
            hr = c.get(c.HOUR);  // 12 hr format
        }
        finally {
            _releaseCal(c);
        }
        return hr;
    }
    public void set12Hour(int hr) {
        if (hr >= 0) hr -= 12;
        setHour(hr);
    }


    /**
        Gets the hour of the day based on 24 hour clock.
        @return Hour 0-23
        @see #setAM_PM
        @see #setHour
    */
    public int get24Hour() {
        return getHour();
    }
    /**
        Sets the hour of the day based on 24 hour clock.
        @param hr is the Hour 0-23
        @see #setAM_PM
        @see #setHour
    */
    public void set24Hour(int hr) {
        setHour(hr);
    }


    /** returns  Calendar.AM or Calendar.PM */
    public int getAM_PM() {
        if (getHour() >= 12) return Calendar.PM;
        return Calendar.AM;
    }
    /** Calendar.AM or Calendar.PM */
    public void setAM_PM(int ap) {
        int hr = getHour();
        if (ap == Calendar.PM) {
            if (hr < 12) hr += 12;
        }
        else {
            if (hr <= 12) hr -= 12;
        }
        set24Hour(hr);
    }

    /** Return value of minutes. */
    public int getMinute() {
        Date d = new Date(_time);
        int hr = d.getMinutes();
        return hr;
    }
    /** Set value for minutes. */
    public void setMinute(int mins) {
        long ms = getMilliSecond();
        Date dThis = new Date(_time);
        Date dNew = new Date(dThis.getYear(), dThis.getMonth(), dThis.getDate(), dThis.getHours(), mins, dThis.getSeconds());
        _time = dNew.getTime();
        if (ms > 0) _time += ms;
    }

    /** Return value of seconds. */
    public int getSecond() {
        Date d = new Date(_time);
        int secs = d.getSeconds();
        return secs;
    }
    /** Sets value for seconds. */
    public void setSecond(int s) {
        long ms = getMilliSecond();
        Date dThis = new Date(_time);
        Date dNew = new Date(dThis.getYear(), dThis.getMonth(), dThis.getDate(), dThis.getHours(), dThis.getMinutes(), s);
        _time = dNew.getTime();
        if (ms > 0) _time += ms;
    }

    public void clearSecondAndMilliSecond() {
        Date dThis = new Date(_time);
        Date dNew = new Date(dThis.getYear(), dThis.getMonth(), dThis.getDate(), dThis.getHours(), dThis.getMinutes(), 0);
        _time = dNew.getTime();
    }
    
    /** Return value of milliseconds. */
    public int getMilliSecond() {
        Date dThis = new Date(_time);
        Date dNew = new Date(dThis.getYear(), dThis.getMonth(), dThis.getDate(), dThis.getHours(), dThis.getMinutes(), dThis.getSeconds());
        long ts = dNew.getTime();
        int ms = (int) (_time - ts);
        return ms;
    }
    /** Sets value for milliseconds. */
    public void setMilliSecond(int ms) {
        _time -= getMilliSecond();
        _time += ms;
    }


    /**
        Returns java.util.Date object that matches this DateTime.
    */
    public Date getDate() {
        return new Date(_time);
    }

    /**
        Returns day of week for date.  See Calendar for list of days (ex: SUNDAY).
        @see Calendar
    */
    public int getDayOfWeek() {
        GregorianCalendar c = _getCal();
        int x;
        try {
            x = c.get(Calendar.DAY_OF_WEEK);
        }
        finally {
            poolGregorianCalendar.release(c);
        }
        return x;
    }

    /**
        Returns day of year, where Jan 1 is 1.
    */
    public int getDayOfYear() {
        GregorianCalendar c = _getCal();
        int x;
        try {
            x = c.get(Calendar.DAY_OF_YEAR);
        }
        finally {
            poolGregorianCalendar.release(c);
        }
        return x;
    }
    /** Returns the number of the week within the month, where first week is 1. */
    public int getWeekOfMonth() {
        GregorianCalendar c = _getCal();
        int x;
        try {
            x = c.get(Calendar.WEEK_OF_MONTH);
        }
        finally {
            poolGregorianCalendar.release(c);
        }
        return x;
    }
    /** Returns number week within the year, where first week is 1. */
    public int getWeekOfYear() {
        GregorianCalendar c = _getCal();
        int x;
        try {
            x = c.get(Calendar.WEEK_OF_YEAR);
        }
        finally {
            poolGregorianCalendar.release(c);
        }
        return x;
    }

    /** Returns number of days in this month. */
    public int getDaysInMonth() {
        GregorianCalendar c = _getCal();
        int x;
        try {
            x = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        finally {
            poolGregorianCalendar.release(c);
        }
        return x;
    }

    /**
        Compares this OADateTime with any object.  If object is not an OADateTime, it will be converted and then compared.
        @param obj Date, OADate, Calendar, String, etc.
        @see #compareTo
    */
    public boolean equals(Object obj) {
        try {
            int i = compareTo(obj);
            return (i == 0);
        }
        catch (Exception e) {
            return false;
        }
    }


    /**
        Compares this OADateTime with any object.  If object is not an OADateTime, it will be converted and then compared.
        @param obj Date, OADate, Calendar, String, etc.
        @see #compareTo
    */
    public boolean before(Object obj) {
        return (compareTo(obj) < 0);
    }
    /**
        Compares this OADateTime with any object.  If object is not an OADateTime, it will be converted and then compared.
        @param obj Date, OADate, Calendar, String, etc.
        @see #compareTo
    */
    public boolean isBefore(Object obj) {
        return (compareTo(obj) < 0);
    }

    /**
        Compares this OADateTime with any object.  If object is not an OADateTime, it will be converted and then compared.
        @param obj Date, OADate, Calendar, String, etc.
        @see #compareTo
    */
    public boolean after(Object obj) {
        return (compareTo(obj) > 0);
    }
    public boolean isAfter(Object obj) {
        return (compareTo(obj) > 0);
    }

    public int compare(Object obj) {
        return compareTo(obj);
    }

    /**
        Compares this object with the specified object for order.<br>
        Returns a negative integer, zero, or a positive integer as this object is less than, equal to,
        or greater than the specified object.

        @param obj Date, OADate, Calendar, String
        @return "0" if equal, "-1" if this OADateTime is less than, "1" if this OADateTime is greater than,
           "2" if objects can not be compared.
    */
    public int compareTo(Object obj) {
        if (obj == null) return 1;
        OADateTime d = convert(obj, false);
        if (d == null) return 2;

        OADateTime dtThis, dtObj;
        if (!this.getClass().equals(d.getClass())) {
            if (this instanceof OADate || obj instanceof OADate) {
                if (this instanceof OADate) dtThis = this;
                else dtThis = new OADate(this);

                if (d instanceof OADate) dtObj = d;
                else dtObj = new OADate(d);
            }
            else if (this instanceof OATime || obj instanceof OATime) {
                if (this instanceof OATime) dtThis = this;
                else dtThis = new OATime(this);

                if (d instanceof OATime) dtObj = d;
                else dtObj = new OATime(d);
            }
            else {
                dtThis = this;
                dtObj = d;
            }
        }
        else {
            dtThis = this;
            dtObj = d;
        }

        if (dtThis._time == dtObj._time) return 0;
        if (dtThis._time > dtObj._time) return 1;
        return -1;
    }



//qqqqqqqqqqqqqqqqqqqq test this qqqqqqqqqqqqqqqqq

    /**
     * Convert the current dt to a different tz, and adjusting it's values
     * @param tz
     * @return
     */
    public OADateTime convertTo(TimeZone tz) {
        OADateTime dt;
        if (this instanceof OADate) {
            dt = new OADate(this);
        }
        else if (this instanceof OATime) {
            dt = new OATime(this);
        }
        else {
            dt = new OADateTime(this);
        }

        GregorianCalendar c = dt._getCal();
        try {
            c.setTimeZone(tz);
            dt = new OADateTime(c);
        }
        finally {
            poolGregorianCalendar.release(c);
        }

        return dt;
    }


    /**
        Return an OADateTime where a specified amount of days is added.
        <p>
        Note: if this is an instanceof OADate or OATime, then the returned object will be the same type.
        @param amount number of days to increment/deincrement (negative number).
        @return new OADateTime object.
    */
    public OADateTime addDays(int amount) {
        if (this instanceof OATime) {
            return new OATime(this);
        }

        OADateTime dtNew;
        GregorianCalendar c = _getCal();
        try {
            c.add(Calendar.DATE, amount);

            if (this instanceof OADate) {
                dtNew = new OADate(c);
            }
            else {
                dtNew = new OADateTime(c);
            }
        }
        finally {
            poolGregorianCalendar.release(c);
        }

        return dtNew;
    }

    /**
        Return an OADateTime where a specified amount of weeks added.
        <p>
        Note: if this is an instanceof OADate or OATime, then the returned object will be the same type.
        @param amount number of weeks to increment/deincrement (negative number).
        @return new OADateTime object.
    */
    public OADateTime addWeeks(int amount) {
        return addDays(amount*7);
    }

    /**
        Return an OADateTime where a specified amount of months is added.
        <p>
        Note: if this is an instanceof OADate or OATime, then the returned object will be the same type.
        @param amount number of months to increment/deincrement (negative number).
        @return new OADateTime object.
    */
    public OADateTime addMonths(int amount) {
        if (this instanceof OATime) {
            return new OATime(this);
        }

        OADateTime dtNew;
        GregorianCalendar c = _getCal();
        try {
            c.add(Calendar.MONTH, amount);

            if (this instanceof OADate) {
                dtNew = new OADate(c);
            }
            else {
                dtNew = new OADateTime(c);
            }
        }
        finally {
            poolGregorianCalendar.release(c);
        }

        return dtNew;
    }

    /**
        Return an OADateTime where a specified amount of years is added.
        <p>
        Note: if this is an instanceof OADate or OATime, then the returned object will be the same type.
        @param amount number of years to increment/deincrement (negative number).
        @return new OADateTime object.
    */
    public OADateTime addYears(int amount) {
        if (this instanceof OATime) {
            return new OATime(this);
        }

        OADateTime dtNew;
        GregorianCalendar c = _getCal();
        try {
            c.add(Calendar.YEAR, amount);

            if (this instanceof OADate) {
                dtNew = new OADate(c);
            }
            else {
                dtNew = new OADateTime(c);
            }
        }
        finally {
            poolGregorianCalendar.release(c);
        }

        return dtNew;
    }

    /**
        Return an OADateTime where a specified amount of hours is added.
        <p>
        Note: if this is an instanceof OADate or OATime, then the returned object will be the same type.
        @param amount number of hours to increment/deincrement (negative number).
        @return new OADateTime object.
    */
    public OADateTime addHours(int amount) {
        OADateTime dtNew;
        GregorianCalendar c = _getCal();
        try {
            c.add(Calendar.HOUR_OF_DAY, amount);

            if (this instanceof OATime) {
                dtNew = new OATime(c);
            }
            else if (this instanceof OADate) {
                dtNew = new OADate(c);
            }
            else {
                dtNew = new OADateTime(c);
            }
        }
        finally {
            poolGregorianCalendar.release(c);
        }
        return dtNew;
    }

    /**
        Return an OADateTime where a specified amount of minutes is added.
        <p>
        Note: if this is an instanceof OADate or OATime, then the returned object will be the same type.
        @param amount number of minutes to increment/deincrement (negative number).
        @return new OADateTime object.
    */
    public OADateTime addMinutes(int amount) {
        OADateTime dtNew;
        GregorianCalendar c = _getCal();
        try {
            c.add(Calendar.MINUTE, amount);

            if (this instanceof OATime) {
                dtNew = new OATime(c);
            }
            else if (this instanceof OADate) {
                dtNew = new OADate(c);
            }
            else {
                dtNew = new OADateTime(c);
            }
        }
        finally {
            poolGregorianCalendar.release(c);
        }
        return dtNew;
    }

    /**
        Return an OADateTime where a specified amount of seconds is added.
        <p>
        Note: if this is an instanceof OADate or OATime, then the returned object will be the same type.
        @param amount number of seconds to increment/deincrement (negative number).
        @return new OADateTime object.
    */
    public OADateTime addSeconds(int amount) {
        OADateTime dtNew;
        GregorianCalendar c = _getCal();
        try {
            c.add(Calendar.SECOND, amount);

            if (this instanceof OATime) {
                dtNew = new OATime(c);
            }
            else if (this instanceof OADate) {
                dtNew = new OADate(c);
            }
            else {
                dtNew = new OADateTime(c);
            }
        }
        finally {
            poolGregorianCalendar.release(c);
        }
        return dtNew;
    }

    /**
        Return an OADateTime where a specified amount of milliseconds is added.
        <p>
        Note: if this is an instanceof OADate or OATime, then the returned object will be the same type.
        @param amount number of milliseconds to increment/deincrement (negative number).
        @return new OADateTime object.
    */
    public OADateTime addMilliSeconds(int amount) {
        OADateTime dtNew;
        GregorianCalendar c = _getCal();
        try {
            c.add(Calendar.MILLISECOND, amount);

            if (this instanceof OATime) {
                dtNew = new OATime(c);
            }
            else if (this instanceof OADate) {
                dtNew = new OADate(c);
            }
            else {
                dtNew = new OADateTime(c);
            }
        }
        finally {
            poolGregorianCalendar.release(c);
        }
        return dtNew;
    }


    /**
        Returns the number of years between this OADateTime and obj.
        @param obj Date, OADateTime, Calendar, etc that can be converted to an OADateTime.
    */
    public int betweenYears(Object obj) {
        OADateTime d = convert(obj, false);
        return Math.abs(this.getYear() - d.getYear());
    }


    /**
        Returns the number of months betweeen this OADateTime and obj.
        @param obj Date, OADateTime, Calendar, etc that can be converted to an OADateTime.
    */
    public int betweenMonths(Object obj) {
        OADateTime d = convert(obj, false);

        int amt = this.getYear() - d.getYear();
        amt = Math.abs(amt) * 12;

        if (compareTo(obj) >= 0) {
            amt += (d.getMonth() - this.getMonth());
        }
        else {
            amt += (this.getMonth() - d.getMonth());
        }

        return Math.abs(amt);
    }

    /**
        Returns the number of days between this OADateTime and obj.
        @param obj Date, OADateTime, Calendar, etc that can be converted to an OADateTime.
    */
    public int betweenDays(Object obj) {
        OADateTime d = convert(obj, true);
        d.setTime(this.getHour(), this.getMinute(), this.getSecond(), this.getMilliSecond());

        double millis;
        GregorianCalendar cThis = _getCal();
        GregorianCalendar cOther = d._getCal();
        try {
            millis = Math.abs(cThis.getTime().getTime() - cOther.getTime().getTime());
        }
        finally {
            poolGregorianCalendar.release(cThis);
            poolGregorianCalendar.release(cOther);
        }
        return (int) Math.floor(millis/(1000 * 60 * 60 * 24) + .5d);  // accounts for daylight savings (23hr day, or 25hr day)
     }


    /**
        Returns the number of hours betweeen this OADateTime and obj.
        @param obj Date, OADateTime, Calendar, etc that can be converted to an OADateTime.
    */
    public int betweenHours(Object obj) {
        OADateTime d = convert(obj, true);
        d.setTime(d.getHour(), this.getMinute(), this.getSecond(), this.getMilliSecond());

        GregorianCalendar cThis = _getCal();
        GregorianCalendar cOther = d._getCal();

        double millis = Math.abs(cThis.getTime().getTime() - cOther.getTime().getTime());

        poolGregorianCalendar.release(cThis);
        poolGregorianCalendar.release(cOther);


        return (int) Math.ceil( millis/(1000 * 60 * 60));
    }

    /**
        Returns the number of minutes betweeen this OADateTime and obj.
        @param obj Date, OADateTime, Calendar, etc that can be converted to an OADateTime.
    */
    public int betweenMinutes(Object obj) {
        OADateTime d = convert(obj, true);
        d.setTime(d.getHour(), d.getMinute(), this.getSecond(), this.getMilliSecond());

        GregorianCalendar cThis = _getCal();
        GregorianCalendar cOther = d._getCal();

        double millis = Math.abs(cThis.getTime().getTime() - cOther.getTime().getTime());

        poolGregorianCalendar.release(cThis);
        poolGregorianCalendar.release(cOther);

        return (int) Math.ceil(millis/(1000 * 60));
    }

    /**
        Returns the number of seconds betweeen this OADateTime and obj.
        @param obj Date, OADateTime, Calendar, etc that can be converted to an OADateTime.
    */
    public int betweenSeconds(Object obj) {
        OADateTime d = convert(obj, true);
        d.setTime(d.getHour(), d.getMinute(), d.getSecond(), this.getMilliSecond());

        GregorianCalendar cThis = _getCal();
        GregorianCalendar cOther = d._getCal();

        double millis = Math.abs(cThis.getTime().getTime() - cOther.getTime().getTime());

        poolGregorianCalendar.release(cThis);
        poolGregorianCalendar.release(cOther);

        return (int) Math.ceil(millis/(1000));
    }

    /**
        Returns the number of seconds betweeen this OADateTime and obj.
        @param obj Date, OADateTime, Calendar, etc that can be converted to an OADateTime.
    */
    public long betweenMilliSeconds(Object obj) {
        OADateTime d = convert(obj, false);

        GregorianCalendar cThis = _getCal();
        GregorianCalendar cOther = d._getCal();

        long millis = Math.abs(cThis.getTime().getTime() - cOther.getTime().getTime());

        poolGregorianCalendar.release(cThis);
        poolGregorianCalendar.release(cOther);

        return millis;
    }

    /**
     * Time as milliseconds, same as Date.getTime()
     */
    public long getTime() {
        return _time;
    }

    /**
        Convert an Object to an OADateTime.
    */
    protected OADateTime convert(Object obj, boolean bAlways) {
        if (obj == null) return null;

        if (obj instanceof OADateTime) {
            if (bAlways) return new OADateTime((OADateTime) obj);
            else return (OADateTime) obj;
        }
        if (obj instanceof java.sql.Time) return new OADateTime((java.sql.Time)obj);
        if (obj instanceof java.sql.Timestamp) return new OADateTime((java.sql.Timestamp)obj);
        if (obj instanceof Date) return new OADateTime((Date)obj);
        if (obj instanceof Calendar) return new OADateTime((Calendar)obj);
        if (obj instanceof String) return new OADateTime((String)obj);
        return null;
        // throw new IllegalArgumentException("OADateTime cant convert class "+obj.getClass()+" to an OADateTime");
    }


    /**
        Static method for converting a String date to an OADateTime.<br>
        If date is " " (space) then todays date will be returned.<br>
        If date is null or "" then null is returned.<br>
        @param fmt format of date.  If not valid, then staticParseFormats and staticOutputFormat will be used.
        @return OADateTime or null
        @see OADateTime#setFormat
        @see OADateTime#valueOf to convert a string using global parse strings
    */
     public static OADateTime valueOf(String strDateTime, String fmt) {
        if (strDateTime == null) return null;
        Date d = valueOfMain(strDateTime, fmt, vecDateTimeParseFormat, staticOutputFormat);
        if (d == null) {
            d = valueOfMain(fixDate(strDateTime), fmt, vecDateTimeParseFormat, staticOutputFormat);
            if (d == null) return null;
        }
        return new OADateTime(d);
    }

    /**
        Internally used to fix a String date.
    */
    protected static String fixDate(String s) {
        if (s == null) return "";
        int x = s.length();
        int max = (x > 3) ? 2 : 1;
        StringBuffer sb = new StringBuffer(x+1);
        for (int i=0,j=0; i<x ; i++) {
            char c = s.charAt(i);
            if (!Character.isLetterOrDigit(c) && j < max) {
                j++;
                c = '/';
            }
            sb.append(c);
        }
        return new String(sb);
    }



    /**
        Converts a String date to an OADateTime. <br>
        If value is " " (space) then todays date/time will be returned.<br>
        If value is null or "" then null is returned.<br>
        StaticParseFormats and staticOutputFormat will be used to try to convert.
        @return OADateTime or null
        @see OADateTime#setFormat
        @see #setGlobalOutputFormat
        @see #addGlobalParseFormat
        see #getGlobalParseFormats
        @see #valueOf(String,String)
    */
    public static OADateTime valueOf(String strDateTime) {
        return valueOf(strDateTime, null);
    }




    // convert from string  ------------------------------------------------------
    protected static Date valueOfMain(String value, String inputFormat, Vector vec, String outputFormat) {
        if (value == null || value.length() == 0) return null;
        if (value.equals(" ")) return new Date();


        String format = null;
        if (inputFormat != null) {
            // Convert 4 digit year to 2 digit.  Otherwise, a 2 digit year input will be wrong.  ex: 1/1/65  -> 01/01/0065
            String s = inputFormat.toUpperCase();
            int pos = s.indexOf("YYYY");
            if (pos >= 0) {
                format = inputFormat.substring(0,pos) + inputFormat.substring(pos+2);
            }
        }

        Date date = null;
        int x = vec.size();

        int j = (format == null) ? -1 : -2;
        for (; j<=x && date == null; j++) {
            if (j == -1) format = inputFormat;
            if (j >= 0) {
                if (j < x) format = (String) vec.elementAt(j);
                else format = outputFormat;
            }
            if (format != null && format.length() > 0) {
                SimpleDateFormat sdf = getFormatter();
                synchronized(sdf) {
                    sdf.applyPattern(format);
                    try {
                        date = sdf.parse(value);
                        if (date != null) break;
                    }
                    catch (Exception e) {
                    }
                }
            }
        }
        return date;
    }


    /**
        Converts OADateTime to a String using specified formatting String.<br>
        Uses the first format that has been set: "format", "staticOutputFormat" else or "yyyy-MMM-dd hh:mma"
    */
    public String toString() {
        return toString(null);
    }
    /**
        Converts OADateTime to a String using specified formatting String.
        @param f is format to apply
    */
    public String toString(String f) {
        if (f == null) {
            f = (format == null) ? staticOutputFormat : format;
            if (f == null || f.length() == 0) f = "yyyy-MMM-dd hh:mma";
        }
        return toStringMain(f);
    }


    // main method called to get string value
    protected String toStringMain(String format) {
        if (format == null || format.length() == 0) return getDate().toString();
        String s;
        SimpleDateFormat sdf = getFormatter();
        synchronized(sdf) {
            sdf.applyPattern(format);
            sdf.setTimeZone(getTimeZone());
            s = sdf.format(getDate());
        }
        return s;
    }



    /**
        Sets the default global format used when converting OADateTime to String.
        @see #setFormat
    */
    public static void setGlobalOutputFormat(String fmt) {
        staticOutputFormat = fmt;
    }
    /**
        Gets the default global format used when converting OADateTime to String.
        @see #setFormat
    */
    public static String getGlobalOutputFormat() {
        return staticOutputFormat;
    }

    /**
        Add additional global parse formats that are used when converting a String to OADateTime.
        @see #setFormat
    */
    public static void addGlobalParseFormat(String fmt) {
        vecDateTimeParseFormat.addElement(fmt);
    }
    /**
        Remove a global parse format.
        @see addGlobalParseFormat
    */
    public static void removeGlobalParseFormat(String fmt) {
        vecDateTimeParseFormat.removeElement(fmt);
    }
    /**
        Remove a all globally used parse format.
        @see addGlobalParseFormat
    */
    public static void removeAllGlobalParseFormats() {
        vecDateTimeParseFormat.removeAllElements();
    }

    /**
        Set format to use for this OADateTime
        This format will be used when converting this datetime to a String, unless
        a format is specified when calling toString.
        @see OADateTime
        @see #toString
    */
    public void setFormat(String fmt) {
        this.format = fmt;
    }
    /**
        get format to use for this OADateTime
        @see OADateTime
    */
    public String getFormat() {
        return format;
    }

    protected static SimpleDateFormat getFormatter() {
        SimpleDateFormat sdf;
        synchronized (simpleDateFormats) {
            simpleDateFormatCounter++;
            if (simpleDateFormatCounter >= simpleDateFormats.length) simpleDateFormatCounter = 0;
            sdf = simpleDateFormats[simpleDateFormatCounter];
            if (sdf == null) {
                sdf = simpleDateFormats[simpleDateFormatCounter] = new SimpleDateFormat();
                sdf.setLenient(false);
            }
        }
        return sdf;
    }

    /**
        Returns the format string to use for system format.
        @param type DateFormat.SHORT, MEDIUM, LONG, FULL, DEFAULT
    */
    public static String getFormat(int type) {
        return getFormat(type, locale);
    }
    /**
        Returns the format string to use for system format.
        @param type DateFormat.SHORT, MEDIUM, LONG, FULL, DEFAULT
    */
    public static String getFormat(int type, Locale locale) {
        DateFormat df = DateFormat.getDateInstance(type, locale);
        if (df instanceof SimpleDateFormat) {
            String s = ((SimpleDateFormat)df).toPattern();
            return s;
        }
        return null;
    }
}