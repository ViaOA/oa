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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectKeyDelegate;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAString;

/**
 * Controls an html input type=checkbox
 * 
 * bind to hub, property
 * show/hide, that can be bound to property
 * enabled, that can be bound to property
 * ajax submit on change
 * 
 * @author vvia
 */
public class OACheckBox implements OAJspComponent, OATableEditor, OAJspRequirementsInterface {
    private static final long serialVersionUID = 1L;

    protected Hub<?> hub;
    protected String id;
    protected String propertyPath;
    protected String visiblePropertyPath;
    protected String enablePropertyPath;
    protected OAForm form;
    protected boolean bEnabled = true;
    protected boolean bVisible = true;
    protected boolean bAjaxSubmit;
    protected boolean bSubmit;

    private Object onValue=true, offValue=false;
    private boolean bChecked;
    private boolean bLastChecked;
    protected String groupName;
    private boolean bFocus;
    protected String forwardUrl;
    
    protected String toolTip;
    protected OATemplate templateToolTip;
    private boolean bHadToolTip;
    
    
    public OACheckBox(String id, Hub hub, String propertyPath) {
        this.id = groupName = id;
        this.hub = hub;
        setPropertyPath(propertyPath);
    }
    
    public OACheckBox(String id) {
        this.id = groupName = id;
    }
    
