package com.viaoa.test.oa;


import java.util.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import com.vetplan.oa.*;

public class OAObjectReflectDelegateTest extends HubListenerAdapter {
	String methodName;

	
	public void testIsPropertyLoaded() {
		methodName = "isPropertyLoaded";
		Pet p = new Pet("6.");
		if (OAObjectReflectDelegate.isPropertyLoaded(p, "client") ) error("1");

		OAObjectFlagDelegate.setLoading(true);
		p.setProperty("client", "6.");
		OAObjectFlagDelegate.setLoading(false);

		if (OAObjectReflectDelegate.isPropertyLoaded(p, "client") ) error("2");
		if (!(OAObjectReflectDelegate.getRawReference(p, "client") instanceof OAObjectKey)) error("2.1");

		Client c = new Client("6.");
		if (!OAObjectReflectDelegate.isPropertyLoaded(p, "client") ) error("3");

		c.setProperty("pets", new OAObjectKey(6));;
		if (!OAObjectReflectDelegate.isPropertyLoaded(c, "pets") ) error("4");

		
		Hub h = (Hub) OAObjectReflectDelegate.getRawReference(c, "pets");
		if (!h.getObjectClass().equals(OAObjectKey.class)) error("5");

		if (h.isOAObject()) error("6");
		h = c.getPets();
		if (!h.isOAObject()) error("6.1");
	}

	public void testGetPropertyObjectKey() {
		methodName = "getPropertyObjectKey";

		Client c = new Client("5");
		Pet p = new Pet();
		OAObjectKey key = OAObjectReflectDelegate.getPropertyObjectKey(c, "client");
		if (key != null) error("1");
		
		OAObjectFlagDelegate.setLoading(true);
		p.setProperty("client", "5");
		OAObjectFlagDelegate.setLoading(false);
		
		key = OAObjectReflectDelegate.getPropertyObjectKey(p, "client");
		if (key == null) error("2");
	}
	
	public void testGetReferenceHub() {
		methodName = "getReferenceHub";
		
		Species sp = new Species();
		Hub h1 = sp.getTemplateCategories();
		String s = h1.getSelectRequiredWhere();
		if (!"ParentTemplateCategory == null".equals(h1.getSelectRequiredWhere())) error("1");
		if (h1.getRootHub() != h1) error("1.1");
		
		for (int i=0; i<5; i++) {
			TemplateCategory tc = new TemplateCategory();
			h1.add(tc);
			if (tc.getSpecies() != sp) error("2");
			Hub h2 = tc.getTemplateCategories();
			if (h2.getRootHub() != h1) error("2.0");
			for (int j=0; j<5; j++) {
				TemplateCategory tc2 = new TemplateCategory();
				h2.add(tc2);
				if (tc2.getParentTemplateCategory() != tc) error("2.1");
				if (tc2.getSpecies() != sp) error("2.1.1");
				Hub h3 = tc2.getTemplateCategories();
				if (h3.getRootHub() != h1) error("2.1.2");
				for (int k=0; k<5; k++) {
					TemplateCategory tc3 = new TemplateCategory();
					h3.add(tc3);
					if (tc3.getParentTemplateCategory() != tc2) error("2.2");
					if (tc3.getSpecies() != sp) error("2.2.1");
				}			
			}			
		}
		
		Species sp1 = new Species();
		TemplateCategory tc = (TemplateCategory) sp.getTemplateCategories().elementAt(0);
		tc.setSpecies(sp1);
		
		verifySpecies(sp.getTemplateCategories(), sp);
		verifySpecies(sp1.getTemplateCategories(), sp1);
	}
	
	private void verifySpecies(Hub hub, Species species) {
		for (int i=0; ; i++) {
			TemplateCategory tc = (TemplateCategory) hub.elementAt(i);
			if (tc == null) break;
			if (tc.getSpecies() != species) error("4");
			verifySpecies(tc.getTemplateCategories(), species);
		}
	}
	
