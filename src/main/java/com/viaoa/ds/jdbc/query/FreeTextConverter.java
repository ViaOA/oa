package com.viaoa.ds.jdbc.query;

import java.util.StringTokenizer;

/**
 * Convert a query to freetext
 * 
 * example:
 *      CONTAINS(tableName.*,'" + convertForFreeText(phrase) + "')";
 * 
 * 20180322
 */
public class FreeTextConverter {

    /**
     * Convert a phrase in a query for freeform text search.
     * Currently supports SQL Server
     */
    public static String convertForFreeText(String phrase) {
        if (phrase == null) return "";
        String s;

        // parse keyword:   a '**' needs to be converted to FORMSOF(...)
        String newPhrase = "";

        String sep = "(), \"&";
        StringTokenizer st = new StringTokenizer(phrase, sep, true);
        String hold = "";
        boolean bInQuote = false;

        while (st.hasMoreTokens()) {
            s = st.nextToken();

            char ch = s.charAt(0);

            if (bInQuote) {
                if (ch != '\"') {
                    hold += s;
                    continue;
                }
                bInQuote = false;
            }
            else if (ch == '\"') {
                bInQuote = true;
                if (hold.trim().length() == 0) {
                    hold = "";
                    continue;
                }
                s = "AND";
                ch = 'A';
            }
            else if (ch == '[') {
                s = "(";
                ch = '(';
            }
            else if (ch == ']') {
                s = ")";
                ch = ')';
            }
            else if (ch == ',') {
                s = "and";
            }
            else if (ch == '(' || ch == ')') {
            }
            else if (ch == '&') {
                if (hold.length() > 0 && !hold.endsWith(" ")) {
                    hold += "&";
                    continue;
                }
                if (!newPhrase.endsWith(" ")) {
                    newPhrase += "&";
                    continue;
                }
                s = "and";
                ch = 'a';
            }
            else if (s.equalsIgnoreCase("and") || s.equalsIgnoreCase("or") || s.equalsIgnoreCase("not") || s.equalsIgnoreCase("near")
                    || s.equalsIgnoreCase("like")) {
            }
            else {
                hold += s;
                continue;
            }

            if (hold.length() > 0) {
                if (ch != '\"') hold = hold.trim();
                if (hold.length() > 0) {
                    hold = com.viaoa.html.Util.convert(hold, "'", "''");
                    if (newPhrase.length() > 0 && !newPhrase.endsWith(" ")) newPhrase += " ";
                    newPhrase += "\"" + hold + "\"";
                }
                hold = "";
            }

            if (ch != '\"' && s != null && s.length() > 0) {
                if (!newPhrase.endsWith(" ")) newPhrase += " ";
                newPhrase += s;
            }
            bInQuote = false;
        }

        if (hold.length() > 0) {
            hold = hold.trim();
            hold = com.viaoa.html.Util.convert(hold, "'", "''");
            if (newPhrase.length() > 0 && !newPhrase.endsWith(" ")) newPhrase += " ";
            newPhrase += "\"" + hold + "\"";
        }
        return newPhrase;
    }
}
