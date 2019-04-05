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

import com.viaoa.hub.*;
import com.viaoa.object.OAObject;
import com.viaoa.util.*;


/** 
<pre>
    [Java Code]
    OARadio rad;
    rad = new OARadio(hubJob,"hourly",true);
    form.add("radHourly",rad);
    rad = new OARadio(hubJob,"salary",false);
    form.add("radSalary",rad);
    ....
    [HTML Code]
    &lt;input type="radio" name="radGroupName" value="&lt;%=form.getRadio("radHourly").getValue()%&gt;"&gt;
    &lt;input type="radio" name="radGroupName" value="&lt;%=form.getRadio("radSalary").getValue()%&gt;"&gt;
    output =&gt;
    &lt;input type="radio" name="radType" value="Sample" checked vav=""&gt;

</pre>
    NOTE: the HTML tag param "name" needs to be the same for all radio buttons in same group
*/
public class OARadio extends OAHtmlComponent {
    private static final long serialVersionUID = 1L;
    protected Object selectValue;  // value to use if selected
    protected boolean bSelected, bOrig;

    
    protected Object currentObject; // the object that this is working with
    private boolean bUsed; // flag to know if it was used on form
    protected String groupName;
    protected Hub hubMasterList;
    protected String masterPropertyPath;
    protected String nullDescription = "None";
    
    public OARadio() {
    }

    public OARadio(Hub hub, String propertyPath, Object val) {
        this(hub,propertyPath,val,null);
    }
    public OARadio(Hub hub, String propertyPath, Object val, String groupName) {
        setHub(hub);
        setPropertyPath(propertyPath);
        setSelectValue(val);
        setGroupName(groupName);
    }
    public OARadio(Hub hub, String propertyPath, boolean tf) {
        this(hub,propertyPath, new Boolean(tf));
    }
    public OARadio(Hub hub, String propertyPath, boolean tf, String groupName) {
        this(hub,propertyPath, new Boolean(tf), groupName);
    }
    public OARadio(Hub hub, String propertyPath, int x) {
        this(hub,propertyPath, new Integer(x));
    }
    public OARadio(Hub hub, String propertyPath, int x, String groupName) {
        this(hub,propertyPath, new Integer(x), groupName);
    }


    /** Create group of Radio buttons used to select a value from a list.
        @param hubMasterList hub with list of all objects
        @param hub of selected objects - (this should be a detail hub)
        @param propertyPath name to display with checkbox
        @param masterPropertyPath name of property to use as label 
        Note: can only be used by getHtml()
    */
    public OARadio(Hub hub, Hub hubMasterList, String propertyPath, String masterPropertyPath) {
        setHub(hub);
        this.hubMasterList = hubMasterList;
        setPropertyPath(propertyPath);
        this.masterPropertyPath = masterPropertyPath;
    }
    public void setMasterListHub(Hub h) {
        this.hubMasterList = h;
    }
    public Hub getMasterListHub() {
        return this.hubMasterList;
    }
    /** the "word(s)" to use for the empty slot (null value) when using a master list.  
        ex: "none".  
        Default: "None" 
        set to null if none should be used
    */
    public String getNullDescription() {
        return nullDescription;
    }
    public void setNullDescription(String s) {
        nullDescription = s;
    }
    
   
    public void setGroupName(String s) {
        groupName = s;
    }
    public String getGroupName() {
        return groupName;
    }
    public boolean getSelected() {
        initialize();
        return bSelected;
    }
    public boolean isSelected() {
        return getSelected();
    }
    public void setSelected(boolean b) {
        initialize();
        this.bSelected = b;
        if (currentObject == null) bOrig = b;
    }

    /** value to set property to if selected */
    public void setSelectValue(Object obj) {
        this.selectValue = obj;
    }
    public Object getSelectValue() {
        return selectValue;
    } 

    protected void initialize() {
        if (actualHub != null) {
            Object obj = actualHub.getActiveObject();
            bSelected = false;
            if (obj != null) {    
                if (isCorrectClass(obj.getClass())) {
                    if (obj instanceof OAObject && ((OAObject)obj).isNull(propertyPath)) {
                        if (selectValue == null) bSelected = true;
                        obj = null;
                    }
                    else {
                        Method[] methods = getGetMethods();
                        obj = ClassModifier.getPropertyValue(obj, methods);
                        if (obj == null && selectValue == null) bSelected = true;
                        else if (obj != null && selectValue != null) bSelected = selectValue.equals(obj);
                    }
                }
            }
            if (currentObject != actualHub.getActiveObject()) {
                currentObject = actualHub.getActiveObject();
                bOrig = bSelected;
            }
        }
    }

