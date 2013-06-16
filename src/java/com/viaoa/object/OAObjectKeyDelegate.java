package com.viaoa.object;

import java.util.*;

import com.viaoa.hub.*;
import com.viaoa.util.*;

public class OAObjectKeyDelegate {

    /**
	    returns the OAObjectKey that uniquely represents this object.
	*/
	public static OAObjectKey getKey(OAObject oaObj) {
	    if (oaObj.objectKey == null) oaObj.objectKey = new OAObjectKey(oaObj);
	    return oaObj.objectKey;
	}

	/**
	 Create a new key based on a certain value "keyValue".
	 This is used to build the key if the Id property has already been changed in the object
	 and the previous (oldValue) is known.  Used by OAObjectEventDelegate.
	 */
	public static OAObjectKey getKey(OAObject oaObj, Object keyValue) {
	    if (oaObj.objectKey == null) {
	    	oaObj.objectKey = new OAObjectKey(new Object[] {keyValue}, oaObj.guid, oaObj.newFlag);
	    }
	    return oaObj.objectKey;
	}
	
    /**
	    Used to update Hubs and HubController when an objects unique values (property Id) are changed.<br>
	    @return true if HubController is updated
	    @see OAObjectKey
	*/
	protected static boolean updateKey(OAObject oaObj, boolean bVerify) {
	    if (oaObj.objectKey == null) {
	    	getKey(oaObj);
	        return false;
	    }
	
	    // replace old key
	    OAObjectKey oldKey = oaObj.objectKey;
	    oaObj.objectKey = new OAObjectKey(oaObj);
	    if (oaObj.objectKey.exactEquals(oldKey)) {  // no change
	    	oaObj.objectKey = oldKey;
	        return false;
	    }
	
	    if (bVerify) {
	        // if change is coming from the server, then it has already been verified
	        if (OAObjectCSDelegate.isRemoteThread()) bVerify = false;
	    }

	    // 20090906 dont need to verify if database/etc has assigned it
	    if (bVerify) {
	        if (OAThreadLocalDelegate.isAssigningObjectKey()) bVerify = false; 
	    }
	    
	
	    // make sure objectId is unique.  Check in Cache, on Server, in Database
	    if (bVerify) {
		    if (!oaObj.getNew()) {
	    	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
	            if (oi.getUseDataSource()) {
	                if (OAObjectDSDelegate.allowIdChange(oaObj.getClass())) {
	                	oaObj.objectKey = oldKey;
	                	throw new RuntimeException("can not be changed if " + oaObj.getClass().getName()+" has already been saved");
	                }
	            }
	        }
	    	
	        Object o = OAObjectCacheDelegate.get(oaObj.getClass(), oaObj.objectKey);
	        if ((o == null || o == oaObj)) {
	            if (OAObjectCSDelegate.isWorkstation()) {
	                // check on server.  If server has same object as this, resolve() will return this object
	                o = OAObjectCSDelegate.getServerObject(oaObj.getClass(), oaObj.objectKey);
	            }
	        }
	
	        if (o != oaObj) {
	            if (o != null) {
	                if (OAThreadLocalDelegate.getObjectCacheAddMode() == OAObjectCacheDelegate.NO_DUPS) {
	                    // id already used
	                    Object[] ids = OAObjectInfoDelegate.getPropertyIdValues(oaObj);
	                    String s = "";
	                    for (int i=0; i<ids.length; i++) {
	                        if (ids[i] != null) {
	                            if (s.length() > 0) s += " ";
	                            s += ids[i];
	                        }
	                    }
	                	oaObj.objectKey = oldKey;
	                    throw new RuntimeException("ObjectId \""+ s +"\" already used.");// by another object - "+oaObj.getClass());
	                }
	            }
	            else {
	                if (!OAThreadLocalDelegate.isLoadingObject()) {
	                    // make sure object does not already exist in datasource
	                    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
	                    if (oi.getUseDataSource()) {
	                    	o = OAObjectDSDelegate.getObject(oaObj);
                            if (o != oaObj && o != null) {
                                Object[] ids = OAObjectInfoDelegate.getPropertyIdValues(oaObj);
                                String s = "";
                                for (int i=0; i<ids.length; i++) {
                                    if (i > 0) s += " ";
                                    s += s + ids[i];
                                }
        	                	oaObj.objectKey = oldKey;
                                throw new RuntimeException("ObjectId \""+ s +"\" already used");// by another object - "+oaObj.getClass());
                            }
	                    }
	                }
	            }
	        }
	    }
	
	    // START Rehashing Key ================================================================
	    OAObjectDelegate.rehash(oaObj, oldKey);
	    // END Rehashing Key ====================================================================
	    
	    // need to recalc keys for all children that have this object as part of their object key
	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
	    ArrayList al = oi.getLinkInfos();
	    for (int i=0; i < al.size(); i++) {
	    	OALinkInfo li = (OALinkInfo) al.get(i); 
	        if (!OAObjectReflectDelegate.isReferenceObjectLoadedAndNotEmpty(oaObj, li.name)) continue;
	
	        String revProp = li.getReverseName();
	        if (revProp == null || revProp.length() == 0) continue;
	        OAObjectInfo oiRev = OAObjectInfoDelegate.getOAObjectInfo(li.getToClass());
	        
	        if (!OAObjectInfoDelegate.isIdProperty(oiRev, revProp)) continue;
	
	        Object obj = OAObjectReflectDelegate.getProperty(oaObj, li.getName());
	        if (obj instanceof Hub) {
	            Hub h = (Hub) obj;
	            if (h.isOAObject()) {
	                for (int ii=0; ;ii++) {
	                    OAObject oa = (OAObject) h.elementAt(ii);
	                    if (oa == null) break;
	                    updateKey(oa, false);
	                }
	            }
	        }
	        else if (obj instanceof OAObject) {
	            updateKey((OAObject)obj, false);
	        }
	    }
	    return true;
	}

