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
package com.viaoa.jfc;

import javax.swing.*;
import java.net.*;
import java.io.*;

import com.viaoa.util.*;

/**
    JProgressBar component used to copy a file, and visually display the progress.
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class OAFileCopyProgressBar extends JProgressBar {
    
    /** 
        Copy a file to a new file.
    */
    public void copy(String fileNameFrom, String fileNameTo) throws Exception {
        fileNameFrom = OAString.convertFileName(fileNameFrom);
        File fileFrom = new File(fileNameFrom);
        if (!fileFrom.exists()) throw new Exception("File " + fileNameFrom + " not found");


        fileNameTo = OAString.convertFileName(fileNameTo);
        File fileTo = new File(fileNameTo);
        fileTo.mkdirs();
        fileTo.delete();
        
        int max = (int) fileFrom.length();
        this.setMinimum(0);
        this.setMaximum(max);
        this.setValue(0);


        InputStream is = new FileInputStream(fileFrom);
        OutputStream os = new FileOutputStream(fileTo);
            
        int tot = 0;
        int bufferSize = 1024 * 8;
        
        byte[] bs = new byte[bufferSize];
        for (int i=0; ;i++) {
            int x = is.read(bs, 0, bufferSize);
            if (x < 0) break;
            tot += x;
            this.setValue( Math.min(tot,max));
            os.write(bs, 0, x);
        }
        is.close();
        os.close();
        setValue(max);
    }
    
}

