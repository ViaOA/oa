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
package com.viaoa.util.filter;

import java.util.Stack;
import java.util.Vector;

import com.viaoa.ds.query.OAQueryToken;
import com.viaoa.ds.query.OAQueryTokenType;
import com.viaoa.ds.query.OAQueryTokenizer;
import com.viaoa.util.OAArray;
import com.viaoa.util.OACompare;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAPropertyPath;

// expanded to support all of the oa filters

/**
 * Convert an Object Query to an OAFilter.
 * This can be used for Hub selects, etc.
 * It is used by OADataSourceObjectCache.
 * 
 * created 20140127 
 * @author vvia
 */
public class OAQueryFilter<T> implements OAFilter {
    private Class<T> clazz;
    private String query; 
    private Object[] args;
    private Object[] newArgs;

//remove these qqqqqqqqq    
    private OAPropertyPath propertyPath;
    private Object value;
    
    // root filter for query
    private FilterInfo filterInfo;    

    private Stack<FilterInfo> stack = new Stack<FilterInfo>();
    private Vector vecToken;
    private int posToken;
    
    public OAQueryFilter(Class<T> clazz, String query, Object[] args) throws Exception {
        this.clazz = clazz;
        this.query = query;
        this.args = args;
        newArgs = new Object[0];
        
        this.filterInfo = parse();
        if (stack.size() != 0) throw new Exception("parse failed, filters not all used, remainder="+stack.size());
    }

    
    private class FilterInfo {
        public FilterInfo(OAFilter f, String pp) {
            this.filter = f;
            this.propertyPath = pp;
        }
        OAFilter filter;
        String propertyPath;
    }
    
    private FilterInfo parse() throws Exception {
        OAQueryTokenizer qa = new OAQueryTokenizer();
        vecToken = qa.convertToTokens(query);
        FilterInfo f = parseBlock();
        return f;
    }
    

    private FilterInfo parseBlock() throws Exception {
        OAQueryToken token = nextToken();
        if (token == null) throw new Exception("token is null");
        parseForConjuction(token);

        if (stack.size() == 0) throw new Exception("Block failed, no filter in stack");
        FilterInfo fi = stack.pop();
        return fi;
    }


    private OAQueryToken parseForConjuction(OAQueryToken token) throws Exception {
        if (token == null) return null;
        return parseForAnd(token);
    }
    
    // AND
    private OAQueryToken parseForAnd(OAQueryToken token) throws Exception {
        if (token == null || token.type != OAQueryTokenType.AND) {
            token = parseForOr(token);
        }
        if (token != null && token.type == OAQueryTokenType.AND) {
            if (stack.size() == 0) throw new Exception("AND failed, no filter in stack");
            FilterInfo f1 = stack.pop();
            
            token = nextToken();
            token = parseForBracket(token);
            if (stack.size() == 0) throw new Exception("AND failed, no filter in stack");
            FilterInfo f2 = stack.pop();
            
            OAFilter f = new OAAndFilter(f1.filter, f2.filter);
            FilterInfo fi = new FilterInfo(f, null);
            stack.push(fi);
            
            token = parseForConjuction(token);
        }
        return token;
    }

    // OR
    private OAQueryToken parseForOr(OAQueryToken token) throws Exception {
        if (token == null || token.type != OAQueryTokenType.OR) {
            token = parseForBracket(token);
        }
        if (token != null && token.type == OAQueryTokenType.OR) {
            if (stack.size() == 0) throw new Exception("OR failed, no filter in stack");
            FilterInfo f1 = stack.pop();
            
            token = nextToken();
            token = parseForBracket(token);
            if (stack.size() == 0) throw new Exception("OR failed, no filter in stack");
            FilterInfo f2 = stack.pop();
            
            OAFilter f = new OAOrFilter(f1.filter, f2.filter);
            
            FilterInfo fi = new FilterInfo(f, null);
            stack.push(fi);
            
            token = parseForConjuction(token);
        }
        return token;
    }

    
    
    
    // ()
    private OAQueryToken parseForBracket(OAQueryToken token) throws Exception {
        OAQueryToken nextToken;
        if (token.type != OAQueryTokenType.SEPERATORBEGIN) {
            nextToken = parseForEndBracket(token);
            return nextToken;
        }

        FilterInfo fi = parseBlock();
        stack.push(fi);
        nextToken = nextToken();
        return nextToken;
    }
    private OAQueryToken parseForEndBracket(OAQueryToken token) throws Exception {
        if (token.type == OAQueryTokenType.SEPERATOREND) {
            return token;
        }
        OAQueryToken nextToken = parseForEqual(token);
        return nextToken;
    }
    

