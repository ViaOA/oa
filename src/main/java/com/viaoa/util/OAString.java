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

import java.util.*;
import java.io.*;
import java.lang.reflect.Array;
import java.awt.*;

/**
    String <i>helper/utility</i> Class.
    @see OAConverter
*/
public class OAString {
    public static final String NL = System.getProperty("line.separator");     
    public static final String FS = File.separator;     
	
    /** 
        String trim method.<br>
        1: removes leading spaces<br>
        2: removes extra spaces within. (Note: even if enclosed in quotes)<br>
        3: removes trailing spaces <br>
        <p>
        <pre>
        Example:  "  this    is   a  test  "  will be: "this is a test"
        </pre>
    */
    public static String trim(String line) {
        if (line == null) return line;
        StringTokenizer st = new StringTokenizer(line," ",false);
        StringBuilder sb = null;
        for ( ;st.hasMoreElements(); ) {
            String word = st.nextToken();
            if (sb == null) sb = new StringBuilder(line.length());
            else sb.append(' ');
            sb.append(word);
        }
        if (sb == null) return line;
        return new String(sb);
    }

    /**
     * get last N chars from string.
     * @param len number of chars to get
     */
    public static String getEnd(String text, int len) {
       if (text == null) return null;
       int x = text.length();
       if (x <= len) return text;
       String s = text.substring(x - len);
       return s;
    }
    public static String getLast(String text, int len) {
        return getEnd(text, len);
    }
    
    /**
     * get first N chars from string.
     * @param len number of chars to get
     */
    public static String getBegin(String text, int len) {
       if (text == null) return null;
       int x = text.length();
       if (x <= len) return text;
       String s = text.substring(0, len);
       return s;
    }
    public static String getFirst(String text, int len) {
        return getBegin(text, len);
    }

    /** 
        converts null to "" and does other xml/html conversions for &lt;, &gt; &amp; &quot; &#39; 
        @see #convertToXML(String,boolean)
    */
    public static String convertToXml(String value) {
        return convertToXML(value, false, true);
    }
    public static String convertToHtml(String value) {
        return convertToXML(value, false, true);
    }

    /**
        converts null to "" and does other xml/html conversions for &lt;, &gt; &amp; &quot; &#39; 
      <pre>
        see: http://www.w3.org/TR/REC-xml#NT-Char

        Legal Chars ::=   #x9 | #xA | #xD | [#x20-#xD7FF] |
                  [#xE000-#xFFFD] | [#x10000-#x10FFFF]

        9, 10, 13
        tab, lf, cr
      </pre>
        @see #convertToXML(String,boolean)
    */
    public static String convertToXML(String value) {
        return convertToXML(value, false, false);
    }

    /**
        Encode/Replace chars that are illegal for XML/HTML with &amp; codes.
        @see #convertToXML(String,boolean)
    */
    public static String encodeIllegalXML(String value) {
        return convertToXML(value, true, false);
    }


    /**
        Encode illegal XML characters with &lt;OAXML#999/&gt; where 999 is character integer value.<br>
        decodeIllegalXML() is used to convert back to character.
        <p>
        This is used internally by convertToXML
        @param ch is character to encode
        @param bConvertLTGT if true then convert &lt; to &amp;lt; and &gt; to &amp;gt;.  This is needed when it
        is not going to be used in a XML CDATA block.
        
        @see #decodeIllegalXML
        @see #convertToXML(String,boolean)
    */
    protected static String encodeIllegalXML(char ch, boolean bConvertLTGT) {
        if (bConvertLTGT) return "&lt;OAXML#"+((int)ch)+"/&gt;";
        return "<OAXML#"+((int)ch)+"/>";  // used in [CDATA]
    }


    /**
        Convert a String to a valid XML String, using special coding for illegal characters. <br>
        Converts &amp; to &amp;amp;, &quot; to &amp;quot, &#39; to &amp;apos, &lt; to &amp;&lt;, &gt; to &amp;gt;<br>
        For characters less then 32, it calls encodeIllegalXML().
        <p>
        Note:  Some characters are illegal even within CDATA blocks.
        <p>
        NOTE: decodeIllegalXML() should be called to <i>reverse</i> this String, since encodeIllegalXML() uses
        a special tag.
        
        @param value is XML String to convert.
        @param bCData true if this String will be used in an XML CDATA block.
        @return converted string.  If value is null then a blank "" is returned.
        @see #decodeIllegalXML
        @see #encodeIllegalXML
    */
    public static String convertToXML(String value, boolean bCData) {
        return convertToXML(value, bCData, false);
    }
    public static String convertToXML(String value, boolean bCData, boolean bIsHtml) {
        if (value == null) return "";

        int x = value.length();
        StringBuilder sb = new StringBuilder(x);
        for (int i=0; i<x ;i++) {
            char ch = value.charAt(i);

            if (!bCData) {
                switch (ch) {
                    case '&': sb.append("&amp;"); continue;
                    case '"': sb.append("&quot;"); continue;
                    case '\'': sb.append("&apos;"); continue;
                    case '<': sb.append("&lt;"); continue;
                    case '>': sb.append("&gt;"); continue;
                    case '\n':  // 20171021
                        if (bIsHtml) {
                            sb.append("<br>"); 
                            continue;  
                        }
                        break;
                    case '\r': { // 20171021
                        if (bIsHtml) {
                            continue;  
                        }
                    }
                }
            }

            switch (ch) {
                case 9:
                case 10:
                case 13:
                    sb.append(ch);
                    continue;
            }

            if (ch < 32) {  // illegal in XML, create special tag
                sb.append(encodeIllegalXML(ch, !bCData));
            }
            else sb.append(ch);
        }
        return new String(sb);
    }


