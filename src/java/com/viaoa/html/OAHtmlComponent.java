/* 
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
package com.viaoa.html;

import java.util.*;
import java.lang.reflect.*;
import java.io.*;

import com.viaoa.hub.*;
import com.viaoa.util.*;

public abstract class OAHtmlComponent implements Serializable {
    private static final long serialVersionUID = 1L;
    protected Object object;
    protected OAForm form; // set by OAForm when component is added to form
    protected String name; // set by OAForm when component is added to form
    
    Hub hub;  // hub assigned
    Hub actualHub;  // hub that is closest to property
    String propertyName;
    String propertyPath;
    Method getMethod, setMethod;  // from ActualHub and propertyName
    Method[] getMethods;  // from propertyPath
    protected Class subClass; // subclass to use instead of Hub.getObjectClass()
    protected boolean bEnabled = true;
    protected boolean bVisible = true;
    protected String htmlBefore, htmlBetween, htmlAfter;
    protected String format;

    protected void handleException(Exception e, String methodName) {
        System.out.println(" ");
        System.out.println("===> OA Exception =========================================== start ");
        String s = getClass().getName();
        int pos = s.lastIndexOf(".");
        if (pos > 0) s = s.substring(pos+1);
        System.out.println("==> Class: "+s);
        System.out.println("==> Property: "+propertyPath);
        System.out.println("==> Method: "+methodName);
        System.out.println("==> Exception: "+e.toString());
        System.out.println("==> Stack Trace: ");
        if (form != null) {
            s = "Internal Error Class:"+s+" Property:"+propertyPath+" Method:"+methodName+" Exception:"+e.toString();
            form.addError(s);
            form.addHiddenMessage(s);
        }
        e.printStackTrace();
        System.out.println("============================================================= end");
        System.out.println(" ");
    }
    
    public void setObject(Object obj) {
        this.object = obj;
    }

    public void setHub(Hub newHub) {
        this.hub = newHub;
        resetHubOrProperty();
    }
    public Hub getHub() {
        return hub;
    }

    public boolean getEnabled() {
        return bEnabled;
    }
    /** if false then the disabled image will be used and inputs will be ignored.*/
    public void setEnabled(boolean b) {
        bEnabled = b;
    }

    public boolean getVisible() {
        return bVisible;
    }
    /** if false then dont display.*/
    public void setVisible(boolean b) {
        bVisible = b;
    }
    
    /**  @return true if this component has changed since it was last displayed. */
    public boolean needsRefreshed() {
        return false;
    }

    /**  @return true if this component wants top of page reset back to 0. */
    public boolean resetTop() {
        return false;
    }

    /** this is the class to use instead of Hub.getObjectClass().
        This is used to find the get/set methods.
    */
    public void setSubClass(Class subClass) {
        this.subClass = subClass;
        resetHubOrProperty();
    }
    public Class getSubClass(){
        return subClass;
    }

    /** determines if the class used by this component is the same as or a superclass to clazz. */
    protected boolean isCorrectClass(Class clazz) {
         Class c = subClass;
         if (c == null) {
            if (actualHub == null) return false;
            c = actualHub.getObjectClass();
         }
         
         if (c.isAssignableFrom(clazz)) return true;
         return false;
    }

    public void setPropertyPath(String s) {
        if (propertyPath == null || !s.equals(propertyPath)) {
            propertyPath = s;
            resetHubOrProperty();
        }
    }
    public String getPropertyPath() {
        return propertyPath;
    }

    /** @returns Script (javascript) needed for initialization. */
    public String getInitScript() {
        return null;
    }

    protected void resetHubOrProperty() {
        close();
        if (propertyPath != null) {
            if (hub == null) {
                if (object != null) hub = HubTemp.createHub(object);
                else return;
            }
            
            int pos = propertyPath.lastIndexOf('.');
            if (pos < 0) {
                actualHub = hub;
                propertyName = propertyPath;
            }
            else {
                actualHub = HubDetailDelegate.getDetailHub(hub, propertyPath.substring(0,pos));
                propertyName = propertyPath.substring(pos+1);
            }
        }
    }
    // from Hub and PropertyPath
    public Method[] getGetMethods() {
        if (getMethods == null) {
            getMethods = ClassModifier.getMethods((subClass!=null)?subClass:hub.getObjectClass(), propertyPath);
        }
        return getMethods;
    }

    // from ActualHub and PropertyName
    public Method getGetMethod() {
        if (getMethod == null && actualHub != null) {
            String methodName;
            if (propertyName != null && propertyName.length() > 0) methodName = "get" + propertyName;
            else methodName = "toString";
            getMethod = ClassModifier.getMethod(actualHub.getObjectClass(), methodName );
            if (getMethod == null) {
                throw new RuntimeException("method="+methodName + " class="+actualHub.getObjectClass());
            }
        }
        return getMethod;
    }
    public Method getSetMethod() {
        if (setMethod == null) {
            String methodName;
            if (propertyName != null && propertyName.length() > 0) {
                methodName = "set" + propertyName;
                setMethod = ClassModifier.getMethod(actualHub.getObjectClass(), methodName );
            }
        }
        return setMethod;
    }
    public void close() {
        if (object != null) HubTemp.deleteHub(hub);
        if (actualHub != null) {
            if (actualHub != null && hub != actualHub) {
                HubDetailDelegate.removeDetailHub(hub, actualHub);
            }
        }
        getMethod = null;
        getMethods = null;
        setMethod = null;
        propertyName = "";
        actualHub = null;
    }
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
    
    
    
    protected void beforeSetValuesInternal() {
        // called by OAForm
    }
    protected void setValuesInternal(String nameUsed, String[] values) { // called by OAForm
    }

    protected void afterSetValuesInternal() {
        // called by OAForm
    }
    public boolean isChanged() {
        return false;
    }

    public void reset() {
    }
    /** when user wants to save changes from form to object */

    public void update() {
    }

    /** name given to component when it was added to form. */
    public String getFormName() {
        return name;
    }
    
    /** this should not be called, it is set when the component is added to OAForm */
    public void setFormName() {
        this.name = name;
    }

    /** name used on form.  Default is to use same value as getFormName() */
    public String getName() {
        return name;
    }

    /** called by OAForm.processRequest() */
    protected String processCommand(OASession session, OAForm form, String command) {
        // url to go to or null
        return null;
    }

    public void setForm(OAForm form) {
        this.form = form;
    }
    public OAForm getForm() {
        return this.form;
    }
    
    public String getHtml() {
        if (!getVisible()) return "";
        return this.getHtml("");
    }
    /** @param htmlTags extra tags to insert into html output.  These tags will come before
        any other generated tag.
    */
    public String getHtml(String htmlTags) {
        if (!getVisible()) return "";
        return "getHtml() not implemented for " + getClass().getName();
    }
    
    /** HTML code/tags to insert at the "inside" when calling getHtml() */
    public void setHtmlBetween(String s) {
        htmlBetween = s;
    }
    public String getHtmlBetween() {
        return htmlBetween;
    }

    public void setHtml(String sBefore, String sBetween, String sAfter) {
        setHtmlBefore(sBefore);
        setHtmlBetween(sBetween);
        setHtmlAfter(sAfter);
    }

    /** HTML code/tags to insert before generated tags when calling getHtml() */
    public void setHtmlBefore(String s) {
        htmlBefore = s;
    }
    public String getHtmlBefore() {
        return htmlBefore;
    }

    /** HTML code/tags to insert after generated tags when calling getHtml() */
    public void setHtmlAfter(String s) {
        htmlAfter = s;
    }
    public String getHtmlAfter() {
        return htmlAfter;
    }
    
    public String getFormat() {
        if (format == null) {
            return OAConverter.getFormat( ClassModifier.getClass(getGetMethod()) );
        }
        return format;
    }
    protected String getFormat(Method method) {
        if (format == null) {
            return OAConverter.getFormat( ClassModifier.getClass(method) );
        }
        return format;
    }

    /** display format for property.  Currently used to format Date, Times and Numbers.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
    public void setFormat(String fmt) {
        format = fmt;
    }

}