    // Operators begin
    
    // == 
    private OAQueryToken parseForEqual(OAQueryToken token) throws Exception {
        OAQueryToken nextToken = parseForNotEqual(token);
        if (nextToken.type == OAQueryTokenType.EQUAL) {
            nextToken = nextToken();
            if (nextToken == null) throw new Exception("token expected for =");
            OAFilter f = new OAEqualFilter(nextToken.value, true);
            FilterInfo fi = new FilterInfo(f, token.value);
            stack.push(fi);
            nextToken = nextToken();
        }
        return nextToken;
    }

    // != 
    private OAQueryToken parseForNotEqual(OAQueryToken token) throws Exception {
        OAQueryToken nextToken = parseForGreater(token);
        if (nextToken.type == OAQueryTokenType.NOTEQUAL) {
            nextToken = nextToken();
            if (nextToken == null) throw new Exception("token expected for !=");
            OAFilter f = new OANotEqualFilter(nextToken.value, true);
            FilterInfo fi = new FilterInfo(f, token.value);
            stack.push(fi);
            nextToken = nextToken();
        }
        return nextToken;
    }
    
    // >
    private OAQueryToken parseForGreater(OAQueryToken token) throws Exception {
        OAQueryToken nextToken = parseForGreaterOrEqual(token);
        if (nextToken.type == OAQueryTokenType.GT) {
            nextToken = nextToken();
            if (nextToken == null) throw new Exception("token expected for !=");
            OAFilter f = new OAGreaterFilter(nextToken.value);
            FilterInfo fi = new FilterInfo(f, token.value);
            stack.push(fi);
            nextToken = nextToken();
        }
        return nextToken;
    }
    
    // >=
    private OAQueryToken parseForGreaterOrEqual(OAQueryToken token) throws Exception {
        OAQueryToken nextToken = parseForLess(token);
        if (nextToken.type == OAQueryTokenType.GE) {
            nextToken = nextToken();
            if (nextToken == null) throw new Exception("token expected for !=");
            OAFilter f = new OAGreaterOrEqualFilter(nextToken.value);
            FilterInfo fi = new FilterInfo(f, token.value);
            stack.push(fi);
            nextToken = nextToken();
        }
        return nextToken;
    }

    // <
    private OAQueryToken parseForLess(OAQueryToken token) throws Exception {
        OAQueryToken nextToken = parseForLessOrEqual(token);
        if (nextToken.type == OAQueryTokenType.LT) {
            nextToken = nextToken();
            if (nextToken == null) throw new Exception("token expected for !=");
            OAFilter f = new OALessFilter(nextToken.value);
            FilterInfo fi = new FilterInfo(f, token.value);
            stack.push(fi);
            nextToken = nextToken();
        }
        return nextToken;
    }

    // <=
    private OAQueryToken parseForLessOrEqual(OAQueryToken token) throws Exception {
        OAQueryToken nextToken = parseForLike(token);
        if (nextToken.type == OAQueryTokenType.LE) {
            nextToken = nextToken();
            if (nextToken == null) throw new Exception("token expected for !=");
            OAFilter f = new OALessOrEqualFilter(nextToken.value);
            FilterInfo fi = new FilterInfo(f, token.value);
            stack.push(fi);
            nextToken = nextToken();
        }
        return nextToken;
    }

    // LIKE 
    private OAQueryToken parseForLike(OAQueryToken token) throws Exception {
        OAQueryToken nextToken = parseForNotLike(token);
        if (nextToken.type == OAQueryTokenType.LIKE) {
            nextToken = nextToken();
            if (nextToken == null) throw new Exception("token expected for !=");
            OAFilter f = new OALikeFilter(nextToken.value);
            FilterInfo fi = new FilterInfo(f, token.value);
            stack.push(fi);
            nextToken = nextToken();
        }
        return nextToken;
    }

