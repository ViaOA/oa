/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.util;

import java.util.*;
import java.text.*;
import java.io.IOException;
import java.sql.Time;


/**
    Superclass of OADate and OATime that combines Calendar, Date and SimpleDateFormat.

	'Hms', 'Mdy'

    yyyyMMdd_HHmmss.SSS

    <p>
    Formatting Symbols used for output display.
    <pre>
    G  era designator          (Text)              AD
    y  year                    (Number)            1996
    M  month in year           (Text & Number)     July & 07
    d  day in month            (Number)            10
    h  hour in am/pm (1~12)    (Number)            12
    H  hour in day (0~23)      (Number)            0
    m  minute in hour          (Number)            30
    s  second in minute        (Number)            55
    S  millisecond             (Number)            978
    E  day in week             (Text)              Tuesday
    D  day in year             (Number)            189
    F  day of week in month    (Number)            2 (2nd Wed in July)
    w  week in year            (Number)            27
    W  week in month           (Number)            2
    a  am/pm marker            (Text)              PM
    k  hour in day (1~24)      (Number)            24
    K  hour in am/pm (0~11)    (Number)            0
    z  time zone               (Text)              Pacific Standard Time
    '  escape for text         (Delimiter)
    '' single quote            (Literal)           '

    Examples:
    "yyyy.MM.dd G 'at' hh:mm:ss z"    ->>  1996.07.10 AD at 15:08:56 PDT
    "EEE, MMM d, ''yy"                ->>  Wed, July 10, '96
    "h:mm a"                          ->>  12:08 PM
    "hh 'o''clock' a, zzzz"           ->>  12 o'clock PM, Pacific Daylight Time
    "K:mm a, z"                       ->>  0:00 PM, PST
    "yyyy.MMMMM.dd GGG hh:mm aaa"    ->>  1996.July.10 AD 12:08 PM
    "yyyy.MM.dd HH:mm:ss.SSS"
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
    protected GregorianCalendar cal;
    protected String format;

    private static SimpleDateFormat[] simpleDateFormats;
    private static int simpleDateFormatCounter;
    static {
        // used by getFormatter()
        simpleDateFormats = new SimpleDateFormat[12];  // keeps a pool of 12 that are shared in a "round robin" pool
    }

    /** default output format */
    protected static String staticOutputFormat;

    /** default parse formats */
    protected static Vector vecParseFormat;

    static {
        setLocale(Locale.getDefault());
    }

    private static Locale locale;
    public static void setLocale(Locale loc) {
        locale = loc;
        vecParseFormat = new Vector(15,10);
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
            vecParseFormat.addElement("MM/dd/yy hh:mm:ss.Sa");
            vecParseFormat.addElement("MM/dd/yy hh:mm:ssa");
            vecParseFormat.addElement("MM/dd/yy hh:mma");

            vecParseFormat.addElement("MM/dd/yy HH:mm:ss.S");
            vecParseFormat.addElement("MM/dd/yy HH:mm:ss");
            vecParseFormat.addElement("MM/dd/yy HH:mm");

            vecParseFormat.addElement("MM/dd/yyyy hh:mm:ss.Sa");
            vecParseFormat.addElement("MM/dd/yyyy hh:mm:ssa");
            vecParseFormat.addElement("MM/dd/yyyy hh:mma");

            vecParseFormat.addElement("MM/dd/yyyy HH:mm:ss.S");
            vecParseFormat.addElement("MM/dd/yyyy HH:mm:ss");
            vecParseFormat.addElement("MM/dd/yyyy HH:mm");
        }
        else if (bYearFirst) {
            staticOutputFormat = "yyyy/MM/dd hh:mma";
            // the "yy" formats must be before the "yyyy" formats because "yyyy" will convert "05/04/65" -> "05/04/0065"
            vecParseFormat.addElement("yy/MM/dd hh:mm:ss.Sa");
            vecParseFormat.addElement("yy/MM/dd hh:mm:ssa");
            vecParseFormat.addElement("yy/MM/dd hh:mma");

            vecParseFormat.addElement("yy/MM/dd HH:mm:ss.S");
            vecParseFormat.addElement("yy/MM/dd HH:mm:ss");
            vecParseFormat.addElement("yy/MM/dd HH:mm");

            vecParseFormat.addElement("yyyy/MM/dd hh:mm:ss.Sa");
            vecParseFormat.addElement("yyyy/MM/dd hh:mm:ssa");
            vecParseFormat.addElement("yyyy/MM/dd hh:mma");

            vecParseFormat.addElement("yyyy/MM/dd HH:mm:ss.S");
            vecParseFormat.addElement("yyyy/MM/dd HH:mm:ss");
            vecParseFormat.addElement("yyyy/MM/dd HH:mm");
        }
        else {  // day first
            staticOutputFormat = "dd/MM/yyyy hh:mma";
            // the "yy" formats must be before the "yyyy" formats because "yyyy" will convert "05/04/65" -> "05/04/0065"
            vecParseFormat.addElement("dd/MM/yy hh:mm:ss.Sa");
            vecParseFormat.addElement("dd/MM/yy hh:mm:ssa");
            vecParseFormat.addElement("dd/MM/yy hh:mma");

            vecParseFormat.addElement("dd/MM/yy HH:mm:ss.S");
            vecParseFormat.addElement("dd/MM/yy HH:mm:ss");
            vecParseFormat.addElement("dd/MM/yy HH:mm");

            vecParseFormat.addElement("dd/MM/yyyy hh:mm:ss.Sa");
            vecParseFormat.addElement("dd/MM/yyyy hh:mm:ssa");
            vecParseFormat.addElement("dd/MM/yyyy hh:mma");

            vecParseFormat.addElement("dd/MM/yyyy HH:mm:ss.S");
            vecParseFormat.addElement("dd/MM/yyyy HH:mm:ss");
            vecParseFormat.addElement("dd/MM/yyyy HH:mm");
        }
        // SQL date formates
        vecParseFormat.addElement("yyyy-MM-dd HH:mm:ss");
        vecParseFormat.addElement("yyyy-MM-dd");
        
        vecParseFormat.addElement(getFormat(DateFormat.SHORT));
        vecParseFormat.addElement(getFormat(DateFormat.MEDIUM));
        vecParseFormat.addElement(getFormat(DateFormat.LONG));
        vecParseFormat.addElement(getFormat(DateFormat.DEFAULT));
    }
    
    /**
        Creates new datetime, using current date and time.
    */
    public OADateTime() {
        java.sql.Timestamp date = new java.sql.Timestamp((new Date()).getTime());
        setCalendar(date);
    }

    /**
        Creates new datetime, using time parameter.
    */
    public OADateTime(java.sql.Time time) {
        setCalendar(time);
    }
    /**
        Creates new datetime, using date parameter.
    */
    public OADateTime(Date date) {
        setCalendar(date);
    }
    /**
	    Creates new datetime, using date parameter.
	*/
	public OADateTime(long time) {
	    this(new Date(time));
	}
    /**
        Creates new datetime, using timestamp parameter.
    */
    public OADateTime(java.sql.Timestamp date) {
        setCalendar(date);
    }
    /**
        Creates new datetime, using Calendar parameter.
    */
    public OADateTime(Calendar c) {
        setCalendar((GregorianCalendar)c);
    }

    /**
        Creates new datetime, using OADateTime parameter.
    */
    public OADateTime(OADateTime odt) {
        setCalendar(odt);
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
        int year = 0;
        int month = 0;
        int day = 0;
        int hrs = 0;
        int mins = 0;
        int secs = 0;
        int milsecs = 0;
        if (d != null) {
            year = d.getYear();
            month = d.getMonth();
            day = d.getDay();
        }
        if (t != null) {
            hrs = t.getHour();
            mins = t.getMinute();
            secs = t.getSecond();
            milsecs = t.getMilliSecond();
        }
        setCalendar(year,month,day,hrs,mins,secs,milsecs);
    }

    public OADateTime(int year, int month, int day) {
        this(year,month,day,0,0,0,0);
    }
    public OADateTime(int year, int month, int day, int hrs, int mins) {
        this(year,month,day, hrs, mins, 0, 0);
    }
    public OADateTime(int year, int month, int day, int hrs, int mins, int secs) {
        this(year,month,day, hrs, mins, secs, 0);
    }
    /**
       @param year full year (not year minus 1900 like Date)
       @param month 0-11
       @param date day of the month
    */
    public OADateTime(int year, int month, int day, int hrs, int mins, int secs, int milsecs) {
        setCalendar(year,month,day,hrs,mins,secs,milsecs);
    }

    // This will fix the bug in JDK and will keep date/times the same across different timezones.
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
    	// might want to add TimeZone
        // stream.defaultWriteObject();
        stream.writeInt(cal.get(Calendar.YEAR));
        stream.writeInt(cal.get(Calendar.MONTH));
        stream.writeInt(cal.get(Calendar.DATE));
        stream.writeInt(cal.get(Calendar.HOUR_OF_DAY));
        stream.writeInt(cal.get(Calendar.MINUTE));
        stream.writeInt(cal.get(Calendar.SECOND));
        stream.writeInt(cal.get(Calendar.MILLISECOND));
        
        // see readObject for using TimeZone
        // stream.writeInt(cal.get(Calendar.ZONE_OFFSET));
    }
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    	// might want to add TimeZone
    	// in.defaultReadObject();
        int y = in.readInt();
        int mon = in.readInt();
        int d = in.readInt();
        int h = in.readInt();
        int min = in.readInt();
        int s = in.readInt();
        int mil = in.readInt();

        cal = new GregorianCalendar(y, mon, d, h, min, s);
        cal.set(Calendar.MILLISECOND, mil);
        
        // Timezone adjustment - this will adjust the clock based on timezone differences
        // int tzone = in.readInt();
        // int diff = cal.get(Calendar.ZONE_OFFSET) - tzone;
        // if (diff != 0) cal.add(Calendar.HOUR, diff / (3600 * 1000));
    }
    
    
    /**
        Returns a clone of the calendar used by this object.
    */
    public Calendar getCalendar() {
        return (Calendar) cal.clone();
    }


    // conversions
    protected void setCalendar(int year, int month, int day, int hrs, int mins, int secs, int milsecs) {
        cal = new GregorianCalendar(year,month,day, hrs, mins, secs);
        setMilliSecond(milsecs);
    }
    protected void setCalendar(GregorianCalendar c) {
        if (c == null) setCalendar(new OADateTime());
        else {
            try {
                cal = (GregorianCalendar) (c.clone());
            }
            catch (Exception e) {}
        }
    }

    protected void setCalendar(java.sql.Timestamp date) {
        if (date == null) date = new java.sql.Timestamp((new Date()).getTime());
        int ms = (int) (date.getTime() % 1000);
        this.setCalendar(date.getYear()+1900, date.getMonth(), date.getDate(), date.getHours(),date.getMinutes(),date.getSeconds(),ms);
    }


    protected void setCalendar(Date date) {
        if (date == null) date = new Date();
        if (date instanceof java.sql.Date) {
            this.setCalendar(date.getYear()+1900, date.getMonth(), date.getDate(), 0,0,0,0);
        }
        else {
        	int ms = (int) (date.getTime() % 1000);
        	this.setCalendar(date.getYear()+1900, date.getMonth(), date.getDate(), date.getHours(),date.getMinutes(),date.getSeconds(), ms);
        }
    }

    protected void setCalendar(Time time) {
        if (time == null) time = new Time( new Date().getTime() );
        int ms = (int) (time.getTime() % 1000);
        this.setCalendar(0,0,0, time.getHours(),time.getMinutes(),time.getSeconds(), ms);
    }

    protected void setCalendar(OADateTime dt) {
        if (dt == null) dt = new OADateTime();
        try {
            setCalendar( (GregorianCalendar) (dt.cal.clone()) );
        }
        catch (Exception e) {}
    }
    protected void setCalendar(String strDate) {
        if (strDate == null) setCalendar(new Date());
        else {
            OADateTime date = valueOf(strDate);
            if (date == null) throw new IllegalArgumentException("OADateTime cant create date from String \""+strDate+"\"");
            cal = date.cal;
        }
    }
    protected void setCalendar(String strDate, String fmt) {
        if (strDate == null) setCalendar(new Date());
        else {
            OADateTime date = valueOf(strDate, fmt);
            if (date == null) throw new IllegalArgumentException("OADateTime cant create date from String \""+strDate+"\"");
            cal = date.cal;
        }
    }

    /**
        Sets hour,minutes and seconds to zero.
    */
    public void clearTime() {
        setHour(0);
        setMinute(0);
        setSecond(0);
        setMilliSecond(0);
        cal.set(Calendar.AM_PM, Calendar.AM);
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
        setHour(hr);
        setMinute(m);
        setSecond(s);
        setMilliSecond(ms);
    }

    // 2006/12/15
    public void setTime(OATime t) {
    	if (t != null) {
    		setTime(t.getHour(), t.getMinute(), t.getSecond(), t.getMilliSecond());
    	}
    }
    
    
    /**
        Sets year, month, day to zero.
    */
    public void clearDate() {
        cal.clear(Calendar.YEAR);
        cal.clear(Calendar.MONTH);
        cal.clear(Calendar.DATE);
        /* 2005/01/03 was:
        setYear(0);
        setMonth(0);
        setDay(0);
        */
    }
    /**
        Sets year, month, and day.
    */
    public void setDate(int yr, int m, int d) {
        setYear(yr);
        setMonth(m);
        setDay(d);
    }

    public void setDate(OADate d) {
    	if (d != null) {
	        setYear(d.getYear());
	        setMonth(d.getMonth());
	        setDay(d.getDay());
    	}
    }

    /**
        Returns year.  This is the <i>real</i>, unlike java.util.Date, which is the date minus 1900.
    */
    public int getYear() {
        return cal.get(Calendar.YEAR);
    }
    /**
        Sets the year.  This is the <i>real</i>, unlike java.util.Date, which is the date minus 1900.
    */
    public void setYear(int y) {
        cal.set(Calendar.YEAR, y);
    }
    /**
        Get month, values between 0-11.
        @returns month as 0-11
    */
    public int getMonth() {
        return cal.get(Calendar.MONTH);
    }
    /**
        Set month, values between 0-11.
        @param month must be between <b>0-11</b>.
    */
    public void setMonth(int month) {
        cal.set(Calendar.MONTH, month);
    }
    /** @returns day of month, 1-31.*/
    public int getDay() {
        return cal.get(Calendar.DATE);
    }
    /** Set the day of month, 1-31. */
    public void setDay(int d) {
        cal.set(Calendar.DATE, d);
    }

    
    
    /**
        Gets the hour of the day based on 12 hour clock.
        @return the Hour 0-11
        @see #get24Hour
        @see #getAM_PM
    */
    public int getHour() {
        return cal.get(Calendar.HOUR);
    }
    /**
        Sets the hour of the day based on 12 hour clock.
        @param hr is the Hour 0-11
        @see #setAM_PM
        @see #set24Hour
    */
    public void setHour(int hr) {
        cal.set(Calendar.HOUR, hr);
    }

    /**
        Gets the hour of the day based on 24 hour clock.
        @return Hour 0-23
        @see #setAM_PM
        @see #setHour
    */
    public int get24Hour() {
        return cal.get(Calendar.HOUR_OF_DAY);
    }
    /**
        Sets the hour of the day based on 24 hour clock.
        @param hr is the Hour 0-23
        @see #setAM_PM
        @see #setHour
    */
    public void set24Hour(int hr) {
        cal.set(Calendar.HOUR_OF_DAY, hr);
    }


    /** returns  Calendar.AM or Calendar.PM */
    public int getAM_PM() {
        return cal.get(Calendar.AM_PM);
    }
    /** Calendar.AM or Calendar.PM */
    public void setAM_PM(int ap) {
        cal.set(Calendar.AM_PM, ap);
    }


    /** Return value of minutes. */
    public int getMinute() {
        return cal.get(Calendar.MINUTE);
    }
    /** Set value for minutes. */
    public void setMinute(int m) {
        cal.set(Calendar.MINUTE, m);
    }

    /** Return value of seconds. */
    public int getSecond() {
        return cal.get(Calendar.SECOND);
    }
    /** Sets value for seconds. */
    public void setSecond(int s) {
        cal.set(Calendar.SECOND, s);
    }

    /** Return value of milliseconds. */
    public int getMilliSecond() {
        return cal.get(Calendar.MILLISECOND);
    }
    /** Sets value for milliseconds. */
    public void setMilliSecond(int ms) {
        cal.set(Calendar.MILLISECOND, ms);
    }


    /**
        Returns java.util.Date object that matches this DateTime.
    */
    public Date getDate() {
        return cal.getTime();
    }

    /**
        Returns day of week for date.  See Calendar for list of days (ex: SUNDAY).
        @see Calendar
    */
    public int getDayOfWeek() {
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    /**
        Returns day of year, where Jan 1 is 1.
    */
    public int getDayOfYear() {
        return cal.get(Calendar.DAY_OF_YEAR);
    }
    /** Returns the number of the week within the month, where first week is 1. */
    public int getWeekOfMonth() {
        return cal.get(Calendar.WEEK_OF_MONTH);
    }
    /** Returns number week within the year, where first week is 1. */
    public int getWeekOfYear() {
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

    /** Returns number of days in this month. */
    public int getDaysInMonth() {
        return cal.getActualMaximum(cal.DAY_OF_MONTH);
        /* 2004/1/15 was:
        Calendar cal = null;
        try {
            cal = (Calendar) this.cal.clone();
        }
        catch (Exception e) {}

        cal.set(Calendar.DAY_OF_MONTH,1);
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DATE, -1);
        return cal.get(Calendar.DAY_OF_MONTH);
        */
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

    // 2007/02/16
    public int hashCode() {
    	return (int) cal.getTime().hashCode();
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

    /**
        Compares this object with the specified object for order.<br>
        Returns a negative integer, zero, or a positive integer as this object is less than, equal to,
        or greater than the specified object.

        @param obj Date, OADate, Calendar, String
        @returns "0" if equal, "-1" if this OADateTime is less than, "1" if this OADateTime is greater than,
           "2" if objects can not be compared.
    */
    public int compareTo(Object obj) {
        if (obj == null) return 1;
        OADateTime d = convert(obj);
        if (d == null) return 2;
        if ( this.cal.equals(d.cal) ) return 0;
        if ( this.cal.before(d.cal) ) return -1;
        return 1;
    }

    /**
        Return an OADateTime where a specified amount of days is added.
        <p>
        Note: if this is an instanceof OADate or OATime, then the returned object will be the same type.
        @param amount number of days to increment/deincrement (negative number).
        @return new OADateTime object.
    */
    public OADateTime addDays(int amount) {
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
        dt.cal.add(Calendar.DATE,amount);
        return dt;
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
        dt.cal.add(Calendar.MONTH,amount);
        return dt;
    }

    /**
        Return an OADateTime where a specified amount of years is added.
        <p>
        Note: if this is an instanceof OADate or OATime, then the returned object will be the same type.
        @param amount number of years to increment/deincrement (negative number).
        @return new OADateTime object.
    */
    public OADateTime addYears(int amount) {
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
        dt.cal.add(Calendar.YEAR,amount);
        return dt;
    }

    /**
        Return an OADateTime where a specified amount of hours is added.
        <p>
        Note: if this is an instanceof OADate or OATime, then the returned object will be the same type.
        @param amount number of hours to increment/deincrement (negative number).
        @return new OADateTime object.
    */
    public OADateTime addHours(int amount) {
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
        dt.cal.add(Calendar.HOUR,amount);
        return dt;
    }

    /**
        Return an OADateTime where a specified amount of minutes is added.
        <p>
        Note: if this is an instanceof OADate or OATime, then the returned object will be the same type.
        @param amount number of minutes to increment/deincrement (negative number).
        @return new OADateTime object.
    */
    public OADateTime addMinutes(int amount) {
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
        dt.cal.add(Calendar.MINUTE,amount);
        return dt;
    }

    /**
        Return an OADateTime where a specified amount of seconds is added.
        <p>
        Note: if this is an instanceof OADate or OATime, then the returned object will be the same type.
        @param amount number of seconds to increment/deincrement (negative number).
        @return new OADateTime object.
    */
    public OADateTime addSeconds(int amount) {
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
        dt.cal.add(Calendar.SECOND,amount);
        return dt;
    }

    /**
        Return an OADateTime where a specified amount of milliseconds is added.
        <p>
        Note: if this is an instanceof OADate or OATime, then the returned object will be the same type.
        @param amount number of milliseconds to increment/deincrement (negative number).
        @return new OADateTime object.
    */
    public OADateTime addMilliSeconds(int amount) {
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
        dt.cal.add(Calendar.MILLISECOND,amount);
        return dt;
    }


    /**
        Returns the number of years between this OADateTime and obj.
        @param obj Date, OADateTime, Calendar, etc that can be converted to an OADateTime.
    */
    public int betweenYears(Object obj) {
        OADateTime d = convert(obj);
        return Math.abs(this.getYear() - d.getYear());
    }


    /**
        Returns the number of months betweeen this OADateTime and obj.
        @param obj Date, OADateTime, Calendar, etc that can be converted to an OADateTime.
    */
    public int betweenMonths(Object obj) {
        OADateTime d = convert(obj);

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
        Returns the number of days betweeen this OADateTime and obj.
        @param obj Date, OADateTime, Calendar, etc that can be converted to an OADateTime.
    */
    public int betweenDays(Object obj) {
        OADateTime d = convert(obj);
        d.setTime(this.getHour(), this.getMinute(), this.getSecond(), this.getMilliSecond());
        double millis = Math.abs(this.cal.getTime().getTime() - d.cal.getTime().getTime());
        
        // this accounts for DLS days where an hour is added or subtracted.
        return (int) Math.floor(millis/(1000 * 60 * 60 * 24) + .5d);
        
        // 20091012 was: does not account for DLS
        // return (int) Math.ceil( millis/(1000 * 60 * 60 * 24));
     }


    /**
        Returns the number of hours betweeen this OADateTime and obj.
        @param obj Date, OADateTime, Calendar, etc that can be converted to an OADateTime.
    */
    public int betweenHours(Object obj) {
        //qqqqqqqqq make sure getTime() will account for leap years.  Might need to use between months, and then get days
        OADateTime d = convert(obj);
        d.setTime(d.getHour(), this.getMinute(), this.getSecond(), this.getMilliSecond());
        double millis = Math.abs(this.cal.getTime().getTime() - d.cal.getTime().getTime());
        return (int) Math.ceil( millis/(1000 * 60 * 60));
    }

    /**
        Returns the number of minutes betweeen this OADateTime and obj.
        @param obj Date, OADateTime, Calendar, etc that can be converted to an OADateTime.
    */
    public int betweenMinutes(Object obj) {
        //qqqqqqqqq make sure getTime() will account for leap years.  Might need to use between months, and then get days
        /// need to account for DST
        OADateTime d = convert(obj);
        d.setTime(d.getHour(), d.getMinute(), this.getSecond(), this.getMilliSecond());
        double millis = Math.abs(this.cal.getTime().getTime() - d.cal.getTime().getTime());
        return (int) Math.ceil(millis/(1000 * 60));
    }

    /**
        Returns the number of seconds betweeen this OADateTime and obj.
        @param obj Date, OADateTime, Calendar, etc that can be converted to an OADateTime.
    */
    public int betweenSeconds(Object obj) {
        //qqqqqqqqq make sure getTime() will account for leap years.  Might need to use between months, and then get days
        OADateTime d = convert(obj);
        d.setTime(d.getHour(), d.getMinute(), d.getSecond(), this.getMilliSecond());

        double millis = Math.abs(this.cal.getTime().getTime() - d.cal.getTime().getTime());
        return (int) Math.ceil(millis/(1000));
    }

    /**
        Returns the number of seconds betweeen this OADateTime and obj.
        @param obj Date, OADateTime, Calendar, etc that can be converted to an OADateTime.
    */
    public long betweenMilliSeconds(Object obj) {
        //qqqqqqqqq make sure getTime() will account for leap years.  Might need to use between months, and then get days
        OADateTime d = convert(obj);
        long millis = Math.abs(this.cal.getTime().getTime() - d.cal.getTime().getTime());
        return millis;
    }

    /**
     * Time as miliseconds, same as Date.getTime()
     */
    public long getTime() {
        return this.cal.getTime().getTime();
    }
    
    /**
        Convert an Object to an OADateTime.
    */
    protected OADateTime convert(Object obj) {
        if (obj == null) return null;

        if (obj instanceof OADateTime) return (OADateTime) obj;
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
        Date d = valueOfMain(strDateTime, fmt, vecParseFormat, staticOutputFormat);
        if (d == null) {
            d = valueOfMain(fixDate(strDateTime), fmt, vecParseFormat, staticOutputFormat);
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
        @see #getGlobalParseFormats
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
        vecParseFormat.addElement(fmt);
    }
    /**
        Remove a global parse format.
        @see addGlobalParseFormat
    */
    public static void removeGlobalParseFormat(String fmt) {
        vecParseFormat.removeElement(fmt);
    }
    /**
        Remove a all globally used parse format.
        @see addGlobalParseFormat
    */
    public static void removeAllGlobalParseFormats() {
        vecParseFormat.removeAllElements();
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

    
    // this verfies that betweenDays works correctly with leap years and DLS (daylight savings) DLS will have a 23hr day and a 25hr day
    public static void main(String[] args) {
        OADate d1 = new OADate(1201, Calendar.SEPTEMBER, 27);
        OADate d2 = new OADate(d1);
        for (int i=0; i<500000; i++) {
            int x = d1.betweenDays(d2);
            if (i != x) {
                x = d1.betweenDays(d2);
                System.out.println("Error: "+d2);
                break;
            }
            d2 = (OADate) d2.addDays(1);
        }
        System.out.println("Done => "+d2);
    }

    public static void mainXX(String[] args) {
        OADateTime dt = new OADateTime(1965, Calendar.MAY, 4, 12, 0, 0);
        int x = dt.getCalendar().get(Calendar.ZONE_OFFSET);
        System.out.println("=>"+dt.toString("MMddyyy HH:mm:ss a"));
        System.out.println("=>"+x);

        dt.getCalendar().add(Calendar.HOUR, 3);
        
        dt.cal.add(Calendar.ZONE_OFFSET, 5);
        x = dt.getCalendar().get(Calendar.ZONE_OFFSET);
        
        System.out.println("=>"+dt.toString("MMddyyy HH:mm:ss a"));
        System.out.println("=>"+x);
    }
}