	public void testGetReferenceObject() {
		methodName = "getReferenceObject";
		Client c = new Client("4");
		OAObjectFlagDelegate.setLoading(true);
		Pet pet = new Pet();
		if (pet.getClient() != null) error("1");
		pet.setProperty("client", 4);
		OAObjectFlagDelegate.setLoading(false);
		
		pet = new Pet();
		OAObjectFlagDelegate.setLoading(true);
		pet.setProperty("client", 12);  // invalid client Id
		OAObjectFlagDelegate.setLoading(false);
		if (pet.getClient() != null) error("1.1");

		pet = new Pet();
		OAObjectFlagDelegate.setLoading(true);
		pet.setProperty("client", 4); 
		OAObjectFlagDelegate.setLoading(false);
		if (pet.getClient() != c) error("2");
		try {
			pet.setProperty("client", 12);  // invalid, will throw exception
			error("3");
		}
		catch (Exception e) { }
		if (pet.getClient() != c) error("3.1");
	}
	
	public void testGetObject() {
		// todo: additonal tests when Database and OAClient are being used.
		methodName = "getObject";
		ExamItemStatus eis = new ExamItemStatus("3");
		
		Integer id = new Integer(3); // test to make sure that object with duplicate id can not be created
		if (OAObjectReflectDelegate.getObject(ExamItemStatus.class, id) != eis ) error("1");
		try {
			eis = new ExamItemStatus("3");
			error("2");
		}
		catch (Exception e) {
		}
	}

	
	public void testGetProperty() {
		methodName = "getProperty";
		
		ExamItemStatus eis = new ExamItemStatus();
		if (eis.getProperty("showOnReport") != null) error("1");
		if (eis.getProperty("type") != null) error("2");
		
		eis.setShowOnReport(true);
		if (eis.getProperty("showOnReport") == null) error("3");
		
		eis.setProperty("name", "name");
		if (!"name".equals(eis.getName())) error("3.1");
		
		ExamItem ei = new ExamItem();
		ei.setExamItemStatus(eis);
		
		if (ei.getProperty("examItemStatus.type") != null) error("4");
		if (ei.getProperty("examItemStatus.showOnReport") == null) error("5");
		
		Exam exam = new Exam();
		ei.setExam(exam);
		if (exam.getProperty("examitems.examItemStatus.showOnReport") != null) error("5.1");
		exam.getExamItems().setPos(0);
		if (exam.getProperty("examitems.examItemStatus.showOnReport") == null) error("6");
		if (!"name".equals(exam.getProperty("examitems.examItemStatus.name"))) error("7");
		
		eis.setProperty("xxx", "xxx");
		if (!"xxx".equals(eis.getProperty("xxx"))) error("8");
		eis.setProperty("xxx", "xxxX");
		if (!"xxxX".equals(eis.getProperty("xxx"))) error("8.1");
		
		eis.setProperty("xxx", null);
		if (eis.getProperty("xxx") != null) error("8.2");
	}	
	
