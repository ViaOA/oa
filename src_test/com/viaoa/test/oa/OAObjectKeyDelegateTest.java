package com.viaoa.test.oa;

import java.util.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import com.vetplan.oa.*;

public class OAObjectKeyDelegateTest {
	String methodName;

	public void testUpdateKey() {
		methodName = "updateKey";
		Client c = new Client();
		OAObjectKey key1 = OAObjectKeyDelegate.getKey(c); 
		c.setId("3");
		
		OAObjectKey key2 = OAObjectKeyDelegate.getKey(c);
		if (key1 == key2) error("1");
		key1 = key2;
		
		key2 = new OAObjectKey(new Object[] {"3"}, 99, false);  // ids, guid, bNew
		if (!key1.equals(key2)) error("1.1");
		if (key1.exactEquals(key2)) error("2");
		
		c.setId("3.1");
		key1 = OAObjectKeyDelegate.getKey(c);
		key2 = new OAObjectKey(new Object[] {"3.1"}, 99, false);  // ids, guid, bNew
		if (!key1.equals(key2)) error("3.2");
		if (key1.exactEquals(key2)) error("4");
		
		c.save();
		key1 = OAObjectKeyDelegate.getKey(c);
		key2 = new OAObjectKey(new Object[] {"3.1"}, 99, false);  // ids, guid, bNew
		if (!key1.equals(key2)) error("5");
		if (key1.exactEquals(key2)) error("6");
		
		try {
			c.setId("3.2");
			error("7");
		}
		catch (Exception e) {
		}

		key1 = OAObjectKeyDelegate.getKey(c);
		key2 = new OAObjectKey(new Object[] {"3.1"}, 99, false);  // ids, guid, bNew
		if (!key1.equals(key2)) error("8");
		if (key1.exactEquals(key2)) error("9");
		c.setId("3.1");

		Client c2 = new Client();
		try {
			c2.setId("3.1");
			error("10");
		}
		catch (Exception e) {
		}
		
		key1 = OAObjectKeyDelegate.getKey(c);
		key2 = new OAObjectKey(c2);
		if (!key1.equals(key2)) error("8.1");
		if (key1.exactEquals(key2)) error("9.1");
		
		c = new Client("5");
		Hub h = new Hub();
		h.add(c);
		Client cx = (Client) h.get("5");
		if (c != cx) error("11");
		
		c.setId("5.1");
		cx = (Client) h.get("5");
		if (cx != null) error("12");
		cx = (Client) h.get("5.1");
		if (c != cx) error("13");
		
		OAObjectFlagDelegate.setLoading(true);
		if (!OAObjectFlagDelegate.isLoading()) error("14");
		c.setId("5.2");
		if (!OAObjectFlagDelegate.isLoading()) error("15");
		OAObjectFlagDelegate.setLoading(false);
	}
	
	public void testConvertToObjectKey() {
		methodName = "convertToObjectKey";
		Client c = new Client("6");
		OAObjectKey key1 = OAObjectKeyDelegate.getKey(c); 
		OAObjectKey key2 = OAObjectKeyDelegate.convertToObjectKey(Client.class, new Integer(6));
		
		if (!key1.equals(key2)) error("1");
		Object[] ids = key2.getObjectIds();
		if (ids == null || ids.length != 1 || !"6".equals(ids[0])) error("2");
	}
	
	public void testGetKey() {
		methodName = "getKey";
		Client c = new Client();
		OAObjectKey key = OAObjectKeyDelegate.getKey(c); 
		if (key == null || key != OAObjectKeyDelegate.getKey(c)) error("1");
		c = new Client("1");
		key = OAObjectKeyDelegate.getKey(c); 
		if (key == null || key != OAObjectKeyDelegate.getKey(c)) error("2");
	}
	
	public void testKey() {
		methodName = "testKey";
		OAObjectKey key1 = new OAObjectKey(new Object[0]);
		if (key1.getGuid() != 0) error("0");  // must be 0, so that compare, equals know that it is not set.
		
		
		key1 = new OAObjectKey(null, 0, true);  // ids, guid, bNew
		OAObjectKey key2 = new OAObjectKey(null, 0, true);  // ids, guid, bNew
		if (!key1.equals(key2)) error("1");
		if (!key1.exactEquals(key2)) error("2");
		if (key1.compareTo(key2) != 0) error("2.0");
		
		key1 = new OAObjectKey(null, 2, true);  // ids, guid, bNew
		key2 = new OAObjectKey(null, 1, false);  // ids, guid, bNew
		if (key1.equals(key2)) error("1.1");
		if (key1.exactEquals(key2)) error("2.1");
		if (key1.compareTo(key2) <= 0) error("2.10");

		key1 = new OAObjectKey(null, 0, true);  // ids, guid, bNew
		key2 = new OAObjectKey(null,  1, false);  // ids, guid, bNew
		if (!key1.equals(key2)) error("1.12");
		if (key1.exactEquals(key2)) error("2.12");
		if (key1.compareTo(key2) != 0) error("2.121");  // if keys are null and guids >= 0, then the GUID is used to compare them
		
		
		key1 = new OAObjectKey(null, -1, true);  // ids, guid, bNew
		key2 = new OAObjectKey(null, 1, true);  // ids, guid, bNew
		if (key1.equals(key2)) error("1.2");
		if (key1.exactEquals(key2)) error("2.2");
		if (key1.compareTo(key2) >= 0) error("2.21");
		
		Object[] ids = new Object[1];
		ids[0] = "id";
		key1 = new OAObjectKey(ids, 0, true);   // ids, guid, bNew
		key2 = new OAObjectKey(ids, 1, false);  // ids, guid, bNew
		if (!key1.equals(key2)) error("3");
		if (key1.exactEquals(key2)) error("4");
		if (key1.compareTo(key2) != 0) error("2.22");

		key1 = new OAObjectKey(ids, 0, true);   // ids, guid, bNew
		key2 = new OAObjectKey(null, 1, false);  // ids, guid, bNew
		if (key1.equals(key2)) error("3.1");
		if (key1.exactEquals(key2)) error("4.1");
		if (key1.compareTo(key2) <= 0) error("4.12");
		if (key2.compareTo(key1) >= 0) error("4.13");
		
		Client c = new Client("id");
		key1 = OAObjectKeyDelegate.getKey(c);
		key2 = new OAObjectKey(ids, -1, false);  // ids, guid, bNew
		if (!key1.equals(key2)) error("5");
		if (key1.exactEquals(key2)) error("6");
	}

	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}

	public void test() {
		testKey();
		testGetKey();
		testUpdateKey();
		testConvertToObjectKey();
	}
	public static void main(String[] args) {
		OAObjectKeyDelegateTest test = new OAObjectKeyDelegateTest();
		test.test();
		System.out.println("done");
	}
	
	
}

