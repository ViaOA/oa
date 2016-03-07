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
import com.viaoa.object.OAFinder;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;

/**
 * Utility used to parse a propertyPath, get methods, class information, and to be able to get 
 * the value by invoking on an object.
 * 
 * A PropertyPath String is separated by "." for each linkPropery, and each linkProperty
 * can have a filter in the format ":filterName(a,b,n)" 

 * Supports casting in property path, 
 *   ex: from Emp, "dept.(manager)employee.name"
 *   ex: from OALeftJoin "(Location)A.name"
 *
 * Supports filters:
 * ex:  "dept.employees:newHires(7).orders.orderItems:overDue(30)"
 * 
 * created 20120809
 * @param <T> type of object that the property path is based on.
 * @see HubMerger which uses propertyPaths to create a Hub of all lastNode objects, and keeps it updated.
 * @see OAPropertyPathDelegate
 */
public class OAPropertyPath<T> {

    private Class<T> fromClass;
    private String propertyPath;
    private Method[] methods = new Method[0];
    private boolean bLastMethodHasHubParam; // true if method requires a Hub param
    
    /**
     *  property class.  
     *  if casting is used, then this will have the casted class. 
     *  note: if the method returns a Hub, then this will be the hub.objectClass
     */
    private Class[] classes = new Class[0]; 

    // flag that is used when data is needed to find the real classes, since generics are being used.
    private boolean bNeedsDataToVerify;
    private String[] properties = new String[0]; // convert properties, without casting
    private String[] castNames = new String[0];
    private String[] filterNames = new String[0];
    private String[] filterParams = new String[0];
    private Object[][] filterParamValues = new Object[0][];

    private Class[] filterClasses = new Class[0]; 
    private Constructor[] filterConstructors = new Constructor[0]; 

    private OALinkInfo[] linkInfos = new OALinkInfo[0];
    private OALinkInfo[] recursiveLinkInfos = new OALinkInfo[0];  // for each linkInfos[]
    private boolean bLastProperyLinkInfo;
    private OAPropertyPath revPropertyPath; 

    
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
        if (linkInfos == null || linkInfos.length == 0) return null;

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

    public OAProperty getOAPropertyAnnotation() {
        if (methods == null || methods.length == 0) return null;
        return methods[methods.length -1].getAnnotation(OAProperty.class);
    }
    public OACalculatedProperty getOACalculatedPropertyAnnotation() {
        if (methods == null || methods.length == 0) return null;
        return methods[methods.length -1].getAnnotation(OACalculatedProperty.class);
    }

