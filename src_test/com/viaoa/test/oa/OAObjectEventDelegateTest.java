package com.viaoa.test.oa;

import java.util.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import com.vetplan.oa.*;

public class OAObjectEventDelegateTest extends HubListenerAdapter {
	String methodName;
	int cntPc;
	public @Override void afterPropertyChange(HubEvent e) {
		cntPc++;
		//System.out.println(cntPc+" "+e.getObject().getClass()+" prop:"+e.getPropertyName()+" new:"+e.getNewValue());
	}
	
	void testUpdateLink() {
		methodName = "updateLink";
		
		Client c1 = new Client();
		Client c2 = new Client();
		
		Pet p1 = new Pet("1");
		Pet p2 = new Pet("2");
		
		c1.getPets().add(p1);
		c1.getPets().add(p2);
		if (p1.getClient() != c1) error("1");
		if (p2.getClient() != c1) error("2");

		c1.getPets().setAO(p1);
		
		Hub[] hubs = new Hub[5];
		for (int i=0; i<hubs.length; i++) {
			hubs[i] = new Hub();
			hubs[i].add(p1);
			hubs[i].add(p2);
			hubs[i].setAO(p1);
		}
		
		p1.setClient(c2);
		if (c1.getPets().getAO() != null) error("3");
		for (int i=0; i<hubs.length; i++) {
			if (hubs[i].getAO() != p1) error("4");
		}
		if (c2.getPets().get(p1) == null) error("5");
	}

	public void testUpdateLink2() {
		methodName = "UpdateLink2";
		
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
	
	
	void testFirePropertyChange() {
		methodName = "firePropertyChange";
		
		Client client = new Client();
		ExamItemStatus eis = new ExamItemStatus();
		Hub h = new Hub();
		h.addHubListener(this);
		h.add(eis);
		if (!OAObjectReflectDelegate.getPrimitiveNull(eis, "type")) error("1");
		int x = cntPc;
		eis.setType(1);  // send changed, type
		if (cntPc != x+2) error("2");
		if (OAObjectReflectDelegate.getPrimitiveNull(eis, "type")) error("3");

		x = cntPc;
		eis.setType(1);
		if (cntPc != x) error("4");
		
		x = cntPc;
		eis.setType(2);
		if (cntPc != x+1) error("5");
		
		x = cntPc;
		eis.setProperty("type", null);
		if (cntPc != x+1) error("6");
		if (!OAObjectReflectDelegate.getPrimitiveNull(eis, "type")) error("7");
		
		OAObjectKey key = OAObjectKeyDelegate.getKey(eis);
		eis.setId("1");
		if (key.equals(OAObjectKeyDelegate.getKey(eis))) error("8");

/*		
		OAObjectFlagDelegate.setThreadIgnoreEvents(true);
		x = cntPc;
		eis.setType(2);
		OAObjectFlagDelegate.setThreadIgnoreEvents(true);
		if (cntPc != x) error("9");
*/		
		OAObjectFlagDelegate.setLoading(true);
		eis = new ExamItemStatus();
		OAObjectDelegate.setNew(eis, false);
		x = cntPc;
		eis.setType(5);
		OAObjectFlagDelegate.setLoading(false);
		if (cntPc != x) error("10");
		if (eis.getChanged()) error("11");
	}
	
	
	
	
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}

	public void test() {
		testFirePropertyChange();
		testUpdateLink();
		testUpdateLink2();
	}
	
	public static void main(String[] args) {
		OAObjectEventDelegateTest test = new OAObjectEventDelegateTest();
		test.test();
		System.out.println("done");
	}
	
}











