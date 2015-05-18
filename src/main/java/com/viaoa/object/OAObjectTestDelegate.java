/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.object;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Logger;

import com.viaoa.hub.Hub;
import com.viaoa.util.*;
import com.viaoa.object.test.*;


/**
 * This class is used to "offer" services for testing the OAObject using Test Data objects from the test package.
 * The idea is to have the Data objects match the changes in OAObject and then to compare the results to make
 * sure that everything matches. 
 * @author vincevia
 *
 */
public class OAObjectTestDelegate {

	private static Logger LOG = OALogger.getLogger(OAObjectTestDelegate.class);
	

	// ============ OAObject ================
	public static OAObjectData getOAObjectData(OAObject oaObj) {
		if (oaObj == null) return null;
		OAObjectData data = new OAObjectData();
		// data.bServerCache = oaObj.bServerCache;
		data.guid = oaObj.guid;
		data.changedFlag = oaObj.changedFlag;
		data.newFlag = oaObj.newFlag;
		
		Object[] ids = OAObjectInfoDelegate.getPropertyIdValues(oaObj);
		data.objectKey = new OAObjectKey(ids, oaObj.guid, oaObj.newFlag);
		
		//if (oaObj.htLink != null) data.htLink = (Hashtable) oaObj.htLink.clone();
//		if (oaObj.hashNull != null) data.hashNull = (Hashtable) oaObj.hashNull.clone();
		//if (oaObj.hmProperty != null) data.hmProperty = (HashMap) oaObj.hmProperty.clone();
//		if (oaObj.hashTransProp != null) data.hashTransientProperty = (Hashtable) oaObj.hashTransProp.clone();
		
		//qqqqqq todo: weakhubs
		return data;
	}
	
	/**
	 * Compare an OAObject with the expected values stored in data object.
	 */
	public static boolean compare(String msg, OAObject obj, OAObjectData data) {
		if (obj == null || data == null) return ((Object)obj == (Object)data);
		boolean bResult = true;

		/*
		if (obj.bServerCache != data.bServerCache) {
			bResult = false;
			LOG.warning(msg + " bServerCache");
		}
		*/
		if (obj.guid != data.guid) {
			bResult = false;
			LOG.warning(msg + " guid");
		}

		if (obj.newFlag != data.newFlag) {
			bResult = false;
			LOG.warning(msg + " newFlag");
		}
		
		if (obj.changedFlag != data.changedFlag) {
			bResult = false;
			LOG.warning(msg + " changedFlag");
		}

		// HASHTABLES
		/*
		if (!compare(obj.hmLink, data.hmLink)) {
			bResult = false;
			LOG.warning(msg + " hashLink");
		}
		*/
		/*
		if (!compare(obj.hashNull, data.hashNull)) {
			bResult = false;
			LOG.warning(msg + " hashNull");
		}
		*/
		/*
		if (!compare(obj.hashProperty, data.hashProperty)) {
			bResult = false;
			LOG.warning(msg + " hashProperty");
		}
		*/
/*		
		if (!compare(obj.hashTransProp, data.hashTransientProperty)) {
			bResult = false;
			LOG.warning(msg + " hashTransientProperty");
		}
*/


		// objectKey
		if (obj.objectKey == null) {
			if (data.objectKey != null) {
				bResult = false;
				LOG.warning(msg + " objectKey");
			}
		}
		else {
			if (data.objectKey == null) {
				bResult = false;
				LOG.warning(msg + " objectKey 2");
			}
			else {
				if (!obj.objectKey.exactEquals(data.objectKey)) {
obj.objectKey.exactEquals(data.objectKey);
					bResult = false;
					LOG.warning(msg + " objectKey 3");
				}
			}
		}
		
//qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq finish the hub comparison
//Object[] weakHubs;
		
		return bResult;
	}

