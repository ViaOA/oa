package com.viaoa.test.hub;

import java.util.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import com.viaoa.test.*;
import com.vetplan.oa.*;

public class HubTest {
	Hub hubClient;
	Hub hubPet;
	Hub hubPetLinked;
	Hub hubExam;
	Hub hubExamItem;
	Hub hubItemCat;
	Hub hubItem;
	Hub hubSpecies;
	Hub hubBreed;
	Hub hubExamItemStatusEI;
	Hub hubExamItemStatusEIH;
	Hub hubExamItemHistory;
	Hub hubUser;
	
	void testHub3() {
		methodName = "hub3";
		testHub3Setup();
		testHub3Verify();
		testHub3RazzleDazzle();
		methodName = "hub3PostRD";
		testHub3Verify();
		testHub3RazzleDazzle2();
		methodName = "hub3PostRD2";
		testHub3Verify();
	}
	
	void testHub3Setup() {
		hubUser = mt.getUsers();
		hubClient = mt.getClients().createSharedHub();
		hubPet = hubClient.getDetailHub("pets");
		hubPetLinked = hubPet.createSharedHub();
		
		hubPetLinked.setLinkHub(hubClient, Client.PROPERTY_SpecialPet);
		
		hubExam = hubPet.getDetailHub("exams");
		hubExamItem = hubExam.getDetailHub("examItems");
		hubExamItemHistory = hubExamItem.getDetailHub("ExamItemHistories");
		
		hubItemCat = mt.getItemCategories().createSharedHub();
		hubItem = hubItemCat.getDetailHub("items");
		hubItem.setLinkHub(hubExamItem);
		
		hubSpecies = mt.getSpecies().createSharedHub();
		hubSpecies.setLinkHub(hubPet);
		hubBreed = hubSpecies.getDetailHub("breeds");
		hubBreed.setLinkHub("name", hubPet, "breed");
		if (hubItem.getAO() != null) error("1");
		
		hubExamItemStatusEI = mt.getExamItemStatuses().createSharedHub();
		hubExamItemStatusEI.setLinkHub(hubExamItem, ExamItem.PROPERTY_ExamItemStatus);
		
		hubExamItemStatusEIH = mt.getExamItemStatuses().createSharedHub();
		hubExamItemStatusEIH.setLinkHub(hubExamItemHistory, ExamItemHistory.PROPERTY_ExamItemStatus);
		hubExamItemHistory.setLinkHub(hubUser, User.PROPERTY_ExamItemHistory);
	}
	
	void testHub3Verify() {
		hubUser.setPos(0);
		ExamItemHistory eihHold=null;
		User user = (User) hubUser.getAO();
		for (int i=0; ;i++) {
			Client c = (Client) hubClient.getAt(i);
			if (c == null) break;
			Pet spx = c.getSpecialPet();
			hubClient.setAO(i);
			Pet sp1 = c.getSpecialPet();
			Pet sp2 = (Pet) hubPetLinked.getAO();
			if (sp1 != sp2) error("2");
			for (int ii=0; ;ii++) {
				Pet p = (Pet) hubPet.getAt(ii);
				if (p == null) break;
				Species sp = p.getSpecies();
				hubPet.setAO(ii);
				if (sp != hubSpecies.getAO()) error("2.1");
				String s = p.getBreed();
				Breed b = (Breed) hubBreed.getAO();
				if (b == null || !b.getName().equals(s)) error("2.2");
				
				for (int iii=0; ;iii++) {
					Exam e = (Exam) hubExam.getAt(iii);
					if (e == null) break;
					hubExam.setAO(iii);
					for (int iiii=0; ;iiii++) {
						ExamItem ei = (ExamItem) hubExamItem.getAt(iiii);
						if (ei == null) break;
						Item it = ei.getItem();
						hubExamItem.setAO(iiii);
						if (ei.getItem() != it) error("3.0");
						if (hubItem.getAO() != it) error("3.1");
						if (ei.getExamItemStatus() != hubExamItemStatusEI.getAO()) error("3.2");
						for (int iiiii=0; ;iiiii++) {
							ExamItemHistory eih = (ExamItemHistory) hubExamItemHistory.getAt(iiiii);
							if (eih == null) break;
							if (eihHold == null) eihHold = eih;
							hubExamItemHistory.setAO(eih);
							if (user.getExamItemHistory() != eih) error("4");
						}
					}
				}
			}
		}
		user.setExamItemHistory(eihHold);
		
		
	}
	
