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
package com.viaoa.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.viaoa.annotation.OACalculatedProperty;
import com.viaoa.annotation.OAProperty;
import com.viaoa.hub.Hub;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;

/**
 * Utility used to parse a propertyPath, get methods, class information, and to be able to get 
 * the value by invoking on an object.
 * 
 * Supports casting in property path, 
 *   ex: from Emp, "dept.(manger)employee.name"
 *   ex: from OALeftJoin "(Location)A.name"
 * 
 * created 20120809
 * @param <T> type of object that the property path is based on.
 */
public class OAPropertyPath<T> {

    private Class<T> fromClass;
    private String propertyPath;
    private Method[] methods;
    private boolean bLastMethodHasHubParam; // true if method requires a Hub param
    
    /**
     *  property class.  
     *  if casting is used, then this will have the casted class. 
     *  note: if the method returns a Hub, then this will be the hub.objectClass
     */
    private Class[] classes; 
    
    private String[] properties; // convert properties, without casting
    private String[] castNames;

    private OALinkInfo[] linkInfos;
    private boolean bLastProperyLinkInfo;
    private OAPropertyPath revPropertyPath; 

    public OAProperty getOAPropertyAnnotation() {
        if (methods == null || methods.length == 0) return null;
        return methods[methods.length -1].getAnnotation(OAProperty.class);
    }
    public OACalculatedProperty getOACalculatedPropertyAnnotation() {
        if (methods == null || methods.length == 0) return null;
        return methods[methods.length -1].getAnnotation(OACalculatedProperty.class);
    }
    
