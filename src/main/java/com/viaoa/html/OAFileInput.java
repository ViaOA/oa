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
package com.viaoa.html;

import com.viaoa.util.OAConv;

/**
Component for uploading a file from the browser to the server.  OAForm.getAction() will automatically
set the form submit to use enctype="multipart/form-data" and it will parse the request stream and save 
the file on the server.
<pre>
    [Java Code]
    OAFileInput fi = new OAFileInput("fileName");
    form.add("fiFile", fi);
    ....
    [HTML Code]
    &lt;input type="file" name="fiFile"&gt;
</pre>

*/
public class OAFileInput extends OAHtmlComponent {
    private static final long serialVersionUID = 1L;
    protected String saveAsFileName;
    protected int max=-1;
    protected boolean bSaved;
    protected String originalFileName;

    public OAFileInput(String fileName) {
        setFileName(fileName);
    }

    /** name of file on server to write to. */
    public String getFileName() {
        return saveAsFileName;
    }
    public void setFileName(String fileName) {
        this.saveAsFileName = fileName;
    }

    public String getSaveAsFileName() {
        return saveAsFileName;
    }
    public void setSaveAsFileName(String fileName) {
        this.saveAsFileName = fileName;
    }
    
    public String getOriginalFileName() {
        return originalFileName;
    }
    public void setOriginalFileName(String fileName) {
        this.originalFileName = fileName;
    }
    
    public int getMax() {
        return max;
    }
    /** max length of file that can be uploaded.  If -1 (default) then unlimited.  If file is greater then this
        amount, then OAForm.processRequest will throw an error.
    */
    public void setMax(int x) {
        max = x;
    }
    
    /** returns true is file was saved else false. */
    public boolean getSaved() {
        // set by OAForm.processMultipart()=>true and OAForm.processRequest()=>false
        return bSaved;
    }
    
    protected void setSaved(boolean b) {
        bSaved = b;
    }

    protected void setValuesInternal(String nameUsed, String[] values) { // called by OAForm
        String svalue;
        if (values == null || values.length != 1 || values[0] == null || values[0].length() == 0) {
            setOriginalFileName(null);
        }
        else {
            setOriginalFileName(values[0]);
        }
    }
}
