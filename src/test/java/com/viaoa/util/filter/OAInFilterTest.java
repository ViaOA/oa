package com.viaoa.util.filter;


import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.hifive.model.oa.*;
import test.hifive.model.oa.propertypath.*;


public class OAInFilterTest extends OAUnitTest {

    @Test
    public void test() throws Exception {

        // verify that a Product is in the list of AwardType.includeItems
        EmployeeAward ea = new EmployeeAward();
        AwardType at = new AwardType();
        ea.setAwardType(at);
        OAInFilter filter = new OAInFilter(ea, EmployeeAwardPP.awardType().includeItems().products().pp);
        
        Item item = new Item();
        at.getIncludeItems().add(item);
        
        Product prod = new Product();
        item.getProducts().add(prod);
        
        assertTrue(filter.isUsed(prod));
    }
    
}
