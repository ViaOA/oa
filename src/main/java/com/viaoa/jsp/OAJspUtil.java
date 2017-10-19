package com.viaoa.jsp;

import com.viaoa.util.OAString;

/**
 * Utility used to convert dynamic data for internal text, so that it is html and javascript safe.
 * 
 * Data that is sent as code to the browser needs to be converted, so that the code that is created for it
 * does not break, either within the javascript or html.
 * 
 * The outer js code will wrap the text in single or double quotes, and can then have html inside of it.
 * The html can also have js code in it, so there is various encoding/escaping that needs to be done
 * to have well formed js and html that will not break the back from processing it.
 * 
 * 
 * @author vvia
 */
public class OAJspUtil {
    
    /**
     * Helper method use to make sure that text is correctly escaped to be inside of javascript code.
     * This will call createJsString(text, '"', false, false) 
     */
    public static String createText(final String text) {
        String s = createJsString(text, '"', false, true);
        return s;
    }
    public static String createText(final String text, char quoteChar) {
        String s = createJsString(text, quoteChar, false, true);
        return s;
    }
    
    
    public static String createHtml(final String text) {
        String s = createJsString(text, '"', false, true);
        return s;
    }
    public static String createHtml(final String text,  char quoteChar) {
        String s = createJsString(text, quoteChar, false, true);
        return s;
    }

    
    
    public static String createHtmlString(final String text, final char htmlQuoteChar) {
        String s;
        if (htmlQuoteChar == '\'') s = OAString.convert(text, "\'", "&#39;");  // &apos;  not yet supported
        else if (htmlQuoteChar == '\"') s = OAString.convert(text, "\"", "&#34;");  // &quot;
        else s = text;
        return s;
    }

    public static String createJsString(final String text, final char jsQuoteChar) {
        return createJsString(text,jsQuoteChar, false, false);
    }
    
    /**
     * Converts a string to be used inside of a codegen Javascript screen
     * @param text
     * @param jsQuoteChar the char used for quoting the string
     * @param bIsJsCodeEmbeddedInHtml true if this is for js string that will be embedded inside of html
     * @param bMakeHtmlSafe if text should be check to see if < or > should be encoded.
     * @return string that can be put into a codegen js string and quoted with quoteChars
     */
    public static String createJsString(final String text, final char jsQuoteChar, final boolean bIsJsCodeEmbeddedInHtml, final boolean bMakeHtmlSafe) {
        if (text == null) return "";
        final int x = text.length();

        boolean bConvertBrackets = false;
        if (bMakeHtmlSafe) {
            // check to see if there is html within the text.  If so, then dont convert.
            int cnt1 = 0;
            int cnt2 = 0;
            for (int i=0; i<x; i++) {
                char c = text.charAt(i);
                if (c == '<') cnt1++;
                else if (c == '>') cnt2++;
            }
            if (cnt1 != cnt2 && cnt1 < 3 && cnt2 < 3) bConvertBrackets = true;
        }
        
        StringBuilder sb = null;

        for (int i=0; i<x; i++) {
            char ch = text.charAt(i);

            if ( (bConvertBrackets && (ch == '<' || ch == '>')) || ch == '\r' || ch == '\n' || ch == '\\' || ch == jsQuoteChar || (bIsJsCodeEmbeddedInHtml && (ch == '\'' || ch == '\"')) ) {
                if (sb == null) {
                    sb = new StringBuilder(x+4);
                    if (i > 0) sb.append(text.substring(0, i));
                }
                
                if (ch == '\'') {
                    if (bIsJsCodeEmbeddedInHtml) sb.append("\\x5Cx27");  //  x5C = "\\"   x27 = "\'"
                    else sb.append("\\"+jsQuoteChar);
                }
                else if (ch == '\"') {
                    if (bIsJsCodeEmbeddedInHtml) sb.append("\\x5Cx22");  //  x5C = "\\"   x22 = "\""
                    else sb.append("\\"+jsQuoteChar);
                }
                else if (ch == '<') {
                    sb.append("&lt;");
                }
                else if (ch == '>') {
                    sb.append("&gt;");
                }
                else if (ch == '\n') {
                    if (bConvertBrackets) sb.append("<br>");
                    else {
                        if (!bIsJsCodeEmbeddedInHtml) sb.append("\\n");
                        else sb.append("\\x5Cn");
                    }
                }
                else if (ch == '\r') {
                    // no-op
                }
                else if (ch == '\\') {
                    if (!bIsJsCodeEmbeddedInHtml) sb.append("\\\\");
                    else sb.append("\\x5C\\x5C");
                }
            }
            else {
                if (sb != null) sb.append(ch);
            }
        }
        
        if (sb == null) {
            return text;
        }
        return sb.toString();
    }
}  
/*
if (ch == '&') {
    sb.append("&amp;");
}
else if (ch == '"') {
    sb.append("&#34;"); // &quot;
}
else if (ch == '\'') {
    sb.append("&#39;"); // &apos;  not yet supported
}
*/
