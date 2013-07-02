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


/**
    Time class that combines Calendar, Time and SimpleDateFormat into a single class.
    <p>
    OATime is not affected by timezone.  A time created on one system will be the same on
    another machine, even if the timezone is different.

    See OADateTime for list or formatting symbols.
    @see OADateTime
*/
public class OATime extends OADateTime {
    private static final long serialVersionUID = 1L;

    // Unique for this subclass
    /** default output format */
    protected static String timeOutputFormat = "hh:mma";

    /** default parse formats */
    protected static Vector vecTimeFormat = new Vector(10,10);

    static {
        vecTimeFormat.addElement("hh:mm:ss.Sa");
        vecTimeFormat.addElement("hh:mm:ssa");
        vecTimeFormat.addElement("hh:mma");

        vecTimeFormat.addElement("HH:mm:ss.S");
        vecTimeFormat.addElement("HH:mm:ss");
        vecTimeFormat.addElement("HH:mm");
    }
    

    /**
        Create a new time that uses that uses the current Time.
        <p>
        Note: clearDate() is called to set date information to 0.
    */
    public OATime() {
        this(new Date());
        clearDate();
    }

    /**
        Create a new time that uses that uses a supplied Time.
        <p>
        Note: clearDate() is called to set date information to 0.
    */
    public OATime(java.sql.Time time) {
        super(time);
        clearDate();
    }
    /**
        Create a new time that uses that uses a supplied Date.
        <p>
        Note: clearDate() is called to set date information to 0.
    */
    public OATime(Date date) {
        this(new java.sql.Time(date.getTime()));
        clearDate();
    }
    public OATime(long time) {
        this(new java.sql.Time(time));
        clearDate();
    }
    /**
        Create a new time that uses that uses a supplied Calendar.
        <p>
        Note: clearDate() is called to set date information to 0.
    */
    public OATime(Calendar c) {
        super(c);
        clearDate();
    }
    /**
        Create a new time that uses that uses a supplied OADateTime.
        <p>
        Note: clearDate() is called to set date information to 0.
    */
    public OATime(OADateTime od) {
        super(od);
        clearDate();
    }

    /**
        Create a new time that uses a supplied String.
        <p>
        Note: uses default format of "hh:mma"
        Note: clearDate() is called to set date information to 0.
        @see OATime#valueOf
    */
    public OATime(String strTime) {
        this( OATime.valueOf(strTime) );
        clearDate();
    }

    /**
        Create a new time that uses a supplied String and format.
        <p>
        Note: clearDate() is called to set date information to 0.
        @see OATime#valueOf
    */
    public OATime(String strTime, String fmt) {
        this( OATime.valueOf(strTime, fmt) );
        clearDate();
    }


    /**
        Create a new time that uses that uses a supplied hours, minutes, seconds.
        <p>
        Note: clearDate() is called to set date information to 0.
        @param hrs 0-23
        @see OATime#valueOf
    */
    public OATime(int hrs, int mins, int secs) {
        super(0, 0,0 , hrs, mins, secs, 0);
        clearDate();
    }

    /**
        Create a new time that uses that uses a supplied hours, minutes, seconds, milliseconds.
        <p>
        Note: clearDate() is called to set date information to 0.
        @param hrs 0-23
        @see OATime#valueOf
    */
    public OATime(int hrs, int mins, int secs, int mili) {
        super(0, 0,0 , hrs, mins, secs, mili);
        clearDate();
    }


    /**
        Time comparision with any object.  Object will first be converted to OATime.
        @param obj Date, OADate, Calendar
        @returns "0" if equal, "-1" if this OADateTime is less than, "1" if this OADateTime is greater than,
           "2" if objects can not be compared.
    */
    public int compare(Object obj) {
        return this.compareTo(obj);
    }
    public int compareTo(Object obj) {
        OADateTime d = convert(obj);
        if (d == null) return 2;
        d.clearDate();

        if ( this.cal.equals(d.cal) ) return 0;
        if ( this.cal.before(d.cal) ) return -1;
        return 1;
    }

    /**
        Converts this time to a String value using default format.
        The default format is the first format that has been set: "format", "timeOutputFormat" else or "hh:mma"
        See OADateTime for list of formatting symbols.
        @see OADateTime
    */
    public String toString() {
        return toString(null);
    }

