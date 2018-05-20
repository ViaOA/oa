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
package com.viaoa.web;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAString;


/**
 * Controls an html elements style background-image.
 * 
 * Note:
 * This will use css background image styling, and the image will be sized to match the sizing of the element.
 * The size of the image will not affect the size of the html element.
 * 
 * @author vvia
 */
public class OABackgroundImage extends OAImage {
    private static final long serialVersionUID = 1L;

    /**
     * @param propertyPath path to the object that stores the image
     * @param rootDirectory directory where the image will be located
     */
    public OABackgroundImage(String id, Hub hub, String propertyPath, String rootDirectory) {
        super(id, hub, propertyPath, rootDirectory);
        setup();
    }
    public OABackgroundImage(String id) {
        super(id);
        setup();
    }

    protected void setup() {
        addStyle("background-repeat", "no-repeat");
        addStyle("background-position", "center center");
        addStyle("background-size", "contain");
        addStyle("display", "inline-block");
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
        else sb.append("$('#"+id+"').removeClass('oaMissingImage');\n");

        String js = sb.toString();

        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;

        return js;
    }
    @Override
    protected String convertSource(String src) {
        return "$('#"+id+"').css('background-image', 'url("+src+")');";
    }
}
