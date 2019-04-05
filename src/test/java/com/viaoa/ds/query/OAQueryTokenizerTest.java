package com.viaoa.ds.query;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Vector;

import com.viaoa.OAUnitTest;

import test.xice.tsac3.model.oa.*;

public class OAQueryTokenizerTest extends OAUnitTest {

    @Test
    public void test() {

        String query = "id IN (1,2, 3, 4 )";

        OAQueryTokenizer t = new OAQueryTokenizer();
        Vector vec = t.convertToTokens(query);
        
        String sx = "";
        for (int i=0; i<vec.size(); i++) {
            OAQueryToken tok = (OAQueryToken) vec.get(i);
            if (tok.type == OAQueryTokenType.EOF) break;
            sx += tok.value;
            System.out.print(tok.value);
        }
        System.out.println();
        assertEquals("idIN(1,2,3,4)", sx);
    }
    
}
