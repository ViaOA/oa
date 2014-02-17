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
package com.viaoa.jfc.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

import com.viaoa.hub.*;
import com.viaoa.object.*;
import com.viaoa.util.*;

/**
 *  Converts html text with custom properties and processing tags into pure html text, by
 *  using a supplied list of values to plug into the text.
  
 *  <ul>Tags that are supported:  
 *  <li><%=prop[,width||fmt]%>  to use value from OAProperties, or one of the values from setProperty()
 *  <li><%=foreach [prop]%>  to loop through a list of values (hub elements)
 *  <li><%=end%>
 *  <li><%=if prop%>  true if value is not null and length > 0, is 0 or false
 *  <li><%=end%>
 *  <li><%=ifnot prop%>  true if value is not null and length > 0
 *  <li><%=end%>
 *  <li><%=ifequals prop "value to match"%>
 *  <li><%=end%>
 *  <li><%=format[X],'12 L'%>  where X can be used as a unique identifier, so that there can be multiple embedded formats.
 *  <li><%=end%>
 *  <li><%=include name%> include another file in the same directory   ex: <%=include include%>
 *  </ul>
 *  
 *  <ul>Aggregate commands, works with current/most recent "foreach"
 *  <li><%=#counter [propName], fmt%> current counter  
 *  <li><%=#sum [propName], propName fmt%> sum of listed properties  
 *  <li><%=#count [propName], fmt%> count of listed properties  
 *  </ul>
 *  
 *  
 *  Other special tag attributes:
 *  <tr header='true'>  used by first row of a table, that will be printed as heading when table spans multiple pages.
 *  <div pagebreak='no'>  block tag to disable page breaks.
 *  
 *  
 *  OAHTMLReport will internally set values for $DATE, $TIME, $PAGE parameters
 *  <br>
 * The html code uses special tags "<%= ? %>", where "?" is the name of the Property name, or object property path.
 * By using setProperties and setObject, you can set the root object where the data is retrieved from.
 * NOTE: Use a "$" prefix (ex: $PAGE) for tag names, to have them use the value from the setProperties name/value pairs.
 * Otherwise, the value of the tag will be taken from the object, using the name as the property path.
 */
public class OAHTMLConverter {

    private static Logger LOG = Logger.getLogger(OAHTMLConverter.class.getName());
    private Properties propInternal;
    private TreeNode rootTreeNode;
    private String htmlTemplate;

    public OAHTMLConverter() {
    }
    public OAHTMLConverter(String htmlTemplate) {
        setHtmlTemplate(htmlTemplate);
    }

    public void setHtmlTemplate(String htmlTemplate) {
        this.htmlTemplate = htmlTemplate;
        this.rootTreeNode = null;
    }
    public String getHtmlTemplate() {
        return this.htmlTemplate;
    }
    
    public String getHtml(OAObject objRoot, OAProperties props) {
        return getHtml(objRoot, null, props);
    }
    public String getHtml(Hub hubRoot, OAProperties props) {
        return getHtml(null, hubRoot, props);
    }
    
    protected String getHtml(OAObject objRoot, Hub hubRoot, OAProperties props) {
        setProperty("DATE", new OADate());
        setProperty("TIME", new OATime());

        if (rootTreeNode == null) {
            rootTreeNode = createTree(htmlTemplate);
        }
        
        StringBuffer sb = new StringBuffer(1024 * 4);
        generateHTML(rootTreeNode, objRoot, hubRoot, sb, props);
        String s = new String(sb);
        sb = null;
        return s;
    }

    /**
     * Internal properties, used in conjunction with OAProperties param from getHtml()
     */
    public void setProperty(String name, Object value) {
        if (name == null) return;
        if (propInternal == null) propInternal = new Properties();
        if (value == null) propInternal.remove(name);
        else propInternal.put(name, value);
    }
    
    protected TreeNode createTree(String doc) {
        if (doc == null) doc = "";
        TreeNode root = new TreeNode();
        String html = preprocess(doc);
        if (html.indexOf("&lt;%=") >= 0) {
            html = OAString.convert(html, "&lt;%=", "<%=");
            html = OAString.convert(html, "%&gt;", "%>");
        }
        alToken = parseTokens(html);
        posToken = 0;
        parse(root);
        return root;
    }
    
