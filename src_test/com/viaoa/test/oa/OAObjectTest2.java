package com.viaoa.test.oa;

import java.util.Hashtable;
import java.util.logging.*;

import com.viaoa.object.*;
import com.viaoa.object.test.*;
import com.viaoa.util.*;
import com.viaoa.logging.*;
import com.viaoa.test.data.*;

public class OAObjectTest2 {
	
	private static Logger LOG = OALogger.getLogger(OAObjectTest.class);

	public void testOAObject5() {
		// OAObjectDelegate
		OAObjectDelegateData data = new OAObjectDelegateData();

		for (int i=0; i<10; i++) {
			ClientAlert ca = new ClientAlert();
			ca.setId("ad.f"+i);
			ca.setDescription("adfa");
			ca.setProperty("Description", "adsf");
			ca.save();
			ca.setId("ads"+i);
			if ((i%3) == 0) ca.delete();
		}

		// InConstructor
		Client c = new Client();
		OAObjectTestDelegate.compare("ObjectDelegateData1", data);
		
		data.hashConstructorCnt--;
		OAObjectTestDelegate.compare("ObjectDelegateData2", data);

		
		// 1.0 setSkipConstructor
//		OAObjectFlagDelegate.setThreadSkipConstructor(true);
		data.hashSkipConstructorCnt++;
		OAObjectTestDelegate.compare("ObjectDelegateData3", data);
		
		// 1.1 create object that skips constructor
		c = new Client();
		OAObjectData od = new OAObjectData();
		od.newFlag = true;
		OAObjectTestDelegate.compare("ObjectDelegateData3.1", c, od);

//		OAObjectFlagDelegate.setThreadSkipConstructor(false);
		data.hashSkipConstructorCnt--;
		OAObjectTestDelegate.compare("ObjectDelegateData4", data);

		// 2.0 isLoading
		c = new Client();
		OAObjectFlagDelegate.setLoading(true);
		data.hashLoadCnt++;
		OAObjectTestDelegate.compare("ObjectDelegateData2.0", data);
		
		c.setLastName("dfad");
		
		OAObjectFlagDelegate.setLoading(false);
		data.hashLoadCnt--;
		OAObjectTestDelegate.compare("ObjectDelegateData2.0a", data);

		// 3.0 isThreadLoading
		c = new Client();
		OAObjectFlagDelegate.setLoading(true);
		data.hashThreadLoadCnt++;
		OAObjectTestDelegate.compare("ObjectDelegateData3.0", data);
		c.setLastName("dfad");
		OAObjectFlagDelegate.setLoading(false);
		data.hashThreadLoadCnt--;
		OAObjectTestDelegate.compare("ObjectDelegateData3.1", data);

		// 4.0 isDeleting
		c = new Client();
		OAObjectFlagDelegate.setDeleting(c, true);
		data.hashDeleteCnt++;
		OAObjectTestDelegate.compare("ObjectDelegateData4.0", data);
		OAObjectFlagDelegate.setDeleting(c, false);
		data.hashDeleteCnt--;
		OAObjectTestDelegate.compare("ObjectDelegateData4.1", data);
		
		// 5.0 isBroadcasting
		c = new Client();
		data.hashBroadcastCnt++;
		OAObjectTestDelegate.compare("ObjectDelegateData5.0", data);
		data.hashBroadcastCnt--;
		OAObjectTestDelegate.compare("ObjectDelegateData5.1", data);
		
		
		OAObjectTestDelegate.compare("ObjectDelegateDataX", data);
	}
	
	
	public void testOAObject4() {
		Client c = new Client();
		OAObjectLockData data = new OAObjectLockData();
		data.lockCnt++;
		c.lock();
		OAObjectTestDelegate.compare("Lock client", data);

		if (!c.isLocked()) {
			LOG.warning("lock");
		}
		
		c.unlock();
		if (c.isLocked()) {
			LOG.warning("lock2");
		}
		data.lockCnt--;
		OAObjectTestDelegate.compare("Lock client", data);
	}
	
