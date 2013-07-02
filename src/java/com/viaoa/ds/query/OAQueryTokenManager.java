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
package com.viaoa.ds.query;


/**
 * Used to parse a query into tokens.
 * @author vincevia
 */
public class OAQueryTokenManager {
    String query;
    int len;
    int pos = 0;
    StringBuffer sb;

    public void setQuery(String query) {
        this.query = query;
        len = query.length();
        sb = new StringBuffer(len+32);
        pos = 0;
    }

    public OAQueryToken getNext() {
        OAQueryToken token = new OAQueryToken();
        char charWaitFor = 0;
        char ch=0, chLast;
        sb.delete(0, len-1);
        boolean bReturn = false;
        String sError = null;

        for ( ; !bReturn && pos<len; pos++) {
            boolean bReturnNow = false;
            chLast = ch;
            ch = query.charAt(pos);
            
            
            if (charWaitFor != 0) {
                if (ch == charWaitFor) {
                	if (chLast != '\\') {
                		bReturn = true;
                		continue;
                	}
                }
            	if (ch != '\\' || chLast == '\\') { 
            		sb.append(ch);
            	}
                continue;
            }

            if (token.type == OAQueryTokenType.PASSTHRU) {
                if (ch == ']') {
                    if (query.substring(pos).toUpperCase().startsWith("]THRU")) {
                        pos += 5;
                        break;
                    }
                }
                sb.append(ch);
                continue;
            }
            
            
            if (ch == '\t' || ch == '\f' || ch == '\n' || ch == '\r') continue;
            if (ch == ' ') {
                if (token.type != 0) bReturn = true; // skip space
                continue;
            }
            
            if (ch == '\'') {
                if (token.type != 0) break;
            	charWaitFor = ch;
                token.type = OAQueryTokenType.STRINGSQ;
                continue;
            }
            if (ch == '\"') {
                if (token.type != 0) break;
            	charWaitFor = ch;
                token.type = OAQueryTokenType.STRINGDQ;
                continue;
            }
            if (ch == '{') {  // escape string
                if (token.type != 0) break;
                token.type = OAQueryTokenType.STRINGESC;
                charWaitFor = '}';
                continue;
            }

            if (token.type == 0) {
                if (Character.isDigit(ch) || ch == '-') {
                    token.type = OAQueryTokenType.NUMBER;
                }
                else if (ch == '+') {
                    token.type = OAQueryTokenType.NUMBER;
                    continue;
                }
                else if (ch == '.') {
                    token.type = OAQueryTokenType.NUMBER;
                }
                else if (ch == '(') {
                    token.type = OAQueryTokenType.SEPERATORBEGIN;
                    bReturn = true;
                }
                else if (ch == ')') {
                    token.type = OAQueryTokenType.SEPERATOREND;
                    bReturn = true;
                }
                else if (ch == '>') {
                    token.type = OAQueryTokenType.GT;
                }
                else if (ch == '<') {
                    token.type = OAQueryTokenType.LT;
                }
                else if (ch == '=') {
                    token.type = OAQueryTokenType.EQUAL;
                }
                else if (ch == '!') {
                    token.type = OAQueryTokenType.OPERATOR;
                }
                else if (ch == '&') {
                    token.type = OAQueryTokenType.AND;
                }
                else if (ch == '|') {
                    token.type = OAQueryTokenType.OR;
                }
                else if ( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') {
                    token.type = OAQueryTokenType.VARIABLE;
                }
                else if (ch == '?') {
                    token.type = OAQueryTokenType.QUESTION;
                    bReturn = true;
                }
                else {
                    sError = "Illegal token '"+ch+"'";
                    bReturn = true;
                }
            }
            else if (token.type == OAQueryTokenType.VARIABLE) {
                if (ch == '[') {  // check for PASSTHRU
                    if ((new String(sb)).equalsIgnoreCase("PASS")) {
                        token.type = OAQueryTokenType.PASSTHRU;
                        sb.delete(0, len-1);
                        continue;
                    }
                }
                if ( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == '_' || ch == '.') {
                }
                else {
                    bReturnNow = true;
                }
            }
            else if (token.type == OAQueryTokenType.NUMBER) {
                if (Character.isDigit(ch)) {
                }
                else if (ch == '.') {
                }
                else {
                    bReturnNow = true;
                }
            }
            else if (token.isOperator()) {
                switch (chLast) {
                    case '>':
                        if (ch != '=') {
                            token.type = OAQueryTokenType.GT;
                            bReturnNow = true;
                        }
                        else token.type = OAQueryTokenType.GE;
                        break;
                    case '<':
                        if (ch != '=') {
                            token.type = OAQueryTokenType.LT;
                            bReturnNow = true;
                        }
                        else token.type = OAQueryTokenType.LE;
                        break;
                    case '=':
                        if (ch != '=') {
                            token.type = OAQueryTokenType.EQUAL;
                            bReturnNow = true;
                        }
                        else token.type = OAQueryTokenType.EQUAL;
                        break;
                    case '!':
                        if (ch != '=') sError = "Token '!' not valid without '='";
                        else token.type = OAQueryTokenType.NOTEQUAL;
                        break;
                    case '&':
                        if (ch != '&') {
                            token.type = OAQueryTokenType.AND;
                            bReturnNow = true;
                        }
                        else token.type = OAQueryTokenType.AND;
                        break;
                    case '|':
                        if (ch != '|') {
                            token.type = OAQueryTokenType.OR;
                            bReturnNow = true;
                        }
                        else token.type = OAQueryTokenType.OR;
                        break;
                }
                bReturn = true;
            }
            if (bReturnNow) break; // leave on current pos
            sb.append(ch);
        }
        if (sError != null) throw new RuntimeException(sError + " at position "+pos+" in query " + query);
        token.value = new String(sb);
        if (token.type == 0) token.type = OAQueryTokenType.EOF;
        else if (token.type == OAQueryTokenType.VARIABLE) {
            // if VarName, check for DESC, LIKE, AND, OR
            String s = token.value.toUpperCase();
            if (s.equals("IS")) token.type = OAQueryTokenType.EQUAL;
            else if (s.equals("AND")) token.type = OAQueryTokenType.AND;
            else if (s.equals("OR")) token.type = OAQueryTokenType.OR;
            else if (s.equals("NULL")) token.type = OAQueryTokenType.NULL;
            else if (s.equals("LIKE")) token.type = OAQueryTokenType.LIKE;
        }
        return token;
    }

}
