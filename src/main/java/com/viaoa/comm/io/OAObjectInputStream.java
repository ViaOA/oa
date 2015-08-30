/* Copyright 1999-2015 Vince Via vvia@viaoa.com Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed
 * to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License. */
package com.viaoa.comm.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.util.Tuple;

/**
 * 
 * This is used to read an objectStream that has classes in it that no longer exist, or that
 * the package and/or class name have changed.
 * 
 * If the class does not exist anymore, then it will be replaced with an IODummy class, and ignored.
 *
 * @author vvia
 */
public class OAObjectInputStream extends ObjectInputStream {
    private HashMap<String, Boolean> hmReplace = new HashMap<String, Boolean>();
    private String oldPackageName, newPackageName;
    private HashMap<String, String> hmReplaceName = new HashMap<String, String>();

    public OAObjectInputStream(InputStream is) throws IOException {
        this(is, null, null);
    }
    
    public OAObjectInputStream(InputStream is, String oldPackageName) throws IOException {
        this(is, oldPackageName, oldPackageName);
    }

    /**
     * 
     * @param is
     * @param oldPackageName package name to look for.
     * @param newPackageName new package name to use, if null then it will use oldpackagename.
     * @throws IOException
     */
    public OAObjectInputStream(InputStream is, String oldPackageName, String newPackageName) throws IOException {
        super(is);
        enableResolveObject(true);
        this.oldPackageName = oldPackageName;
        if (newPackageName == null) newPackageName = oldPackageName;
        this.newPackageName = newPackageName;
    }
    
    public void replaceClassName(String oldName, String newName) {
        hmReplaceName.put(oldName, newName);
    }
    
    @Override
    protected Object resolveObject(Object obj) throws IOException {
        obj = super.resolveObject(obj);
        if (obj instanceof IODummy) {
            OAObjectPropertyDelegate.clearProperties((OAObject) obj);
        }
        return obj;
    }
    
    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass cd = super.readClassDescriptor();
        
        boolean bReplace = false;
        Field f = null;
        try {
            f = cd.getClass().getDeclaredField("name");
            f.setAccessible(true);
            String name = (String) f.get(cd);
            String newName;
            if (oldPackageName != null) {
                if (!name.startsWith(oldPackageName)) return cd;
    
                String s = name.substring(oldPackageName.length()+1);
                newName = hmReplaceName.get(s);
                if (newName == null) newName = s;
                
                newName = newPackageName + "." + newName;
                f.set(cd, newName);
            }
            else newName = name;
            
            Object objx = hmReplace.get(newName);
            
            if (objx == null) {
                hmReplace.put(newName, true);
                bReplace = true;
                Class c = Class.forName(newName);
                bReplace = false;
                hmReplace.put(newName, false);
            }
            else {
                bReplace = ((Boolean) objx).booleanValue();
            }
            
        }
        catch (Exception e) {
        }
        
        if (bReplace) {
            try {
                f.set(cd, IODummy.class.getName());
            }
            catch (Exception e2) {
                // TODO: handle exception
            }
        }
        return cd;
    }


}
