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
package com.viaoa.html;

import java.util.*;
import java.io.*;
import com.viaoa.hub.*;

/** base class used by OAForm, OASession, OAApplication, OASystem for storing objects and messages.
    For objects, all Hubs are stored in a seperate hashtable, all other objects are stored in another hashtable.
*/
public abstract class OABase implements Serializable {
    private static final long serialVersionUID = 1L;
	
    // 2008 made all of these transient
    protected transient volatile Hashtable hashtable = new Hashtable(13,0.75f);
    protected Hashtable hashHub = new Hashtable(13,0.75f);
    protected transient Vector vecError = new Vector(5,5);
    protected transient Vector vecMessage = new Vector(5,5);
    protected transient Vector vecHiddenMessage = new Vector(5,5);

    /** @param name is not case sensitive    */
    public Object get(String name) {
        if (name == null) return null;
        if (hashtable == null) hashtable = new Hashtable(13,0.75f);
        Object obj = hashtable.get(name.toUpperCase());
        return obj;
    }
    /** @param name is not case sensitive  */
    public void put(String name, Object obj) {
        if (name != null && obj != null) {
            name = name.toUpperCase();
            if (hashtable == null) hashtable = new Hashtable(13,0.75f);
            hashtable.put(name.toUpperCase(),obj);
        }
    }
    /** @param name is not case sensitive */
    public void remove(String name) {
        if (name != null) {
            if (hashtable == null) hashtable = new Hashtable(13,0.75f);
            hashtable.remove(name.toUpperCase());
        }
    }
    
   
    
    public void put(String name, int i) {
        if (name != null) put(name, new Integer(i));
    }
    /** calls put(name,i) . */
    public void putInt(String name, int i) {
        this.put(name, i);
    }
    /** calls get(name) and converts to "int". returns -1 if not found. */
    public int getInt(String name) {
        Object obj = get(name);
        if (obj == null || !(obj instanceof Number)) return -1;
        return ((Number)obj).intValue();
    }
    

    
    /** puts a "hub" prefix on name and calls put(name,obj) */
    public void put(String name, Hub hub) {
        if (name != null && hub != null) {
            name = name.toUpperCase();
            if (hashHub.get(name) != null) {
                System.out.println("OABase.putHub(\""+name+"\") Note: hub with name already exists");
            }
            hashHub.put(name,hub);
        }
    }
    /** puts a "hub" prefix on name and calls put(name,obj) */
    public void putHub(String name, Hub hub) {
        this.put(name,hub);
    }
    /** use putHub() or put() instead */
    public void addHub(String name, Hub hub) {
        this.put(name,hub);
    }
    /** puts a "hub" prefix on name and calls get(name) and casts to Hub. */
    public Hub getHub(String name) {
        if (name == null) return null;
        Object obj = hashHub.get(name.toUpperCase());
        if (obj == null || !(obj instanceof Hub)) {
 System.out.println("OABase.getHub(\""+name+"\") Hub not found, returning null");
            return null;
        }
        return (Hub) obj;
    }
    /** puts a "hub" prefix on name and calls remove(name). */
    public void removeHub(String name) {
        hashHub.remove(name);
    }
    

    
    /** @see OABase#put */
    public void putMisc(String name, Object obj) {
        put(name,obj);
    }
    /** use putMisc() or put() instead */
    public void addMisc(String name, Object obj) {
        this.put(name,obj);
    }
    /** @see OABase#get */
    public Object getMisc(String name) {
        return get(name);
    }
    /** @see OABase#remove */
    public void removeMisc(String name) {
        remove(name);
    }
    

    /** adds a message. */
    public void addMessage(String msg) {
    	if (vecMessage == null) vecMessage = new Vector(5,5);
        vecMessage.addElement(msg);
    }
    /** gets all messages. */
    public String[] getMessages() {
    	if (vecMessage == null) vecMessage = new Vector(5,5);
        int x = vecMessage.size();
        String[] s = new String[x];
        vecMessage.copyInto(s);
        return s;
    }
    /** clears all messages. */
    public void clearMessages() {
    	if (vecMessage == null) vecMessage = new Vector(5,5);
        vecMessage.removeAllElements();
    }

    public void addHiddenMessage(String msg) {
    	if (vecHiddenMessage == null) vecHiddenMessage = new Vector(5,5);
        vecHiddenMessage.addElement(msg);
    }
    /** gets all hidden messages. */
    public String[] getHiddenMessages() {
    	if (vecHiddenMessage == null) vecHiddenMessage = new Vector(5,5);
        int x = vecHiddenMessage.size();
        String[] s = new String[x];
        vecHiddenMessage.copyInto(s);
        return s;
    }
    /** clears all hidden messages. */
    public void clearHiddenMessages() {
    	if (vecHiddenMessage == null) vecHiddenMessage = new Vector(5,5);
        vecHiddenMessage.removeAllElements();
    }

    public void addError(String msg) {
    	if (vecError == null) vecError = new Vector(5,5);
        vecError.addElement(msg);
    }
    /** gets all errors.  
        @see OASession#getErrors
    */
    public String[] getErrors() {
    	if (vecError == null) vecError = new Vector(5,5);
        int x = vecError.size();
        String[] s = new String[x];
        vecError.copyInto(s);
        return s;
    }
    /** clears all errors. */
    public void clearErrors() {
    	if (vecError == null) vecError = new Vector(5,5);
        vecError.removeAllElements();
    }

    
}

