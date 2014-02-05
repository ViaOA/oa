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

import java.util.logging.*;

/**
 * This works with Logger.entering() and Logger.exiting() to create an indentation output.
   Note: you can use the following instead of entering() or exiting() methods
  		LOG.finest("ENTRY");  // or ">", "START", "BEGIN"
        LOG.finest("RETURN"); // or "<", "END"
 */
public class IndentFormatter extends SimpleFormatter {
	int indent = 0;
	String strIndent = "";
    public static final String NL = System.getProperty("line.separator");     
	public String format(LogRecord record) {
		String s = record.getMessage();
		if (s == null) s = "";
		boolean bEntry = false;
		if (s.equals("ENTRY") || s.equals(">") || s.equals("BEGIN")) {
			bEntry = true;
			s = "+";
		}
		boolean bExit = false;
		if (s.equals("RETURN") || s.equals("<") || s.equals("END")) {
			bExit = true;
			s = "+";
			indent--;
			if (indent < 0) indent = 0;
			strIndent = "";
			for (int i=0; i<indent;i++) strIndent += "|  ";
		}

		s = strIndent + s;
		
		boolean b = false;
		if (bEntry || bExit) {
			if (record.getSourceClassName() != null) {
				b = true;
			    String s2 = record.getSourceClassName();
			    int pos = s2.lastIndexOf('.');
			    if (pos > 0 && pos < s2.length()) s2 = s2.substring(pos+1);
			    s += s2;
			}
			if (record.getSourceMethodName() != null) {
				if (b) s += ".";
			    s += (record.getSourceMethodName());
			}
		}
		if (record.getThrown() != null) {
			s += ("  EXCEPTION: " + record.getThrown());
		}
		
		if (record.getLevel().intValue() > Level.INFO.intValue()) s += (" *** >>>>" + record.getLevel().getName()).toUpperCase() + "<<<< ***";
		
		s += NL;

		if (bEntry && indent < 10) {
			indent++;
			strIndent = "";
			for (int i=0; i<indent;i++) strIndent += "|  ";
		}
		
		return s;
	}
}

