package com.viaoa.test.oa;

import java.util.*;
import java.io.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import com.vetplan.oa.*;
import com.viaoa.test.*;

public class OAObjectSerializeDelegateTest {

	void testWriteObject() throws Exception {
		FileOutputStream os = new FileOutputStream("c:\\temp\\object.xxx");
		
		ObjectOutputStream oos = new ObjectOutputStream(os);

		OAObjectSerializeDelegate.addSerializedPropertyListener(new OAObjectSerializeInterface() {
			String[] ALL = new String[0];
			public String[] getSerializedProperties(Object obj) {
				return ALL;
			}
		});
		
		Client c = mt.getClient();
		oos.writeObject(c);
		oos.close();

		FileInputStream is = new FileInputStream("c:\\temp\\object.xxx");
		ObjectInputStream ois = new ObjectInputStream(is);
		Object obj = ois.readObject();
		ois.close();
		if (obj != c) error("1");
	}
	
	void testDate() throws Exception {
		FileOutputStream os = new FileOutputStream("c:\\temp\\object.xxx");
		ObjectOutputStream oos = new ObjectOutputStream(os);
		int cnt=0;
		for (int i=0; i<100; i++) {
			oos.writeObject(new OADate());
			oos.writeObject(new OADateTime());
			oos.writeObject(new OATime());
			cnt += 3;
		}
		oos.close();
		
		FileInputStream is = new FileInputStream("c:\\temp\\object.xxx");
		ObjectInputStream ois = new ObjectInputStream(is);
		
		for (int i=0; i<cnt;i++) {
			Object obj = ois.readObject();
			if (obj == null) break;
			// System.out.println(i+"--> "+obj);
		}
		ois.close();
	}
	
	
	String methodName;
	ModelTest mt;
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}
	
	public void test() {
		mt = new ModelTest();
		try {
			testWriteObject();
			testDate();
		}
		catch (Exception e) {
			System.out.println("Error: in "+methodName+" "+e);
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		OAObjectSerializeDelegateTest test = new OAObjectSerializeDelegateTest();
		test.test();
		System.out.println("OAObjectSerializeDelegateTest done");
	}
	
	
}
