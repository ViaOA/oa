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
package com.viaoa.jsp;

import com.viaoa.hub.*;
import com.viaoa.object.OAObject;
import com.viaoa.util.*;

/**
 * Used to add a column to a OATable.
 * @author vvia
 *
 */
public class OATableColumn implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    
    OATable table;
    protected String[] propertyPaths;
    protected boolean bPassword;
    protected String heading;
    protected int columns;
    protected String format;
    private OATableEditor editor;
    private int charWidth = 0; // single X char width in pixels

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
    public OATableColumn(String propertyPath, String heading, int columns, String format) {
        this.propertyPaths = new String[] { propertyPath };
        this.heading = heading;
        this.columns = columns;
        if (!OAString.isEmpty(format)) this.setFormat(format);
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

    public int getColumns() {
        return columns;
    }
    /** length of text (in characters).  */
    public void setColumns(int x) {
        columns = x;
    }
    public void setHeading(String s) {
        this.heading = s;
    }
    public String getHeader() {
        return heading;
    }

    
    /** width of a single character.  Used to compute the width based on columns, defaults to 12 */
    public void setAverageCharWidth(int w) {
        charWidth = w;
    }
    public int getAverageCharWidth() {
        return charWidth;
    }
    
    /**
        Returns format to use for displaying value as a String.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
    public String getFormat() {
        if (format == null) getDefaultFormat();
        return format;
    }
    public void setFormat(String fmt) {
        this.format = fmt;
        bDefaultFormat = false;
    }
    
    private boolean bDefaultFormat=true;
    protected String getDefaultFormat() {
        if (!bDefaultFormat || format != null || table == null) return format;
        Hub hub = table.getHub();
        if (hub == null) return format;
        
        if (propertyPaths == null || propertyPaths.length!=1 || OAString.isEmpty(propertyPaths[0])) return format;
        bDefaultFormat = false;
    
        OAPropertyPath pp = new OAPropertyPath(hub.getObjectClass(), propertyPaths[0]);
        if (pp != null) format = pp.getFormat();
        
        return format;
    }
    
    
    /** if true, then the output will be converted to '*' characters. */
    public boolean getPassword() {
        return bPassword;
    }
    public void setPassword(boolean tf) {
        bPassword = tf;
    }

    
    protected String getHtml(Hub hub, Object object, int row) {
        String s = null;
        if (object == null) s = "&nbsp;";
        else if (bPassword) {
            s = "*****";
        }
        else if (!(object instanceof OAObject)) {
            s = object.toString();
        }
        else {
            for (String prop : propertyPaths) {
                if (s == null) s = "";
                else s += "<br>";
                String s2 = ((OAObject)object).getPropertyAsString(prop, getFormat());
                if (s2 == null) s2 = "";
                s += s2;
            }
        }
        return s;
    }
    
    // called by OATable
    protected String getHtml(Hub hub, Object object, int row, boolean bHeading) {
        String s = null;

        if (bHeading) {
            s = heading;
            if (s == null) s = "";
        }
        else {
            s = getHtml(hub, object, row);
        }
        /** qqqq not needed, table will have width and overflow set        
        if (s.length() > columns) {
            if (s.charAt(0) == ' ') s = OAString.format(s, columns+" R.");
            else s = OAString.format(s, columns+"L.");
        }
        else if (bHeading) {
            for (int i=s.length(); i<columns; i++) {
                s += "&nbsp";
            }
        }
         */        
        return s;
    }

    public void setEditor(OATableEditor editor) {
        this.editor = editor;
    }
    public OATableEditor getEditor() {
        return this.editor;
    }
    
}

