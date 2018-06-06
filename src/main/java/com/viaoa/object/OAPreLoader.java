/* Copyright 1999-2015 Vince Via vvia@viaoa.com Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed
 * to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License. */
package com.viaoa.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.viaoa.ds.OADataSource;
import com.viaoa.ds.OASelect;
import com.viaoa.ds.jdbc.OADataSourceJDBC;
import com.viaoa.ds.jdbc.db.ManyToMany;
import com.viaoa.hub.*;
import com.viaoa.util.*;

/* This is used to preload objects.
 * 
 * Compared to OALoader, which loads objects by following a root object or Hub, and then goes through calling all of the methods in the property path.
 * ex:  Customers.Orders.Items.products
 * This can lead to a lot of calls to DS.
 *  
 * OAPreLoader is used when loading all objects, so that it can first preload the objects in the property path, and then use the cached objects to set
 * the property values.
 * 
 * This will load and set recursive properties, M2M properties.
 * 
 * Ex: from calss: ItemCategory, pp: items.products 
 *  where itemCategory is recursive and itemCategory.items is M2M 
 * 
 * */
public class OAPreLoader {
    private static Logger LOG = Logger.getLogger(OAPreLoader.class.getName());

    private Class classFrom;
    private String strPropertyPath;

    /**
     * Create a new pre loader.  Call load method to run.
     * @param classFrom base/root class for property path
     * @param propPath property path to load.  Note: all links need to be of type Many
     */
    public OAPreLoader(Class classFrom, String propPath) {
        this.classFrom = classFrom;
        this.strPropertyPath = propPath;
    }

    /**
     * load the property path. 
     * @return objects load from classFrom, so that gc does not remove objects from OAObjectCache
     */
    public ArrayList load() {
        if (classFrom == null) return null;

        OAPropertyPath propertyPath = null;
        OALinkInfo[] linkInfos = null;

        if (OAString.isNotEmpty(strPropertyPath)) {
            propertyPath = new OAPropertyPath(classFrom, strPropertyPath);
            linkInfos = propertyPath.getLinkInfos();
        }
        ArrayList al = null;
        try {
            OAThreadLocalDelegate.setLoading(true);
            al = _load(linkInfos);
        }
        finally {
            OAThreadLocalDelegate.setLoading(false);
        }
        return al;
    }

    protected ArrayList _load(OALinkInfo[] linkInfos) {
        final HashMap<Class, ArrayList> hm = new HashMap<>();

        final ArrayList al = load(classFrom, null);
        hm.put(classFrom, al);

        if (linkInfos != null) {
            for (OALinkInfo linkInfo : linkInfos) {
                Class c = linkInfo.getToClass();

                if (linkInfo.getType() != OALinkInfo.MANY) break;
                
                ArrayList alx = hm.get(c);
                if (alx == null) {
                    alx = load(c, linkInfo);
                    hm.put(c, alx);
                }
                
                if (linkInfo.isMany2Many()) {
                    loadMtoM(linkInfo);
                }
                else {
                    loadOtoM(linkInfo, alx);
                }
            }
        }
        return al;
    }