    /**
        Converts this time to a String value using supplied format.
        See OADateTime for list of formatting symbols.
        @see OADateTime
    */
    public String toString(String f) {
        if (f == null) {
            f = (format == null) ? timeOutputFormat : format;
            if (f == null || f.length() == 0) f = "hh:mma";
        }
        return toStringMain(f);
    }


    /**
        Converts a String to an OATime using a supplied format.
        See OADateTime for list of formatting symbols.
        @see #valueOf(String,String)
        @see OADateTime
    */
    public static OATime timeValue(String time, String fmt) {
        return (OATime) valueOf(time, fmt);
    }


    /**
        Converts a String to an OATime using a default format.
        The default format is the first format that has been set: "format", "timeOutputFormat" else or "hh:mma"
        See OADateTime for list of formatting symbols.
        @see #valueOf(String,String)
        @see OADateTime
    */
    public static OATime timeValue(String time) {
        return (OATime) valueOf(time, null);
    }


    /**
        Converts a String to an OATime.
        See OADateTime for list of formatting symbols.  If time can not be parsed based on supplied format, then
        other formatting and conversions will be used to try to convert to an OATime.
        <p>
        Note: you will need to cast the return value to a OATime.
        @param fmt is format to use for parsing.  See OADateTime for list of formatting symbols.
        @see OADateTime
        @see #timeValue(String,String)
    */
    public static OADateTime valueOf(String time, String fmt) {
        if (time != null && time.length() > 0) {
            char c = time.charAt(time.length()-1);
            if (c == 'A' || c == 'a' || c == 'P' || c == 'p') time += "m";
        }

        Date d = valueOfMain(time, fmt, vecTimeFormat, timeOutputFormat);
        if (d == null) {
            return null;
        }
        return new OATime(d);
    }

    /**
        Converts a String to an OATime using a default format.
        The default format is the first format that has been set: "format", "timeOutputFormat" else or "hh:mma"
        See OADateTime for list of formatting symbols.
        <p>
        Note: you will need to cast the return value to a OATime.
        @see #valueOf(String,String)
        @see OADateTime
        @see #timeValue(String,String)
    */
    public static OADateTime valueOf(String time) {
        return OATime.valueOf(time, null);
    }



    /**
        Sets the default global format used when converting OADate to String.
        @see OADate#setFormat
    */
    public static void setGlobalOutputFormat(String fmt) {
        timeOutputFormat = fmt;
    }
    /**
        Gets the default global format used when converting OADate to String.
        @see OADate#setFormat
    */
    public static String getGlobalOutputFormat() {
        return timeOutputFormat;
    }

    /**
        Sets the default global parse format used when converting a String to OATime.
        @see OADate#setFormat
    */
    public static void addGlobalParseFormat(String fmt) {
        vecTimeFormat.addElement(fmt);
    }
    /**
        Removes a default global parse format that is used when converting a String to OATime.
        @see OADate#setFormat
    */
    public static void removeGlobalParseFormat(String fmt) {
        vecTimeFormat.removeElement(fmt);
    }
    /**
        Removes all global parse formats that are used when converting a String to OATime.
        @see OADate#setFormat
    */
    public static void removeAllGlobalParseFormats() {
        vecTimeFormat.removeAllElements();
    }

    /**
        Always returns 0.
    */
    public int getYear() {
        return 0;
    }
    /**
        Always returns 0.
    */
    public int getMonth() {
        return 0;
    }
    /**
        Always returns 0.
    */
    public int getDay() {
        return 0;
    }
/**
    public void setYear(int y) {
        if (y > 0) throw new RuntimeException("cant set year on an OATime object");
    }

    public void setMonth(int m) {
        if (m > 0) throw new RuntimeException("cant set month on an OATime object");
    }

    public void setDay(int d) {
        if (d > 0) throw new RuntimeException("cant set day on an OATime object");
    }
    public void setDate(int yr, int m, int d) {
        throw new RuntimeException("cant set date on an OATime object");
    }
****/
    
    public static void main(String[] args) {
        // this is the start time - grep "Opened file" *error*.log        
        OATime dt = new OATime("19:08:28.024", "HH:mm:ss.SSS");
        
        // this is the gc timestamp to find        
        dt = (OATime) dt.addSeconds(69099);
        dt = (OATime) dt.addMilliSeconds(830);
        
        System.out.print("==> "+dt.toString("hh:mm:ss.SSS aa"));        
        System.out.println(" ==> "+dt.toString("HH:mm:ss.SSS"));        
    }
}


























