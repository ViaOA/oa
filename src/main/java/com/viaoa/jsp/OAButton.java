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


import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.util.*;


/**
 * Used to control an html button
 * show/hide
 * enable
 * ajax submit option
 * forward url
 * @author vvia
 *
 */
public class OAButton<T extends OAObject> implements OAJspComponent, OAJspRequirementsInterface {
    private static final long serialVersionUID = 1L;

    public static enum CommandType {
        Up, 
        Down, 
        Save, 
        First, 
        Last, 
        Next, 
        Previous, 
        Delete, 
        Remove, 
        New, 
        ClearAO, 
        Return
    }
    
    protected CommandType commandType;
    protected Hub<T> hub;
    protected String id;
    protected OAForm form;
    protected boolean bEnabled = true;
    protected boolean bVisible = true;
    protected boolean bAjaxSubmit;
    protected String forwardUrl;
    protected boolean bSubmit;
    protected boolean bSpinner;
    protected String toolTip;
    protected OATemplate templateToolTip;
    private boolean bHadToolTip;
    protected String confirmMessage;
    protected OATemplate templateConfirmMessage;
    protected String enablePropertyPath;
    protected String visiblePropertyPath;

    protected OAFilter<T> filterEnabled;
    protected OAFilter<T> filterVisible;
    
    public OAButton(String id, Hub<T> hub) {
        this(id, hub, null);
    }
    public OAButton(String id, Hub<T> hub, CommandType ct) {
        this.id = id;
        this.hub = hub;
        this.commandType = ct;
        setSubmit(true);
    }

    public OAButton(Hub<T> hub) {
        this(null, hub, null);
    }
    public OAButton(Hub<T> hub, CommandType ct) {
        this(null, hub, ct);
    }

    public OAButton() {
        setSubmit(true);
    }
    public OAButton(CommandType ct) {
        this.commandType = ct;
        setSubmit(true);
    }
    
    public OAButton(String id, CommandType ct) {
        this.id = id;
        this.commandType = ct;
        setSubmit(true);
    }
    public OAButton(String id) {
        this.id = id;
        setSubmit(true);
    }
    