    protected String preprocess(String doc) {
        return preprocess(doc, null);
    }
    protected String preprocess(String doc, ArrayList<String> alInclude) {
        if (alInclude == null) alInclude = new ArrayList<String>();

        int pos = 0;
        for (;;) {
            int posHold = pos;
            pos = doc.indexOf("<%=include ", pos);
            if (pos < 0) break;
            int pos1 = doc.indexOf(" ", pos) + 1;
            int pos2 = doc.indexOf("%>", pos1);
            if (pos2 < 0) {
                LOG.warning("Error: missing end tag %>");
                break;
            }
            String text = doc.substring(pos1, pos2).trim();
            if (alInclude.contains(text)) {
                text = " ERROR: recursive include for " + text + " ";
            }
            else {
                alInclude.add(text);
                text = getIncludeText(text);
            }
            if (pos > 0) {
                doc = doc.substring(0, pos) + text + doc.substring(pos2 + 2);
            }
        }
        return doc;
    }

    /**
     * This is called to get the text for any include tags.
     * By default, it will return an error message.
     */
    protected String getIncludeText(String name) {
        return " ERROR: no text for include " + name + " ";
    }

    // 20130327 created new parser, so that tags could use <%=end%> instead of matching named end tag.    


 // qqqqq set up for ...    
 // qqqqqqqqqqqqqqq TagType.Command     
    
    
    // descendant parser for html <%= xxx %> tags
    protected void parse(TreeNode root) {
        for (;;) {
            TreeNode node = new TreeNode();
            root.alChildren.add(node);
            Token tok = getNextToken();
            if (tok == null) break;
            parseA(tok, node);
        }
    }

    private void parseA(Token tok, TreeNode node) {
        if (tok.hasEndToken()) {
            Token tokB = parseB(tok, node);
            if (tokB == null || tokB.tagType == null || tokB.tagType != TagType.End) {
                node.errorMsg = "Error: missing end tag for " + tok.data;
            }
        }
        else if (tok.tagType == null) {
            node.arg1 = tok.data;
        }
        else if (tok.tagType == TagType.GetProp) {
            node.tagType = TagType.GetProp;
            String s = OAString.field(tok.data, ",", 1).trim();
            node.arg1 = s;
            String fmt = OAString.field(tok.data, ",", 2, 99);
            if (!OAString.isEmpty(fmt)) {
                fmt = fmt.trim();
                fmt = OAString.convert(fmt, '\'', "");
                fmt = OAString.convert(fmt, '\"', "");
                node.arg2 = fmt;
            }
        }
        if (tok.tagType != TagType.Command) return;

        String s = OAString.field(tok.data, ",", 1).trim();
        if (s == null) {
            s = tok.data;
            if (s == null) s = "";
        }
        String s1 = OAString.field(s, " ", 2);
        if (s1 == null) s1 = "";
        s = OAString.field(s, " ", 1);
        
        String fmt = OAString.field(tok.data, ",", 2, 99); // fmt
        if (fmt == null) fmt = "";
        else {
            fmt = fmt.trim();
            fmt = OAString.convert(fmt, '\'', "");
            fmt = OAString.convert(fmt, '\"', "");
        }
      
        if (s.equalsIgnoreCase("#counter")) {
            node.tagType = TagType.Counter;
        }
        else if (s.equalsIgnoreCase("#count")) node.tagType = TagType.Count;
        else if (s.equalsIgnoreCase("#sum")) node.tagType = TagType.Sum;

        node.arg1 = s1;  // name
        node.arg2 = fmt;
    }

    // if token has an end token
    private Token parseB(Token tok, TreeNode node) {
        if (tok.tagType == TagType.Format) {
            node.tagType = TagType.Format;
            String fmt = OAString.field(tok.data, ",", 2, 99);
            if (fmt == null) fmt = "";
            fmt = fmt.trim();
            fmt = OAString.convert(fmt, '\'', "");
            fmt = OAString.convert(fmt, '\"', "");
            node.arg1 = fmt;
        }
        else if (tok.tagType == TagType.ForEach) {
            node.tagType = TagType.ForEach;
            node.arg1 = OAString.field(tok.data, " ", 2);
            if (node.arg1 == null) node.arg1  = "";
        }
        else if (tok.tagType == TagType.IfNot) {
            node.tagType = TagType.IfNot;
            node.arg1 = OAString.field(tok.data, " ", 2);
        }
        else if (tok.tagType == TagType.IfNotEquals) {
            node.tagType = TagType.IfNotEquals;
            node.arg1 = OAString.field(tok.data, " ", 2);
            node.arg2 = OAString.field(tok.data, " ", 3);
        }
        else if (tok.tagType == TagType.If) {
            node.tagType = TagType.If;
            node.arg1 = OAString.field(tok.data, " ", 2);
        }
        else if (tok.tagType == TagType.IfEquals) {
            node.tagType = TagType.IfEquals;
            node.arg1 = OAString.field(tok.data, " ", 2);
            node.arg2 = OAString.field(tok.data, " ", 3);
        }

        // go to end tag
        TreeNode nodex = new TreeNode();
        node.alChildren.add(nodex);
        Token tokx = parseC(tok, nodex);

        return tokx;
    }

