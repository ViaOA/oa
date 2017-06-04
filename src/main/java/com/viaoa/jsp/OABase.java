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
package com.viaoa.jsp;

import java.util.*;
import java.io.*;
import com.viaoa.hub.*;

/** base class used by OAForm, OASession, OAApplication, for storing objects and messages.
*/
public abstract class OABase implements Serializable {
    private static final long serialVersionUID = 1L;
	
    protected HashMap<String, Object> hashmap = new HashMap<String, Object>(29, 0.75f);

    protected transient ArrayList<String> alMessage = new ArrayList<String>(5);
    protected transient ArrayList<String> alError = new ArrayList<String>(5);
    protected transient ArrayList<String> alHidden = new ArrayList<String>(5);
    protected transient ArrayList<String> alPopup = new ArrayList<String>(5);
    
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
        alPopup.clear();
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
        return s;
    }
    public void clearMessages() {
        alMessage.clear();
    }
    
    public void addHiddenMessage(String msg) {
        alHidden.add(msg);
    }
    public String[] getHiddenMessages() {
        int x = alHidden.size();
        String[] s = new String[x];
        alHidden.toArray(s);
        return s;
    }
    public void clearHiddenMessages() {
        alHidden.clear();
    }

    public void addError(String msg) {
        addErrorMessage(msg);
    }
    public void addErrorMessage(String msg) {
        alError.add(msg);
    }
    public String[] getErrorMessages() {
        int x = alError.size();
        String[] s = new String[x];
        alError.toArray(s);
        return s;
    }
    public void clearErrorMessages() {
        alError.clear();
    }
    
    public void addPopupMessage(String msg) {
        alPopup.add(msg);
    }
    public String[] getPopupMessages() {
        int x = alPopup.size();
        String[] s = new String[x];
        alPopup.toArray(s);
        return s;
    }
    public void clearPopupMessages() {
        alPopup.clear();
    }
}

