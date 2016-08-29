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

import java.io.IOException;
import java.io.ObjectStreamException;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;
import com.viaoa.comm.io.IODummy;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDelegate;
import com.viaoa.hub.HubSerializeDelegate;
import com.viaoa.remote.multiplexer.io.RemoteObjectInputStream;
import com.viaoa.remote.multiplexer.io.RemoteObjectOutputStream;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.OANotExist;
import com.viaoa.util.OANullObject;

public class OAObjectSerializeDelegate {
	private static Logger LOG = Logger.getLogger(OAObjectSerializeDelegate.class.getName());
    
	protected static void _readObject(OAObject oaObj, java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
	    // 20140310 client only needs to send the key to the server
	    if (in instanceof RemoteObjectInputStream) {
            byte bx = in.readByte();
            if (bx == 1) {
                OAObjectKey ok = (OAObjectKey) in.readObject();
                OAObjectKeyDelegate.setKey(oaObj, ok);
                oaObj.guid = ok.guid;
                cntDup--;
                return;
            }
            else if (bx == 2) {
            }
        }
	    in.defaultReadObject();
	    
        OAObjectInfo oi = null;
        boolean bIsServer = OASyncDelegate.isServer(oaObj.getClass());
	    
	    // read properties
        for ( ; ; ) {
            Object obj = in.readObject();
            if (!(obj instanceof String)) break; // flag to end

            String key = (String)obj;
            Object value = in.readObject();
            if (value instanceof OANullObject) value = null;
            
            if (bIsServer) {
                // 20160206 dont read calcProps if server, they need to be recalc'ed 
                if (oi == null) {
                    oi = OAObjectHashDelegate.hashObjectInfo.get(oaObj.getClass());
                }
                if (oi != null) {
                    OALinkInfo li = oi.getLinkInfo(key);
                    if (li != null && li.bCalculated) {
                        continue;
                    }
                }
                
                if (value instanceof IODummy) value = null;
                if (value instanceof Hub) {
                    Hub hx = (Hub) value;
                    if (hx.getObjectClass().equals(IODummy.class)) value = null;
                }
                
            }
            OAObjectPropertyDelegate.unsafeSetPropertyIfEmpty(oaObj, key, value);  // HubSerializeDelegate._readResolve could have set this first (as weakref)
        }
        OAObjectDelegate.updateGuid(oaObj.guid);
    }
	
	protected static Object _readResolve(final OAObject oaObjRead) throws ObjectStreamException {
		OAObject oaObjUse;

		/* 20151029 on hold
        OASyncCombinedClient cc = OASyncDelegate.getSyncCombinedClient();
        if (cc != null) {
            oaObjNew = cc.resolveObject(oaObjOrig);
            if (oaObjNew != null) return oaObjNew;
        }
		*/
		
		boolean bDup;
        if (oaObjRead.guid == 0) {
        	LOG.warning("received object with guid=0, obj="+oaObjRead+", reassigning a new guid");
        	OAObjectDelegate.assignGuid(oaObjRead);
        	oaObjRead.objectKey.guid = oaObjRead.guid;
        }
        
		OAObjectInfo oi =  OAObjectInfoDelegate.getOAObjectInfo(oaObjRead);
		if (oi.bAddToCache) {
			oaObjUse = OAObjectCacheDelegate.add(oaObjRead, false, false);
			bDup = (oaObjRead != oaObjUse);
		}
		else {
			oaObjUse = oaObjRead;
			bDup = false;
		}

		if (!bDup) {
		    cntNew++;
		    return oaObjUse;
		}
		cntDup++;
		
        final Object[] objs = oaObjRead.properties;

		// check to see if references are needed or not
        for (int i=0; objs != null && i < objs.length; i+=2) {
            String key = (String) objs[i];
            if (key == null) continue;
            Object value = objs[i+1];
		
            Object localValue = OAObjectPropertyDelegate.getProperty(oaObjUse, key, true, true);

            if (localValue != OANotExist.instance) {
                if (localValue instanceof OAObjectKey && (value instanceof OAObject)) {
                    OAObjectKey k1 = (OAObjectKey) localValue;
                    OAObjectKey k2 = OAObjectKeyDelegate.getKey( (OAObject) value);
                    if (k1.equals(k2)) {
                        OAObjectPropertyDelegate.setPropertyCAS(oaObjUse, key, value, localValue);
                    }
                    continue;
                }
                else if (localValue == null && value instanceof Hub) {
                    // fall through and store the oaObjNew Hub value
                }
                else continue;  // note: any other value could be from a propertyChange that happened on the server, that is in the msg que for this client
            }

            
            OALinkInfo linkInfo = OAObjectInfoDelegate.getLinkInfo(oi, key);
            
            // need to replace any references to oaObjOrig with oaObjNew
			boolean b = replaceReferences(oaObjRead, oaObjUse, linkInfo, value);
			if (b) {
			    if (value == null && linkInfo.getType() == linkInfo.MANY) {
			        // 20150826 skip if prop is locked by another
			        try {
		                b = OAObjectPropertyDelegate.attemptPropertyLock(oaObjUse, key);
		                if (b) {
		                    OAObjectPropertyDelegate.setPropertyCAS(oaObjUse, key, value, localValue, (localValue == OANotExist.instance), false);
		                }
			        }
			        finally {
                        if (b) OAObjectPropertyDelegate.releasePropertyLock(oaObjUse, linkInfo.getName());
			        }
			    }
			    else {
			        if (value instanceof Hub && linkInfo.cacheSize > 0) {
			            Hub hub = (Hub) value;
    	                if (OAObjectInfoDelegate.cacheHub(linkInfo, hub)) {
    	                    value = new WeakReference(hub);
    	                }
			        }			        
			        OAObjectPropertyDelegate.setPropertyCAS(oaObjUse, key, value, localValue, (localValue == OANotExist.instance), false);
			    }
			}
        }
        OAObjectDelegate.dontFinalize(oaObjRead);

        return oaObjUse;
    }

