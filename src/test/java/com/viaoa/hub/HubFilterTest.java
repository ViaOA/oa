package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import com.tmgsc.hifivetest.model.oa.*;
import com.viaoa.OAUnitTest;

public class HubFilterTest extends OAUnitTest {

    @Test
    public void test() {
        init();

        Hub<PointsAwardLevel> hubMaster = new Hub<PointsAwardLevel>(PointsAwardLevel.class);
        for (int i=0; i<20; i++) {
            PointsAwardLevel pal = new PointsAwardLevel();
            hubMaster.add(pal);
        }
        
        for (int i=0; i<100; i++) {
            Hub<PointsAwardLevel> hubFiltered = new Hub<PointsAwardLevel>(PointsAwardLevel.class);
            hubMaster.copyInto(hubFiltered);

            HubFilter<PointsAwardLevel> hf = new HubFilter<PointsAwardLevel>(hubMaster, hubFiltered) {
                public boolean isUsed(PointsAwardLevel level) {
                    return true;
                }
            }; 

            assertEquals(20, hubFiltered.getSize());
//            hf.close();
        }

        for (int i=0; i<100; i++) System.gc();

        PointsAwardLevel pal = new PointsAwardLevel();
        hubMaster.add(pal);
        
        int xx = 0;
        xx++;
    }

    
}




