package com.viaoa.jfc.text;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import java.net.URL;
import com.viaoa.jfc.*;

public abstract class TextAreaMergeDialog extends JDialog {

    private JTextArea taOrig, taCurrent, taLocal, taNewValue;
    private boolean bCancel;
    private JPanel pan;

    public TextAreaMergeDialog(JFrame frame, String title) {
        super(frame, title, true); 
        this.setResizable(true);
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        this.addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent e) {
        		super.windowClosing(e);
        		onClose();
        	}
        });

        getContentPane().setLayout(new BorderLayout());
        
        getContentPane().add(getPanel(), BorderLayout.CENTER);
        
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 2,2));
        p.add(getOkButton());
        getContentPane().add(p, BorderLayout.SOUTH);

        
        pack();

        setLocationRelativeTo(frame);
    }    

    protected JPanel getPanel() {
    	if (pan != null) return pan;
    	
        pan = new JPanel(new GridLayout(2,2,5,5));
        pan.setBorder(new EmptyBorder(5,5,5,5));
    	JPanel p;
    	JLabel lbl;
    	JTextArea ta;
    	Border b;
        
    	
    	ta = new JTextArea(8, 25);
    	ta.setWrapStyleWord(true);
    	ta.setLineWrap(true);
        b = new CompoundBorder(new TitledBorder("Original Value"), new EmptyBorder(5,5,5,5));
        ta.setToolTipText("value before you began your changes.");
    	taOrig = ta;
    	JScrollPane sp = new JScrollPane(ta);
    	sp.setBorder(b);
        pan.add(sp);

        
        ta = new JTextArea(8, 25);
        ta.setWrapStyleWord(true);
        ta.setLineWrap(true);
        b = new CompoundBorder(new TitledBorder("Current Value"), new EmptyBorder(5,5,5,5));
        ta.setToolTipText("value after another user has changed it.");
        taCurrent = ta;
        sp = new JScrollPane(ta);
        sp.setBorder(b);
        pan.add(sp);

        
        ta = new JTextArea(8, 25);
        ta.setWrapStyleWord(true);
        ta.setLineWrap(true);
        b = new CompoundBorder(new TitledBorder("Your Changes"), new EmptyBorder(5,5,5,5));
        // ta.setToolTipText("your changes.");
        taLocal = ta;
        sp = new JScrollPane(ta);
        sp.setBorder(b);
        pan.add(sp);

        
        ta = new JTextArea(8, 25);
        ta.setWrapStyleWord(true);
        ta.setLineWrap(true);
        taNewValue = ta;
        ta.setToolTipText("new value to use.");
        
        p = new JPanel(new GridLayout(1,3,1,0));
        JButton cmd = new JButton("Use original");
        cmd.setToolTipText("use the original text, before anyone made a change");
        OAButton.setup(cmd);
        cmd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                taNewValue.setText(taOrig.getText());
            }
        });
        p.add(cmd);
        cmd = new JButton("Use current");
        cmd.setToolTipText("use the current text, set while you were editing");
        OAButton.setup(cmd);
        cmd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                taNewValue.setText(taCurrent.getText());
            }
        });
        p.add(cmd);
        cmd = new JButton("Use your changes");
        cmd.setToolTipText("use your changes to the text");
        OAButton.setup(cmd);
        cmd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                taNewValue.setText(taLocal.getText());
            }
        });
        p.add(cmd);

        
        JPanel px = new JPanel(new BorderLayout(0,0));
        px.add(new JScrollPane(ta), BorderLayout.CENTER);
        px.add(p, BorderLayout.SOUTH);

        b = new CompoundBorder(new TitledBorder("New value to use"), new EmptyBorder(5,5,5,5));
        px.setBorder(b);
        
        pan.add(px);
        
        
    	return pan;
    }

    public JTextArea getOriginalTextArea() {
        return taOrig;
    }
    public JTextArea getCurrentTextArea() {
        return taCurrent;
    }
    public JTextArea getLocalTextArea() {
        return taLocal;
    }
    public JTextArea getNewValueTextArea() {
        return taNewValue;
    }
    

    private JButton cmdOk;
    public JButton getOkButton() {
        if (cmdOk == null) {
            cmdOk = new JButton("Ok");
            cmdOk.setMnemonic(KeyEvent.VK_O);
            cmdOk.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onOk();
                }
            });
        }
        return cmdOk;
    }

    
    protected abstract void onOk();
    protected abstract void onClose();
    
    
    public static void main(String[] args) {
		TextAreaMergeDialog dlg = new TextAreaMergeDialog(null, "title") {
			@Override
			public void onOk() {
				System.exit(0);
			}
			@Override
			protected void onClose() {
                System.exit(0);
			}
		};
		dlg.setVisible(true);
	}
    
}
