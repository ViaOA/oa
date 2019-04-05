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


import java.lang.reflect.*;
import java.util.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;


/** CheckBox that can be used for the following purposes:<br>
    1: set a property to either a selected or unselected value.<br>
    2: add/remove an object to/from Hub.<br>
    3: checkbox group to add remove many objects from a master list Hub to a Hub.<br>
<pre>
    [Java Code]
    OACheckBox chk = new OACheckBox(hubJob,"hourly", "Yes", "No" );
    form.add("chkHourly",chk);

    chk = new OACheckBox(hubJob,"status", status1, status2 );
    form.add("chkStatus",chk);

    chk = new OACheckBox(hubJob, job);
    form.add("chkJob",chk);
    
    ....
    [HTML Code]
    &lt;input type="checkbox" name="chkHourly" value="&lt;%=form.getCheckBox("chkHourly").getValue()%&gt;"&gt;
    output =&gt;
    &lt;input type="checkbox" name="chkHourly" value="true" checked vav=""&gt;
</pre>
*/
public class OACheckBox extends OAHtmlComponent {
    private static final long serialVersionUID = 1L;
    protected Object onValue, offValue, hubObject;
    protected Object currentObject; // the object that this is working with
    protected boolean bSelected, bOrig;
    private boolean bUsed; // flag to know if it was used on form
    protected Hub hubMasterList;
    protected Vector vecSent = new Vector(5,5);
    protected Vector vecRecv = new Vector(5,5);
    protected String displayPropertyPath; // used with hubMasterList
    protected Method[] getDisplayMethods;  // from hubMasterList
    
    public OACheckBox() {
    }

    /** create checkBox that can be used to set a property to either a selected or unselected value.
    */
    public OACheckBox(Hub hub, String propertyPath, Object onValue, Object offValue) {
        setHub(hub);
        setPropertyPath(propertyPath);
        setOnValue(onValue);
        setOffValue(offValue);
    }

    public OACheckBox(Hub hub, String propertyPath, int onValue, int offValue) {
        this(hub, propertyPath, new Integer(onValue), new Integer(offValue));
    }
    
    /** defaults to using true/false for on/off values */
    public OACheckBox(Hub hub, String propertyPath) {
        this(hub,propertyPath, new Boolean(true),new Boolean(false));
    }    

    /** create checkBox that can be used to add/remove object to/from a hub. 
        @param propertyPath to detail hub
        @param hubObject object to add/remove from detail hub
    */
    public OACheckBox(Hub hub, String propertyPath, Object hubObject) {
        this(hub, propertyPath);
        setHubObject(hubObject);
    }


    
    /** Create group of checkboxes used to select many values from a list.
        @param hubMasterList hub with list of all objects
        @param hub of selected objects
        @param propertyPath to Hub of objects
        @param displayPropertyPath name to display with checkbox from hubMasterList
        Note: can only be used by getHtml()
    */
    public OACheckBox(Hub hub, String propertyPath, Hub hubMasterList, String displayPropertyPath) {
        setHub(hub);
        this.hubMasterList = hubMasterList;
        this.displayPropertyPath = displayPropertyPath;
        setPropertyPath(propertyPath);
    }
    
    
    public void setMasterListHub(Hub h) {
        this.hubMasterList = h;
    }
    public Hub getMasterListHub() {
        return this.hubMasterList;
    }
    
    
    /** value to set property to if checkBox is selected. */
    public Object getOnValue() {
        return onValue;
    }
    public void setOnValue(Object obj) {
        this.onValue = obj;
    }
    /** value to set property to if checkBox is not selected. */
    public Object getOffValue() {
        return offValue;
    }
    public void setOffValue(Object obj) {
        this.offValue = obj;
    }

    /** object to add/remove from Hub. */
    public Object getHubObject() {
        return hubObject;
    }
    public void setHubObject(Object obj) {
        this.hubObject = obj;
    }
    
    public boolean getSelected() {
        initialize();
        return bSelected;
    }
    public boolean isSelected() {
        return getSelected();
    }

    protected boolean getSelected(Object obj) {
        if (obj == null) return false;
        if (isCorrectClass(obj.getClass())) {
            if (obj instanceof OAObject && ((OAObject)obj).isNull(propertyPath)) obj = null;
            else {
                Method method = getGetMethod();
                obj = ClassModifier.getPropertyValue(obj, method);
            }
            if (hubObject != null && (obj instanceof Hub)) {
                Hub h = (Hub) obj;
                return (h.getObject(hubObject) != null);
            }
            if (obj == onValue) return true;
            if (obj != null && onValue != null) return obj.equals(onValue);
        }
        else {
            obj = OAObjectReflectDelegate.getProperty((OAObject)obj, propertyPath);
            if (obj == onValue) return true;
            if (obj != null && onValue != null) return obj.equals(onValue);
        }
        return false;
    }
    
    public void setSelected(boolean b) {
        initialize();
        this.bSelected = b;
        if (hub == null) bOrig = b;
    }


