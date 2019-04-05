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

/** ObjectBound TextArea 
<pre>
    [Java Code]
    OATextArea txta = new OATextArea(hubJob,"benefits");
    form.add("txtDesc",txta);
    ....
    [HTML Code]
    &lt;textarea name="txtDesc" rows="3" cols="12" wrap="VIRTUAL|PHYSICAL|OFF"&gt;&lt;%=form.getTextArea("txtDesc").getText()%&gt;&lt;/textarea&gt;

</pre>
*/
public class OATextArea extends OATextField {
    private static final long serialVersionUID = 1L;
    protected int rows=-1, wrap;
    public static final int WRAP_NONE=0;
    public static final int WRAP_VIRTUAL=1;
    public static final int WRAP_PHYSICAL=2;
    public static final int WRAP_OFF=3;
    

    public OATextArea() {
        bEscape = false;
    }
    public OATextArea(Hub hub, String propertyPath, int rows, int cols, int max) {
        super(hub,propertyPath, cols, max);
        bEscape = false;
        setRows(rows);
    }

    public OATextArea(Hub hub, String propertyPath) {
        this(hub, propertyPath,-1,-1,-1);
    }
    public OATextArea(Hub hub, String propertyPath, int max) {
        this(hub,propertyPath,-1,-1,max);
    }
    public OATextArea(Hub hub, String propertyPath, int rows, int cols) {
        this(hub,propertyPath, rows, cols, -1);
    }
    
    public void setWrap(int x) {
        if (x < 4 && x >= 0) wrap = x;
    }
    public int getWrap() {
        return wrap;
    }

    public OATextArea(Object object, String propertyPath, int rows, int cols, int max) {
        super(object,propertyPath, cols, max);
        bEscape = false;
        setRows(rows);
    }
    
    public OATextArea(Object object, String propertyPath) {
        this(object, propertyPath, -1, -1, -1);
    }
    public OATextArea(Object object, String propertyPath, int max) {
        this(object, propertyPath, -1, -1, max);
    }



    public int getRows() {
        return rows;
    }
    /** number of rows.  */
    public void setRows(int x) {
        rows = x;
    }

    public String getHtml(String htmlTags) {
//<textarea name="txtDesc" rows="3" cols="12" wrap="VIRTUAL|PHYSICAL|OFF"><%=form.getTextArea("txtDesc").getValue()%></textarea>
        String s = "";
        if (htmlBefore != null) s += htmlBefore;
        s += "<TEXTAREA";
        s += " NAME=\""+name+"\"";
        
        if ( htmlTags != null) s += " "+htmlTags;
        if ( htmlBetween != null) s += " "+htmlBetween;
        
        // s += " VALUE=\""+getValue()+"\"";
        if (rows >= 0) s += " ROWS=\""+rows+"\"";
        if (size >= 0) s += " COLS=\""+size+"\"";

        if (wrap > 0) {
            String[] ss = {"VIRTUAL","PHYSICAL","OFF" };
            s += " WRAP=\""+ss[wrap-1]+"\"";
        }
        if ( (hub != null && hub.getPos() < 0) || !bEnabled || (form != null && form.getReadOnly()) ) s += " READONLY";
        s += ">";
        s += getValue();
        s += "</textarea>";
        if (htmlAfter != null) s += htmlAfter;
        return s;

//<TEXTAREA NAME="txtaNotes"  VALUE="" READONLY viaoa="" ROWS="8" COLS="65" READONLY>" READONLY viaoa="</textarea>
    
    }

}