	public void testSetProperty() {
		methodName = "setProperty";
		
		// use setProperty to set Property
		Client c = new Client();
		c.setProperty("id", 1);
		
		c = new Client();
		c.setProperty("id", 1.2);
		if (!c.getId().equals("1.2")) error("2");
		
		c = new Client();
		c.setProperty("id", "xxx");
		if (!c.getId().equals("xxx")) error("3");

		c = new Client();
		c.setProperty("id", "004", "3R0");
		if (!c.getId().equals("004")) error("4");
	
		// use setProperty to add Hub items
		ArrayList al = new ArrayList(); 
		for (int i=0; i<5; i++) {
			c.setProperty("pets", i+1);
	  		Pet pet = new Pet((i+1)+"");
	  		al.add(pet);
		}		
		for (int i=5; i<10; i++) {
	  		Pet pet = new Pet((i+1)+"");
			c.setProperty("pets", i+1);
	  		al.add(pet);
		}		
		
		Hub h = c.getPets();
		for (int i=0; i<al.size(); i++) {
			Pet p = (Pet) al.get(i);
			Pet p2 = (Pet) h.elementAt(i);
			if (p != p2) error("5");
		}

		// use OANullObject
		c = new Client();
		c.setProperty("lastName", "lastName");
		if (!"lastName".equals(c.getLastName())) error("6");
		c.setProperty("lastName", null);
		if (c.getLastName() != null) error("7");
		c.setProperty("firstName", OANullObject.nullObject);
		if (c.getFirstName() != null) error("8");
		
		// primitive type
		ExamItemStatus eis = new ExamItemStatus();
		eis.setProperty("type", "5");
		if (eis.getType() != 5) error("9");
		eis.setProperty("type", 5);
		if (eis.getType() != 5) error("9.1");
		eis.setProperty("type", null);
		if (eis.getType() != 0) error("9.2");
		if (!OAObjectReflectDelegate.getPrimitiveNull(eis, "type")) error("9.3");
		eis.setType(0);  // should remove nullFlag
		if (OAObjectReflectDelegate.getPrimitiveNull(eis, "type")) error("9.4");
		int x = eis.debug_setTypeCnt;
		eis.setProperty("type", null);
		if (!OAObjectReflectDelegate.getPrimitiveNull(eis, "type")) error("9.5");
		if (eis.debug_setTypeCnt != x) error("9.6");
		eis.setId("1");
		
		// reference type
		ExamItem ei = new ExamItem();
		x = ei.debug_setExamItemStatusCnt;
		ei.setProperty("examItemStatus", null);
		if (ei.examItemStatus != null) error("10.1");
		if (x != ei.debug_setExamItemStatusCnt) error("10.2");
		OAObjectFlagDelegate.setLoading(true);
		ei.setProperty("examItemStatus", 1);
		OAObjectFlagDelegate.setLoading(false);
		if (x != ei.debug_setExamItemStatusCnt) error("10.3");
		ei.setProperty("examItemStatus", null);
		if (ei.examItemStatus != null) error("10.4");
		x++;
		if (x != ei.debug_setExamItemStatusCnt) error("10.5");
		ei.setProperty("examItemStatus", 1);
		x++;
		if (x != ei.debug_setExamItemStatusCnt) error("10.6");
		ExamItemStatus eisx = ei.getExamItemStatus();
		if (eisx == null) error("10.7");
		ei.setProperty("examItemStatus", 1);
		if (x != ei.debug_setExamItemStatusCnt) error("10.8");
		eisx = new ExamItemStatus("2");
		ei.setProperty("examItemStatus", 2);
		x++;
		if (x != ei.debug_setExamItemStatusCnt) error("10.9");
		if (ei.getExamItemStatus() != eisx) error("10.10");
		ei.setExamItemStatus(null);
		x++;
		if (x != ei.debug_setExamItemStatusCnt) error("10.11");
		OAObjectFlagDelegate.setLoading(true);
		ei.setProperty("examItemStatus", 1);
		if (x != ei.debug_setExamItemStatusCnt) error("10.12");
		OAObjectFlagDelegate.setLoading(false);
		ei.setProperty("examItemStatus", eis);
		if (x != ei.debug_setExamItemStatusCnt) error("10.13");
		ei.setProperty("examItemStatus", null);
		if (++x != ei.debug_setExamItemStatusCnt) error("10.14");
		OAObjectFlagDelegate.setLoading(true);
		ei.setProperty("examItemStatus", 1);
		OAObjectFlagDelegate.setLoading(false);
		if (ei.examItemStatus != null) error("10.14.1");
		if (ei.getExamItemStatus() != eis) error("10.14.2");
		ei.setProperty("examItemStatus", 2);
		if (++x != ei.debug_setExamItemStatusCnt) error("10.15");
		if (ei.getExamItemStatus() != eisx) error("10.15.1");
		eisx.setProperty("showOnReport", 1);
		if (!eisx.getShowOnReport()) error("10.16");
		eisx.setProperty("showOnReport", false);
		if (eisx.getShowOnReport()) error("10.16.1");
		eisx.setProperty("showOnReport", "true");
		if (!eisx.getShowOnReport()) error("10.16.2");
	}
	
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}
	
	public void test() {
		testSetProperty();
		testGetProperty();
		testGetObject();
		testGetReferenceObject();
		testGetReferenceHub();
		testGetPropertyObjectKey();
		testIsPropertyLoaded();
	}
	public static void main(String[] args) {
		OAObjectReflectDelegateTest test = new OAObjectReflectDelegateTest();
		test.test();
		System.out.println("done");
	}
	
}



