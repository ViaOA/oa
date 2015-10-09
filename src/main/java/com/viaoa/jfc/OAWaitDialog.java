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
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

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
        
        JPanel pan = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(1, 3, 1, 3);
        gc.anchor = gc.CENTER;
        
        gc.fill = gc.NONE;
        gc.weightx = .25f;
        pan.add(new JLabel(""), gc);
        
        gc.fill = gc.HORIZONTAL;
        gc.weightx = .50f;
        pan.add(getProgressBar(), gc);
        
        gc.gridwidth = gc.REMAINDER;
        gc.fill = gc.NONE;
        gc.weightx = .25f;
        pan.add(new JLabel(""), gc);

        panel.add(pan, BorderLayout.SOUTH);
        return panel;
    }
    
    private boolean bCancelled;
    public boolean wasCancelled() {
        return bCancelled;
    }
    
    @Override
    public void setVisible(boolean b) {
        setVisible(b, b);
    }
    public void setVisible(boolean b, boolean bShowProcessing) {
        if (b) {
            bDone = false;
            bCancelled = false;
            pack();
            this.setLocationRelativeTo(parent);
        }
        setCursor(Cursor.getPredefinedCursor(bShowProcessing?Cursor.WAIT_CURSOR:Cursor.DEFAULT_CURSOR));
        getProgressBar().setIndeterminate(bShowProcessing);
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
        
        JScrollPane scrollPane = new JScrollPane(con);
        scrollPane.setBorder(new TitledBorder("Console"));
        
        if (console != null) {
            if (compDisplay != null) {
                JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, compDisplay, scrollPane);
                getContentPane().add(splitPane, BorderLayout.CENTER);
            }
            else getContentPane().add(scrollPane, BorderLayout.CENTER);
            setResizable(true);
        }
    }

    private JComponent compDisplay;
    public void setDisplayComponent(JComponent pan) {
        this.compDisplay = pan;
        if (pan != null) {
            if (console != null) {
                JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, compDisplay, new JScrollPane(console));
                getContentPane().add(sp, BorderLayout.CENTER);
            }
            else getContentPane().add(compDisplay, BorderLayout.CENTER);
            setResizable(true);
        }
    }
    public JComponent getDisplayComponent() {
        return compDisplay;
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
                    for (int i=0; i<180;i++) {
                        updateObject.setText(i+" "+OAString.getRandomString(5, 75, true, true, true));
                        Thread.sleep(350);
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


