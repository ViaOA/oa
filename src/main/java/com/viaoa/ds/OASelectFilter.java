package com.viaoa.ds;

import java.util.Vector;

import com.viaoa.ds.query.OAQueryToken;
import com.viaoa.ds.query.OAQueryTokenType;
import com.viaoa.ds.query.OAQueryTokenizer;
import com.viaoa.util.OACompare;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAPropertyPath;

/**
 * Convert an Object Query to an OAFilter.
 * 
 * This currently only handles a query with a "propertypath = ?" format
 * 
 * Todo: expand it to build a AND/OR tree.
 *    handle (), AND, OR || &&, etc...
 * 
 * created 20140127 
 * @author vvia
 */
public class OASelectFilter<T> implements OAFilter {
    private Class<T> clazz;
    private String query; 
    private Object[] args;
    
    private OAPropertyPath propertyPath;
    private Object value;
    
    public OASelectFilter(Class<T> clazz, String query, Object[] args) {
        this.clazz = clazz;
        this.query = query;
        this.args = args;
    }

    private void test() {
        OAQueryTokenizer qa = new OAQueryTokenizer();
        Vector vecToken = qa.convertToTokens(query);
        
        int paramPos = 0;
        int x = vecToken.size();
        
        
        for (int i=0; i<x; i++) {
            OAQueryToken token = (OAQueryToken) vecToken.elementAt(i);
            String s = null;
        
            
            if (token.type == OAQueryTokenType.VARIABLE) {
                propertyPath = new OAPropertyPath(clazz, token.value);
            }
            else if (token.isOperator()) {
                // operator
                if (token.type == OAQueryTokenType.EQUAL) {
                    
                }

            }
            else if (token.type == OAQueryTokenType.QUESTION) {
                // arg
            }
        }        
    }
    
    @Override
    public boolean isUsed(Object obj) {
        try {
            Object objx = propertyPath.getValue(null, obj);
            return OACompare.isEqual(objx, args.length > 0 ? args[0] : null);
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