    /**
        Used to determine if a String has any illegal XML characters in it.
        @return false if any of the following characters are found: &amp; &quot; \ &lt; &gt; LF CR or char&lt;32.  
        If value is null then false is returned.
    */
    public static boolean isLegalXML(String value) {
        if (value == null) return false;
        int x = value.length();
        for (int i=0; i<x ;i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '&':
                case '"':
                case '\'':
                case '<':
                case '>':
                case 10:
                case 13:
                    return false;
                case 9:
                    break;
                default:
                    if (ch < 32) return false;
            }
        }
        return true;
    }

    /**
        Convert XML Strings converted with encodeIllegalXML() back to a String.  Since
        encodeIllegalXML encodes illegal characters with a &lt;OAXML#99/&gt; code, this
        method will convert those tags to the actual character.
        @return string that was decoded. If value is null then null is returned.
        @see #encodeIllegalXML
    */
    public static String decodeIllegalXML(String value) {
        if (value == null) return null;
        int pos = 0;
        for (;;) {
            int apos = value.indexOf("<OAXML#",pos);
            if (apos < 0) break;
            int bpos = value.indexOf("/>", apos+7);
            if (bpos > 0 && bpos > apos + 7) {
                int ch = ' ';
                try {
                    ch = Integer.parseInt(value.substring(apos+7, bpos));
                }
                catch (Exception e) {
                }
                value = value.substring(0, apos) + ((char)ch) + value.substring(bpos + 2);
            }
            pos = apos+1;
        }
        return value;
    }

    /**
        Used to replace one value with another within a String.  
        @param replace the string or null that will replace every occurance of the the character "c"
        @see #convert(String,String,String,boolean) convert()
    */
    public static String convert(String value, char c, String replace) {
        return convert(value, c+"", replace,false);
    }

    /**
        Used to replace one value with another within a String, ignoring case.
        @param replace the string or null that will replace every occurance of the the search string
        @see #convert(String,String,String,boolean) convert()
    */
    public static String convertIgnoreCase(String line, String search, String replace) {
        return convert(line,search,replace,true);
    }

    /**
        Used to replace one value with another within a String.
        @param replace the string or null that will replace every occurance of the the search string
        @see #convert(String,String,String,boolean) convert()
    */
    public static String convert(String line, String search, String replace) {
        return convert(line,search,replace,false);
    }

    /**
     * Remove any and all search characters from a string. 
     * @param line original data
     * @param search characters to remove
     * @return
     */
    public static String removeCharacters(String line, String search) {
        if (line == null || search == null) return line;
        StringBuilder sb = new StringBuilder(line.length());
        int x = line.length();
        for (int i=0; i<x; i++) {
            char ch = line.charAt(i);
            if ( search.indexOf(ch) < 0) sb.append(ch);
        }
        return new String(sb);
    }

    /**
     * Remove any and all characters that are not in string
     * @param line original data
     * param search characters to keep
     */
    public static String removeOtherCharacters(String line, String keep) {
        if (line == null || keep == null) return line;
        StringBuilder sb = new StringBuilder(line.length());
        int x = line.length();
        for (int i=0; i<x; i++) {
            char ch = line.charAt(i);
            if (keep.indexOf(ch) >= 0) sb.append(ch);
        }
        return new String(sb);
    }

    /**
     * Remove any and all characters that are not digits
     * @param line original data
     */
    public static String removeNonDigits(String line) {
        return removeNonDigits(line, false);
    }    
    public static String removeNonDigits(String line, boolean bAllowDot) {
        if (line == null) return line;
        StringBuilder sb = new StringBuilder(line.length());
        int x = line.length();
        for (int i=0; i<x; i++) {
            char ch = line.charAt(i);
            if (Character.isDigit(ch) || (bAllowDot && ch == '.')) sb.append(ch);
        }
        return new String(sb);
    }

    
    public static final String OtherFileNameChars = "_-. \\/";
    /**
     * Remove any and all characters that are not valid in filename.
     */
    public static String removeNonFileNameChars(String line) {
        if (line == null) return line;
        StringBuilder sb = new StringBuilder(line.length());
        int x = line.length();
        for (int i=0; i<x; i++) {
            char ch = line.charAt(i);
            boolean b = (ch == ':' && i == 1); 
            if (!b) b = (Character.isDigit(ch) || Character.isLetter(ch) || OtherFileNameChars.indexOf(ch) >= 0);
            if (b) sb.append(ch);
        }
        return new String(sb);
    }
    
    
    /**
        Used to replace one value with another within a String.  
        @param line is String that is to be converted.
        @param search is String that is to be replaced.
        @param replace is replacement value to use.  If null, then a blank String will be used.
        @param bIgnoreCase if true, then search is not case sensitive.
        @return new String where search String is replaced with replace String.  If line is null then null is
        returned.  If search is null then line is returned.
    */
    public static String convert(String line, String search, String replace, boolean bIgnoreCase) {
        return convert(line, search, replace, bIgnoreCase, false, 0, -1);
    }
    public static String convert(String line, String search, String replace, boolean bIgnoreCase, boolean bFirstOnly, int startPos, int endPos) {
        if (line == null || search == null || search.length() == 0) return line;
        if (replace == null) replace = "";

        int xs = search.length();
        if (xs == 0) return line;
        if (bIgnoreCase) search = search.toLowerCase();

        int xr = replace.length();
        int xl = line.length();
        
        StringBuilder sb = null;  // dont allocate until first match is found
        char c=0, origChar=0;
        for (int i=startPos,j=0; ;i++) {

            if (i < xl && (endPos<0 || i < endPos)) {
                origChar = c = line.charAt(i);
                if (bIgnoreCase) c = Character.toLowerCase(c);
                if (c == search.charAt(j)) {
                    j++;
                    if (j == xs) {
                        if (sb == null) {
                            sb = new StringBuilder(xl + (xl/10));
                            int e = (i - j) + 1;
                            if (e > 0) sb.append(line.substring(0,e));
                        }

                        if (xr > 0) {
                            sb.append(replace);
                            if (bFirstOnly) break;
                        }
                        
                        j = 0;
                    }
                    continue;
                }
            }
            if (j > 0) {
                if (sb != null) {
                    // go back to previously matched chars
                    int b = i-j;
                    sb.append(line.substring(b,b+j));
                }
                j = 0;
            }
            if (i >= xl || (endPos>=0 && i >= endPos)) break;
            if (sb != null) sb.append(origChar);
        }
        if (sb == null) return line;
        return new String(sb);
    }

    /**
     * Removes the package name
     */
    public static String getClassName(Class c) {
        if (c == null) return null;
        return c.getSimpleName();
        /*
        String s = c.getName();
        int x = s.lastIndexOf('.');
        if (x > 0) s = s.substring(x+1);
        return s;
        */
    }
    /**
     * gets package name
     */
    public static String getPackageName(Class c) {
        if (c == null) return null;
        String s = c.getName();
        int x = s.lastIndexOf('.');
        if (x > 0) s = s.substring(0, x);
        return s;
    }

    /** 
        Used to convert a String that uses Hungarian notation to a titled, space separated String.
        <p>
        Example: "yourNameTest" converts to "Your Name Test"  
        @param value String to convert
        @return new String that is titled case, with spaces to seperate words.  If value is null, then 
        a blank "" is returned.
    */
    public static String convertHungarian(String value) {
        return getDisplayName(value);
    }


    private final static String validToHungarianSep = " _,.:|\t-/";
    /** 
        Example: "Your Name Test" converts to "YourNameTest"   
        Example: "your name test" converts to "yourNameTest"   
        Example: "Your_name_test" converts to "YourNameTest"
        Example: "your.name.test" converts to "yourNameTest"   
        
        first char upper/lower-case is not changed.
    */
    public static String convertToHungarian(String value) {
        return convertToHungarian(value, null);
    }
    public static String convertToHungarian(String value, String sepChars) {
        if (value == null) return null;
        if (sepChars == null) sepChars = validToHungarianSep;
        int x = value.length();
        StringBuilder sb = new StringBuilder(x);

        char chSep = 0; 
        char chLast = 0;
        for (int i=0; i<x; i++) {
            char ch = value.charAt(i);
            if (sepChars.indexOf(ch) >= 0) {
                chSep = ch;
                continue;
            }
            if (chSep > 0) {
                if (Character.isDigit(ch)) {
                    if (chLast > 0 && Character.isDigit(chLast)) {
                        if (chSep == ' ') chSep = '_';
                        sb.append(chSep);
                    }
                }
                else ch = Character.toUpperCase(ch);
                chSep = 0;
            }
            sb.append(ch);
            chLast = ch;
        }
        return sb.toString();
    }
    
    /**
        Used to convert a String that uses Hungarian notation to a titled, space separated String.
        The first char and all letter chars following non-letter characters will be converted to
        uppercase.  Words will be seperated using space character.
        <p>
        Example: "yourNameTest" converts to "Your Name Test"  <br>
        Example: "USAmerica" converts to "US America"  <br>
        Example: "v.via" converts to "V.Via"  
        @param value String to convert
        @return new String that is titled case, with spaces to seperate words.  If value is null, then 
        a blank "" is returned.
    */
    public static String getDisplayName(String value) {
        if (value == null) return "";
        int x = value.length();

        StringBuilder sb = new StringBuilder(x+3);
        
        char c;
        char cLast = 0;
        char cNext = 0;
        
        for (int i=0; i<x; i++) {
            c = (cNext>0) ? cNext : value.charAt(i);
            if (i+1 < x) cNext = value.charAt(i+1);
            else cNext = 0;
            
            if (i == 0) {
                if (Character.isLowerCase(c)) c = Character.toUpperCase(c);
            }
            else if (c == '_') c = ' ';
            else if (cLast == '_') {
                if (Character.isLowerCase(c)) c = Character.toUpperCase(c);
            }
            else if (Character.isUpperCase(c)) {
                if (!Character.isUpperCase(cLast)) sb.append(" ");
                else {
                    if (cNext > 0 && Character.isLowerCase(cNext)) sb.append(" ");
                }
            }
            sb.append(c);
            cLast = c;
        }
        return new String(sb);
    }
    public static String createDisplayName(String value) {
        return getDisplayName(value);
    }
    public static String convertToDisplayName(String value) {
        return getDisplayName(value);
    }

    /**
        Converts a String that is plural to singular.<br>
        Converts end characters: "hes" to "h", "ses" to "s", "zzes" to "zz", "ies" to "y", "s" to "".
        This is the reverse method of makePlural.
        
        @return new String.  If s is null, then a blank "" is returned.
        @see #makePlural
    */
    public static String makeSingular(String str) {
        if (str == null) return "";
        int x = str.length();
        if (x < 2) return str;
        boolean bUpper = Character.isUpperCase(str.charAt(x - 1));
        
        String test = str.toUpperCase();
        if (test.charAt(x-1) != 'S') return str;
        char ch = test.charAt(x-2);
        if (ch == 'A' || ch == 'I' || ch == 'O' || ch == 'U' || ch == 'Y' || ch == 'S') return str;
        if (test.endsWith("HES")) return str.substring(0, x-2);
        if (test.endsWith("SES")) return str.substring(0, x-2);
        if (test.endsWith("ZZES")) return str.substring(0, x-2);
        if (test.endsWith("IES")) return str.substring(0, x-3) + (bUpper?"Y":"y");
        if (test.endsWith("XES")) return str.substring(0, x-2);
        return str.substring(0, x-1);
    }

    /**
        Converts a String to plural.
        @see #makePlural
    */
    public static String getPlural(String s) {
        return makePlural(s);
    }

    public static String getAorAn(String s) {
        if (s == null || s.length() == 0) return "a";
        char ch = Character.toLowerCase(s.charAt(0));
        if ("aeiou".indexOf(ch) < 0) return "a";
        return "an";
    }
    
    
    
    /**
        Converts a String to plural.
        <ul>
        <li>If str ends in "es" then no change is made.
        <li>If str ends in "s" then add "es".
        <li>If str ends in "zz" then add "s".
        <li>If str ends is an "h", "z", "x" then add "es".
        <li>If str ends in a vowel + "y", then add "s".
        <li>If str ends in a nonvowel + "y", then convert "y" to "ies".
        <li>All others have an "s" added.
        </ul>
        <p>
        Note: case will be matched, whatever characters are appended will match the case of the String.
        This is the reverse method of makeSingular.
        
        @return new plural String.  If s is null, then a blank "" is returned.
        @see #makeSingular
    */
    public static String makePlural(String str) {
        if (str == null) return "";
        int x = str.length();
        if (x == 0) return str;
        char ch = str.charAt(x - 1);
        boolean bUpper = Character.isUpperCase(ch);
        ch = Character.toUpperCase(ch);
        char ch2 = 0;
        if (x > 1) ch2 = Character.toUpperCase(str.charAt(x - 2));
        
        if (ch == 'S') {
            if (ch2 == 'E') return str;
            return str + (bUpper?"ES":"es");
        }
        
        if (ch == 'Z' && ch2 == 'Z') {
            return str + (bUpper?"S":"s"); 
        }
        
        if (ch2 == 'T' && ch == 'H') return str + 's';
        if (ch == 'H' || ch == 'Z' || ch == 'X') return str + (bUpper?"ES":"es"); 
        
        if (ch == 'Y') {
            if (ch2 == 'A' || ch2 == 'E' || ch2 == 'I' || ch2 == 'O' || ch2 == 'U') return str + (bUpper?"S":"s"); 
            return str.substring(0,x - 1) + (bUpper?"IES":"ies");
        }
        return str + (bUpper?"S":"s");
    }

    public static String makePossessive(String str) {
        if (str == null) return "";
        int x = str.length();
        if (x == 0) return str;
        char ch = str.charAt(x - 1);
        boolean bUpper = Character.isUpperCase(ch);
        
        if (ch == 'S' || ch == 's') {
            return str+"'";
        }

        return str + "'" + (bUpper?"S":"s");
    }

    /**
        Converts a String to possissive by adding "'s" or "'".
    */
    public static String getPossessive(String str) {
        if (str == null) return "";
        int x = str.length();
        if (x == 0) return str;
        char ch = str.charAt(x - 1);
        
        if (ch == 'S' || ch == 's') return str + "'";
        return str + (Character.isUpperCase(ch) ? "'S" : "'s");
    }




    /** 
        Converts first letter in each word to uppercase. 
        @return new String.
    */
    public static String getTitleCase(String s) {
        return getTitle(s);
    }
    /** 
        Converts first letter in each word to uppercase. 
        @return new String.
    */
    public static String toTitleCase(String s) {
        return getTitle(s);
    }
    /** 
        Converts first letter in each word to uppercase. 
        @return new String.
    */
    public static String titleCase(String s) {
        return getTitle(s);
    }

    public static String mfcl(String s) {
        return makeFirstCharLower(s);
    }
    public static String makeFirstCharLower(String s) {
        if (s == null) return null; 
        int x = s.length();
        if (x > 0) {
            char c = Character.toLowerCase(s.charAt(0));
            if (x == 1) s = ""+c;
            else s = c + s.substring(1);
        }
        return s;
    }

    public static String mfucl(String s) {
        return makeFirstUpperCharsLower(s);
    }
    /**
     * Example:  GSMRServer -&gt; gsmrServer
     */
    public static String makeFirstUpperCharsLower(String s) {
        if (s == null) return null; 
        int x = s.length();
        StringBuilder sb = null;
        for (int i=0; i<x; i++) {
            char ch = s.charAt(i);
            char ch2 = (i+1==x ? 0 : s.charAt(i+1));
            
            if (Character.isUpperCase(ch) && (i == 0 || (ch2==0 || Character.isUpperCase(ch2))) ) {
                if (sb == null) sb = new StringBuilder(x);
                sb.append(Character.toLowerCase(ch));
            }
            else {
                if (sb != null) {
                    sb.append(s.substring(i));
                }
                break;
            }
        }
        if (sb != null) return new String(sb);
        return s;
    }
    
    public static String mfcu(String s) {
        return makeFirstCharUpper(s);
    }
    public static String makeFirstCharUpper(String s) {
        if (s == null) return null; 
        int x = s.length();
        if (x > 0) {
            char c = Character.toUpperCase(s.charAt(0));
            if (x == 1) s = ""+c;
            else s = c + s.substring(1);
        }
        return s;
    }
    
    /** 
        Converts first letter in each word to uppercase. 
        @return new String.
    */
    public static String getTitle(String s) {
        if (s == null) return "";

        String s2 = s.toUpperCase();
        boolean bAllUpper = s2.equals(s);

        int x = s.length();
        if (x == 0) return s;
        boolean b = true;
        String newValue = "";
        for (int i=0; i<x; i++) {
            char ch = s.charAt(i);
            if (Character.isLetter(ch)) {
                if (b) {
                    ch = Character.toUpperCase(ch);
                    b = false;
                }
                else {
                    if (bAllUpper) ch = Character.toLowerCase(ch);
                }
            }
            else b = true;
            newValue += ch;
        }
        return newValue;
    }
    
    
    public static String mfcu(String s, String basedOn) {
        return getTitle(s, basedOn);
    }
    
    /**
     * @param basedOn is another name that this one should use to figure out which letters to capitalize.
     * ex: gsmrServer, GSMRServer   =&gt; GSMRServer
     */
    public static String getTitle(String s, String basedOn) {
        if (s == null) return "";

        String s2 = s.toUpperCase();
        boolean bAllUpper = s2.equals(s);

        int x = s.length();
        if (x == 0) return s;
        boolean bConvert = true;
        String newValue = "";
        int cnt = 0;
        for (int i=0; i<x; i++) {
            char ch = s.charAt(i);
            if (Character.isLetter(ch)) {
                if (bConvert) {
                    char chHold = ch;
                    ch = Character.toUpperCase(ch);
                    cnt++;
                    if (basedOn != null && i < basedOn.length()) {
                        char ch2 = basedOn.charAt(i);
                        if (ch != ch2) {
                            bConvert = false;
                            if (cnt > 1) ch = chHold;
                        }
                    }
                    else bConvert = false;
                }
                else {
                    if (bAllUpper) ch = Character.toLowerCase(ch);
                }
            }
            else bConvert = true;
            newValue += ch;
        }
        return newValue;
    }

    /** 
        Used to retrieve a portion of a String based on a separator value.
        @see #field(String,String,int,int)
    */
    public static String field(String str, char sep, int beg) {
        return field(str,sep+"",beg,1);
    }
    /** 
        Used to retrieve a portion of a String based on a separator value.
        @see #field(String,String,int,int)
    */
    public static String field(String str, char sep, int beg, int amt) {
        return field(str,sep+"",beg,amt);
    }
    /** 
        Used to retrieve a portion of a String based on a separator value.
        @see #field(String,String,int,int)
    */
    public static String field(String str, String sep, int beg) {
        return field(str,sep,beg,1);
    }
    /** 
        Used to retrieve a portion of a String based on a separator value.
        @param str String to parse
        @param sep seperator wihin str
        @param beg field to find, where first field is <b>1</b>
        @param amt number of fields to return, -1 for all after the beg
        @return string value of field if begin position exists, else null if not found
     */
    public static String field(String str, String sep, int beg, int amt) {
        if (str == null) return null;
        if (sep == null || sep.length() == 0) {
        	if (beg == 1) return str;
        	return null;
        }
        if (beg < 1 || amt == 0) return null;

        int pos = 0;
        int beginPos=-1, endPos=str.length();
        if (beg == 1) beginPos = 0;

        for (int i=2;;i++) {
            pos = str.indexOf(sep,pos);
            if (pos < 0) break;
            if (i == beg) {
                beginPos = pos + sep.length();
                endPos = str.length();
            }
            if (beginPos >= 0) {
                if (amt == -1) break;
                if (i == beg + amt) {
                    endPos = pos;
                    break;
                }
            }
            pos += sep.length();
        }
        if (beginPos < 0) return null;
        if (beginPos >= endPos) return "";
        return str.substring(beginPos, endPos);
    }

    /**
        Used to get a count of the number of values between a separator/delimiter.
        <p>
        Note: even if there is not a value between consective separators, it is still
        counted as another value - in this case a blank.
        
        @param str is String to search.  If null or length = 0, then 0 is returned.
        @param sep separator.
    */
    public static int dcount(String str, String sep) {
        if (str == null || str.length() == 0) return 0;
        return count(str, sep) + 1;
    }
    public static int dcount(String str, char sep) {
        if (str == null || str.length() == 0) return 0;
        return count(str, sep+"") + 1;
    }

    /**
        Returns the amount of particular String within a String.
        @param str is String to search within.
        @param sep is String to search for.
        @return number of occurrences of sep.
    */
    public static int count(String str, String sep) {
        if (str == null || sep == null) return 0;
        int cnt = 0;
        int x = sep.length();
        int pos = 0;
        for (;;cnt++) {
            pos = str.indexOf(sep, pos);
            if (pos < 0) break;
            pos += x;
        }
        return cnt;
    }



    /** 
        Append characters to begin/end of a value.
        @param value is String to append to.  If null, then it will be initialized to an empty string.
        @param amount is number of characters to add.
        @param bAddToEnd if true, then charPad characters are appended to value, else charPad chars are prefixed to value.
        @param padCharacter is the character to use for adding to value.
        @see #align
    */
    public static String pad(String value, int amount, boolean bAddToEnd, char padCharacter) {
        if (value == null) value = "";
        String s = "";
        for (int i=0; i<amount; i++) s += padCharacter;
        if (bAddToEnd) value += s;
        else value = s + value;
        return value;
    }


    /** 
        Aligns a string either left or right to a certain width.
        If length of value is less then width, then characters are added to begin/end of value to make it equal to the width.
        If length of value is greater then the width, then the beginning/ending width amount of characters will be returned.

        @param value is String to alter.  If null, then it will be initialized to an empty string.
        @param width is number of characters for the converted string.  If &lt;= 0, then a blank string is returned.
        @param bAlignLeft if true then value is right aligned, else left aligned.
        @param charPad is the character to use to pad the value, if length of value is less then width.  The 
        value will then have this character either at the beginning or ending, depending on value of bAlignLeft.
        @return new String that is "width" characters in length.
        @see #pad(String,int,boolean,char)
    */
    public static String align(String value, int width, boolean bAlignLeft, char charPad) {
        if (width < 1) return "";
        if (value == null) value = "";
        int w = value.length();
        if (w == width) return value;
        
        if (w < width) return pad(value, width-w, bAlignLeft, charPad);
        
        if (bAlignLeft) return value.substring(0,width);
        return value.substring(w - width);
    }


    /**
        Calls OAConverter to convert/format number.
        see OAConverterNumber
    */
    public static String format(long value, String format) {
        return OAConv.toString(value, format);
    }

    
    /**
        Used to convert an integer to a formatted String, either using OAConverter or OAString.format()
        param vaule integer to format
        @param format if String has an "L", "R", or "C" in it, then OAString.format(String,String) will be used, else
        OAConverter.toString() will be called.
        see OAConverterNumber
        @see #fmt(String,String)
    */
    public static String format(int value, String format) {
        // see which format to use
        String s = format.toUpperCase();
        if (s.indexOf('R') >= 0 || s.indexOf('L') >= 0 || s.indexOf('C') >= 0) {
            return OAString.format(Integer.toString(value), format);
        }
        return OAConv.toString(value, format);
    }

    /**
        Used to convert an double to a formatted String, either using OAConverter or OAString.format()
        param vaule integer to format
        @param format if String has an "L", "R", or "C" in it, then OAString.format(String,String) will be used, else
        OAConverter.toString() will be called, which uses Java formatting.
        see OAConverterNumber
        @see #fmt(String,String)
    */
    public static String format(double value, String format) {
        String s = format.toUpperCase();
        if (s.indexOf('R') >= 0 || s.indexOf('L') >= 0 || s.indexOf('C') >= 0) {
            return OAString.format(Double.toString(value), format);
        }
        return OAConv.toString(value, format);
    }


    /**
        Calls OAConverter to format boolean to a String.
        see OAConverterBoolean
    */
    public static String format(boolean value, String format) {
        return OAConv.toString(value, format);
    }
    /**
        Calls OAConverter to convert and format OADateTime.
        see OAConverterOADateTime
    */
    public static String format(OADateTime value, String format) {
        return OAConv.toString(value, format);
    }
    /**
        Calls OAConverter to convert and format OADate.
        see OAConverterOADate
    */
    public static String format(OADate value) {
        return OAConv.toString(value, OADate.getGlobalOutputFormat());
    }


    
    /** fmt/format javadoc
        Used to format/mask Strings using a "Pick like" format/mask String.
        <p>
        
        Also suoports formats for Date/Times (see OADateTime)
        and Numbers (see OAConverterNumber)
        <p>

        <b>Formatting Strings</b>
        <pre>

        Example:  fmt(str,"12 L2.,$0(MASK)");
        
        Format description for "12 L2,$0(MASK)":
            12 = width - not required. 
                 will pad with spaces if pad character is not defined.
                 if width is not included, then length of String is not restricted.
        ' ' = trailing blanks that will be added to the end of formatted String.
            L = L, R, or C justified
            2 = decimal places - can only be ONE digit.  Rounding will be used.
            . = if value has to be truncated, then "..." will be the last 3 chars.  Only used with "L" justified.
            , = if you want commas to seperate numbers
            $ = dollar sign, only if 'R' justified  puts it in first char
            0 = any pad character - default space. Dont put this 1 after L/R, since
                that position is used for the amount of decimal places.
            Mask = must be in "()".  Use # character to have actual characters inserted,
                   all other characters in mask will be inserted.

        Examples:
        
        fmt("1234.5", "R4,") 
            "R4," = align right, 4 decimal places with comma seperators.
            output: "1,234.5000"  
        
        fmt("123.5", "R00")
            "R00" = align right, 0 decimal places (causes rounding), pad with '0' character
            output: "123"
        
        fmt("123.5", "8R00")
            "8R00" = 8 width to fill, 
            output: "00000123"
        
        fmt("123.5", "8 R00")
            "8 R00" = 8 width, append one space, right justified, 0 decimal places, '0' fill
            output: "00000123 "
        
        fmt("1231231234","13  R((###)###-####)")
            "13  R((###)###-####)" = 13 width, append 2 spaces, right justified, mask to use.
                 Note: the mask must be put into () and use # to denote where to insert the 
                 characters within the supplied String.
            output: "(123)123-1234  "
        fmt("CustomerName", "8L.")
             output: "Custo..."
        </pre>
    */
    public static String format(String str, String format) {
        return fmt(str, format);
    }
    public static String pickFormat(String str, String format) {
        return fmt(str, format);
    }

    
    
    
    /**  
        Used to format/mask Strings using a format/mask String.
        @see #format(String,String)
    */
    public static String fmt(String str, String format) {
        if (format == null) return "";
        
        // see if format is for a data/time
        String s = format.toLowerCase();
        int x = s.length();
        boolean b = false;
        boolean bLetters = false;
        for (int i=0; i<x; i++) {
            char c = s.charAt(i);
            if ("lrc".indexOf(c) >= 0) {
                b = true;
                break;
            }
            if (Character.isLetter(c)) bLetters = true;
        }
        if (!b) {
            if (bLetters) {
                // try date
                try {
                    OADateTime dt = new OADateTime(str);
                    return dt.toString(format);
                }
                catch (Exception e) {}
            }
            else {
                // try number
                try {
                    Number num = OAConv.toDouble(str);
                    return OAConv.toString(num, format);
                }
                catch (Exception e) {}
            }
        }
        
        
        // see if format is for a number
        
        
        int i,j,k,l, blanks=0, len=0;
        char lr=0;
        char testc, charPad = ' ';
        int deci=0;
        boolean comma=false, dollar=false;
        boolean deci_flag=false;
        boolean bDots = false;
        String test, test1;

        if (str == null) str = "";
        if (format == null) return str;

        x = format.length();
        StringBuilder sb = new StringBuilder(str.length() + x);


        // find L or R and format number
        for (i=0,len=0; i<x && lr==0; i++) {
            testc = Character.toUpperCase(format.charAt(i));
            if ("RLC".indexOf(testc) < 0 ) continue;

            lr = testc;
            test = field(format,format.charAt(i),1);
            if (test == null) test = "";
            test1 = field(test,' ',1);
            if (test1 == null) test1 = "";
            /* length plus spaces  ex: "10 L" */
            blanks = test.length() - test1.length();
            try {
                len = Integer.parseInt(test1);
            }
            catch (Exception e) {
                len = 0;
            }
        }

        if (lr == 0) {
            lr = 'L';
            test1 = field(format,' ',1);
            if (test1 == null) test1 = "";
            i = test1.length();
            if (i > 0) {
                try {
                    len = Integer.parseInt(test1);
                }
                catch (Exception e) {
                    len = 0;
                }
            }
            for ( ;i<x && format.charAt(i) == ' '; i++) blanks++;
        }


        // check for decimals
        if ( i < format.length() ) {
            if ( Character.isDigit(format.charAt(i)) ) {
                deci =  (format.charAt(i++) - '0');
                deci_flag = true;
            }
        }

        for (; i < format.length() && format.charAt(i) != '('; i++) {
            switch (format.charAt(i)) {
            case ',': comma = true;
                break;
            case '$': dollar = true;
                break;
            case '.':
                if (lr == 'L' && !bDots) {
                    bDots = true;
                    break;
                }
            default:
                charPad = format.charAt(i);
            }
        }

        if (deci_flag || comma) {
            double d = 0.0;
            try {
                d = OAConv.toDouble(str);
                s = "";
                if (deci_flag) {
                    s = "";
                    for (j=0; j<deci; j++) {
                        if (j == 0) s += ".";
                        s += "0";
                    }
                }
                if (comma) {
                    s = "#,###" + s;
                }
                else s = "#"+s;
                str = OAConv.toString(new Double(d), s);
            }
            catch (Exception e) {
            }
        }


        // create mask
        j = format.indexOf("(");
        if (j>=0) {
            test = format.substring(j+1);
            j = test.length();

            if ( test.charAt(j-1) == ')' ) test = test.substring(0, (--j));

            if (lr == 'R') {
                for (i=0,k=0; i < j ;i++) {
                    testc = test.charAt(i);
                    if (testc == '#') k++;
                }
                k = (k - str.length());
                if (k > 0) str = pad(str, k, false, ' ');
            }

            String newString = "";
            for (i=k=l=0; i < j ;i++,k++ ) {
                testc = test.charAt(i);

                if (testc == '#') {
                    if (str.length() > l) newString += str.charAt(l++);
                    else newString += ' ';
                }
                else newString += testc;
            }
            str = newString;
        }


        if (dollar && lr == 'R') {
            str = '$' + str;
        }


        /* format */
        i = str.length();
        x = (Math.abs(i - len)) / 2;

        if (i > len) {
            if (len != 0) {
                if (lr == 'R') {
                    str = str.substring(i - len);
                }
                else {
                    if (lr == 'L') {
                        if (bDots && len > 3) str = str.substring(0, len-1) + "...";
                        else str = str.substring(0, len);
                    }
                    else { // 'C'
                        str = str.substring(x,x+len);
                    }
                }
            }
        }
        else {
            for (j=0; i<len; i++,j++) {
                if (lr == 'R' || (lr == 'C' && j<x)) {
                    str = charPad + str;
                }
                else str += charPad;
            }
        }
        for (i=0; i<blanks; i++) str += " ";


        return str;
    }


    /**
        Remove digit characters from String.
        @param value is String to strip.
        @return if value=null then null, else new String with digits removed.  
        Note: does not remove "." between digits.
    */
    public static String stripNumber(String value) {
        if (value == null) return null;
        String s = "";
        int x = value.length();
        for (int i=0; i<x; i++) {
            char c = value.charAt(i);
            if ( Character.isDigit(c) ) s += c;
        }
        return s;
    }


    /**
        Used to generate a String based on a mask.
        @param value is String to use with mask.
        @param mask where all # characters will be replaced by data in value.  All other characters (non #)
               in mask will be outputted with new String.
        @return new String with mask applied.
    */
    public static String mask(String value, String mask) {
        return mask(value, mask, false);
    }

    /**
        Used to generate a String based on a mask.
        @param value is String to use with mask.  If null, then value is set to a blank "" String.
        @param mask where all # characters will be replaced by data in value.  All other characters (non #)
               in mask will be outputted with new String.
        @param bRightJustified if true, then mask will be generated from right to left, else left to right.
        @return new String with mask applied.  If mask == null, then value is returned.  
    */
    public static String mask(String value, String mask, boolean bRightJustified) {
        if (mask == null) return value;
        if (value == null) value = "";
        String s = "";

        int i = 0;
        int x = value.length();

        int i2 = 0;
        int x2 = mask.length();

        if (bRightJustified) {
            int cnt = 0;
            for ( ; i2<x2; i2++) {
                char c = mask.charAt(i2);
                if (c == '#') cnt++;
            }
            i2 = 0;
            if (cnt > x) {
                value = pad(value, cnt-x, false, ' ');
                x = value.length();
            }
            else {
                if (x > cnt) {
                    value = value.substring(x-cnt);
                }
            }
        }

        for ( ; i2<x2; i2++) {
            char c = mask.charAt(i2);
            if (c == '#') {
                if (i < x) {
                    s += value.charAt(i);
                    i++;
                }
            }
            else s += c;
        }
        return s;
    }

    /**
        Removes characters from a String.
        @param value is String to strip from.
        @param chars values to remove from value
    */
    public static String strip(String value, String chars) {
        return stripChars(value, chars, false);
    }

    /**
        Removes characters from a String that are not valid.
        @param value is String to strip from.
        @param chars is the characters that are valid, other characters will be removed.
    */
    public static String accept(String value, String chars) {
        return stripChars(value, chars, true);
    }
    
    /**
        called by strip, accept
    */
    protected static String stripChars(String value, String chars, boolean bKeepChars) {
        if (value == null) return null;
        if (chars == null) return value;
        String s = "";
        int x = value.length();
        for (int i=0; i<x; i++) {
            char c = value.charAt(i);
            if (bKeepChars) {
                if (chars.indexOf(c) >= 0) s += c;
            }
            else {
                if (chars.indexOf(c) < 0) s += c;
            }
        }
        return s;
    }


    /** 
        Converts fileName path to correct system file.separator characters.
        @return new String with corrected file path characters.
    */
    public static String convertFileName(String fileName) {
    	return OAFile.convertFileName(fileName);
    }

    /** 
	    Converts fileName path to correct system file.separator characters.
	    @return new String with corrected file path characters.
	*/
	public static String convertFileName(String fileName, boolean bEndWithSlashChar) {
	    return OAFile.convertFileName(fileName, bEndWithSlashChar);
	}
    
    
    public static String getFileName(String filePath) {
        return OAFile.getFileName(filePath);
    }
    public static String getDirectoryName(String filePath) {
        return OAFile.getDirectoryName(filePath);
    }
    

    /**
        Converts a color to a String that represents the Hex value. 
        @return null if color=null, else Hex String with leading "#", ex: "#00FFCC"
    */
    public static String colorToHex(Color color) {
        if (color == null) return null;
        String colorstr = new String("#");

        // Red
        String str = Integer.toHexString(color.getRed());
        if (str.length() > 2)
            str = str.substring(0, 2);
        else if (str.length() < 2)
            colorstr += "0" + str;
        else
            colorstr += str;

        // Green
        str = Integer.toHexString(color.getGreen());
        if (str.length() > 2)
            str = str.substring(0, 2);
        else if (str.length() < 2)
            colorstr += "0" + str;
        else
            colorstr += str;

        // Blue
        str = Integer.toHexString(color.getBlue());
        if (str.length() > 2)
            str = str.substring(0, 2);
        else if (str.length() < 2)
            colorstr += "0" + str;
        else
            colorstr += str;

        return colorstr;
    }

    /**
        Returns true if String has any digit characters in it.
    */
    public static boolean hasDigits(String word) {
        if (word == null) return false;
        for (int k=1; k<word.length(); k++) {
            char ch = word.charAt(k);
            if (Character.isDigit(ch))
                return true;
        }
        return false;
    }

    /**
        Soundex is used for creating a code that is used to find similar words.
        <p>
        20100417 now using more advanced algorithm
          see: http://www.archives.gov/genealogy/census/soundex.html
        <p>
        This code is set to 4 char value - padded with char '0'.<br>
        If word == NULL then sndx = "0000"
        <pre>
        use first letter
        exclude "AEHIOUWY" or any char that is not a letter
        exclude all duplicates
        '0' pad to 4 chars             Note: this will also use digits

        From: "BFPVCGJKQSZXDTLMNR"
        To  : "111122222222334556"

        EXAMPLE:
        soundex(sndx,"Vincent")  sndx = "V523"
        soundex(sndx,"Via")      sndx = "V000"
        </pre>
        @param word String to create a soundex code for.
        @return new String that is soundex code for word.  If word is null,then "0000" is returned.
    */
    public static String soundex(String word) {
        if (word == null || word.trim().length() == 0) return "0000";
        word = word.toLowerCase();
        char[] result = new char[4];
        result[0] = word.charAt(0);
        result[1] = result[2] = result[3] = '0';
        int index = 1;

        char codeLast = _getSoundexChar(result[0]);
        boolean bCheckNext = false;
        
        for (int k=1; k<word.length(); k++) {
            char ch = word.charAt(k);
            char code = _getSoundexChar(ch);
            
            if (code == 0) {
                bCheckNext = false;
                codeLast = 0;
                continue;
            }

            if (code == 1) {
                if (bCheckNext) bCheckNext = false;
                else bCheckNext = true;
                continue;
            }
            

            if (bCheckNext) {
                bCheckNext = false;
                if (code == codeLast) {
                    codeLast = 0;
                    continue;
                }
            }
            
            
            if (code == codeLast) {
                codeLast = 0;
                continue;
            }

            result[index++] = code;
            if (index > 3) break;

            codeLast = code;
        }
        return new String(result);
    }

    /** 
     * @param ch
     * @param chLast
     * @return 0 if ch should not be used, 1 if it is vowel, or 'h' or 'w'
     */
    private static char _getSoundexChar(char ch) {
        char code = ' ';
        switch (ch) {
            case 'b': case 'f': case 'p': case 'v':
                code = '1';
                break;
            case 'c': case 'g': case 'j': case 'k':
            case 'q': case 's': case 'x': case 'z':
                code = '2';
                break;
            case 'd': case 't':
                code = '3';
                break;
            case 'l':
                code = '4';
                break;
            case 'm': case 'n':
                code = '5';
                break;
            case 'r':
                code = '6';
                break;
            case 'a': case 'e': case 'i': case 'o': case 'u': case 'y':
            case 'h': case 'w':
                code = 1;
                break;
            default:
                code = 0;
                break;
        }
        return code;
    }
    
    
    /**
        Returns true if String is a valid number.
        This will try to convert the String to a Double.
        @param str String to check
        @return true if String can be converted to a Double.  If str is null then false is returned.
        see OAConverterNumber
    */
    public static boolean isNumber(String str) {
        if (str == null || str.length() == 0) return false;
        Double d = (Double) OAConverter.convert(Double.class, str);
        return d != null;
    }

    public static boolean isInteger(String str) {
        if (str == null || str.length() == 0) return false;
        Long d = (Long) OAConverter.convert(Long.class, str);
        return d != null;
    }
    
    /**
        Returns true if String is a valid Date.
        This will try to convert the String to a OADate.
        @param s String to check
        @return true if String can be converted to a OADate.
        see OAConverterOADate
    */
    public static boolean isDate(String s) {
        if (s == null || s.length() == 0) return false;
        OADate d = (OADate) OAConverter.convert(OADate.class, s);
        return d != null;
    }
    /**
        Returns true if String is a valid Time.
        This will try to convert the String to a OATime.
        @param s String to check
        @return true if String can be converted to a OATime.
        see OAConverterOATime
    */
    public static boolean isTime(String s) {
        if (s == null || s.length() == 0) return false;
        OATime d = (OATime) OAConverter.convert(OATime.class, s);
        return d != null;
    }
    /**
        Returns true if String is a valid DateTime.
        This will try to convert the String to a OADateTime.
        @param s String to check
        @return true if String can be converted to a OADateTime.
        see OAConverterOADateTime
    */
    public static boolean isDateTime(String s) {
        if (s == null || s.length() == 0) return false;
        OADateTime d = (OADateTime) OAConverter.convert(OADateTime.class, s);
        return d != null;
    }

    /**
        Case sensitive, compares two String to see if they are equal.  This will automatically check
        for nulls.
        @return true if both Strings are equal, including if both are null.
        @see equals(String,String,boolean)
    */
    public static boolean equals(String s1, String s2) {
        return equals(s1, s2, false);
    }

    /**
        Compares two String to see if they are equal.  This will automatically check
        for nulls.
        @param bIgnoreCase if true, performs a comparision that is case insensitive.
        @return true if both Strings are equal, including if both are null.
        @see equals(String,String,boolean)
    */
    public static boolean equals(String s1, String s2, boolean bIgnoreCase) {
        if (s1 == s2) return true;
        if (s1 == null || s2 == null) return false;
        if (bIgnoreCase) return s1.equalsIgnoreCase(s2);
        return s1.equals(s2);
    }


