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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.viaoa.annotation.OACalculatedProperty;
import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAProperty;
import com.viaoa.hub.Hub;
import com.viaoa.hub.CustomHubFilter;
import com.viaoa.hub.HubMerger;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;

/**
 * Utility used to parse a propertyPath, get methods, class information, and to be able to get 
 * the value by invoking on an object.
 * 
 * A PropertyPath String is separated by "." for each linkPropery, and each linkProperty
 * can have a filter in the format ":filterName(a,b,n)" 

 * Supports casting in property path, 
 *   ex: from Emp, "dept.(manger)employee.name"
 *   ex: from OALeftJoin "(Location)A.name"
 *
 * Supports filters:
 * ex:  "dept.employees:newHires(7).orders.orderItems:overDue(30)"
 * 
 * created 20120809
 * @param <T> type of object that the property path is based on.
 * @see HubMerger which uses propertyPaths to create a Hub of all lastNode objects, and keeps it updated.
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
    private String[] filterNames;
    private String[] filterParams;
    private Object[][] filterParamValues;

    private Class[] filterClasses; 
    private Constructor[] filterConstructors; 

    private OALinkInfo[] linkInfos;
    private OALinkInfo[] recursiveLinkInfos;  // for each linkInfos[]
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
    // ex: Employees:recentBirthday()  => "recentBirthday()" 
    public String[] getFilterNames() {
        return filterNames;
    }
    // params used for filter, ex: "(a,b)", "()"
    public String[] getFilterParams() {
        return filterParams;
    }
    public Object[][] getFilterParamValues() {
        return filterParamValues;
    }
    public Method[] getMethods() {
        return methods;
    }
    public Class[] getClasses() {
        return classes;
    }
    public Constructor[] getFilterConstructors() {
        return filterConstructors;
    }
    public OALinkInfo[] getLinkInfos() {
        return linkInfos;
    }
    public OALinkInfo[] getRecursiveLinkInfos() {
        return recursiveLinkInfos;
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
        if (propertyPath == null) propertyPath = "";
        else propertyPath = propertyPath.trim();

        
        // 20140118 if leading with "[ClassName].", then it is the fromClass
        int pos = propertyPath.indexOf("[");
        if (pos >= 0) {
            int pos2 = propertyPath.indexOf("].");
            if (pos2 > 0) {
                String fromClassName = propertyPath.substring(pos+1, pos2); 
                propertyPath = propertyPath.substring(pos2+2);
                
                String packageName = fromClass.getName();
                pos = packageName.lastIndexOf('.');
                if (pos > 0) {
                    packageName = packageName.substring(0, pos+1);
                }
                else packageName = "";
                
                Class c = Class.forName(packageName + fromClassName);
                fromClass = c;
            }
        }
        
        
    
        String propertyPathClean = propertyPath;
        // a String that uses quotes "" could have special chars ',:()' inside of "" it  
        //qqq todo:  this wont protect against \" - need to create a tokenizer     
        for (int i=0; ;i++) {
            int p = propertyPathClean.indexOf('\"');
            if (p < 0) break;
            int p2 = propertyPathClean.indexOf('\"', p+1);
            if (p2 < 0) break;
            int x = (p2 - p) - 1;
            String s = OAString.getRandomString(x, x, false, true, false);
            propertyPathClean = propertyPath.substring(0, p) + "\"" + s + "\"" + propertyPath.substring(p2+1);
        }
        
        Class classLast = clazz;
        int posDot, prevPosDot;
        posDot = prevPosDot = 0;
        for ( ; posDot >= 0; prevPosDot=posDot+1) {
            posDot = propertyPathClean.indexOf('.', prevPosDot);
            int posCast = propertyPathClean.indexOf('(', prevPosDot);
            int posFilter = propertyPathClean.indexOf(':', prevPosDot);

            if (posCast >= 0) {
                if (posFilter > 0 && posCast > posFilter) posCast = -1;
                else if (posDot >= 0) {  
                    if (posCast > posDot) posCast = -1;
                    else {
                        // cast could have package name, with '.' in it
                        posDot = propertyPathClean.indexOf(')', posCast+1);
                        posDot = propertyPathClean.indexOf('.', posDot);
                    }
                }
            }
            
            if (posDot >= 0 && posFilter > posDot) posFilter = -1;

    
            String propertyName;
            String propertyNameClean;
            
            if (posDot >= 0) {
                propertyName = propertyPath.substring(prevPosDot, posDot);
                propertyNameClean = propertyPathClean.substring(prevPosDot, posDot);
            }
            else {
                propertyName = propertyPath.substring(prevPosDot);
                propertyNameClean = propertyPathClean.substring(prevPosDot);
            }

            String castName = null;
            if (posCast >= 0) {
                int p = propertyNameClean.indexOf('(');
                if (p >= 0) {
                    int p2 = propertyNameClean.indexOf(')', p);
                    if (p2 > 0) {
                        castName = propertyName.substring(p+1, p2).trim();
                        propertyName = propertyName.substring(p2+1).trim();
                        propertyNameClean = propertyNameClean.substring(p2+1).trim();
                    }
                }
            }
            this.castNames = (String[]) OAArray.add(String.class, this.castNames, castName);

             
            String filterName = null;
            String filterNameClean = null;
            String filterParam = null;
            String filterParamClean = null;
            Constructor filterConstructor = null;
            if (posFilter >= 0) {
                posFilter = propertyNameClean.indexOf(':');
                filterName = propertyName.substring(posFilter+1).trim();
                filterNameClean = propertyNameClean.substring(posFilter+1).trim();
                
                propertyName = propertyNameClean = propertyName.substring(0, posFilter).trim();
                int p = filterNameClean.indexOf('(');
                
                if (p >= 0) {
                    filterParam = filterName.substring(p).trim();
                    filterParamClean = filterNameClean.substring(p).trim();
                    filterName = filterNameClean = filterName.substring(0, p).trim();
                }
            }
            this.filterNames = (String[]) OAArray.add(String.class, this.filterNames, filterName);
            this.filterParams = (String[]) OAArray.add(String.class, this.filterParams, filterParam); // ex: "(a,b)", "()"


            // figure out params
            int paramCount = 0;
            if (filterParam != null) {
                if (filterParam.charAt(0) == '(') {
                    filterParam = filterParam.substring(1);
                    filterParamClean = filterParamClean.substring(1);
                    if (filterParam.charAt(filterParam.length()-1) == ')') {
                        filterParam = filterParam.substring(0, filterParam.length()-1);
                        filterParamClean = filterParamClean.substring(0, filterParamClean.length()-1);
                    }
                }
                paramCount = OAString.dcount(filterParamClean, ",");
            }

            
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
            OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, propertyName);
            if (li != null) {
                linkInfos = (OALinkInfo[]) OAArray.add(OALinkInfo.class, linkInfos, li);
                bLastProperyLinkInfo = true;
            }
            else {
                bLastProperyLinkInfo = false;
            }
    
            String mname;
            if (propertyName.length() == 0) propertyName = mname = "toString";
            else {
                mname = "get"+propertyName;
            }
            this.properties = (String[]) OAArray.add(String.class, this.properties, propertyName);

            Method method = OAReflect.getMethod(clazz, mname, 0);
            bLastMethodHasHubParam = false;
            if (method == null) {
                if (posDot < 0) {
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
                    mname = "is"+propertyName;
                    method = OAReflect.getMethod(clazz, mname, 0);
                    if (method == null) {
                        throw new Exception("OAReflect.setup() cant find method. class="+(clazz==null?"null":clazz.getName())+" prop="+propertyName+" path="+propertyPath);
                    }
                }
            }
            this.methods = (Method[]) OAArray.add(Method.class, this.methods, method);
    
            clazz = method.getReturnType();
            if (clazz.equals(Hub.class)) {
                // try to find the ObjectClass for Hub
                Class c = OAObjectInfoDelegate.getHubPropertyClass(classLast, propertyName);
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
                            int p = s.lastIndexOf('.');
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
            
            // finish with Filter info
            Class filterClass = null;
            if (filterName != null) {
                String filterClassName;
                if (filterName.indexOf('.') >= 0) {
                    filterClassName = filterName;
                }
                else {
                    String s = clazz.getName();
                    int p = s.lastIndexOf('.');
                    filterClassName = s.substring(p+1) + filterName + "Filter";
                    
                    // check annotations for correct upper/lower case
                    OAClass oac = (OAClass) clazz.getAnnotation(OAClass.class);
                    if (oac != null) {
                        Class[] cs = oac.filterClasses();
                        for (Class c : cs) {
                            if (!CustomHubFilter.class.isAssignableFrom(c)) continue;
                            int px = c.getName().toUpperCase().indexOf("."+filterClassName.toUpperCase());
                            if (px >= 0) {
                                if ( (px + filterClassName.length() + 1) == c.getName().length()) {
                                    filterClass = c;
                                    filterClassName = c.getName();
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (filterClass == null) {
                        if (p >= 0) {
                            s = s.substring(0, p+1) + "filter.";
                        }
                        else s = "";
                        filterClassName = s + filterName;
                    }
                }
                if (filterClass == null) {
                    try {
                        filterClass = Class.forName(filterClassName);
                        // note: filterClass does not have to exist, as some tools will allow
                        //    creating custom ones.  ex: OAFinder has a method that is called to create the filter
                    }
                    catch (Exception e) {}
                }
                if (filterClass != null && !CustomHubFilter.class.isAssignableFrom(filterClass)) {
                    throw new RuntimeException("Filter must implement interface CustomHubFilter");
                }
            }
            this.filterClasses = (Class[]) OAArray.add(Class.class, this.filterClasses, filterClass);
            
            Object[] filterParamValue = null;
            if (filterClass != null && paramCount > 0) {
                for (Constructor con : filterClass.getConstructors()) {
                    Class[] cs = con.getParameterTypes();
                    if (cs.length != paramCount + 2) continue;
                    if (!cs[0].equals(Hub.class)) continue;
                    filterConstructor = con;

                    filterParamValue = new Object[paramCount];
                    int p = 0;
                    int prev = 0;
                    for (int i=0 ; p>=0; i++,prev=p+1) {
                        p = filterParamClean.indexOf(',', prev);
                        String s;
                        if (p < 0) s =  filterParam.substring(prev).trim();
                        else s = filterParam.substring(prev, p).trim();

                        // remove double quotes
                        int x = s.length();
                        if (x > 0 && s.charAt(0) == '\"' && s.charAt(x-1) == '\"') {
                            if (x < 3) s = "";
                            else s = s.substring(1, x-2);
                        }
                        if (s.equals("?")) {
                            // needs to be an inputValue
                            filterParamValue[i] = "?";
                        }
                        else filterParamValue[i] = OAConv.convert(cs[i+2], s);
                    }
                    break;
                }
            }
            else {
                if (filterClass != null) {
                    filterConstructor = filterClass.getConstructor(new Class[] {Hub.class, Hub.class});
                }
            }
            if (filterClass != null && filterConstructor == null) {
                //throw new RuntimeException("Could not find constructor for Filter, name="+filterName);
            }
            this.filterConstructors = (Constructor[]) OAArray.add(Constructor.class, this.filterConstructors, filterConstructor);
            
            if (this.filterParamValues == null) this.filterParamValues = new Object[1][];
            else {
                Object[][] objs = new Object[filterParamValues.length+1][];
                System.arraycopy(filterParamValues, 0, objs, 0, filterParamValues.length);
                filterParamValues = objs;
            }
            filterParamValues[filterParamValues.length-1] = filterParamValue;
        }
        
        // 20140118 update recursiveMethods
        if (linkInfos != null) {
            recursiveLinkInfos = new OALinkInfo[linkInfos.length];
            int j = 0;
            for (OALinkInfo li : linkInfos) {
                j++;
                if (li == null || !li.getRecursive()) continue;
                OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(li.getToClass());
                OALinkInfo lix = OAObjectInfoDelegate.getRecursiveLinkInfo(oi, OALinkInfo.MANY);
                recursiveLinkInfos[j-1] = lix;
            }
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
