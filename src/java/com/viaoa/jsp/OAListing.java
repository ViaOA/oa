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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
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
 * Used with an HTML <UL> or <OL> to replace the <LI> with hub objects.
 * 
 * @author vvia
 */
public class OAListing implements OAJspComponent {
    private static final long serialVersionUID = 1L;

    private Hub hub;
    private String id;
    private OAForm form;
    private boolean bEnabled = true;
    private boolean bVisible = true;
    private boolean bAjaxSubmit=true;
    private boolean bSubmit=false;
    private String forwardUrl;

//qqqqqqq these are not all finished    
    protected String format;
    protected int lineWidth, maxRows, minLineWidth;
    
    protected String propertyPath;
    protected String visiblePropertyPath;
    
    public OAListing(String id, Hub hub, String propertyPath) {
        this.id = id;
        this.hub = hub;
        setPropertyPath(propertyPath);
    }
    
    public Hub getHub() {
        return hub;
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

    public String getPropertyPath() {
        return propertyPath;
    }
    public void setPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
    }
    
    
    @Override
    public void reset() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setForm(OAForm form) {
        this.form = form;
    }
    @Override
    public OAForm getForm() {
        return this.form;
    }

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
    
    
    @Override
    public boolean _beforeSubmit() {
        return true;
    }

    private String submitUpdateScript;
    
    private boolean bWasSubmitted;

    @Override
    public boolean _onSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {
        bWasSubmitted = _myOnSubmit(req, resp, hmNameValue);
        return bWasSubmitted;
    }
    
    protected boolean _myOnSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String,String[]> hmNameValue) {
        // Enumeration enumx = req.getParameterNames();

        String name = null;
        OAObject obj = null;
        String[] values = null;

        for (Map.Entry<String, String[]> ex : hmNameValue.entrySet()) {
            name = (String) ex.getKey();
            if (!name.equalsIgnoreCase("oalisting"+id)) continue;
            values = ex.getValue();
            break;
        }

        if (values == null || values.length == 0 || OAString.isEmpty(values[0])) {
            return false;        
        }

        int row = OAConv.toInt(values[0]);
        hub.setPos(row);
        
        submitUpdateScript = "$('#oalisting"+id+"').val('');";
        submitUpdateScript += "$('#"+id+" li').removeClass('oaSelected');";

        if (hub.getPos() >= 0) {
            String s = " li:nth-child("+(hub.getPos()+1)+")";
            submitUpdateScript += "$('#"+id+s+"').addClass('oaSelected');";
        }

        return true; // true if this caused the form submit
    }

    @Override
    public String _afterSubmit(String forwardUrl) {
        if (bWasSubmitted) {
            if (this.forwardUrl != null) forwardUrl = this.forwardUrl;
            return onSubmit(forwardUrl); 
        }
        return forwardUrl;
    }

    @Override
    public String onSubmit(String forwardUrl) {
        return forwardUrl;
    }

    public void setAjaxSubmit(boolean b) {
        bAjaxSubmit = b;
        if (b) setSubmit(false);
    }
    public boolean getAjaxSubmit() {
        return bAjaxSubmit;
    }
    public void setSubmit(boolean b) {
        if (b) setAjaxSubmit(false);
        bSubmit = b;
    }
    public boolean getSubmit() {
        return bSubmit;
    }
    
    @Override
    public String getScript() {
        lastAjaxSent = null;
        submitUpdateScript = null;
        StringBuilder sb = new StringBuilder(1024);
        sb.append("$('form').prepend(\"<input id='oalisting"+id+"' type='hidden' name='oalisting"+id+"' value=''>\");\n");
        
        sb.append(getAjaxScript());
    
        String js = sb.toString();
        return js;
    }

    @Override
    public String getVerifyScript() {
        // TODO Auto-generated method stub
        return null;
    }

    private String lastAjaxSent;
    
    @Override
    public String getAjaxScript() {

        if (submitUpdateScript != null) {
            String s = submitUpdateScript;
            submitUpdateScript = null;
            return s;
        }
        StringBuilder sb = new StringBuilder(2048);
        
        
        for (int pos=0; ;pos++) {
            Object obj = hub.getAt(pos);
            if (obj == null) break;

            sb.append("<li");
            if (obj == hub.getAO()) sb.append(" class='oaSelected'");
            sb.append(" oarow='"+(pos)+"'>");
            String s = getHtml(obj, pos);
            if (s != null) sb.append(s);
            sb.append("</li>");
        }

        String strListing = sb.toString();
        strListing = Util.convert(strListing, "\\", "\\\\");
        strListing = Util.convert(strListing, "'", "\\'");
        
        
        sb = new StringBuilder(strListing.length() + 2048);
        sb.append("$('#"+id+"').addClass('oaListing');\n");
        sb.append("$('#"+id+"').html('"+strListing+"');\n");
        

        sb.append("function oaListing"+id+"Click() {\n");
        sb.append("    var v = $(this).attr('oarow');\n");
        sb.append("    if (v == null) return;\n");
        sb.append("    $('#oalisting"+id+"').val(v);\n");
        if (getAjaxSubmit() && OAString.isEmpty(forwardUrl)) {
            sb.append("    ajaxSubmit();\n");
        }
        else {
            sb.append("    $('form').submit();\n");
        }
        sb.append("}\n");
        
        if (getEnabled()) {
            sb.append("$('#"+id+" li').click(oaListing"+id+"Click);\n");
        }
        sb.append("$('#"+id+"').addClass('oaSubmit');\n");

        sb.append("$('#oalisting"+id+"').val('');"); // set back to blank
        
        String js = sb.toString();
        
        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;
        return js;
    }


    @Override
    public void setEnabled(boolean b) {
        lastAjaxSent = null;  
        this.bEnabled = b;
    }
    @Override
    public boolean getEnabled() {
        return bEnabled && hub != null && hub.getAO() != null;
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

//qqqqqqq add format, length, etc
    
    public String getHtml(Object obj, int pos) {

        String value = ((OAObject) obj).getPropertyAsString(getPropertyPath(), getFormat());
        
        return value;
    }
}
