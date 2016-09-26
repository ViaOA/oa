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


/**
 * Controls an html img "src" attribute, to use an image using the ImageSerlvet
 * 
 * Example:  if image is in object Employee.ImageStore.bytes
 * OAImage(hubEmployee, Employee.P_EmpImageStore);
 * 
 * this will change the <img src=".."> to use the "/servlet/img" url
 * 
 * 
 * @author vvia
 */
public class OAServletImage extends OAHtmlElement {
    private static final long serialVersionUID = 1L;

    protected String propertyPath;
    protected String bytePropertyName="bytes"; // name of property that has the bytes
    protected int maxWidth, maxHeight;

    /**
     * @param propertyPath path to the object that stores the image
     */
    public OAServletImage(String id, Hub hub, String propertyPath) {
        this(id, hub, propertyPath, 0, 0);
    }
    public OAServletImage(String id, Hub hub, String propertyPath, int maxWidth, int maxHeight) {
        super(id, hub);
        setPropertyPath(propertyPath);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }
    
    public String getPropertyPath() {
        return propertyPath;
    }
    public void setPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
    }

    // default is "bytes"
    public void setBytePropertyName(String propName) {
        this.bytePropertyName = propName;
    }
    public String getBytePropertyName() {
        return this.bytePropertyName;
    }
    
    
    private String lastAjaxSent;
    @Override
    public String getScript() {
        lastAjaxSent = null;
        return super.getScript();
    }
    
    
    public String getHtmlSource(Object objx) {
        String src = _getHtmlSource(objx);
        if (src == null) src = "";
        src = getSource(objx, src);
        return src;
    }
    private String _getHtmlSource(Object objx) {
        String src = null;

        if (!(objx instanceof OAObject)) return src;
        if (hub == null || propertyPath == null || bytePropertyName == null) return src;

        OAObject obj = (OAObject) hub.getAO();
        Object value = obj.getProperty(propertyPath);
        String className = null;
        if (value instanceof OAObject) {
            className = value.getClass().getName();
            value = ((OAObject) value).getProperty("id");
        }
        if (value == null) return null;
                
        src = String.format("/servlet/img?c=%s&id=%s&p=%s", className, value+"", getBytePropertyName());
        if (maxHeight > 0) src = String.format("%s&mh=%d", src, maxHeight);
        if (maxWidth > 0) src = String.format("%s&mw=%d", src, maxWidth);

        return src;
    }

    
    @Override
    public String getAjaxScript() {
        String s = super.getAjaxScript();
        StringBuilder sb = new StringBuilder(1024);
        if (s != null) sb.append(s);

        String src = null;
        OAObject obj = null;

        if (hub != null) src = getHtmlSource(hub.getAO());

        if (src == null) src = "";
        if (src.length() == 0) sb.append("$('#"+id+"').addClass('oaMissingImage');\n");
        else {
            sb.append("$('#"+id+"').attr('src', '"+src+"');\n");
            sb.append("$('#"+id+"').removeClass('oaMissingImage');\n");
        }
        sb.append("$('#"+id+"').removeAttr('width');\n");
        sb.append("$('#"+id+"').removeAttr('height');\n");
        
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
