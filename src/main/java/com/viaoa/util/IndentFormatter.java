/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
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

import java.util.logging.*;

/*
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

