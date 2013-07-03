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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.annotation.OACalculatedProperty;
import com.viaoa.annotation.OAProperty;
import com.viaoa.html.Util;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDetailDelegate;
import com.viaoa.hub.HubLinkDelegate;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectKeyDelegate;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAConverter;
import com.viaoa.util.OADate;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAReflect;
import com.viaoa.util.OAString;

/**
 * Controls html select+options
 *
 * bind to hub, property
 * set column width
 * show/hide, that can be bound to property
 * enabled, that can be bound to property
 * ajax submit on change
 * handle required validation
 * recursive, displayed using indentation
 * option to set the null description
 * 
 * @author vvia
 *
 */
public class OACombo implements OAJspComponent, OATableEditor {
    private static final long serialVersionUID = 1L;

    private Hub hub;

    protected Hub topHub;
    protected OALinkInfo recursiveLinkInfo;
    protected Hub hubSelect;  // used by OAList
    
    protected String id;
    protected int columns;
    protected String propertyPath;
    protected String visiblePropertyPath;
    protected String enablePropertyPath;
    private OAForm form;
    private boolean bEnabled = true;
    private boolean bVisible = true;
    private boolean bAjaxSubmit, bSubmit;
    protected String nullDescription = "";
    private boolean required;
    private String name;
    private boolean bFocus;
    protected String forwardUrl;

    
    
    public OACombo(String id, Hub hub, String propertyPath, int columns) {
        this.id = id;
        this.hub = hub;
        this.propertyPath = propertyPath;
        this.columns = columns;
    }
    
