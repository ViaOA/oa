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
    OATextField txt = new OATextField(hub,"name");
    form.add("txtName", txt);
    ....
    [HTML Code]
    &lt;input type="text" name="txtName" value="&lt;%=form.getTextField("txtName").getValue()%&gt;" size="16" maxlength="75"&gt;
    =&gt;
    &lt;input type="text" name="txtName" value="test data" size="16" maxlength="75"&gt;
</pre>
*/
public class OATextField extends OAHtmlComponent {
    private static final long serialVersionUID = 1L;
    protected Object value, origValue, submittedValue; 
    protected String lastSubmitValue;
    protected String lastValueSent;
    protected int max=-1;
    protected int size=-1;
    protected boolean bPassword;
    protected Object currentObject; // the object that this is working with
    protected boolean bUsed; // flag to know if it was used on form
    protected boolean bEscape = true;
    public OATextField() {
    }
    public OATextField(Hub hub, String propertyPath) {
        setHub(hub);
        setPropertyPath(propertyPath);
    }
    public OATextField(Hub hub, String propertyPath, int max) {
        this(hub, propertyPath, -1, max);
    }
    public OATextField(Hub hub, String propertyPath, int size, int max) {
        setHub(hub);
        setPropertyPath(propertyPath);
        setSize(size);
        setMax(max);
    }

    public OATextField(Object object, String propertyPath, int size, int max) {
        setObject(object);
        setPropertyPath(propertyPath);
        setSize(size);
        setMax(max);
    }
    public OATextField(Object object, String propertyPath) {
        this(object, propertyPath,-1,-1);        
    }
    public OATextField(Object object, String propertyPath, int max) {
        this(object, propertyPath,-1,max);        
    }

    public int getMax() {
        return max;
    }
    /** max length of text.  If -1 (default) then unlimited.  All input will be truncated to 
        this amount without a warning 
    */
    public void setMax(int x) {
        max = x;
    }

    public int getSize() {
        return size;
    }
    /** length of text.  */
    public void setSize(int x) {
        size = x;
    }
    public int getColumns() {
        return size;
    }
    /** length of text.  */
    public void setColumns(int x) {
        size = x;
    }

    public void setText(String s) {
        value = convertToObject(s);
        if (hub == null) origValue = value;
    }

    /** unconverted/raw value stored for textfield. 
        @returns string value of text or null
        @see OATextField#getValue to get HTML safe text
        @see OATextField#getHtmlText to get HTML safe text
    */
    public String getText() {
        try {
            initialize();
        }
        catch (Exception e) {
            handleException(e,"getText()");
            return "Exception Occured";
        }
        return OAConv.toString(value);
    }



    protected void initialize() {
        if (actualHub == null && object == null) return;
        Object obj;
        if (actualHub != null) obj = actualHub.getActiveObject();
        else obj = object;

        if (obj != null) {
            if ( (obj instanceof OAObject) && ((OAObject)obj).isNull(propertyPath)) value = null;
            else {
                if (!isCorrectClass(obj.getClass())) value = null;
                else value = ClassModifier.getPropertyValue(obj, getGetMethod());
            }
        }
        else value = null;
        if (currentObject != obj) {
            currentObject = obj;
            bResetTop = true;
            origValue = value;
        }
    }
    
    /** HTML "safe", "formatted" value stored for textfield. 
        Converts null to "", converts &lt;,&gt;,&quot;,&amp; to html strings.
        @see OATextField#getValue - same as calling getHtmlText()
        @see OATextField#getText to get "real" raw value
    */
    public String getHtmlText() {
        try {
            initialize();
        }
        catch (Exception e) {
            handleException(e,"getHtmlText()");
            return "Exception Occured";
        }

        bResetTop = false;
        bUsed = true;

        String s = OAConv.toString(value, getFormat());
        if (s == null) s = "";
        
        if (bPassword) {
            // convert password chars to "*" chars, this will get unconverted by setValues()
            int x = s.length();
            s = "";
            for (int i=0; i<x; i++) s += '*';
        }        
        if (bEscape) {
            s = com.viaoa.html.Util.toEscapeString(s);
            if ( (hub != null && hub.getPos() < 0) || !bEnabled || (form != null && form.getReadOnly()) ) {
                s += "\" READONLY viaoa=\"";
            }
        }
        lastValueSent = s;
        return s;
    }

    /** same as calling getHtmlText()
        @returns HTML "safe" vaule
        @see OATextField#getText
    */
    public String getValue() {
        return getHtmlText();
    }

