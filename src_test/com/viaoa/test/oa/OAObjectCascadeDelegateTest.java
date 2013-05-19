package com.viaoa.test.oa;

import java.util.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import com.vetplan.oa.*;
import com.viaoa.test.*;


public class OAObjectCascadeDelegateTest {

	void testCascade() {
		methodName = "Cascade";
		
		OACascade c = OAObjectCascadeDelegate.getCascade("test");

		OACascade c1 = OAObjectCascadeDelegate.getCascade("test");
		if (c1 != c) error("0");
		if (c1.iUsed != 2) error("0.1");
		c1.release();
		if (c1.thread == null) error("0.2");  
		
		Pet pet = new Pet();
		if (OAObjectCascadeDelegate.hasBeenCalled(pet, c, true)) error("1");
		if (!OAObjectCascadeDelegate.hasBeenCalled(pet, c, true)) error("2");
		c.release();
		if (c.thread != null) error("2.1");  
		
		OACascade c2 = OAObjectCascadeDelegate.getCascade("test");
		if (c != c2) error("3");
		c2.release();
		if (c2.thread != null) error("4");  
	}
	
	
	String methodName;
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}
	
	public void test() {
		testCascade();
	}
	
	public static void main(String[] args) {
		OAObjectCascadeDelegateTest test = new OAObjectCascadeDelegateTest();
		test.test();
		System.out.println("OAObjectCascadeDelegateTest done");
	}
	
}