    protected void initialize() {
        if (hubObject != null) {  // see if object exists in hub
            bSelected = (hub.getObject(hubObject) != null);
            if (currentObject == null) {
                currentObject = object;
                bOrig = bSelected;
            }
            return;
        }
        
        if (actualHub != null) {
            Object obj = actualHub.getActiveObject();
            bSelected = getSelected(obj);
            if (currentObject != obj) {
                currentObject = actualHub.getActiveObject();
                bOrig = bSelected;
            }
        }
    }


    /************************** OAHtmlComponent ************************/

    public boolean isChanged() {
        initialize(); 
        return isChanged(bSelected);
    }
    protected boolean isChanged(boolean b) {
        return (b != bOrig);
    }
    
    public void reset() {
        if (hub == null && object == null) bSelected = bOrig;
    }

    protected void beforeSetValuesInternal() {  // a check box is only submitted if it is checked
//System.out.println("----> OACheckBox.beforeSetValuesInternal "+name+" bUsed="+bUsed+" bSelected="+bSelected);//qqqqqqqq
        vecRecv.removeAllElements();
        if (bUsed) this.bSelected = false;
    }

    protected void setValuesInternal(String nameUsed, String[] values) { // called by OAForm
//System.out.println("----> OACheckBox.setValuesInternal "+name);//qqqqqqqq
        if (Util.isEncodedName(nameUsed)) {
            vecRecv.addElement(nameUsed);
        }
        bUsed = true;
        this.bSelected = true; // OAForm will only send if "true"
    }
    
    /** this will update the Hub activeObjects property */
    public void update() {
//System.out.println("----> OACheckBox.update "+name+" bUsed="+bUsed+" bSelected="+bSelected+" bOrig="+bOrig);//qqqqqqqq
        if (!bUsed) return;  // else never displayed on form
        bUsed = false;
        
        int x = vecSent.size();
        if (x > 0) {
            for (int i=0; i<x; i++) {
                String nameUsed = (String) vecSent.elementAt(i);
                boolean b = vecRecv.contains(nameUsed);
                Object obj = Util.getEncodedObject(nameUsed, hub, 0);
                Object obj2 = Util.getEncodedObject(nameUsed, hubMasterList, 1);

                if (obj2 == null) {
                    if (hubObject != null) {
                        obj = ClassModifier.getPropertyValue(obj, getGetMethod());
                        if (obj != null && (obj instanceof Hub)) {
                            Hub h = (Hub) obj;
                            if (b) {
                                if (h.getObject(hubObject) == null) h.add(hubObject);
                            }
                            else h.remove(hubObject);
                        }
                    }
                    else if (obj != null) {
                        if (isCorrectClass(obj.getClass())) {
                            ClassModifier.setPropertyValue(obj, getSetMethod(), b?onValue:offValue);
                        }
                        else {
                            OAObjectReflectDelegate.setProperty((OAObject)obj, propertyPath, b?onValue:offValue, null);
                        }
                    }
                }
                else {
                    obj = ClassModifier.getPropertyValue(obj, getGetMethod());
                    if (obj != null && (obj instanceof Hub)) {
                        Hub h = (Hub) obj;
                        if (b) {
                            if (h.getObject(obj2) == null) h.add(obj2);
                        }
                        else h.remove(obj2);
                    }
                }
            }
        }
        else {        
//System.out.println("----> OACheckBox.update BOTTOM isChanged="+isChanged(bSelected));//qqqqqqqq
            if (hubObject != null) {  // see if object exists in hub
                Object obj = ClassModifier.getPropertyValue(hub.getAO(), getGetMethod());
                if (obj != null && (obj instanceof Hub)) {
                    Hub h = (Hub) obj;
                    if (bSelected) {
                        if (h.getObject(hubObject) == null) h.add(hubObject);
                    }
                    else h.remove(hubObject);
                }
            }
            else if (actualHub != null && isChanged(bSelected)) {
                Object obj = actualHub.getActiveObject();
                if (obj != null) {
                    if (isCorrectClass(obj.getClass())) {
                        ClassModifier.setPropertyValue(obj, getSetMethod(), bSelected?onValue:offValue);
                    }
                }
            }
            currentObject = null;
        }
        bOrig = bSelected;
    }
    protected void afterSetValuesInternal() {
        vecSent.removeAllElements();
    }

    /** returns the HTML string needed for "value" and "checked". See example at top of class */
    public String getValue() {
        initialize();
        if (hub == null && object == null) {
            bUsed = true;
            String s = "true";
                    
            boolean b1 = bSelected;
            boolean b2 = ( !bEnabled || (form != null && form.getReadOnly()) );
            
            if (b1 || b2) s += "\"";
            if (b1) s += " CHECKED";
            if (b2) s += " DISABLED";
            if (b1 || b2) s += " viaoa=\""; // eat the extra "
            
            return s;
        }
        else return getValue( (hub == null)?object:hub.getAO() );
    }
    
