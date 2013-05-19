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
import java.awt.event.*;

import javax.swing.*;
import com.viaoa.jfc.*;

/**
    Editor used by OACheckBox when it is used for an OATable column.
*/
public class OACheckBoxTableCellEditor extends OATableCellEditor {
    OACheckBox vcb;
   
    public OACheckBoxTableCellEditor(OACheckBox cb) {
        super(cb);
        this.vcb = cb;
    }
    public Object getCellEditorValue() {
        return new Boolean( vcb.isSelected() );
	}

}

