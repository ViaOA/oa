package com.viaoa.ds.query;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

public class OAQueryTokenManagerTest extends OAUnitTest {

    @Test
    public void test() {

        testQuery("id = 4");
        testQuery("id = '4'");
        testQuery("id = 'abcXYZ'");
        
        testQuery("id IN (1,2,3,4)");

    }
    
    protected void testQuery(String query) {
        OAQueryTokenManager tm = new OAQueryTokenManager();
        tm.setQuery(query);
        for (;;) {
            OAQueryToken tok = tm.getNext();
            if (tok == null) break;
            if (tok.type == OAQueryTokenType.EOF) break;
            System.out.print(tok.value);
        }
        System.out.println();
    }
    
}