/* ***
    public static void main(String[] argv) {
//        String s = OAString.fmt("1234.5678", "12R2,");
        OAString oas = new OAString();
        String s = oas.fmt(argv[0], argv[1]);

        System.out.println("-------->"+s+"<------");

        // double x = OAConv.toDouble("-12345.5678");
        int x = OAConv.toInt("-12345.5678");
        System.out.println("-------->"+OAConv.toString(x, "#,###.####")+"<------");
    }
*****/

    public static String toString(Object obj) {
        if (obj == null) return "";
        return OAConverter.toString(obj);
    }

    /**
        Convert a number to a string value.  Example: 21 = "21st"
    */
    public static String toNumberString(int x) {
        String text;
        if ((x%10) == 0) text = x+"th";
        else if ((x%100) > 9 && (x%100) < 21) text = x+"th";
        else if ((x%10) == 1) text = x+"st";
        else if ((x%10) == 2) text = x+"nd";
        else if ((x%10) == 3) text = x+"rd";
        else text = x+"th";
        return text;
    }


    /**
     * Shorten line to width amount of characters.  If longer then width, then "..." will be the end.
     */
    public static String trunc(String orig, int width) {
        return truncate(orig, width);
    }
    public static String truncate(String orig, int width) {
        if (orig == null) return null;
        if (width == 0) return "";
        if (orig.length() < width) return orig;
        
        if (width > 2) {
            orig = orig.substring(0,width-3) + "...";
        }
        else {
            orig = ".";
            if (width > 1) orig += ".";
        }
        return orig;
    }
    
    /**
     * Shorten line to width amount of characters.  If longer then width, then "..." will be the end.
     */
    public static String lineBreak(String origLine, int width) {
    	return truncate(origLine, width);
    }
    
    /**
     * Split text into lines, and adding a line seperator between the breaks.

     * @param width
     * @param delim, string to insert at line break
     * @param maxLines, the max amount of lines to create. If longer, then "..." will be added
     * @return
     */
    public static String lineBreak_OLD(String origLine, int width, String delim, int maxLines) {
    	if (origLine == null) return null;

		String newline = "";
		for (int i=1;;i++) {
			String line = field(origLine, delim, i);
			if (line == null) break;
			if (i > 1) newline += delim;
			for (int linecnt=1; ;linecnt++) {
				int x = line.length();
				if (x > width) {
					int j = width;
					if (linecnt == maxLines) j -= 4;
					for (; j>5; j--) {
						char ch = line.charAt(j);
						if (". -".indexOf(ch) >= 0) {
							break;
						}
					}
					if (linecnt != maxLines) j++;
					if (linecnt != 1) newline += delim;
					newline += line.substring(0, j);
					line = line.substring(j);
	
					if (linecnt == maxLines) {
						newline += " ...";
						break;
					}
				}
				else {
					newline += line;
					break;
				}
			}
		}
		return newline;
    }

    public static String createRandomString(int min, int max) {
        return getRandomString(min, max, true, true, false);
    }    
    public static String getRandomString(int min, int max) {
        return getRandomString(min, max, true, true, false);
    }    
    public static String getRandomString(int normal, int min, int max) {
        return getRandomString(normal, min, max, true, true, false);
    }    
    
    /**
     * Returns a string that has random generated characters
     * @param min minimum amount of chars in the result.
     * @param max maximum amount of chars in the result.
     * @param bUseDigits if true will use chars 0-9
     * @param bUseAlpha if true will use chars a-z
     * @param bCapFirstChar if true and bUseAlpha, then the first char will be capitalized, otherwise all chars will be lowercase.
     */
    public static String createRandomString(int min, int max, boolean bUseDigits, boolean bUseAlpha, boolean bCapFirstChar) {
        return getRandomString(min, max, bUseDigits, bUseAlpha, bCapFirstChar);
    }
    
    public static String getRandomString(int min, int max, boolean bUseDigits, boolean bUseAlpha, boolean bCapFirstChar) {
        return getRandomString(0, min, max, bUseDigits, bUseAlpha, bCapFirstChar);
    }
    public static String getRandomString(int normal, int min, int max, boolean bUseDigits, boolean bUseAlpha, boolean bCapFirstChar) {
		String result = "";

		// adjust min/max based on normal
		if (normal > 0) {
		    if (normal > min) {
                int diff = (normal - min);
		        if (Math.random() < .75) {
		            diff = (int) (diff * .30);
		        }
                min = (int) (normal - (Math.random() * diff));
		    }
		    else min = normal;

            if (normal < max) {
                int diff = (max - normal);
                if (Math.random() < .9) {
                    diff = (int) (diff * .20);
                }
                max = (int) (normal + (Math.random() * diff));
            }
            else max = normal;
		}
		
		int x = min;
		if (min < max) x += (int) (Math.random() * (max-min));
		
		
		for (int i=0; i<x; i++) {
			char ch;
			
			boolean bAlpha;
			if (bUseDigits) {
				if (!bUseAlpha) bAlpha = false;
				else if (i == 0 && bUseAlpha) bAlpha = true;
				else bAlpha = Math.random() > .5;
			}
			else bAlpha = true;
			
			if (bAlpha) {
				ch = (char) (Math.random() * 26);
				if ((i == 0 && bCapFirstChar) || Math.random() > .70) {
				    ch += 'A';
				    if (ch == 'O') ch = 'P';
				}
				else {
				    ch += 'a';
                    if (ch == 'l') ch = 'm';
				}
				result += ch;
			}
			else {
				ch = (char) (Math.random() * 10);
				ch += '0';
				if (bUseAlpha && ch == '0') ch = '1';
                if (bUseAlpha && ch == '1') ch = '2';
				result += ch;
			}
		}
		return result;
	}

    public static final String LoremLipsum = 
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque nec eros pretium, dignissim est sit amet, malesuada augue. Sed pharetra ex ut nulla feugiat laoreet. Nunc finibus malesuada est, et fermentum lorem iaculis eget. Aenean pharetra augue ac elit gravida consectetur. Praesent dapibus sem quis tellus condimentum, eget finibus massa maximus. Quisque tempor a felis in consectetur. Donec a rutrum neque. Nam viverra eros ut arcu interdum facilisis.  " + 
        "Etiam ultricies nisl id lacus vulputate mattis. Nulla condimentum et metus vitae vestibulum. Aliquam ac risus eros. Vestibulum dignissim bibendum sapien, quis feugiat sapien lacinia nec. Mauris id justo pharetra, tincidunt est vel, varius libero. Ut efficitur nulla nec malesuada efficitur. Nulla luctus purus eu metus feugiat, eu semper metus viverra. Aliquam erat volutpat. Vivamus mollis turpis augue, eget maximus lorem convallis vel. Nam sed arcu vitae diam tempus malesuada id non nisl. Phasellus scelerisque nunc ut dapibus interdum.  " + 
        "Donec ornare elementum laoreet. Sed diam mauris, eleifend quis lacinia at, egestas eu tellus. Sed neque augue, vestibulum ut arcu non, accumsan aliquet enim. Aliquam fringilla neque a enim pellentesque hendrerit. Sed ac semper arcu, vitae porta purus. Curabitur sit amet faucibus augue. Praesent accumsan elit ut sem dictum vulputate. Praesent sed tempus mauris, ut ultrices dolor. Nunc congue, tortor sed lacinia pulvinar, mauris mi molestie lorem, at rutrum lorem est euismod magna. Suspendisse sagittis mauris in interdum gravida. Phasellus a ante hendrerit, pulvinar urna eget, scelerisque massa."; 
    
    public static String getDummyText(int normal, int min, int max) {
        // adjust min/max based on normal
        if (normal > 0) {
            if (normal > max) normal = max;
            if (normal > min) {
                int diff = (normal - min);
                if (Math.random() < .75) {
                    diff = (int) (diff * .30);
                }
                min = (int) (normal - (Math.random() * diff));
            }
            else min = normal;

            if (normal < max) {
                int diff = (max - normal);
                if (Math.random() < .9) {
                    diff = (int) (diff * .20);
                }
                max = (int) (normal + (Math.random() * diff));
            }
        }
        int sampleSize = min;
        if (min < max) sampleSize += (int) (Math.random() * (max-min));
        
        String result = "";
        final int maxLipsum = LoremLipsum.length();
        
        for ( ; sampleSize > maxLipsum; sampleSize -= maxLipsum) {
            result += LoremLipsum;
            result += "  ";
        }
        
        int beginPos = maxLipsum - sampleSize;
        beginPos = (int) (Math.random() * beginPos);
        for ( ;beginPos > 0 && LoremLipsum.charAt(beginPos) != ' '; beginPos--);
        if (beginPos > 0) beginPos++;
        
        result += LoremLipsum.substring(beginPos, beginPos+sampleSize);
        return result;
    }    
    
    
    /**
     * Short name for createPropertyPath
     * @see #createPropertyPath(String...)
     */
    public static String cpp(String... args) {
        return createPropertyPath(args);
    }
    /**
     * @param clazz name of the first class, in case it is a generic type.  (ex: OALeftJoin.A)
     * @param args
     * @return
     */
    public static String cpp(Class clazz, String... args) {
        return createPropertyPath(clazz, args);
    }

    /**
     * Used to create a dot separated String, used for property paths.
     */
    public static String createPropertyPath(String... args) {
        if (args == null) return "";
        String result = null;
        for (String s : args) {
            if (s == null) continue;
            if (result == null) result = s;
            else {
                if (s.indexOf(':') == 0) result += s; // filter
                else result += "." + s;
            }
        }
        return result;
    }
    /**
     * @param clazz name of the first class, in case it is a generic type.  (ex: OALeftJoin.A)
     * @param args each property used in the property path.
     * @return a dot separated string that represents a valid property path.
     */
    public static String createPropertyPath(Class clazz,  String... args) {
        if (args == null) return "";
        String result = null;
        for (String s : args) {
            if (s == null) continue;
            if (result == null) {
                if (clazz != null) {
                    result = "(" + clazz.getName() + ")" + s; 
                }
                else result = s;
            }
            else {
                if (s.indexOf(':') == 0) result += s;  // filter
                else result += "." + s;
            }
        }
        return result;
    }
    
    
    
    public String toUTF8(String isoString) {
        String utf8String = null;
        if (null != isoString && !isoString.equals("")) {
            try {
                byte[] stringBytesISO = isoString.getBytes("ISO-8859-1");
                utf8String = new String(stringBytesISO, "UTF-8");
            }
            catch(UnsupportedEncodingException e) {
                //  TODO: This should never happen. The UnsupportedEncodingException
                // should be propagated instead of swallowed. This error would indicate
                // a severe misconfiguration of the JVM.

                // As we can't translate just send back the best guess.
                System.out.println("UnsupportedEncodingException is: " + e.getMessage());
                utf8String = isoString;
            }
        }
        else {
            utf8String = isoString;
        }
        return utf8String;
    } 
    

    public static String getSHAHash(String input){
        return OAEncryption.getHash(input);
    }
    public static String convertToSHAHash(String input){
        return OAEncryption.getHash(input);
    }
    

    /**
     * Formats a string to have a max width per line separated by a delimiter.  Lines are broken by whitespace, '-', '.'
     * @param width max width per line.
     * @param delim inserted into origLine to create line breaks
     * @param maxLines maximum number of lines, if result is longer, then it will be truncated and append with " ..." 
     */
    public static String lineBreak(String origLine, int width, String delim, int maxLines) {
        if (origLine == null) return "";
        if (width < 1) return origLine;
        if (maxLines < 0) maxLines = 0;
        
        String newLine = "";
        
        int len = origLine.length();
        int lastBreakChar = 0;  // position of last place that a break can be made
        int startPos = 0;  // start position of new line
        int currentPos = 0;  // current position 
        int currentLine = 0;
        
        for (int i=1; ; i++, currentPos++) {

            if (i >= len) {
                // copy the rest
                newLine += origLine.substring(startPos);
                break; // done
            }
            
            if ((currentPos-startPos) > width) {
                currentLine++;
                if (maxLines > 0 && currentLine >= maxLines) {
                    // need room to append " ..."
                    if (lastBreakChar == 0) currentPos = startPos+1;
                    else if (lastBreakChar+4 > currentPos) {
                        currentPos -= 4;
                        lastBreakChar = 0;
                        for ( ;currentPos-1>startPos; currentPos--) {
                            char ch = origLine.charAt(currentPos);
                            if (Character.isWhitespace(ch) || ch == '.' || ch == '-') {
                                break;
                            }
                        }
                    }
                }
                
                if (lastBreakChar > 0) currentPos = lastBreakChar+1;

                newLine += origLine.substring(startPos, currentPos);

                if (maxLines > 0 && currentLine >= maxLines) {
                    newLine += " ...";
                    break; // done
                }
                newLine += delim;
                
                startPos = currentPos;
                lastBreakChar = 0;
                continue;
            }
            char ch = origLine.charAt(i);
            if (Character.isWhitespace(ch) || ch == '.' || ch == '-') lastBreakChar = i;
        }
        return newLine;
    }
    
    public static void mainXX(String[] args) {
        String s = "Tymczak";
        System.out.println(soundex(s));

        System.out.println(soundex("Ashcraft"));
        
         
    }
    
    public static void mainX(String[] args) {
        
        long xx = (long) (1234 * 1e5);
        xx += 56789;
        xx = (xx % 7777);
        String codex = ""+xx;
        String codexx = "V";
        for (int ix=0; ix<codex.length(); ix++) {
            if ( (Math.random() * 10) > 5) {
                codexx += (char) ('a' + ((int)(Math.random()*26.0)));
            }
            codexx += codex.charAt(ix);
            if ( (Math.random() * 10) > 5) {
                codexx += (char) ('A' + ((int)(Math.random()*26.0)));
            }
        }

        System.out.println("Codexx="+codexx);
    }

    public static boolean notEmpty(Object obj) {
        return !isEmpty(obj, false);
    }
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj, false);
    }
    

    /**
     * @return If null, then returns true; if String and length is 0, returns true; if array and length == 0, returns true.
     * all others return false.
     * @see OACompare#isEmpty(Object)
     */
    public static boolean isEmpty(Object obj) {
        return isEmpty(obj, false);
    }
    public static boolean isEmpty(Object obj, boolean bTrim) {
        if (obj == null) return true;
        if (obj instanceof String) {
            if (bTrim) {
                if (((String)obj).trim().length() == 0) return true;
            }
            else {
                if (((String)obj).length() == 0) return true;
            }
        }
        else if (obj.getClass().isArray()) {
            if (Array.getLength(obj) == 0) return true;
        }
        return false;
    }
    public static boolean isEqual(String s, String s2) {
        return isEqual(s, s2, false);
    }
    public static boolean isEqual(String s, String s2, boolean bIgnoreCase) {
        if (s == s2) return true;
        if (s == null || s2 == null) return false;
        if (bIgnoreCase) return s.equalsIgnoreCase(s2);
        return s.equals(s2);
    }

    /**
     * Returns a "non-null" value
     * @param str
     * @return "" if str is null, else return str
     */
    public static String fmt(String str) {
        if (str == null) return "";
        else return str;
    }

    public static int compare(String s1, String s2) {
        if (s1 == s2) return 0;
        if (s1 == null) return -1;
        return s1.compareTo(s2);
    }
    public static boolean equalsIgnoreCase(String s1, String s2) {
        if (s1 == s2) return true;
        if (s1 == null) return false;
        return s1.equalsIgnoreCase(s2);
    }

    
    /**
        Only allows digits and ' ' characters.
        Will left pad with spaces to make 10 char long.
     */
    public static String convertToValidPhoneNumber(String phone) {
        if (phone == null) return null;
        int x = phone.length();
        if (x ==0) return phone;
        StringBuilder sb = new StringBuilder(x);
        boolean b = false;
        for (int i=0; i<x; i++) {
            char ch = phone.charAt(i);
            if (!Character.isDigit(ch)) {
                if (ch != ' ') {
                    b = true;
                    continue;
                }
            }
            sb.append(ch);
            
        }
        x = sb.length();
        for (int i=x; i<10; i++) {
            b = true;
            sb.insert(0,' ');
        }
        if (b) {
            phone = sb.toString();
        }
        return phone;
    }

    // add leading spaces to each line in a string that is separated by '\n' 
    public static String indent(String text, int amt) {
        String newText = "";
        String pad = pad("", amt, false, ' ');
        for (String s : text.split("\n") ) {
            newText += pad + s + '\n';
        }
        return newText;
    }

    /**
     * remove leading spaces from each line in a string that is separated by '\n' 
     * @param text
     * @param bBasedOnFirstLine if true, then each line will only remove the same leading spaces found in the first line.  This is good for code.
     */
    public static String unindent(String text) {
        return unindent(text, false);
    }
    /**
     * Used for removing the extra indent spacing for pasting code.
     */
    public static String unindentCode(String text) {
        return unindent(text, true);
    }
    public static String unindent(String text, boolean bBasedOnFirstLine) {
        String newText = "";

        int max = -1;
        for (String s : text.split("\n") ) {
            if (newText.length() > 0) newText += '\n';
            
            int pos = 0;
            for ( ; pos < s.length() && s.charAt(pos) == ' ' && (!bBasedOnFirstLine || max < 0 || pos < max); pos++);
            if (bBasedOnFirstLine && max < 0) max = pos;
            
            if (pos > 0) s = s.substring(pos);
            newText += s;
        }
        return newText;
    }
    
    
    /**
     * Remove ending whitspace from a string.
     */
    public static String trimEndingWhitespace(String text) {
        if (text == null) return null;
        int x = text.length();
        for (int i=0; i<x; i++) {
            char c = text.charAt(x-i-1);
            if (!Character.isWhitespace(c)) {
               if (i == 0) return text;
               return text.substring(0, x-i);
            }
        }
        return "";
    }
    
    
    public static String[] parseLine(String line, char sep, boolean bCouldHaveQuotes) {
        return parseLine(line, sep, bCouldHaveQuotes, 25);
    }    

    /**
     * Strips out leading and trailing whitespace for each column.
     * if bCouldHaveQuotes is true, then begin and end quotes will be removed; either single or double quote char.
     */
    public static String[] parseLine(String line, char sep, boolean bCouldHaveQuotes, int sizeEstimate) {
        if (line == null || sep == 0) return null;
        if (line.length() == 0) return new String[0];
        ArrayList<String> alString = new ArrayList<String>(Math.max(5,sizeEstimate));

        int lineLength = line.length();
        boolean bStarted = false;
        int startPos = 0;
        char qchar = 0;
        int lastQpos = -1;
        int firstWhitespace = -1;
        
        for (int i=0; ;i++) {
            char ch = 0;
            if (i != lineLength) {
                ch = line.charAt(i);
                if (bCouldHaveQuotes && (ch == '\'' || ch == '\"') && ch != sep) {
                    if (!bStarted) {
                        qchar = ch;
                        bStarted = true;
                        startPos = i+1;
                        continue;
                    }
                    else {
                        if (ch == qchar) {
                            lastQpos = i;  // might be ending pos
                            continue; // continue to sep char or eol
                        }
                    }
                }
            }

            boolean bWhitespace;
            if (i != lineLength) {
                bWhitespace = (" \n\r\f\b\t".indexOf(ch) >= 0);
            }
            else bWhitespace = false;
            
            if (i == lineLength || ch == sep) {
                if (qchar > 0 && lastQpos < 1) {
                    startPos--;
                }
                if (i == startPos) alString.add("");
                else {
                    int j;
                    if (lastQpos >= startPos) j = lastQpos;
                    else if (firstWhitespace >= 0) j = firstWhitespace;
                    else j = i;
                    
                    String s = line.substring(startPos, j);
                    alString.add(s);
                }
                if (i == lineLength) break;
                startPos = i+1;
                bStarted = false;
                qchar = 0;
                firstWhitespace = -1;
                continue;
            }
            lastQpos = -1;
            if (!bStarted) {
                if (bWhitespace) { // skip
                    startPos = i+1;
                    continue;
                }
                bStarted = true;
            }
            else {
                if (bWhitespace) {
                    if (firstWhitespace < 0) firstWhitespace = i;
                    continue;
                }
            }
            firstWhitespace = -1;
        }

        String[] ss = new String[0];
        ss = alString.toArray(ss);
        return ss;
    }
    public static void mainXXx(String[] args) {
        String s = "a','b,'c\'     \'x      ,'d";
        s = "'c\'     \'x      ,'d";
        s = "\'\"\"";
        String[] ss = parseLine(s,',',true);
        ss = parseLine(s,',',false);
        
        
        int x = 4;
        x++;
    }

    /**
     * Removes any leading &amp; trailing whitespace chars, but will leave single space chars
     * within text. 
     */
    public static String trimWhitespace(String text) {
        if (text == null) return null;
        StringBuilder sb = null;
        int x = text.length();
        
        char chLast = ' ';
        boolean bAddSpace = false;
        
        for (int i=0; i<x; i++) {
            char ch = text.charAt(i);
            
            if (Character.isWhitespace(ch)) {
                if (ch == ' ') {
                    if (chLast != ' ') {
                        bAddSpace = true;
                    }
                    chLast = ch;
                }
                if (sb == null) {
                    sb = new StringBuilder(x);
                    if (i > 0) sb.append(text.substring(0, i));
                }
            }
            else {
                if (sb != null) {
                    if (bAddSpace) {
                        sb.append(' ');
                        bAddSpace = false;
                    }
                    sb.append(ch);
                }
                chLast = ch;
            }
        }
        if (sb == null) return text;
        return sb.toString();
    }
    
    
    /**
     * Convert '&amp;' prefixed html codes to character.
     */
    public static String convertFromHtml(String html) {
        if (html == null) return null;

        if (html.indexOf('&') >= 0) {
            html = convert(html, "&amp;", "&");
            html = convert(html, "&quot;", "\"");
            html = convert(html, "&apos;", "'");
            html = convert(html, "&lt;", "<");
            html = convert(html, "&gt;", ">");
        }
        return html;
    }
   
    /**
     * Make sure that all chars value is &lt;= 127, otherwise convert to a space char 
     */
    public static String convertToAscii(String text) {
        if (text == null) return text;
        
        int x = text.length();
        StringBuilder sb = null;
        for (int i=0; i<x; i++) {
            char c = text.charAt(i);
            if (c > 127) {
                if (sb == null) {
                    sb = new StringBuilder(text.length());
                    if (i > 0) sb.append(text.substring(0,i));
                }
                switch (c) {
                case 8216:  
                case 8217: 
                    c = '\''; 
                    break; 
                case 8220:  
                case 8221: 
                    c = '\"'; 
                    break; 
                case 8211: 
                    c = '-'; 
                    break; 
                default: c = ' '; break;
                }
            }
            if (sb != null) sb.append(c);
        }
        if (sb == null) return text;
        else return sb.toString();
    }


    /**
     * This will split a string based on a delimiter char, 
     * and will also take into account values that are in single or double quotes.
     * Used to parse attributes from html tag, or name/value pairs from CSS style 
     * @param text
     * @param delimChar ex: '=', or ':'
     * @param bIncludeDelim if true then the delim will be included in the tokens
     * @param begChar ex: '&lt;'
     * @param endChar ex: '&gt;'
     * @param eovChar end of value, ex: ';'
     * @return
     */
    protected static String[] tokenize(String text, char delimChar, boolean spaceIsDelim, boolean bIncludeDelim, char begChar, char endChar, char eovChar) {
        if (text == null) return null;
        int x = text.length();
        char chQuote = 0;
        ArrayList<String> al = new ArrayList<String>();
        String next = "";
        int lastPos = 0;
        boolean bStarted = false;
        boolean bParsingValue = false;
        for (int i=0; ; i++) {
            if (i == x) {
                if (i == 0 || !bStarted) break;
                char ch = text.charAt(i-1);
                if (ch == endChar) i--;
                if (lastPos != i) al.add(text.substring(lastPos, i));
                break;
            }
            char ch = text.charAt(i);
            if (!bStarted) {
                if (ch == ' ' | ch == '\t') {
                    lastPos++;
                    continue;
                }
                if (ch != delimChar) bStarted = true;
            }
            if (i == 0) {
                if (ch == begChar) {
                    lastPos = 1;
                    continue;
                }
            }
            if (chQuote > 0) {
                if (ch == chQuote) {
                    al.add(text.substring(lastPos, i+1)); // include quotes
                    chQuote = 0;
                    lastPos = i+1;
                    bStarted = false;
                    bParsingValue = false;
                }
                continue;
            }
            if (ch == eovChar) {
                al.add(text.substring(lastPos, i));
                lastPos = i+1;
                bStarted = false;
                bParsingValue = false;
                continue;
            }
            if (bParsingValue && (ch == '\'' || ch == '\"')) {
                if (i == lastPos) {
                    chQuote = ch;
                    continue;
                }
            }
            if (ch == delimChar && (!bStarted || !bParsingValue)) {
                al.add(text.substring(lastPos, i));
                if (bIncludeDelim) al.add(""+ch);
                lastPos = i+1;
                bStarted = false;
                bParsingValue = true;
            }
            if (spaceIsDelim && ch == ' ') {
                al.add(text.substring(lastPos, i));
                lastPos = i+1;
                bStarted = false;
                if (bParsingValue) bParsingValue = false; 
            }
        }
        String[] ss = new String[al.size()];
        al.toArray(ss);
        return ss;
    }
    
    /* ex: 
     * <div style='background-image:url(oaproperty://com.tmgsc.hifive.model.oa.ImageStore/bytes?232); width:88; height:99' colspan=4 test xyz abc=Abcde123>adfa</div>
     */
    public static Map<String, String> getHTMLAttributeMap(String htmlTag) {
        HashMap<String, String> map = new HashMap<String, String>();
        if (htmlTag == null) return map;
        String[] ss = tokenize(htmlTag, '=', true, true, '<', '>', (char) 0);
        
        for (int i=1; i<ss.length; i++) { //skip first value, "tag name"
            String s1 = ss[i];
            
            if (i+1 == ss.length) {
                map.put(s1, "");
                break;
            }
            String s2 = ss[++i];
            if (s2.equals("=")) {
                if (i+1 == ss.length) {
                    map.put(s1, "");
                    break;
                }
                s2 = ss[++i];
            }
            else {
                map.put(s1, "");
                i--;
            }
            map.put(s1, s2);
        }
        return map;
    }
    
    public static Map<String, String> getCSSMap(String style) {
        HashMap<String, String> map = new HashMap<String, String>();
        if (style == null || style.length() == 0) return map;
        
        char ch = style.charAt(0);
        if (ch != '\'' && ch != '\"') ch = 0;
        
        String[] ss = tokenize(style, ':', false, false, ch , ch, ';');
        for (int i=0; i<ss.length; i+=2) {  
            String s = ss[i];
            map.put(ss[i], i+1==ss.length?"":ss[i+1]);
        }
        return map;
    }

    public static int parseInt(String val) {
        int x = 0;
        if (val == null) return x;
        boolean bStarted = false;
        boolean bNeg = false;
        int len = val.length();
        for (int i=0; i<len; i++) {
            char c = val.charAt(i);
            if (Character.isDigit(c)) {
                x *= 10;
                x += c - '0';
                bStarted = true;
            }
            else {
                if (bStarted) break;
                if (c == '-') {
                    bNeg = true;
                    bStarted = true;
                }
            }
        }
        if (bNeg) x *= -1;
        return x;
    }
    
    /**
     * Convert to a string, if null then it returns ""
     */
    public static String toString(String str) {
        return defaultString(str, "");
    }
    
    public static String toNonNull(String str) {
        if (str == null) return "";
        return str;
    }
    public static String getNonNull(String str) {
        if (str == null) return "";
        return str;
    }
    public static String convertToNonNull(String str) {
        if (str == null) return "";
        return str;
    }
    
    /**
     * Convert to a string, if null then it returns ""
     */
    public static String toString(String str, String strIfNull) {
        return defaultString(str, strIfNull);
    }
    /**
     * Convert to a string, if null then it returns ""
     */
    public static String defaultString(String str) {
        return defaultString(str, "");
    }
    /**
     * Convert to a string, if null then return strIfNull
     */
    public static String defaultString(String str, String strIfNull) {
        if (str == null) return strIfNull;
        return str;
    }
    
    /**
     * Convert to a string, if null then it returns ""
     */
    public static String notNull(String s) {
        if (s == null) s = "";
        return s;
    }
    
    public static String substring(String s, int pos1, int pos2) {
        if (s == null) return null;
        if (s.length() <= pos1) return "";
        if (pos2 >= s.length()) return s.substring(pos1);
        return s.substring(pos1, pos2);
    }
    
    public static void main99(String[] args) {
        String s = "123.456";
        s = format(s, "#,##0.00"); 
        
        s = "123.456";
        s = format(s, "$#,##0.00"); 
        
        OADate d = new OADate();
        s = d.toString();
        s = format(s, "MMMM, dd yyyy");
        int x = 4;
        x++;
    }
    
    public static void mainB(String[] args) {
        String html = "<body>adfadfdsdxxx<div style='background-image:url(oaproperty://com.tmgsc.hifive.model.oa.ImageStore/bytes?232); width:88; height:99px' colspan=4 test xyz abc=Abcde123>adfa</div>";

        int w = 0;
        int h = 0;

        String find = "background-image:url(oaproperty://com.tmgsc.hifive.model.oa.ImageStore/bytes?";
        
        int pos = html.toLowerCase().indexOf(find.toLowerCase());
        
        // need to find width:88, height:99
        if (pos >= 0) {
            int divPos = html.substring(0,pos).toLowerCase().lastIndexOf("<div ");
            if (divPos >= 0) {
                int divPos2 = html.indexOf(">", divPos);
                if (divPos2 >= 0) {
                    String s = html.substring(divPos, divPos2+1);
                    String style = null;
                    Map<String, String> map = OAString.getHTMLAttributeMap(s);
                    for (Map.Entry<String, String> ex : map.entrySet()) {
                        String sx = ex.getKey();
                        if (sx.equalsIgnoreCase("style")) {
                            style = ex.getValue();
                            break;
                        }
                    }
                    if (style != null) {
                        map = OAString.getCSSMap(style);
                        for (Map.Entry<String, String> ex : map.entrySet()) {
                            String sx = ex.getKey();
                            if (sx.equalsIgnoreCase("width")) {
                                String val = ex.getValue();
                                w = OAString.parseInt(val);
                            }
                            else if (sx.equalsIgnoreCase("height")) {
                                String val = ex.getValue();
                                h = OAString.parseInt(val);
                            }
                        }
                    }
                }
            }
        }        
        int x = 4;
        x++;
    }
    
    /**
     * Converts any non-Java indentifier characters to a '_'
     */
    public static String makeJavaIndentifier(String txt) {
        if (txt == null) return null;
        int x = txt.length();
        StringBuilder sb = null;
        for (int i=0; i<x; i++) {
            char ch = txt.charAt(i);
            if (!Character.isJavaIdentifierPart(ch)) {
                if (sb == null) {
                    sb = new StringBuilder(x);
                    if (i > 0) sb.append(txt.substring(0, i));
                }
                ch = '_';
            }
            if (sb != null) {
                sb.append(ch);
            }
        }
        if (sb == null) return txt;
        return new String(sb);
    }
    public static String convertToJavaIndentifier(String txt) {
        return makeJavaIndentifier(txt);
    }
    public static String getJavaIndentifier(String txt) {
        return makeJavaIndentifier(txt);
    }

    public static String removeEndingChars(String s, int amt) {
        if (s == null) return null;
        int x = s.length();
        if (amt >= x) return "";
        s = s.substring(0, x-amt);
        return s;
    }
    
    
    public static String append(String orig, String append) {
        return concat(orig, append, " ");
    }

    public static String append(String orig, String append, String sep) {
        return concat(orig, append, sep);
    }

    /*  See:  OACompare
    public static boolean isLike(String value, String matchValue) {
        return OACompare.isLike(value, matchValue);        
    }
    */
    
    public static String csv(String toText, Object value) {
        if (value == null) value = "";
        else {
            value = value.toString();
            value = ((String)value).replace('\"', '\'');
            value = ((String)value).replace(',', ' ');
            value = ((String)value).trim();
        }
        String s = concat(toText, (String) value, ",", true);
        return s;
    }
    
    public static String concat(String toText, String value) {
        return concat(toText, value, " ", true);
    }
    public static String concat(String toText, String value, String sepChar) {
        return concat(toText, value, sepChar, false);
    }
    public static String concat(String toText, String value, String sepChar, boolean bForce) {
        if (!bForce && (value == null || value.length() == 0)) {
            if (toText == null) return "";
            return toText;
        }
        if (value == null) value = "";
        if (toText == null || toText.length() == 0) {
            toText = value;
        }
        else {
            toText += sepChar;
            toText += value;
        }
        return toText;
    }

    
    
    public static String hilite(String line, String search) {
        return hilite(line, search, "<b style='background:yellow'>", "</b>", true);
    }

    public static String hiliteIgnoreCase(String line, String search, String beginTag, String endTag) {
        return hilite(line,search,beginTag, endTag,true);
    }
    public static String hilite(String line, String search, String beginTag, String endTag) {
        return hilite(line,search,beginTag, endTag,false);
    }
    public static String hilite(String line, String search, String beginTag, String endTag, boolean bIgnoreCase) {
        if (line == null || search == null) return line;

        final int xs = search.length();
        if (xs == 0) return line;
        if (bIgnoreCase) search = search.toLowerCase();
        
        final int xl = line.length();
        StringBuilder sb = null;  // dont allocate until first match is found
        char c=0, origChar=0;
        
        for (int i=0,j=0; ;i++) {
            if (i < xl) {
                origChar = c = line.charAt(i);
                if (bIgnoreCase) c = Character.toLowerCase(c);
                if (c == search.charAt(j)) {
                    j++;
                    if (j == xs) {
                        if (sb == null) {
                            sb = new StringBuilder(xl + (xl/10));
                            int e = (i - j) + 1;
                            if (e > 0) sb.append(line.substring(0,e));
                        }
                        sb.append(beginTag);
/*                    
Search="Vi"
i=6
i: 0123456789
   VinceViNce   
j:      12
*/   
                        int b = (i-j)+1;
                        sb.append(line.substring(b,b+j));
                        sb.append(endTag);
                        j = 0;   
                    }
                    continue;
                }
            }
            if (j > 0) { 
                if (sb != null) {
                    // go back to previously matched chars
                    int b = i-j;
/*                    
Search="Vix"
i=7
i: 0123456789
   VinceViNce   
j:      12
*/   
                    sb.append(line.substring(b,b+j));
                }
                i -= j;  // start at last checking point, loop with inc i by +1
                j = 0;
            }
            if (i >= xl) break;
            if (sb != null) sb.append(origChar);
        }
        if (sb == null) return line;
        return new String(sb);
    }
    
    
    public static void main2(String[] args) {
        String s = OAString.fmt("CustomerName", "8L.");
        
        s = OAString.fmt("CustomerName", "28L.");
        
        
        s = "abCDe_ 1.2-34.59:5";
        String s2 = OAString.makeJavaIndentifier(s);
        System.out.println(s+" ==> "+s2);
    }

    public static void main(String[] args) {
        
        String sx = String.format("%.5s", "this is a test");
        sx = String.format("%5.15s", "test");
        
        int x = LoremLipsum.length();
        String s;
        for (int i=0; i<5000; i++) {
            int x1 = (int) (Math.random() * (x*3));
            int x2 = (int) (Math.random() * x1);
            int x3 = x1 + ((int) (Math.random() * x*2));
            s = getDummyText(x1, x2, x3);
            System.out.printf("%d) %d,%d,%d=%d => %s \n", i, x1,x2,x3, s.length(), OAString.format(s, "120l."));
            
        }
    }
    
}
