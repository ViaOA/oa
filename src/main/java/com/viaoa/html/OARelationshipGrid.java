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
import com.viaoa.hub.*;
import com.viaoa.util.*;

/**   qqqqqqqqqqqqqqqqqq
<pre>
    [Java Code]
    Hub hubR = new Hub(DataType.class);
    hubR.select();

    Hub hubC = new Hub(ChartType.class);
    hubC.select();

    OARelationshipGrid grid = new OARelationshipGrid(hubR, "value", hubC, "value", "chartTypes");
    form.add("grid", grid);

   
    ....
    [HTML Code]
<pre>

&lt;table border=1&gt;
    &lt;tr&gt;
&lt;%
    int cc = grid.getColumnCount();
    out.println("&lt;th&gt;&lt;/th&gt;");
    for (int c=0; c &lt; cc; c++) {
        out.println("&lt;th&gt;"+grid.getColumnHeading(c)+"&lt;/th&gt;");
    }
    out.println("&lt;/tr&gt;");

    int rr = grid.getRowCount();
    for (int r=0; r &lt; rr; r++) {
        out.println("&lt;tr&gt;");
        out.println("&lt;th&gt;"+grid.getRowHeading(r)+"&lt;/th&gt;");
        for (int c=0; c &lt; cc; c++) {
            out.println("&lt;td align=\"center\"&gt;"+grid.getCheckBoxHtml(r,c)+"&lt;/td&gt;");
        }
        out.println("&lt;/tr&gt;");
    }
%&gt;

&lt;/table&gt;

</pre>
*/
public class OARelationshipGrid extends OAHtmlComponent {
    private static final long serialVersionUID = 1L;
    protected Hub hubRow, hubColumn;
    protected String propertyHeaderRow, propertyHeaderColumn;

    protected String propertyFromRow;
    protected Method[] methodsHeaderRow, methodsHeaderColumn, methodsFromRowToColumn;
    private Vector vecSelected = new Vector(20,20);
    private boolean bViewOnly;
    private String imageOn, imageOff;
    
    /**
        @param propertyFromRow name of property from row Hub to get to column Hub
    */
    public OARelationshipGrid(Hub hubRow, String rowHeaderProperty, Hub hubColumn, String columnHeaderProperty, String propertyFromRow) {
        this.hubRow = hubRow;
        this.propertyHeaderRow = rowHeaderProperty;

        this.hubColumn = hubColumn;
        this.propertyHeaderColumn = columnHeaderProperty;
        
        this.propertyFromRow = propertyFromRow;
        setup();
    }

    /** creates a grid using images instead of CheckBoxes.
        @see OARelationshipGrid#setOnImage
        @see OARelationshipGrid#setOffImage
    */
    public void setViewOnly(boolean b) {
        bViewOnly = b;
    }
    public boolean getViewOnly() {
        return bViewOnly;
    }
    /** used with setViewOnly(true) to display image instead of CheckBox. */
    public void setOnImage(String s) {
        imageOn = s;
    }
    public String getOnImage() {
        return imageOn;
    }
    /** used with setViewOnly(true) to display image instead of CheckBox. */
    public void setOffImage(String s) {
        imageOff = s;
    }
    public String getOffImage() {
        return imageOff;
    }
    
    protected void setup() {
        propertyPath = propertyHeaderRow;
        if (propertyPath == null) throw new IllegalArgumentException("Row header property cant be null");
        hub = hubRow;
        if (hub == null) throw new IllegalArgumentException("Row hub cant be null");
        resetHubOrProperty();
        methodsHeaderRow = getGetMethods();
        if (methodsHeaderRow == null) throw new IllegalArgumentException("Method for Row header property cant be found");

        propertyPath = propertyHeaderColumn;
        if (propertyPath == null) throw new IllegalArgumentException("Column header property cant be null");
        hub = hubColumn;
        if (hub == null) throw new IllegalArgumentException("Column hub cant be null");
        resetHubOrProperty();
        methodsHeaderColumn = getGetMethods();
        if (methodsHeaderColumn == null) throw new IllegalArgumentException("Method for Column header property cant be found");

        propertyPath = propertyFromRow;
        if (propertyPath == null) throw new IllegalArgumentException("property from row hub to column hub cant be null");
        hub = hubRow;
        resetHubOrProperty();
        methodsFromRowToColumn = getGetMethods();
        if (methodsFromRowToColumn == null) throw new IllegalArgumentException("methods from row hub to column hub cant be found");
        
        close();
    }
    

