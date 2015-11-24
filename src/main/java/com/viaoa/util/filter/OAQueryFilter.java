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
    
    private OAPropertyPath propertyPath;
    private Object value;
    
    // root filter for query
    private OAFilter filter;    
    
    public OAQueryFilter(Class<T> clazz, String query, Object[] args) {
        this.clazz = clazz;
        this.query = query;
        this.args = args;
        setup();
        newArgs = new Object[0];
        
        this.filter = setupA();
    }

    private Vector vecToken;
    private int posToken;
    private OAQueryToken nextToken;
    
    private OAQueryToken nextToken() {
        if (vecToken == null || posToken >= vecToken.size()) return null;
        OAQueryToken t = (OAQueryToken) vecToken.elementAt(posToken++);
        return t;
    }
    
    // descendant parser to create filters
    private OAFilter setupA() {
        OAQueryTokenizer qa = new OAQueryTokenizer();
        vecToken = qa.convertToTokens(query);
        nextToken();
        if (nextToken == null) return new OAFilter() {
            @Override
            public boolean isUsed(Object obj) {
                return false;
            }
        };
        return setupB();
    }

    // ()
    private OAFilter setupB() {
        boolean b;
        if (b = (nextToken.type == OAQueryTokenType.SEPERATORBEGIN)) {
            nextToken();
        }
        OAFilter f = setupC();
        if (b) {
            if (nextToken.type != OAQueryTokenType.SEPERATOREND) {
                //qqqqq error
            }

            //qq OAFilter f = new OABlockFilter()
        }
        
    }
    
    // AND
    private OAFilter setupC() {
    }
    // OR
    private OAFilter setupD() {
    }
    // >
    private OAFilter setupE() {
    }
    // >=
    private OAFilter setupF() {
    }
    // <
    private OAFilter setupG() {
    }
    // <=
    private OAFilter setupH() {
    }
    // == 
    private OAFilter setupI() {
    }
    // !=
    private OAFilter setupJ() {
    }
    // LIKE 
    private OAFilter setupK() {
    }
    // NOTLIKE
    private OAFilter setupL() {
    }
    // bottom
    private OAFilter setupM() {
        nextToken();
    }

    
    
    
    private void setup() {
        OAQueryTokenizer qa = new OAQueryTokenizer();
        Vector vecToken = qa.convertToTokens(query);

//qqqqqqqqqq this only handles format "propertyPath = ?"        
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

//qqqqqqqqq                
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
//qqqqqqq support (), AND, OR, ETC            
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

    
    
    
    public static void main(String[] args) {
        OAQueryTokenizer qt = new OAQueryTokenizer();
        String query = "Code = 'CT13''6\"X16HALF-COL'";
        query = "test.this.Code = ?";
        Vector vec = qt.convertToTokens(query);
        int x = vec.size();
    }
}
