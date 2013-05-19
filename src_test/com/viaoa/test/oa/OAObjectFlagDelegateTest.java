package com.viaoa.test.oa;

import java.util.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import com.vetplan.oa.*;

public class OAObjectFlagDelegateTest {
	String methodName;

	void testFlags() {
		methodName = "flags";
/*		
		Client c = new Client();
		OAObjectFlagDelegate.setInConstructor(c, true);
		if (!OAObjectFlagDelegate.isInConstructor(c)) error("1");
		OAObjectFlagDelegate.setInConstructor(c, true);
		OAObjectFlagDelegate.setInConstructor(c, false);
		if (OAObjectFlagDelegate.isInConstructor(c)) error("2");
		
		OAObjectFlagDelegate.setLoading(c, true);
		if (!OAObjectFlagDelegate.isLoading(c)) error("3");
		OAObjectFlagDelegate.setLoading(c, true);
		OAObjectFlagDelegate.setLoading(c, false);
		if (OAObjectFlagDelegate.isLoading(c)) error("4");
		
		OAObjectFlagDelegate.setThreadLoading(true);
		if (!OAObjectFlagDelegate.isThreadLoading()) error("5");
		OAObjectFlagDelegate.setThreadLoading(true);
		OAObjectFlagDelegate.setThreadLoading(false);
		if (OAObjectFlagDelegate.isThreadLoading()) error("6");
		
		OAObjectFlagDelegate.setDeleting(c, true);
		if (!OAObjectFlagDelegate.isDeleting(c)) error("7");
		OAObjectFlagDelegate.setDeleting(c, true);
		OAObjectFlagDelegate.setDeleting(c, false);
		if (OAObjectFlagDelegate.isDeleting(c)) error("8");
		
		OAObjectFlagDelegate.setThreadIgnoreEvents(true);
		if (!OAObjectFlagDelegate.getThreadIgnoreEvents()) error("9");
		OAObjectFlagDelegate.setThreadIgnoreEvents(true);
		OAObjectFlagDelegate.setThreadIgnoreEvents(false);
		if (!OAObjectFlagDelegate.getThreadIgnoreEvents()) error("10");
		OAObjectFlagDelegate.setThreadIgnoreEvents(false);
		if (OAObjectFlagDelegate.getThreadIgnoreEvents()) error("11");

		OAObjectFlagDelegate.setIgnoreAllEvents(true);
		if (!OAObjectFlagDelegate.getIgnoreAllEvents()) error("12");
		OAObjectFlagDelegate.setIgnoreAllEvents(true);
		OAObjectFlagDelegate.setIgnoreAllEvents(false);
		if (!OAObjectFlagDelegate.getIgnoreAllEvents()) error("13");
		OAObjectFlagDelegate.setIgnoreAllEvents(false);
		if (OAObjectFlagDelegate.getIgnoreAllEvents()) error("14");
*/		
	}
	
	
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}
	public void test() {
		testFlags();
	}
	
	public static void main(String[] args) {
		OAObjectFlagDelegateTest test = new OAObjectFlagDelegateTest();
		test.test();
		System.out.println("done");
	}
	
	
	
	
}
