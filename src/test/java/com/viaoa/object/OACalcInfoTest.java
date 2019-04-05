package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import com.viaoa.OAUnitTest;
import com.viaoa.annotation.OACalculatedProperty;
import com.viaoa.util.OAArray;

import test.xice.tsac3.model.oa.*;
import test.xice.tsam.model.oa.Server;

public class OACalcInfoTest extends OAUnitTest {

    @Test
    public void calcInfoTest() {
        OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(Server.class);
        
        ArrayList<OACalcInfo> al = oi.getCalcInfos();
        assertEquals(1, al.size());
        
        OACalcInfo ci = al.get(0);
        assertNotNull(ci);
        
        //OACalculatedProperty(displayName = "Display Name", displayLength = 15, properties = {P_Name, P_HostName, P_IpAddress})

        String[] ss = new String[] {Server.P_Name, Server.P_HostName, Server.P_IpAddress};
        
        boolean b = OAArray.isEqual(ci.getDependentProperties(), ss);
        if (!b) {
            Arrays.sort(ss);
            b = OAArray.isEqual(ci.getDependentProperties(), ss);
        }
        assertTrue(b);
        
    }
    
}
