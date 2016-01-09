package com.viaoa.object;

import org.junit.Test;

import static org.junit.Assert.*;

import com.tmgsc.hifivetest.model.oa.Location;
import com.viaoa.OAUnitTest;

public class OAObjectInfoDelegateTest extends OAUnitTest {
   
    
   @Test
   public void testRecursive() {
       OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Location.class);
       
       OAObjectHashDelegate.hashObjectInfo.clear();
       
       OALinkInfo li1 = oi.getRecursiveLinkInfo(OALinkInfo.MANY);
       assertNotNull(li1);
       
       OALinkInfo li2 = oi.getRecursiveLinkInfo(OALinkInfo.ONE);
       assertNotNull(li2);
       
       OAObjectHashDelegate.hashObjectInfo.clear();

       li2 = oi.getRecursiveLinkInfo(OALinkInfo.ONE);
       assertNotNull(li2);
       
       li1 = oi.getRecursiveLinkInfo(OALinkInfo.MANY);
       assertNotNull(li1);
   }
   
   @Test
   public void testLinkToOwner() {
       OAObjectHashDelegate.hashObjectInfo.clear();

       OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Location.class);
       
       OALinkInfo li = OAObjectInfoDelegate.getLinkToOwner(oi);
       
       assertNotNull(li);
       
       
   }
   
    
    
}