    public void setCommandType(CommandType ct) {
        this.commandType = ct;
    }
    public CommandType getCommandType() {
        return this.commandType;
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
    @Override
    public void setId(String id) {
        this.id = id;
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
    public boolean _beforeFormSubmitted() {
        return true;
    }

    @Override
    public boolean _onFormSubmitted(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {
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
        if (bWasSubmitted && getSpinner()) {
            lastAjaxSent = null;
        }
        
        return bWasSubmitted; // true if this caused the form submit
    }

    @Override
    public String _afterFormSubmitted(String forwardUrl) {
        return afterFormSubmitted(forwardUrl);
    }
    @Override
    public String afterFormSubmitted(String forwardUrl) {
        return forwardUrl;
    }

    
    @Override
    public void _beforeOnSubmit() {
    }
    
    @Override
    public String _onSubmit(String forwardUrl) {
        return onSubmit(forwardUrl);
    }
    
    @Override
    public String onSubmit(String forwardUrl) {
        if (commandType == null) return forwardUrl;
        
        switch (commandType) {
            case Up:
                if (hub == null) break;
                int pos = hub.getPos();
                if (pos < 1) break;
                hub.move(pos, pos-1);
                break;
            case Down:
                if (hub == null) break;
                pos = hub.getPos();
                hub.move(pos, pos+1);
                break;
            case Save:
                if (hub == null) break;
                T obj = hub.getAO();
                if (obj == null) break;
                obj.save();
                break;
            case First:
                if (hub == null) break;
                hub.setPos(0);
                break;
            case Last:
                if (hub == null) break;
                hub.setPos(hub.getSize()-1);
                break;
            case Next:
                if (hub == null) break;
                hub.setPos(hub.getPos()+1);
                break;
            case Previous:
                if (hub == null) break;
                hub.setPos(hub.getPos()-1);
                break;
            case Delete:
                if (hub == null) break;
                obj = hub.getAO();
                if (obj == null) break;
                obj.delete();
                break;
            case Remove:
                if (hub == null) break;
                obj = hub.getAO();
                if (obj != null) hub.remove(obj);
                break;
            case New:
                if (hub == null) break;
                T objx = (T) OAObjectReflectDelegate.createNewObject(hub.getObjectClass());
                hub.add(objx);
                hub.setAO(objx);
                break;
            case ClearAO:
                if (hub == null) break;
                hub.setAO(null);
                break;
            case Return:
                OAForm form = getForm();
                if (form  == null) break;
                OASession ses = form.getSession();
                if (ses == null) break;
                form = ses.getPreviousForm();
                if (form == null) break;
                forwardUrl = form.getUrl();
                break;
        }
        
        return forwardUrl;
    }
    
    
    
    
    private String lastAjaxSent;
    
    @Override
    public String getScript() {
        lastAjaxSent = null;
        bHadToolTip = false;
        StringBuilder sb = new StringBuilder(1024);
        sb.append("$('#"+id+"').attr('name', '"+id+"');\n");
        
        if (getSubmit() || getAjaxSubmit()) {
            sb.append("$('#"+id+"').addClass('oaSubmit');\n");
        }

        if (getSpinner()) {
            sb.append("$('#"+id+"').addClass('ladda-button');\n");
            sb.append("$('#"+id+"').attr('data-style', 'slide-right');\n");
            sb.append("$('#"+id+"').html(\"<span class='ladda-label'>\"+$('#"+id+"').html()+\"</span>\");\n");
            //see:  file:///C:/Projects/metronic_v4.7.5_copy/theme/admin_1/ui_buttons_spinner.html
        }

        String confirm = getConfirmMessage();
        if (OAString.isNotEmpty(confirm)) {
            confirm = OAJspUtil.createJsString(confirm, '\"');
            confirm = "if (!window.confirm(\""+confirm+"\")) return false;";
        }
        else confirm = "";
        
        if (bAjaxSubmit) {
            if (getSpinner()) {
                sb.append("$('#"+id+"').click(function() {"+confirm+"$('#oacommand').val('"+id+"');Ladda.create(this).start();ajaxSubmit();return false;});\n");
            }
            else {
                sb.append("$('#"+id+"').click(function() {"+confirm+"$('#oacommand').val('"+id+"');ajaxSubmit();return false;});\n");
            }
        }
        else if (getSubmit()) {
            if (getSpinner()) {
                sb.append("$('#"+id+"').click(function() {"+confirm+"$('#oacommand').val('"+id+"');var spinner = Ladda.create(this); spinner.start(); $('form').submit(); $('#oacommand').val(''); if (oaSubmitCancelled) spinner.stop(); return false;});\n");
            }
            else {
                sb.append("$('#"+id+"').click(function() {"+confirm+"$('#oacommand').val('"+id+"'); $('form').submit(); $('#oacommand').val(''); return false;});\n");
            }
        }
        
        sb.append(getAjaxScript(true));
        String js = sb.toString();
        return js;
    }

    @Override
    public String getVerifyScript() { 
        return null;
    }

    @Override
    public String getAjaxScript() {
        return getAjaxScript(false);
    }
    public String getAjaxScript(boolean bInit) {
        StringBuilder sb = new StringBuilder(1024);

        if (getSpinner()) { // before disable
            sb.append("Ladda.create($('#"+id+"')[0]).stop();\n");
        }
        
        if (getEnabled()) sb.append("$('#"+id+"').removeAttr('disabled');\n");
        else sb.append("$('#"+id+"').attr('disabled', 'disabled');\n");
        if (getVisible()) sb.append("$('#"+id+"').show();\n");
        else sb.append("$('#"+id+"').hide();\n");
        
        // tooltip
        String prefix = null;
        String tt = getProcessedToolTip();
        if (OAString.isNotEmpty(tt)) {
            if (!bHadToolTip) {
                bHadToolTip = true;
                prefix = "$('#"+id+"').tooltip();\n";
            }

            tt = OAJspUtil.createJsString(tt, '\'');
            sb.append("$('#"+id+"').data('bs.tooltip').options.title = '"+tt+"';\n");
            sb.append("$('#"+id+"').data('bs.tooltip').options.placement = 'top';\n");
        }
        else {
            if (bHadToolTip) {
                sb.append("$('#"+id+"').tooltip('destroy');\n");
                bHadToolTip = false;
            }
        }
        
        String js = sb.toString();
        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;
        
        if (prefix != null) {
            js = prefix + OAString.notNull(js);
        }
        return js;
    }


    @Override
    public void setEnabled(boolean b) {
        this.bEnabled = b;
    }
    @Override
    public boolean getEnabled() {
        if (!bEnabled) return false;
        if (hub == null) return true;

        if (!hub.isValid()) return false;
        
        T obj = hub.getAO();
        
        if (filterEnabled != null) {
            boolean b = (obj != null) && filterEnabled.isUsed(obj);
            if (!b) return false;
        }

        if (OAString.isEmpty(enablePropertyPath)) return true;
        
        if (obj == null) return false;
        Object value = obj.getProperty(enablePropertyPath);
        boolean b;
        if (value instanceof Hub) {
            b = ((Hub) value).size() > 0;
        }
        else b = OAConv.toBoolean(value);
        return b;
    }
    public String getEnablePropertyPath() {
        return enablePropertyPath;
    }
    public void setEnablePropertyPath(String enablePropertyPath) {
        this.enablePropertyPath = enablePropertyPath;
    }

    
    @Override
    public void setVisible(boolean b) {
        this.bVisible = b;
    }
    @Override
    public boolean getVisible() {
        if (!bVisible) return false;
        if (hub == null) return true;

        T obj = hub.getAO();
        
        if (filterVisible != null) {
            boolean b = (obj != null) && filterVisible.isUsed(obj);
            if (!b) return false;
        }
        
        if (OAString.isEmpty(visiblePropertyPath)) return true;
        if (obj == null) return false;
        
        Object value = obj.getProperty(visiblePropertyPath);
        boolean b;
        if (value instanceof Hub) {
            b = ((Hub) value).size() > 0;
        }
        else b = OAConv.toBoolean(value);
        return b;
    }
    public String getVisiblePropertyPath() {
        return visiblePropertyPath;
    }
    public void setVisiblePropertyPath(String visiblePropertyPath) {
        this.visiblePropertyPath = visiblePropertyPath;
    }
    
    
    /**
     * see: https://github.com/msurguy/ladda-bootstrap
     * @param b
     */
    public void setSpinner(boolean b) {
        this.bSpinner = b;
    }
    public boolean getSpinner() {
        return bSpinner;
    }

    @Override
    public String[] getRequiredCssNames() {
        ArrayList<String> al = new ArrayList<>();
        if (getSpinner()) {
            al.add(OAJspDelegate.CSS_bootstrap_ladda);
        }
        if (OAString.isNotEmpty(getToolTip())) {
            al.add(OAJspDelegate.CSS_bootstrap);
        }

        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }

    @Override
    public String[] getRequiredJsNames() {
        ArrayList<String> al = new ArrayList<>();
        al.add(OAJspDelegate.JS_jquery);
        if (getSpinner()) {
            al.add(OAJspDelegate.JS_bootstrap_spin);
            al.add(OAJspDelegate.JS_bootstrap_ladda);
        }
        if (OAString.isNotEmpty(getToolTip())) {
            al.add(OAJspDelegate.JS_bootstrap);
        }
        
        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }

    public Hub getHub() {
        return hub;
    }
    
    public void setToolTip(String tooltip) {
        this.toolTip = tooltip;
        templateToolTip = null;
    }
    public String getToolTip() {
        return this.toolTip;
    }
    public String getProcessedToolTip() {
        if (OAString.isEmpty(toolTip)) return toolTip;
        if (templateToolTip == null) {
            templateToolTip = new OATemplate();
            templateToolTip.setTemplate(getToolTip());
        }
        OAObject obj = null;
        if (hub != null) {
            Object objx = hub.getAO();
            if (objx instanceof OAObject) obj = (OAObject) objx;
        }
        String s = templateToolTip.process(obj, hub, null);
        return s;
    }
    
    @Override
    public String getRenderHtml(OAObject obj) {
        String txt = getRenderText(obj);
        if (txt == null) txt = "";

        String classz = getRenderClass(obj);
        if (OAString.isEmpty(classz)) classz = "";
        else classz = " class='"+classz+"' ";
        
        String style = getRenderStyle(obj);
        if (OAString.isEmpty(style)) style = "";
        else style = " style='"+style+"' ";
        
        String s = "<button type='button' "+classz+style+getRenderOnClick(obj)+">"+txt+"</button>";
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
        String js = "";
        String s = getProcessedConfirmMessage(obj);
        if (OAString.isNotEmpty(s)) {
            s = OAJspUtil.createEmbeddedJsString(s, '\"');
            js += "if (!window.confirm(\""+s+"\")) return false;";  
        }
        js += "$(\"#oacommand\").val(\""+id+"\");"; 
        
        js = "onClick='" + js + "'";
        
        return js;
    }

    @Override
    public String getEditorHtml(OAObject obj) {
        return getRenderHtml(obj);
    }

    public void setConfirmMessage(String msg) {
        this.confirmMessage = msg;
    }
    public String getConfirmMessage() {
        return confirmMessage;
    }
    public String getProcessedConfirmMessage(OAObject obj) {
        if (OAString.isEmpty(confirmMessage)) return confirmMessage;
        if (templateConfirmMessage == null) {
            templateConfirmMessage = new OATemplate();
            templateConfirmMessage.setTemplate(getConfirmMessage());
        }
        String s = templateConfirmMessage.process(obj);
        return s;
    }

    public void setEnabledFilter(OAFilter<T> filter) {
        this.filterEnabled = filter;
    }
    
    public void setVisibleFilter(OAFilter<T> filter) {
        this.filterVisible = filter;
    }

    
    
}