    protected String getValue(Object obj) {
        // dont call initialize
        bUsed = true;

        String s = "true";
        try {

            boolean b1 = getSelected(obj);
            boolean b2 = ( obj == null || !bEnabled || (form != null && form.getReadOnly()) );
            
            if (b1 || b2) s += "\"";
            if (b1) s += " CHECKED";
            if (b2) s += " DISABLED";
            if (b1 || b2) s += " viaoa=\""; // eat the extra "
            
        }
        catch (Exception e) {
            handleException(e,"getValue()");
            return "\">Exception Occured<xx value=\"";
        }
        
        lastValue = s;
        return s;
    }

    private String lastValue;
    public boolean needsRefreshed() {
        String s = lastValue;
        String s2 = getValue();
        lastValue = s;
        return (s == null || !s.equals(s2));
    }

    /** get the HTML tags for an Object in this hub */
    public String getHtml(Object obj) {
        if (obj == null) return "";
        return getHtml("", obj);
    }
    
    /** get the HTML tags for the Object in this hub at position pos */
    public String getHtml(int pos) {
        return getHtml("", hub.elementAt(pos));
    }
    
    public String getHtml(String htmlTags) {
        if (hub == null) {
            return getHtml(htmlTags, null);
        }
        return getHtml(htmlTags, hub.getAO());
    }
    
    protected String getHtml(String htmlTags, Object obj) {
        bUsed = true;
// <input type="checkbox" name="chkHourly" value="<%=form.getCheckBox("chkHourly").getValue()%>">
// <input type="checkbox" name="chkHourly" value="true" checked vav="">
        if (hubMasterList != null) return getHtml2(obj, htmlTags);
        String s="";
        if (htmlBefore != null) s += htmlBefore;
        s += "<input type=\"checkbox\"";

        if ( htmlTags != null) s += " "+htmlTags;
        if ( htmlBetween != null) s += " "+htmlBetween;

        String nameUsed;
        if (obj == null) nameUsed = name;
        else {
            nameUsed = Util.getEncodedName(name, hub, (OAObject) obj);
            vecSent.addElement(nameUsed);
        }

        s += " NAME=\""+ nameUsed + "\"";
        if (hub == null) s += " VALUE=\""+getValue()+"\"";
        else s += " VALUE=\""+getValue(obj)+"\"";
        s += ">";
        if (htmlAfter != null) s += htmlAfter;
        return s;
    }

    
    protected String getHtml2(Object objectUsed, String htmlTags) {
        String s="";
        
        Hub h = null;
        if (objectUsed != null) h = (Hub) ClassModifier.getPropertyValue(objectUsed, getGetMethods());

        // loop through all master hub objects
        for (int i=0; ;i++) {
            OAObject obj = (OAObject) hubMasterList.elementAt(i);
            if (obj == null) break;

            if (htmlBefore != null) s += htmlBefore;
            s += "<INPUT TYPE=\"CHECKBOX\"";

            String n = Util.getEncodedName(name, hub, objectUsed, hubMasterList, obj);
            vecSent.addElement(n);
            
            s += " NAME=\"" + n + "\"";
            if (htmlTags != null) s += " "+htmlTags;
            if (hub == null || (!hub.isValid()) || !bEnabled || (form != null && form.getReadOnly()) ) {
                s += " DISABLED";
            }
            s += " VALUE=\"true\"";
            if (h != null && h.getObject(obj) != null) s += " CHECKED";

            if (i > 0 && htmlBetween != null) s += " "+htmlBetween;
            s += "> ";
            
            if (getDisplayMethods == null) {
                getDisplayMethods = ClassModifier.getMethods(hubMasterList.getObjectClass(), displayPropertyPath);
            }
            if (getDisplayMethods == null) s += "OACheckBox error: invalid propertyPath from hubMasterList";
            else s += ClassModifier.getPropertyValueAsString(obj, getDisplayMethods);
            
            if (htmlAfter != null) s += htmlAfter;
        }            
        return s;
    }

}

/***

    public boolean getSelected(Object obj) {
        if (object != null) {  // see if object exists in hub
            return (hub.get(obj) != null);
        }
        if (actualHub != null) {
            if (obj != null) {    
                if (isCorrectClass(obj.getClass())) {
                    if (obj instanceof OAObject && ((OAObject)obj).isNull(propertyPath)) return false;
                    Method method = getGetMethod();
                    obj = ClassModifier.getPropertyValue(obj, method);
                    if (obj == onValue) return true;
                    if (obj != null && onValue != null) return obj.equals(onValue);
                }
            }
        }
        return false;
    }

    // from Hub and PropertyPath
    protected Method[] displayPropertyMethods;
    public Method[] getDisplayPropertyMethods() {
        if (displayPropertyMethods == null) {
            displayPropertyMethods = ClassModifier.getMethods(hubMaster.getObjectClass(), displayPropertyPath);
        }
        return displayPropertyMethods;
    }



    private String getHtml2(String htmlTags) {
        Object objx = hub.getAO();
        String s="";

        // get detail hub
        Hub h = null;
        if (objx != null) {
            Object o = ClassModifier.getPropertyValue(objx, getGetMethods());
            if (!(o instanceof Hub)) return "OACheckBox "+name+" get"+propertyPath+" does not return a Hub";
            h = (Hub) o;
        }
    ...
    
    
**/