    public static volatile int cntDup; 
    public static volatile int cntNew; 
    public static volatile int cntSkip;

	private static boolean replaceReferences(OAObject oaObjFrom, OAObject oaObjTo, OALinkInfo linkInfo, Object value) {
        // 20130215 value can be null
	    if (linkInfo == null) return false;
		//was: if (value == null || linkInfo == null) return false;

        // 20130215
        if (value == null) {
            return true;
        }
	    
	    String revName = linkInfo.getReverseName();
    	if (revName != null) revName = revName.toUpperCase();
    	
		Object origValue = value;
    	if (value instanceof WeakReference) value = ((WeakReference) value).get();

		if (value instanceof Hub) {
        	// handles M-1, M-M
        	Hub hub = (Hub) value;
			if (!HubSerializeDelegate.isResolved(hub)) { 
			    // not fully loaded
			    return false;
			}
			
			// this will only replace if current masterObj = oaObjOrig
    		HubSerializeDelegate.replaceMasterObject((Hub) value, oaObjFrom, oaObjTo);

			for (int i=0; revName!=null; i++) { 
            	OAObject objx = (OAObject) hub.getAt(i);
            	if (objx == null) break;
            	Object ref = OAObjectPropertyDelegate.getProperty(objx, revName, false, true);
            	if (ref == null) {
            	}
            	else if (ref == oaObjFrom || ref instanceof OAObjectKey) {
            	    OAObjectPropertyDelegate.setPropertyCAS(objx, revName, oaObjTo, oaObjFrom);
            	}
            	else if (ref instanceof Hub) {
            		HubSerializeDelegate.replaceObject((Hub) ref, oaObjFrom, oaObjTo);
            	}
            }        	
        }
        else if (value instanceof OAObject && revName != null) {
        	// handles 1-1, 1-Many
        	OAObject objx = (OAObject) value;

        	Object ref = OAObjectPropertyDelegate.getProperty(objx, revName, false, true);
        	if (ref == null) return true;
        	if (ref == oaObjFrom || ref.equals(oaObjFrom.objectKey)) {
        	    OAObjectPropertyDelegate.setPropertyCAS(objx, revName, oaObjTo, oaObjFrom);
        	}
        	else {
        		if (ref instanceof WeakReference) ref = ((WeakReference) ref).get();
        		if (ref instanceof Hub) {
            		HubSerializeDelegate.replaceObject((Hub) ref, oaObjFrom, oaObjTo);
        		}
        	}
        }
		return true;
	}	

//TEST 
/*	
    static volatile int cntx;
    static volatile int indentx;
    protected static void _writeObject(OAObject oaObj, java.io.ObjectOutputStream stream) throws IOException {
        String s = "";
        for (int i=0; i<indentx; i++) s += "   ";
        indentx++;
        int cx = ++cntx;
        System.out.println(s+":"+(cx)+" "+oaObj);
        _writeObjectx(oaObj, stream);
        if (cx != cntx) System.out.println(s+":"+(cx)+" END   "+oaObj);
        indentx--;
    }
*/	
	protected static void _writeObject(OAObject oaObj, java.io.ObjectOutputStream stream) throws IOException {
        //if (xxx % 1000 == 0) System.out.println((xxx)+") writeObject "+oaObj);
        if (oaObj == null) return;
	    OAObjectSerializer serializer = OAThreadLocalDelegate.getObjectSerializer();
        if (serializer != null) {
            serializer.beforeSerialize(oaObj);
        }
        
        boolean bNewObjectCache = OAObjectCSDelegate.isInNewObjectCache(oaObj);
        
        if (stream instanceof RemoteObjectOutputStream) {
            if (bNewObjectCache){
                stream.writeByte((byte) 2); 
            }
            else if (!OASyncDelegate.isServer(oaObj.getClass())) {
               // only need to send key to the server
               stream.writeByte((byte) 1); 
               stream.writeObject(oaObj.getObjectKey());
               if (serializer != null) {
                   serializer.afterSerialize(oaObj);
               }
               return;
            }
            else stream.writeByte((byte) 0); 
        }
        
        stream.defaultWriteObject();  // does not write references (transient)
        
        _writeProperties(oaObj, stream, serializer, bNewObjectCache); // this will write transient properties
  
  		stream.writeObject(OAObjectDelegate.FALSE);  // end of property list

        OAObjectCSDelegate.removeFromNewObjectCache(oaObj);

        // 20141124
        if (serializer != null) {
            serializer.afterSerialize(oaObj);
        }
	}