    // process to the end tag
    private Token parseC(Token tok, TreeNode node) {
        Token tokX;
        for (;;) {
            tokX = getNextToken();
            if (tokX == null || (tokX.tagType != null && tokX.tagType == TagType.End)) {
                break;
            }
            TreeNode nodex = new TreeNode();
            node.alChildren.add(nodex);
            parseA(tokX, nodex);
        }
        return tokX;
    }

    private ArrayList<Token> alToken;
    private int posToken;

    static enum TagType {
        GetProp,     // arg1=prop, arg2=fmt 
        Format,      // arg1=fmt
        If,          // arg1=prop
        IfNot,       // arg1=prop
        IfEquals,    // arg1=prop, arg2=value
        IfNotEquals, // arg1=prop, arg2=value
        ForEach,     // arg1=prop
        Equals,      // arg1=prop, arg2=value
        NotEquals,   // arg1=prop, arg2=value
        End,        
        Command,     // arg1=prop
        Counter,     // arg1=prop, arg2=fmt
        Count,       // arg1=prop, arg2=fmt
        Sum          // arg1=prop, arg2=prop, arg3=fmt
    }

    static class TreeNode {
        TagType tagType;
        String arg1, arg2, arg3;
        String errorMsg;
        ArrayList<TreeNode> alChildren = new ArrayList<TreeNode>(5);
    }

    static class Token {
        String data;
        TagType tagType;
        boolean missingEnd;
        public boolean hasEndToken() {
            boolean b;
            if (tagType != null) {
                b = (tagType == TagType.Format || tagType == TagType.If || tagType == TagType.IfNot
                        || tagType == TagType.IfNotEquals || tagType == TagType.ForEach
                        || tagType == TagType.Equals || tagType == TagType.NotEquals);
                        
            }
            else b = false;
            return b;
        }
    }

    private Token getNextToken() {
        int x = alToken.size();
        if (posToken >= x) return null;
        Token t = alToken.get(posToken++);
        return t;
    }

    protected ArrayList<Token> parseTokens(String doc) {
        ArrayList<Token> alToken = new ArrayList<OAHTMLConverter.Token>();
        int pos = 0;
        for (;;) {
            int posHold = pos;
            pos = doc.indexOf("<%=", pos);
            if (pos < 0) {
                if (posHold < doc.length()) {
                    Token tok = new Token();
                    alToken.add(tok);
                    tok.data = doc.substring(posHold);
                }
                break; // done
            }

            Token tok = new Token();
            alToken.add(tok);

            int pos2 = doc.indexOf("%>", pos + 3);
            if (pos2 < 0) {
                tok.missingEnd = true;
                tok.data = doc.substring(pos);
                break;
            }

            if (posHold < pos) {
                tok.data = doc.substring(posHold, pos);
                tok = new Token();
                alToken.add(tok);
            }

            String tag = doc.substring(pos + 3, pos2);
            String tag2 = doc.substring(pos + 3, pos2+1);

            
            pos2 += 2; // after %>
            tag = OAString.trimWhitespace(tag);
            tok.data = tag;
            tag2 = OAString.trimWhitespace(tag2);

            pos = pos2;

            tag = tag.toLowerCase();
            tag2 = tag2.toLowerCase();
            
            
            if (tag.startsWith("#")) {
                tok.tagType = TagType.Command;
            }
            else if (tag2.startsWith("end %")) {
                tok.tagType = TagType.End;
            }
            else if (tag2.startsWith("end%")) {
                tok.tagType = TagType.End;
            }
            else if (tag2.contains("end%")) {
                tok.tagType = TagType.End;
            }
            else if (tag2.contains("end ")) {
                tok.tagType = TagType.End;
            }
            else if (tag.startsWith("format ")) {
                tok.tagType = TagType.Format;
            }
            else if (tag.startsWith("foreach")) {
                tok.tagType = TagType.ForEach;
            }
            else if (tag.startsWith("ifnot ")) {
                tok.tagType = TagType.IfNot;
            }
            else if (tag.startsWith("if ")) {
                tok.tagType = TagType.If;
            }
            else if (tag.startsWith("ifequals ")) {
                tok.tagType = TagType.IfEquals;
            }
            else if (tag.startsWith("ifnotequals ")) {
                tok.tagType = TagType.IfNotEquals;
            }
            else { // get property value
                tok.tagType = TagType.GetProp;
            }
        }
        return alToken;
    }