	private static boolean compare(Hashtable h1, Hashtable h2) {
		int x1 = (h1 == null ? 0 : h1.size());
		int x2 = (h2 == null ? 0 : h2.size());
		if (x1 != x2) return false;
		if (x1 == 0) return true;

		Enumeration en = h1.keys();
		for ( ;en.hasMoreElements(); ) {
			Object key = en.nextElement();
			Object o1 = h1.get(key);
			Object o2 = h2.get(key);

			if (o1 instanceof WeakReference) {
				o1 = ((WeakReference) o1).get();
			}
			if (o2 instanceof WeakReference) {
				o2 = ((WeakReference) o2).get();
			}
			
			if (o1.equals(o2)) continue;
			
			if (o1 == null || o2 == null) return false;
			if (!o1.getClass().equals(o2.getClass())) return false;
			if (o1 instanceof Vector) {
				if (!compare((Vector)o1, (Vector) o2)) return false;
			}
			else if (o1 instanceof Hashtable) {
				if (!compare((Hashtable)o1, (Hashtable) o2)) return false;
			}
			else {
				if (!o1.equals(o2)) return false;
			}
		}
		return true;
	}

	private static boolean compare(Vector v1, Vector v2) {
		int x1 = (v1 == null ? 0 : v1.size());
		int x2 = (v2 == null ? 0 : v2.size());
		if (x1 != x2) return false;
		if (x1 == 0) return true;
		
		for (int i=0; i<v1.size(); i++) {
			Object val1 = v1.elementAt(i);
			Object val2 = v2.elementAt(i);

			if (val1 instanceof WeakReference) {
				val1 = ((WeakReference) val1).get();
			}
			if (val2 instanceof WeakReference) {
				val2 = ((WeakReference) val2).get();
			}
			
			if (val1 == val2) continue;
			if (val1 == null || val2 == null) return false;
			if (val1 instanceof Vector) {
				if (!compare((Vector)val1, (Vector) val2)) return false;
			}
			else if (val1 instanceof Hashtable) {
				if (!compare((Hashtable)val2, (Hashtable) val2)) return false;
			}
			else {
				if (!val1.equals(val2)) return false;
			}
		}				
		return true;
	}
	
	public static OAObjectKey getOAObjectKey(Object[] ids, int guid, boolean bNew) {  // used by OAObjectTestDelegate for testing use
		return new OAObjectKey(ids, guid, bNew);
	}

	private static Vector clone(Vector vec) {
		if (vec == null) return null;
		Vector v = new Vector();
		for (int i=0; i<v.size(); i++) {
			Object value = v.elementAt(i);
			if (value instanceof Vector) {
				value = clone((Vector) value);
			}
			else if (value instanceof Hashtable) {
				value = clone((Hashtable) value);
			}
			v.add(value);
		}
		return v;
	}	
	private static Hashtable clone(Hashtable hash) {
		if (hash == null) return null;
		Hashtable ht = new Hashtable();
		
		Enumeration en = hash.keys();
		for ( ;en.hasMoreElements(); ) {
			Object key = en.nextElement();
			Object value = hash.get(key);
			if (value instanceof Vector) {
				value = clone((Vector) value);
			}
			else if (value instanceof Hashtable) {
				value = clone((Hashtable) value);
			}
			ht.put(key, value);
		}		
		
		return ht;
	}
	
	
	// ============ OAObjectCacheDelegate ================
	public static OAObjectCacheData getOAObjectCacheData() {
		OAObjectCacheData data = new OAObjectCacheData();
		
		
		return data;
	}

	public static boolean compare(String msg, OAObjectCacheData data) {
		return true; //qqqqqqqqqqqqqq
	}	

	
	
