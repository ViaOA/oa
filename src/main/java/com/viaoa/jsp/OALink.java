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
 * Component for managing html links.
 * @author vvia
 *
 */
public class OALink extends OAHtmlElement {
    private static final long serialVersionUID = 1L;

    
    public OALink(String id) {
        super(id);
    }
    public OALink(String id, String forwardUrl) {
        super(id);
        setForwardUrl(forwardUrl);
    }
    public OALink(String id, Hub hub) {
        super(id, hub);
    }

    public OALink(String id, Hub hub, String forwardUrl) {
        super(id, hub);
        setForwardUrl(forwardUrl);
    }

    @Override
    public String getScript() {
        lastAjaxSent = null;

        StringBuilder sb = new StringBuilder(1024);
        
        String furl = getForwardUrl();
        if (!OAString.isEmpty(furl)) {
            sb.append("$('#"+id+"').attr('href', '"+furl+"');\n");
        }
        
        if (bSubmit || bAjaxSubmit) {
            if (bAjaxSubmit) {
                sb.append("$('#"+id+"').click(function() {$('#oacommand').val('"+id+"');ajaxSubmit();return false;});\n");
            }
            else {
                sb.append("$('#"+id+"').click(function() { $('#oacommand').val('"+id+"'); $('form').submit(); return false;});\n");
            }
            sb.append("$('#"+id+"').addClass('oaSubmit');\n");
        }
        
        String s = getAjaxScript();
        if (s != null) sb.append(s);
        String js = sb.toString();
        
        return js;
    }

    
    @Override
    public String getRenderHtml(OAObject obj) {
        String s = "<a href='#' class='"+getRenderClass(obj)+"' style='"+getRenderStyle(obj)+"' "+getRenderOnClick(obj)+">"+getRenderText(obj)+"</a>";
        return s;
    }
    public String getRenderText(OAObject obj) {
        String s = "Text";
        return s;
    }
    public String getRenderStyle(OAObject obj) {
        return "";
    }
    public String getRenderClass(OAObject obj) {
        return "";
    }
    public String getRenderOnClick(OAObject obj) {
        String js = "onClick='";
        
        String s = getProcessedConfirmMessage(obj);
        if (OAString.isNotEmpty(s)) {
            // will be wrapped in "
            s = OAString.convert(s, "\\\"", "xQxq");
            s = OAString.convert(s, "\"", "\\\"");
            s = OAString.convert(s, "xQxq", "\\\"");

            s = OAString.convert(s, "\\\'", "xQxq");
            s = OAString.convert(s, "\'", "\\\'");
            s = OAString.convert(s, "xQxq", "\\\'");
            
            js += "if (!window.confirm(\\\""+s+"\\\")) return false;";
        }

        js += "$(\\\"#oacommand\\\").val(\\\""+id+"\\\");'";
        return js;
    }
    @Override
    public String getEditorHtml(OAObject obj) {
        return getRenderHtml(obj);
    }

    
    
    
    
}
