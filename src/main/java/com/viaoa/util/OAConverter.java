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

import java.math.BigDecimal;

import java.util.*;

import com.viaoa.util.converter.*;

/** 
    Conversion class for converting Objects from one form to another.
    Most common usage is to convert Objects to Strings, String to Objects, Formatting, Decimal Arithmetic.<br>

    <p>
    contains two key static methods:
    toString(Object, format) & toObject(class, String, format)
    @see OAConverterNumber
    @see OAConverterOADate
    @see OAString#format(String,String) OAString.format()
    @see OADate
*/
public class OAConverter {

    protected static Hashtable hashtable = new Hashtable(10,.75f);

    protected static String dateFormat, timeFormat, dateTimeFormat, 
    				integerFormat, decimalFormat, bigDecimalFormat, moneyFormat="\u00A4#,##0.00", booleanFormat;

    static {
        addConverter( String.class, new OAConverterString() ); 
        addConverter( Number.class, new OAConverterNumber() ); 
        addConverter( Character.class, new OAConverterCharacter() );
        addConverter( Boolean.class, new OAConverterBoolean() );
        addConverter( BigDecimal.class, new OAConverterBigDecimal() ); 

        addConverter( java.util.Date.class, new OAConverterDate() );
        addConverter( java.sql.Time.class, new OAConverterTime() );
        addConverter( java.sql.Timestamp.class, new OAConverterTimestamp() );

        addConverter( com.viaoa.util.OADateTime.class, new OAConverterOADateTime() );
        addConverter( com.viaoa.util.OADate.class, new OAConverterOADate() );
        addConverter( com.viaoa.util.OATime.class, new OAConverterOATime() );

        addConverter( java.awt.Point.class, new OAConverterPoint() );
        addConverter( java.awt.Dimension.class, new OAConverterDimension() );
        addConverter( java.awt.Rectangle.class, new OAConverterRectangle() );
        addConverter( java.awt.Color.class, new OAConverterColor() );
        addConverter( java.awt.Font.class, new OAConverterFont() );

        addConverter( Enum.class, new OAConverterEnum() );
    }

    private static OAConverterArray oaConverterArray = new OAConverterArray();
    
    
    /**
        Returns a OAConverterInterface Object for a specific Class.
        @see OAConverterInterface
    */
    public static OAConverterInterface getConverter(Class clazz) {
        if (clazz != null && clazz.isPrimitive()) {
            clazz = OAReflect.getPrimitiveClassWrapper(clazz);
        }
        for ( ;clazz != null; ) {        
            OAConverterInterface  ci = (OAConverterInterface) hashtable.get(clazz); 
            if (ci != null) return ci;
            clazz = clazz.getSuperclass();
            if (clazz == null || clazz.equals(Object.class)) break;
        }
        return null;
    }
    public static void addConverter(Class clazz, OAConverterInterface oci) {
        hashtable.put(clazz, oci);
    }

    

    /**
        Returns formatting default String to use for specific Class.
    */
    public static String getFormat(Class clazz) {
        if ( clazz == null || clazz.equals(String.class) ) return null;
        if ( clazz.equals(BigDecimal.class) ) return getBigDecimalFormat();
        if ( OAReflect.isInteger(clazz)) return getIntegerFormat();
        if ( OAReflect.isFloat(clazz)) return getDecimalFormat();
        
        if ( Date.class.equals(clazz) ) return getDateFormat();
        if ( java.sql.Time.class.equals(clazz) ) return getTimeFormat();
        if ( java.sql.Timestamp.class.equals(clazz) ) return getDateTimeFormat();

        if ( OADateTime.class.equals(clazz) ) return getDateTimeFormat();
        if ( OADate.class.equals(clazz) ) return getDateFormat();
        if ( OATime.class.equals(clazz) ) return getTimeFormat();
        if ( Calendar.class.isAssignableFrom(clazz) ) return getDateFormat();

        if ( clazz.equals(boolean.class) || clazz.equals(Boolean.class)) return getBooleanFormat();
        
        return null;
    }

    /** 
        Default format to use for dates.  
        <p>
        Note: OAConverter.toString() will not automatically use this format 
        unless it is sent as a parameter.  This method is to be used as a global area for other APIs to store 
        default system formatting.
        Note: OADate is used for formatting dates and has its own default formatting if one is not supplied.
        @see OADate#OADate for format options and default formatting/parsing values
    */
    public static String getDateFormat() {
        if (dateFormat == null) return OADate.getGlobalOutputFormat();
        return dateFormat;
    }
    /**
        Set default format for Dates.
        @see #getDateFormat
    */
    public static void setDateFormat(String fmt) {
        dateFormat = fmt;
    }

