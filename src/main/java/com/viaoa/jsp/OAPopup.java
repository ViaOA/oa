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

import com.viaoa.util.OAString;

/**
 * turn an html element into a popup.
 * if location is on viewport edge, then it will use the slide affect, else fade.
 * 
 * @author vvia
 */
public class OAPopup extends OAHtmlElement {
    private static final long serialVersionUID = 1L;

    protected String clickId;
    protected String top, right, bottom, left;

    private String type = "'fade'";

    private String lastAjaxSent2;

    /**
     * create popup that is centered;
     * @param id
     */
    public OAPopup(String id) {
        super(id);
        setup();
    }
    public OAPopup(String id, String clickId) {
        super(id);
        this.clickId = clickId;
        setup();
    }
    
    public OAPopup(String id, String top, String right, String bottom, String left) {
        super(id);
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
        setup();
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

    protected void setup() {
        addClass("oaPopup");
        addClass("oaShadow");
        setVisible(false);
    }
    
    
    @Override
    public boolean isChanged() {
        return false;
    }

    @Override
    public void reset() {
        setVisible(false);
    }
    
    
    @Override
    public String getScript() {
        lastAjaxSent = null;

        StringBuilder sb = new StringBuilder(2048);
        
        String s = super.getScript();
        if (s != null) sb.append(s);
        
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
    public String getAjaxScript() {
        StringBuilder sb = new StringBuilder(256);
        
        String s = super.getAjaxScript();
        if (s != null) sb.append(s);
        
        if (getVisible()) {
            sb.append("if (!$('#"+id+"').is(':visible')) $('#"+id+"').show("+type+", 325);\n");
        }
        else {
            sb.append("if ($('#"+id+"').is(':visible')) $('#"+id+"').hide("+type+", 325);\n");
        }

        String js = sb.toString();
        if (lastAjaxSent2 != null && lastAjaxSent2.equals(js)) js = null;
        else lastAjaxSent2 = js;
        
        return js;
    }

}