    public boolean isChanged() {
        return (bOrig != bSelected);
    }

    /************************** OAHtmlComponent ************************/

    protected void beforeSetValuesInternal() {  // a Radio is only submitted if it is checked
        if (bUsed) bSelected = false;
        if (hubMasterList != null) selectValue = null;
    }
 
    protected void setValuesInternal(String nameUsed, String[] values) { // called by OAForm
        // a Radio is only submitted if it is checked
        if (hubMasterList != null) {
            selectValue = Util.getEncodedObject(nameUsed, hubMasterList, 0);
            bOrig = false; // force update() to run
        }
        bUsed = true;
        this.bSelected = true; // OAForm will only send if "true"
    }

    
    /** this will update the Hub activeObjects property */
    public void update() {
        if (!bUsed) return;

        bUsed = false;
        if (bSelected && !bOrig) {
            if (actualHub != null) {
                Object obj = actualHub.getActiveObject();
                if (obj != null) {
                    if (isCorrectClass(obj.getClass())) {
                        ClassModifier.setPropertyValue(obj, getSetMethod(), selectValue);
                    }
                }
            }
        }
        bOrig = bSelected;
        currentObject = null;
    }

    public void reset() {
        if (hub == null && selectValue == null) bSelected = bOrig;
        currentObject = null;
    }

    /** Html value */
    public String getValue() {
        bUsed = true;
        String s = name;

        try {        
            if ( isSelected() ) {
                s += "\" checked";
                s += " viaoa=\""; // "eat" ">
            }
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

    public String getHtml(String htmlTags) {
// <input type="radio" name="radType" value="<%=form.getRadio("radSalary").getValue()%>">
// <input type="radio" name="radType" value="Sample" checked vav="">        
        bUsed = true;
        if (hubMasterList != null) return getHtml2(htmlTags);
        String s = "";
        if (htmlBefore != null) s += htmlBefore;
        s += "<INPUT TYPE=\"RADIO\"";
        s += " NAME=\""+groupName+"\"";
        if ( htmlTags != null) s += " "+htmlTags;
        if ( htmlBetween != null) s += " "+htmlBetween;
        if ( (hub != null && hub.getPos() < 0) || !bEnabled || (form != null && form.getReadOnly()) ) s += " DISABLED";
        s += " VALUE=\""+getValue()+"\"";
        s += ">";
        if (htmlAfter != null) s += htmlAfter;
        return s;
    }


    protected Method[] displayPropertyMethods;
    public Method[] getDisplayPropertyMethods() {
        if (displayPropertyMethods == null) {
            displayPropertyMethods = ClassModifier.getMethods(hubMasterList.getObjectClass(), masterPropertyPath);
        }
        return displayPropertyMethods;
    }

    protected String getHtml2(String htmlTags) {
        String s="";
        Object value = hub.getAO();
        if (value != null) value = ClassModifier.getPropertyValue(value, getGetMethods());

        // loop through all master hub objects
        for (int i=0; ;i++) {
            OAObject obj = (OAObject) hubMasterList.elementAt(i);

            if (obj == null && nullDescription == null) break;
            
            if (htmlBefore != null) s += htmlBefore;
            s += "<INPUT TYPE=\"RADIO\"";

            s += " NAME=\"" + name + "\"";
            if (htmlTags != null) s += " "+htmlTags;
            if (hub == null || (hub.getAO() == null) || !bEnabled || (form != null && form.getReadOnly()) ) {
                s += " DISABLED";
            }

            String n = Util.getEncodedName(name, hubMasterList, obj);
            
            s += " VALUE=\""+n+"\"";
            if (obj == value) s += " CHECKED";

            if (i > 0 && htmlBetween != null) s += " "+htmlBetween;
            s += "> ";
            if (obj == null) s += nullDescription;
            else s += ClassModifier.getPropertyValueAsString(obj, getDisplayPropertyMethods());
            if (htmlAfter != null) s += htmlAfter;

            if (obj == null) break;
        }            
        return s;
    }

}