	/**
	 * Convert a value to an OAObjectKey
	 */
	public static OAObjectKey convertToObjectKey(OAObjectInfo oi, Object value) {
        if (oi == null || value == null) return null;
        if (value instanceof OAObjectKey) return (OAObjectKey) value;

        String[] ids = oi.idProperties;
        if (ids != null && ids.length > 0) {
            Class c = OAObjectInfoDelegate.getPropertyClass(oi, ids[0]);
            value = OAConverter.convert(c, value, null);
        }
        else if ( OAObject.class.isAssignableFrom(oi.getForClass()) ) {
            // 20120729 oaObject without pkey property, which will only use guid for key
            int guid = OAConv.toInt(value);
            OAObjectKey key = new OAObjectKey();
            key.guid = guid;
            return key;
        }
        return new OAObjectKey(value);
	}
/*was:	
	public static OAObjectKey convertToObjectKey(OAObjectInfo oi, Object value) {
        if (oi == null || value == null) return null;
        if (value instanceof OAObjectKey) return (OAObjectKey) value;

        String[] ids = oi.idProperties;
        if (ids != null && ids.length > 0) {
            Class c = OAObjectInfoDelegate.getPropertyClass(oi, ids[0]);
            value = OAConverter.convert(c, value, null);
        }
        return new OAObjectKey(value);
    }	
*/	
	public static OAObjectKey convertToObjectKey(Class clazz, Object value) {
		if (clazz == null || value == null) return null;
        if (value instanceof OAObjectKey) return (OAObjectKey) value;
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
        return convertToObjectKey(oi, value);
	}
	public static OAObjectKey convertToObjectKey(Class clazz, Object[] values) {
		if (clazz == null || values == null) return null;
	    
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
        String[] ids = oi.idProperties;
        for (int i=0; ids != null && i < ids.length; i++) {
        	Class c = OAObjectInfoDelegate.getPropertyClass(clazz, ids[0]);
            values[i] = OAConverter.convert(c, values[i], null);
        }
        return new OAObjectKey(values);
	}

}







