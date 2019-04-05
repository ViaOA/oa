package com.viaoa.util;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.xice.tsac3.model.oa.*;

public class OATimeTest extends OAUnitTest {

    @Test
    public void test() {
        OATime time = new OATime(20, 0, 0);
        
        long ms;
        OATime tNow = new OATime();
        if (tNow.before(time)) ms = time.betweenMilliSeconds(tNow);
        else {
            ms = tNow.betweenMilliSeconds(time);
            ms = (24 * 60 * 60 * 1000) - ms;
        }
        
        ms = ms / (1000 * 60);

        int xx = 4;
        xx++;
        
    }
    
}