    protected Object convertToObject(String s) {
        Object obj;
        if (s == null) s = "";
        if (max >= 0 && s != null && s.length() > max) s = s.substring(0,max);

        if (object == null && hub == null) obj = s;
        else {
            obj = OAConv.convert( ClassModifier.getClass(getGetMethod()), s );
        }
        return obj;
    }
    


    /************************** OAHtmlComponent ************************/
    protected void beforeSetValuesInternal() {
        submittedValue = null;
    }
    protected void setValuesInternal(String nameUsed, String[] values) { // called by OAForm
        String svalue;
        if (values == null || values.length != 1 || values[0] == null || values[0].length() == 0) {
            svalue = ""; 
        }
        else {
            if (max >= 0 && values[0].length() > max) {
                throw new RuntimeException("TextField "+name+" max length exceeded. length="+values[0].length()+" max="+max);
                // values[0] = values[0].substring(0,max);
            }

            String hold = OAConv.toString(value);
            svalue = values[0];

            // this will unencrypt a password field
            if (bPassword && hold != null && svalue != null) {  // need to convert "*" to original value
                int x = hold.length();
                if (svalue.length() < x) x = svalue.length();
                int i=0;
                for (; i<x && values[0].charAt(i) == '*'; i++);
                if (i > 0) {
                    svalue = hold.substring(0,i);
                    if (i < values[0].length()) svalue += values[0].substring(i);
                }
            }
        }
        lastSubmitValue = svalue;
        submittedValue = convertToObject(svalue);
    }
    public void update() {
        if (submittedValue == null) return;
        bUsed = false;
        value = submittedValue;
        if (isChanged(value)) {
            Object obj;
            if (actualHub != null) obj = actualHub.getActiveObject();
            else obj = object;
            
            if (obj != null && isCorrectClass(obj.getClass())) {
                Class c = ClassModifier.getClass(getSetMethod());
                if ((lastSubmitValue == null || lastSubmitValue.length() == 0) && obj instanceof OAObject) {
                    if (String.class.equals(c) ) {
                    	if ( ((OAObject) obj).isNull(getPropertyPath()) ) submittedValue = null;
                    }
                }            	
                ClassModifier.setPropertyValue(obj, getSetMethod(), submittedValue);
                // if class is numeric and value is blank, then set property to null
                if ((lastSubmitValue == null || lastSubmitValue.length() == 0) && obj instanceof OAObject) {
                    if ( ClassModifier.isNumber(c) ) {
                        ((OAObject) obj).setProperty(propertyName, null);
                    }
                }
            }
        }
        origValue = value;
    }
    protected void afterSetValuesInternal() {
        submittedValue = null;
    }

    public boolean isChanged() {
        initialize();
        
        
        return isChanged(value);
    }
    protected boolean isChanged(Object newValue) {

        // 2008/01/18
        if (lastValueSent != null) {
        	if (lastValueSent.equals(lastSubmitValue)) return false;
        }
    	
    	boolean b = (newValue == origValue);
        if (!b) {
            if (newValue != null && origValue != null) b = newValue.equals(origValue);
        }
        return !b;
    }

    /** set value back to property value of object or null if no object is used. */
    public void reset() {
        submittedValue = null;
        if (hub == null && object == null) value = origValue;
    }
    
    /** same as calling getValue() and converts [cr][lf] to HTML &lt;BR&gt; 
    */
    public String getHtmlValue() {
        String value = getValue();
        String s = value;
        s = Util.convert(s, "\r\n", "<BR>");
        s = Util.convert(s, "\n", "<BR>");
        s = Util.convert(s, "\r", "<BR>");
        return s;
    }


    public boolean needsRefreshed() {
        String s = lastValueSent;
        String s2 = getText();
        return (s == null || !s.equals(s2));
    }

    protected boolean bResetTop;
    /**  @return true if this component wants top of page reset back to 0. */
    public boolean resetTop() {
        initialize();
        return bResetTop;
    }

    public String getHtml(String htmlTags) {
        // <input type="text" name="txtName" value="test data" size="16" maxlength="75">
        String s = "";
        if (htmlBefore != null) s += htmlBefore;
        s += "<INPUT TYPE=\"text\"";
        s += " NAME=\""+name+"\"";
        if ( htmlTags != null) s += " "+htmlTags;
        if ( htmlBetween != null) s += " "+htmlBetween;
        s += " VALUE=\""+getValue()+"\"";
        if (size >= 0) s += " SIZE=\""+size+"\"";
        if (max >= 0) s += " MAXLENGTH=\""+max+"\"";
        s += ">";
        if (htmlAfter != null) s += htmlAfter;
        return s;
    }
}

