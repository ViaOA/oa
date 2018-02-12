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
            for (i=0; i&lt;files.length; i++) {
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
