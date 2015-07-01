package com.viaoa.util;

import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;
import com.theice.tsactest.model.oa.*;

public class OADateTimeTest extends OAUnitTest {

    @Test
    public void dateTimeTest() {
    

        String s = "04/28/2015 18:59:32";
        OADateTime dt = new OADateTime(s, "MM/dd/yyyy HH:mm:ss");

        String s2 = dt.toString("MM/dd/yyyy HH:mm:ss");
        
        assertEquals(s, s2);
    }
}
