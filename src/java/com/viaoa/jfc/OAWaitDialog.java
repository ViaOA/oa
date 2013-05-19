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

import com.viaoa.jfc.*;


public class OAWaitDialog extends JDialog implements ActionListener {

	private JLabel lblStatus;
    private JButton cmdCancel;
    private JProgressBar progressBar;
    private boolean bAllowCancel;
    private Window parent;

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
        getContentPane().add(getPanel());
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
    
    public static void main(String[] args) {
        OAWaitDialog dlg = new OAWaitDialog(null);
        dlg.setTitle("Wait for me");
        dlg.getStatusLabel().setText("this is a wait dialog");
        dlg.setVisible(true);
    }
}


