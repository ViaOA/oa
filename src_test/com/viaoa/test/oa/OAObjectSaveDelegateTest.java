package com.viaoa.test.oa;

import java.util.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import com.vetplan.oa.*;
import com.viaoa.test.*;

public class OAObjectSaveDelegateTest {

	void testSave() {
		methodName = "Save";
		Client c = new Client();
		c.setFirstName("firstName");
		
		c.save();
		if (c.getNew()) error("1");
		if (c.getChanged()) error("1.1");
		
		Pet p = new Pet();
		c.getPets().add(p);
		c.save(OAObject.CASCADE_NONE);
		
		if (!p.getNew()) error("2");
		if (!p.getChanged()) error("2.1");
		
		c.save(OAObject.CASCADE_OWNED_LINKS);
		if (!p.getNew()) error("3");
		if (!p.getChanged()) error("3.1");
		
		c.save();
		if (p.getNew()) error("4");
		if (p.getChanged()) error("4.1");
	}	
	
	
	void testSaveRecursive() {
		methodName = "SaveRecursive";
		ItemCategory icParent = mt.getItemCategory();
		Object[] objs = mt.getAllItemCategories(icParent);	
		icParent.save(OAObject.CASCADE_NONE);
		for (int i=1; i<objs.length; i++) {
			ItemCategory ic = (ItemCategory) objs[i];
			if (!ic.getNew()) error("5");
			if (!ic.getChanged()) error("5.1");
		}
		icParent.save();
		for (int i=0; i<objs.length; i++) {
			ItemCategory ic = (ItemCategory) objs[i];
			if (ic.getNew()) error("6");
			if (ic.getChanged()) error("6.1");
		}
	}

	
	
	void testSaveMany2Many() {
		methodName = "SaveMany2Many";
		Item[] items = new Item[5];
		for (int i=0; i<items.length; i++) {
			items[i] = new Item();
		}
		
		ItemCategory[] ics = new ItemCategory[5];
		for (int i=0; i<ics.length; i++) {
			ics[i] = new ItemCategory();
		}
		
		for (int i=0; i<items.length; i++) {
			for (int j=0; j<ics.length; j++) {
				items[i].getItemCategories().add(ics[j]);
			}
		}
		
		// check both hubs to make sure both have same objects
		for (int i=0; i<items.length; i++) {
			if (items[i].getItemCategories().getSize() != 5) error("1");
			Object[] objs = HubAddRemoveDelegate.getAddedObjects(items[i].getItemCategories());
			if (objs.length != 5) error("1.1");
		}
		for (int j=0; j<ics.length; j++) {
			if (ics[j].getItems().getSize() != 5) error("2");
			Object[] objs = HubAddRemoveDelegate.getAddedObjects(ics[j].getItems());
			if (objs.length != 5) error("2.1");
			Item item = (Item) ics[j].getItems().elementAt(0); 
			ics[j].getItems().remove(0);
			objs = HubAddRemoveDelegate.getAddedObjects(ics[j].getItems());
			if (objs.length != 4) error("2.2");
			objs = HubAddRemoveDelegate.getRemovedObjects(ics[j].getItems());
			if (objs.length != 0) error("2.3");
			if (item.getItemCategories().getSize() != 4) error("2.4");
			if (item.getItemCategories().contains(ics[j])) error("2.5");
			ics[j].getItems().add(item);
			objs = HubAddRemoveDelegate.getAddedObjects(ics[j].getItems());
			if (objs.length != 5) error("2.6");
		}
		
		for (int i=0; i<items.length; i++) {
			items[i].save();
		}
		
		
	}	
	
	void testSaveOne2Many() {
		methodName = "save";
		Client c = new Client();
		c.save();
		if (c.getNew()) error("1");
		
		ExamItem eix = null;
		
		for (int i=0; i<5; i++) {
			Pet p = new Pet();
			c.getPets().add(p);
			for (int ii=0; ii<5; ii++) {
				Exam e = new Exam();
				p.getExams().add(e);
				for (int iii=0; iii<5; iii++) {
					ExamItem ei = new ExamItem();
					e.getExamItems().add(ei);
					eix = ei;
				}
			}
		}
		Object[] objs = HubAddRemoveDelegate.getAddedObjects(c.getPets());
		if (objs.length > 5) error("2");
		objs = HubAddRemoveDelegate.getRemovedObjects(c.getPets());
		if (objs.length != 0) error("2.1");
		
		if (!eix.getNew()) error("2.2");
		c.save(OAObject.CASCADE_NONE);
		
		if (c.getNew()) error("2.3");
		if (c.getChanged(c.CASCADE_NONE)) error("3");
		if (!eix.getNew()) error("4");
		c.save();
		if (eix.getNew()) error("5");
		objs = HubAddRemoveDelegate.getAddedObjects(c.getPets());
		if (objs.length != 0) error("5.1");
		objs = HubAddRemoveDelegate.getRemovedObjects(c.getPets());
		if (objs.length != 0) error("5.2");
		
		Exam ex = eix.getExam();
		eix.delete();
		if (!eix.getNew()) error("6");
		if (eix.getExam() != null) error("6.1");
		if (ex.getExamItems().getSize() != 4) error("6.1.1");
		
		
		eix = (ExamItem) ex.getExamItems().elementAt(0);
		ex.getExamItems().remove(eix);
		objs = HubAddRemoveDelegate.getRemovedObjects(ex.getExamItems());
		if (objs.length != 1) error("7");
		if (eix.getExam() != null) error("7.1");
		if (eix.getNew()) error("7.2");
		
		eix.delete();
		if (!eix.getNew()) error("7.3");
		ex.save();
		objs = HubAddRemoveDelegate.getRemovedObjects(ex.getExamItems());
		if (objs.length != 0) error("7.4");
	}
	
	
	ModelTest mt;
	String methodName;
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}
	public void test() {
		mt = new ModelTest();
		testSave();
		testSaveOne2Many();
		testSaveMany2Many();
		testSaveRecursive();
	}
	
	public static void main(String[] args) {
		OAObjectSaveDelegateTest test = new OAObjectSaveDelegateTest();
		test.test();
		System.out.println("OAObjectSaveDelegateTest done");
	}
	
}
