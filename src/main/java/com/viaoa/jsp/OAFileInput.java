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

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.hub.Hub;
import com.viaoa.jfc.image.OAImageUtil;
import com.viaoa.object.*;
import com.viaoa.util.*;


/**
 * Used to upload a file from a browser form, input file element.
 * 
 * <input type="file" id="fiFile" name="fiFile" size="40">
 * 
 * @author vvia
 */
public class OAFileInput extends OAHtmlElement implements OAJspMultipartInterface {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(OAFileInput.class.getName());
    protected int maxFileSize=-1;
    private File file;
    private String fname;
    protected String propertyPath;
    protected boolean required;
    private String name;
    protected String placeholder;
    private ArrayList<String> alAcceptFileExt;

    // if using images, this will make sure that the final version is sized
    private int maxImageWidth, maxImageHeight;
    
    public OAFileInput(String id) {
        super(id);
    }
    public OAFileInput(String id, Hub hub, String propertyPath) {
        super(id, hub);
        // note: dont send propertyPath to super, since it is used to build the output html
        setPropertyPath(propertyPath);
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

    
    /**
     * Add file name extension for valid files that can be uploaded.
     * example:  "gif"
     */
    public void addAcceptFileExt(String ext) {
        if (OAString.isEmpty(ext)) return;
        if (alAcceptFileExt == null) alAcceptFileExt = new ArrayList<>();
        alAcceptFileExt.add(ext);
    }
    

    protected void onImageUploaded() {
        // used to create the object to hold image
    }
    
    @Override
    public OutputStream getOutputStream(final int length, final String originalFileName) {
        onImageUploaded();
        
        int x = getMaxFileSize();
        if (maxFileSize >= 0 && length > maxFileSize) {
            return null;
        }
        
        if (alAcceptFileExt != null && originalFileName != null) {
            boolean b = false;
            for (String ext : alAcceptFileExt) {
                if (ext == null) continue;
                if (ext.length() == 0) continue;
                if (!ext.startsWith(".")) ext = "." + ext;
                if (originalFileName.toUpperCase().endsWith(ext.toUpperCase())) {
                    b = true;
                    break;
                }
            }
            if (!b) return null;
        }

        OutputStream os = null;
        if (maxImageWidth > 0 || maxImageHeight > 0) {
            // need to resize/scale image
            os = new ByteArrayOutputStream(length) {
                @Override
                public void close() throws IOException {
                    super.close();
                    byte[] bs = this.toByteArray();

                    BufferedImage bi = OAImageUtil.convertToBufferedImage(bs);
                    bi = OAImageUtil.scaleDownToSize(bi, maxImageWidth, maxImageHeight);
                    bs = OAImageUtil.convertToBytes(bi);
                    
                    OutputStream os = _getOutputStream(bs.length, originalFileName);
                    os.write(bs);
                    os.close();
                }
            };
        }
        else {
            os = _getOutputStream(length, originalFileName);
        }
        return os;
    }
    
    protected OutputStream _getOutputStream(final int length, final String originalFileName) {
        if (getHub() != null && OAString.isNotEmpty(getPropertyPath())) {
            final OAObject objAO = (OAObject) getHub().getAO();
            if (objAO != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(length) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        byte[] bs = this.toByteArray();
                        objAO.setProperty(getPropertyPath(), bs);
                    }
                };
                return baos;
            }
        }

        OutputStream os = null;
        if (file != null) {
            try {
                os = new FileOutputStream(this.file) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                    }
                };
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "error while creating file, file="+file, e);
            }
        }
        else if (fname != null) {
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
    public String getScript() {
        StringBuilder sb = new StringBuilder(1024);
        String s = super.getScript();
        if (s != null) sb.append(s);

        sb.append("$('#"+id+"').attr('name', '"+id+"');\n");
        
        // keeps track of required or not
        sb.append("$('#"+getForm().getId()+"').prepend(\"<input type='hidden' name='"+id+"Required' value=''>\");\n");
        
        
        //qqqqqq todo:  allow support for multiple, and use with pp that is a hub
        
        if (alAcceptFileExt != null) {
            s = "";
            for (String ext : alAcceptFileExt) {
                if (ext == null) continue;
                if (ext.length() == 0) continue;
                if (!ext.startsWith(".")) ext = "." + ext;
                s = OAString.append(s, ext, ", ");
            }
            if (OAString.isNotEmpty(s)) sb.append("$('#" + id + "').attr('accept', '"+s+"');\n");
        }

        s = getPlaceholder();
        if (s != null) sb.append("$('#" + id + "').attr('placeholder', '"+OAString.convertForSingleQuotes(s)+"');\n");
        
        String js = sb.toString();
        return js;
    }
    
    @Override
    public String getVerifyScript() {
        StringBuilder sb = new StringBuilder(1024);
        String s = super.getVerifyScript();
        if (s != null) sb.append(s);
        
        // add js code to check for max size
        if (getMaxFileSize() > 0) {
            sb.append("if ($('#"+id+"').files && $('#\"+id+\"').files.length > 0) {\n");
            sb.append("    if ($('#"+id+"').files[0].size > "+getMaxFileSize()+") {\n");
            sb.append("        errors.push('File size over limit of "+getMaxFileSize()+"');\n");
            sb.append("    }\n");
            sb.append("}\n");
        }

        sb.append("if ($('#"+id+"Required').val() == 'true') {\n");

        s = name;
        if (OAString.isEmpty(s)) {
            s = placeholder;
            if (OAString.isEmpty(s)) s = id;
        }
        sb.append("if ($('#\"+id+\"').files === undefined || $('#"+id+"').files.length == 0) requires.push('\" + s + \"');\n");
        
        sb.append("}\n");
        
        String js = sb.toString();
        return js;
    }

    @Override
    public String getAjaxScript() {
        String s = super.getAjaxScript();
        StringBuilder sb = new StringBuilder(1024);
        if (s != null) sb.append(s);

        if (getRequired()) {
            boolean b = false;
            
            // see if the file/data is already populated
//qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq check propertyPath=null, file.exists, qqqqqqqqqq
            
            if (b) sb.append("if ($('#"+id+"Required').val('true');\n");
            else sb.append("if ($('#"+id+"Required').val('');\n");
        }
        
        String js = sb.toString();
        return js;
    }
    

    @Override
    public void setSubmit(boolean b) {
        // no-op, wont work with file input
    }
    @Override
    public void setAjaxSubmit(boolean b) {
        // no-op, wont work with file input
    }

    public int getMaxFileSize() {
        return maxFileSize;
    }
    /** max length of file that can be uploaded.  If -1 (default) then unlimited.  If file is greater then this
        amount, then OAForm.processRequest will throw an error.
    */
    public void setMaxFileSize(int x) {
        maxFileSize = x;
    }

    public String getPropertyPath() {
        return propertyPath;
    }
    public void setPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
    }

    public boolean isRequired() {
        return required;
    }
    public boolean getRequired() {
        return required;
    }
    public void setRequired(boolean required) {
        this.required = required;
    }

    /** used when displaying error message for this textfield */
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        placeholder = null;
    }
    public String getPlaceholder() {
        return this.placeholder;
    }

    /**
     * Set max size of images that are saved.
     * This will use OAImage to resize/scale before storing the file.
     */
    public void setMaxImageSize(int width, int height) {
        maxImageWidth  = width;
        maxImageHeight = height;
    }
}
