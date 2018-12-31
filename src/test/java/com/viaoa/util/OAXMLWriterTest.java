package com.viaoa.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.PrintWriter;

import com.viaoa.OAUnitTest;
import com.viaoa.hub.Hub;

import test.hifive.model.oa.Employee;

public class OAXMLWriterTest extends OAUnitTest {

    @Test
    public void test() {
        Hub<Employee> hub = new Hub<>(Employee.class);
        hub.select();
        
        hub.add(new Employee());
        
        System.out.println("Start, hub="+hub);
        
        PrintWriter pw = new PrintWriter(System.out);
        OAXMLWriter w = new OAXMLWriter(pw) {
            public int shouldWriteProperty(Object obj, String propertyName, Object value) {
                if("empQueryVets".equalsIgnoreCase(propertyName)) return 2;
                return 0;
            }

            boolean bPw = false;
            @Override
            public void print(String line) {
                if (line != null && "<password>".equalsIgnoreCase(line.trim())) {
                    bPw = true;
                }
                super.print(line);
            }
            @Override
            public void printXML(String line) {
                if (bPw) {
                    bPw = false;
                    line = "";
                }
                super.printXML(line);
            }
            @Override
            public void printCDATA(String line) {
                if (bPw) {
                    bPw = false;
                    line = "***";
                }
                super.printCDATA(line);
            }
        };        
        
        w.write(hub);
        pw.flush();
        System.out.println("Done");
        
    }
    
}