    private HashMap<String, Integer> hmForEachCounter = new HashMap<String, Integer>();
    
    protected void generateHTML(TreeNode rootNode, OAObject obj, Hub hub, StringBuffer sb, OAProperties props) {
        boolean bNot = false;
        boolean bProcessChildren = true;

        if (rootNode.errorMsg != null) {
            sb.append(rootNode.errorMsg);
        }
        if (rootNode.tagType == null) {
            String s = rootNode.arg1;
            if (!OAString.isEmpty(rootNode.arg2)) {
                s = OAString.format(s, rootNode.arg2);
            }
            if (s != null) sb.append(s);
        }
        else {
            switch (rootNode.tagType) {
            case ForEach:
                bProcessChildren = false;
                Object objValue;
                if (obj != null && !OAString.isEmpty(rootNode.arg1)) objValue = this.getProperty(obj, rootNode.arg1);
                else objValue = hub;

                if (objValue instanceof Hub) {
                    Hub h = (Hub) objValue;
                    for (int i = 0;; i++) {
                        hmForEachCounter.put(rootNode.arg1, i+1);
                        OAObject oa = (OAObject) h.elementAt(i);
                        if (oa == null) break;
                        for (TreeNode dn : rootNode.alChildren) {
                            generateHTML(dn, oa, hub, sb, props);
                        }
                    }
                }
                else {
                    if (obj != null) {
                        LOG.warning("Hub for 'Foreach' not found");
                    }
                }
                break;

            case Format:
                bProcessChildren = false;

                StringBuffer sbHold = sb;
                sb = new StringBuffer(1024 * 4);

                for (TreeNode dn : rootNode.alChildren) {
                    generateHTML(dn, obj, hub, sb, props);
                }

                String s = new String(sb);
                s = OAString.format(s, rootNode.arg1);
                s = OAString.convert(s, " ", "&nbsp;");
                sb = sbHold;
                sb.append(s);

                break;

            case IfNot:
                bNot = true;
            case If:
                // if not null, blank or 0.0
                s = getValue(obj, rootNode.arg1, 0, null, props);

                bProcessChildren = false;
                if (s != null) {
                    if (s.length() > 0) {
                        if (OAString.isNumber(s)) {
                            bProcessChildren = (OAConv.toDouble(s) != 0.0);
                        }
                        else {
                            // bProcessChildren = OAConv.toBoolean(s);
                            if (s == null || s.length() == 0) {
                                bProcessChildren = false;
                            }
                            else {
                                if (s.equalsIgnoreCase("false")) bProcessChildren = false;
                                else bProcessChildren = true;
                            }
                        }
                    }
                }
                if (bNot) bProcessChildren = !bProcessChildren;
                break;

            case IfEquals:
                int x = rootNode.arg1.indexOf(' ');
                String prop;
                String match;
                if (x > 0) {
                    prop = rootNode.arg1.substring(0, x);
                    match = rootNode.arg1.substring(x + 1);
                }
                else {
                    prop = rootNode.arg1;
                    match = null;
                }

                s = getValue(obj, prop, 0, null, props);

                bProcessChildren = (s == match) || (s != null && s.equals(match));
                break;

            case GetProp:
                prop = rootNode.arg1;
                String fmt = rootNode.arg2;

                int width = 0;
                if (!OAString.isEmpty(fmt)) {
                    if (OAString.isNumber(fmt)) {
                        width = OAConv.toInt(fmt);
                        fmt = null;
                    }
                    else {
                        fmt = fmt.trim();
                        fmt = OAString.convert(fmt, '\'', "");
                        fmt = OAString.convert(fmt, '\"', "");
                    }
                }

                s = getValue(obj, prop, width, fmt, props);
                sb.append(s);
                break;
                
            case Counter:
                prop = rootNode.arg1;  // from open forEach loop
                fmt = rootNode.arg2;
                Integer ix = hmForEachCounter.get(prop);
                if (ix == null) sb.append("Error: "+prop+".counter not valid");
                else {
                    s = ix.toString();
                    if (!OAString.isEmpty(fmt)) {
                        s = OAString.format(s, fmt);
                    }
                    sb.append(s);
                }
                break;
            case Count:
                prop = rootNode.arg1;
                fmt = rootNode.arg2;
                if (obj == null) break;
                Object objx = obj.getProperty(prop);
                if (!(objx instanceof Hub)) return;
                s = OAConv.toString( ((Hub) objx).getSize(), fmt);
                sb.append(s);
                break;
            case Sum:
                prop = rootNode.arg1;
                String prop2 = rootNode.arg2;
                fmt = rootNode.arg3;
                if (obj == null) break;
                objx = obj.getProperty(prop);
                if (!(objx instanceof Hub)) return;
                double d = 0.0d;
                for (Object objz : ((Hub) objx)) {
                    if (!(objz instanceof OAObject)) continue;
                    objx = ((OAObject)objz).getProperty(prop2);
                    if (!(objx instanceof Number)) continue;
                    d += OAConv.toDouble(objx);
                }
                s = OAConv.toString(d, fmt);
                sb.append(s);
                break;
            }
        }
        if (bProcessChildren && rootNode.alChildren != null) {
            for (TreeNode dn : rootNode.alChildren) {
                generateHTML(dn, obj, hub, sb, props);
            }
        }
    }

