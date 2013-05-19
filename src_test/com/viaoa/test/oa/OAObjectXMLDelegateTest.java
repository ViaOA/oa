package com.viaoa.test.oa;

import java.util.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import com.vetplan.oa.*;
import com.viaoa.test.*;

public class OAObjectXMLDelegateTest {
	String fname = "c:\\temp\\test.xml";
	
	public void testXML() throws Exception {
		methodName = "XML";
		OAXMLWriter oxw = new OAXMLWriter(fname);
		
		Client c = mt.getClient();
		oxw.write(c);
		
		oxw.close();
		
		OAXMLReader oxr = new OAXMLReader(fname);
		Client c2 = (Client) oxr.read();
		if (c != c2) error("1 OK for now...todo: here");  //qqqqqqqq todo: this will create an error since the objects wont have an ID assigned - use NextNum DataSource
	}
	

	
	String methodName;
	ModelTest mt;
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}
	
	public void test() {
		mt = new ModelTest();
		
		try {
			testXML();
		}
		catch (Exception e) {
			System.out.println("Error: in "+methodName+" "+e);
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		OAObjectXMLDelegateTest test = new OAObjectXMLDelegateTest();
		test.test();
		System.out.println("OAObjectXMLDelegateTest done");
	}
	
	
}
