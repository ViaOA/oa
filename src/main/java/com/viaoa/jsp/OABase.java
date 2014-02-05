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
package com.viaoa.jsp;

import java.util.*;
import java.io.*;
import com.viaoa.hub.*;

/** base class used by OAForm, OASession, OAApplication, for storing objects and messages.
*/
public abstract class OABase implements Serializable {
    private static final long serialVersionUID = 1L;
	
    protected HashMap<String, Object> hashmap = new HashMap<String, Object>(29, 0.75f);
    protected transient ArrayList<String> alError = new ArrayList<String>(5);
    protected transient ArrayList<String> alMessage = new ArrayList<String>(5);
    protected transient ArrayList<String> alHidden = new ArrayList<String>(5);

    protected boolean debug;
    protected boolean enabled=true;
    
    public void setDebug(boolean b) {
        this.debug = b;
    }
    public boolean getDebug() {
        return debug;
    }
    
    public void setEnabled(boolean b) {
        this.enabled = b;
    }
    public boolean getEnabled() {
        return enabled;
    }
    
    public void removeAll() {
        hashmap.clear();
        alError.clear();
        alMessage.clear();
        alHidden.clear();
    }
    
    
    /** @param name is not case sensitive    */
    public Object get(String name) {
        if (name == null) return null;
        Object obj = hashmap.get(name.toUpperCase());
        return obj;
    }

    /** @param name is not case sensitive */
    public void put(String name, Object obj) {
        if (name != null && obj != null) {
            name = name.toUpperCase();
            hashmap.put(name, obj);
        }
    }
    /** @param name is not case sensitive */
    public void remove(String name) {
        if (name != null) {
            name = name.toUpperCase();
            hashmap.remove(name);
        }
    }
    
    public int getInt(String name) {
        Object obj = get(name);
        if (!(obj instanceof Number)) return -1;
        return ((Number)obj).intValue();
    }
    
    public Hub getHub(String name) {
        Object obj = get(name);
        if (!(obj instanceof Hub)) return null;
        return (Hub) obj;
    }
    

    /** adds a message. */
    public void addMessage(String msg) {
        alMessage.add(msg);
    }
    /** gets all messages, and clears. */
    public String[] getMessages() {
        int x = alMessage.size();
        String[] s = new String[x];
        alMessage.toArray(s);
        alMessage.clear();
        return s;
    }
    public void addHiddenMessage(String msg) {
        alHidden.add(msg);
    }
    public String[] getHiddenMessages() {
        int x = alHidden.size();
        String[] s = new String[x];
        alHidden.toArray(s);
        alHidden.clear();
        return s;
    }

    public void addError(String msg) {
        alError.add(msg);
    }
    public String[] getErrors() {
        int x = alError.size();
        String[] s = new String[x];
        alError.toArray(s);
        alError.clear();
        return s;
    }
    
}

