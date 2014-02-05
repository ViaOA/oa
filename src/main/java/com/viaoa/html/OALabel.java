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
import com.viaoa.object.*;
import com.viaoa.util.*;

/** OALabel will output the string value for a property path
<pre>
    [Java Code]
    OALabel lbl = new OALabel(hubEmployee, "lastName");
    -or-
    OALabel lbl = new OALabel(hubEmployee);  // property is needed when calling getText()
    form.add("lblEmployee", lbl);
    ....
    ....
    [HTML Code]
    form.getLabel("lblLastName").getText()
    form.getLabel("lblEmployee").getText("lastName")

</pre>    
*/

public class OALabel extends OAHtmlComponent {
    private static final long serialVersionUID = 1L;
    protected String propertyPath, value;
    protected String url; // forward url

    public OALabel() {
    }
    public OALabel(OAObject obj, String propertyPath) {
        setObject(obj);
        setPropertyPath(propertyPath);
    }
    public OALabel(Hub hub, String propertyPath) {
        setHub(hub);
        setPropertyPath(propertyPath);
    }
    public OALabel(Hub hub) {
        setHub(hub);
    }

         
    /************************** OAHtmlComponent ************************/

    /** this is used if both Hub and Object are not used. */
    public void setText(String s) {
        value = s;
    }
    /** sets propertyName and calls getText() to get HTML safe string.
        @see OALabel#getText
    */
    public String getText(String propertyName) {
        setPropertyPath(propertyName);
        return getText();
    }

    /** @returns HTML safe/converted String 
        Converts null to "", converts &lt;,&gt;,&quot;,&amp; to html strings,
        [CR][LF} to <br> 
        @see OALabel#getRawText to get unconverted value
    */
    public String getText() {
       
        String s = getValue();
        if (s == null) s = "";
        else {
            s = com.viaoa.html.Util.toEscapeString(s);
            s = Util.convert(s, "\r\n", "<BR>");
            s = Util.convert(s, "\n", "<BR>");
            s = Util.convert(s, "\r", "<BR>");
        }
        lastValue = s;
        return s;
    }
    
    
    /** returns string value of text or null
        @see OALabel#getText to get a HTML safe string
    */
    public String getRawText() {
        return getValue();
    }
    
    private String lastValue;
    public boolean needsRefreshed() {
        String s = lastValue;
        String s2 = getText();
        return (s == null || !s.equals(s2));
    }
    

    /** returns the String value of the current object property, "" if null, or object class is not
        assignable.  
    */
    protected String getValue() {
        Object obj = null;
        if (hub != null) obj = hub.getActiveObject();
        else {
            if (object != null) obj = object;   
            else {
                if (value != null) return value;
                obj = null;
            }
        }

        if (obj == null) return null;

        Method[] methods = getGetMethods();
        if (methods == null) return "OALabel cant get methods for propertyPath \""+propertyPath+"\"";
        
        String s = "";
        try {        
            s = ClassModifier.getPropertyValueAsString(obj, methods,getFormat());
        }
        catch (Exception e) {
            handleException(e,"getValue()");
            return "Exception Occured";
        }

        return s;
    }

    public String getHtml() {
        return getText();
    }
    protected void setValuesInternal(String nameUsed, String[] values) { // called by OAForm
    }
}