    public void setOnValue(Object obj) {
        this.onValue = obj;
    }
    public void setOffValue(Object obj) {
        this.offValue = obj;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public String getGroupName() {
        return groupName;
    }
    
    @Override
    public boolean isChanged() {
        return (bChecked == bLastChecked);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void reset() {
        bChecked = bLastChecked;
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
    public boolean _beforeFormSubmitted() {
        return true;
    }

    @Override
    public boolean _onFormSubmitted(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {
        String s = req.getParameter("oacommand");
        if (s == null && hmNameValue != null) {
            String[] ss = hmNameValue.get("oacommand");
            if (ss != null && ss.length > 0) s = ss[0];
        }
        boolean bWasSubmitted  = (id != null && id.equals(s));
        
        String name = null;
        OAObject obj = null;
        bChecked = false;
        
        if (hmNameValue != null) {
            for (Map.Entry<String, String[]> ex : hmNameValue.entrySet()) {
                name = (String) ex.getKey();
                if (!name.equals(getGroupName())) continue; 
                String[] values = ex.getValue();
                if (values == null) continue;
    
                boolean b = false;
                for (String sx: values) {
                    if (sx.toUpperCase().startsWith(id.toUpperCase())) {
                        b = true;
                        name = sx;
                        break;
                    }
                }
                if (!b) continue;
                
                if (name.equalsIgnoreCase(id)) {
                    bChecked = true;
                    if (hub != null) { 
                        obj = (OAObject) hub.getAO();
                    }
                    break;
                }
                else {
                    if (name.toUpperCase().startsWith(id.toUpperCase()+"_")) {
                        s = name.substring(id.length()+1);
                        
                        if (s.startsWith("guid.")) {
                            s = s.substring(5);
                            OAObjectKey k = new OAObjectKey(null, OAConv.toInt(s), true);
                            obj = OAObjectCacheDelegate.get(hub.getObjectClass(), k);
                        }
                        else {
                            obj = OAObjectCacheDelegate.get(hub.getObjectClass(), s);
                        }
                        bChecked = true;
                        lastAjaxSent = null;  
                        break;
                    }
                }
            }
        }
        if (hub != null) {
            if (obj != null) updateProperty(obj, bChecked);
        }
        return bWasSubmitted;
    }
    
    
    protected void updateProperty(OAObject obj, boolean bSelected) {
        if (obj != null) obj.setProperty(propertyPath, bChecked?onValue:offValue);
    }

    public boolean isChecked() {
        return bChecked;
    }
    public void setChecked(boolean b) {
        lastAjaxSent = null;  
        this.bChecked = b;
    }
    
    @Override
    public String _afterFormSubmitted(String forwardUrl) {
        return forwardUrl;
    }

    @Override
    public String _onSubmit(String forwardUrl) {
        return onSubmit(forwardUrl);
    }
    
    @Override
    public String onSubmit(String forwardUrl) {
        return forwardUrl;
    }

    public void setForwardUrl(String forwardUrl) {
        this.forwardUrl = forwardUrl;
    }
    public String getForwardUrl() {
        return this.forwardUrl;
    }
    
    public void setAjaxSubmit(boolean b) {
        bAjaxSubmit = b;
    }
    public boolean getAjaxSubmit() {
        return bAjaxSubmit;
    }
    public void setSubmit(boolean b) {
        bSubmit = b;
    }
    public boolean getSubmit() {
        return bSubmit;
    }
    
    @Override
    public String getScript() {
        lastAjaxSent = null;
        bHadToolTip = false;
        StringBuilder sb = new StringBuilder(1024);
        String s = getAjaxScript();
        if (s != null) sb.append(s);
        // sb.append("$(\"<span class='error'></span>\").insertAfter('#"+id+"');\n");
        if (bAjaxSubmit) {
            sb.append("$('#"+id+"').change(function() {$('#oacommand').val('"+id+"'); ajaxSubmit(); return false;});\n");
        }
        else if (getSubmit()) {
            sb.append("$('#"+id+"').click(function() { $('#oacommand').val('"+id+"'); $('form').submit(); return false;});\n");
        }
        if (getSubmit() || getAjaxSubmit()) {
            sb.append("$('#"+id+"').addClass('oaSubmit');\n");
        }
        if (bFocus) {
            sb.append("$('#"+id+"').focus();\n");
            bFocus = false;
        }

        // sb.append("$('#"+id+"').on('blur', ajaxSubmit);\n");
        String js = sb.toString();
        return js;
    }

    @Override
    public String getVerifyScript() {
        return null;
    }
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    private String lastAjaxSent;
    
    @Override
    public String getAjaxScript() {
        StringBuilder sb = new StringBuilder(1024);
        
        String ids = id;
        boolean bValue = false;
        
        if (hub != null && !OAString.isEmpty(propertyPath)) {
            OAObject obj = (OAObject) hub.getAO();
            if (obj != null) {
                OAObjectKey key = OAObjectKeyDelegate.getKey(obj);
                Object[] objs = key.getObjectIds();
                if (objs != null && objs.length > 0 && objs[0] != null) {
                    ids += "_" + objs[0];
                }
                else {
                    ids += "_guid."+key.getGuid();
                }
            }
            
            if (obj != null) {
                Object objx = obj.getProperty(propertyPath);
                bValue = (onValue == objx || (onValue != null && onValue.equals(objx)));
            }
            else {
                bValue = (onValue == obj || (onValue != null && onValue.equals(obj)));
            }
        }
        else {
            bValue = isChecked();
        }

        sb.append("$('#"+id+"').attr('name', '"+groupName+"');\n");
        sb.append("$('#"+id+"').attr('value', '"+ids+"');\n");

        bLastChecked = bValue;        
        
        if (bValue) sb.append("$('#"+id+"').attr('checked', 'checked');\n");
        else sb.append("$('#"+id+"').removeAttr('checked');\n");
        
        if (getEnabled()) sb.append("$('#"+id+"').removeAttr('disabled');\n");
        else sb.append("$('#"+id+"').attr('disabled', 'disabled');\n");

        if (bVisible) sb.append("$('#"+id+"').show();");
        else sb.append("$('#"+id+"').hide();");

        // tooltip
        String prefix = null;
        String tt = getProcessedToolTip();
        if (OAString.isNotEmpty(tt)) {
            tt = OAString.convertForSingleQuotes(tt);
            if (!bHadToolTip) {
                bHadToolTip = true;
                prefix = "$('#"+id+"').tooltip();\n";
            }
            
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
        if (hub == null) return bEnabled;

        OAObject obj = (OAObject) hub.getAO();
        if (obj == null) return false;
        
        if (OAString.isEmpty(enablePropertyPath)) return bEnabled;
        
        Object value = obj.getPropertyAsString(enablePropertyPath);
        boolean b = OAConv.toBoolean(value);
        return b;
    }

    @Override
    public void setVisible(boolean b) {
        lastAjaxSent = null;  
        this.bVisible = b;
    }
    @Override
    public boolean getVisible() {
        if (!bVisible) return false;
        if (hub == null) return bVisible;
        
        if (OAString.isEmpty(visiblePropertyPath)) return bVisible;
        
        OAObject obj = (OAObject) hub.getAO();
        if (obj == null) return false;
        
        Object value = obj.getPropertyAsString(visiblePropertyPath);
        boolean b = OAConv.toBoolean(value);
        return b;
    }


    public String getPropertyPath() {
        return propertyPath;
    }
    public void setPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
    }
    public String getVisiblePropertyPath() {
        return visiblePropertyPath;
    }
    public void setVisiblePropertyPath(String visiblePropertyPath) {
        this.visiblePropertyPath = visiblePropertyPath;
    }
    public String getEnablePropertyPath() {
        return enablePropertyPath;
    }
    public void setEnablePropertyPath(String enablePropertyPath) {
        this.enablePropertyPath = enablePropertyPath;
    }
    public void setFocus(boolean b) {
        this.bFocus = b;
    }

    @Override
    public String getTableEditorHtml() {
        String s = "<input id='"+id+"' type='checkbox'>";
        return s;
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

    public String[] getRequiredJsNames() {
        ArrayList<String> al = new ArrayList<>();
        al.add(OAJspDelegate.JS_jquery);

        if (OAString.isNotEmpty(getToolTip())) {
            al.add(OAJspDelegate.JS_bootstrap);
        }

        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }

    @Override
    public String[] getRequiredCssNames() {
        ArrayList<String> al = new ArrayList<>();

        if (OAString.isNotEmpty(getToolTip())) {
            al.add(OAJspDelegate.CSS_bootstrap);
        }

        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }

    @Override
    public String getRenderHtml(OAObject obj) {
        boolean bValue;
        if (obj != null && hub != null && !OAString.isEmpty(propertyPath)) {
            Object objx = obj.getProperty(propertyPath);
            bValue = (onValue == objx || (onValue != null && onValue.equals(objx)));
        }
        else {
            bValue = false;
        }
        return bValue ? "true" : "false";
    }
    @Override
    public String getEditorHtml(OAObject obj) {
        return null;
    }
    @Override
    public void _beforeOnSubmit() {
    }
}
