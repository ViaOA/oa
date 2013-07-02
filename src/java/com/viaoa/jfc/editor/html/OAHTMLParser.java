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
package com.viaoa.jfc.editor.html;

/** 
    Parse and "clean up" HTML code.
    Currently used by Editor to convert pasted code from Microsoft Word.
    This needs to be set up to convert styles to HTML tags.
*/
public class OAHTMLParser {
    HTMLTokenManager tokenManager;
    HTMLToken token;

    public static final int EOF = 1;
    public static final int VARIABLE = 2;
    public static final int GT = 3;
    public static final int LT = 4;
    public static final int EQUAL = 5;
    public static final int STRING = 6;
    public static final int SQ = 7; // single quote
    public static final int DQ = 8; // double quote
    public static final int SLASH = 9; // "/>"
    public static final int COMMENT = 10; 


    public OAHTMLParser() {
        tokenManager = new HTMLTokenManager();
    }

    public boolean isMicrosoft(String code) {
        return (code != null && code.toLowerCase().indexOf("urn:schemas-microsoft-com") >= 0);
    }

    /**
        Removes all code outside and including html body tags, and leading white space and leading paragraphs
    */
    public static String removeBody(String code) {
        if (code == null) return null;

        String lcode = code.toLowerCase();
        
        int beginPos = 0;
        int endPos = code.length();
        
        String tag = "body";
        beginPos = lcode.indexOf("<"+tag);
        if (beginPos < 0) {
            tag = "html";
            beginPos = lcode.indexOf("<"+tag);
        }
        if (beginPos >= 0) {
            beginPos = code.indexOf(">", beginPos);
            if (beginPos > 0) {
                beginPos++;
                endPos = lcode.indexOf("</"+tag, beginPos);
                if (endPos < 0) return code;
            }
        }
        if (beginPos < 0) {
            beginPos = 0;
            
            // 20110329 check for ending </body> .. </html>
            endPos = lcode.indexOf("</body");
            if (endPos < 0) endPos = lcode.indexOf("</html");
            if (endPos >= 0) {
                code = code.substring(0, endPos);
                lcode = lcode.substring(0, endPos);
            }
            else {
                return code;
            }
        }
        

        // skip all whitespace and leading <p></p>
        boolean bRemovedP = false;
        for ( ;; ) {
            for ( ; beginPos < endPos; beginPos++) {
                if (!Character.isWhitespace(code.charAt(beginPos))) break;
            }
         
            String s = "</p>";
            if (lcode.startsWith(s, beginPos)) {
                beginPos += s.length();
                bRemovedP = false;
            }
            else {
                s = "<p";
                if (!lcode.startsWith(s, beginPos)) break;
                int pos = lcode.indexOf(">", beginPos);
                if (pos < 0) break;
                if (pos - beginPos > 5) {
                    if (!lcode.startsWith("<p style=\"margin-top:0\">", beginPos)) break; // <p> has attribute data
                }
                if (pos >= 0) {
                    beginPos = pos+1;
                    bRemovedP = true;
                }
            }
        }            
        if (beginPos > 0) {
            // remove ending whitespace
            for (;;) {
                for ( ; endPos > beginPos; endPos--) {
                    if (!Character.isWhitespace(code.charAt(endPos-1))) break;
                }
                
                if (!bRemovedP) break;
                bRemovedP = false;
                // remove last </p>
                String s = "</p>";
                int pos = lcode.indexOf(s, endPos-s.length());
                if (pos > 0) endPos = pos;
            }   
            if (endPos >= beginPos) {
                code = code.substring(beginPos, endPos);
            }
        }
        
        return code;
    }


    public String convert(String code) {
        return convert(code, true);
    }

