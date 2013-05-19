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
