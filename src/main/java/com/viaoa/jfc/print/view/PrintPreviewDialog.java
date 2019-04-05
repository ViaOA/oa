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


