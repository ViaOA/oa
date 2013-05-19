/* 
This software and documentation is the confidential and proprietary 
information of ViaOA, Inc. ("Confidential Information").  
You shall not disclose such Confidential Information and shall use 
it only in accordance with the terms of the license agreement you 
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 
package com.viaoa.jfc;

import java.io.*;
import java.awt.event.*;
import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;
//import javax.swing.preview.*;
//import javax.swing.preview.filechooser.*;  

/** 
    JFileChooser subclass.
    <p>
    Examples:
    <pre>
    OAFileChooser fc = new OAFileChooser("");

    FileFilter filter = new FileFilter() {  // controls files that can be selected
        public boolean accept(File f) {
            if ( f.getName().toUpperCase().endsWith(".JAVA") ) return true;
            if ( f.isDirectory() ) return true;
            return false;
        }
        public String getDescription() {
            return "Java source code";   
        }
    };

    fc.setFileFilter(filter);
    fc.setDialogTitle("Java source code (*.java)");
    fc.setDialogType(JFileChooser.OPEN_DIALOG);
    fc.setFileHidingEnabled(false);
    fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    fc.setMultiSelectionEnabled(true);

    -- Usage
    void addProgram() {
        int i = fc.showOpenDialog(this);
        if (i == JFileChooser.APPROVE_OPTION) {
            File[] files = fc.getSelectedFiles();
            for (i=0; i<files.length; i++) {
                System.out.println("" + files[i] );
                if (!files[i].exists()) continue;
                if (files[i].isDirectory()) continue;
                ...
            }
        }
    }
    </pre>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class OAFileChooser extends JFileChooser {
    File[] files;

    public OAFileChooser(File directory) {
        super(directory);
        setup();
    }
    public OAFileChooser(String path) {
        super(path);
        setup();
    }
    public OAFileChooser() {
        super();
        setup();
    }

    /**
        Set the file that is to be selected when the chooser is displayed.
    */
    public void setSelectedFile(File selectedFile) {
//        setSelectedFiles(files);
        super.setSelectedFile(selectedFile);
    }

    protected void setup() {
/**qqqqqqqqqqqq  does not compile with jdk1.2
        JComponent jc = (JComponent) this.getComponents()[3];
        jc = (JComponent) jc.getComponents()[0];
        jc = (JComponent) jc.getComponents()[0];
        jc = (JComponent) jc.getComponents()[6];
        JList lst = (JList) jc.getComponents()[0];

        lst.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                Object[] objects = ((JList)e.getSource()).getSelectedValues();
                if (objects != null) {
                    files = new File[objects.length];
                    System.arraycopy(objects,0,files,0,objects.length);
                }
            }
        });
******/
    }
}
