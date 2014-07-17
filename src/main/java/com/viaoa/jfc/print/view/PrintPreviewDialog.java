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
package com.viaoa.jfc.print.view;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.net.URL;

import javax.swing.*;

import com.viaoa.jfc.*;
import com.viaoa.jfc.print.*;

/**
 * Print preview dialog used to view any printable.
 * @author vvia
 *
 */
public abstract class PrintPreviewDialog extends JDialog implements ActionListener {
	
	public static final String CMD_Close      = "close";
	public static final String CMD_Print      = "print";
	public static final String CMD_PrintSetup = "printSetup";
	
	private JComboBox cboScale;
	private PreviewPanel panPreview;
	private String[] scales;
	
	public PrintPreviewDialog(Window parentWindow, String[] scales) {
		super(parentWindow, "", ModalityType.MODELESS);
		this.scales = scales;
		setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	onClose();
            }
        });
		setup();
        pack();
        this.setLocationRelativeTo(parentWindow);
 	}
	
    protected void setup() {
		JToolBar toolbar = new JToolBar();
		toolbar.setMargin(new Insets(2,5,2,2));
		toolbar.setBorderPainted(true);
        toolbar.setFloatable(false);

		JButton cmd = new JButton(" Print ... ");
		OAButton.setup(cmd);
        URL url = PrintController.class.getResource("view/image/print.gif");
        ImageIcon icon = new ImageIcon(url);
		cmd.setIcon(icon);
		cmd.setToolTipText("Send to Printer");
		cmd.setMnemonic('P');
		cmd.setActionCommand(CMD_Print);
		cmd.addActionListener(this);
        cmd.registerKeyboardAction(this, CMD_Print, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK, true), JComponent.WHEN_IN_FOCUSED_WINDOW);
		toolbar.add(cmd);

		cmd = new JButton(" Page Setup ... ");
		OAButton.setup(cmd);
        url = PrintController.class.getResource("view/image/pageSetup.gif");
        icon = new ImageIcon(url);
        cmd.setIcon(icon);
		cmd.setToolTipText("Change/View Page Settings");
		cmd.setMnemonic('S');
		cmd.setActionCommand(CMD_PrintSetup);
        cmd.registerKeyboardAction(this, CMD_PrintSetup, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK, true), JComponent.WHEN_IN_FOCUSED_WINDOW);
        cmd.addActionListener(this);
		toolbar.add(cmd);


		toolbar.addSeparator();
		toolbar.add(getScaleComboBox());


		cmd = new JButton(" Close ");
		OAButton.setup(cmd);
		cmd.setToolTipText("Close without Printing");
		cmd.setMnemonic((int) 'C');
		cmd.setActionCommand(CMD_Close);
		cmd.addActionListener(this);
        cmd.registerKeyboardAction(this, CMD_Close, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true), JComponent.WHEN_IN_FOCUSED_WINDOW);
        cmd.registerKeyboardAction(this, CMD_Close, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, true), JComponent.WHEN_IN_FOCUSED_WINDOW);
		toolbar.add(cmd);

		getContentPane().add(toolbar, BorderLayout.NORTH);

    	getContentPane().add(new JScrollPane(getPreviewPanel()));
	}

	public PreviewPanel getPreviewPanel() {
		if (panPreview == null) panPreview = new PreviewPanel();
		return panPreview;
	}
    
    public JComboBox getScaleComboBox() {
		if (cboScale != null) return cboScale;
		cboScale = new JComboBox( scales );
		cboScale.setMaximumSize(new Dimension(72, 23));
		return cboScale;
	}
    
	public void actionPerformed(ActionEvent e) {
	    String cmd = e.getActionCommand();
	    if (cmd == null) return;
	    if (cmd.equals(CMD_Close)) onClose();
	    else if (cmd.equals(CMD_Print)) onPrint(); 
	    else if (cmd.equals(CMD_PrintSetup)) onPageSetup();
	}

	protected abstract void onClose();
	protected abstract void onPrint();
	protected abstract void onPageSetup();
}


