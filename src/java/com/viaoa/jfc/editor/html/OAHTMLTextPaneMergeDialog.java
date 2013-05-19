package com.viaoa.jfc.editor.html;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import java.net.URL;
import com.viaoa.jfc.*;

/**
 * Dialog used to edit concurrent changes to HTMLEditor, so that user
 * can select which version to save, or merge.
 * @author vvia
 *
 */
public abstract class OAHTMLTextPaneMergeDialog extends JDialog {

    private OAHTMLTextPane taOrig, taCurrent, taLocal, taNewValue;
    private boolean bCancel;
    private JPanel pan;

    public OAHTMLTextPaneMergeDialog(Window parent, String title) {
        super(parent, title, Dialog.ModalityType.APPLICATION_MODAL); 
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
        
        JLabel lbl = new JLabel("<html>The text was changed by another user while you were making changes.");
        lbl.setBorder(new CompoundBorder(new EmptyBorder(5,5,5,5), new TitledBorder("")));
        getContentPane().add(lbl, BorderLayout.NORTH);
        
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 2,2));
        p.add(getOkButton());
        getContentPane().add(p, BorderLayout.SOUTH);

        
        pack();
        Dimension d = this.getSize();
        this.setSize(d.width, (int)(d.height * 1.45));

        setLocationRelativeTo(parent);
    }    

    protected JPanel getPanel() {
    	if (pan != null) return pan;
    	
        pan = new JPanel(new GridLayout(2,2,5,5));
        pan.setBorder(new EmptyBorder(5,5,5,5));
    	JPanel p;
    	JLabel lbl;
    	OAHTMLTextPane ta;
    	Border b;
        
    	
    	ta = new OAHTMLTextPane();
        b = new CompoundBorder(new TitledBorder("Original Value"), new EmptyBorder(5,5,5,5));
        ta.setToolTipText("value before you began your changes.");
    	taOrig = ta;
    	JScrollPane sp = new JScrollPane(ta);
        sp.setBorder(b);
        pan.add(sp);

        
        ta = new OAHTMLTextPane();
        b = new CompoundBorder(new TitledBorder("Current Value"), new EmptyBorder(5,5,5,5));
        ta.setToolTipText("value after another user has changed it.");
        taCurrent = ta;
        sp = new JScrollPane(ta);
        sp.setBorder(b);
        pan.add(sp);

        
        ta = new OAHTMLTextPane();
        b = new CompoundBorder(new TitledBorder("Your Changes"), new EmptyBorder(5,5,5,5));
        // ta.setToolTipText("your changes.");
        taLocal = ta;
        sp = new JScrollPane(ta);
        sp.setBorder(b);
        pan.add(sp);

        
        ta = new OAHTMLTextPane();
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

    public OAHTMLTextPane getOriginalTextArea() {
        return taOrig;
    }
    public OAHTMLTextPane getCurrentTextArea() {
        return taCurrent;
    }
    public OAHTMLTextPane getLocalTextArea() {
        return taLocal;
    }
    public OAHTMLTextPane getNewValueTextArea() {
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
		OAHTMLTextPaneMergeDialog dlg = new OAHTMLTextPaneMergeDialog(null, "title") {
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