    public int getColumnCount() {
        return hubColumn.getSize();
    }
    public int getRowCount() {
        return hubRow.getSize();
    }
    
    public String getColumnHeading(int col) {
        Object obj = hubColumn.elementAt(col);
        return ClassModifier.getPropertyValueAsString(obj, methodsHeaderColumn);        
    }
    public String getRowHeading(int row) {
        Object obj = hubRow.elementAt(row);
        return ClassModifier.getPropertyValueAsString(obj, methodsHeaderRow);        
    }
    

    
    /************************** OAHtmlComponent ************************/
    protected void beforeSetValuesInternal() {  // a check box is only submitted if it is checked
        if (!bViewOnly) vecSelected.removeAllElements();
    }

    protected void setValuesInternal(String nameUsed, String[] values) { // called by OAForm
    }
    
    protected String processCommand(OASession session, OAForm form, String command) {
        //    Sring name = "oacommand_5_name_5_3
        if (bViewOnly) return null;
        String s = com.viaoa.html.Util.field(command,'_',4,1);
        int row = 0;
        try {
            row = Integer.parseInt(s);
        }
        catch (Exception e) {
        }

        s = com.viaoa.html.Util.field(command,'_',5,1);
        int col = 0;
        try {
            col = Integer.parseInt(s);
        }
        catch (Exception e) {
        }
        vecSelected.addElement(new int[] { row,col });

        return null;
    }

    
    protected void afterSetValuesInternal() {
        if (bViewOnly) return;
        int x = vecSelected.size();
        for (int i=0; i<x; i++) {
            int[] ii = (int[]) vecSelected.elementAt(i);

            Object objRow = hubRow.elementAt(ii[0]);
            if (objRow == null) continue;
            Object objCol = hubColumn.elementAt(ii[1]);
            if (objCol == null) continue;
            
            Hub hub = (Hub) ClassModifier.getPropertyValue(objRow, methodsFromRowToColumn);
            if (hub == null) continue;
            if (hub.getObject(objCol) == null) {
                hub.add(objCol);
            }
        }
        for (int r=0;;r++) {
            Object objRow = hubRow.elementAt(r);
            if (objRow == null) break;
            Hub hub = (Hub) ClassModifier.getPropertyValue(objRow, methodsFromRowToColumn);
            if (hub == null) continue;
            for (int cc=0; ;cc++) {
                Object objCol = hub.elementAt(cc);
                if (objCol == null) break;
                int c = hubColumn.getPos(objCol);
                if (c < 0) continue;
                boolean b = false;
                for (int i=0; i<x && !b; i++) {
                    int[] ii = (int[]) vecSelected.elementAt(i);
                    if (ii[0] == r && ii[1] == c) b = true;
                }
                if (!b) {
                    hub.remove(cc--);
                }
            }
        }
    }

    public boolean isChanged() {
        //qqqqqq
        return false;
    }
    public boolean needsRefreshed() {
        //qqqqqq
        return false;
    }

    public String getCheckBoxHtml(int row, int col) {
        boolean b = getValue(row,col);
        String s;
        if (bViewOnly) {
            if (b) s = imageOn;
            else s = imageOff;
            s = "<img src=\""+s+"\" border=0>";
        }
        else {
            if (name == null) name = "NoName";
            s = "oacommand_"+name.length()+"_"+name+"_"+row+"_"+col;

            s = "<input type=\"checkbox\" name=\""+s+"\" value=\"true\"";
            if (b) s += " checked";
            s += ">";
        }       
        return s;
    }
    public void setValue(int row, int col, boolean tf) {
        Object objRow = hubRow.elementAt(row);
        Object objCol = hubColumn.elementAt(col);
        if (objRow == null || objCol == null) return;
        
        Hub hub = (Hub) ClassModifier.getPropertyValue(objRow, methodsFromRowToColumn);
        if (hub == null) return;
        if (tf) {
            if (!hub.contains(objCol)) hub.add(objCol);
        }
        else {
            hub.remove(objCol);
        }
            
    }
    
    public void saveAll() {
        hubRow.saveAll();
        hubColumn.saveAll();
    }

    public boolean getValue(int row, int col) {
        Object objRow = hubRow.elementAt(row);
        Object objCol = hubColumn.elementAt(col);
        if (objRow == null || objCol == null) return false;

        Hub hub = (Hub) ClassModifier.getPropertyValue(objRow, methodsFromRowToColumn);
        if (hub == null) return false;

        return (hub.getObject(objCol) != null);
    }
}

