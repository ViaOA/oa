/*
2004/01/30 does not convert names to uppercase.  Methods to retreive/get and remove values are case insensitive


This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.util;

import java.util.*;
import java.io.*;

/**
    Subclass of java.util.Properties where all "names" for name/value pairs are case insensitive.<br>
    NOTE: All name/value pairs are converted to Strings.
    <p>
    This can be used for working with *.ini files.
*/
public class OAProperties extends java.util.Properties implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String fileName;
    protected boolean bUpperCase=true;
    private ArrayList<String> alKeys = new ArrayList<String>();


    /**
        Creates a new OAProperties.
    */
    public OAProperties() {
    }

    /**
        Creates a new OAProperties using the specified file name and loads the file.
        @see #load
    */
    public OAProperties(String fname) {
        setFileName(fname);
        load();
    }

    public OAProperties(InputStream in) {
        load(in);
    }

    /**
        Sets name of file that properties are associated with.
        @see #load
        @see #save
    */
    public void setFileName(String fname) {
        this.fileName = fname;
    }

    /**
        Returns name of file that properties are associated with.
    */
    public String getFileName() {
        return fileName;
    }

    /**
        Load name/value properties from file.

        @throws IllegalArgumentException if fileName has not been set.
        @see #setFileName
    */
    public void load() {
        if (fileName == null) throw new IllegalArgumentException("fileName must be set before calling load()");
        load(fileName);
    }

    /**
        Converts fileName path to correct system file.separator chars and loads name/value properties.
    */
    public void load(String fileName) {
        fileName = OAString.convertFileName(fileName);
        setFileName(fileName);
        try {
            File file = new File(fileName);
            if (!file.exists()) return;
            FileInputStream in = new FileInputStream(file);
            load(in);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads name/values from a file.
     * NOTE: if value has spaces after it on the line, they will be included in the value of the property. 
     */
    public void load(InputStream in) {
        try {
            super.load(in);
            in.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
        Saves name/value properties to file.
        @throws IllegalArgumentException if fileName has not been set.
        @see #setFileName
        @see #save(String,String)
    */
    public void save() {
        if (fileName == null) throw new IllegalArgumentException("fileName must be set before calling save()");
        save(fileName,"");
    }

    /**
        Converts fileName path to correct system file.separator chars and saves name/value properties to file.
        @see #ssave(String,String)
    */
    public void save(String fileName) {
        save(fileName, "");
    }

    /**
        Converts fileName path to correct system file.separator chars and saves name/value properties to file.
        @param fileName name of file to store name/values.
        @param title is commented title used within file.
    */
    public void save(String fileName, String title) {
        if (fileName == null) {
            throw new RuntimeException("OAProperties.save() fileName is not assigned (null)");
        }
        fileName = OAString.convertFileName(fileName);
        setFileName(fileName);

        OAFile.mkdirsForFile(fileName);
        FileOutputStream os = null;
        try {
            File file = new File(fileName);
            os = new FileOutputStream(file);
            if (title == null) title = "";
            super.save(os, title);
            os.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
        Retrieve value of a name/value pair where name is case insensitive.
        @param name is not case sensitive
    */
    public String getProperty(String name) {
        if (name == null) return null;

        String s = (String) super.getProperty(name);
        if (s != null) return s;

        Enumeration enumx = this.keys();
        for (;enumx.hasMoreElements();) {
            s = (String) enumx.nextElement();
            if (s != null && s.equalsIgnoreCase(name)) return super.getProperty(s);
        }
        return null;
    }

    /**
        Retrieve value of a name/value pair where name is case insensitive.  If property does not exists, then
        a default value can be specified to use.
        @param name is name of property and is not case sensitive.
        @param defaultValue is returned if property does not exist.
    */
    public String getProperty(String name, String defaultValue) {
        String s = this.getProperty(name);
        if (s != null) return s;
        return defaultValue;
    }


    /**
        Retrieve value of a name/value pair where name is case insensitive.
        @param name is name of property and is not case sensitive.
    */
    public Object get(String name) {
        if (name == null) return null;
        return this.getProperty(name);
    }

    public Object setProperty(String name, String value) {
        return this.put(name, value);
    }

    public Object setProperty(String name, boolean value) {
    	return put(name, OAConv.toString(value));
    }
    
    public Object setProperty(String name, int value) {
    	return put(name, OAConv.toString(value));
    }

    public Object setProperty(String name, long value) {
    	return put(name, OAConv.toString(value));
    }
    
    public Object setProperty(String name, double value) {
    	return put(name, OAConv.toString(value));
    }

    public Object setProperty(String name, Object value) {
    	return put(name, OAConv.toString(value));
    }
    
    
    @Override
    public synchronized Enumeration keys() {
        if (alKeys == null) {
            return super.keys();
        }
        Enumeration enumx = new Enumeration() {
            int pos = 0;
            @Override
            public boolean hasMoreElements() {
                return pos < alKeys.size();
            }
            @Override
            public Object nextElement() {
                Object obj = alKeys.get(pos);
                pos++;
                return obj;
            }
        };
        return enumx;
    }
    @Override
    public synchronized void clear() {
        super.clear();
        alKeys.clear();
    }
   
    /**
        Add/replace a name/value.
        @param name is name of property and is not case sensitive.
    */
    @Override
    public synchronized Object put(Object key, Object obj) {
        if (!(key instanceof String)) return null;
        String name = (String) key;
        
        if (super.getProperty(name) == null) {
            // see if it exists under a different case
            Enumeration enumx = this.keys();
            for (;enumx.hasMoreElements();) {
                String s = (String) enumx.nextElement();
                if (s != null && s.equalsIgnoreCase(name)) {
                    if (obj == null) return super.remove(s);
                    remove(s);
                    break;
                }
            }
            if (obj == null) return null;
        }
        else {
            if (obj == null) return remove(name);
        }
        if (!alKeys.contains(name)) alKeys.add(name);
        return super.put(name, OAConv.toString(obj));
    }


    /**
        Remove a name/value.
        @param name is name of property and is not case sensitive.
    */
    @Override
    public synchronized Object remove(Object key) {
        if (!(key instanceof String)) return null;
        String name = (String) key;
        alKeys.remove(name);
        Object obj = super.remove(name);
        if (obj != null) return obj;

        Enumeration enumx = this.keys();
        for (;enumx.hasMoreElements();) {
            String s = (String) enumx.nextElement();
            if (s != null && s.equalsIgnoreCase(name)) {
                alKeys.remove(name);
                return super.remove(s);
            }
        }
        return null;
    }

    /**
        Add/replace a name/value.
        @param name is name of property and is not case sensitive.
        @see #put(String,Object)
    */
    public void put(String name, int i) {
        if (name != null) put(name, (i+""));
    }
    /**
        Add/replace a name/value.
        @param name is name of property and is not case sensitive.
        @see #put(String,Object)
    */
    public void put(String name, boolean b) {
        if (name != null) put(name, b+"");
    }
    /**
        Add/replace a name/value.
        @param name is name of property and is not case sensitive.
        @see #put(String,Object)
    */
    public void putInt(String name, int i) {
        this.put(name, i);
    }
    /**
        Returns a property that is converted to an "int".  If name/value does not exist,
        or not a number, then -1 is returned.
        @param name is name of property and is not case sensitive.
    */
    public int getInt(String name) {
        Object obj = getProperty(name);
        if (obj == null) return -1;
        if (obj instanceof String) {
            obj = (Object) OAConv.convert(Integer.class, obj);
        }
        if (obj == null || !(obj instanceof Number)) return -1;
        return ((Number)obj).intValue();
    }
    /**
        Returns a property that is converted to an "int".  If name/value does not exist,
        then iDefault is returned.
        if value is not a number, then -1 is returned.
        @param name is name of property and is not case sensitive.
    */
    public int getInt(String name, int iDefault) {
        Object obj = getProperty(name);
        if (obj == null) return iDefault;
        if (obj == null) return -1;
        if (obj instanceof String) {
            obj = (Object) OAConv.convert(Integer.class, obj);
        }
        if (obj == null || !(obj instanceof Number)) return -1;
        return ((Number)obj).intValue();
    }

    /**
        Returns a property value.  If property does not exist, then null is returned.
        @param name is name of property and is not case sensitive.
    */
    public String getString(String name) {
        if (name == null) return null;
        return this.getProperty(name);
    }

    /**
        Returns a property value.  If property does not exist, then null is returned.
        @param name is name of property and is not case sensitive.
    */
    public String getString(String name, String strDefault) {
        if (name == null) return null;
        String s = this.getProperty(name);
        if (s == null) return strDefault;
        return s;
    }

    /**
        Returns a property that is converted to a "boolean".  If name/value does not exist, then false is returned.
        @param name is name of property and is not case sensitive.
    */
    public boolean getBoolean(String name) {
        if (name == null) return false;
        Object obj = this.getProperty(name);
        Boolean b = (Boolean) OAConverter.convert(Boolean.class, obj);
        if (b == null) return false;
        return ((Boolean)b).booleanValue();
    }
    /**
        Returns a property that is converted to a "boolean".  If name/value does not exist, then false is returned.
        @param name is name of property and is not case sensitive.
    */
    public boolean getBoolean(String name, boolean bDefault) {
        if (name == null) return false;
        Object obj = this.getProperty(name);
        if (obj == null) return bDefault;
        Boolean b = (Boolean) OAConverter.convert(Boolean.class, obj);
        if (b == null) return false;
        return ((Boolean)b).booleanValue();
    }


    /**
        Returns true if property name exists.
        @param name is name of property and is not case sensitive.
    */
    public boolean exists(String name) {
        if (name == null) return false;
        Object obj = this.getProperty(name);
        return obj != null;
    }

    
    
}

