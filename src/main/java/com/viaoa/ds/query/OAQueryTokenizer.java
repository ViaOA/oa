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
package com.viaoa.ds.query;

import java.util.*;

/**
    Descendant parser internally used to parse object queries into a Vector of OAQueryToken Objects.
    Uses a OAQueryTokenManager to parse into tokens.
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class OAQueryTokenizer implements OAQueryTokenType {
    OAQueryTokenManager tokenManager;
    OAQueryToken token, lastToken;
    Vector vec;

    /** convert query to vector of tokens */
    public Vector convertToTokens(String query) {
        if (tokenManager == null) tokenManager = new OAQueryTokenManager();
        vec = new Vector(20,20);
        tokenManager.setQuery(query);
        nextToken();
        evaluate();
        return vec;
    }

    protected void evaluate() {
        evaluateA();
        if (token.type != OAQueryTokenType.EOF) {
            throw new RuntimeException("unexpected token \""+token.value+"\" while parsing query " + tokenManager.query);
        }
    }

    // AND OR
    protected void evaluateA() {
        evaluateB();
        if (token.type == OAQueryTokenType.AND || token.type == OAQueryTokenType.OR) {
            vec.addElement(token);
            nextToken();
            evaluateA();
        }
    }

    // GT, GE, LT, LE, EQUAL, NOTEQUAL, LIKE
    protected void evaluateB() {
        evaluateB2();
        if (token.isOperator()) {
            vec.addElement(token);
            nextToken();
            evaluateA();
        }
    }

    // 20171222 support for IN(1,2,3) 
    // IN
    protected void evaluateB2() {
        evaluateC();
        if (token.type ==  OAQueryTokenType.IN) {
            vec.addElement(token);

            for (int i=0; ;i++) {
                nextToken();
                vec.addElement(token);
                if (i == 0) {
                    if (token.type != OAQueryTokenType.SEPERATORBEGIN) {
                        throw new RuntimeException("IN operator expected begin '(',  query " + tokenManager.query);
                    }
                }
                else if (i % 2 == 0) {
                    if (token.type == OAQueryTokenType.SEPERATOREND) {
                        break;
                    }
                    if (token.type != OAQueryTokenType.COMMA) {
                        throw new RuntimeException("IN operator expected comma or ')',  query " + tokenManager.query);
                    }
                }
                else {
                    if (token.type != OAQueryTokenType.NUMBER) {
                        throw new RuntimeException("IN operator expected number,  query " + tokenManager.query);
                    }
                }
            }
            nextToken();
        }
    }
    
    
    
    // () used to surround
    protected void evaluateC() {
        if (token.type == OAQueryTokenType.SEPERATORBEGIN) {
            vec.addElement(token);
            nextToken();
            evaluateA();

            if (token.type == OAQueryTokenType.SEPERATOREND) {
                vec.addElement(token);
                nextToken();
            }
            else throw new RuntimeException("Unbalanced brackets in query " + tokenManager.query);
        }
        else evaluateC2();
    }
    
// 20090608 added C2, to allow for sql functions, ex: lower(lastName)
    // () func call
    protected void evaluateC2() {
        evaluateD();
        if (token.type == OAQueryTokenType.SEPERATORBEGIN) {
            token.type = OAQueryTokenType.FUNCTIONBEGIN;
            vec.addElement(token);
            nextToken();
            
            evaluateA();
            if (token.type == OAQueryTokenType.SEPERATOREND) {
                token.type = OAQueryTokenType.FUNCTIONEND;
                vec.addElement(token);
                nextToken();
            }
            else throw new RuntimeException("Unbalanced brackets in query " + tokenManager.query);
        }
    }

    
    // single quotes
    protected void evaluateD() {
        evaluateE();
        if (token.type == OAQueryTokenType.STRINGSQ) {
            vec.addElement(token);
            nextToken();
        }
    }


    // Single Quote
    protected void evaluateE() {
        // sql allows for single quotes to be doubled up to show a single quote in string
        evaluateF();
        while (token.type == OAQueryTokenType.STRINGSQ) {
            vec.addElement(token);
            nextToken();
        }
    }


    // VARIABLE, NUMBER, EOF, ?
    protected void evaluateF() {
        if ((token.type == OAQueryTokenType.STRINGDQ) ||
            (token.type == OAQueryTokenType.STRINGSQ) ||
            (token.type == OAQueryTokenType.STRINGESC) ||
            (token.type == OAQueryTokenType.NUMBER) ||
            (token.type == OAQueryTokenType.NULL) ||
            (token.type == OAQueryTokenType.PASSTHRU) ||
            (token.type == OAQueryTokenType.VARIABLE) || 
            (token.type == OAQueryTokenType.QUESTION)) {
            vec.addElement(token);
            nextToken();
        }
        else {
            throw new RuntimeException("Unexpected value in query " + tokenManager.query + " expecting variable or string, received "+token.value);
        }
    }

    protected void nextToken() {
        lastToken = token;
        token = tokenManager.getNext();
    }

    public static void main(String[] args) {
        OAQueryTokenizer qt = new OAQueryTokenizer();
        String query = "Code = 'CT13''6\"X16HALF-COL'";
        Vector vec = qt.convertToTokens(query);
        int x = vec.size();
    }
    
}



