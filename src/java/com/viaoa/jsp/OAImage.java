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
package com.viaoa.jsp;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAString;


/**
 * Controls an html img "src" attribute, to use from a property.
 * 
 * 
 * @author vvia
 */
public class OAImage extends OAHtmlElement {
    private static final long serialVersionUID = 1L;

    protected String propertyPath;
    protected String rootDirectory;
    protected String source;

    /**
     * @param propertyPath path to the object that stores the image
     * @param rootDirectory directory where the image will be located
     */
    public OAImage(String id, Hub hub, String propertyPath, String rootDirectory) {
        super(id, hub);
        setPropertyPath(propertyPath);
        this.rootDirectory = rootDirectory;
    }
    public OAImage(String id) {
        super(id);
    }
    public void setSource(String src) {
        lastAjaxSent = null;  
        this.source = src;
    }
    public String getSource() {
        return this.source;
    }

    
    public String getPropertyPath() {
        return propertyPath;
    }
    public void setPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
    }

    public String getRootDirectory() {
        return rootDirectory;
    }
    public void setRootDirectory(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    
    
    private String lastAjaxSent;
    @Override
    public String getScript() {
        lastAjaxSent = null;
        return super.getScript();
    }
    
    
    @Override
    public String getAjaxScript() {
        String s = super.getAjaxScript();
        StringBuilder sb = new StringBuilder(1024);
        if (s != null) sb.append(s);

        String src = null;
        OAObject obj = null;

        if (hub == null || propertyPath == null) {
        }
        else {
            obj = (OAObject) hub.getAO();
            if (obj != null) {
                String value = obj.getPropertyAsString(propertyPath);
                
                s = getRootDirectory();
                if (OAString.isEmpty(s)) s = "/";
                else s += "/";
                
                if (!OAString.isEmpty(value)) {
                    src = String.format("%s%s", s, value+"");
                }
                else src = null;
            }
        }
                
        if (src == null) {
            src = this.source;
            if (src == null) src = "";
        }
        src = getSource(obj, src);
        if (src == null) src = "";
        if (src.length() == 0) sb.append("$('#"+id+"').addClass('oaMissingImage');\n");
        else {
            sb.append("$('#"+id+"').attr('src', '"+src+"');\n");
            sb.append("$('#"+id+"').removeClass('oaMissingImage');\n");
        }
        
        String js = sb.toString();

        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;

        return js;
    }

    /**
     * Called to get the image source.
     * @param defaultSource default value that will be for the image src used for ImageServlet
     * @return
     */
    public String getSource(Object object, String defaultSource) {
        return defaultSource;
    }
}
