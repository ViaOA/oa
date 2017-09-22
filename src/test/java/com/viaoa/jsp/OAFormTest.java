package com.viaoa.jsp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;


public class OAFormTest extends OAUnitTest {

    @Test
    public void test() {
        OAForm form = new OAForm();
        String id = "cmdTest";
        OAButton but = new OAButton(id);
        form.add(but);
        
        assertEquals(but, form.getComponent(id));
        assertEquals(but, form.getButton(id));
        
        form.remove(id);
        assertNull(form.getComponent(id));
        assertNull(form.getButton(id));
    }
    
    @Test
    public void test2() {
        OAForm form = new OAForm();
        String id = "cmdTest";
        OAButton but = new OAButton(id) {
            // subclass
        };
        form.add(but);
        
        assertEquals(but, form.getComponent(id));
        assertEquals(but, form.getButton(id));
        
        form.remove(id);
        assertNull(form.getComponent(id));
        assertNull(form.getButton(id));
    }
    
    @Test
    public void test3() {
        OAForm form = new OAForm();
        String id = "cmdTest";
        OAButton but = new OAButton(id);
        form.add(but);

        OAList lst = new OAList(id, null, null);
        form.add(lst);

        assertNotNull(form.getComponent(id));
        
        assertEquals(but, form.getButton(id));
        assertEquals(lst, form.getList(id));
        
        form.remove(id);
        assertNotNull(form.getComponent(id));
        form.remove(id);
        assertNull(form.getComponent(id));
    }
    
}