    // NOTLIKE
    private OAQueryToken parseForNotLike(OAQueryToken token) throws Exception {
        OAQueryToken nextToken = parseBottom(token);
        if (nextToken.type == OAQueryTokenType.NOTLIKE) {
            nextToken = nextToken();
            if (nextToken == null) throw new Exception("token expected for !=");
            OAFilter f = new OANotLikeFilter(nextToken.value);
            FilterInfo fi = new FilterInfo(f, token.value);
            stack.push(fi);
            nextToken = nextToken();
        }
        return nextToken;
    }
    
    private OAQueryToken parseBottom(OAQueryToken token) throws Exception {
        return nextToken();
    }

    private OAQueryToken nextToken() {
        if (vecToken == null || posToken >= vecToken.size()) return null;
        OAQueryToken t = (OAQueryToken) vecToken.elementAt(posToken++);
        
        if (t == null) return t;
        
        return t;
    }

    
    

/**qqqqqq    
    private void setupOLD() {
        OAQueryTokenizer qa = new OAQueryTokenizer();
        Vector vecToken = qa.convertToTokens(query);

        //qqqqq this only handles format "propertyPath = ?"        
        int paramPos = 0;
        boolean bNextIsValue = false;
        int operatorType = 0;
        
        int x = vecToken.size();
        for (int i=0; i<x; i++) {
            OAQueryToken token = (OAQueryToken) vecToken.elementAt(i);
        
            if (bNextIsValue) {
                bNextIsValue = false;

                if (token.type == OAQueryTokenType.QUESTION && args != null && args.length >= paramPos) {
                    newArgs = OAArray.add(Object.class, newArgs, args[paramPos++]);
                }
                else {
                    String s = token.value;
                    if (s != null && s.length() > 1) {
                        char c = s.charAt(0);
                        if ( (c == '\'' || c == '\"') && s.charAt(s.length()-1) == c) {
                            s = s.substring(1, s.length()-2);
                        }
                    }
                    newArgs = OAArray.add(Object.class, newArgs, token.value);
                }

                filter = new OAEqualFilter(token.value, true);
            }
            else if (token.type == OAQueryTokenType.VARIABLE) {
                propertyPath = new OAPropertyPath(clazz, token.value);
            }
            else if (token.type == OAQueryTokenType.AND) {
                // filter = new OAAndFilter()
            }
            else if (token.type == OAQueryTokenType.OR) {
                // filter = new OAOrFilter()
            }
            else if (token.type == OAQueryTokenType.SEPERATORBEGIN) {
                // filter = new OAOrFilter()
            }
            else if (token.type == OAQueryTokenType.SEPERATOREND) {
                // filter = new OAOrFilter()
            }
            else if (token.isOperator()) {
                operatorType = token.type;
                bNextIsValue = true;
            }
        }        
    }
    
    
    
    @Override
    public boolean isUsed(Object obj) {
        try {
            Object objx = propertyPath.getValue(null, obj);
            
if (filter != null) return filter.isUsed(objx); //qqqqqqqqqqqqqqqqqqqqq

            return OACompare.isEqual(objx, args != null &&  args.length > 0 ? args[0] : null);
        }
        catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return false;
    }
    
    public static void mainOLD(String[] args) {
        OAQueryTokenizer qt = new OAQueryTokenizer();
        String query = "Code = 'CT13''6\"X16HALF-COL'";
        query = "test.this.Code = ?";
        Vector vec = qt.convertToTokens(query);
        int x = vec.size();
    }

*/

    
    
    @Override
    public boolean isUsed(Object obj) {
        try {
            Object objx = propertyPath.getValue(null, obj);
            
            if (filterInfo != null) return filterInfo.filter.isUsed(objx); //qqqqqqqqqqqqqqqqqqqqq
        }
        catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return false;
    }
    
    
    
    
    public static void main(String[] args) throws Exception {
        String query = "A = 1";
        query = "A == 1 && B = 2";
        query = "(A == 1) && B = 2";
        query = "A == 1 || B = 2 && C == 3";
        query = "A == 1 && B = 2 && C == 3";
        query = "A == 1 && (B = 2 && C == 3)";

        query = "(A == '1' && (B = 2 && (C == 3))) || X = 5 && Z = 9";
        
        
        OAQueryFilter qf = new OAQueryFilter(Object.class, query, null);
        int xx = 4;
        xx++;
    }
}








