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
package com.viaoa.web;

import java.awt.Color;
import java.util.*;
import com.viaoa.hub.*;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.util.*;


/**
 * Utility class used by JSP components.
 * @author vvia
 *
 */
public class JspUtil {
    public final static String idPrefix = "OAJsp";
    

    /** given a componentName and an Object, a new name is returned with the object Id encoded */
    public static String getEncodedName(String compName, Hub hub, Object obj) {
        // OAid4_name_2_id
        String id = getObjectIdAsString(hub, obj);

        if (id == null) return null;
        return idPrefix + compName.length() + "_" + compName + "_" + id.length() + "_" + id;
    }

    public static String getEncodedName(String compName, Hub hub, Object obj, Hub hub2, Object obj2) {
        // OAid4_name_3_idA_3_idB
        String id = getObjectIdAsString(hub,obj);
        if (id == null) return null;

        String name = idPrefix + compName.length() + "_" + compName + "_" + id.length() + "_" + id;
        id = getObjectIdAsString(hub2,obj2);
        name +=  "_" + id.length() + "_" + id;
        return name;
    }

    public static boolean isEncodedName(String nameUsed) {
        // OAid4_name_2_id
        return nameUsed.startsWith(idPrefix);
    }
    /** get the component name from an encoded name. Used by OAForm.processRequest() to get component name. */
    public static String getDecodedComponentName(String nameUsed) {
        // OAid4_name_2_id
        if (!nameUsed.startsWith(idPrefix)) return nameUsed;
        try {
            String s = com.viaoa.html.Util.field(nameUsed,'_',1);  //  = "OAid4"
            int len = Integer.parseInt(s.substring(idPrefix.length()));  // = 4
            int start = s.length() + 1; // = 6
            nameUsed = nameUsed.substring(start,  start + len);  // = "name"
        }
        catch (NumberFormatException e) {
        }
        return nameUsed;
    }

    /** get the id from an Id encoded name 
        @param nameUsed created by getEncodedName()

        @return object or null if encoding was not done on nameUsed
    */
    public static Object getDecodedObject(String nameUsed, Hub hub) {
        return getDecodedObject(nameUsed, hub, 0);
    }
    public static Object getDecodedObject(String nameUsed, Hub hub, int cnt) {
        // OAid4_name_2_id_3_idb
        if (hub == null || !nameUsed.startsWith(idPrefix)) return null;
        String id = null;
        try {
            String s = com.viaoa.html.Util.field(nameUsed,'_',1); // = "OAid4"
            int len = Integer.parseInt(s.substring(idPrefix.length())); // = 4
            // skip comp name
            int start = s.length() + 1 + len + 1; // = 10
            nameUsed = nameUsed.substring(start); // "2_id_3_idb"

            for (int i=0; i<=cnt && nameUsed.length() > 0; i++) {
                id = null;
                s = com.viaoa.html.Util.field(nameUsed,'_',1);   // "2"
                len = Integer.parseInt(s); // = 2
                start = s.length() + 1;
                id = nameUsed.substring(start, start+len);
                if (i == cnt) break;
                if ((start + len + 1) < nameUsed.length()) nameUsed = nameUsed.substring(start + len + 1);
                else nameUsed = "";
            }
        }
        catch (NumberFormatException e) {
        }
        if (id == null) return null;
        if (id.equals(idPrefix+"NULL")) return null;
        return getObject(hub, id);
    }


//qqqqqqqqqq Use ObjectKey for this    
    /** returns the Objects first propertyId as a string */
    public static String getObjectIdAsString(Hub hub, Object obj) {
        if (obj == null) return idPrefix+"NULL";

        if ( !(obj instanceof OAObject) ) {
            return idPrefix+hub.getPos(obj);
        }

        if ( ((OAObject)obj).isNew()) {
            if (hub == null) return null;
            return idPrefix+hub.getPos(obj);
        }
        
        OAObjectInfo oi = Hub.getOAObjectInfo(obj.getClass());
        String[] ids = oi.getIdProperties();
        if (ids.length == 0) return null;
                    
        try {
            Object object = OAObjectReflectDelegate.getProperty((OAObject)obj, ids[0]);
            return OAConverter.toString(object);
        }
        catch (Exception e) {
	        throw new RuntimeException("Util.getObjectId() "+e);
        }
    }

