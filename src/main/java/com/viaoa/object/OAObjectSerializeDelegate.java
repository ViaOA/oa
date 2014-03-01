/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.object;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubSerializeDelegate;
import com.viaoa.util.OANullObject;

//20140226 reworked to use PropertyLock


public class OAObjectSerializeDelegate {

	private static Logger LOG = Logger.getLogger(OAObjectSerializeDelegate.class.getName());
    
	protected static void _readObject(OAObject oaObj, java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        OAObjectInfo oi =  null;
        for ( ; ; ) {
            Object obj = in.readObject();
            if (!(obj instanceof String)) break; // flag to end

            String key = (String)obj;
            Object value = in.readObject();
            
            if (value instanceof OANullObject) value = null;
            else if (value instanceof HashLinkWrap) {
                value = ((HashLinkWrap) value).obj;
                if (oi == null) oi =  OAObjectInfoDelegate.getOAObjectInfo(oaObj);
                OALinkInfo linkInfo = OAObjectInfoDelegate.getLinkInfo(oi, key);
                if (linkInfo != null) {
                    if (OAObjectInfoDelegate.cacheHub(linkInfo, (Hub) value)) {
                        value = new WeakReference(value);
                    }
                }
            }
            OAObjectPropertyDelegate.unsafeSetProperty(oaObj, key, value);
        }
        OAObjectDelegate.updateGuid(oaObj.guid);
    }
	
	protected static Object _readResolve(OAObject oaObjOrig) throws ObjectStreamException {
		OAObject oaObjNew;
		boolean bDup;
        if (oaObjOrig.guid == 0) {
        	LOG.warning("received object with guid=0, obj="+oaObjOrig+", reassigning a new guid");
        	OAObjectDelegate.assignGuid(oaObjOrig);
        	oaObjOrig.objectKey.guid = oaObjOrig.guid;
        }

		OAObjectInfo oi =  OAObjectInfoDelegate.getOAObjectInfo(oaObjOrig);
		if (oi.bAddToCache && !oi.bLocalOnly) {
			oaObjNew = OAObjectCacheDelegate.add(oaObjOrig, false, false);
			bDup = (oaObjOrig != oaObjNew);
			if (bDup) {
			    int xx = 4;
			    xx++;
			}
		}
		else {
			oaObjNew = oaObjOrig;
			bDup = false;
		}

        Object[] objs = oaObjOrig.properties;
        for (int i=0; objs != null && i < objs.length; i+=2) {
            String key = (String) objs[i];
            if (key == null) continue;
            Object value = objs[i+1];
		
            OALinkInfo linkInfo = null;
            
            if (bDup) {  // check to see if reference is needed or not
                Object objx = OAObjectPropertyDelegate.getProperty(oaObjNew, key, false);
                if (objx != null) {
                    if (objx instanceof OAObjectKey && (value instanceof OAObject)) {
                        OAObjectKey k1 = (OAObjectKey) objx;
                        OAObjectKey k2 = OAObjectKeyDelegate.getKey( (OAObject) value);
                        if (k1.equals(k2)) {
                            OAObjectPropertyDelegate.setPropertyCAS(oaObjNew, key, value, objx);
                        }
                    }
                    continue;
                }

                if (linkInfo == null) linkInfo = OAObjectInfoDelegate.getLinkInfo(oi, key);
                
                // need to replace any references to oaObjOrig with oaObjNew
    			boolean b = replaceReferences(oaObjOrig, oaObjNew, linkInfo, value);
    			if (b) {
            	    if (!(value instanceof OAObject) && !(value instanceof OAObjectKey)) {
                        OAObjectPropertyDelegate.setPropertyCAS(oaObjNew, key, value, objx);
            	    }
        	        // otherwise, the new value is from a property change that will be sent from the server
    			}
        	}
        }
        if (bDup) {
            OAObjectDelegate.dontFinalize(oaObjOrig);
        }

if (bDup) {
    cntDup++;
}
else cntNew++;

/*
if ( ((cntDup+cntNew) % 5000) == 0) {
    System.out.println(String.format("OAObjectSerializeDelegate: totDup=%d totNew=%d", cntDup, cntNew));
}
*/        
        return oaObjNew;
    }

public static volatile int cntDup; 
public static volatile int cntNew; 
public static volatile int cntSkip;