    public boolean isLastPropertyLinkInfo() {
        return bLastProperyLinkInfo;
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

    public Object getValue(T fromObject) {
        return getValue(null, fromObject);
    }
    public String getValueAsString(T fromObject) {
        return getValueAsString(null, fromObject);
    }
    
    /**
     * Returns the value of the propertyPath from a base object.
     * Notes: if any of the property's is null, then null is returned.
     * If any of the non-last properties is a Hub, then the AO will be used.
     */
    public Object getValue(Hub<T> hub, T fromObject) {
        if (fromObject == null) return null;
        if (this.fromClass == null) {
            setup( (Class<T>)fromObject.getClass());
        }
        if (methods == null || methods.length == 0) return 0;
        
        Object result = fromObject;
        for (int i=0; i < methods.length; i++) {
            
            if (bLastMethodHasHubParam && i+1 == methods.length) {
                try {
                    result = methods[i].invoke(result, hub);
                }
                catch (Exception e) {
                    throw new RuntimeException("error invoking method="+methods[i], e);
                }
            }
            else {
                try {
                    result = methods[i].invoke(result);
                }
                catch (Exception e) {
                    throw new RuntimeException("error invoking method="+methods[i], e);
                }
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
    public String getValueAsString(Hub<T> hub, T fromObject) {
        Object obj = getValue(hub, fromObject);
        String s = OAConv.toString(obj, getFormat());
        return s;
    }
    public String getValueAsString(Hub<T> hub, T fromObject, String format) {
        Object obj = getValue(hub, fromObject);
        String s = OAConv.toString(obj, format);
        return s;
    }
    
    public Class<T> getFromClass() {
        return fromClass;
    }
    
    public void setup(Hub hub) {
        if (hub == null) return;
        setup(hub, hub.getObjectClass(), false);
    }
    
    public void setup(Class clazz) {
        setup(clazz, false);
    }

    /**
     * @param clazz
     * @param bIgnorePrivateLink if true, then a link that does not have a get method will not throw an exception.  Used by HubGroupBy
     */
    public void setup(Class clazz, boolean bIgnorePrivateLink) {
        String s = setup(null, clazz, bIgnorePrivateLink);
        if (s != null) throw new RuntimeException(s);
    }
    
    public boolean hasPrivateLink() {
        if (linkInfos == null) return false;
        for (OALinkInfo li : linkInfos) {
            if (li.getPrivateMethod()) return true;
        }
        return false;
    }
    
    
    public boolean getNeedsDataToVerify() {
        return bNeedsDataToVerify;
    }
    public String setup(final Hub hub, Class clazz, final boolean bIgnorePrivateLink) {
        bNeedsDataToVerify = false;
        if (clazz == null) return null;
        this.fromClass = clazz;
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
                
                if (fromClassName.indexOf('.') >= 0) {
                    Class c;
                    try {
                        c = Class.forName(fromClassName);
                    }
                    catch (Exception e) {
                        throw new RuntimeException("error getting class="+fromClassName, e);
                    }
                    this.fromClass = c;
                }
                else {
                    String packageName = fromClass.getName();
                    pos = packageName.lastIndexOf('.');
                    if (pos > 0) {
                        packageName = packageName.substring(0, pos+1);
                    }
                    else packageName = "";
                    Class c;
                    try {
                        c = Class.forName(packageName + fromClassName);
                    }
                    catch (Exception e) {
                        throw new RuntimeException("error getting class="+packageName+fromClassName, e);
                    }
                    this.fromClass = c;
                }
                
            }
        }
        clazz = this.fromClass;
    
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
        
        if (OAString.isEmpty(propertyPathClean)) posDot = -1;
        int cnter = 0;
        for ( ; posDot >= 0; prevPosDot=posDot+1) {
            cnter++;
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
            if (propertyName.length() == 0) {
                propertyName = mname = "toString";
            }
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
                        if (bIgnorePrivateLink && li != null && li.getPrivateMethod()) {
                            // wait to show error
                        }
                        else {
                            return "OAReflect.setup() cant find method. class="+(clazz==null?"null":clazz.getName())+" prop="+propertyName+" path="+propertyPath;
                        }
                    }
                }
            }
            this.methods = (Method[]) OAArray.add(Method.class, this.methods, method);
    
            if (method == null) clazz = li.getToClass();
            else clazz = method.getReturnType();
            
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
                    
                    try {
                        clazz = Class.forName(cn);
                    }
                    catch (Exception e) {
                        throw new RuntimeException("error getting castName class="+cn, e);
                    }
                    
                }
                else if (cnter == 1 && clazz.equals(OAObject.class) && hub != null) {  // 20150712
                    // see if there is an object to check with
                    for (Object o : hub) {
                        if (o instanceof OAObject) {
                            Object x = ((OAObject)o).getProperty(propertyName);
                            if (x != null) {
                                clazz = x.getClass();
                                break;
                            }
                        }
                    }
                }
            }
            
            // 20150715 
            if (clazz.equals(OAObject.class) && hub != null) { 
                // see if it can be found using data from hub
                Class c = findLastClass(hub);
                if (c == null || c.equals(OAObject.class)) {
                    bNeedsDataToVerify = true;
                }
                else clazz = c;
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
                    return "Filter must implement interface CustomHubFilter";
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
                    try {
                        filterConstructor = filterClass.getConstructor(new Class[] {Hub.class, Hub.class});
                    }
                    catch (Exception e) {
                        throw new RuntimeException("error getting filter constructor", e);
                    }
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
        return null;
    }

    
    // 20150715 find the class by looking at the data    
    private Class findLastClass(final Object obj) {
        return _findLastClass(obj, 0);
    }
    private Class _findLastClass(final Object obj, final int pos) {
        if (this.properties == null || pos >= this.properties.length) return null;
        Class clazz;
        if (obj instanceof Hub) clazz = _findLastClass((Hub)obj, pos);
        else if (obj instanceof Object) clazz = _findLastClass((Hub)obj, pos);
        else clazz = null;
        return clazz;
    }
    
    private Class _findLastClass(final Hub hubRoot, final int pos) {
        if (hubRoot == null) return null;
        
        if (this.properties == null || pos >= this.properties.length) { 
            // this is last prop
            return hubRoot.getObjectClass();
        }
        
        Class clazz = null;
        for (Object obj : hubRoot) {
            if (!(obj instanceof OAObject)) break;
            clazz = _findLastClass(obj, pos+1);
            if (clazz != null) break;
        }
        return clazz;
    }
    private Class _findLastClass(final OAObject obj, final int pos) {
        if (obj == null) return null;
        
        if (this.properties == null || pos >= this.properties.length) { 
            // this is last prop
            return obj.getClass();
        }

        Class clazz = null;
        Object objValue = ((OAObject)obj).getProperty(this.properties[pos]);
        if (objValue != null) {
            clazz = _findLastClass(objValue, pos+1);
        }
        return clazz;
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





