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
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import java.net.URL;
import java.util.*;

import com.viaoa.hub.Hub;
import com.viaoa.jfc.*;
import com.viaoa.jfc.console.Console;
import com.viaoa.util.OAString;


public class OAWaitDialog extends JDialog implements ActionListener {

	private JLabel lblStatus;
    private JButton cmdCancel;
    private JProgressBar progressBar;
    private boolean bAllowCancel;
    private Window parent;
    private OAConsole console;

    public OAWaitDialog(Window parent) {
        this(parent, true);
    }    
    public OAWaitDialog(Window parent, boolean bAllowCancel) {
    	super(parent, "", ModalityType.APPLICATION_MODAL);
    	this.parent = parent;
    	this.bAllowCancel = bAllowCancel;
        this.setResizable(false);
        
        // create window without decoration
        setUndecorated(true);
        // use the root pan decoration
        getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
        
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        if (bAllowCancel) {
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    getCancelButton().doClick();
                }
            });
        }
        getContentPane().setLayout(new BorderLayout(2,2));
        getContentPane().add(getPanel(), BorderLayout.NORTH);
    }    
    
    public void setStatus(String msg) {
        getStatusLabel().setText(msg);
    }
    
	public JLabel getStatusLabel() {
		if (lblStatus == null) {
			lblStatus = new JLabel("   Please wait ... processing request ....   ");
			lblStatus.setHorizontalAlignment(JLabel.CENTER);
		}
		return lblStatus;
	}

    public JProgressBar getProgressBar() {
        if (progressBar == null) {
            progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 20);
        }
        return progressBar;
    }
	
    public JButton getCancelButton() {
    	if (cmdCancel == null) {
    	    cmdCancel = new JButton("Cancel");
    	    cmdCancel.registerKeyboardAction(this, "cancel", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
    	    cmdCancel.setActionCommand("cancel");
    	    cmdCancel.addActionListener(this);
    	}
    	return cmdCancel;
    }
	
    protected JPanel getPanel() {
    	JPanel panel = new JPanel(new BorderLayout(2,2));

        Border border;
        border = new EmptyBorder(5,5,5,5);
        panel.setBorder(border);

        JPanel pan2 = new JPanel(new BorderLayout(2,2));
        border = new EmptyBorder(25,5,25,5);
        pan2.setBorder(border);
        pan2.add(getStatusLabel(), BorderLayout.CENTER);
        panel.add(pan2, BorderLayout.NORTH);
        
        if (bAllowCancel) {
            pan2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
            border = new EmptyBorder(1,1,15,1);
            pan2.setBorder(border);
            pan2.add(getCancelButton());
            panel.add(pan2, BorderLayout.CENTER);
        }
        panel.add(getProgressBar(), BorderLayout.SOUTH);
        
        return panel;
    }
    
    private boolean bCancelled;
    public boolean wasCancelled() {
        return bCancelled;
    }
    
    @Override
    public void setVisible(boolean b) {
        if (b) {
            bDone = false;
            bCancelled = false;
            pack();
            this.setLocationRelativeTo(parent);
        }
        setCursor(Cursor.getPredefinedCursor(b?Cursor.WAIT_CURSOR:Cursor.DEFAULT_CURSOR));
        getProgressBar().setIndeterminate(b);
        super.setVisible(b);  // this will put it in blocking mode
    }
    
    public void actionPerformed(ActionEvent e) {
    	if (e == null) return;
        String cmd = e.getActionCommand();
    	if (cmd == null) return;
        if (cmd.equalsIgnoreCase("cancel")) {
            bCancelled = true;
            setVisible(false);
            /*
            int x = JOptionPane.showConfirmDialog(this, "Ok to cancel", "", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (x == JOptionPane.YES_OPTION) {
                bCancelled = true;
                setVisible(false);
            }
            */
        }
    }
    public void setConsole(OAConsole con) {
        this.console = con;
        if (console != null) {
            getContentPane().add(new JScrollPane(console), BorderLayout.CENTER);
            setResizable(true);
        }
    }

    private boolean bDone;
    public void done() {
        bDone = true;
    }
    
    @Override
    public void setCursor(Cursor cursor) {
        if (bDone && cursor.equals(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR))) cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        super.setCursor(cursor);
    }
    
    
    public static void main(String[] args) {
        final OAWaitDialog dlg = new OAWaitDialog(null);
        dlg.setTitle("Wait for me");
        dlg.getStatusLabel().setText("this is a wait dialog");

        Hub<Console> h = new Hub(Console.class);
        final Console updateObject = new Console();
        updateObject.setText("");
        h.add(updateObject);
        h.setAO(updateObject);
        
        OAConsole oac = new OAConsole(h, "text", 45);
        dlg.setConsole(oac);

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    for (int i=0; i<20;i++) {
                        updateObject.setText(i+" "+OAString.getRandomString(5, 55, true, true, true));
                        Thread.sleep(300);
                    }
dlg.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
               }
                catch (Exception e) {
                    // TODO: handle exception
                }
            }
        };
        t.start();
        dlg.setVisible(true);
        System.exit(0);
    }
    
}


