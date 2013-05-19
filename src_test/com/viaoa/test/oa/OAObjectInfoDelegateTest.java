package com.viaoa.test.oa;


import java.util.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import com.vetplan.oa.*;

public class OAObjectInfoDelegateTest {

	String methodName;

	public void testGetOAObjectInfo() {
		methodName = "getOAObjectInfo";

		Hashtable h = OAObjectHashDelegate.getObjectInfoHash();
		int size = h.size();
		
		OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo((OAObject)null);
		if (oi == null) error("1");
		if (h.size() != ++size) error("1.1");
		
		OAObjectInfo oi2 = OAObjectInfoDelegate.getOAObjectInfo((OAObject)null);
		if (oi != oi2) error("2");
		if (h.size() != size) error("1.2");
		
		oi = OAObjectInfoDelegate.getOAObjectInfo(String.class);
		if (oi == null) error("3");
		if (h.size() != size) error("3.1");
		
		oi2 = OAObjectInfoDelegate.getOAObjectInfo(String.class);
		if (oi != oi2) error("4");
		if (h.size() != size) error("4.1");

		if (h.get(String.class) != oi2) error("4.2");
		
		Pet p = new Pet();
		oi = OAObjectInfoDelegate.getOAObjectInfo(p);
		if ( OAObjectInfoDelegate.getOAObjectInfo(p) != oi) error("5");
		
		oi = OAObjectInfoDelegate.getOAObjectInfo(Exam.class);
		ArrayList al = oi.getLinkInfos();
		if (al.size() != 8) error("6");
		
		String[] ss = OAObjectInfoDelegate.getPropertyNames(Exam.class);
		if (ss == null || ss.length != 25) error("7");
	}
	
	
	
	public void testGetRecursiveLinkInfo() {
		methodName = "getRecursiveLinkInfo";

		OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Pet.class);
		OALinkInfo li = OAObjectInfoDelegate.getRecursiveLinkInfo(oi, OALinkInfo.ONE);
		if (li != null) error("1");
		
		oi = OAObjectInfoDelegate.getOAObjectInfo(TemplateCategory.class);
		li = OAObjectInfoDelegate.getRecursiveLinkInfo(oi, OALinkInfo.ONE);
		if (li == null) error("2");
		if (li.getType() != li.ONE) error("3");
		
		li = OAObjectInfoDelegate.getRecursiveLinkInfo(oi, OALinkInfo.MANY);
		if (li == null) error("4");
		if (li.getType() != li.MANY) error("5");
	}
	
	public void testGetLinkToOwner() {
		methodName = "getLinkToOwner";
		
		OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(ExamItem.class);
		OALinkInfo li = OAObjectInfoDelegate.getLinkToOwner(oi);
		if (li== null) error("1");
		else {
			if (!"exam".equalsIgnoreCase(li.getName())) error("2");
		}
		
		oi = OAObjectInfoDelegate.getOAObjectInfo(Species.class);
		li = OAObjectInfoDelegate.getLinkToOwner(oi);
		if (li != null) error("3");
	}

	public void testSetRootHub() {
		methodName = "setRootHub";
		OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(ItemCategory.class);
		Hub h = new Hub(ItemCategory.class);
		OAObjectInfoDelegate.setRootHub(oi, h);
		if (OAObjectInfoDelegate.getRootHub(oi) != h) error("1");
	}

	public void testCacheHub() {
		methodName = "cacheHub";
		Exam e = new Exam();
		Hub h = e.getExamItems();
		OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(Exam.class, "examItems");
		if (li == null) error("1");
		if (!OAObjectInfoDelegate.isCached(li, h)) error("2");
		for (int i=0; i<li.getCacheSize(); i++) {
			e = new Exam();
			e.getExamItems();
		}
		if (OAObjectInfoDelegate.isCached(li, h)) error("3");
	}
	
	public void testGetReverseLinkInfo() {
		methodName = "getReverseLinkInfo";
		OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(Exam.class, "examItems");
		OALinkInfo rli = OAObjectInfoDelegate.getReverseLinkInfo(li);
		if (OAObjectInfoDelegate.getReverseLinkInfo(rli) != li) error("1");
		if (!li.getName().equalsIgnoreCase(rli.getReverseName())) error("2");
	}
	
	public void testType() {
		methodName = "testType";
		OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(Exam.class, "examItems");
		if (OAObjectInfoDelegate.isMany2Many(li)) error("1");
		if (OAObjectInfoDelegate.isOne2One(li)) error("2");
		li = OAObjectInfoDelegate.getLinkInfo(ItemCategory.class, "items");
		if (!OAObjectInfoDelegate.isMany2Many(li)) error("3");
		if (OAObjectInfoDelegate.isOne2One(li)) error("4");
	}
	
	public void testGetMethod() {
		methodName = "getMethod";
		OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Client.class);
		Object[] ms = OAObjectInfoDelegate.getAllMethods(oi);
		if (ms == null || ms.length < 20) error("1");
		Object m = OAObjectInfoDelegate.getMethod(Client.class, "getlastName");
		if (m == null) error("1.1");
		Object m2 = OAObjectInfoDelegate.getMethod(oi, "getlastName");
		if (m2 == null) error("2");
		if (m != m2) error("3");

		m = OAObjectInfoDelegate.getMethod(Client.class, "xwsc");
		if (m != null) error("4");
		m2 = OAObjectInfoDelegate.getMethod(oi, "xwsc");
		if (m2 != null) error("5");
	}
	
	public void testGetClass() {
		methodName = "getClass";
		OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Client.class);
		Class c = OAObjectInfoDelegate.getPropertyClass(oi, "lastName");
		if (!String.class.equals(c)) error("1");
		c = OAObjectInfoDelegate.getPropertyClass(oi, "pets");
		if (!Hub.class.equals(c)) error("2");

		c = OAObjectInfoDelegate.getPropertyClass(ExamItemStatus.class, "type");
		if (!int.class.equals(c)) error("3");
		
		c = OAObjectInfoDelegate.getHubPropertyClass(Client.class, "pets");
		if (!Pet.class.equals(c)) error("4");
	}
	
	public void testIsIdProperty() {
		methodName = "isIdProperty";
		OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Client.class);
		if (OAObjectInfoDelegate.isIdProperty(oi, "xxx")) error("1");
		if (!OAObjectInfoDelegate.isIdProperty(oi, "id")) error("2");
		if (OAObjectInfoDelegate.isIdProperty(oi, "lastName")) error("3");
		
		Client c = new Client();
		Object[] obj = OAObjectInfoDelegate.getPropertyIdValues(c);
		if (obj == null || obj.length != 1 || obj[0] != null) error("1");
	}
	
	public void testGetPropertyIdValues() {
		methodName = "getPropertyIdValues";
		Client c = new Client();
		Object[] obj = OAObjectInfoDelegate.getPropertyIdValues(c);
		if (obj == null || obj.length != 1 || obj[0] != null) error("1");
		c.setId("1");
		obj = OAObjectInfoDelegate.getPropertyIdValues(c);
		if (obj == null || obj.length != 1 || !"1".equals(obj[0])) error("2");
	}
	
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}

	public void test() {
		testGetOAObjectInfo();
		testGetRecursiveLinkInfo();
		testGetLinkToOwner();
		testSetRootHub();
		testCacheHub();
		testGetReverseLinkInfo();
		testType();
		testGetMethod();
		testGetClass();
		testIsIdProperty();
		testGetPropertyIdValues();
	}
	public static void main(String[] args) {
		OAObjectInfoDelegateTest test = new OAObjectInfoDelegateTest();
		test.test();
		System.out.println("done");
	}
	
}
