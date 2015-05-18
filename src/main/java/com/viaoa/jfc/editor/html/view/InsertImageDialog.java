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
package com.viaoa.jfc.editor.html.view;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.jfc.OAButton;
import com.viaoa.jfc.OAComboBox;
import com.viaoa.jfc.OATextField;
import com.viaoa.jfc.OATreeComboBox;
import com.viaoa.jfc.editor.html.OAHTMLTextPane;
import com.viaoa.jfc.propertypath.OAPropertyPathTree;
import com.viaoa.jfc.propertypath.model.oa.ObjectDef;


public class InsertImageDialog extends JDialog {

    protected boolean bCancelled;
    private Hub<Integer> hubImageDpi;
    private OATextField txtFileName;
    private OATextField txtImageDpi;
    private OAComboBox cboImageDpi;
    private JFileChooser fileChooser;

    public InsertImageDialog(Window parent, JFileChooser fc) {
        super(parent, "Insert Image", ModalityType.APPLICATION_MODAL);

        this.fileChooser = fc;
        this.hubImageDpi = new Hub<Integer>(Integer.class);
        hubImageDpi.add(72);
        hubImageDpi.add(96);
        hubImageDpi.add(300);
        hubImageDpi.add(600);

        hubImageDpi.addHubListener(new HubListenerAdapter() {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                String s = "";
                Object obj = hubImageDpi.getAO();
                if (obj != null) s = ""+((Integer) obj).intValue();
                getImageDpiTextField().setText(s);
            }
        });
        
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setResizable(true);
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
        if (b) txtFileName.requestFocus();
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

        pan.add(new JLabel("Image File:"), gc);
        
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setBorder(new EmptyBorder(new Insets(0,0,0,0)));
        p.add(getFileNameTextField());
        p.add(getSelectFileButton());
        gc.gridwidth = gc.REMAINDER;
        pan.add(p, gc);
        gc.gridwidth = 1;
        
        pan.add(new JLabel("Image DPI:"), gc);
        pan.add(getImageDpiComboBox(), gc);
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

    private JButton cmdSelectFile;
    public JButton getSelectFileButton() {
        if (cmdSelectFile == null) {
            cmdSelectFile = new JButton("Select ...");
            ActionListener al = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InsertImageDialog.this.onSelectFile();
                }
            };
            cmdSelectFile.addActionListener(al);
            URL url = OAHTMLTextPane.class.getResource("view/image/openFile.gif");
            cmdSelectFile.setIcon(new ImageIcon(url));
            OAButton.setup(cmdSelectFile);
        }
        return cmdSelectFile;
    }
    
    
    private JButton butSelect;
    public JButton getOkButton() {
        if (butSelect == null) {
            butSelect = new JButton("Ok");
            ActionListener al = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InsertImageDialog.this.onOk();
                }
            };
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
                    InsertImageDialog.this.onCancel();
                }
            });
            butCancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "cancel");
            butCancel.getActionMap().put("cancel",
                    new AbstractAction() {
                        public void actionPerformed(ActionEvent e) {
                            InsertImageDialog.this.onCancel();
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
    protected void onSelectFile() {
        int x = fileChooser.showOpenDialog(this);
        if (x != JFileChooser.APPROVE_OPTION) return;
        File file = fileChooser.getSelectedFile();
        if (file == null) return;
        if (file.isDirectory()) return;
        if (!file.exists()) return;
        long sz = file.length();
        getFileNameTextField().setText(file.getAbsolutePath());
    }

    public OATextField getFileNameTextField() {
        if (txtFileName == null) {
            txtFileName = new OATextField();
            txtFileName.setColumns(22);
        }
        return txtFileName;
    }    
    
    public OATextField getImageDpiTextField() {
        if (txtImageDpi == null) {
            txtImageDpi = new OATextField();
            txtImageDpi.setColumns(6);
        }
        return txtImageDpi;
    }    

    public OAComboBox getImageDpiComboBox() {
        if (cboImageDpi == null) {
            cboImageDpi = new OAComboBox(hubImageDpi, "", 6);
            cboImageDpi.setEditor(getImageDpiTextField());
        }
        return cboImageDpi;
    }
    
    public static void main(String[] args) {
        Hub<ObjectDef> hub = new Hub<ObjectDef>(ObjectDef.class);
        JFileChooser fc = new JFileChooser();
        InsertImageDialog dlg = new InsertImageDialog(null, fc);
        dlg.setVisible(true);
    }
}
