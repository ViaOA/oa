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
