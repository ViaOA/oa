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

package com.viaoa.jfc.table;

import java.awt.*;
import com.viaoa.jfc.*;


/**
    Implemented by OATable listeners to be able to customize
    the renderer component for a table cell.
*/
public interface OATableListener {

    /** 
        Called by OATable to render a table cell at column/row.
        @param value is value of cell.
    */
    public Component getTableCellRendererComponent(Component comp, OATable table, Object value,
                          boolean isSelected, boolean hasFocus,
                          int row, int column);
    
}