    /** convert HTML to safe HTML 
        Strips all span tags, style attributes, 
        makes sure that all attribute values are in quotes
        ignores bogus attribute names
    */
    public String convert(String code, boolean bStrict) {
        code = convertBadChars(code, bStrict);
        // 20120505 added bStrict
        StringBuffer sb = new StringBuffer(code.length());
        tokenManager.setCode(code);

        for (;;) {
            if (token != null && token.type == EOF) break;
            nextToken();
            if (token.type == EOF) break;

            if (token.whitespace != null) sb.append(token.whitespace);
            
            if (token.type != LT) {
                if (token.type == COMMENT) continue;
                if (token.type == SQ) sb.append("'");
                else if (token.type == DQ) sb.append("\"");
                sb.append(token.value);
                if (token.type == SQ) sb.append("'");
                else if (token.type == DQ) sb.append("\"");
                continue;
            }
            
            // process begin tag "<"
            nextToken();
            boolean bSlash = false;
            if (token.type == SLASH) {
                bSlash = true;
                nextToken();
            }
            
            if (bStrict) {
            if (token.type != VARIABLE || (token.value != null && token.value.equalsIgnoreCase("SPAN"))) {
                // bad tag name - find ending GT
                for (;;) {
                    nextToken();
                    if (token == null) break;
                    if (token.type == GT) break;
                    if (token.type == EOF) break;
                }
                continue;
            }
            }
            sb.append("<");
            if (bSlash) sb.append("/");
            sb.append(token.value.toLowerCase()); // TAG Name
            nextToken();
            
            // get attribute name=value pairs
            for (;;) { 
                if (token.type == SLASH) break;
                if (token.type == GT) break;
                if (token.type == EOF) break;

                if (bStrict) {
                // need to strip/ignore all "styles"
                if (token.value != null) {
                    if (token.value.equalsIgnoreCase("style")) {
                        token.type = STRING; 
                    }
                    else if (token.value.equalsIgnoreCase("class")) token.type = STRING; 
                    else if (token.value.equalsIgnoreCase("id")) token.type = STRING; 
                }
                }
                
                if (token.type != VARIABLE) {
                    // bad attribute name - find next attribute or ending GT
                    for (;;) {
                        nextToken();
                        if (token.type == SLASH) break;
                        if (token.type == GT) break;
                        if (token.type == EOF) break;
                        if (token.type == EQUAL) {
                            nextToken(); // skip value
                            if (token.type != EOF) nextToken();
                            break;
                        }
                    }
                    continue;
                }
                
                String s = token.value;
                nextToken();
                if (token.type == EQUAL) {
                    sb.append(' ' + s + " = ");
                    nextToken();
                    if (token.type == VARIABLE) sb.append("'" + token.value + "'");
                    else if (token.type == SQ) sb.append("'" + token.value + "'");
                    else if (token.type == DQ) sb.append("\"" + token.value + "\"");
                    else if (token.type == STRING) sb.append("\'" + token.value + "\'");
                    nextToken();
                }
            }     
            if (token.type == SLASH) {
                sb.append("/");
                nextToken();
            }
                
            sb.append(">");
        }
        return new String(sb);
    }

    protected String convertBadChars(String code, boolean bStrict) {
        int len = code.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i=0; i<len; i++) {
            char ch = code.charAt(i);
            if (ch > 8000) {
                if (ch == 8216) ch = '\'';
                else if (ch == 8217) ch = '\'';
                else if (ch == 8220) ch = '"';
                else if (ch == 8221) ch = '"';
                else if (ch == 8211) ch = '-';
                else if (!bStrict && ch == 8482) {
                    // trademark
                    sb.append("&#153"); 
                    ch = ';'; 
                }
                else  {
                    if (bStrict) continue;
                    
                    sb.append("&#"+((int)ch)); 
                    ch = ';'; 
                }
            }
            else if (ch == 25) ch = '\'';
            else if (ch > 127 || (ch < 32 && (ch < 9 || ch > 12)) ) {
                ch = ' ';
            }
            sb.append(ch);
        }
        return new String(sb);
    }


    protected void nextToken() {
        token = tokenManager.getNext();
        // System.out.println("TOKEN"+token.type+": "+token.value);
    }

}


