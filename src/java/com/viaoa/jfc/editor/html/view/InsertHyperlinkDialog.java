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


public class InsertHyperlinkDialog extends JDialog {

    protected boolean bCancelled;
    private JTextField txt;

    public InsertHyperlinkDialog(Window parent) {
        super(parent, "Insert web page link", ModalityType.APPLICATION_MODAL);
        
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
        
        pan.add(new JLabel("Web Page URL:"), gc);
        gc.fill = gc.HORIZONTAL;
        pan.add(getTextField(), gc);
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
                    InsertHyperlinkDialog.this.onOk();
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
                    InsertHyperlinkDialog.this.onCancel();
                }
            });
            butCancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "cancel");
            butCancel.getActionMap().put("cancel",
                    new AbstractAction() {
                        public void actionPerformed(ActionEvent e) {
                            InsertHyperlinkDialog.this.onCancel();
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
    
    public JTextField getTextField() {
        if (txt == null) {
            txt = new JTextField(18);
        }
        return txt;
    }    

    public static void main(String[] args) {
        InsertHyperlinkDialog dlg = new InsertHyperlinkDialog(null);
        dlg.setVisible(true);
        
    }
}
