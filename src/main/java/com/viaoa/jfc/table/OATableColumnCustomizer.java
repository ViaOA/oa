/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.jfc.table;

import javax.swing.JLabel;

import com.viaoa.hub.Hub;
import com.viaoa.jfc.OATable;
import com.viaoa.object.OAObject;

/** 
    Used by OATableColumn for customizing table column
    
    see: OATable.add(..)
*/
public class OATableColumnCustomizer {
    
    private OATableColumn tc;
    
    public OATable getTable() {
        if (tc == null) return null;
        return tc.table;
    }
    
    /**
     * Table hub
     */
    public Hub getHub() {
        if (tc != null && tc.table != null) return tc.table.getHub();
        return null;
    }
    
    public OATableColumn getTableColumn() {
        return tc;
    }
    
    public void setup(OATableColumn tc) {
        this.tc = tc;
    }

    
    /**
     * get the "normalized" table row that this column is based on.
     * This is usually table.hub(row), but it could be that this column
     * uses a hub that is a detail hub from the table.hub, or a linked hub to the table.hub
     * The code calling this needs to get the same class that it was set up for.
     *      ex: a table using hub of emps, with a dept.name column
     *          where: dept.name could be a label used directly from hubEmps as lbl(hubEmps, "dept.name") => getRow(.) should will an Emp
     *              or:  created using a detail hub, hubDept = emps.getDetails("dept"), and then lbl(hubDept, "name")  => getRow(.) will return a Dept
     */
    public Object getRow(int row) {
        if (row < 0) return null;
        if (tc == null) return null;
        Object obj = getHub().getAt(row);
        obj = tc.getNormalizedRow(obj);
        return obj;
    }

    /**
     * This will be called after the default OATable settings are set for the cell, and before the
     * OATable.customizeRenderer is called.
     */
    public void customizeRenderer(JLabel label, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean wasChanged, boolean wasMouseOver) {
    }

    /**
     * Mouse over popup tooltip
     * @param object
     * @param row if < 0, then it is for the heading's tooltip.
     * @param defaultValue
     * @return
     */
    public String getToolTipText(Object object, int row, String defaultValue) {
        return defaultValue;
    }

}


