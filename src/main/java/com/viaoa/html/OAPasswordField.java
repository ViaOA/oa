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
import com.viaoa.util.*;


/**
<pre>
    [Java Code]
    OAPasswordField ptxt = new OAPasswordField(hub,"password");
    form.add("txtPassword", ptxt);
    ....
    [HTML Code]
    &lt;input type="password" name="txtPassword" value="&lt;%=form.getTextField("txtPassword").getValue()%&gt;" size="16" maxlength="75"&gt;
    =&gt;
    &lt;input type="password" name="txtName" value="test data" size="16" maxlength="75"&gt;
</pre>
*/
public class OAPasswordField extends OATextField {
    private static final long serialVersionUID = 1L;

    public OAPasswordField() {
        bEscape = false;
        bPassword = true;
    }
    public OAPasswordField(Hub hub, String propertyPath) {
        super(hub,propertyPath);
        bEscape = false;
        bPassword = true;
    }
    public OAPasswordField(Hub hub, String propertyPath, int max) {
        super(hub,propertyPath,max);
        bEscape = false;
        bPassword = true;
    }

    public OAPasswordField(Object object, String propertyPath) {
        super(object,propertyPath);
        bEscape = false;
        bPassword = true;
    }
    public OAPasswordField(Object object, String propertyPath, int max) {
        super(object,propertyPath,max);
        bEscape = false;
        bPassword = true;
    }

    public String getHtml(String htmlTags) {
        String s = "";
        if (htmlBefore != null) s += htmlBefore;
        s += "<INPUT TYPE=\"PQSSWORD\"";
        s += " NAME=\""+name+"\"";

        if ( htmlTags != null) s += " "+htmlTags;
        if ( htmlBetween != null) s += " "+htmlBetween;
        if ( (hub != null && hub.getPos() < 0) || !bEnabled || (form != null && form.getReadOnly()) ) s += " READONLY";

        s += " VALUE=\""+getValue()+"\"";
        if (size >= 0) s += " SIZE=\""+size+"\"";
        if (max >= 0) s += " MAXLENGTH=\""+max+"\"";
        s += ">";
        if (htmlAfter != null) s += htmlAfter;
        return s;
    }


}

