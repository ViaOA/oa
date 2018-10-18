/*  Copyright 1999-2016 Vince Via vvia@viaoa.com
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.annotation.*;
import com.viaoa.ds.jdbc.db.*;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.util.*;


/**
 * Delegate used load OAObject annotations into OAObjectInfo, Database, etc
 * @author vvia
 * 
 */
public class OAAnnotationDelegate {
    private static Logger LOG = Logger.getLogger(OAAnnotationDelegate.class.getName());
    /**
     * Load/update ObjectInfo using annotations
     */
    public static void update(OAObjectInfo oi, Class clazz) {
        HashSet<String> hs = new HashSet<String>();
        for ( ; clazz != null; ) {
            if (OAObject.class.equals(clazz)) break;
            _update(oi, clazz, hs);
            clazz = clazz.getSuperclass();
        }
    }    

    /**
     * needs to be called after OAObjectInfo has been created.
     */
    public static void update2(OAObjectInfo oi, Class clazz) {
        HashSet<String> hs = new HashSet<String>();
        for ( ; clazz != null; ) {
            if (OAObject.class.equals(clazz)) break;
            _update2(oi, clazz);
            clazz = clazz.getSuperclass();
        }
    }    
    
    private static void _update(final OAObjectInfo oi, final Class clazz, HashSet<String> hs) {
        String s;
        
        if (!hs.contains("OAClass")) {
            OAClass oaclass = (OAClass) clazz.getAnnotation(OAClass.class);
            if (oaclass != null) {
                hs.add("OAClass");
                oi.setUseDataSource(oaclass.useDataSource());
                oi.setLocalOnly(oaclass.localOnly());
                oi.setAddToCache(oaclass.addToCache());
                oi.setInitializeNewObjects(oaclass.initialize());
                oi.setDisplayName(oaclass.displayName());

                // 20140118 rootTreePropertyPaths
                String[] pps = oaclass.rootTreePropertyPaths();
                oi.setRootTreePropertyPaths(pps);
                oi.setLookup(oaclass.isLookup());
                oi.setProcessed(oaclass.isProcessed());
            }
            OAEditQuery eq = (OAEditQuery) clazz.getAnnotation(OAEditQuery.class);
            if (eq != null) {
                oi.setEnabledProperty(eq.enabledProperty());
                oi.setEnabledValue(eq.enabledValue());
                oi.setVisibleProperty(eq.visibleProperty());
                oi.setVisibleValue(eq.visibleValue());
                oi.setUserEnabledProperty(eq.userEnabledProperty());
                oi.setUserEnabledValue(eq.userEnabledValue());
                oi.setUserVisibleProperty(eq.userVisibleProperty());
                oi.setUserVisibleValue(eq.userVisibleValue());
                oi.setViewDependentProperties(eq.viewDependentProperties());
                oi.setUserDependentProperties(eq.userDependentProperties());
            }
        }
        // prop ids
        Method[] methods = clazz.getDeclaredMethods();
        String[] ss = oi.getIdProperties();
        for (Method m : methods) {
            OAId oaid = m.getAnnotation(OAId.class);
            if (oaid == null) continue;
            OAProperty oaprop = (OAProperty) m.getAnnotation(OAProperty.class);
            if (oaprop == null) {
                String sx = "annotation OAId - should also have OAProperty annotation";
                LOG.log(Level.WARNING, sx, new Exception(sx));
            }
            s = getPropertyName(m.getName());
            if (hs.contains("OAId." + s)) continue;
            hs.add("OAId."+s);
            
            int pos = oaid.pos();

            if (ss == null) {
                ss = new String[pos+1];
            }
            else {
                if (pos >= ss.length) {
                    String[] ss2 = new String[pos+1];
                    System.arraycopy(ss, 0, ss2, 0, pos);
                    ss = ss2;
                }
                if (ss[pos] != null) {
                    if (!ss[pos].equalsIgnoreCase(s)) {
                        String sx = "annotation OAId - duplicate pos for property id";
                        LOG.log(Level.WARNING, sx, new Exception(sx));
                    }
                }
            }
            ss[pos] = s;
        }
        
        for (String sx : ss) {
            if (sx == null) {
                sx = "annotation OAId - missing pos for property id(s)";
                LOG.log(Level.WARNING, sx, new Exception(sx));
            }
        }
        oi.setPropertyIds(ss);

/*qqq test        
if (clazz.getName().equals("com.cdi.model.oa.SalesOrder")) {
    int xx = 4;
    xx++;
}
*/
        // properties
        for (Method m : methods) {
            OAProperty oaprop = (OAProperty) m.getAnnotation(OAProperty.class);
            if (oaprop == null) continue;
            String name = getPropertyName(m.getName());
            if (hs.contains("prop." + name)) continue;
            hs.add("prop."+name);
            
            OAPropertyInfo pi = oi.getPropertyInfo(name);            
            if (pi == null) {
                pi = new OAPropertyInfo();
                pi.setName(name);
                oi.addPropertyInfo(pi);
            }
            pi.setDisplayName(pi.getDisplayName());
            pi.setColumnName(pi.getColumnName());
            pi.setMaxLength(oaprop.maxLength());
            pi.setDisplayLength(oaprop.displayLength());
            pi.setColumnLength(oaprop.columnLength());
            pi.setRequired(oaprop.required());
            pi.setDecimalPlaces(oaprop.decimalPlaces());
            pi.setId(m.getAnnotation(OAId.class) != null);
            pi.setUnique(oaprop.isUnique());
            pi.setProcessed(oaprop.isProcessed());
            pi.setClassType(m.getReturnType());

            OAColumn oacol = (OAColumn) m.getAnnotation(OAColumn.class);
            if (oacol != null) {
                int x = oacol.maxLength();
                if (x > 0) pi.setMaxLength(x);
            }
            
            boolean b = oaprop.isBlob();
            if (b) {
                b = false;
                Class c = m.getReturnType();
                if (c.isArray()) {
                    c = c.getComponentType();
                    if (c.equals(byte.class)) b = true;
                }
            }
            pi.setBlob(b);
            pi.setNameValue(oaprop.isNameValue());
            pi.setUnicode(oaprop.isUnicode());
            pi.setImportMatch(oaprop.isImportMatch());
            pi.setPassword(oaprop.isPassword());
            pi.setCurrency(oaprop.isCurrency());
            pi.setOAProperty(oaprop);
            
            if (oaprop.isNameValue()) {
                Hub<String> h = pi.getNameValues();
                try {
                    Field f = clazz.getField("hub"+OAString.mfcu(name));
                    Object objx = f.get(null);
                    if (objx instanceof Hub) {
                        for (Object o : ((Hub) objx)) {
                            if (o instanceof String) {
                                h.add((String)o);
                            }
                        }
                    }
                }
                catch (Exception e) {
                }
            }
            
            OAEditQuery eq = (OAEditQuery) m.getAnnotation(OAEditQuery.class);
            if (eq != null) {
                pi.setEnabledProperty(eq.enabledProperty());
                pi.setEnabledValue(eq.enabledValue());
                pi.setVisibleProperty(eq.visibleProperty());
                pi.setVisibleValue(eq.visibleValue());
                pi.setUserEnabledProperty(eq.userEnabledProperty());
                pi.setUserEnabledValue(eq.userEnabledValue());
                pi.setUserVisibleProperty(eq.userVisibleProperty());
                pi.setUserVisibleValue(eq.userVisibleValue());
                pi.setViewDependentProperties(eq.viewDependentProperties());
                pi.setUserDependentProperties(eq.userDependentProperties());
            }
        }
      
        // calcProperties
        ArrayList<OACalcInfo> alCalc = oi.getCalcInfos();
        for (Method m : methods) {
            OACalculatedProperty annotation = (OACalculatedProperty) m.getAnnotation(OACalculatedProperty.class);
            if (annotation == null) continue;

            boolean bHub = false;
            Class[] cs = m.getParameterTypes();
            if (cs != null && cs.length == 1 && Hub.class.equals(cs[0])) {
                if (Modifier.isStatic(m.getModifiers())) bHub = true;
            }
            
            String name = getPropertyName(m.getName(), false);
            if (hs.contains("calc." + name)) continue;
            hs.add("calc."+name);
            
            OACalcInfo ci = OAObjectInfoDelegate.getOACalcInfo(oi, name);
            if (ci == null) {
                ci = new OACalcInfo(name, annotation.properties(), bHub);
                oi.addCalcInfo(ci);
            }
            else {
                ci.setDependentProperties(annotation.properties());
            }
            ci.setOACalculatedProperty(annotation);
            ci.setClassType(m.getReturnType());

            OAEditQuery eq = (OAEditQuery) m.getAnnotation(OAEditQuery.class);
            if (eq != null) {
                ci.setEnabledProperty(eq.enabledProperty());
                ci.setEnabledValue(eq.enabledValue());
                ci.setVisibleProperty(eq.visibleProperty());
                ci.setVisibleValue(eq.visibleValue());
                ci.setUserEnabledProperty(eq.userEnabledProperty());
                ci.setUserEnabledValue(eq.userEnabledValue());
                ci.setUserVisibleProperty(eq.userVisibleProperty());
                ci.setUserVisibleValue(eq.userVisibleValue());
                ci.setViewDependentProperties(eq.viewDependentProperties());
                ci.setUserDependentProperties(eq.userDependentProperties());
            }
            ci.setEditQueryMethod(m);
        }

        // linkInfos
        List<OALinkInfo> alLinkInfo = oi.getLinkInfos();
        // Ones
        for (Method m : methods) {
            OAOne annotation = (OAOne) m.getAnnotation(OAOne.class);
            if (annotation == null) continue;
            Class c = m.getReturnType();
                        
            String name = getPropertyName(m.getName(), false);
            if (hs.contains("link." + name)) continue;
            hs.add("link."+name);
            
            OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, name);
            if (li == null) {
                li = new OALinkInfo(name, m.getReturnType(), OALinkInfo.ONE);
                oi.addLinkInfo(li);
            }
          
            li.setCalcDependentProperties(annotation.calcDependentProperties());
            li.setImportMatch(annotation.isImportMatch());
            li.setCascadeSave(annotation.cascadeSave());
            li.setCascadeDelete(annotation.cascadeDelete());
            li.setReverseName(annotation.reverseName());
            li.setOwner(annotation.owner());
            li.setAutoCreateNew(annotation.autoCreateNew());
            li.setMustBeEmptyForDelete(annotation.mustBeEmptyForDelete());
            li.setCalculated(annotation.isCalculated());
            li.setProcessed(annotation.isProcessed());
            //li.setRecursive(annotation.recursive());
            li.setOAOne(annotation);

            OAEditQuery eq = (OAEditQuery) m.getAnnotation(OAEditQuery.class);
            if (eq != null) {
                li.setEnabledProperty(eq.enabledProperty());
                li.setEnabledValue(eq.enabledValue());
                li.setVisibleProperty(eq.visibleProperty());
                li.setVisibleValue(eq.visibleValue());
                li.setUserEnabledProperty(eq.userEnabledProperty());
                li.setUserEnabledValue(eq.userEnabledValue());
                li.setUserVisibleProperty(eq.userVisibleProperty());
                li.setUserVisibleValue(eq.userVisibleValue());
                li.setViewDependentProperties(eq.viewDependentProperties());;
                li.setUserDependentProperties(eq.userDependentProperties());;
            }
        }
        // Manys
        for (Method m : methods) {
            Class c = m.getReturnType();
            if (!Hub.class.isAssignableFrom(c)) continue; // 20111027 added
            if ((m.getModifiers() & Modifier.STATIC) > 0) continue;
                        
            String name = getPropertyName(m.getName(), false);
            
            OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, name);
            OAMany annotation = (OAMany) m.getAnnotation(OAMany.class);
            Class cx = OAAnnotationDelegate.getHubObjectClass(annotation, m);

