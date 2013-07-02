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
package com.viaoa.ds.jdbc.delegate;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.*;

import com.viaoa.ds.jdbc.db.*;
import com.viaoa.util.*;


/**
 * Used to convert query data into correct SQL data type.
 * @author vvia
 *
 */
public class ConverterDelegate {

	public static String convert(DBMetaData dbmd, Column column, Object value) {
		Class clazz = null;

		if (column == null) {
		    if (value == null) return "NULL";
		    return value.toString();
		}
		
		switch (column.type) {
		case Types.CLOB:
		case Types.LONGVARCHAR:
		case Types.VARCHAR:
			clazz = String.class;
            if (dbmd.blanksAsNulls && ((value instanceof String) && ((String)value).length() == 0) ) value = null;
			break;
		
		case Types.BIGINT: 
		case Types.DECIMAL:
		case Types.FLOAT:
		case Types.INTEGER:
		case Types.NUMERIC:
		case Types.REAL:
		case Types.SMALLINT:
		case Types.DOUBLE:
		case Types.TINYINT: 
			clazz = Number.class;
			break;

		case Types.CHAR:
			clazz = Character.class;
			break;
			
		case Types.BIT: 
		case Types.BOOLEAN:
			clazz = Boolean.class;
			break;

		case Types.DATE:
			clazz = OADate.class;
			break;
		case Types.TIME:
			clazz = OATime.class;
			break;
		case Types.TIMESTAMP:
			clazz = OADateTime.class;
			if (dbmd.databaseType == dbmd.SQLSERVER) clazz = Number.class;
			break;
			
		case Types.LONGVARBINARY:
		case Types.VARBINARY:
			// todo: qqq these are for byte[]
			// throw new RuntimeException("SQL Type not mapped to a class");
		default:
			// throw new RuntimeException("SQL Type not known");
		}

		if (clazz != null && value != null) {
		    value = OAConverter.convert(clazz, value);
		}
		
		return convertToString(dbmd, value, true, column.maxLength, column.decimalPlaces);
	}
	
	public static boolean areSingleQuotesNeeded(Column column) {
        switch (column.type) {
        case Types.CLOB:
        case Types.LONGVARCHAR:
        case Types.VARCHAR:
            return true;
        }
        return false;
	}
	
    protected static String convertToString(DBMetaData dbmd, Object obj, boolean bConvertSingleQuotes, int maxLength, int decimalPlaces) {
        if (obj == null) return "NULL";
        Class c = obj.getClass();

        if ( c.equals(Boolean.class)) {
            boolean b = ((Boolean) obj).booleanValue();
            if (dbmd.objectTrue != null) {
                obj = b ? dbmd.objectTrue : dbmd.objectFalse;
            }
            else {
                return (b ? "1" : "0");
            }
            return obj.toString();
        }

        if (Number.class.isAssignableFrom(c) ) {
            String fmt = null;
            if (decimalPlaces > 0) {
                if (obj instanceof BigDecimal) {
                    if (decimalPlaces <= 0 || ((BigDecimal)obj).scale() <= decimalPlaces) {
                        return ((BigDecimal)obj).toPlainString();
                    }
                }
                if (OAReflect.isFloat(c)) {
                    fmt = ".0";
                    for (int i=1; i<decimalPlaces; i++) fmt += "0";
                }
            }
            return OAConverter.toString(obj, fmt);
        }


        if (c.equals(OADate.class)) {
            String s = ((OADate) obj).toString("yyyy-MM-dd");
            if (dbmd.databaseType == dbmd.ACCESS) return "#" + s + "#";
            return "{d '" + s + "'}";
        }
        
        if (c.equals(OATime.class)) {
        	OATime time = (OATime) obj;
            String s = OAString.fmt(""+time.getHour(),"2R00") + ":" + OAString.fmt(""+time.getMinute(),"2R00") + ":" + OAString.fmt(""+time.getSecond(),"2R00");
            if (dbmd.databaseType == dbmd.ACCESS) return "#" + s + "#";
            return "{t '" + s + "'}";
        }

        if (c.equals(OADateTime.class)) {
            String s = ((OADateTime) obj).toString("yyyy-MM-dd HH:mm:ss");
            if (dbmd.databaseType == dbmd.ACCESS) return "#" + s + "#";
            return "{ts '" + s + "'}";
        }

        
        String s = OAConverter.toString(obj);
        
        if (maxLength > 0 && s.length() > maxLength) s = s.substring(0,maxLength);

        if (bConvertSingleQuotes) {
        	s = convertSingleQuotes(dbmd, s);
            s =  "'" + s + "'";
        }
        return s;
    }

    protected static String convertSingleQuotes(DBMetaData dbmd, String value) {
    	if (value == null) return null;
        // convert all ' to ''
        if (value.indexOf('\'') >= 0 || value.indexOf('\\') >= 0) {
            StringTokenizer st = new StringTokenizer(value, "'\\", true);
            StringBuffer newValue = new StringBuffer(value.length()+16);
            while ( st.hasMoreTokens() ) {
                String s = st.nextToken();
                if (s.charAt(0) == '\'') {
                    if (dbmd.useBackslashForEscape) newValue.append("\\'");
                    else newValue.append("''");
                }
                else if (s.charAt(0) == '\\' && dbmd.useBackslashForEscape) {
                	newValue.append("\\");
                }
                else newValue.append(s);
            }
            value = new String(newValue);
        }
        return value;
    }
}