    /** default format to use for times.  
        Note: OAConverter.toString() will not automatically use this format 
        unless it is sent as a parameter.  This method is to be used as a global area for other APIs to store 
        default system formatting.
        Note: OADate is used for formatting times and has its own default formatting.
        @see OADate#OADate for format options and default formatting/parsing values
    */
    public static String getTimeFormat() {
        if (timeFormat == null) return OATime.getGlobalOutputFormat();
        return timeFormat;
    }
    /**
        Set default format for Times.
        @see #getTimeFormat
    */
    public static void setTimeFormat(String fmt) {
        timeFormat = fmt;
    }

    /** default format to use for datetimes.  
        Note: OAConverter.toString() will not automatically use this format 
        unless it is sent as a parameter.  This method is to be used as a global area for other APIs to store 
        default system formatting.
        Note: OADate is used for formatting times and has its own default formatting.
        @see OADate#OADate for format options and default formatting/parsing values
    */
    public static String getDateTimeFormat() {
        if (timeFormat == null) return OADateTime.getGlobalOutputFormat();
        return dateTimeFormat;
    }
    /**
        Set default format for DateTimes.
        @see #getDateTimeFormat
    */
    public static void setDateTimeFormat(String fmt) {
        dateTimeFormat = fmt;
    }
    
    /** 
        Default format to use for integer values.
        <p>
        Note: OAConverter.toString() will not automatically use this format 
        unless it is sent as a parameter.  This method is to be used as a global area for other APIs to store 
        default system formatting.
        @see OANumberConverter#OANumberConverter for format options
    */
    public static String getIntegerFormat() {
        return integerFormat;
    }
    /**
        Set default format for Integers.
        @see #getIntegerFormat
    */
    public static void setIntegerFormat(String fmt) {
        integerFormat = fmt;
    }
    /** 
        Default format to use for decimal numbers (floats, doubles).
        <p>
        Note: OAConverter.toString() will not automatically use this format 
        unless it is sent as a parameter.  This method is to be used as a global area for other APIs to store 
        default system formatting.
        @see OAConverterNumber#OAConverterNumber for format options
    */
    public static String getDecimalFormat() {
        return decimalFormat;
    }
    /**
        Set default format for Decimal numbers (floats/doubles).
        @see #getDecimalFormat
    */
    public static void setDecimalFormat(String fmt) {
        decimalFormat = fmt;
    }

    /** 
	    Default format to use for BigDecimal numbers, mostly to represent currency.
	*/
	public static String getBigDecimalFormat() {
	    return bigDecimalFormat;
	}
	/**
	    Default format to use for BigDecimal numbers, mostly to represent currency.
        @see OAConverterNumber#OAConverterNumber for format options
	*/
	public static void setBigDecimalFormat(String fmt) {
		bigDecimalFormat = fmt;
	}

    /** 
        Default format to use for Money/Currency.
    */
    public static String getMoneyFormat() {
        return moneyFormat;
    }
    public static String getCurrencyFormat() {
        return moneyFormat;
    }
    /**
        Default format to use for Money/Currency.
    */
    public static void setMoneyFormat(String fmt) {
        moneyFormat = fmt;
    }
    	
	
    /** default format to use for boolean values when converted to a String.
        <p>
        Note: OAConverter.toString() will not automatically use this format 
        unless it is sent as a parameter.  This method is to be used as a global area for other APIs to store 
        default system formatting.
        @param fmt is a String with a semicolon seperating the three values "true;false;null"
        Example: ("true;false");
    */
    public static String getBooleanFormat() {
        return booleanFormat;
    }

    /**
        Set default format for booleans.
        @see #getBooleanFormat
    */
    public static void setBooleanFormat(String fmt) {
        booleanFormat = fmt;
    }