	public void testOAObject3() {
		Client c = new Client();
		// delegate.updateKey()
		c.setId("cde");
		// setProperty()
		c.setProperty("lastName", "jones");
		
		ClientAlert ca = new ClientAlert();
		OAObjectKey key = new OAObjectKey("cde");
		
		// make sure that hashLink has property and not hashProperty
		OAObjectData data = OAObjectTestDelegate.getOAObjectData(ca);
		ca.setProperty("client", key);
		data.addHashLink("client", key);
		data.removeHashNull("client");
		data.changedFlag = true;
		OAObjectTestDelegate.compare("setClient key", ca, data);
		
		
		// delegate.getObject()
		Client c2 = ca.getClient();
		if (c2 != c) {
			LOG.warning("Client alert client");
		}
		
		// find()
		Object obj = ca.find("client.lastname", "jones");
		if (obj != c) {
			LOG.warning("Client alert client2");
		}
		
		// setProperty()
		ca = new ClientAlert();
		ca.setProperty("client", "xxx");
		c2 = ca.getClient();
		if (c2 != null) {
			LOG.warning("Client alert client4");
		}
		
		ca = new ClientAlert();
		// make sure that hashLink has property and not hashProperty
		data = OAObjectTestDelegate.getOAObjectData(ca);
		ca.setProperty("client", "cde");
		data.addHashLink("client", "cde");
		data.removeHashNull("client");
		data.changedFlag = true;
		OAObjectTestDelegate.compare("setClient key", ca, data);

		ca = new ClientAlert();
		ca.setProperty("client", "cde");
		obj = ca.getProperty("client");
		if (obj != c) {
			LOG.warning("Client alert client3");
		}
		
		obj = ca.getProperty("client.lastName");
		if (!"jones".equals(obj)) {
			LOG.warning("getProp client.lastName");
		}
		
		// delete ClientAlert
		// should not be in Cache
		OAObjectHashData cd = OAObjectTestDelegate.getOAObjectHashData();
		ca.delete();
		OAObjectTestDelegate.compare("ca.delete", cd);

		
		// more cache work using "cd" - check for rehashing
		ca = new ClientAlert();
//		cd.addObject(ca);
/*
		String s = ca.getCantSaveMessage();
		if (s == null) {
			LOG.warning("ca.CantSaveMessage");
		}
		*/
		for (int i=0; i<10; i++) {
//			cd.removeObject(ca);
			ca.setId("adf"+i);
//			cd.addObject(ca);
			ca.save(); // this changes key, but will not affect "equals()"
		}
		OAObjectTestDelegate.compare("cache add ca", cd);
	}
	
	
	public void testOAObject2() {
		int i = OAObjectTestDelegate.getGuid();
		Client c = new Client();
		if (OAObjectTestDelegate.getGuid() != i+1) {
			LOG.warning("guid");
		}
		OAObjectTestDelegate.verifyKey("verifyKey", c);
		
		OAObjectData data = OAObjectTestDelegate.getOAObjectData(c);
		
		c.setProperty("lastname", "jones");
		
		String s = c.getLastName();
		if (!s.equals("jones")) LOG.warning("jones");
		
		data.hashNull.remove("LASTNAME");
		data.changedFlag = true;
		OAObjectTestDelegate.compare("lastName", c, data);
		
		c.setProperty("lastname", null);
		s = c.getLastName();
		if (s != null) LOG.warning("jones null");
//		if (!c.isNull("lastName")) LOG.warning("jones null2");
		data.changedFlag = true;
		data.addHashNull("lastName");
		OAObjectTestDelegate.compare("lastName2", c, data);
		if (c.getLastName() != null) {
			LOG.warning("lastName3");
		}

		data = OAObjectTestDelegate.getOAObjectData(c);
		c.setProperty("xxx", "123");
		data.changedFlag = true;
//		if (data.hashProperty == null) data.hashProperty = new Hashtable();
//		data.hashProperty.put("XXX", "123");
		OAObjectTestDelegate.compare("xxx", c, data);
		
		
		c.setProperty("xxx", null);
		data.changedFlag = true;
//		data.hashProperty.put("XXX", OANullObject.nullObject);
		data.addHashNull("XXX");
		OAObjectTestDelegate.compare("xxx2", c, data);
	}
	
