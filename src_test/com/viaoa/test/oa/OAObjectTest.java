package com.viaoa.test.oa;

import java.util.Hashtable;
import java.util.logging.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;

import com.vetplan.oa.*;

public class OAObjectTest extends HubListenerAdapter {
	
	public @Override void afterAdd(HubEvent e) {
		//System.out.println("hubAdd "+e.getHub());
	}
	public @Override void afterSort(HubEvent e) {
		//System.out.println("hubAfterSort "+e.getHub());
	}
	public @Override void beforeSelect(HubEvent e) {
		//System.out.println("hubBeforeSelect "+e.getHub());
	}
	public @Override void afterChangeActiveObject(HubEvent evt) {
		System.out.println("hubChangeActiveObject "+evt.getHub());
	}	
	public @Override void beforeDelete(HubEvent e) {
		System.out.println("hubDelete "+e.getHub());
	}
	public @Override void afterInsert(HubEvent e) {
		System.out.println("hubInsert "+e.getHub());
	}
	public @Override void afterMove(HubEvent e) {
		System.out.println("hubMove "+e.getHub());
	}
	public @Override void onNewList(HubEvent e) {
		System.out.println("hubNewList "+e.getHub());
	}
	public @Override void afterPropertyChange(HubEvent e) {
		System.out.println("hubPropertyChange "+e.getHub());
	}
	public @Override void afterRemove(HubEvent e) {
		System.out.println("hubAfterRemove "+e.getHub());
	}
	public @Override void afterSave(HubEvent e) {
		System.out.println("hubSave "+e.getHub());
	}

	public void testGetProperty() {
	}

	/*
		Non props: setting, resetting, null value, changed flag, events
				
		Real Props reg and prim: watch for directly setting, calling setMethod, setting changedFlag, 
		
		Ref: Props: setLoading(), without prev value, with prev value, null, key, key value, real value.  Setting and chanding
		Hub:
				
		primitve value number, string, etc. (boolean, char, ..., int, double)
		prim value to null, with prev value
		prim value to real value, when it was prev null
			
		regular property (String, etc) to/fromo value, to/from null
 	*/	
	public void testSetProperty() {

		ExamItemStatus eis = new ExamItemStatus();
		eis.setId("1");
		eis.setProperty("xxx", 4);

		ExamItem ei = new ExamItem();
		ei.setExamItemStatus(eis);

		Client c = new Client();
		c.setId("1");
		c.setLastName("Jones");
		c.setFirstName("Chris");
		for (int ii=0; ii<5; ii++) {
	  		Pet pet = new Pet("x"+ii);
	  		Hub h = c.getPets();
	  		h.add(pet);
		}		
  		c.getPets().setPos(0);
		
		
		Object obj = c.getProperty("pets.client.pets.id");
	
// Reiterate		
		
//	ReflectDel:	 test getHub(), getObject(), getObject()
		
// moving pet to another cient hub, setting pet.client to new value
//test setProperty(Hub prop)
//test recursive, OAObjectEventDelegate.updateLink code		
		
		System.out.println("Done");
}
	
	
	public void testx() {
		Hub hubAllClients = new Hub();
		hubAllClients.addHubListener(this);
		for (int i=0; i<5; i++) {
			Client c = new Client();
			c.setId(i+"");
			c.setLastName("Jones "+i);
			c.setFirstName("Chris "+i);
			hubAllClients.add(c);
			
			for (int ii=0; ii<5; ii++) {
		  		Pet pet = new Pet();
		  		if ( (i%2) == 0) c.getPets().add(pet);
		  		else c.getPets().insert(pet, 0);
			}
		}
		hubAllClients.setAO(0);
		Client c = (Client) hubAllClients.getAO();
		c.delete();
		
		System.out.println("Done");
	}
	
	public static void main(String[] args) {
		OAObjectTest oatest = new OAObjectTest();
		oatest.testSetProperty();

// finish testing to make sure that events are all called
// hubAllClients.sort("lastName");
//		detail
//		shared
//		listeners - add, remove, etc.
	
	}
}










