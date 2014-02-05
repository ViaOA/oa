/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.jsp;

import com.viaoa.hub.Hub;

/**
 * Used for an HTML select, with attribute multiple.
 *
 */
public class OAList extends OACombo {

    protected int rows;
    public OAList(String id, Hub hub, String propertyPath, int rows, int columns) {
        super(id, hub, propertyPath, columns);
        this.rows = rows;
    }
    
    public void setSelectHub(Hub<?> hubSelect) {
        this.hubSelect = hubSelect;
    }
    
    public int getRows() {
        return rows;
    }
    public void setRows(int rows) {
        this.rows = rows;
    }
    
    @Override
    public String getScript() {
        String s = super.getScript();
        if (rows > 0) {
            s += "$('#"+id+"').attr('size', '"+getRows()+"');\n";
            if (hubSelect != null) s += "$('#"+id+"').attr('multiple', 'multiple');\n";
            else s += "$('#"+id+"').removeAttr('multiple');\n";
        }
        return s;
    }
}