	void testHub3RazzleDazzle() {
		for (int i=0; ;i++) {
			Client c = (Client) hubClient.getAt(i);
			if (c == null) break;
			hubClient.setAO(i);
			hubPetLinked.setPos(getRandom(hubPet, true));
			for (int ii=0; ;ii++) {
				Pet p = (Pet) hubPet.getAt(ii);
				if (p == null) break;
				hubPet.setAO(ii);
				p.setSpecies(mt.getSpecie());
				hubBreed.setPos(getRandom(hubBreed, false));
				
				for (int iii=0; ;iii++) {
					Exam e = (Exam) hubExam.getAt(iii);
					if (e == null) break;
					hubExam.setAO(iii);
					for (int iiii=0; ;iiii++) {
						ExamItem ei = (ExamItem) hubExamItem.getAt(iiii);
						if (ei == null) break;
						hubItemCat.setAO(getRandom(hubItemCat, false));
						hubItem.setAO(getRandom(hubItem,false));
						hubExamItemStatusEI.setAO(getRandom(hubExamItemStatusEI, true));
					}
				}
			}
		}
	}

	void testHub3RazzleDazzle2() {
		Client c = (Client) hubClient.getAt(0);
		Hub h = c.getPets(); 
		c.delete();
		for (int i=0; ;i++) {
			Pet p = (Pet) h.getAt(i);
			if (p == null) break;
			if (p.getClient() != null) error("1");
		}
		
		for (int i=0; ;i++) {
			c = (Client) hubClient.getAt(i);
			if (c == null) break;
			c.save();
			
			hubClient.setAO(i);
			hubPetLinked.setPos(getRandom(hubPet, true));
			for (int ii=0; ;ii++) {
				Pet p = (Pet) hubPet.getAt(ii);
				if (p == null) break;
				p.save();
				hubPet.setAO(ii);
				p.setSpecies(mt.getSpecie());
				hubBreed.setPos(getRandom(hubBreed, false));
				
				for (int iii=0; ;iii++) {
					Exam e = (Exam) hubExam.getAt(iii);
					if (e == null) break;
					hubExam.setAO(iii);
					for (int iiii=0; ;iiii++) {
						ExamItem ei = (ExamItem) hubExamItem.getAt(iiii);
						if (ei == null) break;
						hubItemCat.setAO(getRandom(hubItemCat, false));
						hubItem.setAO(getRandom(hubItem,false));
						hubExamItemStatusEI.setAO(getRandom(hubExamItemStatusEI, true));
						ei.setItem(mt.getItem());
					}
				}
			}
		}
	}
	public int getRandom(Hub h, boolean bNull) {
		int max = h.getSize();
		int x = (int) (Math.random() * (max+(bNull?1:0)));
		if (x == max) x = -1;
		return x;
	}
	
	
	void testHub2() {
		methodName = "hub2";
	
		Hub hubClient = mt.getClients().createSharedHub();
		Hub hubPetDetail = hubClient.getDetailHub("pets");
				
		hubPetDetail.setLinkHub(hubClient, Client.PROPERTY_SpecialPet);
				
		hubClient.setAO(null);
		if (hubPetDetail.getAO() != null) error("1");
		Pet pet = new Pet();
		try {
			hubPetDetail.add(pet);
			error("1.1");
		}
		catch (Exception e) {
		}
	
		for (int i=0; ;i++) {
			Client c = (Client) hubClient.getAt(i);
			if (c == null) break;
			hubClient.setAO(c);
			if (hubPetDetail.getAO() == null) error("2");
			hubPetDetail.setAO(null);
			if (c.getSpecialPet() != null) error("3");
			Pet p = (Pet) hubPetDetail.getAt(2);
			hubPetDetail.setAO(p);
			if (c.getSpecialPet() != p) error("3");
		}
	}
	
	void testHub1() {
		methodName = "hub1";
		
		Hub hubClient = new Hub();
		Client c = new Client();
		hubClient.add(c);
		
		Hub h = OAObjectReflectDelegate.getReferenceHub(c, "pets", "");
		if (!Pet.class.equals(h.getObjectClass())) error("1");
		if (!h.isOAObject()) error("2");
		
		OAObjectKey k = new OAObjectKey("h1"); 
		h.add(k);
		k = new OAObjectKey("h2"); 
		h.add(k);
		
		Pet p1 = new Pet("h1");
		Pet p2 = new Pet("h2");
		
		h = c.getPets();

		
		Object obj = h.getObjectAt(0);
		if (obj == null) error("5");
		
	}
	void testHub() {
		methodName = "hub";
		
		Hub h = new Hub();
		if (h.getObjectClass() != null) error("1");
		if (h.isOAObject()) error("2");
		h.add(new OAObjectKey("1"));
		if (h.getObjectClass() != null) error("3");
		if (h.isOAObject()) error("4");
		
		Client c = new Client();
		h.add(c);
		if (h.getObjectClass() == null) error("5");
		if (!h.isOAObject()) error("6");
	}
	
	
	
	String methodName;
	ModelTest mt;
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}
	
	public void test() {
		mt = new ModelTest();
		try {
			testHub();
			testHub1();
			testHub2();
			testHub3();
		}
		catch (Exception e) {
			System.out.println("Error: in "+methodName+" "+e);
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		HubTest test = new HubTest();
		test.test();
		System.out.println("HubTest done");
	}
	
}


