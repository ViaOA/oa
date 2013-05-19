package com.viaoa.test.oa;

import java.util.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import com.vetplan.oa.*;

public class OAObjectHubDelegateTest {
	String methodName;

	public void testHubs() {
		Client c = new Client();
		if (OAObjectHubDelegate.isInHub(c)) error("1");
		Hub h = new Hub();
		h.add(c);
		if (!OAObjectHubDelegate.isInHub(c)) error("2");
		
		OAObjectHubDelegate.removeHub(c, h);
		if (OAObjectHubDelegate.isInHub(c)) error("3");
		
		c = new Client();
		for (int i=0; i<5; i++) {
			h = new Hub();
			h.add(c);
			Hub[] hs = OAObjectHubDelegate.getHubs(c); 
			if (hs == null || hs.length != (i+1)) error("4");
		}
	}
	
	
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}

	public void test() {
		testHubs();
	}
	
	public static void main(String[] args) {
		OAObjectDelegateTest test = new OAObjectDelegateTest();
		test.test();
		System.out.println("done");
	}
	
}