    protected static void _writeProperties(final OAObject oaObj, final java.io.ObjectOutputStream stream, final OAObjectSerializer serializer, final boolean bNewObjectCache) throws IOException {
        // this method can not support synchronized blocks, since multiple threads could be calling it and then cause deadlock
        // default way for OAServer to send objects.  Clients always send objectKeys.
        //   this way, only the object properties are sent, no reference objects or Hubs
        if (oaObj == null) return;
        Object[] objs = oaObj.properties;
        if (objs == null) return;
        
        OAObjectInfo oi = OAObjectHashDelegate.hashObjectInfo.get(oaObj.getClass());
        boolean bIsServer = OASyncDelegate.isServer(oaObj.getClass());
        
        for (int i=0; i<objs.length; i+=2) {
            String key = (String) objs[i];
            if (key == null) continue;
            OALinkInfo li = oi.getLinkInfo(key);

            if (li != null && li.bCalculated) {
                if (!bIsServer || !li.bServerSideCalc) {
                    continue;
                }
            }

            Object obj = objs[i+1];

            if (obj instanceof IODummy) {
                continue;
            }
            
            if (obj instanceof WeakReference) {
                obj = ((WeakReference) obj).get();
                if (obj == null) continue;
            }
            
            if (obj != null && !(obj instanceof OAObject) && !(obj instanceof OAObjectKey) && !(obj instanceof Hub) && !(obj instanceof byte[])) {
                stream.writeObject(key);
                stream.writeObject(obj);
                continue;
            }

            boolean b = false;
            if (serializer != null && obj != null && !(obj instanceof byte[])) {
                b = serializer.shouldSerializeReference(oaObj, (String) key, obj, li);
            }

            if (b) {
                if (obj instanceof OAObject) {
                    // option to dont send oaobj if it is already on the client
                    obj = serializer.getReferenceValueToSend(obj); 
                }
            }
            else {  
                // see if something can be sent
                if (obj instanceof OAObject) {
                    // always send OAObjectKey to reference objects
                    if (!OAObjectCSDelegate.isInNewObjectCache((OAObject)obj)) {
                        obj = OAObjectKeyDelegate.getKey((OAObject)obj);
                    }
                    b = true;
                }
                else if (obj == null || obj instanceof OAObjectKey) {
                    b = true;
                }
                else if (obj instanceof Hub) {
                    // see if a hub.size=0 can be sent
                    
                    Hub hubx = (Hub) obj;
                    if (bNewObjectCache) {
                        b = true;  // this is when the client is sending an object that the server does not have
                    }
                    else if (hubx.size() > 0 || hubx.getSharedHub() != null) {
                        b = false;  // dont send
                    }
                    else {
                        // if hx.size=0
                        if (!bIsServer || li == null) {
                            obj = null;
                            b = true;
                        }
                        else {
                            // server. need to make sure that autoMatch (if needed) was set up.
                            String matchProperty = li.getMatchProperty();
                            if (matchProperty != null && matchProperty.length() > 0) {
                                if (HubDelegate.getAutoMatch(hubx) != null) {
                                    obj = null;
                                    b = true;
                                }
                                // otherwise, need to call oaObj.getHub(..), so that it's created with an autoMatch  
                            }
                            else {
                                // 20150826 this was missing (not sure why), needs to send a null for empty hub
                                obj = null;
                                b = true;
                            }
                        }
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

