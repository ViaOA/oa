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
package com.viaoa.jsp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Used to upload a file from a browser form, input file element.
 * 
 * 
 * <input type="file" id="fiFile" name="fiFile" size="40">
 * 
 * @author vvia
 */
public class OAFileInput extends OAHtmlElement implements OAJspMultipartInterface {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(OAFileInput.class.getName());
    
    private File file;
    private String fname;

    public OAFileInput(String id) {
        super(id);
    }

    public void setFile(File file) {
        this.file = file;
    }
    public File getFile() {
        return file;
    }
    public void setFileName(String fname) {
        this.fname = fname;
    }
    public String getFileName() {
        return this.fname;
    }
    
    @Override
    public OutputStream getOutputStream(int length, String originalFileName) {
        OutputStream os = null;
        if (file != null) {
            try {
                os = new FileOutputStream(this.file);
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "error while creating file, file="+file, e);
            }
        }
        if (fname != null) {
            try {
                File fx = new File(fname);
                os = new FileOutputStream(fx);
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "error while creating file, file="+fname, e);
            }
        }
        return os;
    }

    @Override
    public String getAjaxScript() {
        StringBuilder sb = new StringBuilder(1024);
        String s = super.getAjaxScript();
        if (s != null) sb.append(s);
        
        
        sb.append("$('#"+id+"').attr('name', '"+id+"');\n");
        
        s = sb.toString();
        return s;
    }
}
