package com.viaoa.test.hub;

import java.util.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import com.viaoa.test.*;
import com.vetplan.oa.*;

public class HubDelegateTest {

	String methodName;
	ModelTest mt;
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}
	
	public void test() {
		mt = new ModelTest();
		try {

		}
		catch (Exception e) {
			System.out.println("Error: in "+methodName+" "+e);
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		HubDelegateTest test = new HubDelegateTest();
		test.test();
		System.out.println("HubDelegateTest done");
	}
	
}
