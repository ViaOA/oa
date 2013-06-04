package com.viaoa.jsp;

import java.io.OutputStream;

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

    public OAFileInput(String id) {
        super(id);
    }

    
    @Override
    public OutputStream getOutputStream(int length, String originalFileName) {
        return null;
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