    /** 
        Uses BigDecimal to round number to "decimalPlaces" amount of decimal numbers. 
        <p>
        <b>Notes:</b><br>
        Since doubles are floating point numbers, there is a chance for loss of precision.<br>
        For rounding, this could cause a problem when the digit that is being removed should be
        a 5, in which case the previous digit might need to be changed based on the type of rounding
        that is being performed (ex: BigDecimal.ROUND_HALF_UP, ROUND_HALF_DOWN, etc.). 
        <p>
        Floating point numbers mostly lose precision when being used for a lot of calculations.
        <p>
        Example: a number "1.5" could be represented as "1.499999" or "1.500001" as a double.
        <br>
        The round method allows you to specify the number of decimal places that you know the number currently should have.
        The round method will first round the number to this amount of decimal places before performing the round.
        
        @param accurateDecimalPlaces is number of decimal places that number should have.  See note above.
        @param decimalPlaces is number of decimal places that number needs to be rounded to.
        @param roundType is type of rounding to perform.  See BigDecimal for types of rounding, ex: BigDecimal.ROUND_HALF_UP
        @see BigDecimal
    */
    public static double round(double d, int accurateDecimalPlaces, int decimalPlaces, int roundType) {
        BigDecimal bd = new BigDecimal(d);
        if (accurateDecimalPlaces != decimalPlaces) bd = bd.setScale(accurateDecimalPlaces, roundType);
        bd = bd.setScale(decimalPlaces, roundType);
        return bd.doubleValue();
    }

    
    /**
        Uses BigDecimal to round number to "decimalPlaces" amount of decimal numbers. 
        <p>
        By Default, uses BigDecimal.ROUND_HALF_UP.
        @param accurateDecimalPlaces is number of decimal places that number should have.  See note above.
        @param decimalPlaces number of decimal places to round to
        @see #round(double,int,int,int) Notes about rounding
        @see BigDecimal
    */
    public static double round(double d, int accurateDecimalPlaces, int decimalPlaces) {
        return round(d, accurateDecimalPlaces, decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }

    
    /** was: Uses BigDecimal to round number to "decimalPlaces" amount of decimal numbers. 
        <p>
        By Default, uses BigDecimal.ROUND_HALF_UP.
        @param decimalPlaces number of decimal places to round to
        @see #round(double,int,int,int) Notes about rounding
        @see BigDecimal
    */
    
    /*
     * Rounds using Round_HALF_UP
     */
    public static double round(double d, int decimalPlaces) {
        // was: return round(d, decimalPlaces, decimalPlaces, BigDecimal.ROUND_HALF_UP);
        
        // this will be faster (no BigDecimal needed)
        if (decimalPlaces < 0) return d;
        
        boolean bNegative;
        if (d < 0) {
            d = Math.abs(d);
            bNegative = true;
        }
        else bNegative = false;
        
        double decimalValue = Math.pow(10, decimalPlaces);
        d *= decimalValue;
        
        d = StrictMath.round(d);
        d /= decimalValue;
        if (bNegative) d *= -1;
        return d;
    }
    
    /**
     * Convert to a long, with "assumed" decimalPlaces
     */
    public static long toLong(double d, int decimalPlaces) {
        if (decimalPlaces < 0) return (long) d;
        
        boolean bNegative;
        if (d < 0) {
            d = Math.abs(d);
            bNegative = true;
        }
        else bNegative = false;
        
        double decimalValue = Math.pow(10, decimalPlaces);
        d *= decimalValue;
        
        long x = StrictMath.round(d);
        if (bNegative) x *= -1;
        return x;
    }
    /**
     * Compare two doubles, using fixed decimal places.
     */
    public static int compare(double d1, double d2, int decimalPlaces) {
        long l1 = toLong(d1, decimalPlaces);
        long l2 = toLong(d2, decimalPlaces);
        if (l1 == l2) return 0;
        return (l1 > l2) ? 1 : -1;
    }
    /**
     * Compare two doubles, using fixed decimal places.
     */
    public static boolean isEqual(double d1, double d2, int decimalPlaces) {
        return compare(d1, d2, decimalPlaces) == 0;
    }
    public static boolean isEqual(double d1, double d2) {
        return compare(d1, d2, 0) == 0;
    }
    
    
    private static final int MATH_OP_MULTIPLY = 0;
    private static final int MATH_OP_DIVIDE = 1;
    private static final int MATH_OP_ADD = 2;
    private static final int MATH_OP_SUBTRACT = 3;

    /**
        Math operations used when working with floating point numbers.
        Numbers are rounded before and after operation.
    */
    private static double mathOp(int opType, Number n1, Number n2, int decimalPlaces, int roundType) {
        BigDecimal bd1;
        if (n1 instanceof BigDecimal) bd1 = (BigDecimal) n1;
        else bd1 = new BigDecimal(n1==null?0.0:n1.doubleValue());
        if (decimalPlaces >= 0 && roundType >= 0) bd1 = bd1.setScale(decimalPlaces, roundType);

        BigDecimal bd2;
        if (n2 instanceof BigDecimal) bd2 = (BigDecimal) n2;
        else bd2 = new BigDecimal(n2==null?0.0:n2.doubleValue());
        if (decimalPlaces >= 0 && roundType >= 0) bd2 = bd2.setScale(decimalPlaces, roundType);

        switch(opType) {
            case MATH_OP_MULTIPLY:
                bd1 = bd1.multiply(bd2);
                break;
            case MATH_OP_DIVIDE:
                int x = roundType;
                if (x <= 0) x = BigDecimal.ROUND_HALF_UP;
                bd1 = bd1.divide(bd2, x);
                break;
            case MATH_OP_ADD:
                bd1 = bd1.add(bd2);
                break;
            case MATH_OP_SUBTRACT:
                bd1 = bd1.subtract(bd2);
                break;
        }
        
        if (decimalPlaces >= 0 && roundType >= 0) bd1 = bd1.setScale(decimalPlaces, roundType);
        return bd1.doubleValue();
    }    
    private static double mathOp(int opType, double d, Number n, int decimalPlaces, int roundType) {
        return mathOp(opType, new BigDecimal(d), n, decimalPlaces, roundType);
    }    
    private static double mathOp(int opType, Number d, double d1, int decimalPlaces, int roundType) {
        return mathOp(opType, d, new BigDecimal(d1), decimalPlaces, roundType);
    }    
    private static double mathOp(int opType, double d, double d1, int decimalPlaces, int roundType) {
        return mathOp(opType, new BigDecimal(d), new BigDecimal(d1), decimalPlaces, roundType);
    }    
    private static double mathOp(int opType, Number n1, Number n2, int decimalPlaces) {
        return mathOp(opType, n1, n2, decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    
    private static double mathOp(int opType, double d, Number n, int decimalPlaces) {
        return mathOp(opType, new BigDecimal(d), n, decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    
    private static double mathOp(int opType, Number d, double d1, int decimalPlaces) {
        return mathOp(opType, d, new BigDecimal(d1), decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    
    private static double mathOp(int opType, double d, double d1, int decimalPlaces) {
        return mathOp(opType, new BigDecimal(d), new BigDecimal(d1), decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    


    // ADD METHODS
    public static double add(Number n1, Number n2, int decimalPlaces, int roundType) {
        return mathOp(MATH_OP_ADD, n1, n2, decimalPlaces, roundType);
    }    
    public static double add(double d, Number n, int decimalPlaces, int roundType) {
        return mathOp(MATH_OP_ADD, new BigDecimal(d), n, decimalPlaces, roundType);
    }    
    public static double add(Number d, double d1, int decimalPlaces, int roundType) {
        return mathOp(MATH_OP_ADD, d, new BigDecimal(d1), decimalPlaces, roundType);
    }    
    public static double add(double d, double d1, int decimalPlaces, int roundType) {
        return mathOp(MATH_OP_ADD, new BigDecimal(d), new BigDecimal(d1), decimalPlaces, roundType);
    }    
    public static double add(Number n1, Number n2, int decimalPlaces) {
        return mathOp(MATH_OP_ADD, n1, n2, decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    
    public static double add(double d, Number n, int decimalPlaces) {
        return mathOp(MATH_OP_ADD, new BigDecimal(d), n, decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    
    public static double add(Number d, double d1, int decimalPlaces) {
        return mathOp(MATH_OP_ADD, d, new BigDecimal(d1), decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }
    public static double add(double d1, double d2, int decimalPlaces) {
        double d = d1 + d2;
        return round(d, decimalPlaces);
        //was: return mathOp(MATH_OP_ADD, new BigDecimal(d1), new BigDecimal(d2), decimalPlaces, BigDecimal.ROUND_HALF_UP);
    } 
   
    public static double add(Number n1, Number n2) {
        return mathOp(MATH_OP_ADD, n1, n2, -1, -1);
    }    
    public static double add(double d, Number n) {
        return mathOp(MATH_OP_ADD, new BigDecimal(d), n, -1, -1);
    }    
    public static double add(Number d, double d1) {
        return mathOp(MATH_OP_ADD, d, new BigDecimal(d1), -1, -1);
    }    
    public static double add(double d, double d1) {
        return mathOp(MATH_OP_ADD, new BigDecimal(d), new BigDecimal(d1), -1, -1);
    }    


    // SUBTRACT METHODS
    public static double subtract(Number n1, Number n2, int decimalPlaces, int roundType) {
        return mathOp(MATH_OP_SUBTRACT, n1, n2, decimalPlaces, roundType);
    }    
    public static double subtract(double d, Number n, int decimalPlaces, int roundType) {
        return mathOp(MATH_OP_SUBTRACT, new BigDecimal(d), n, decimalPlaces, roundType);
    }    
    public static double subtract(Number d, double d1, int decimalPlaces, int roundType) {
        return mathOp(MATH_OP_SUBTRACT, d, new BigDecimal(d1), decimalPlaces, roundType);
    }    
    public static double subtract(double d, double d1, int decimalPlaces, int roundType) {
        return mathOp(MATH_OP_SUBTRACT, new BigDecimal(d), new BigDecimal(d1), decimalPlaces, roundType);
    }    
    public static double subtract(Number n1, Number n2, int decimalPlaces) {
        return mathOp(MATH_OP_SUBTRACT, n1, n2, decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    
    public static double subtract(double d, Number n, int decimalPlaces) {
        return mathOp(MATH_OP_SUBTRACT, new BigDecimal(d), n, decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    
    public static double subtract(Number d, double d1, int decimalPlaces) {
        return mathOp(MATH_OP_SUBTRACT, d, new BigDecimal(d1), decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    
    public static double subtract(double d1, double d2, int decimalPlaces) {
        double d = d1 + d2;
        return round(d, decimalPlaces);
        // return mathOp(MATH_OP_SUBTRACT, new BigDecimal(d1), new BigDecimal(d2), decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    
    public static double subtract(Number n1, Number n2) {
        return mathOp(MATH_OP_SUBTRACT, n1, n2, -1, -1);
    }    
    public static double subtract(double d, Number n) {
        return mathOp(MATH_OP_SUBTRACT, new BigDecimal(d), n, -1, -1);
    }    
    public static double subtract(Number d, double d1) {
        return mathOp(MATH_OP_SUBTRACT, d, new BigDecimal(d1), -1, -1);
    }    
    public static double subtract(double d1, double d2) {
        return mathOp(MATH_OP_SUBTRACT, new BigDecimal(d1), new BigDecimal(d2), -1, -1);
    }    


    // MULTIPLY METHODS
    public static double multiply(Number n1, Number n2, int decimalPlaces, int roundType) {
        return mathOp(MATH_OP_MULTIPLY, n1, n2, decimalPlaces, roundType);
    }    
    public static double multiply(double d, Number n, int decimalPlaces, int roundType) {
        return mathOp(MATH_OP_MULTIPLY, new BigDecimal(d), n, decimalPlaces, roundType);
    }    
    public static double multiply(Number d, double d1, int decimalPlaces, int roundType) {
        return mathOp(MATH_OP_MULTIPLY, d, new BigDecimal(d1), decimalPlaces, roundType);
    }    
    public static double multiply(double d, double d1, int decimalPlaces, int roundType) {
        return mathOp(MATH_OP_MULTIPLY, new BigDecimal(d), new BigDecimal(d1), decimalPlaces, roundType);
    }    
    public static double multiply(Number n1, Number n2, int decimalPlaces) {
        return mathOp(MATH_OP_MULTIPLY, n1, n2, decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    
    public static double multiply(double d, Number n, int decimalPlaces) {
        return mathOp(MATH_OP_MULTIPLY, new BigDecimal(d), n, decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    
    public static double multiply(Number d, double d1, int decimalPlaces) {
        return mathOp(MATH_OP_MULTIPLY, d, new BigDecimal(d1), decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    
    public static double multiply(double d1, double d2, int decimalPlaces) {
        double d = d1 * d2;
        return round(d, decimalPlaces);
        //was: return mathOp(MATH_OP_MULTIPLY, new BigDecimal(d1), new BigDecimal(d2), decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    
    public static double multiply(Number n1, Number n2) {
        return mathOp(MATH_OP_MULTIPLY, n1, n2, -1, -1);
    }    
    public static double multiply(double d, Number n) {
        return mathOp(MATH_OP_MULTIPLY, new BigDecimal(d), n, -1, -1);
    }    
    public static double multiply(Number d, double d1) {
        return mathOp(MATH_OP_MULTIPLY, d, new BigDecimal(d1), -1, -1);
    }    
    public static double multiply(double d, double d1) {
        return mathOp(MATH_OP_MULTIPLY, new BigDecimal(d), new BigDecimal(d1), -1, -1);
    }    


    // DIVIDE METHODS
    public static double divide(Number n1, Number n2, int decimalPlaces, int roundType) {
        return mathOp(MATH_OP_DIVIDE, n1, n2, decimalPlaces, roundType);
    }    
    public static double divide(double d, Number n, int decimalPlaces, int roundType) {
        return mathOp(MATH_OP_DIVIDE, new BigDecimal(d), n, decimalPlaces, roundType);
    }    
    public static double divide(Number d, double d1, int decimalPlaces, int roundType) {
        return mathOp(MATH_OP_DIVIDE, d, new BigDecimal(d1), decimalPlaces, roundType);
    }    
    public static double divide(double d, double d1, int decimalPlaces, int roundType) {
        return mathOp(MATH_OP_DIVIDE, new BigDecimal(d), new BigDecimal(d1), decimalPlaces, roundType);
    }    
    public static double divide(Number n1, Number n2, int decimalPlaces) {
        return mathOp(MATH_OP_DIVIDE, n1, n2, decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    
    public static double divide(double d, Number n, int decimalPlaces) {
        return mathOp(MATH_OP_DIVIDE, new BigDecimal(d), n, decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    
    public static double divide(Number d, double d1, int decimalPlaces) {
        return mathOp(MATH_OP_DIVIDE, d, new BigDecimal(d1), decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    
    public static double divide(double d1, double d2, int decimalPlaces) {
        double d = d1 / d2;
        return round(d, decimalPlaces);
        //was: return mathOp(MATH_OP_DIVIDE, new BigDecimal(d1), new BigDecimal(d2), decimalPlaces, BigDecimal.ROUND_HALF_UP);
    }    
    public static double divide(Number n1, Number n2) {
        return mathOp(MATH_OP_DIVIDE, n1, n2, -1, -1);
    }    
    public static double divide(double d, Number n) {
        return mathOp(MATH_OP_DIVIDE, new BigDecimal(d), n, -1, -1);
    }    
    public static double divide(Number d, double d1) {
        return mathOp(MATH_OP_DIVIDE, d, new BigDecimal(d1), -1, -1);
    }    
    public static double divide(double d, double d1) {
        return mathOp(MATH_OP_DIVIDE, new BigDecimal(d), new BigDecimal(d1), -1, -1);
    }    



//========== Convert from one type to another type

    /**  
        Convert an object "value" to another type "clazz" using a format "fmt"
        <p>
        This will call getConverter(clazz).  If the converter is null or it returns
        a null, then getConverter(value.getClass()) will be called.
        <p>
        <b>Note:</b> If clazz is String and value is null, then a blank String "" is returned.
        @param fmt is format to use for parsing/formatting.
        @returns converted object of type clazz or null if converstion can not be done. 
        @see #getConverter
    */
    public static Object convert(Class clazz, Object value, String fmt) {
        if (clazz == null) {
            return value;
        }
        if (clazz.isPrimitive()) {
            clazz = OAReflect.getPrimitiveClassWrapper(clazz);
        }
        if (value != null && value.getClass().equals(clazz)) {
            if (fmt == null) return value;
        }

        OAConverterInterface oci = OAConverter.getConverter(clazz);
        if (oci == null && value != null) oci = OAConverter.getConverter(value.getClass());
        if (oci != null) {
            if (Object.class.equals(clazz)) return value; // 20091009
        	return oci.convert(clazz, value, fmt);
        }
    	if (value != null && clazz.isAssignableFrom(value.getClass())) return value;
    	return null;
    }
    
    /**  
        Convert an Object to another type "clazz".
        @return converted object of type clazz or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static Object convert(Class clazz, Object value) {
        return convert(clazz, value, null);
    }
    
    /**  
        Convert a double to another type "clazz".
        @return converted object of type clazz or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static Object convert(Class clazz, double value, String fmt) {
        return convert(clazz, new Double(value), fmt);
    }
    /**  
        Convert a double to another type "clazz".
        @return converted object of type clazz or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static Object convert(Class clazz, double value) {
        return convert(clazz, new Double(value), null);
    }
    /**  
        Convert a float to another type "clazz".
        @return converted object of type clazz or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static Object convert(Class clazz, float value, String fmt) {
        return convert(clazz, new Float(value), fmt);
    }
    /**  
        Convert an float to another type "clazz".
        @return converted object of type clazz or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static Object convert(Class clazz, float value) {
        return convert(clazz, new Float(value), null);
    }
    /**  
        Convert a long to another type "clazz".
        @return converted object of type clazz or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static Object convert(Class clazz, long value, String fmt) {
        return convert(clazz, new Long(value), fmt);
    }
    /**  
        Convert a long to another type "clazz".
        @return converted object of type clazz or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static Object convert(Class clazz, long value) {
        return convert(clazz, new Long(value), null);
    }
    /**  
        Convert an int to another type "clazz".
        @return converted object of type clazz or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static Object convert(Class clazz, int value, String fmt) {
        return convert(clazz, new Integer(value), fmt);
    }
    /**  
        Convert an int to another type "clazz".
        @return converted object of type clazz or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static Object convert(Class clazz, int value) {
        return convert(clazz, new Integer(value), null);
    }
    /**  
        Convert a short to another type "clazz".
        @return converted object of type clazz or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static Object convert(Class clazz, short value, String fmt) {
        return convert(clazz, new Short(value), fmt);
    }
    /**  
        Convert an short to another type "clazz".
        @return converted object of type clazz or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static Object convert(Class clazz, short value) {
        return convert(clazz, new Short(value), null);
    }
    /**  
        Convert a char to another type "clazz".
        @return converted object of type clazz or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static Object convert(Class clazz, char value, String fmt) {
        return convert(clazz, new Character(value), fmt);
    }
    /**  
        Convert a char to another type "clazz".
        @return converted object of type clazz or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static Object convert(Class clazz, char value) {
        return convert(clazz, new Character(value), null);
    }

    /**  
        Convert a byte to another type "clazz".
        @return converted object of type clazz or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static Object convert(Class clazz, byte value, String fmt) {
        return convert(clazz, new Byte(value), fmt);
    }
    /**  
        Convert a byte to another type "clazz".
        @return converted object of type clazz or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static Object convert(Class clazz, byte value) {
        return convert(clazz, new Byte(value), null);
    }

    /**  
        Convert a boolean to another type "clazz".
        @return converted object of type clazz or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static Object convert(Class clazz, boolean value, String fmt) {
        return convert(clazz, new Boolean(value), fmt);
    }
    /**  
        Convert a boolean to another type "clazz".
        @return converted object of type clazz or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static Object convert(Class clazz, boolean value) {
        return convert(clazz, new Boolean(value), null);
    }

    /**  
        Convert an Object to a double.
        @return value converted into a double
        @see #convert(Class,Object,String)
        @throws IllegalArgumentException if number is not a valid number
    */
    public static double toDouble(Object value) {
        Number num = (Number) convert(double.class, value);
        if (num == null) throw new IllegalArgumentException("OAConverter.toDouble(): "+value+" cant be converted to double");
        return num.doubleValue();
    }

    /**  
	    Convert an Object to a BigDecimal.
	    @return value converted into a double
	    @see #convert(Class,Object,String)
	    @throws IllegalArgumentException if number is not a valid number
	*/
	public static BigDecimal toBigDecimal(Object value) {
		BigDecimal num = (BigDecimal) convert(BigDecimal.class, value);
	    if (num == null) throw new IllegalArgumentException("OAConverter.toBigDecimal(): "+value+" cant be converted to double");
	    return num;
	}
    
    /**  
        Convert an Object to a float.
        @return value converted into a float
        @see #convert(Class,Object,String)
        @throws IllegalArgumentException if number is not a valid number
    */
    public static float toFloat(Object value) {
        Number num = (Number) convert(float.class, value);
        if (num == null) throw new IllegalArgumentException("OAConverter.toFloat(): "+value+" cant be converted to float");
        return num.floatValue();
    }
    /**  
        Convert an Object to a long.
        @return value converted into a long
        @see #convert(Class,Object,String)
        @throws IllegalArgumentException if number is not a valid number
    */
    public static long toLong(Object value) {
        if (value == null) return 0L;
        Number num = (Number) convert(long.class, value);
        if (num == null) throw new IllegalArgumentException("OAConverter.toLong():"+value+" cant be converted to long");
        return num.longValue();
    }
    /**  
        Convert an Object to an int.
        @return value converted into an int
        @see #convert(Class,Object,String)
        @throws IllegalArgumentException if number is not a valid number
    */
    public static int toInt(Object value) {
        if (value == null) return (int) 0;
        Number num = (Number) convert(int.class, value);
        if (num == null) throw new IllegalArgumentException("OAConverter.toInt(): string "+value+" cant be converted to int");
        return num.intValue();
    }
    /**  
        Convert an Object to a short.
        @return value converted into a short
        @see #convert(Class,Object,String)
        @throws IllegalArgumentException if number is not a valid number
    */
    public static short toShort(Object value) {
        if (value == null) return (short) 0;
        Number num = (Number) convert(short.class, value);
        if (num == null) throw new IllegalArgumentException("OAConverter.toShort(): "+value+" cant be converted to short");
        return num.shortValue();
    }
    /**  
        Convert an Object to a char.
        @return value converted into a char
        @see #convert(Class,Object,String)
        @throws IllegalArgumentException if number is not a valid number
    */
    public static char toChar(Object value) {
        if (value == null) return (char) 0;
        Character c = (Character) convert(char.class, value);
        if (c== null) throw new IllegalArgumentException("OAConverter.toChar(): "+value+" cant be converted to char");
        return c.charValue();
    }
    /**  
        Convert an Object to a byte.
        @return value converted into a byte
        @see #convert(Class,Object,String)
        @throws IllegalArgumentException if number is not a valid number
    */
    public static byte toByte(Object value) {
        if (value == null) return (byte) 0;
        Byte c = (Byte) convert(byte.class, value);
        if (c== null) throw new IllegalArgumentException("OAConverter.toByte(): "+value+" cant be converted to byte");
        return c.byteValue();
    }
    /**  
        Convert an Object to a boolean.
        @return value converted into a boolean
        @see #convert(Class,Object,String)
        @throws IllegalArgumentException if number is not a valid number
    */
    public static boolean toBoolean(Object value){
        if (value == null) return false;
        Boolean b = (Boolean) convert(boolean.class, value);
        if (b== null) throw new IllegalArgumentException("OAConverter.toBoolean(): "+value+" cant be converted to boolean");
        return b.booleanValue();
    }


    /**  
        Convert a String to an OADateTime.
        @return OADateTime or null if conversion could not be completed.
        @see #convert(Class,Object,String)
        @see OADateTime
    */
    public static OADateTime toDateTime(String value, String fmt) {
        return (OADateTime) convert(OADateTime.class, value, fmt);
    }
    /**  
        Convert a String to an OADateTime.
        @return OADateTime or null if conversion could not be completed.
        @see #convert(Class,Object,String)
    */
    public static OADateTime toDateTime(String value) {
        return (OADateTime) convert(OADateTime.class, value, null);
    }
    /**  
        Convert a String to an OADate.
        @return OADate or null if conversion could not be completed.
        @see #convert(Class,Object,String)
        @see OADateTime
    */
    public static OADate toDate(Object value, String fmt) {
        return (OADate) convert(OADate.class, value, fmt);
    }
    /**  
        Convert a String to an OADate.
        @return OADate or null if conversion could not be completed.
        @see #convert(Class,Object,String)
        @see OADateTime
    */
    public static OADate toDate(Object value) {
        return (OADate) convert(OADate.class, value);
    }
    /**  
        Convert a String to an OATime.
        @return OATime or null if conversion could not be completed.
        @see #convert(Class,Object,String)
        @see OADateTime
    */
    public static OATime toTime(Object value, String fmt) {
        return (OATime) convert(OATime.class, value, fmt);
    }
    /**  
        Convert a String to an OATime.
        @return OATime or null if conversion could not be completed.
        @see #convert(Class,Object,String)
        @see OADateTime
    */
    public static OATime toTime(Object value) {
        return (OATime) convert(OATime.class, value);
    }



    /** 
        Convert an Object to a String, without formatting. 
        @return if obj is null then a blank string is returned, otherwise String version of obj.
        @see #convert(Class,Object,String)
    */
    public static String toString(Object obj) {
        return toString(obj, null);
    }
    public static String toString(Object obj, boolean bUseDefaultFormat) {
        String fmt = null;
        if (bUseDefaultFormat && obj != null) {
            fmt = getFormat(obj.getClass());
        }
        return toString(obj, fmt);
    }
    /** 
        Convert an Object to a String, using a specific format. 
        For formatters, see below.
        @return if obj is null then a blank string is returned, otherwise String version of obj.
        @see #convert(Class,Object,String)
        @see OAString#format(String,String)
    */
    public static String toString(Object obj, String fmt) {
        return (String) convert(String.class, obj, fmt);
    }

    /** 
        Convert a double to a String, using a specific format. 
        For formatters, see below.
        @return String version of a double
        @see OAConverterNumber
        @see #convert(Class,Object,String)
        @see OAString#format(String,String)
    */
    public static String toString(double d, String fmt) {
        return (String) convert(String.class, d, fmt);
    }
    public static String toString(double d, boolean bUseDefaultFormat) {
        String fmt = null;
        if (bUseDefaultFormat) {
            fmt = getDecimalFormat();
        }
        return toString(d, fmt);
    }

    /** 
        Convert a double to a String 
        @return String version of a double
        @see OAConverterNumber
        @see #convert(Class,Object,String)
    */
    public static String toString(double d) {
        return (String) convert(String.class, d, null);
    }
    /** 
        Convert a long to a String, using a specific format. 
        @return String version of a long
        @see OAConverterNumber
        @see #convert(Class,Object,String)
        @see OAString#format(String,String)
    */
    public static String toString(long l, String fmt) {
        return (String) convert(String.class, l, fmt);
    } 
    /** 
        Convert a long to a String. 
        @return String version of a long
        @see OAConverterNumber
        @see #convert(Class,Object,String)
    */
    public static String toString(long l) {
        return (String) convert(String.class, l, null);
    }
    public static String toString(long l, boolean bUseDefaultFormat) {
        String fmt = null;
        if (bUseDefaultFormat) {
            fmt = getIntegerFormat();
        }
        return toString(l, fmt);
    }

    
    /** 
        Convert a character to a String.
        @return String version of a character
        @see OAConverterCharacter
    */
    public static String toString(char c) {
        return (String) convert(String.class, c, null);
    }
    public static String toString(char c, boolean bUseDefaultFormat) {
        return toString(c);
    }

    /**
        Convert boolean value to a String.
        @param fmt format string to determine values for true, false, null.  Ex: "true;false;null", "yes;no;maybe"
        @return String version of a boolean
        @see OAConverterBoolean
        @see #convert(Class,Object,String)
        @see OAString#format(String,String)
    */
    public static String toString(boolean b, String fmt) {
        return (String) convert(String.class, b, fmt);
    } 
    public static String toString(boolean b, boolean bUseDefaultFormat) {
        String fmt = null;
        if (bUseDefaultFormat) {
            fmt = getBooleanFormat();
        }
        return toString(b, fmt);
    }
    /**
        Convert boolean value to a String.
        @param b boolean value, can be null if used as "(Boolean) null"
        @param fmt format string to determine values for true, false, null.  Ex: "true;false;null", "yes;no;maybe"
        @return String version of a boolean
        @see OAConverterBoolean
        @see #convert(Class,Object,String)
        @see OAString#format(String,String)
    */
    public static String toString(Boolean b, String fmt) {
        return (String) convert(String.class, b, fmt);
    } 
    /**
        Convert boolean value to a String, using Boolean(b).toString()
        @see toString(boolean,String)
        @see toString(Boolean,String)
        @see #convert(Class,Object,String)
    */
    public static String toString(boolean b) {
        return (String) convert(String.class, b, null);
    }


    
    public static void main(String[] args) {
        // -4.1 => -5.0
        double d = -4.5;

d = OAConv.divide(1.0, 3.0, 4);


        System.out.println("Math.round "+d+" == "+Math.round(d));

        double d2 = round(d, 0, 0, BigDecimal.ROUND_HALF_UP);
        System.out.println("BigDecimal "+d+" == "+d2);
        
        d = OAConverter.round(d, 0);
        System.out.println("new round "+d+" == "+d);
    }

}

