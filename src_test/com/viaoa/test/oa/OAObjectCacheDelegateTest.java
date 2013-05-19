package com.viaoa.test.oa;

import java.util.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import com.vetplan.oa.*;
import com.viaoa.test.*;

public class OAObjectCacheDelegateTest {

	
	void testFind() {
		methodName = "find";
		
		Pet pet = new Pet();
		pet.setName("pet");
		
		if (OAObjectCacheDelegate.find(Pet.class, "name", "pet") != pet) error("1");		
	}
	
	void testGet() {
		methodName = "get";
		
		Pet pet = new Pet();
		pet.setId("3");
		
		Pet p = (Pet) OAObjectCacheDelegate.get(Pet.class, 3);
		if (p != pet) error("1");
		if (OAObjectCacheDelegate.get(Pet.class, OAObjectKeyDelegate.getKey(pet)) != pet) error("2");
	}
	
	void testAdd() {
		methodName = "Add";
		
		Pet pet = new Pet();
		pet.setId("1");
		
		Object obj = OAObjectCacheDelegate.get(Pet.class, OAObjectKeyDelegate.getKey(pet));
		if (obj != pet) error("1");
	
		obj = OAObjectCacheDelegate.get(Pet.class, "1");
		if (obj != pet) error("1.1");
		
		
		Pet pet2 = new Pet();
		try {
			pet2.setId("1");
			error("1.2");
		}
		catch (Exception e) {
		}
		
		OAObjectCacheDelegate.add(pet, true, false);
	}	
	
	private int testListenerCnt;
	void testListener() {
		methodName = "Listener";
		HubListener hl = new HubListenerAdapter() {
			public @Override void afterPropertyChange(HubEvent e) {
				testListenerCnt++;
			}
		};
		OAObjectCacheDelegate.addListener(Pet.class, hl);
		
		Pet pet = new Pet();
		if (testListenerCnt != 0) error("0");

		pet.setName("dog"); // 2 prop change events, name & changed
		if (testListenerCnt != 2) error("0.1");
		
		HubListener[] hls = OAObjectCacheDelegate.getListeners(Pet.class);
		if (hls == null || hls.length != 1 || hls[0] != hl) error("1");
		
		hls = OAObjectCacheDelegate.getListeners(Client.class);
		if (hls != null && hls.length != 0) error("2");

		OAObjectCacheDelegate.removeListener(Exam.class, hl);
		hls = OAObjectCacheDelegate.getListeners(Pet.class);
		if (hls == null || hls.length != 1 || hls[0] != hl) error("3");
		
		OAObjectCacheDelegate.removeListener(Pet.class, hl);
		hls = OAObjectCacheDelegate.getListeners(Pet.class);
		if (hls != null && hls.length != 0) error("4");
	}
	
	
	void testNamedHub() {
		methodName = "NamedHub";

		if (OAObjectCacheDelegate.getNamedHub("xx") != null) error("1");
		
		Hub h = new Hub(Client.class);
		OAObjectCacheDelegate.setNamedHub("hc", h);
		
		if (h != OAObjectCacheDelegate.getNamedHub("hc")) error("2");
		if (OAObjectCacheDelegate.getNamedHub("hcx") != null) error("3");

		Hub h2 = new Hub(Client.class);
		OAObjectCacheDelegate.setNamedHub("hc", h2);
		if (h2 != OAObjectCacheDelegate.getNamedHub("hc")) error("4");
	}
	
	void testSelectAllHub() {
		methodName = "SelectAllHub";
		
		Hub h = new Hub(Client.class);
		OAObjectCacheDelegate.setSelectAllHub(h);
		
		Hub[] hs = OAObjectCacheDelegate.getSelectAllHubs(Client.class);
		if (hs == null || hs.length != 1 || hs[0] != h) error("1");
		
		Hub hx = OAObjectCacheDelegate.getSelectAllHub(Client.class);
		if (hx != h) error("2");

		Hub h2 = new Hub(Client.class);
		OAObjectCacheDelegate.setSelectAllHub(h2);
		
		hx = OAObjectCacheDelegate.getSelectAllHub(Pet.class);
		if (hx != null) error("3");

		hs = OAObjectCacheDelegate.getSelectAllHubs(Client.class);
		if (hs == null || hs.length != 2 || hs[0] != h || hs[1] != h2) error("4");
		
		OAObjectCacheDelegate.removeSelectAllHub(h);

		hs = OAObjectCacheDelegate.getSelectAllHubs(Client.class);
		if (hs == null || hs.length != 1 || hs[0] != h2) error("5");

		OAObjectCacheDelegate.removeSelectAllHub(h);
	
		hs = OAObjectCacheDelegate.getSelectAllHubs(Client.class);
		if (hs == null || hs.length != 1 || hs[0] != h2) error("6");

		OAObjectCacheDelegate.removeSelectAllHub(h2);
		
		hs = OAObjectCacheDelegate.getSelectAllHubs(Client.class);
		if (hs != null && hs.length != 0) error("7");
	}
	
	
	String methodName;
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}

	
	public void test() {
		testAdd();
		testSelectAllHub();
		testNamedHub();
		testListener();
		testGet();
		testFind();
	}
	
	public static void main(String[] args) {
		OAObjectCacheDelegateTest test = new OAObjectCacheDelegateTest();
		test.test();
		System.out.println("OAObjectCacheDelegateTest done");
	}
	

	
}
