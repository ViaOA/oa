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

/** represents columns within a OATable.  Each column can have multiple properties, each
    on a different line within the column.
<pre>
    [Java Code]
    OATableColumn tc = new OATableColumn("addressType.name");
    table.add(tc);
    tc = new OATableColumn(new String[] {"addressType.name","csz"} );
    table.add(tc);
    ....
    [HTML Code]
    &lt;td align="LEFT" valign="TOP" width="80"&gt;
    &nbsp;&nbsp;&nbsp;&nbsp;&lt;%= table.getCellValue(row,0) %&gt;
    &lt;/td&gt;
</pre>    
*/
public class OATableColumn extends OAHtmlComponent {
    private static final long serialVersionUID = 1L;
    String[] propertyPaths;
    protected Method[][] methods;
    protected boolean bPassword;
    protected OAHtmlComponent comp;
    protected String heading;
    protected int columns;

    public OATableColumn(String propertyPath) {
        this.propertyPaths = new String[] { propertyPath };
    }
    public OATableColumn(String propertyPath, String heading) {
        this.propertyPaths = new String[] { propertyPath };
        this.heading = heading;
    }
    public OATableColumn(String propertyPath, String heading, int columns) {
        this.propertyPaths = new String[] { propertyPath };
        this.heading = heading;
        this.columns = columns;
    }
    public OATableColumn(String[] propertyPaths) {
        this.propertyPaths = propertyPaths;
    }
    public OATableColumn(String[] propertyPaths, String heading) {
        this.propertyPaths = propertyPaths;
        this.heading = heading;
    }
    public OATableColumn(String[] propertyPaths, String heading, int columns) {
        this.propertyPaths = propertyPaths;
        this.heading = heading;
        this.columns = columns;
    }
    public OATableColumn(OAHtmlComponent comp) {
        this.comp = comp;
    }
    public OATableColumn(OAHtmlComponent comp, String heading) {
        this.comp = comp;
        this.heading = heading;
    }

    public int getColumns() {
        return columns;
    }
    /** length of text (in characters).  */
    public void setColumns(int x) {
        columns = x;
    }


   
    // methods are set to null whenever Hub or PropertyPath get changed
    Method[][] getMethods(Class clazz) {
        if (methods != null) return methods;

        if (comp != null) {
            String path = comp.getPropertyPath();
            // get path from any link Hub
            Hub h = comp.getHub();
            for ( ; h != null;) {
                Hub lh = h.getLinkHub();
                if (lh == null) break;
                if (path == null) path = "";
                path = h.getLinkPath() + "." + path;
                h = lh;
            }
            // if path == null then getMethods() will use "toString"
            methods = new Method[1][];
            methods[0] = ClassModifier.getMethods(clazz, path);
        }
        else {
            methods = new Method[propertyPaths.length][];
            for (int i=0; i<methods.length; i++) {
                methods[i] = ClassModifier.getMethods(clazz, propertyPaths[i]);
            }
        }
        return methods;           
    }
    public void resetMethods() {
        methods = null;
    }
    
    public void setHeading(String s) {
        this.heading = s;
    }
    public String getHeader() {
        return heading;
    }
    
    /** if true, then the output will be converted to '*' characters. */
    public boolean getPassword() {
        return bPassword;
    }
    public void setPassword(boolean tf) {
        bPassword = tf;
    }


    protected String getValue(Object obj) {
        if (obj == null) return "";

        getMethods(obj.getClass());
        String value = "";
        for (int i=0; i < methods.length; i++) {
            if (i > 0) value += "<br>";
            Object objx = ClassModifier.getPropertyValue(obj, methods[i]);
            String fmt = getFormat(methods[i][methods[i].length-1]);
            value += OAConverter.toString(objx,fmt);
        }
        return value;
    }

    // called by OATable
    protected String getHtml(Hub hub, Object object, boolean bHeading, boolean bActiveRow) {
        String s = "";

        if (bHeading) s += "<TH";
        else s += "<TD";
        if ( htmlBetween != null) s += " "+ htmlBetween;
        s += ">";

        if (htmlBefore != null) s += htmlBefore;

        if (bHeading) s += heading;
        else {
            if (bActiveRow && comp != null) {
                s += comp.getHtml();
            }
            else if (object == null) s += "&nbsp;";
            else {
                String value = getValue(object);
                if (value == null || value.length() == 0) value = "&nbsp;";
                s += value;
            }
        }
        
        if (htmlAfter != null) s += htmlAfter;
        if (bHeading) s += "</TH>";
        else s += "</TD>";
        
        return s;
    }

}

