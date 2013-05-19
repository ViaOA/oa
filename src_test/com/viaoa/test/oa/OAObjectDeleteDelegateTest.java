package com.viaoa.test.oa;

import java.util.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import com.vetplan.oa.*;
import com.viaoa.test.*;

public class OAObjectDeleteDelegateTest {
	String methodName;
	
	void testDeleteMany2Many() {
		methodName = "DeleteMany2Many";
		
		Hub h = new Hub();
		for (int i=0; i<5; i++) {
			ItemCategory ic = new ItemCategory();
			h.add(ic);
			for (int j=0; j<5; j++) {
				Item item = new Item();
				ic.getItems().add(item);
				if (!item.getItemCategories().contains(ic)) error("1");
			}
		}
		
		for (int i=0; i<3; i++) {
			ItemCategory ic = (ItemCategory) h.elementAt(0);
			if (ic == null) break;
			Object[] items = ic.getItems().toArray();
			ic.delete();
			if (ic.getItems().getSize() > 0) error("2");
			for (int j=0; j<items.length; j++) {
				Item item = (Item) items[j];
				if (item.getItemCategories().contains(ic)) error("2.1");
			}
		}
		
		h.saveAll();
		for (int i=0; ;i++) {
			ItemCategory ic = (ItemCategory) h.elementAt(0);
			if (ic == null) break;
			Object[] items = ic.getItems().toArray();
			ic.delete();
			if (ic.getItems().getSize() > 0) error("3");
			for (int j=0; j<items.length; j++) {
				Item item = (Item) items[j];
				if (item.getItemCategories().contains(ic)) error("3.1");
			}
		}
		
		
		
	}	
	
	
	
	void testDelete() {
		methodName = "delete";
		Client c = new Client();
		c.setFirstName("firstName");
		
		c.delete();
		if (!c.getNew()) error("1");
		if (!c.getChanged()) error("1.1");

		Pet p = new Pet();
		c.getPets().add(p);
		c.delete();
		
		if (!p.getNew()) error("2");
		if (!p.getChanged()) error("2.1");
		if (p.getClient() != null) error("2.2");
		if (c.getPets().getSize() != 0) error("2.3");
		
	}
	
	void testDeleteRecursive() {
		methodName = "DeleteRecursive";
		ItemCategory icParent = mt.getItemCategory();
		Object[] objs = mt.getAllItemCategories(icParent);	
		if (objs.length == 0) error("0");
		Hub h = mt.getItemCategories();
		for (int i=0; i<objs.length; i++) {
			ItemCategory ic = (ItemCategory) objs[i];
			if (ic == icParent) {
				if (!h.contains(ic)) error("1.1");
			}
			else {
				if (ic.getParentItemCategory() == null) error("1");
				if (ic.getItems().getSize() == 0) error("1.2");
			}
		}
		
		
		icParent.delete();
		for (int i=0; i<objs.length; i++) {
			ItemCategory ic = (ItemCategory) objs[i];
			if (ic.getParentItemCategory() != null) error("2");
			if (h.contains(ic)) error("2.1");
			if (ic.getItems().getSize() > 0) error("2.2");
		}
	}
	
	void testDeleteRecursive2() {
		methodName = "DeleteRecursive2";
		Species sp = mt.getSpecie();
		
		Object[] objs = mt.getAllTemplateCategories(sp);	
		if (objs.length == 0) error("0");
		
		Hub h = sp.getTemplateCategories();
		for (int i=0; i<objs.length; i++) {
			TemplateCategory tc = (TemplateCategory) objs[i];
			if (tc.getSpecies() != sp) error("1");
			if (h.contains(tc)) {
				if (tc.getParentTemplateCategory() != null) error("1.1");
			}
			else {
				if (tc.getParentTemplateCategory() == null) error("1.2");
			}
		}
		
		sp.delete();
		if (h.getSize() > 0) error("2");
		for (int i=0; i<objs.length; i++) {
			TemplateCategory tc = (TemplateCategory) objs[i];
			if (tc.getSpecies() != null) error("2.1");
			if (tc.getParentTemplateCategory() != null) error("2.2");
		}
	}
	
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}

	ModelTest mt;
	public void test() {
		mt = new ModelTest();
		testDelete();
		testDeleteRecursive();
		testDeleteRecursive2();
		testDeleteMany2Many();
	}
	
	public static void main(String[] args) {
		OAObjectDeleteDelegateTest test = new OAObjectDeleteDelegateTest();
		test.test();
		System.out.println("OAObjectDeleteDelegateTest done");
	}
	
}



