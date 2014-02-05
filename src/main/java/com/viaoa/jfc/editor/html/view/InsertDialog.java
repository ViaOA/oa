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
import com.viaoa.jfc.OARadioButton;
import com.viaoa.jfc.OATextField;
import com.viaoa.jfc.OATree;
import com.viaoa.jfc.OATreeComboBox;
import com.viaoa.jfc.OATreeNode;
import com.viaoa.jfc.editor.html.oa.DocElement;
import com.viaoa.jfc.editor.html.oa.Insert;


/**
 * Dialoge used to insert html, attribute into HTML document.
 * @author vvia
 *
 */
public class InsertDialog extends JDialog {

    protected boolean bCancelled;
    private Hub<Insert> hubInsert;
    private Hub<DocElement> hubDocElement;
    private Hub<DocElement> hubRootDocElement;

    public InsertDialog(Window parent, Hub<Insert> hubInsert, Hub<DocElement> hubRootDocElement, Hub<DocElement> hubDocElement) {
        super(parent, "Inset", ModalityType.APPLICATION_MODAL);
        this.hubInsert = hubInsert;
        this.hubRootDocElement = hubRootDocElement;
        this.hubDocElement = hubDocElement;
        
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        
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
    }
    
    public boolean wasCanceled() {
        return bCancelled;
    }

    
    
    private OATreeComboBox treeComboBox;
    public OATreeComboBox getTreeComboBox() {
        if (treeComboBox == null) {
            OATree tree = new OATree(24, 64);
            OATreeNode node = new OATreeNode("Name", hubRootDocElement, hubDocElement);
            tree.add(node);

            node.add(node, "name");
            
            treeComboBox = new OATreeComboBox(tree, hubDocElement, "name");
            treeComboBox.setColumns(24);
        }
        return treeComboBox;
    }

    
    protected JPanel getPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel pan = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        Insets ins = new Insets(1, 1, 1, 10);
        gc.insets = ins;
        gc.anchor = gc.NORTHWEST;
        JLabel lbl;
        OATextField txt;

                
        pan.add(new JLabel("HTML Element:"), gc);
        gc.gridwidth = gc.REMAINDER;
        pan.add(getTreeComboBox(), gc);
        gc.gridwidth = 1;
        
                
        pan.add(new JLabel("Insert Type:"), gc);

        Box box = new Box(BoxLayout.X_AXIS);
        ButtonGroup bg = new ButtonGroup();

        OARadioButton rad;

        rad = new OARadioButton(hubInsert, Insert.PROPERTY_Type, Insert.TYPE_BR);
        rad.setToolTipText("HTML <br> break");
        rad.setText("Break");
        bg.add(rad);
        box.add(rad);
        box.add(Box.createHorizontalStrut(7));
        
        rad = new OARadioButton(hubInsert, Insert.PROPERTY_Type, Insert.TYPE_P);
        rad.setToolTipText("HTML <p> paragraph");
        rad.setText("Paragraph");
        bg.add(rad);
        box.add(rad);
        box.add(Box.createHorizontalStrut(7));
        
        rad = new OARadioButton(hubInsert, Insert.PROPERTY_Type, Insert.TYPE_DIV);
        rad.setToolTipText("HTML <div> Division");
        rad.setText("Division");
        bg.add(rad);
        box.add(rad);
        box.add(Box.createHorizontalStrut(7));

        gc.gridwidth = gc.REMAINDER;
        pan.add(box, gc);
        gc.gridwidth = 1;

        
        pan.add(new JLabel("Location:"), gc);

        box = new Box(BoxLayout.X_AXIS);
        bg = new ButtonGroup();

        rad = new OARadioButton(hubInsert, Insert.PROPERTY_Location, Insert.LOCATION_Inside);
        rad.setText("Current");
        rad.setToolTipText("Insert at current location");
        bg.add(rad);
        box.add(rad);
        box.add(Box.createHorizontalStrut(7));

        rad = new OARadioButton(hubInsert, Insert.PROPERTY_Location, Insert.LOCATION_Before);
        rad.setText("Before");
        rad.setToolTipText("Insert before current selected tag");
        bg.add(rad);
        box.add(rad);
        box.add(Box.createHorizontalStrut(7));

        rad = new OARadioButton(hubInsert, Insert.PROPERTY_Location, Insert.LOCATION_After);
        rad.setText("After");
        rad.setToolTipText("Insert after current selected tag");
        bg.add(rad);
        box.add(rad);
        box.add(Box.createHorizontalStrut(7));
        
        gc.gridwidth = gc.REMAINDER;
        pan.add(box, gc);
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
        // pan.add(getApplyButton());
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
                    InsertDialog.this.onOk();
                }
            };
            butSelect.addActionListener(al);
            butSelect.registerKeyboardAction(al, "cmdOK", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
            butSelect.setMnemonic('O');
        }
        return butSelect;
    }
    private JButton butApply;
    public JButton getApplyButton() {
        if (butApply == null) {
            butApply = new JButton("Apply");
            ActionListener al = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InsertDialog.this.onApply();
                }
            };
            butApply.addActionListener(al);
            butApply.registerKeyboardAction(al, "cmdApply", KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
            butApply.setMnemonic('A');
        }
        return butApply;
    }
    private JButton butCancel;
    public JButton getCancelButton() {
        if (butCancel == null) {
            butCancel = new JButton("Cancel");
            butCancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InsertDialog.this.onCancel();
                }
            });
            butCancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "cancel");
            butCancel.getActionMap().put("cancel",
                    new AbstractAction() {
                        public void actionPerformed(ActionEvent e) {
                            InsertDialog.this.onCancel();
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
    protected void onApply() {
    }
    

    public static void main(String[] args) {
        InsertHyperlinkDialog dlg = new InsertHyperlinkDialog(null);
        dlg.setVisible(true);
        
    }
}
