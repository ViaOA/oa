package com.viaoa.util;

import java.util.*;
import java.text.*;
import java.sql.Time;

/**
    Date class that combines Calendar, Date and SimpleDateFormat into a single class.
    <p>
    OADate is not affected by timezone.  A date created on one system will be the same on
    another machine, even if the timezone is different.

    See OADateTime for list or formatting symbols.
    @see OADateTime
*/
public class OADate extends OADateTime {
    private static final long serialVersionUID = 1L;

    // Unique for this subclass
    /** default output format.  Default is DateFormat.SHORT */
    protected static String dateOutputFormat;

    /** default parse formats:  "MM/dd/yy",  "MM/dd/yyyy" or "dd/MM/yy", "dd/MM/yyyy"*/
    protected static Vector vecDate = new Vector(10,10);

    static {
        setLocale(Locale.getDefault());
    }
    
    public static void setLocale(Locale loc) {
        vecParseFormat = new Vector(15,10);
        String s = getFormat(DateFormat.SHORT, loc);
        boolean bMonthFirst = true;
        boolean bYearFirst = false;
        if (s != null && s.length() > 0) {
            char ch = s.charAt(0);
            if (ch != 'M') bMonthFirst = false;
            if (ch == 'y') bYearFirst = true;;
            vecParseFormat.addElement(s);
        }
        if (bMonthFirst) {
            vecDate.addElement("MM/dd/yy"); // must be before "MM/dd/yyyy" since "MM/dd/yyyy" will convert 5/4/65 -> 05/04/0065
            vecDate.addElement("MM/dd/yyyy");
            dateOutputFormat = "MM/dd/yyyy";
        }
        else if (bYearFirst) {
            vecDate.addElement("yy/MM/dd"); // must be before "MM/dd/yyyy" since "MM/dd/yyyy" will convert 5/4/65 -> 05/04/0065
            vecDate.addElement("yyyy/MM/ddy");
            dateOutputFormat = "yyyy/MM/dd";
        }
        else {  // day first
            vecDate.addElement("dd/MM/yy");
            vecDate.addElement("dd/MM/yyyy");
            dateOutputFormat = "dd/MM/yyyy";
        }
        // SQL date formates
        vecDate.addElement("yyyy-MM-dd");
    }


    /**
        Create a new date that uses todays date. Same as new OADate(new Date())
    */
    public OADate() {
       super(new Date());
       clearTime();
    }

    /**
        Create a new date that uses a specified Date.
    */

    public OADate(Date date) {
        super(date);
        clearTime();
    }

    public OADate(long time) {
    	this(new Date(time));
    }
    
    
    /**
        Create a new date that uses a specified Time.
    */
    public OADate(Time time) {
        super(time);
        clearTime();
    }


    /**
        Create a new date that uses a specified Calendar.
    */
    public OADate(Calendar c) {
        super(c);
        clearTime();
    }
    /**
        Create a new date that uses a specified OADateTime.
    */
    public OADate(OADateTime odt) {
        super(odt);
        clearTime();
    }

    /**
        Create a new date from a specified String.
        <p>
        @see OADate#valueOf
    */
    public OADate(String strDate) {
        this( OADate.valueOf2(strDate) );
        clearTime();
    }

    /**
        Create a new date from a specified String, using a specified format.
        <p>
        @see OADateTime
        @see OADate#valueOf
    */
    public OADate(String strDate, String format) {
        super(strDate, format);
        clearTime();
    }


    /**
       Create new date using year, month, day.
       @param year full year (not year minus 1900 like Date)
       param month 0-11, use Calendar.JUNE, etc.
       param date day of the month (1-31)
    */
    public OADate(int year, int month, int day) {
        super(year,month,day);
        clearTime();
    }


    /**
        Sets the default global format used when converting OADate to String.
        This format will be used if this dates format has not been set and a format is not specified.
        @see #setFormat
    */
    public static void setGlobalOutputFormat(String fmt) {
        dateOutputFormat = fmt;
    }
    /**
        Returns the default global format used when converting OADate to String.
    */
    public static String getGlobalOutputFormat() {
        return dateOutputFormat;
    }

    /**
        Sets the default global parse format used when converting a String to OADate.
        @see #setFormat
    */
    public static void addGlobalParseFormat(String fmt) {
        vecDate.addElement(fmt);
    }
    /**
        Removes a global parse format used when converting a String to OADate.
    */
    public static void removeGlobalParseFormat(String fmt) {
        vecDate.removeElement(fmt);
    }
    /**
        Removes all global parse formats that are used to convert Strings to OADates.
    */
    public static void removeAllGlobalParseFormats() {
        vecDate.removeAllElements();
    }