	public void testOAObject() {
		// OAObjectCacheData
		OAObjectCacheData cacheData = OAObjectTestDelegate.getOAObjectCacheData();		

// create new Client ================		
		Client c = new Client();
		// verify cache Cache 
		// should be on server if using OAClient
		// if loaded from DB, then fkeys should have keys in hashProps
		// data should match OAObjectData
		OAObjectData data = new OAObjectData();
		data.guid = OAObjectTestDelegate.getGuid() - 1;
		data.newFlag = true;
		data.setHashNull(new String[] {"id", "firstName", "lastName", "address1", "address2", "city", "state", "zip", "phone", "phone2", "pmsId", "email", "country"});
		data.objectKey = OAObjectTestDelegate.getOAObjectKey(null, 1, true);
		OAObjectTestDelegate.compare("()", c, data);
		// check getOAObjectData
		data = OAObjectTestDelegate.getOAObjectData(c);
		OAObjectTestDelegate.compare("Dup", c, data);
		
// set properties ====================
		c.setId("123");
		// data.hashNull.remove("ID");
		data.changedFlag = true;
		data.objectKey = OAObjectTestDelegate.getOAObjectKey(new Object[] {"123"}, data.guid, true);
		OAObjectTestDelegate.compare("setId()", c, data);

		c.setFirstName("Ralph");
		// data.hashNull.remove("FIRSTNAME");
		OAObjectTestDelegate.compare("setFirstName()", c, data);
		
		c.setFirstName("Joe");
		OAObjectTestDelegate.compare("setFirstName()2", c, data);

		c.save();
		data.changedFlag = false;
		data.newFlag = false;
		data.objectKey = OAObjectTestDelegate.getOAObjectKey(new Object[] {"123"}, data.guid, false);
		OAObjectTestDelegate.compare("save()", c, data);
		
		c.setFirstName("Harry");
		data.changedFlag = true;
		OAObjectTestDelegate.compare("setFirstName()3", c, data);
		
		c.setId("ABC");
		data.changedFlag = true;
		data.objectKey = OAObjectTestDelegate.getOAObjectKey(new Object[] {"ABC"}, data.guid, false);
		OAObjectTestDelegate.compare("setId()2", c, data);
		
		c.save();
		data.changedFlag = false;
		data.newFlag = false;
		OAObjectTestDelegate.compare("save()2", c, data);
		
		c.setId("qqq");
		data.changedFlag = true;
		data.objectKey = OAObjectTestDelegate.getOAObjectKey(new Object[] {"qqq"}, data.guid, false);
		OAObjectTestDelegate.compare("setId()3", c, data);

		c.setFirstName("aaa");
		c.setLastName("zzz");
		c.setChanged(false);
		data.changedFlag = false;
		data.newFlag = false;
		// data.removeHashNull("lastname");
		OAObjectTestDelegate.compare("cancel()", c, data);
		
		
		// check getOAObjectData
		data = OAObjectTestDelegate.getOAObjectData(c);
		OAObjectTestDelegate.compare("Dup2", c, data);

		// Check Cache
//		cacheData.addObject(c);
		
		for (int i=0; i<40; i++) {
			c = new Client();
			c.setId("xx"+i);
			c.setId("bb"+i);
//			cacheData.addObject(c);
		}
		OAObjectTestDelegate.compare("Cache", cacheData);
	}

	
	
	
	public void testCascade() {
		
		OAObjectCascadeData data = new OAObjectCascadeData();
		data.vectorSize = 1;

		for (int i=0; i<40; i++) {
			Client c = new Client();
			c.setId("xx"+i);
			c.save();
			// OAObjectDelegate.verify(c);
			// c.delete();
//			c.getCantSaveMessage();
		}
		OAObjectTestDelegate.compare("Cascade", data);
	}

	
	public static void main(String[] args) {
		OALogger.createIndentConsoleLogger("com.viaoa", Level.WARNING);
		
		OAObjectTest2 test = new OAObjectTest2();
		
		test.testOAObject();  //qqq add reference properties once Hub is ready, make sure that rehash is done
		test.testCascade();
		test.testOAObject2();
		test.testOAObject3();
		test.testOAObject4();
		test.testOAObject5();
		
		
		// calc properties
		// find
	
		
		
		System.out.println("Done");
	}
}