            if (li == null) {
                li = new OALinkInfo(name, cx, OALinkInfo.MANY);
                oi.addLinkInfo(li);
            }
            
            if (cx != null) li.setToClass(cx);
            if (annotation == null) continue;

            if (hs.contains("link." + name)) continue;
            hs.add("link."+name);
            
            li.setCascadeSave(annotation.cascadeSave());
            li.setCascadeDelete(annotation.cascadeDelete());
            li.setReverseName(annotation.reverseName());
            li.setOwner(annotation.owner());
            li.setRecursive(annotation.recursive());
            li.setCacheSize(annotation.cacheSize());
            li.setCouldBeLarge(annotation.couldBeLarge());
            li.setProcessed(annotation.isProcessed());

            s = annotation.matchHub();
            if (s != null && s.length() == 0) s = null;
            li.setMatchHub(s);

            s = annotation.matchProperty();
            if (s != null && s.length() == 0) s = null;
            li.setMatchProperty(s);
            
            s = annotation.uniqueProperty();
            if (s != null && s.length() == 0) s = null;
            li.setUniqueProperty(s);

            s = annotation.sortProperty();
            if (s != null && s.length() == 0) s = null;
            li.setSortProperty(s);
            li.setSortAsc(annotation.sortAsc());
            
            s = annotation.seqProperty();
            if (s != null && s.length() == 0) s = null;
            li.setSeqProperty(s);
            