    @Override
    public boolean isChanged() {
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    public Hub getHub() {
        return hub;
    }
    
    @Override
    public void reset() {
        lastAjaxSent = null;
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

    private boolean bWasSubmitted;
    @Override
    public boolean _onSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {
        String name = null;
        OAObject obj = null;
        String[] values = null;

        String s = req.getParameter("oacommand");
        if (s == null && hmNameValue != null) {
            String[] ss = hmNameValue.get("oacommand");
            if (ss != null && ss.length > 0) s = ss[0];
        }
        bWasSubmitted  = (id != null && id.equals(s));

        OAObject objLinkTo = null;
        for (Map.Entry<String, String[]> ex : hmNameValue.entrySet()) {
            name = (String) ex.getKey();
            if (!name.toUpperCase().startsWith(id.toUpperCase())) continue;
        
            if (!name.toUpperCase().startsWith(id.toUpperCase())) continue;

            values = ex.getValue();
            if (values == null) continue;
            
            if (name.equalsIgnoreCase(id)) {  // no link to hub
                break;
            }
            if (!name.toUpperCase().startsWith(id.toUpperCase()+"_")) continue;
            Hub hubLink = hub.getLinkHub();
            if (hubLink == null) continue;
            
            if (name.toUpperCase().startsWith(id.toUpperCase()+"_")) {
                s = name.substring(id.length()+1);
                if (s.startsWith("guid.")) {
                    s = s.substring(5);
                    OAObjectKey k = new OAObjectKey(null, OAConv.toInt(s), true);
                    objLinkTo = OAObjectCacheDelegate.get(hubLink.getObjectClass(), k);
                }
                else {
                    objLinkTo = OAObjectCacheDelegate.get(hubLink.getObjectClass(), s);
                }
                break;
            }
        }

        ArrayList alSelected = new ArrayList();
        Object objSelected = null;
        for (int i=0; values != null && i < values.length; i++) {
            String value = values[i];
            // now get selected object
            if ("oanull".equals(value)) objSelected = null;
            else {
                if (value.startsWith("pos.")) {
                    int pos = OAConv.toInt(value.substring(4));
                    objSelected = hub.getAt(pos);
                }
                else if (hub.isOAObject()) {
                    if (value.startsWith("guid.")) {
                        value = value.substring(5);
                        OAObjectKey k = new OAObjectKey(null, OAConv.toInt(value), true);
                        objSelected = OAObjectCacheDelegate.get(hub.getObjectClass(), k);
                    }
                    else objSelected = OAObjectCacheDelegate.get(hub.getObjectClass(), value);
                }
                alSelected.add(objSelected);
            }
        }

        if (hubSelect != null) {
            for (Object objx : alSelected) {
                if (!hubSelect.contains(objx)) hubSelect.add(objx);
            }
            for (Object objx : hubSelect) {
                if (!alSelected.contains(objx)) hubSelect.remove(objx);
            }
        }
        else if (objLinkTo != null) {
            if (hub != null && lastActiveObject != objSelected) {
                String linkProp = HubLinkDelegate.getLinkToProperty(hub);
                if (HubLinkDelegate.getLinkedOnPos(hub)) {
                    objLinkTo.setProperty(linkProp, hub.getPos(objSelected));
                }
                else {
                    String linkFromProp = HubLinkDelegate.getLinkFromProperty(hub);
                    if (linkFromProp != null) {
                        if (objSelected instanceof OAObject) {
                            objSelected = ((OAObject)objSelected).getProperty(linkFromProp);
                        }
                    }
                    objLinkTo.setProperty(linkProp, objSelected);
                }
            }
        }
        else {
            if (hub != null && (bWasSubmitted || lastActiveObject != objSelected)) {
                hub.setAO(objSelected);
            }
        }
        return bWasSubmitted; // true if this caused the form submit
    }

    @Override
    public String _afterSubmit(String forwardUrl) {
        if (bWasSubmitted) {
            String furl = getForwardUrl();
            if (furl != null) forwardUrl = furl;
            return onSubmit(forwardUrl); 
        }
        return forwardUrl;
    }

    public void setForwardUrl(String forwardUrl) {
        this.forwardUrl = forwardUrl;
    }
    public String getForwardUrl() {
        return this.forwardUrl;
    }
    
    @Override
    public String onSubmit(String forwardUrl) {
        return forwardUrl;
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

    private String lastAjaxSent;
    @Override
    public String getScript() {
        lastAjaxSent = null;
        StringBuilder sb = new StringBuilder(1024);
        sb.append(getAjaxScript());
        // sb.append("$(\"<span class='error'></span>\").insertAfter('#"+id+"');\n");
        
        if ( (bAjaxSubmit || HubDetailDelegate.hasDetailHubs(hub)) && OAString.isEmpty(getForwardUrl()) ) {
            sb.append("$('#"+id+"').on('change', function() {$('#oacommand').val('"+id+"');ajaxSubmit();return false;});\n");
        }
        else if (getSubmit() || !OAString.isEmpty(getForwardUrl())) {
            sb.append("$('#"+id+"').change(function() { $('#oacommand').val('"+id+"'); $('form').submit(); return false;});\n");
        }
        
        if (getSubmit() || getAjaxSubmit() || HubDetailDelegate.hasDetailHubs(hub)) {
            sb.append("$('#"+id+"').addClass('oaSubmit');\n");
        }
        
        if (isRequired()) {
            sb.append("$('#"+id+"').addClass('oaRequired');\n");
        }
        sb.append("$('#"+id+"').blur(function() {$(this).removeClass('oaError');}); \n");
        
        String js = sb.toString();
        return js;
    }

    @Override
    public String getVerifyScript() {
        if (!isRequired()) return null;
        
        // see: OAForm.getInitScript for using "requires[]" and "errors[]"
        return ("if ($('#"+id+"').val() == 'oanull') { requires.push('"+(name!=null?name:id)+"'); $('#"+id+"').addClass('oaError');}");
        // was: return ("if ($('#"+id+"').val() == '') { oaShowError('"+(name!=null?name:id)+" is required'); $('#"+id+"').addClass('oaError');return false;}");
    }
    public boolean isRequired() {
        return required;
    }
    public void setRequired(boolean required) {
        this.required = required;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    
    private Object lastActiveObject;
    
    @Override
    public String getAjaxScript() {
        StringBuilder sb = new StringBuilder(1024);

        if (hub != null) lastActiveObject = hub.getAO();
        
        //todo: qqqqq could be link on pos, link on property
        // todo: create script to only send change of selection
        
        String ids = id;
        Hub hubLink = hub.getLinkHub();
        if (hubLink != null) {
            Object objLink = hubLink.getAO();
            if (objLink != null) {
                OAObjectKey key = OAObjectKeyDelegate.getKey((OAObject)objLink);
                Object[] objs = key.getObjectIds();
                if (objs != null && objs.length > 0 && objs[0] != null) {
                    ids += "_" + objs[0];
                }
                else {
                    ids += "_guid."+key.getGuid();
                }
            }
        }

        // id + "_" + linkToObjectKey
        sb.append("$('#"+id+"').attr('name', '"+ids+"');\n");
        
        String options = "";
        
        if (recursiveLinkInfo != null) options = getOptions(topHub, 0);
        else options = getOptions(hub, 0);

        String value = nullDescription;
        if (value == null) {
            if (options.length() == 0) value = "";
        }
        if (value != null) {
            if (options.length() == 0) {
                for (int i=value.length(); i<columns; i++) value += " ";
                value = Util.convert(value, " ", "&nbsp;");
            }
            boolean b;
            if (hubSelect != null) b = hubSelect.getSize() == 0;
            else b = hub.getAO() == null;
            
            String sel = b ? "selected='selected'" : "";
            options += "<option value='oanull' "+sel+">"+value+"</option>";
        }        
        sb.append("$('#"+id+"').empty();\n");
        sb.append("$('#"+id+"').append(\"" + options + "\");\n");
        
        if (getEnabled()) sb.append("$('#"+id+"').removeAttr('disabled');\n");
        else sb.append("$('#"+id+"').attr('disabled', 'disabled');\n");
        if (bVisible) sb.append("$('#"+id+"').show();\n");
        else sb.append("$('#"+id+"').hide();\n");
        
        if (bFocus) {
            sb.append("$('#"+id+"').focus();\n");
            bFocus = false;
        }

        String js = sb.toString();
        
        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;
        
        return js;
    }

    
    /** 
     * this is called to render each option.
     * @param option is the string formatted value of object 
    */
    protected String getOption(int pos, Object object, String option) {
        return option;
    }
    
    protected String format;
    private boolean bDefaultFormat=true;
    
    public void setFormat(String fmt) {
        this.format = fmt;
        bDefaultFormat = false;
    }
    public String getFormat() {
        if (format == null && bDefaultFormat && hub != null) {
            bDefaultFormat = false;
            OAPropertyPath pp = new OAPropertyPath(hub.getObjectClass(), propertyPath);
            if (pp != null) format = pp.getFormat();
        }
        return format;
    }
    
    protected String getOptions(Hub hubx, int indent) {
        if (hubx == null) {
            return "";
        }
        String options = "";
        for (int i=0; ;i++) {
            Object obj = hubx.getAt(i);
            if (obj == null) break;

            String value = null;
            if (obj instanceof OAObject) {
                value = ((OAObject) obj).getPropertyAsString(propertyPath, getFormat());
            }
            else {
                value = OAConv.toString(obj, getFormat());
            }
            if (value == null) value = "";
            
            value = getOption(i, obj, value);

            value = Util.convert(value, "\r\n", " ");
            value = Util.convert(value, "\n", " ");
            value = Util.convert(value, "\r", " ");

            if (columns > 0) {
                value = OAString.lineBreak(value, columns, " ", 1);
            }
            
            //value = com.viaoa.html.Util.toEscapeString(value);
            if (i == 0) {
                int addSp = (columns <= 0) ? 0 : (columns - value.length());
                for (int j=0; j<addSp; j++) value += " ";
                value = Util.convert(value, " ", "&nbsp;");
            }

            String v = null; 
            if (obj instanceof OAObject) {
                OAObjectKey key = OAObjectKeyDelegate.getKey((OAObject) obj);
                Object[] objs = key.getObjectIds();
                if (objs != null && objs.length > 0 && objs[0] != null) {
                    v = "" + objs[0];
                }
                else {
                    v = "pos."+i;
                }
            }
            else {
                v = "pos."+i;
            }
            
            boolean b;
            if (hubSelect != null) {
                b = hubSelect.contains(obj);
            }
            else if (recursiveLinkInfo != null) {
                b = (hub.getAO() == obj);
            }
            else {
                b = (hub.getAO() == obj || hub.getPos() == i);
            }
            
            
            String sel = (b) ? "selected='selected'" : "";

            if (indent > 0) {
                String s = ("&nbsp;&nbsp;&nbsp;");
                for (int j=0; j<indent-1; j++) s += ("&nbsp;&nbsp;&nbsp;");
                s += ("--&nbsp;");
                value = s + value;
            }
            
            options += "<option value='"+v+"' "+sel+">"+value+"</option>";
            
            
            if (recursiveLinkInfo != null) {
                Hub h = (Hub) recursiveLinkInfo.getValue(obj);
                if (h != null) {
                    options += getOptions(h, indent+1); 
                }
            }
            
        }
        return options;
    }
    
    public String getNullDescription() {
        return nullDescription;
    }
    public void setNullDescription(String s) {
        nullDescription = s;
    }

    @Override
    public void setEnabled(boolean b) {
        this.bEnabled = b;
    }
    @Override
    public boolean getEnabled() {
        if (!bEnabled) return false;
        if (hub == null) return bEnabled;

        if (!hub.isValid()) return false;
        
        if (OAString.isEmpty(enablePropertyPath)) return bEnabled;
        
        OAObject obj = (OAObject) hub.getAO();
        if (obj == null) return bEnabled;
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
    public boolean setRecursive(boolean b) {
        this.topHub = null;
        this.recursiveLinkInfo = null;
        if (b) {
            recursiveLinkInfo = OAObjectInfoDelegate.getRecursiveLinkInfo(OAObjectInfoDelegate.getObjectInfo(hub.getObjectClass()), OALinkInfo.MANY);
            if (recursiveLinkInfo == null) {
                return false;
            }
            this.topHub = hub.getRootHub();
            if (topHub == null) {
                this.recursiveLinkInfo = null;
                return false;
            }
        }
        return true;
    }
    public boolean getRecursive() {
        return (recursiveLinkInfo != null);
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
        String s = "<select id='"+id+"' style='position:absolute; top:0px; left:1px; width:97%;'></select>";
        return s;
    }
    
}
