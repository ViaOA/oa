package com.viaoa.test.oa;

import java.util.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import com.vetplan.oa.*;

public class OAObjectDelegateTest {
	String methodName;

	public void testNulls() {
		methodName = "nulls";
		ExamItem ei = new ExamItem();
		String[] ssx = new String[] {"PRIORITY","SEQ","CHECKEDVALUE","SEVERITY","REMINDER","CARRIEDFORWARD","AUTOCHECKED","DONTPRINT","WELLNESSITEM","REASONFORVISIT","CLIENTDESCRIPTIONCHANGED","QUANTITY","REPORTTYPE","SHORTDESCRIPTIONONLY"};
		String[] ss = OAObjectDelegate.getNulls(ei);
		if (ss == null) error("1");
		else if (ss.length != ssx.length) error("2");
		for (int i=0; ss != null && i<ss.length; i++) {
			if (ss[i] == null) error("2");
			else if (!ss[i].equals(ss[i].toUpperCase())) error("3");
			else if (!ss[i].equals(ssx[i])) {
				error("4 "+ss[i] + ", "+ssx[i]);
			}
		}
		
	}
	
	public void testInitialize() {
		methodName = "initialize";
		
		ExamItemStatus eis = new ExamItemStatus();
		if (!OAObjectReflectDelegate.getPrimitiveNull(eis, "type")) error("1");
		
		if (OAObjectDelegate.getGuid(eis) < 1) error("1.1");
		
		eis = new ExamItemStatus();
		if (!OAObjectReflectDelegate.getPrimitiveNull(eis, "type")) error("2");
		if (OAObjectDelegate.getGuid(eis) < 1) error("3");
	}

	public void testSetNew(){
		methodName = "setNew";
		Client c = new Client();
		OAObjectKey key = OAObjectKeyDelegate.getKey(c);
		if (!c.getNew()) error("1");
		OAObjectDelegate.setNew(c, false);
		OAObjectKey key2 = OAObjectKeyDelegate.getKey(c);
		if (c.getNew()) error("2");
		if (key == key2) error("3");
		
		c = (Client) OAObjectReflectDelegate.createNewObject(Client.class);
		Object obj = OAObjectCacheDelegate.get(c);
		if (c != obj) error("4");
		OAObjectDelegate.finalizeObject(c);
		obj = OAObjectCacheDelegate.get(c);
		if (obj != null) error("5");
	}
	
	public void testGetChanged() {
		methodName = "getChanged";
		Client c = new Client();
		for (int i=0; i<5;i++) {
			Pet p = new Pet();
			c.getPets().add(p);
		}
		
		boolean b = OAObjectDelegate.getChanged(c, OAObject.CASCADE_LINK_RULES);
		if (!b) error("1");
		b = c.getChanged();
		if (!b) error("1.1");
		c.save();
		b = OAObjectDelegate.getChanged(c, OAObject.CASCADE_LINK_RULES);
		if (b) error("2");
		Pet p = new Pet();
		c.getPets().add(p);
		b = c.getChanged();
		if (!b) error("3");
	}
	
	private int cntC, cntP, cntE, cntEi;
	public void testRecurse() {
		methodName = "recurse";
		Client c = new Client();
		for (int i=0; i<5;i++) {
			Pet p = new Pet();
			c.getPets().add(p);
			for (int ii=0; ii<5;ii++) {
				Exam e = new Exam();
				p.getExams().add(e);
				for (int iii=0; iii<5;iii++) {
					ExamItem ei = new ExamItem();
					e.getExamItems().add(ei);
				}
			}
		}
		c.save();
		OAObjectDelegate.recurse(c, new OAObjectRecurseInterface() {
			boolean bError;
			public void updateObject(Object obj) {
				if (!bError && OAObjectDelegate.getChanged((OAObject) obj, OAObject.CASCADE_NONE)) {
					bError = true;
					error("1");
				}
				if (obj instanceof Client) cntC++;
				else if (obj instanceof Pet) cntP++;
				else if (obj instanceof Exam) cntE++;
				else if (obj instanceof ExamItem) cntEi++;
			}
		});
		if (cntC != 1 || cntP != 5 || cntE != 25 || cntEi != 125) error("2");
	}
	
	public void testFind() {
		methodName = "find";
		Client c = new Client();
		for (int i=0; i<5;i++) {
			Pet p = new Pet();
			p.setBreed(i+"");
			c.getPets().add(p);
			for (int ii=0; ii<5;ii++) {
				Exam e = new Exam();
				p.getExams().add(e);
				for (int iii=0; iii<5;iii++) {
					ExamItem ei = new ExamItem();
					ei.setInstruction(i+"."+ii+"."+iii);
					e.getExamItems().add(ei);
				}
			}
		}
		Object obj = c.find("pets.exams.examItems.Instruction", "1.2.4");
		if (obj == null) error("1");
		obj = c.find("pets.exams.examItems.Instruction", "3.2.4");
		if (obj == null) error("1");
	}
	
	
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}

	public void test() {
		testNulls();
		testInitialize();
		testSetNew();
		testGetChanged();
		testRecurse();
		testFind();
	}
	
	public static void main(String[] args) {
		OAObjectDelegateTest test = new OAObjectDelegateTest();
		test.test();
		System.out.println("done");
	}
	
}

