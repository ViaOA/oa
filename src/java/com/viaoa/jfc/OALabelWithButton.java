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
package com.viaoa.jfc;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.TableCellEditor;

import com.viaoa.hub.Hub;
import com.viaoa.jfc.border.CustomLineBorder;
import com.viaoa.jfc.table.*;

public class OALabelWithButton extends OALabel {
    private JButton button;
    private OALabelWithButtonTableCellEditor tableCellEditor;

    
    public OALabelWithButton(Hub hub, String propertyPath) {
        super(hub, propertyPath);
        setup();
    }
    
    protected void setup() {
        setLayout(new BorderLayout(3, 0));
        button = new JButton("...");
        
        // button.setBorderPainted(false);
        // button.setContentAreaFilled(false);
        // button.setMargin(new Insets(1,1,1,1));
        
        //OAButton.setup(button);
        
        button.setFocusPainted(false);
     
        button.setMargin(new Insets(1,4,1,4));
        
        add(button, BorderLayout.EAST);
        
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OALabelWithButton.this.onButtonClick();
            }
        });
    }
    
    
    public TableCellEditor getTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new OALabelWithButtonTableCellEditor(this);
        }
        return tableCellEditor;
    }
    
    public JButton getButton() {
        return button;
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        button.setEnabled(b);
    }
    
    /**
     * Called when button is clicked.
     */
    public void onButtonClick() {
    }
    
}