	// ============ OAObjectHashDelegate ================
	public static OAObjectHashData getOAObjectHashData() {
		OAObjectHashData data = new OAObjectHashData();
		
		Map hash;
		int x;
/*		
		hash = OAObjectHashDelegate.hashSettingPropToMaster;
		x = hash == null ? 0 : hash.size();
		data.iHashSettingPropToMaster = x;
*/
/*		
		hash = OAObjectHashDelegate.hashLoad;
		x = hash == null ? 0 : hash.size();
		data.iHashLoad = x;
*/
/*		
		hash = OAObjectHashDelegate.hashLoadingObject;
		x = hash == null ? 0 : hash.size();
		data.iHashThreadLoad = x;
*/		
		
//		hash = OAObjectHashDelegate.hashSkipConstructor;
/*		
		x = hash == null ? 0 : hash.size();
		data.iHashSkipConstructor = x;
*/		
//		hash = OAObjectHashDelegate.hashSkipConstructor;
/*		
		x = hash == null ? 0 : hash.size();
		data.iHashSkipConstructor = x;
*/		
/*
		hash = OAObjectHashDelegate.hashConstructor;
		x = hash == null ? 0 : hash.size();
		data.iHashConstructor = x;
*/
/*		
		hash = OAObjectHashDelegate.hashDelete;
		x = hash == null ? 0 : hash.size();
		data.iHashDelete = x;
*/		
/*
		hash = OAObjectHashDelegate.hashBroadcast;
		x = hash == null ? 0 : hash.size();
		data.iHashBroadcast = x;
*/
		hash = OAObjectHashDelegate.hashObjectInfo;
		x = hash == null ? 0 : hash.size();
		data.iHashObjectInfo = x;
/*
		hash = OAObjectHashDelegate.hashSerialize;
		x = hash == null ? 0 : hash.size();
		data.iHashSerialize = x;
*/
		hash = OAObjectHashDelegate.hashLock;
		x = hash == null ? 0 : hash.size();
		data.iHashLock = x;

		/*
		hash = OAObjectHashDelegate.hashPropertyLock;
		x = hash == null ? 0 : hash.size();
		data.iHashLinkLock = x;
        */
		
		hash = OAObjectHashDelegate.hashCacheClass;
		x = hash == null ? 0 : hash.size();
		data.iHashCacheClass = x;


		hash = OAObjectHashDelegate.hashCacheListener;
		x = hash == null ? 0 : hash.size();
		data.iHashCacheListener = x;

		hash = OAObjectHashDelegate.hashCacheListener;
		x = hash == null ? 0 : hash.size();
		data.iHashCacheListener = x;
/*
		hash = OAObjectHashDelegate.hashCacheIgnoreEvent;
		x = hash == null ? 0 : hash.size();
		data.iHashCacheIgnoreEvent = x;
*/
		hash = OAObjectHashDelegate.hashCacheSelectAllHub;
		x = hash == null ? 0 : hash.size();
		data.iHashCacheSelectAllHub = x;

		hash = OAObjectHashDelegate.hashCacheNamedHub;
		x = hash == null ? 0 : hash.size();
		data.iHashCacheNamedHub = x;
/*		
		hash = OAObjectHashDelegate.hashCacheAddMode;
		x = hash == null ? 0 : hash.size();
		data.iHashCacheAddMode = x;
*/		
		return data;
	}

	public static boolean compare(String msg, OAObjectHashData data) {
		boolean bResult = true;
		
		Map hash;
		int x;
/*		
		hash = OAObjectHashDelegate.hashSettingPropToMaster;
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashSettingPropToMaster) {
			bResult = false;
			LOG.warning(msg + " hashSettingPropToMaster");
		}
*/		
/*		
		hash = OAObjectHashDelegate.hashLoad;
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashLoad) {
			bResult = false;
			LOG.warning(msg + " hashLoad");
		}
*/
/*		
		hash = OAObjectHashDelegate.hashLoadingObject;
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashThreadLoad) {
			bResult = false;
			LOG.warning(msg + " hashThreadLoad");
		}
*/		
		
//		hash = OAObjectHashDelegate.hashSkipConstructor;
/*		
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashSkipConstructor) {
			bResult = false;
			LOG.warning(msg + " hashSkipConstructor");
		}
*/		
//		hash = OAObjectHashDelegate.hashSkipConstructor;
/*		
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashSkipConstructor) {
			bResult = false;
			LOG.warning(msg + " hashSkipConstructor");
		}
*/		
/*		
		hash = OAObjectHashDelegate.hashConstructor;
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashConstructor) {
			bResult = false;
			LOG.warning(msg + " hashConstructor");
		}
*/
/*		
		hash = OAObjectHashDelegate.hashDelete;
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashDelete) {
			bResult = false;
			LOG.warning(msg + " hashDelete");
		}
*/		
/*
		hash = OAObjectHashDelegate.hashBroadcast;
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashBroadcast) {
			bResult = false;
			LOG.warning(msg + " hashBroadcast");
		}
*/

		hash = OAObjectHashDelegate.hashObjectInfo;
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashObjectInfo) {
			bResult = false;
			LOG.warning(msg + " hashObjectInfo");
		}
/*
		hash = OAObjectHashDelegate.hashSerialize;
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashSerialize) {
			bResult = false;
			LOG.warning(msg + " hashSerialize");
		}
*/
		hash = OAObjectHashDelegate.hashLock;
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashLock) {
			bResult = false;
			LOG.warning(msg + " hashLock");
		}

		/*
		hash = OAObjectHashDelegate.hashPropertyLock;
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashLinkLock) {
			bResult = false;
			LOG.warning(msg + " hashLinkLock");
		}
		*/

		hash = OAObjectHashDelegate.hashCacheClass;
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashCacheClass) {
			bResult = false;
			LOG.warning(msg + " hashCacheClass");
		}


		hash = OAObjectHashDelegate.hashCacheListener;
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashCacheListener) {
			bResult = false;
			LOG.warning(msg + " hashCacheListener");
		}

		hash = OAObjectHashDelegate.hashCacheListener;
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashCacheListener) {
			bResult = false;
			LOG.warning(msg + " hashCacheListener");
		}
