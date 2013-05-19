package com.viaoa.test.oa;

import java.util.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import com.vetplan.oa.*;
import com.viaoa.test.*;


public class OAObjectLockDelegateTest {
	
	void testLock() {
		methodName = "lock";
		Item item = new Item();
		item.lock();
		if (!item.isLocked()) error("1");
		item.unlock();
		if (item.isLocked()) error("2");
		
		Object ref = new Object();
		item.lock(ref);
		OALock ol = item.getLock();
		if (ol == null || ol.getReferenceObject() != ref) error("3");
		item.unlock();
		if (item.isLocked()) error("4");
		
	}

	
	String methodName;
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}
	public void test() {
		testLock();
	}
	
	public static void main(String[] args) {
		OAObjectLockDelegateTest test = new OAObjectLockDelegateTest();
		test.test();
		System.out.println("OAObjectLockDelegateTest done");
	}
	
}
