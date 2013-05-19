package com.viaoa.test.hub;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.test.ModelTest;
import com.vetplan.oa.*;


public class HubDataDelegatetTest {

	
	
	
	
	
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
		HubDataDelegatetTest test = new HubDataDelegatetTest();
		test.test();
		System.out.println("HubDataDelegatetTest done");
	}
	
	
	
}