            li.setMustBeEmptyForDelete(annotation.mustBeEmptyForDelete());
            li.setCalculated(annotation.isCalculated());
            li.setCalcDependentProperties(annotation.calcDependentProperties());
            li.setServerSideCalc(annotation.isServerSideCalc());
            li.setPrivateMethod(!annotation.createMethod());
            li.setCacheSize(annotation.cacheSize());
            s = annotation.mergerPropertyPath();
            li.setMergerPropertyPath(s);
            li.setOAMany(annotation);

            OAEditQuery eq = (OAEditQuery) m.getAnnotation(OAEditQuery.class);
            if (eq != null) {
                li.setEnabledProperty(eq.enabledProperty());
                li.setEnabledValue(eq.enabledValue());
                li.setVisibleProperty(eq.visibleProperty());
                li.setVisibleValue(eq.visibleValue());
                li.setUserEnabledProperty(eq.userEnabledProperty());
                li.setUserEnabledValue(eq.userEnabledValue());
                li.setUserVisibleProperty(eq.userVisibleProperty());
                li.setUserVisibleValue(eq.userVisibleValue());
                li.setViewDependentProperties(eq.viewDependentProperties());;
                li.setUserDependentProperties(eq.userDependentProperties());;
            }
        }

        // methods
        for (Method m : methods) {
            OAMethod oamethod = (OAMethod) m.getAnnotation(OAMethod.class);
            if (oamethod == null) continue;
            final String name = m.getName();
            if (hs.contains("method." + name)) continue;
            hs.add("method."+name);
            
            OAMethodInfo mi = oi.getMethodInfo(name);            
            if (mi == null) {
                mi = new OAMethodInfo();
                mi.setName(name);
                mi.setOAMethod(oamethod);
                oi.addMethodInfo(mi);
            }
            
            OAEditQuery eq = (OAEditQuery) m.getAnnotation(OAEditQuery.class);
            if (eq != null) {
                mi.setEnabledProperty(eq.enabledProperty());
                mi.setEnabledValue(eq.enabledValue());
                mi.setVisibleProperty(eq.visibleProperty());
                mi.setVisibleValue(eq.visibleValue());
                mi.setUserEnabledProperty(eq.userEnabledProperty());
                mi.setUserEnabledValue(eq.userEnabledValue());
                mi.setUserVisibleProperty(eq.userVisibleProperty());
                mi.setUserVisibleValue(eq.userVisibleValue());
                mi.setViewDependentProperties(eq.viewDependentProperties());
                mi.setUserDependentProperties(eq.userDependentProperties());
            }
        }
        
        
        // onEditQueryXxx 
        for (final Method m : methods) {
            String name = m.getName();
            final OAEditQuery eq = (OAEditQuery) m.getAnnotation(OAEditQuery.class);
            final Class[] cs = m.getParameterTypes();
            if (eq == null) {
                s = name.toUpperCase();
                if (s.indexOf("EDITQUERY") >= 0 && s.indexOf("$") < 0) {
                    if (!s.endsWith("MODEL") && !s.startsWith("GET") && !s.startsWith("SET")) {
                        s = "missing @OAEditQuery() annotation, class="+clazz+", method="+m;
                        LOG.log(Level.WARNING, s, new Exception(s));
                    }
                }
                else {
                    continue;
                }
            }
            if (oi.getMethodInfo(name) != null) continue;
            s = getPropertyName(name);
            if (!s.equals(name)) {
                boolean b = true;
                if (oi.getPropertyInfo(s) == null) {
                    if (oi.getLinkInfo(s) == null) {
                        if (oi.getCalcInfo(s) == null) {
                            b = false;
                        }
                    }
                }
                if (b) continue;
            }
            
            String s2 = "onEditQuery";
            if (!name.startsWith(s2)) {
                s = "OAEditQuery annotation, class="+clazz+", method="+m+", should be named onEditQuery*";
                LOG.log(Level.WARNING, s, new Exception(s));
            }
            boolean b = (cs != null && cs.length == 1);
            if (b) {
                b = cs[0].equals(OAObjectEditQuery.class);
                if (!Modifier.isPublic(m.getModifiers())) {
                    s = "OAEditQuery annotation, class="+clazz+", method="+m+", should be public";
                    LOG.log(Level.WARNING, s, new Exception(s));
                }
                if (!b && cs[0].isAssignableFrom(OAObjectModel.class)) {
                    // public static void onEditQueryAddressesModel(OAObjectModel model)
                    if (!name.endsWith("Model")) {
                        s = "OAEditQuery annotation, class="+clazz+", method="+m+", should be named onEditQuery*Model";
                        LOG.log(Level.WARNING, s, new Exception(s));
                    }
                    s = name.substring(s2.length());
                    s = s.substring(0, s.length()-5);
                    OALinkInfo lix = oi.getLinkInfo(s);
                    if (lix == null) {
                        s = "OAEditQuery annotation, class="+clazz+", method="+m+", link not found, name="+s;
                        LOG.log(Level.WARNING, s, new Exception(s));
                    }
                    if (!Modifier.isStatic(m.getModifiers())) {
                        s = "OAEditQuery annotation, class="+clazz+", method="+m+", should be static";
                        LOG.log(Level.WARNING, s, new Exception(s));
                    }
                    continue;
                }
                if (Modifier.isStatic(m.getModifiers())) {
                    s = "OAEditQuery annotation, class="+clazz+", method="+m+", should not be static";
                    LOG.log(Level.WARNING, s, new Exception(s));
                }
            }
            if (!b) {
                s = "OAEditQuery annotation, class="+clazz+", method="+m+", should have one param of OAObjectEditQuery";
                LOG.log(Level.WARNING, s, new Exception(s));
            }
            name = name.substring(s2.length());
            
            oi.addEditQueryMethod(name, m);
            
            // make sure it belongs to a prop/calc/link/method
            OAPropertyInfo pi = oi.getPropertyInfo(name);
            if (pi != null) {
                pi.setEditQueryMethod(m);
                if (eq != null) {
                    s = eq.enabledProperty();
                    if (OAString.isNotEmpty(s)) {
                        pi.setEnabledProperty(s);
                        pi.setEnabledValue(eq.enabledValue());
                    }
                    s = eq.visibleProperty();
                    if (OAString.isNotEmpty(s)) {
                        pi.setVisibleProperty(s);
                        pi.setVisibleValue(eq.visibleValue());
                    }
                    s = eq.userEnabledProperty();
                    if (OAString.isNotEmpty(s)) {
                        pi.setUserEnabledProperty(s);
                        pi.setUserEnabledValue(eq.userEnabledValue());
                    }
                    s = eq.userVisibleProperty();
                    if (OAString.isNotEmpty(s)) {
                        pi.setUserVisibleProperty(s);
                        pi.setUserVisibleValue(eq.userVisibleValue());
                    }
                    pi.setViewDependentProperties(eq.viewDependentProperties());
                    pi.setUserDependentProperties(eq.userDependentProperties());
                }
            }
            else {
                if (name.length() == 0) {
                    oi.setEditQueryMethod(m);
                    if (eq != null) {
                        s = eq.enabledProperty();
                        if (OAString.isNotEmpty(s)) {
                            oi.setEnabledProperty(s);
                            oi.setEnabledValue(eq.enabledValue());
                        }
                        s = eq.visibleProperty();
                        if (OAString.isNotEmpty(s)) {
                            oi.setVisibleProperty(s);
                            oi.setVisibleValue(eq.visibleValue());
                        }
                        s = eq.userEnabledProperty();
                        if (OAString.isNotEmpty(s)) {
                            oi.setUserEnabledProperty(s);
                            oi.setUserEnabledValue(eq.userEnabledValue());
                        }
                        s = eq.userVisibleProperty();
                        if (OAString.isNotEmpty(s)) {
                            oi.setUserVisibleProperty(s);
                            oi.setUserVisibleValue(eq.userVisibleValue());
                        }
                        oi.setViewDependentProperties(eq.viewDependentProperties());
                        oi.setUserDependentProperties(eq.userDependentProperties());
                    }
                }
                else {
                    OALinkInfo li = oi.getLinkInfo(name);
                    if (li != null) {
                        li.setEditQueryMethod(m);
                        if (eq != null) {
                            s = eq.enabledProperty();
                            if (OAString.isNotEmpty(s)) {
                                li.setEnabledProperty(s);
                                li.setEnabledValue(eq.enabledValue());
                            }
                            s = eq.visibleProperty();
                            if (OAString.isNotEmpty(s)) {
                                li.setVisibleProperty(s);
                                li.setVisibleValue(eq.visibleValue());
                            }
                            s = eq.userEnabledProperty();
                            if (OAString.isNotEmpty(s)) {
                                li.setUserEnabledProperty(s);
                                li.setUserEnabledValue(eq.userEnabledValue());
                            }
                            s = eq.userVisibleProperty();
                            if (OAString.isNotEmpty(s)) {
                                li.setUserVisibleProperty(s);
                                li.setUserVisibleValue(eq.userVisibleValue());
                            }
                            li.setViewDependentProperties(eq.viewDependentProperties());
                            li.setUserDependentProperties(eq.userDependentProperties());
                        }
                    }
                    else {
                        OAMethodInfo mi = oi.getMethodInfo(name);
                        if (mi != null) {
                            mi.setEditQueryMethod(m);
                            if (eq != null) {
                                s = eq.enabledProperty();
                                if (OAString.isNotEmpty(s)) {
                                    mi.setEnabledProperty(s);
                                    mi.setEnabledValue(eq.enabledValue());
                                }
                                s = eq.visibleProperty();
                                if (OAString.isNotEmpty(s)) {
                                    mi.setVisibleProperty(s);
                                    mi.setVisibleValue(eq.visibleValue());
                                }
                                if (OAString.isNotEmpty(s)) {
                                    mi.setUserEnabledProperty(s);
                                    mi.setUserEnabledValue(eq.userEnabledValue());
                                }
                                s = eq.userVisibleProperty();
                                if (OAString.isNotEmpty(s)) {
                                    mi.setUserVisibleProperty(s);
                                    mi.setUserVisibleValue(eq.userVisibleValue());
                                }
                                mi.setViewDependentProperties(eq.viewDependentProperties());
                                mi.setUserDependentProperties(eq.userDependentProperties());
                            }
                        }
                        else {
                            b = false;
                            for (Method mx : methods) {
                                if (mx.getName().equals(name)) {
                                    b = true;
                                    break;
                                }
                            }
                            if (!b) {
                                s = "OAEditQuery annotation, class="+clazz+", method="+m+", could not find method that it goes with, ex: get"+name;
                                LOG.log(Level.WARNING, s, new Exception(s));
                            }
                        }
                    }
                }
            }
        }        
    }

    // 20160305 OACallback annotations
    private static void _update2(final OAObjectInfo oi, final Class clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        if (methods == null) return;
        String s;
        String[] ss;
        
        for (Method method : methods) {
            OATriggerMethod annotation = (OATriggerMethod) method.getAnnotation(OATriggerMethod.class);
            if (annotation == null) continue;
            
            String[] props = annotation.properties();
            if (props == null || props.length == 0) continue;
            final boolean bBackgroundThread = annotation.runInBackgroundThread(); 
            final boolean bOnlyUseLoadedData = annotation.onlyUseLoadedData();
            boolean bServerSideOnly = annotation.runOnServer(); 
            
            // verify that method signature is correct, else log.warn
            s = "public void callbackName(HubEvent hubEvent)";
            s = ("callback method signature for class="+clazz.getSimpleName()+", callbackMethod="+method.getName()+", must match: "+s);
            Class[] cs = method.getParameterTypes();
            if (cs == null || cs.length != 1 || !Modifier.isPublic(method.getModifiers())) {
                throw new RuntimeException(s);
            }
            if (!cs[0].equals(HubEvent.class)) {
                throw new RuntimeException(s);
            }

            // 20160625
            OATriggerListener tl = new OATriggerMethodListener(clazz, method, bOnlyUseLoadedData);
            OATriggerDelegate.createTrigger(method.getName(), clazz, tl, props, bOnlyUseLoadedData, bServerSideOnly, bBackgroundThread, true);
        }        
    }

    // 20111027
    /**
     * Find the OAObject class that use contained by the Hub. 
     * @see OAObjectReflectDelegate#getHubObjectClass(Method)
     */
    public static Class getHubObjectClass(OAMany annotation, Method method) {
        Class cx = OAObjectReflectDelegate.getHubObjectClass(method);
        if (cx == null && annotation != null) {
            Class cz = annotation.toClass();
            if (cz != null && !cz.equals(Object.class)) {
                cx = cz;
            }
        }
        return cx;
    }
    
    public static void update(Database database, Class[] classes) throws Exception {
        if (classes == null) return;
        for (Class c : classes) {
            _createColumns(database, c);
        }
        for (Class c : classes) {
            _updateTable(database, c);
        }
    }
    
    
    /**
     * Load/update Database using annotations
     */
    private static void _createColumns(Database database, Class clazz) throws Exception {
        Method[] methods = clazz.getDeclaredMethods();  // need to get all access types, since some could be private.  does not get superclass methods

        OATable dbTable = (OATable) clazz.getAnnotation(OATable.class);
        if (dbTable == null) throw new Exception("Annotation for Table not defined for this class");
        
        
        Table table = database.getTable(clazz);
        if (table == null) {
            String s = dbTable.name();
            if (s.length() == 0) s = clazz.getSimpleName();
            table = new Table(s, clazz);
            database.addTable(table);
        }

        // 1: create pkey and regular columns
        for (Method m : methods) {
            OAColumn dbcol = (OAColumn) m.getAnnotation(OAColumn.class);
            if (dbcol == null) continue;

            OAProperty oaprop = (OAProperty) m.getAnnotation(OAProperty.class);
            OAId oaid = (OAId) m.getAnnotation(OAId.class);
            
            String name = name = getPropertyName(m.getName());
            
            String colName = dbcol.name();  // will be "", if the property name should be used.
            if (colName == null || colName.length() == 0) {
                colName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }
            
            Column column = new Column(colName, name, dbcol.sqlType(), dbcol.maxLength());
            if (oaprop != null) column.decimalPlaces = oaprop.decimalPlaces();
            if (oaid != null) {
                column.primaryKey = true;
                column.guid = oaid.guid();
                column.assignNextNumber = oaid.autoAssign();
                // c.assignedByDatabase = 
            }
            if (oaprop != null) {
                column.unicode = oaprop.isUnicode();
            }
            column.fullTextIndex = dbcol.isFullTextIndex();             
            
            table.addColumn(column);
        }
    }

    
    private static void _updateTable(Database database, Class clazz) throws Exception {

        Method[] methods = clazz.getDeclaredMethods();  // need to get all access types, since some could be private. does not get superclass methods

        OATable dbTable = (OATable) clazz.getAnnotation(OATable.class);
        if (dbTable == null) throw new Exception("Annotation for Table not defined for this class"); 
        
        Table table = database.getTable(clazz);
        if (table == null) throw new Exception("Table for class="+clazz+" was not found");
        // 2: create fkey columns and links
        for (Method m : methods) {
            OAColumn dbcol = (OAColumn) m.getAnnotation(OAColumn.class);
            OAFkey dbfk = (OAFkey) m.getAnnotation(OAFkey.class);
            OAOne oaone = (OAOne) m.getAnnotation(OAOne.class);
            OAMany oamany = (OAMany) m.getAnnotation(OAMany.class);
            OALinkTable oalt = (OALinkTable) m.getAnnotation(OALinkTable.class);
            
            if (dbfk != null) {
                if (dbcol != null) throw new Exception("fkey column should not have a column annotation defined, method is "+m.getName());
                if (oaone == null) throw new Exception("method with fkey does not have a One annotation defined, method is "+m.getName());
                if (oamany != null) throw new Exception("method with fkey should not have a Many annotation defined, method is "+m.getName());
                
                Class returnClass = m.getReturnType();
                if (returnClass == null) throw new Exception("method with fkey does not have a return class type, method is "+m.getName());
                OATable toTable = (OATable) returnClass.getAnnotation(OATable.class);
                if (toTable == null) {
                    throw new Exception("class for fkey does not have a Table annotation defined, method is "+m.getName());
                }
                
                Table fkTable = database.getTable(returnClass);
                if (fkTable == null) {
                    fkTable = new Table(toTable.name(), returnClass);
                    database.addTable(fkTable);
                }
                
                // tables[COLORCODE].addLink("orders", tables[ORDER], "colorCode", new int[] {0});
                //   tables[WORKER].addLink("orderProductionAreas", tables[ORDERPRODUCTIONAREA], "worker", new int[] {0});

                String[] fkcols = dbfk.columns();
                int[] poss = new int[0];
                for (String sfk : fkcols) {
                    poss = OAArray.add(poss, table.getColumns().length);
                    Column c = new Column(sfk, true);
                    table.addColumn(c);
                }
                table.addLink( getPropertyName(m.getName()), fkTable, oaone.reverseName(), poss);
            }
            else if (oalt != null) {
                Table linkTable = database.getTable(oalt.name());
                if (linkTable == null) {
                    linkTable = new Table(oalt.name(), true);
                    database.addTable(linkTable);
                }
                // create columns for link table
                // create link for table to linkTable
                // create link for linktable to table
                int[] poss = new int[0];  // pos for pk columns in table
                int[] poss2 = new int[0];  // pos for fkey columsn in linkTable
                String[] indexColumns = new String[0];
                Column[] cols = table.getColumns();
                int j = 0;
                for (int i=0; i<cols.length;i++) {
                    if (!cols[i].primaryKey) continue;

                    poss = OAArray.add(poss, i);
                    poss2 = OAArray.add(poss2, linkTable.getColumns().length);
                    
                    if (j > oalt.columns().length) {
                        throw new Exception("mismatch between linktable fkey columns and pkey columns, more pkeys. method is "+m.getName());
                    }

                    Column c = new Column(oalt.columns()[j], "", cols[i].getSqlType(), cols[i].maxLength);
                    c.primaryKey = false; // no pkeys in linkTable, only indexes
                    linkTable.addColumn(c);
                    
                    indexColumns = (String[]) OAArray.add(String.class, indexColumns, oalt.columns()[j]);
                    j++;
                }
                if (j < oalt.columns().length) throw new Exception("mismatch between fkey columns and pkey columns, more fkeys. method is "+m.getName());
                
                if (oamany != null) table.addLink(getPropertyName(m.getName()), linkTable, oamany.reverseName(), poss);
                else table.addLink(getPropertyName(m.getName()), linkTable, oaone.reverseName(), poss);
                
                if (oamany != null) linkTable.addLink(oamany.reverseName(), table, getPropertyName(m.getName()), poss2);
                else linkTable.addLink(oaone.reverseName(), table, getPropertyName(m.getName()), poss2);
                
                String s = oalt.indexName();
                if (s != null) linkTable.addIndex(new Index(s, indexColumns));
            }
            else if (oaone != null) {
                if (oamany != null) throw new Exception("method with OAOne annotation should not have a OAMany annotation defined, method is "+m.getName());

                // link using pkey columns                
                int[] poss = new int[0];
                Column[] cols = table.getColumns();
                for (int i=0; i<cols.length;i++) {
                    if (cols[i].primaryKey) {
                        poss = OAArray.add(poss, i);
                        break;
                    }
                }
                table.addLink(getPropertyName(m.getName()), database.getTable(m.getReturnType()), oaone.reverseName(), poss);
            }
            else if (oamany != null) {
                Column[] cols = table.getColumns();
                int[] poss = new int[0];  // pos for pk columns in table
                for (int i=0; i<cols.length;i++) {
                    if (!cols[i].primaryKey) continue;
                    poss = OAArray.add(poss, i);
                }
                Class c = OAAnnotationDelegate.getHubObjectClass(oamany, m);
                table.addLink(getPropertyName(m.getName()), database.getTable(c), oamany.reverseName(), poss);
            }
        }
        
        // Indexes
        OAIndex[] indexes = dbTable.indexes();
        for (OAIndex ind : indexes) {
            String[] ss = new String[0];
            OAIndexColumn[] dbics = ind.columns();
            for (OAIndexColumn dbic : dbics) {
                ss = (String[]) OAArray.add(String.class, ss, dbic.name());
            }
            table.addIndex( new Index(ind.name(), ss, ind.fkey()) );
        }
    }

    
    public static void main(String[] args) throws Exception {
/**        
        OAAnnotationDelegate del = new OAAnnotationDelegate();
        
        DataSource ds = new DataSource("server", "database", "user", "pw");
        Database database = ((OADataSourceJOAC)ds.getOADataSource()).getDatabase();
        OAMetaData dbmd = ((OADataSourceJOAC)ds.getOADataSource()).getOAMetaData();

        String[] fnames = OAReflect.getClasses("com.viaoa.scheduler.oa");

        for (String fn : fnames) {
            System.out.println("oi&ds ==>"+fn);
            Class c = Class.forName("com.viaoa.scheduler.oa." + fn);
            
            if (c.getAnnotation(OATable.class) == null) continue;
            
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(c);
            del.verify(oi);
            del.verify(c, database);
        }

        
        // Create database
        database = new Database();

        Table table = new Table("NextNumber",com.viaoa.ds.autonumber.NextNumber.class); // ** Used by all OADataSource Database
        // NextNumber COLUMNS
        Column[] columns = new Column[2];
        columns[0] = new Column("nextNumberId","nextNumberId", Types.VARCHAR, 75);
        columns[0].primaryKey = true;
        columns[1] = new Column("nextNumber","nextNumber", Types.INTEGER);
        table.setColumns(columns);
        database.addTable(table);
        
        
        for (String fn : fnames) {
            System.out.println("create columns ==>"+fn);
            Class c = Class.forName("com.viaoa.scheduler.oa." + fn);
            if (c.getAnnotation(OATable.class) == null) continue;
            del.createColumns(database, c);
        }
        for (String fn : fnames) {
            System.out.println("update table ==>"+fn);
            Class c = Class.forName("com.viaoa.scheduler.oa." + fn);
            if (c.getAnnotation(OATable.class) == null) continue;
            del.updateTable(database, c); // fkeys, links, linktables
        }
        
        
        // Verify
        for (String fn : fnames) {
            System.out.println("verify OA ==>"+fn);
            Class c = Class.forName("com.viaoa.scheduler.oa." + fn);
            if (c.getAnnotation(OATable.class) == null) continue;
            del.verify(c, database);
        }
        
        System.out.println("verify database Links ==>");
        del.verifyLinks(database);
*/

        /* must have database access to run this
        System.out.println("datasource VerifyDelegate.verify database ==>");
        OADataSourceJOAC dsx = new OADataSourceJDBC(database, dbmd);
        VerifyDelegate.verify(dsx);
        */
        
        System.out.println("done");
    }

    public static String getPropertyName(String s) {
        return getPropertyName(s, true);
    }
    public static String getPropertyName(String s, boolean bToLower) {
        boolean b = true;
        if (s.startsWith("get")) {
            s = s.substring(3);
        }
        else if (s.startsWith("is")) {
            s = s.substring(2);
        }
        else if (s.startsWith("has")) {
            s = s.substring(3);
        }
        else if (s.startsWith("set")) {
            s = s.substring(3);
        }
        else {
            b = false;
        }
        if (bToLower && b && s.length() > 1) {
            s = Character.toLowerCase(s.charAt(0)) + s.substring(1);
        }
        return s;
    }
}
