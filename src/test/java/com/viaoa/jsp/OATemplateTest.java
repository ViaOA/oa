package com.viaoa.jsp;

import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;
import com.viaoa.object.OAObject;
import com.viaoa.util.OATemplate;

import test.hifive.model.oa.Employee;


public class OATemplateTest extends OAUnitTest {

    @Test
    public void test() {
        Employee emp = new Employee();
        emp.setLastName("Smith");
        
        OATemplate<Employee> temp = new OATemplate<Employee>() {
            @Override
            protected Object getProperty(OAObject oaObj, String propertyName) {
                if (propertyName.equalsIgnoreCase("test")) {
                    return "Works";
                }
                return super.getProperty(oaObj, propertyName);
            }
        };
        temp.setTemplate("<%=lastName%>, <%=test%>");
        String s = temp.process(emp);
        assertEquals("Smith, Works", s);
    }
    
}
