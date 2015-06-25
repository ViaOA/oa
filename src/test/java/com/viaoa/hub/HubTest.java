package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;

public class HubTest extends OAUnitTest {

    @Test
    public void setAO() {
        reset();
        
        Hub h = new Hub();
        h.add("one");
        h.add("two");
        
        h.setAO("one");
        
        assertEquals(h.getAO(), "one");
    }

    @Test
    public void setAO2() {
        reset();
        
        Hub h = new Hub();
        Row r = new Row();
        h.add(new Row());
        h.add(new Row());
        h.add(r);
        h.add(new Row());
        
        h.setAO(r);
        
        assertEquals(h.getAO(), r);
    }
    
    class Row {
    }
    
}