/*
		hash = OAObjectHashDelegate.hashCacheIgnoreEvent;
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashCacheIgnoreEvent) {
			bResult = false;
			LOG.warning(msg + " hashCacheIgnoreEvent");
		}
*/
		hash = OAObjectHashDelegate.hashCacheSelectAllHub;
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashCacheSelectAllHub) {
			bResult = false;
			LOG.warning(msg + " hashCacheSelectAllHub");
		}

		hash = OAObjectHashDelegate.hashCacheNamedHub;
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashCacheNamedHub) {
			bResult = false;
			LOG.warning(msg + " hashCacheNamedHub");
		}
/*		
		hash = OAObjectHashDelegate.hashCacheAddMode;
		x = hash == null ? 0 : hash.size();
		if (x != data.iHashCacheAddMode) {
			bResult = false;
			LOG.warning(msg + " hashCacheAddMode");
		}
*/		
		return bResult;
	}

	
	public static int getGuid() {
		return OAObjectDelegate.guidCounter.get();
	}
	
	
	public static boolean compare(String msg, OAObjectCascadeData data) {
		boolean bResult = true;
/*qqqqqqqqq
		int x = OAObjectCascadeDelegate.vecCascade.size();
		if (x != data.vectorSize) {
			bResult = false;
			LOG.warning(msg + " vecCascade.size");
		}
		
		int cnt = 0;
		for (int i=0; i<x; i++) {
			OACascade c = (OACascade) OAObjectCascadeDelegate.vecCascade.elementAt(i);
			if (c.iUsed != 0) cnt++;
		}
		if (cnt != data.inUse) {
			bResult = false;
			LOG.warning(msg + " inUse");
		}
*/		
		return bResult;
	}	


	public static boolean verifyKey(String msg, OAObject oaObj) {
		boolean bResult = true;
		OAObjectKey key = OAObjectKeyDelegate.getKey(oaObj);
		if (key.guid != oaObj.guid) {
			bResult = false;
			LOG.warning(msg + " guid");
		}
		if (key.bNew != oaObj.newFlag) {
			bResult = false;
			LOG.warning(msg + " newFlag");
		}
		
		Object[] ids = OAObjectInfoDelegate.getPropertyIdValues(oaObj);
		boolean b = (ids == null || ids.length == 0 || ids[0] == null);
		if (key.bEmpty != b) {
			bResult = false;
			LOG.warning(msg + " bEmpty");
		}

		OAObjectKey key2 = getOAObjectKey(ids, oaObj.guid, oaObj.newFlag);
		if (!key.exactEquals(key2)) {
			bResult = false;
			LOG.warning(msg + " key");
		}
		
		return bResult;
	}
	

	public static boolean compare(String msg, OAObjectLockData data) {
		boolean bResult = true;
		int x = OAObjectHashDelegate.hashLock == null ? 0 : OAObjectHashDelegate.hashLock.size();
		if (data.lockCnt != x) {
			bResult = false;
			LOG.warning(msg + " lockCnt");
		}
		return bResult;
	}	
	
	
