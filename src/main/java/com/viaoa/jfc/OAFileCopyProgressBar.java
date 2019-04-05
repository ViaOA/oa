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

