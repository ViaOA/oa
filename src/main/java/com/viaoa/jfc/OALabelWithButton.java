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