///qqqqqqqqqqqqq
	public static boolean compare(String msg, OAObjectDelegateData data) {
		if (data == null) return true;
		boolean bResult = true;
		Map hash;
		int x1;
		
/*		
		// hashSettingPropToMaster
		hash = OAObjectHashDelegate.hashSettingPropToMaster;
		x1 = hash == null ? 0 : hash.size();
		if (x1 != data.hashSettingPropToMasterCnt) {
			bResult = false;
			LOG.warning(msg + " hashSettingPropToMaster");
		}
*/
		// hashLoad
/*		
		hash = OAObjectHashDelegate.hashLoad;
		x1 = hash == null ? 0 : hash.size();
		if (x1 != data.hashLoadCnt) {
			bResult = false;
			LOG.warning(msg + " hashLoad");
		}
*/		
		// hashThreadLoad
/*		
		hash = OAObjectHashDelegate.hashLoadingObject;
		x1 = hash == null ? 0 : hash.size();
		if (x1 != data.hashThreadLoadCnt) {
			bResult = false;
			LOG.warning(msg + " hashThreadLoad");
		}
*/		
		// hashSkipConstructor		
//		hash = OAObjectHashDelegate.hashSkipConstructor;
/*		
		x1 = hash == null ? 0 : hash.size();
		if (x1 != data.hashSkipConstructorCnt) {
			bResult = false;
			LOG.warning(msg + " hashSkipConstructor");
		}
*/		
		// hashConstructor		
/*		
		hash = OAObjectHashDelegate.hashConstructor;
		x1 = hash == null ? 0 : hash.size();
		if (x1 != data.hashConstructorCnt) {
			bResult = false;
			LOG.warning(msg + " hashConstructor");
		}
*/		
		// hashDelete
/*		
		hash = OAObjectHashDelegate.hashDelete;
		x1 = hash == null ? 0 : hash.size();
		if (x1 != data.hashDeleteCnt) {
			bResult = false;
			LOG.warning(msg + " hashDelete");
		}
*/		
/*		
		// hashBroadcast		
		hash = OAObjectHashDelegate.hashBroadcast;
		x1 = hash == null ? 0 : hash.size();
		if (x1 != data.hashBroadcastCnt) {
			bResult = false;
			LOG.warning(msg + " hashBroadcast");
		}
				*/
		// hashLinkLock		
		/*
		hash = OAObjectHashDelegate.hashPropertyLock;
		x1 = hash == null ? 0 : hash.size();
		if (x1 != data.hashLinkLockCnt) {
			bResult = false;
			LOG.warning(msg + " hashLinkLock");
		}
		*/
		
		
		
		return bResult;
	}	





    public static void verify(OAObject oaObj) {
/*    	
        OACascade c = OAObjectCascadeDelegate.getCascade("verify");
        try {
            verify(oaObj, c);
        }
        finally {
            c.release();
        }
*/        
    }
/*
    private static void verify(OAObject oaObj, OACascade c) {
        if (OAObjectCascadeDelegate.hasBeenCalled(oaObj, c, true)) return;
        Object obj = OAObjectCacheDelegate.getObject(oaObj.getClass(), OAObjectKeyDelegate.getKey(oaObj));
        if (obj != oaObj) System.out.println(oaObj+" not in Cache");
*/        
/*        
        if (oaObj.hmLink != null) {
            Enumeration enumx = oaObj.hashLink.keys();
            for ( ; enumx.hasMoreElements(); ) {
                Object key = enumx.nextElement();
                obj = oaObj.hashLink.get(key);
                // 2006/05/15 hashLink needs to use WeakReferences
                if (obj != null && obj instanceof WeakReference) obj = ((WeakReference) obj).get();
                
                if (obj instanceof OAObject) {
                    verify((OAObject)obj, c);
                }
                else if (obj instanceof Hub) {
                    Hub h = (Hub) obj;
                    for (int i=0; ;i++) {
                        obj = h.elementAt(i);
                        if (obj == null) break;
                        if (obj instanceof OAObject) {
                            verify((OAObject)obj, c);
                        }
                    }
                }
            }
        }
*/        
 //   }

}






