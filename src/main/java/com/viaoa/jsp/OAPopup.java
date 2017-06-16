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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.html.Util;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;


/**
 * turn an html element into a popup.
 * if location is on viewport edge, then it will use the slide affect, else fade.
 * 
 * @author vvia
 */
public class OAPopup implements OAJspComponent {
    private static final long serialVersionUID = 1L;

    protected String id;
    protected String clickId;
    protected OAForm form;
    protected String maxHeight; // ex:  200px,  12em
    protected String maxWidth; // ex:  200px,  12em
    protected boolean bVisible;
    protected String top, right, bottom, left;


    /**
     * create popup that is centered;
     * @param id
     */
    public OAPopup(String id) {
        this.id = id;
    }
    public OAPopup(String id, String clickId) {
        this.id = id;
        this.clickId = clickId;
    }
    
    public OAPopup(String id, String top, String right, String bottom, String left) {
        this.id = id;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    /**
     * 
     * @param id html element to popup
     * @param clickId component that can popup directly on webpage, without calling/ajax the server.
     */
    public OAPopup(String id, String clickId, String top, String right, String bottom, String left) {
        this(id, top, right, bottom, left);
        this.clickId = clickId;
    }
    
    @Override
    public boolean isChanged() {
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void reset() {
        setVisible(false);
    }
    
    @Override
    public void setForm(OAForm form) {
        this.form = form;
    }
    @Override
    public OAForm getForm() {
        return this.form;
    }

    @Override
    public boolean _beforeSubmit() {
        return true;
    }


    @Override
    public boolean _onSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {
        return false;
    }
    
    @Override
    public String onSubmit(String forwardUrl) {
        return forwardUrl;
    }
    
    @Override
    public String _afterSubmit(String forwardUrl) {
        return forwardUrl;
    }

    private String type = "'fade'";
    
    @Override
    public String getScript() {
        lastAjaxSent = null;

        StringBuilder sb = new StringBuilder(2048);
        sb.append("$('#"+id+"').addClass('oaPopup');\n");
        sb.append("$('#"+id+"').addClass('oaShadow');\n");
        
        String css = "";
        
        String topx = top;
        String leftx = left;
        
        
        if (OAString.isEmpty(top) && OAString.isEmpty(bottom)) {
            // center
            sb.append("$('#"+id+"').css({transform: 'translate(-50%, -50%)'});"); 
            topx = "50vh";
            leftx = "50vw";
        }

        if (topx != null && OAString.isNumber(topx)) topx += "px";
        if (leftx != null && OAString.isNumber(leftx)) leftx += "px";
        if (bottom != null && OAString.isNumber(bottom)) bottom += "px";
        if (right != null && OAString.isNumber(right)) right += "px";
        
        
        if (OAString.isEmpty(topx) && OAString.isEmpty(bottom)) topx = "0";
        
        if (OAString.isNotEmpty(topx)) {
            if (topx.charAt(0) == '0') type = "'slide', {direction: 'up'}";
            css = "top:'"+topx + "', ";
        }
        else {
            if (bottom.charAt(0) == '0') type = "'slide', {direction: 'down'}";
            css = "bottom:'"+bottom + "', ";
        }

        String rightx = right;
        if (OAString.isEmpty(rightx) && OAString.isEmpty(leftx)) rightx = "0";
        
        if (OAString.isNotEmpty(right)) {
            if (right.charAt(0) == '0') type = "'slide', {direction: 'right'}";
            css += "right:'"+right+"'";
        }
        else {
            if (leftx.charAt(0) == '0') type = "'slide', {direction: 'left'}";
            css += "left:'"+leftx+"'";
        }

        String max = getMaxHeight();
        if (OAString.isNotEmpty(max)) css += ", max-height:'"+max+"'";
        
        max = getMaxWidth();
        if (OAString.isNotEmpty(max)) css += ", max-width:'"+max+"'";
        
        sb.append("$('#"+id+"').css({"+css+"});\n");
        
        if (OAString.isNotEmpty(clickId)) {
            sb.append("$('#"+clickId+"').click(function() {\n");
            sb.append("  if ($('#"+id+"').is(':visible')) $('#"+id+"').hide("+type+", 325); else $('#"+id+"').show("+type+", 325);return false;}\n");
            sb.append(");\n");
        }
        
        String js = sb.toString();
        return js;
    }
    
    @Override
    public String getVerifyScript() {
        return null;
    }

    private String lastAjaxSent;
    
    @Override
    public String getAjaxScript() {
        
        StringBuilder sb = new StringBuilder(256);
        
        if (getVisible()) {
            sb.append("if (!$('#"+id+"').is(':visible')) $('#"+id+"').show("+type+", 325);\n");
        }
        else {
            sb.append("if ($('#"+id+"').is(':visible')) $('#"+id+"').hide("+type+", 325);\n");
        }

        String js = sb.toString();
        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        lastAjaxSent = js;
        
        return null;
    }

    @Override
    public void setEnabled(boolean b) {
    }
    @Override
    public boolean getEnabled() {
        return true;
    }


    @Override
    public void setVisible(boolean b) {
        lastAjaxSent = null;  
        this.bVisible = b;
    }
    @Override
    public boolean getVisible() {
        return this.bVisible;
    }


    public void setMaxHeight(String val) {
        this.maxHeight = val;
    }
    public String getMaxHeight() {
        return this.maxHeight;
    }

    public void setMaxWidth(String val) {
        this.maxWidth = val;
    }
    public String getMaxWidth() {
        return this.maxWidth;
    }

    @Override
    public String getForwardUrl() {
        return null;
    }
}