    /** using a string that was created by "getObjectIdAsString()", try to find 
        object in hub */
    public static Object getObject(Hub hub, String strId) {
        if (hub == null || strId == null) return null;
        if (strId.equals(idPrefix+"NULL")) return null;
        Class c = hub.getObjectClass();
        if (c == null) return null;

        if (strId.startsWith(idPrefix)) {
            int len = Integer.parseInt(strId.substring(idPrefix.length()));
            return hub.elementAt(len);
        }
        
        
        if (!hub.isOAObject()) return strId;
        
        /*
        OAObjectInfo oi = Hub.getOAObjectInfo(c);

        String[] ids = oi.getIdProperties();
        Method[] methods = new Method[ids.length]; 
        for (int i=0; i<methods.length; i++) {
            methods[i] = OAReflect.getMethod(c, "get"+ids[i]);
        }
        c = methods[0].getReturnType();
        Object obj = OAConverter.convert(c, strId);
        return OAObjectCacheDelegate.get(hub.getObjectClass(), obj);
        */
       
        return OAObjectCacheDelegate.get(hub.getObjectClass(), strId);
    }

    public static String field(String str, char sep, int beg) {
        return field(str,sep+"",beg,1);
    }
    public static String field(String str, char sep, int beg, int amt) {
        return field(str,sep+"",beg,amt);
    }
    public static String field(String str, String sep, int beg) {
        return field(str,sep,beg,1);
    }
    /** same as pick fld()
        @param str String to parse
        @param sep seperator wihin str
        @param beg field to find, where first field is 1
        @param amt number of fields to return, -1 for all after the beg
     */
    public static String field(String str, String sep, int beg, int amt) {
        if (str == null) return null;
        if (sep == null || sep.length() == 0) return null;
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
 
    
    /* replaced with OAHtmlUtil
    public static String toExcapeString(String value) {
        return htmlEscape(value);
    }

    / ** converts null to "" and other html conversions for &lt;, &gt; &amp; &quot; &#39; * /
    public static String htmlEscape(String value) {
        if (value == null) return "";
        value = convert(value, '&', "&amp;");
        value = convert(value, '"', "&#34;");  // &quot;
        value = convert(value, '\'', "&#39;"); // &apos;  not yet supported
        value = convert(value, '<', "&lt;");
        value = convert(value, '>', "&gt;");
        return value;
    }
    
    public static String smartEscapeHtml(String text) {
        if (text == null || text.length() == 0) return text;

        int cnt1 = 0;
        int cnt2 = 0;
        
        int x = text.length();
        for (int i=0; i<x; i++) {
            char c = text.charAt(i);
            if (c == '<') cnt1++;
            else if (c == '>') cnt2++;
        }
        if (cnt1 > 0 && cnt1 == cnt2) return text;  /// assuming it's ok
        return htmlEscape(text);
    }
*/    

    public static String convert(String value, char c, String replace) {
        return convert(value, c+"", replace);
    }
        
    public static String convertIgnoreCase(String line, String search, String replace) {
        return convert(line,search,replace,true);
    }
    public static String convert(String line, String search, String replace) {
        return convert(line,search,replace,false);
    }

    public static String convert(String line, String search, String replace, boolean bIgnoreCase) {
        if (line == null || replace == null || search == null || search.length() == 0) return line;

        int xs = search.length();
        if (xs == 0) return line;
        if (bIgnoreCase) search = search.toLowerCase();
        
        int xr = replace.length();
        int xl = line.length();
        StringBuffer sb = null;  // dont allocate until first match is found
        char c=0, origChar=0;
        for (int i=0,j=0; ;i++) {
 
            if (i < xl) {
                origChar = c = line.charAt(i);
                if (bIgnoreCase) c = Character.toLowerCase(c);
                if (c == search.charAt(j)) {
                    j++;
                    if (j == xs) {
                        if (sb == null) {
                            sb = new StringBuffer(xl + (xl/10));
                            int e = (i - j) + 1;
                            if (e > 0) sb.append(line.substring(0,e));
                        }
                        
                        if (xr > 0) sb.append(replace);
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
i: 0123456789
   VinceViNce   
j:      12
*/   
                    sb.append(line.substring(b,b+j));
                }
                j = 0;
            }
            if (i >= xl) break;
            if (sb != null) sb.append(origChar);
        }
        if (sb == null) return line;
        return new String(sb);
    }
   

    public static String hiliteIgnoreCase(String line, String search, String beginTag, String endTag) {
        return hilite(line,search,beginTag, endTag,true);
    }
    public static String hilite(String line, String search, String beginTag, String endTag) {
        return hilite(line,search,beginTag, endTag,false);
    }

    public static String hilite(String line, String search, String beginTag, String endTag, boolean bIgnoreCase) {
        if (line == null || search == null) return line;

        int xs = search.length();
        if (xs == 0) return line;
        if (bIgnoreCase) search = search.toLowerCase();
        
        int xl = line.length();
        StringBuffer sb = null;  // dont allocate until first match is found
        char c=0, origChar=0;
        for (int i=0,j=0; ;i++) {
 
            if (i < xl) {
                origChar = c = line.charAt(i);
                if (bIgnoreCase) c = Character.toLowerCase(c);
                if (c == search.charAt(j)) {
                    j++;
                    if (j == xs) {
                        if (sb == null) {
                            sb = new StringBuffer(xl + (xl/10));
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
                j = 0;
            }
            if (i >= xl) break;
            if (sb != null) sb.append(origChar);
        }
        if (sb == null) return line;
        return new String(sb);
    }
   

    
/* * 20171017 removed, replaced with OAJsUtil.java  OAHtmlUtil.java   
    
    public static String toString(OAObject obj, String prop, int value) {
        return toString(obj,prop,new Integer(value));
    }
    public static String toString(OAObject obj, String prop, float value) {
        return toString(obj,prop,new Double(value));
    }
    public static String toString(OAObject obj, String prop, double value) {
        return toString(obj,prop,new Double(value));
    }
    public static String toString(OAObject obj, String prop, Object value) {
        if (value == null) return "";
        if (obj != null && prop != null && obj.isNull(prop)) return "";
        return smartEscapeHtml(OAConverter.toString(value));
    }
    public static String toString(Object value) {
        if (value == null) return "";
        return smartEscapeHtml(OAConverter.toString(value));
    }
*/    

    public static String convertForFreeText(String keyword) {
        if (keyword == null) return "";
        String s;

        // parse keyword:   a '**' needs to be converted to FORMSOF(...)
        String newKeyword = "";
        keyword = keyword.trim();
        char quoteChar = 0;
        boolean convertFlag = true, andFlag=false;

        String sep = "(), \"&";
        StringTokenizer st = new StringTokenizer(keyword, sep, true);
        String hold="";       
        while (st.hasMoreTokens()) {
            s = st.nextToken();
            
            if (quoteChar != 0) {  // in quotes
                hold += s;
                if (s.charAt(0) == quoteChar) {
                    s = hold;
                    quoteChar = 0;
                }
                else continue;
            }
            
            if (s.equals("\"")) {
                if (quoteChar == 0) {
                    quoteChar = s.charAt(0);
                    hold = s;
                    continue;
                }
            }
            else if (s.equals("&")) {
                s = "AND";
            }
            else if (s.indexOf("**") >= 0) {
                s = s.substring(0,s.indexOf("**"));
                s = "FORMSOF(INFLECTIONAL,\""+s+"\")";
            }

            if (s.equalsIgnoreCase("and") || s.equalsIgnoreCase("or") || s.equalsIgnoreCase("not") || s.equalsIgnoreCase("near")) {
                andFlag = false;
                convertFlag = false;  // user is using keywords already, we wont need to convert them
            }
            
            if (s.indexOf('\'') > 0 || s.indexOf('*') > 0) {
                s = com.viaoa.html.Util.convert(s,"'","''");
                if (s.indexOf('\"') < 0) s = '"' + s + '"';
            }
            
            if (andFlag) {
                newKeyword += "AND ";
                andFlag = false;
            }
            newKeyword += s;
//System.out.println("'"+s+ "'  -> '"+newKeyword+"'");//qqqqqqqqqq
            
            if (convertFlag && (s.equals(" ") || s.equals(",")) && !s.equals("(") ) {
                // need to add "AND" if the next word is not AND
                andFlag = true;
            }

        }
//System.out.println("returning  -> '"+newKeyword+"'");//qqqqqqqqqq
        return newKeyword;
    }

    public static String formatWidth(String msg, int width) {
        // try to break a long text into width of size "width"
        // remove ending spaces
        // convert tabs to 4 spaces
        if (msg == null) msg = "";


        // first find how many leading spaces are in all
        StringTokenizer st = new StringTokenizer(msg, "\n\r", false);
        int leadingSpaceCount = 99; // remove all leading spaces that are same for all lines
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            int x = line.length();
            if (x == 0) continue;
            int i=0, j=0;
            for ( ; i<x; i++, j++) {
                if (line.charAt(i) == '\t') j += 3;
                else if (line.charAt(i) != ' ') break;
            }
            if (j != x) { // all spaces
                if (j < leadingSpaceCount) leadingSpaceCount = j;
            }
        }
        if (leadingSpaceCount == 99) leadingSpaceCount = 0;
        
        boolean bCr = msg.indexOf('\r') > 0;
        boolean bLf = msg.indexOf('\n') > 0;
        if (!bCr && !bLf) bLf = true;
        
        st = new StringTokenizer(msg, "\n\r", true); // need to return seperators since there could be blank lines
        StringBuffer sb = new StringBuffer(msg.length() + 64);
        boolean flag = false;
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            char c = line.charAt(0);
            if (c == '\n' && bCr) continue;   // ignore new line char
            if (c == '\r' || c == '\n') {
                if (!flag) {
                    flag = true;  // if next token is CR then enter a blank line
                    continue;
                }
                line = " "; // print blank line
            }
            flag = false;
            int startPos = 0;
            int x = line.length();
            // remove trailing '\t' and ' '
            int i = x;
            for ( ; i > 0; i--) {
                c = line.charAt(i-1);
                if (c != ' ' &&  c != '\t') break;
            }
            if (i != x) line = line.substring(0, i);

            x = line.length();
            if (x > leadingSpaceCount) startPos = leadingSpaceCount;
            // fun time: break up into chunks of size "width", breaking on spaces
            StringTokenizer st2 = new StringTokenizer(line, " \t", true);
            
            int skip = 0;
            StringBuffer sb2 = new StringBuffer(line.length() + 32);
            while (st2.hasMoreTokens()) {
                String word = st2.nextToken();
                int wordLen = word.length();
                if (word.charAt(0) == '\t') wordLen += 3;
                
                if ( (sb2.length() + wordLen > width) && sb2.length() > 0) {
                    sb.append(sb2);
                    sb2.setLength(0);
                    if (bCr) sb.append('\r');
                    if (bLf) sb.append('\n');
                    skip = 0;
                    if (word.charAt(0) == '\t' || word.charAt(0) == ' ') continue; // dont start line with tab or spaces
                }

                if (word.charAt(0) == '\t') word = "    ";
                if (skip < leadingSpaceCount) {
                    if (skip + word.length() > leadingSpaceCount) {
                        word = word.substring(leadingSpaceCount - skip);
                        skip = leadingSpaceCount;
                    }
                    else {
                        skip += word.length();
                        continue;
                    }
                }
                sb2.append(word);
            }
            sb.append(sb2);
            if (bCr) sb.append('\r');
            if (bLf) sb.append('\n');
        }        
        return new String(sb);    
    }
    
    public static String format(String s, int cols, boolean bPad) {
    	if (s == null) s = "";
    	if (cols < 1) return s;
        if (cols > 0) {
            int j = s.length();
            if (j > cols) {
                s = s.substring(0, cols-1) + "...";
            }
            else {
                //for ( ; j<cols;j++) s += " ";
                for ( ;bPad && j<cols;j++) s += "&nbsp;";
            }
        }
        return s;
    }
    
    /**
     * Converts color to rgb value
     * @param color
     * @return rgb(r#,g#,b#)
     */
    public static String convertToCss(Color color) {
       if (color == null) return null;
       int r = color.getRed();
       int g = color.getGreen();
       int b = color.getBlue();
       String s = "rgb(" + r + "," + g + "," + b + ")"; 
       return s;
    }
}