class HTMLToken {
    public int type;
    public String value;
    public String whitespace;
}


class HTMLTokenManager {
    String code;
    int len;
    int pos = 0;
    StringBuilder sb;


    public void setCode(String code) {
        this.code = code;
        len = code.length();
        sb = new StringBuilder(len);
    }

    public HTMLToken getNext() {
        HTMLToken token = new HTMLToken();
        char chQuote = 0;
        sb.delete(0, len-1);
        boolean bReturn = false;
        char ch=0;

        int dashCount = 0;
        int commentCount = 0;
        boolean bCodeComment = false;
        for ( ; !bReturn && pos<len; pos++) {
            char prev = ch;
            ch = code.charAt(pos);
            char chNext = (pos+1==len)?0:code.charAt(pos+1);

            if (token.type == OAHTMLParser.LT) {
                if (ch != '!') break;
                token.type = OAHTMLParser.COMMENT;
                bCodeComment = (chNext != '-');
            }
            
            if (token.type == OAHTMLParser.COMMENT) {
                sb.append(ch);
                if (ch == '!' && prev == '<' && chNext == '-') commentCount++;
                if (ch == '>' && bCodeComment) {
                    pos++;
                    break; // end of code
                }
                if (ch == '>' && dashCount >= 2) {
                    if (--commentCount == 0) {
                        pos++;
                        break;
                    }
                }
                if (ch == '-') dashCount++;
                else dashCount = 0;
                continue;
            }

            // convert MS Word smart quotes
            if (ch > 8000) {
                if (ch == 8216) ch = '\'';
                else if (ch == 8217) ch = '\'';
                else if (ch == 8220) ch = '"';
                else if (ch == 8221) ch = '"';
                else if (ch == 8211) ch = '-';
                else if (ch == 8482) {
                    sb.append("&#153"); // trademark
                    ch = ';';
                }
            }
            if (ch > 127) ch = ' ';
            if (ch < 32 && (ch < 9 || ch > 12)) {
                ch = ' ';
            }
            
            if (chQuote != 0) {
                if (ch == chQuote) {
                    bReturn = true;
                    continue;
                }
                sb.append(ch);
                continue;
            }

            if (ch == ' ' || ch == '\t' || ch == '\f' || ch == '\n' || ch == '\r') {
                if (token.type != 0) break;
                else {
                    if (token.whitespace == null) token.whitespace = "";
                    token.whitespace += ""+ch;
                }
                continue;
            }

            if (ch == '\'' || ch == '\"') {
                if (token.type == 0) {
                    chQuote = ch;
                    if (ch == '\'') token.type = OAHTMLParser.SQ;
                    else token.type = OAHTMLParser.DQ;
                    continue;
                }
            }
            else if (ch == '/' && token.type == 0) {
                if (token.type != 0) {
                    if (chNext == '>') break;
                    else token.type = OAHTMLParser.STRING;
                }
                else {
                    token.type = OAHTMLParser.SLASH;
                    bReturn = true;
                }
            }
            else if (ch == '>') {
                if (token.type != 0) break;
                else {
                    token.type = OAHTMLParser.GT;
                    bReturn = true;
                }
            }
            else if (ch == '<') {
                if (token.type != 0) break;
                else {
                    token.type = OAHTMLParser.LT;
                }
            }
            else if (ch == '=') {
                if (token.type != 0) break;
                else {
                    token.type = OAHTMLParser.EQUAL;
                    bReturn = true;
                }
            }
            else if ( (token.type == 0 || token.type == OAHTMLParser.VARIABLE) && ( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') ) {
                token.type = OAHTMLParser.VARIABLE;
            }
            else {
                if (token.type != OAHTMLParser.SQ && token.type != OAHTMLParser.DQ) token.type = OAHTMLParser.STRING;
            }
            
            sb.append(ch);
        }

        token.value = new String(sb);

        if (token.type == 0) token.type = OAHTMLParser.EOF;
        return token;
    }
}

