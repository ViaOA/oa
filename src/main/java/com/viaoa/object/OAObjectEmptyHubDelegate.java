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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.logging.Logger;

import com.viaoa.hub.Hub;
import com.viaoa.util.OAArray;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OANullObject;


/**
 * This is used to store a reference to all empty hubs, so that a restart can 
 * create as an empty Hub, and not go to DS.
 * 
 * see: OAObjectReflectDelegate.getReferenceHub(), which will create an empty hub without
 * accessing the db.
 * 
 * @author vvia
 */
public class OAObjectEmptyHubDelegate {
    private static Logger LOG = Logger.getLogger(OAObjectEmptyHubDelegate.class.getName());

    private static HashMap<String, HashMap<Integer, String[]>> map;    
    private static boolean bEnabled;
    
    /**
     * Called by OAObject.afterLoad() to initialize any Hubs that are empty, so that
     * they will not need to go to the database.
     * @param oaObj
     */
    public static void initialize(OAObject obj) {
        if (map == null) return;
        if (obj == null) return;
        
        Class clazz = obj.getClass();

        HashMap<Integer, String[]> hm = map.get(clazz.getName());
        if (hm == null) return;
        
        OAObjectKey key = OAObjectKeyDelegate.getKey(obj);
        if (key == null) return;
        
        Object[] keys = key.getObjectIds();
        if (keys == null || keys.length != 1 || !(keys[0] instanceof Integer)) return;
            
        int x = (Integer) keys[0];
        
        Object objx = hm.get(x);
        if (objx == null) return;
        if (!(objx instanceof String[])) return;

        hm.remove(x);
        
        for (String s : (String[]) objx) {
            OAObjectPropertyDelegate.setProperty(obj, s, null);
        }
    }
 
    /**
     * Load the file that contains info for all empty hubs.
     * Note: the file should then be deleted, and not reused.
     */
    public static void load(File file) throws Exception {
        if (file == null || !file.exists()) {
            LOG.fine("file does not exist");
            return;
        }
        FileInputStream fis = new FileInputStream(file);

        ObjectInputStream ois = new ObjectInputStream(fis);
        
        OADateTime dt = (OADateTime) ois.readObject();
        
        map = (HashMap<String, HashMap<Integer, String[]>>) ois.readObject();
        
        ois.close();
        fis.close();
    }
    
    
    /**
     * This can be called at program close, to create a list of all objects
     * with empty hubs - to be used by load.
     */
    public static void save(File file) throws Exception {
        LOG.fine("saving all null properties");

        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(new OADateTime());
        
        final HashMap<String, HashMap<Integer, String[]>> mapx = new HashMap<String, HashMap<Integer,String[]>>();
        
        OAObjectCacheDelegate.callback(new OACallback() {
            int cnt = 0;
            @Override
            public boolean updateObject(Object obj) {
                if (!(obj instanceof OAObject)) return true;
                cnt++;
                if (cnt % 250 == 0) {
                    LOG.fine(cnt+") saving "+obj);
                }
                
                String[] ssNew = null;
                String[] ss = OAObjectPropertyDelegate.getPropertyNames((OAObject) obj);
                if (ss != null) { 
                    for (String s : ss) {
                        if (OAObjectReflectDelegate.isReferenceHubLoadedAndEmpty((OAObject) obj, s)) {
                            ssNew = (String[]) OAArray.add(String.class, ssNew, s);
                        }
                    }
                }
                if (ssNew == null) return true;
                
                OAObjectKey key = OAObjectKeyDelegate.getKey((OAObject)obj);
                if (key == null) return true;
                
                Object[] keys = key.getObjectIds();
                if (keys == null || keys.length != 1 || !(keys[0] instanceof Integer)) return true;
                    
                int keyId = (Integer) keys[0];
                
                Class clazz = obj.getClass();
                HashMap<Integer, String[]> hm = mapx.get(clazz.getName());
                if (hm == null) {
                    hm = new HashMap<Integer, String[]>();
                    mapx.put(clazz.getName(), hm);
                }
                hm.put(keyId, ssNew);
                return true;
            }
        });
        
        oos.writeObject(mapx);
        oos.close();
    }
    
}