    /**
     * Called to get the value of a property.
     * @param obj Object parameter from getHtml()
     * @param name name of property parsed between <%=XX%> parameters.
     * @return
     */
    protected String getValue(OAObject obj, String propertyName, int width, String fmt, OAProperties props) {
        if (propertyName == null) return "";
        String result = null;

        boolean bFmt = true;
        if (propertyName.startsWith("$")) {
            if (propertyName.length() > 1) propertyName = propertyName.substring(1);

            if (fmt != null && fmt.length() > 0) {
                Object objx = null;
                if (props != null) objx = props.get(propertyName);
                if (objx == null) {
                    if (propInternal != null) objx = propInternal.get(propertyName);
                }
                if (objx != null) {
                    if (objx instanceof OADateTime) {
                        result = ((OADateTime) objx).toString(fmt);
                        bFmt = false;
                    }
                    else {
                        if (objx != null) result = objx.toString();
                    }
                }

            }
            else {
                if (props != null) result = props.getString(propertyName);
                if (result == null) {
                    if (propInternal != null) {
                        Object objx = propInternal.get(propertyName);
                        if (objx == null) result = null;
                        else result = objx.toString();
                    }
                }
            }
        }
        else {
            if (obj != null && propertyName.length() > 0) {
                Object objx;
                if (obj != null) objx = this.getProperty(obj, propertyName);
                else objx = null;
                if (objx instanceof Boolean && fmt != null && fmt.indexOf(';') >= 0) {
                    result = OAConv.toString(objx, fmt);
                    bFmt = false;
                }
                else {
                    result = OAConv.toString(objx);

                    // if not html, then convert [lf] to <br>
                    boolean b = true;
                    if (result.indexOf('<') >= 0 && result.indexOf('>') >= 0) {
                        String s = result.toLowerCase();
                        if (s.indexOf("<p") >= 0 || s.indexOf("<span") >= 0 || s.indexOf("<b") >= 0 || s.indexOf("<i") >= 0) {
                            b = false;
                        }
                    }

                    if (b && result.indexOf("\n") >= 0) {
                        result = OAString.convert(result, "\r\n", "<br>");
                        result = OAString.convert(result, "\n", "<br>");
                    }
                }
            }
        }
        if (result == null) result = "";
        if (width > 0) result = OAString.lineBreak(result, width);

        if (bFmt && fmt != null && fmt.length() > 0) {
            result = OAString.format(result, fmt);
            result = OAString.convert(result, " ", "&nbsp;");
        }

        return result;
    }

    /**
     * Method that is called to get an object property value.
     * @param obj object that is currently active.  Either the report object or the object in foreach loop.
     * @param props set of properties passed to report
     */
    protected Object getProperty(OAObject oaObj, String propertyName) {
        if (oaObj == null) return null;
        return oaObj.getProperty(propertyName);
    }

    
    public static void main(String[] args) throws Exception {
        //String s = OAFile.readTextFile("/projects/java/vetplan/src/com/vetplan/report/PetMedicalSummaryReport.html", 1024*8);
        String s = OAFile.readTextFile("C:\\projects\\java\\hifive\\src\\com\\tmgsc\\hifive\\report\\html\\oa\\test.html", 1024 * 8);
        OAHTMLConverter c = new OAHTMLConverter(s);
        c.getHtml(null, null, null);
    }
}
