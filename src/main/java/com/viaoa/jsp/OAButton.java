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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.hub.Hub;


/**
 * Used to control an html button
 * show/hide
 * enable
 * ajax submit option
 * forward url
 * @author vvia
 *
 */
public class OAButton implements OAJspComponent {
    private static final long serialVersionUID = 1L;

//qqqqqqq get command types/logic from OAButton
    
    protected Hub hub;
    protected String id;
    protected OAForm form;
    protected boolean bEnabled = true;
    protected boolean bVisible = true;
    protected boolean bAjaxSubmit;
    protected String forwardUrl;
    protected boolean bSubmit;
    
    public OAButton(String id, Hub hub) {
        this.id = id;
        this.hub = hub;
        setSubmit(true);
    }

    public OAButton(String id) {
        this.id = id;
        setSubmit(true);
    }
    
    public void setForwardUrl(String forwardUrl) {
        this.forwardUrl = forwardUrl;
    }
    public String getForwardUrl() {
        return this.forwardUrl;
    }
    
    @Override
    public boolean isChanged() {
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setAjaxSubmit(boolean b) {
        bAjaxSubmit = b;
        if (b) setSubmit(false);
    }
    public boolean getAjaxSubmit() {
        return bAjaxSubmit;
    }

    /** if the html is not a submit, then use this to have form submitted. */
    public void setSubmit(boolean b) {
        bSubmit = b;
        if (b) setAjaxSubmit(false);
    }
    public boolean getSubmit() {
        return bSubmit;
    }
    
    @Override
    public void reset() {
    }

    @Override
    public void setForm(OAForm form) {
        this.form = form;
    }
    public OAForm getForm() {
        return form;
    }

    @Override
    public boolean _beforeSubmit() {
        return true;
    }

    @Override
    public boolean _onSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {
        boolean bWasSubmitted = (req.getParameterValues(id) != null);
        if (!bWasSubmitted && hmNameValue != null) {
            bWasSubmitted = hmNameValue.get(id) != null;
        }
            
        if (!bWasSubmitted) {
            String s = req.getParameter("oacommand");
            if (s == null && hmNameValue != null) {
                String[] ss = hmNameValue.get("oacommand");
                if (ss != null && ss.length > 0) s = ss[0];
            }
            bWasSubmitted  = (id != null && id.equals(s));
        }
        return bWasSubmitted; // true if this caused the form submit
    }

    @Override
    public String _afterSubmit(String forwardUrl) {
        return forwardUrl;
    }

    @Override
    public String onSubmit(String forwardUrl) {
        return forwardUrl;
    }
    
    private String lastAjaxSent;
    
    @Override
    public String getScript() {
        lastAjaxSent = null;
        StringBuilder sb = new StringBuilder(1024);
        sb.append("$('#"+id+"').attr('name', '"+id+"');\n");
        
        if (getSubmit() || getAjaxSubmit()) {
            sb.append("$('#"+id+"').addClass('oaSubmit');\n");
        }
        
        if (bAjaxSubmit) {
            sb.append("$('#"+id+"').click(function() {$('#oacommand').val('"+id+"');ajaxSubmit();return false;});\n");
        }
        else if (getSubmit()) {
            sb.append("$('#"+id+"').click(function() { $('#oacommand').val('"+id+"'); $('form').submit(); return false;});\n");
        }
        sb.append(getAjaxScript());
        String js = sb.toString();
        return js;
    }

    @Override
    public String getVerifyScript() {
        return null;
    }

    @Override
    public String getAjaxScript() {
        StringBuilder sb = new StringBuilder(1024);
        if (getEnabled()) sb.append("$('#"+id+"').removeAttr('disabled');\n");
        else sb.append("$('#"+id+"').attr('disabled', 'disabled');\n");
        if (getVisible()) sb.append("$('#"+id+"').show();");
        else sb.append("$('#"+id+"').hide();");
        // sb.append("\n");

        String js = sb.toString();
        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;
        return js;
    }


    @Override
    public void setEnabled(boolean b) {
        this.bEnabled = b;
    }
    @Override
    public boolean getEnabled() {
        return bEnabled && (hub == null || hub.getAO() != null);
    }


    @Override
    public void setVisible(boolean b) {
        this.bVisible = b;
    }
    @Override
    public boolean getVisible() {
        return this.bVisible;
    }

    
}
