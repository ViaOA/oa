// Copied from OATemplate project by OABuilder 09/21/15 03:11 PM
package com.viaoa.jfc.dialog;


import java.awt.*;
import java.awt.event.*;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.*;

import com.viaoa.jfc.*;
import com.viaoa.util.OAString;

public class OAConfirmDialog extends JDialog {

    private String msg;
    private JButton cmdOk;
    private JButton cmdCancel;
	
    public OAConfirmDialog(Window parent, String title, String msg) {
        super(parent, title==null?"":title, ModalityType.APPLICATION_MODAL); 
        this.msg = msg;

        this.setResizable(true);
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
        getContentPane().add(p, BorderLayout.NORTH);

        resize();
    }    

    
    public void resize() {
        pack();

        Dimension d = getSize();
        d.width += 25;
        d.height += 20;
        setSize(d);
        
        setLocationRelativeTo(getParent());
    }
    
    private boolean bWasCancelled;
    public boolean wasCancelled() {
        return bWasCancelled;
    }
    
    
    @Override
    public void setVisible(boolean b) {
        if (b) bWasCancelled = true;
        super.setVisible(b);
    }
   
    private JLabel jlbl;
    
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
        
        // Password
        jlbl = new JLabel(msg);
        
        gc.gridwidth = GridBagConstraints.REMAINDER; 
        pan.add(jlbl,gc);
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
    
    

    public void onOk() {
        setVisible(false);
    }
    public void onCancel() {
        bWasCancelled = true;
        setVisible(false);
    }
    
    
    public static void main(String[] args) {
                
        OAConfirmDialog dlg = new OAConfirmDialog(null, "title", "message");
        
        dlg.add(new JButton("adfadfadfs"), BorderLayout.SOUTH);
        
        dlg.setVisible(true);
        System.exit(0);
    }

    
}
