// Copied from OATemplate project by OABuilder 09/21/15 03:11 PM
package com.viaoa.jfc.dialog;


import java.awt.*;
import java.awt.event.*;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.*;

import com.viaoa.jfc.*;
import com.viaoa.util.OAString;

public abstract class OAPasswordDialog extends JDialog {

	private JPasswordField txtPassword;
    private JButton cmdOk;
    private JButton cmdCancel;
	
    public OAPasswordDialog(Window parent, String title) {
        super(parent, title==null?"":title, ModalityType.APPLICATION_MODAL); 

        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        this.addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent e) {
        		super.windowClosing(e);
        		onCancel();
        	}
        });

        getContentPane().setLayout(new BorderLayout());
        
        JPanel p = getPanel();
        p.setBorder(new TitledBorder(""));
        getContentPane().add(p, BorderLayout.CENTER);

        JLabel lbl = getStatusLabel();
        getContentPane().add(lbl, BorderLayout.SOUTH);

        
        pack();

        Dimension d = getSize();
        d.width += 25;
        d.height += 20;
        setSize(d);
        
        setLocationRelativeTo(parent);
    }    

    private boolean bWasCancelled;
    public boolean wasCancelled() {
        return bWasCancelled;
    }
    
    
    @Override
    public void setVisible(boolean b) {
        if (b) bWasCancelled = true;
        getStatusLabel().setText("Enter password");
        getStatusLabel().setForeground(Color.gray);
        getStatusLabel().setIcon(null);
        getPasswordTextField().setText("");
        if (b) getPasswordTextField().requestFocusInWindow();
        super.setVisible(b);
    }
   
    
    private JPanel pan;
    protected JPanel getPanel() {
    	if (pan != null) return pan;
    	
        pan = new JPanel(new GridBagLayout());
    	pan.setBorder(new EmptyBorder(5,5,5,15));
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;         
        gc.insets = new Insets(2,10,0,0);
        JLabel jlbl;
        OALabel lbl;
        OATextField txt;
        
        // Password
        jlbl = new JLabel("Password:");
        pan.add(jlbl,gc);
        
        gc.gridwidth = GridBagConstraints.REMAINDER; 
        pan.add(getPasswordTextField(), gc);
        gc.gridwidth = 1;

        JPanel panCommand = new JPanel();

        panCommand.add(getOkButton());

        JButton cmd = new JButton("Cancel");
        ActionListener al = new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        	    bWasCancelled = true;
        		onCancel();
        	}
        };
        cmd.addActionListener(al);
        cmd.registerKeyboardAction(al, "cmdCancel", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
        // cmd.setMnemonic('Q');
        panCommand.add(cmd);

        gc.insets = new Insets(15,10,0,0);
        gc.gridwidth = GridBagConstraints.REMAINDER; 
        gc.anchor = GridBagConstraints.CENTER;
        pan.add(panCommand, gc);
        gc.gridwidth = 1;
        return pan;
    }

    public JButton getOkButton() {
        if (cmdOk == null) {
            cmdOk = new JButton("OK");
            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    bWasCancelled = false;
                    onOk();
                }
            };
            cmdOk.addActionListener(al);
            cmdOk.registerKeyboardAction(al, "cmdOK", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
            cmdOk.setMnemonic('O');
        }
        return cmdOk;
    }
    
    public JPasswordField getPasswordTextField() {
        if (txtPassword == null) txtPassword = new JPasswordField(12);
        return txtPassword;
    }
    
    private JLabel lblStatus;
    public JLabel getStatusLabel() {
        if (lblStatus == null) {
            lblStatus = new JLabel("", JLabel.CENTER);
            
            lblStatus.setBorder(new CompoundBorder(new TitledBorder(""), new EmptyBorder(1, 15, 1, 5)));
        }
        return lblStatus;
    }

    public void onOk() {
        if (isValidPassword()) setVisible(false);
        else {
            getStatusLabel().setText("Invalid password");
            getStatusLabel().setForeground(Color.BLACK);
            
            URL url = OAButton.class.getResource("icons/error20.png");
            if (url != null) { 
                getStatusLabel().setIcon(new ImageIcon(url));
            }
            
            getPasswordTextField().requestFocusInWindow();
            getPasswordTextField().selectAll();
        }
    }
    public void onCancel() {
        bWasCancelled = true;
        setVisible(false);
    }
    
    public boolean isValidPassword() {
        String s = getPasswordTextField().getText();
        
        s = OAString.getSHAHash(s);
        
        return isValidPassword(s); 
    }

    /**
     * Called to verify the password.
     * 
     * @param pw will be the OAString.getSHAHash(pw) value
     * 
     * @see OAString#getSHAHash(String)
     */
    protected abstract boolean isValidPassword(String pw);
    
    public static void main(String[] args) {
        
        OAPasswordDialog dlg = new OAPasswordDialog(null, "hey") {
            @Override
            public boolean isValidPassword(String pw) {
                // TODO Auto-generated method stub
                return false;
            }
        };
        
        dlg.setVisible(true);
        System.exit(0);
    }
    
}
