package com.viaoa.jsp;

import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.*;

import com.viaoa.OAUnitTest;


public class OAButtonListTest extends OAUnitTest {

    OAForm form;
    OAButtonList blist;
    
    @Before
    public void setUp() throws Exception {
          
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testFormReturnsButtonListById() {
          form = new OAForm("formid", "formURL");
          blist = new OAButtonList("buttonListId", null, null);
          form.add(blist);
          
          assertEquals(blist, form.getComponent("buttonListId"));
    }

    
}