    /**
        Compare this OADate with another date.
        @returns "0" if equal, "-1" if this OADateTime is less than, "1" if this OADateTime is greater than,
           "2" if objects can not be compared.
        @param obj Date, OADate, Calendar
    */
    public int compare(Object obj) {
        return compareTo(obj);
    }
    public int compareTo(Object obj) {
        OADateTime d = convert(obj);
        if (d == null) return 2;
        d.clearTime();

        if ( this.cal.equals(d.cal) ) return 0;
        if ( this.cal.before(d.cal) ) return -1;
        return 1;
    }

    /**
        Compares this date with two dates to see if this date is between them.
        @returns true if this.OADate is GreaterThan or Equal to Obj1 and LessThan or Equal Obj2
        @param obj Date, OADate, Calendar
        @param obj2 Date, OADate, Calendar
    */
    public boolean between(Object obj1, Object obj2) {
        int i = compare(obj1);
        if (i < 0) return false;
        i = compare(obj2);
        return (i <= 0);
    }

    /**
        Converts this date to a String value using default format.
        The default format is the first format that has been set: "format", "dateOutputFormat" else or "yyyy-MMM-dd"
        See OADateTime for list of formatting symbols.
        @see OADateTime
    */
    public String toString() {
        return toString(null);
    }
    /**
        Converts this date to a String value using specified format.
        See OADateTime for list of formatting symbols.
        @see OADateTime
    */
    public String toString(String f) {
        if (f == null) {
            f = (format == null) ? dateOutputFormat : format;
            if (f == null || f.length() == 0) f = "yyyy-MMM-dd";
        }
        return toStringMain(f);
    }


    /**
        Converts a String to an OADate.
        @see #valueOf(String,String)
    */
    public static OADate dateValue(String date, String fmt) {
        return (OADate) valueOf(date,fmt);
    }
    /**
        Converts a String to an OADate.
        @see #valueOf(String,String)
    */
    public static OADate dateValue(String date) {
        return (OADate) valueOf(date,null);
    }


    /**
        Converts a String to an OADate.
        See OADateTime for list of formatting symbols.  If date can not be parsed based on supplied format, then
        other formatting and conversions will be used to try to convert to an OADate.
        @param fmt is format to use for parsing.  See OADateTime for list of formatting symbols.
        @see OADateTime
        @see #valueOf(String,String)
        @see OADateTime
    */
    public static OADateTime valueOf(String date, String fmt) {
        if (date == null) return null;
        Date d = valueOfMain(date, fmt, vecDate, dateOutputFormat);
        if (d == null) {
            if (date.length() < 6 && OAString.isNumber(date)) {
                return OADate.valueOf(date + "/" + (new OADate()).getYear());
            }

            d = valueOfMain(fixDate(date), fmt, vecDate, dateOutputFormat);
            if (d == null) return null;
        }
        return new OADate(d);
    }
    /**
        Converts a String to an OADate.
        @see #valueOf(String,String)
    */
    public static OADateTime valueOf(String date) {
        return OADate.valueOf(date, null);
    }
    /**
     * Throws an exception if date if not valid.
     */
    public static OADateTime valueOf2(String date) {
        OADateTime dt = OADate.valueOf(date, null);
        if (dt == null) throw new IllegalArgumentException("OADate cant create date from String \""+date+"\"");
        return dt;
    }


/****
    public void setTime(int hr, int m) {
        throw new RuntimeException("cant set time on an OADate object");
    }
    public void setTime(int hr, int m, int s) {
        throw new RuntimeException("cant set time on an OADate object");
    }
    public void setTime(int hr, int m, int s, int ms) {
        throw new RuntimeException("cant set time on an OADate object");
    }

    public void setHour(int hr) {
        if (hr > 0) throw new RuntimeException("cant set hour on an OADate object");
    }

    public void setMinute(int m) {
        if (m > 0) throw new RuntimeException("cant set minute on an OADate object");
    }

    public void setSecond(int s) {
        if (s > 0) throw new RuntimeException("cant set second on an OADate object");
    }

    public void setMilliSecond(int ms) {
        if (ms > 0) throw new RuntimeException("cant set millisecond on an OADate object");
    }
****/

/****
    public static void main(String[] args) {
        Date date = null;
        OADate d = new OADate(date);
        System.out.println("-----> "+d);
    }
****/
}


