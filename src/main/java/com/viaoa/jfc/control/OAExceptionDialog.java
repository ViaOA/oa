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
package com.viaoa.jfc.control;

import java.util.Vector;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import com.viaoa.hub.*;

/**
    Dialog box that works directly with OAExceptions to display dialog when an exception is created.
    Registers with OAException as a listener.
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class OAExceptionDialog extends JDialog { //implements OAExceptionListener {
    JList lst;
    Vector vec = new Vector();
    JButton cmd;
    boolean bFatal, bIgnore, bIgnoreNext;
    JTextArea txt;
//    OAException exception;
    static int cnt;
    
    public OAExceptionDialog() {
        this(null);
    }

    
    public OAExceptionDialog(Frame parent) {
        super(parent,"OAException",false);
//        OAException.addListener(this);
        setup();
        this.pack();
        this.addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }

    /** number of OAExceptions that have occured  */
    public static int getCount() {
        return cnt;
    }

    /** control whether dialog shows Exceptions.  
        @see setIgnoreNext
    */
    public void setIgnore(boolean b) {
        bIgnore = b;
    }
    /** control whether dialog shows Exceptions.  
        @see setIgnoreNext
    */
    public boolean getIgnore() {
        return bIgnore;
    }

    /** Controls whether dialog shows on the next Exceptions.  */
    public void setIgnoreNext(boolean b) {
        bIgnoreNext = b;
    }
    /** Controls whether dialog shows on the next Exceptions.  */
    public boolean getIgnoreNext() {
        return bIgnoreNext;
    }

    protected void close() {
        if (bFatal) System.exit(1);
        vec.removeAllElements();   
    }
    
    /** Listener method that is called by OAException whenever a new exception is created. */
    public void OnOAException(Exception e) { //OAException e) {
        cnt++;
        //exception = e;
        if (bIgnore) return;
        if (bIgnoreNext) {
            bIgnoreNext = false;
            return;
        }
        // if (e.getType() == OAException.FATAL) bFatal = true;
            
        /* Throwable ex = e.getOriginalException();
        String s;
        if (ex != null) {
            //System.out.println("OAException: "+ex);   
            //ex.printStackTrace();
            s = ex.getClass().getName();
        }
        else {
            //System.out.println("OAException: "+e);   
            //e.printStackTrace();
            s = "OAException";
        }
        */
        String s = "";
        if (bFatal) {
            cmd.setText("Close Application");
            s = "Fatal Exception";
        }
        else cmd.setText("OK");

        setTitle(s);
/*
        String[] ss = e.getMessages();
        for (int i=0; i<ss.length; i++) {
            vec.addElement(ss[i]);
        }
*/        
        vec.addElement(" ");
        lst.setListData(vec);
        txt.setText("");
        setVisible(true);
    }

    protected void setup() {
        GridBagLayout gb = new GridBagLayout(); 
        GridBagConstraints gc = new GridBagConstraints(); 
        Container cont = getContentPane(); 
        cont.setLayout(gb); 

        lst = new JList(vec);
/*
        // lst.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
        lst.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int pos = lst.getSelectedIndex();
                if (pos >= 0 && vec != null && pos < vec.size()) {
                    txt.setText((String) vec.elementAt(pos));
                    Throwable ex = exception;
                    if (exception.getOriginalException() != null) ex = exception.getOriginalException();
                    System.out.println("---------- Last Stack Trace ------------- "+exception.getThread());
                    ex.printStackTrace();
                }
            }
        });
*/        
        JScrollPane span = new JScrollPane(lst);
        
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.insets = new Insets(2,2,2,2);
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 1;
        gb.setConstraints(span, gc); 
        cont.add(span);
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        gc.weighty = 0;

        
        txt = new JTextArea(4,10);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        // txt.setEnabled(false);

        span = new JScrollPane(txt);
        
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.insets = new Insets(2,2,2,2);
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 1;
        gb.setConstraints(span, gc); 
        cont.add(span);
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        gc.weighty = 0;
        
        
        
        cmd = new JButton("OK");
        cmd.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OAExceptionDialog.this.setVisible(false);
                close();
            }
        } );
        gb.setConstraints(cmd, gc); 
        cont.add(cmd);
    }

    public void addNotifyXXXX() {
        super.addNotify(); 
        Insets inset = getInsets(); 
        Dimension d = getContentPane().getPreferredSize(); 
        int w = d.width + inset.right + inset.left; 
        int h = d.height + inset.top + inset.bottom; 
        JMenuBar mb = getJMenuBar(); 
        if (mb != null) h += mb.getPreferredSize().height; 

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        Point p = new Point((dim.width/2) - (w/2), (dim.height/2) - (h/2));
        setBounds(p.x, p.y, w,h); 
    }     
    
}