package com.viaoa.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;

import com.viaoa.OAUnitTest;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAFinder;
import com.viaoa.object.OALinkInfo;

import test.hifive.HifiveDataGenerator;
import test.hifive.delegate.ModelDelegate;
import test.hifive.model.oa.*;
import test.hifive.model.oa.propertypath.*;

public class OAPropertyPathTest extends OAUnitTest {

    @Test
    public void test() {
        
    }

    @Test
    public void test2() {
        init();
        
        String spp = ProgramPP.locations().employees().pp;
        OAPropertyPath<Program> pp = new OAPropertyPath<>(Program.class, spp);

        OALinkInfo[] lis = pp.getRecursiveLinkInfos();
        assertEquals(2, lis.length);
        assertNotNull(lis[0]);
        assertNull(lis[1]);
    }
    
}