    public OAPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
    }
    public OAPropertyPath(Class<T> fromClass, String propertyPath) {
        this.propertyPath = propertyPath;
        this.fromClass = fromClass;
        try {
            setup(fromClass);
        }
        catch (Exception e) {
            try {
//setup(fromClass);  // for debugging
            }
            catch (Exception e2) {
                // TODO: handle exception
            }
            throw new IllegalArgumentException("cant setup property path= "+propertyPath+", fromClass="+fromClass, e);
        }
    }
 
    public String getPropertyPath() {
        return this.propertyPath;
    }
    
    public OAPropertyPath getReversePropertyPath() {
        if (revPropertyPath != null) return revPropertyPath;
        if (linkInfos == null) return null;

        Class c = null;
        String pp = "";
        for (int i=0; i<linkInfos.length; i++) {
            OALinkInfo li = linkInfos[i];
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (pp.length() > 0) pp = "." + pp;
            pp = liRev.getName() + pp;
        }
        c = linkInfos[linkInfos.length-1].getToClass();
        
        revPropertyPath = new OAPropertyPath(c, pp); 
        return revPropertyPath;
    }
   
    public String[] getProperties() {
        return properties;
    }
    public String[] getCastNames() {
        return castNames;
    }
    public Method[] getMethods() {
        return methods;
    }
    public Class[] getClasses() {
        return classes;
    }
    /**
     * Returns the value of the propertyPath from a base object.
     * Notes: if any of the property's is null, then null is returned.
     * If any of the non-last properties is a Hub, then the AO will be used.
     */
    public Object getValue(Hub<T> hub, T fromObject) throws Exception {
        if (fromObject == null) return null;
        if (this.fromClass == null) {
            setup( (Class<T>)fromObject.getClass());
        }
        if (methods == null || methods.length == 0) return 0;
        
        Object result = fromObject;
        for (int i=0; i < methods.length; i++) {
            
            if (bLastMethodHasHubParam && i+1 == methods.length) {
                result = methods[i].invoke(result, hub);
            }
            else {
                result = methods[i].invoke(result);
            }
            
            if (result == null) break;
            if (i+1 < methods.length && result instanceof Hub) {
                result = ((Hub) result).getAO();
                if (result == null) break;
            }
        }
        return result;
    }
    /**
     * This will call getValue, and then call OAConv.toString using getFormat. 
     */
    public String getValueAsString(Hub<T> hub, T fromObject) throws Exception {
        Object obj = getValue(hub, fromObject);
        String s = OAConv.toString(obj, getFormat());
        return s;
    }
    public String getValueAsString(Hub<T> hub, T fromObject, String format) throws Exception {
        Object obj = getValue(hub, fromObject);
        String s = OAConv.toString(obj, format);
        return s;
    }
    
    public Class<T> getFromClass() {
        return fromClass;
    }
    
    public void setup(Class<T> fromClass) throws Exception {
        if (fromClass == null) return;
        this.fromClass = fromClass;
        Class clazz = this.fromClass;
        String propertyPath = this.propertyPath;
        
        int pos,prev;
        if (propertyPath == null) propertyPath = "";
    
        
        Class classLast = clazz;
        for (pos=prev=0; pos >= 0; prev=pos+1) {
            // check for casting
            int posx = propertyPath.indexOf('(', prev);
            pos = propertyPath.indexOf('.', prev);

            if (posx >= 0 && posx < pos) {
                pos = propertyPath.indexOf(')', posx);
                pos = propertyPath.indexOf('.', pos);
            }
            else {
                pos = propertyPath.indexOf('.', prev);
            }
    
            String name;
            if (pos >= 0) name = propertyPath.substring(prev,pos);
            else name = propertyPath.substring(prev);
    
            String castName = null;
            int p = name.indexOf('(');
            if (p >= 0) {
                int p2 = name.indexOf(')', p);
                if (p2 > 0) {
                    castName = name.substring(p+1, p2).trim();
                    if (p2+1 == name.length()) name = "";
                    else name = name.substring(p2+1).trim();
                }
            }
            this.castNames = (String[]) OAArray.add(String.class, this.castNames, castName);

            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
            OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, name);
            if (li != null) {
                linkInfos = (OALinkInfo[]) OAArray.add(OALinkInfo.class, linkInfos, li);
            }
            else {
                bLastProperyLinkInfo = false;
            }
    
            String mname;
            if (name.length() == 0) name = mname = "toString";
            else {
                mname = "get"+name;
            }
            this.properties = (String[]) OAArray.add(String.class, this.properties, name);

            Method method = OAReflect.getMethod(clazz, mname, 0);
            bLastMethodHasHubParam = false;
            if (method == null) {
                if (pos < 0) {
                    // 20131029 see if it is for hubCalc, which is a static method that has a Hub param
                    //    must be the last property
                    method = OAReflect.getMethod(clazz, mname, 1);
                    if (method != null && Modifier.isStatic(method.getModifiers())) {
                        if (Hub.class.equals(method.getParameterTypes()[0])) {
                            bLastMethodHasHubParam = true;
                        }
                        else method = null;
                    }
                    else method = null;
                }
                
                if (method == null) {
                    mname = "is"+name;
                    method = OAReflect.getMethod(clazz, mname, 0);
                    if (method == null) {
                        throw new Exception("OAReflect.setup() cant find method. class="+(clazz==null?"null":clazz.getName())+" prop="+name+" path="+propertyPath);
                    }
                }
            }
            this.methods = (Method[]) OAArray.add(Method.class, this.methods, method);
    
            clazz = method.getReturnType();

            if (clazz.equals(Hub.class)) {
                // try to find the ObjectClass for Hub
                Class c = OAObjectInfoDelegate.getHubPropertyClass(classLast, name);
                if (c != null) {
                    clazz = c;
                }
            }
            else {
                if (castName != null) {
                    String cn;
                    if (castName.indexOf('.') >= 0) {
                        cn = castName;
                    }
                    else {
                        if (clazz != null) {
                            String s = clazz.getName();
                            p = s.lastIndexOf('.');
                            if (p >= 0) s = s.substring(0, p+1);
                            else s = "";
                            cn = s + castName;
                        }
                        else cn = castName;
                    }
                    clazz = Class.forName(cn);
                }
            }
            this.classes = (Class[]) OAArray.add(Class.class, this.classes, clazz);
            classLast = clazz;
        }
    }
 
    private boolean bFormat;
    private String format;
    public String getFormat() {
        if (format != null || bFormat) return format;
        bFormat = true;
        Class[] cs = getClasses();
        if (cs == null || cs.length == 0) return null;
        
        Class c = cs[cs.length-1];

        OAProperty op = getOAPropertyAnnotation();
        if (op != null) {
            format = op.outputFormat();
            if (!OAString.isEmpty(format)) return format;

            int deci = op.decimalPlaces();
            if (OAReflect.isFloat(c)) {
                if (op.isCurrency()) {
                    format = OAConv.getCurrencyFormat();
                    format = getDecimalFormat(format, deci);
                }
                else {
                    format = getDecimalFormat(deci);
                }
                return format;
            }                        
            if (OAReflect.isInteger(c)) {
                if (op.isCurrency()) {
                    String format = OAConv.getCurrencyFormat();
                    if (deci < 0) deci = 0;
                    format = getDecimalFormat(format, deci);
                }                
                else {
                    format = OAConv.getIntegerFormat();
                    format = getDecimalFormat(format, deci);
                }
                return format;
            }
        }
            
        OACalculatedProperty cp = getOACalculatedPropertyAnnotation();
        if (cp != null) {
            format = cp.outputFormat();
            if (!OAString.isEmpty(format)) return format;

            if (OAReflect.isFloat(c)) {
                if (cp.isCurrency()) {
                    format = OAConv.getCurrencyFormat();
                    format = getDecimalFormat(format, cp.decimalPlaces());
                }
                else {
                    format = getDecimalFormat(cp.decimalPlaces());
                }
                return format;
            }                        
            if (OAReflect.isInteger(c)) {
                if (cp.isCurrency()) {
                    String format = OAConv.getCurrencyFormat();
                    int  x = cp.decimalPlaces();
                    if (x < 0) x = 0;
                    format = getDecimalFormat(format, x);
                }                
                else {
                    format = OAConv.getIntegerFormat();
                    format = getDecimalFormat(format, cp.decimalPlaces());
                }
                return format;
            }
        }
        
        if (OAReflect.isFloat(c)) {
            format = OAConv.getDecimalFormat();
        }
        else format = OAConverter.getFormat(c);
        return format;
    }

    private String getDecimalFormat(int deci) {
        String format = OAConv.getDecimalFormat();
        return getDecimalFormat(format, deci);
    }
    
    private String getDecimalFormat(String format, int deci) {
        if (format == null) format = "";
        if (deci < 0) return format;
        
        // DecimalFormat     = #,##0.00
        int pos = format.indexOf('.');
        if (pos < 0) {
            if (deci != 0) {
                format += ".";
                for (int i=0; i<deci; i++) format += "0";
            }
        }
        else {
            int current = (format.length() - pos) - 1;
            int diff = deci - current;
            if (diff < 0) {
                format = format.substring(0, pos+deci+1);
            }
            else if (diff > 0) {
                for (int i=0; i<diff; i++) format += "0";
            }
        }
        return format;
        
    }
    
}
