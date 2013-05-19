package com.dispatcher.editor;

import java.util.*;
import java.lang.reflect.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

/** 
    Parse and "clean up" HTML code.
    Currently used by Editor to convert pasted code from Microsoft Word.
    This needs to be set up to convert styles to HTML tags.
*/
public class HTMLParser {
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


    public HTMLParser() {
        tokenManager = new HTMLTokenManager();
    }

    public boolean isMicrosoft(String code) {
        return (code != null && code.toLowerCase().indexOf("urn:schemas-microsoft-com") >= 0);
    }

    /**
        remove all code outside and including html body tags.
        Also removes base href tag.
    */
    public static String removeBody(String code) {
        if (code == null) return null;
        String tag = "body";
        
        String lcode = code.toLowerCase();
        int pos = lcode.indexOf("<"+tag);
        if (pos >= 0) {
            code = code.substring(pos);
            pos = code.indexOf(">");
            if (pos > 0) {
                int x = code.length();
                while (pos < x && Character.isWhitespace(code.charAt(pos+1))) {
                    pos++;
                }
                code = code.substring(pos+1);
            }
            pos = code.toLowerCase().indexOf("</"+tag);
            if (pos >= 0) {
                while (pos > 0 && Character.isWhitespace(code.charAt(pos-1))) {
                    pos--;
                }
                code = code.substring(0, pos);
            }
        }
        else {
            pos = lcode.toLowerCase().indexOf("</"+tag);
            if (pos >= 0) {
                code = code.substring(0, pos);
            }
        }

        // 2004/09/08
        pos = code.toLowerCase().indexOf("<base href");
        if (pos >= 0) {
            int pos2 = code.indexOf('>', pos);
            if (pos2 > 0) {
                if (pos > 0) {
                    if (pos2 == (code.length()-1)) code = code.substring(0, pos);
                    else code = code.substring(0,pos) + code.substring(pos2+1);
                }
                else {
                    if (pos2 == (code.length()-1)) code = "";
                    else code = code.substring(pos2+1);
                }
            }
        }        
        return code;
    }




    /** convert HTML to safe HTML 
        Strips all span tags, style attributes 
        makes sure that all attribute values are in quotes
        ignores bogus attribute names
    */
    public String convert(String code) {
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
            sb.append("<");
            if (bSlash) sb.append("/");
            sb.append(token.value.toLowerCase()); // TAG Name
            nextToken();
            
            // get attribute name=value pairs
            for (;;) { 
                if (token.type == SLASH) break;
                if (token.type == GT) break;
                if (token.type == EOF) break;

                // need to strip/ignore all "styles"
                if (token.value != null) {
                    if (token.value.equalsIgnoreCase("style")) {
                        token.type = STRING; 
                    }
                    else if (token.value.equalsIgnoreCase("class")) token.type = STRING; 
                    else if (token.value.equalsIgnoreCase("id")) token.type = STRING; 
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
    StringBuffer sb;


    public void setCode(String code) {
        this.code = code;
        len = code.length();
        sb = new StringBuffer(len);
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

            if (token.type == HTMLParser.LT) {
                if (ch != '!') break;
                token.type = HTMLParser.COMMENT;
                bCodeComment = (chNext != '-');
            }
            
            if (token.type == HTMLParser.COMMENT) {
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
                    if (ch == '\'') token.type = HTMLParser.SQ;
                    else token.type = HTMLParser.DQ;
                    continue;
                }
            }
            else if (ch == '/' && token.type == 0) {
                if (token.type != 0) {
                    if (chNext == '>') break;
                    else token.type = HTMLParser.STRING;
                }
                else {
                    token.type = HTMLParser.SLASH;
                    bReturn = true;
                }
            }
            else if (ch == '>') {
                if (token.type != 0) break;
                else {
                    token.type = HTMLParser.GT;
                    bReturn = true;
                }
            }
            else if (ch == '<') {
                if (token.type != 0) break;
                else {
                    token.type = HTMLParser.LT;
                }
            }
            else if (ch == '=') {
                if (token.type != 0) break;
                else {
                    token.type = HTMLParser.EQUAL;
                    bReturn = true;
                }
            }
            else if ( (token.type == 0 || token.type == HTMLParser.VARIABLE) && ( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') ) {
                token.type = HTMLParser.VARIABLE;
            }
            else {
                if (token.type != HTMLParser.SQ && token.type != HTMLParser.DQ) token.type = HTMLParser.STRING;
            }
            
            sb.append(ch);
        }

        token.value = new String(sb);

        if (token.type == 0) token.type = HTMLParser.EOF;
        return token;
    }
}