    protected void loadOtoM(OALinkInfo linkInfo, ArrayList alMany) {
        if (linkInfo == null || linkInfo.getType() != OALinkInfo.MANY) return;
        if (linkInfo.getPrivateMethod()) return;

        OALinkInfo liMany = linkInfo;
        OALinkInfo liOne = linkInfo.getReverseLinkInfo();
        if (liOne == null || liOne.getType() != OALinkInfo.ONE) return;
        
        for (Object objFromMany : alMany) {
            Object objOne = ((OAObject)objFromMany).getProperty(liOne.getName());
            if (!(objOne instanceof OAObject)) continue;
            
            Hub hub;
            Object objOneHub = OAObjectPropertyDelegate.getProperty((OAObject)objOne, liMany.getName(), false, true); 
            if (objOneHub instanceof Hub) {
                hub = (Hub) objOneHub;
            }
            else {
                hub = new Hub(liMany.getToClass());
                OAObjectPropertyDelegate.setProperty((OAObject) objOne, liMany.getName(), hub);
            }
            hub.add(objFromMany);
        }
    }
    
    
    protected void loadMtoM(OALinkInfo linkInfo) {
        if (linkInfo == null || !linkInfo.isMany2Many()) return;
        OADataSource ds = OADataSource.getDataSource(linkInfo.getToClass());
        if (!(ds instanceof OADataSourceJDBC)) return;

        OALinkInfo liA = linkInfo;
        OALinkInfo liB = linkInfo.getReverseLinkInfo();
        if (liB == null) return;
        
        Class classA = liB.getToClass();
        Class classB = liA.getToClass();
        
        ArrayList<ManyToMany> alManyToMany = ((OADataSourceJDBC) ds).getManyToMany(linkInfo);
        if (alManyToMany == null) return;
        
        for (ManyToMany mm : alManyToMany) {
            Object objA = OAObjectCacheDelegate.get(classA, mm.ok1);
            Object objB = OAObjectCacheDelegate.get(classB, mm.ok2);
            if (objA == null || objB == null) continue;

            if (!liA.getPrivateMethod()) {
                Hub hub;
                Object objx = OAObjectPropertyDelegate.getProperty((OAObject) objA, liA.getName(), false, true);
                if (objx instanceof Hub) {
                    hub = (Hub) objx;
                }
                else {
                    hub = new Hub(classB);
                    OAObjectPropertyDelegate.setProperty((OAObject) objA, liA.getName(), hub);
                }
                hub.add(objB);
            }
            
            if (!liB.getPrivateMethod()) {
                Hub hub;
                Object objx = OAObjectPropertyDelegate.getProperty((OAObject) objB, liB.getName(), false, true);
                if (objx instanceof Hub) {
                    hub = (Hub) objx;
                }
                else {
                    hub = new Hub(classA);
                    OAObjectPropertyDelegate.setProperty((OAObject) objB, liB.getName(), hub);
                }
                hub.add(objA);
            }
        }
    }

    // 1toM recursive - load all then populate f.hubChildren
    protected ArrayList load(Class clazz, final OALinkInfo linkInfo) {
        OASelect sel = new OASelect<>(clazz);
        OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(clazz);
        OALinkInfo liRecursive = OAObjectInfoDelegate.getRecursiveLinkInfo(oi, OALinkInfo.MANY);

        String sortOrder = null;
        if (liRecursive != null) {
            OALinkInfo liRev = liRecursive.getReverseLinkInfo();
            if (liRev != null) {
                sortOrder = liRecursive.getSortProperty();
            }
        }
        if (sortOrder == null && linkInfo != null) {
            sortOrder = linkInfo.getSortProperty();
        }
        if (OAString.isNotEmpty(sortOrder)) sel.setOrder(sortOrder);

        ArrayList al = new ArrayList<>();
        for (;;) {
            Object obj = sel.next();
            if (obj == null) break;
            al.add(obj);
        }
        
        if (liRecursive != null) {
            loadRecursive(clazz, al, liRecursive);

            // might need to reselect if both had different sortOrder
            String s = liRecursive.getSortProperty();
            if (linkInfo != null && OAString.isNotEmpty(s)) {
                String s2 = linkInfo.getSortProperty();
                if (OAString.isNotEmpty(s2)) {
                    if (!OAString.isEqual(s, s2, true)) {
                        sel = new OASelect<>(clazz);
                        sel.setOrder(s);
                        al = new ArrayList<>();
                        for (;;) {
                            Object obj = sel.next();
                            if (obj == null) break;
                            al.add(obj);
                        }
                    }
                }
            }
        }
        return al;
    }

    protected void loadRecursive(Class clazz, ArrayList al, OALinkInfo liMany) {
        if (liMany == null) return;
        OALinkInfo liOne = liMany.getReverseLinkInfo();
        if (liOne == null) return;

        for (Object f : al) {
            Object fParent = liOne.getValue(f);
            if (!(fParent instanceof OAObject)) continue;

            Hub hub;
            Object objx = OAObjectPropertyDelegate.getProperty((OAObject) fParent, liMany.getName(), false, true);
            if (objx instanceof Hub) {
                hub = (Hub) objx;
            }
            else {
                hub = new Hub(clazz);
                OAObjectPropertyDelegate.setProperty((OAObject) fParent, liMany.getName(), hub);
            }
            hub.add(f);
        }
    }

}