	private static boolean replaceReferences(OAObject oaObjOrig, OAObject oaObjNew, OALinkInfo linkInfo, Object value) {
        // 20130215 value can be null
	    if (linkInfo == null) return false;
		//was: if (value == null || linkInfo == null) return false;

        // 20130215
        if (value == null) {
            OAObjectPropertyDelegate.setProperty(oaObjNew, linkInfo.name, null);
            return true;
        }
	    
	    String revName = linkInfo.getReverseName();
    	if (revName != null) revName = revName.toUpperCase();
    	
		if (value instanceof WeakReference) value = ((WeakReference) value).get();

		if (value instanceof Hub) {
        	// handles M-1, M-M
        	Hub hub = (Hub) value;
			if (!HubSerializeDelegate.isResolved(hub)) { //qqqqqqqqqqq Temp only
				LOG.fine("resolve ref not fully loaded. obj"+oaObjOrig+", new="+oaObjNew+" orig.guid="+oaObjOrig.guid+" new.guid="+oaObjNew.guid);    				
				return false;  // should only be needed for temp basis
			}
			
			// this will only replace if current masterObj = oaObjOrig
    		HubSerializeDelegate.replaceMasterObject((Hub) value, oaObjOrig, oaObjNew);

			for (int i=0; revName!=null; i++) { 
            	OAObject objx = (OAObject) hub.getAt(i);
            	if (objx == null) break;
            	Object ref = OAObjectPropertyDelegate.getProperty(objx, revName);
            	if (ref == null) continue;
            	if (ref == oaObjOrig || ref instanceof OAObjectKey) {
            	    OAObjectPropertyDelegate.setPropertyCAS(objx, revName, oaObjNew, oaObjOrig);
            	}
            	else {
            		if (ref instanceof WeakReference) ref = ((WeakReference) ref).get();
            		if (ref instanceof Hub) {
                		HubSerializeDelegate.replaceObject((Hub) ref, oaObjOrig, oaObjNew);
            		}
            	}
            }        	
        }
        else if (value instanceof OAObject && revName != null) {
        	// handles 1-1, 1-Many
        	OAObject objx = (OAObject) value;

        	Object ref = OAObjectPropertyDelegate.getProperty(objx, revName);
        	if (ref == null) return true;
        	if (ref == oaObjOrig || ref.equals(oaObjOrig.objectKey)) {
        	    OAObjectPropertyDelegate.setPropertyCAS(objx, revName, oaObjNew, oaObjOrig);
        	}
        	else {
        		if (ref instanceof WeakReference) ref = ((WeakReference) ref).get();
        		if (ref instanceof Hub) {
            		HubSerializeDelegate.replaceObject((Hub) ref, oaObjOrig, oaObjNew);
        		}
        	}
        }
		return true;
	}	

	protected static void _writeObject(OAObject oaObj, java.io.ObjectOutputStream stream) throws IOException {
        OAObjectSerializer serializer = OAThreadLocalDelegate.getObjectSerializer();
        if (serializer != null) {
            serializer.beforeSerialize(oaObj);
        }

        stream.defaultWriteObject();  // does not write references (transient)
        
        _writeProperties(oaObj, stream, serializer); // this will write transient properties
        
        if (serializer != null) {
            serializer.afterSerialize();
        }

  		stream.writeObject(OAObjectDelegate.FALSE);  // end of property list
	}
	
    protected static void _writeProperties(OAObject oaObj, java.io.ObjectOutputStream stream, OAObjectSerializer serializer) throws IOException {
        // this method can not support synchronized blocks, since multiple threads could be calling it and then cause deadlock
        // default way for OAServer to send objects.  Clients always send objectKeys.
        //   this way, only the object properties are sent, no reference objects or Hubs

        Object[] objs = oaObj.properties;
        if (objs == null) return;
        
        OAObjectInfo oi = OAObjectHashDelegate.hashObjectInfo.get(oaObj.getClass());
        
        for (int i=0; i<objs.length; i+=2) {
            String key = (String) objs[i];
            if (key == null) continue;
            OALinkInfo li = oi.getLinkInfo(key);
            if (li != null && li.bCalculated) continue;
            Object obj = objs[i+1];

            boolean bWeakRef = (obj instanceof WeakReference);
            if (bWeakRef) {
                obj = ((WeakReference) obj).get();
                if (obj == null) continue;
            }

            if (obj != null && !(obj instanceof OAObject) && !(obj instanceof OAObjectKey) && !(obj instanceof Hub) && !bWeakRef && !(obj instanceof byte[])) {
                stream.writeObject(key);
                stream.writeObject(obj);
                continue;
            }

            boolean b = false;
            if (serializer != null && obj != null && !(obj instanceof byte[])) {
                b = serializer.shouldSerializeReference(oaObj, (String) key, obj);
                if (bWeakRef) {
                    if (b) obj = new HashLinkWrap(obj);  // flag to know that this is weakRef
                }
            }

            // always send OAObjectKey to reference objects
            if (!b) {
                if (obj instanceof OAObject) {
                    obj = OAObjectKeyDelegate.getKey((OAObject)obj);
                    b = true;
                }
                else if (obj == null || obj instanceof OAObjectKey) { // 20120827 changed to include nulls
                    b = true;
                }
                else if (obj instanceof Hub) {
                    // 20120926 dont send calc hubs if they are shared, they can be created empty and then
                    //         have the calc method code set it up.
                    //     If it is sent here then the above code (see: 20120926 commented out code) will
                    //        need to set up the shared hub correctly
                    Hub hx = (Hub) obj;
                    if (hx.getSharedHub() != null || ((Hub)obj).getSize() == 0) {
                        // send something to know that there are 0 in hub 
                        obj = null;
                        b = true;
                    }
                }
            }

            if (b) {
                stream.writeObject(key);
                if (obj == null) obj = OANullObject.instance;
                stream.writeObject(obj);
            }
        }
    }
}


//Used for serialized object/property that uses a weakReference, so that unwrapping will "know" to put it in a weakRef 
class HashLinkWrap implements Serializable {
    static final long serialVersionUID = 1L;
    Object obj;
    public HashLinkWrap(Object obj) {
        this.obj = obj;
    }
}

