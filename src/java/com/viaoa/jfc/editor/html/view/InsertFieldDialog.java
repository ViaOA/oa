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
package com.viaoa.jfc.editor.html.view;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.viaoa.hub.Hub;
import com.viaoa.jfc.OAComboBox;
import com.viaoa.jfc.OATextField;
import com.viaoa.jfc.OATreeComboBox;
import com.viaoa.jfc.propertypath.OAPropertyPathTree;
import com.viaoa.jfc.propertypath.model.oa.ObjectDef;


public class InsertFieldDialog extends JDialog {

    protected boolean bCancelled;
    private Hub<ObjectDef> hub;
    private OATextField txt;
    private OATreeComboBox cbo;

    public InsertFieldDialog(Window parent, Hub<ObjectDef> hub) {
        super(parent, "Insert Field", ModalityType.APPLICATION_MODAL);

        this.hub = hub;
        
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setResizable(true);
        setLayout(new BorderLayout());
        add(getPanel(), BorderLayout.CENTER);
        
        this.pack();

        this.setLocationRelativeTo(parent);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            }
            @Override
            public void windowOpened(WindowEvent e) {
            }
        });
    }

    public void setVisible(boolean b) {
        if (b) bCancelled = true;
        super.setVisible(b);
        if (b) txt.requestFocus();
    }
    
    public boolean wasCanceled() {
        return bCancelled;
    }

    
    protected JPanel getPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel pan = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        Insets ins = new Insets(1, 1, 1, 10);
        gc.insets = ins;
        gc.anchor = gc.NORTHWEST;
        JLabel lbl;
        
        pan.add(new JLabel("Field:"), gc);
        gc.fill = gc.HORIZONTAL;
        pan.add(getComboBox(), gc);
        gc.fill = gc.NONE;
        gc.gridwidth = gc.REMAINDER;
        pan.add(new JLabel(""), gc);
        gc.gridwidth = 1;
        
        // bottom
        gc.gridwidth = gc.REMAINDER;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        pan.add(new JLabel(""), gc);

        panel.add(pan, BorderLayout.NORTH);

        Border border = new CompoundBorder(new EmptyBorder(5,5,5,5), new TitledBorder(""));
        border = new CompoundBorder(border, new EmptyBorder(5,5,5,5));
        pan.setBorder(border);
        panel.add(pan, BorderLayout.NORTH);
        
        
        pan = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pan.add(getOkButton());
        pan.add(getCancelButton());
        pan.setBorder(border);
        panel.add(pan, BorderLayout.SOUTH);
        
        
        return panel;
    }
    
    private JButton butSelect;
    public JButton getOkButton() {
        if (butSelect == null) {
            butSelect = new JButton("Ok");
            ActionListener al = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InsertFieldDialog.this.onOk();
                }
            };
            butSelect.addActionListener(al);
            butSelect.addActionListener(al);
            butSelect.registerKeyboardAction(al, "cmdOK", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
            butSelect.setMnemonic('O');
        }
        return butSelect;
    }
    private JButton butCancel;
    public JButton getCancelButton() {
        if (butCancel == null) {
            butCancel = new JButton("Cancel");
            butCancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InsertFieldDialog.this.onCancel();
                }
            });
            butCancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "cancel");
            butCancel.getActionMap().put("cancel",
                    new AbstractAction() {
                        public void actionPerformed(ActionEvent e) {
                            InsertFieldDialog.this.onCancel();
                        }
                    });
        }
        return butCancel;
    }

    protected void onCancel() {
        bCancelled = true;
        setVisible(false);
    }
    protected void onOk() {
        bCancelled = false;
        setVisible(false);
    }
    
    public OATextField getTextField() {
        if (txt == null) {
            txt = new OATextField();
            txt.setColumns(20);
        }
        return txt;
    }    

    private OAPropertyPathTree tree;
    public OAPropertyPathTree getPropertyPathTree() {
        if (tree == null) {
            tree = new OAPropertyPathTree(hub, false, true, true, false, true) {
                @Override
                public void propertyPathCreated(String propertyPath) {
                    getTextField().setText(propertyPath);
                    cbo.hidePopup();
                }
            };
        }
        return tree;
    }
    public JComboBox getComboBox() {
        if (cbo == null) {
            cbo = new OATreeComboBox(getPropertyPathTree(), hub, "name");
            cbo.setEditor(getTextField());
        }
        return cbo;
    }
    
    public static void main(String[] args) {
        Hub<ObjectDef> hub = new Hub<ObjectDef>(ObjectDef.class);
        InsertFieldDialog dlg = new InsertFieldDialog(null, hub);
        dlg.setVisible(true);
    }
}
