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


import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.html.Util;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAConv;
import com.viaoa.util.OADate;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;

/**
 * Used to control any html element: 
 * hide or show
 * html text: min/max line width, max rows to display
 * click event: ajax or form submit 
 * forward URL: to act as a link
 * 
 * @see #setConvertTextToHtml(boolean) set to false if text is already in html
 * @author vvia
 */
public class OAHtmlElement implements OAJspComponent {
    private static final long serialVersionUID = 1L;

    protected Hub hub;
    protected String id;
    protected String visiblePropertyPath;
    protected String htmlPropertyPath;
    
    protected OAForm form;
    protected boolean bVisible = true;
    protected boolean bSubmit;
    protected boolean bAjaxSubmit;
    protected String forwardUrl;
    protected ArrayList<OAHtmlAttribute> alAttribute; 
    protected String lastAjaxSent;
    private boolean bIsPlainText;  // true if the text is not HTML
    
    public void addAttribute(OAHtmlAttribute attr) {
        if (attr == null) return;
        if (alAttribute == null) alAttribute = new ArrayList<OAHtmlAttribute>();
        alAttribute.add(attr);
    }
    
    // used when setting html text
    protected String format;
    protected int lineWidth, maxRows, minLineWidth; // in characters
   

    public OAHtmlElement() {
    }
    
    public OAHtmlElement(String id) {
        this.id = id;
    }
    public OAHtmlElement(String id, Hub hub) {
        this.id = id;
        this.hub = hub;
    }
    public OAHtmlElement(String id, Hub hub, String propertyPath, int width) {
        this.id = id;
        this.hub = hub;
        setHtmlPropertyPath(propertyPath);
        setLineWidth(width);
        setMinLineWidth(width-3);
        setMaxRows(0);
    }
    public OAHtmlElement(String id, Hub hub, String propertyPath) {
        this.id = id;
        this.hub = hub;
        setHtmlPropertyPath(propertyPath);
        setLineWidth(0);
        setMinLineWidth(0);
        setMaxRows(0);
    }
    public OAHtmlElement(String id, Hub hub, String propertyPath, int width, int minWidth, int maxRows) {
        this.id = id;
        this.hub = hub;
        setHtmlPropertyPath(propertyPath);
        setLineWidth(width);
        setMinLineWidth(minWidth);
        setMaxRows(maxRows);
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

    @Override
    public void reset() {
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

    public void setAjaxSubmit(boolean b) {
        bAjaxSubmit = b;
        if (b) setSubmit(false);
    }
    public boolean getAjaxSubmit() {
        return bAjaxSubmit;
    }

    public void setSubmit(boolean b) {
        bSubmit = b;
        if (b) setAjaxSubmit(false);
    }
    public boolean getSubmit() {
        return bSubmit;
    }

    protected boolean bWasSubmitted;
    @Override
    public boolean _onSubmit(HttpServletRequest req, HttpServletResponse resp) {
        String s = req.getParameter("oacommand");
        bWasSubmitted  = (id != null && id.equals(s));
        return bWasSubmitted; // true if this caused the form submit
    }

    @Override
    public String _afterSubmit(String forwardUrl) {
        if (bWasSubmitted) {
            String furl = getForwardUrl();
            if (furl != null) forwardUrl = furl;
            return onSubmit(forwardUrl); 
        }
        return null;
    }

    public String onSubmit(String forwardUrl) {
        return forwardUrl;
    }
    
    
    @Override
    public String getScript() {
        lastAjaxSent = null;

        StringBuilder sb = new StringBuilder(1024);
        
        String furl = getForwardUrl();

        if (bSubmit || bAjaxSubmit) {
            if (bAjaxSubmit) {
                sb.append("$('#"+id+"').click(function() {$('#oacommand').val('"+id+"');ajaxSubmit();return false;});\n");
            }
            else {
                sb.append("$('#"+id+"').click(function() { $('#oacommand').val('"+id+"'); $('form').submit(); return false;});\n");
            }
            
            sb.append("$('#"+id+"').addClass('oaSubmit');\n");
        }
        else if (!OAString.isEmpty(furl)) {
            sb.append("$('#"+id+"').click(function() {window.location = 'oaforward.jsp?oaform="+getForm().getId()+"&oacommand="+id+"';return false;});\n");
            //was: sb.append("$('#"+id+"').click(function() {$('#oacommand').val('"+id+"');window.location = '"+furl+"';return false;});\n");
        }
        
        String s = getAjaxScript();
        if (s != null) sb.append(s);
        String js = sb.toString();
        
        return js;
    }

    @Override
    public String getVerifyScript() {
        return null;
    }

    public void setPlainText(boolean b) {
        bIsPlainText = b;
    }
    public boolean isPlainText() {
        return bIsPlainText;
    }
    
    @Override
    public String getAjaxScript() {
        StringBuilder sb = new StringBuilder(1024);
        
        if (getVisible()) sb.append("$('#"+id+"').show();");
        else sb.append("$('#"+id+"').hide();");

        String html = getHtml();
        if (html != null) {
            if (isPlainText()) {  // some html properties dont have < or > in them
            //was: if (html.indexOf("<") < 0 && html.indexOf(">") < 0) {
                if (maxRows == 1) {
                    if (lineWidth > 0) html = OAString.lineBreak(html, lineWidth, " ", 1);
                    html = Util.convert(html, "\r\n", " ");
                    html = Util.convert(html, "\n", " ");
                    html = Util.convert(html, "\r", " ");
                }
                else {
                    if (maxRows > 1) {
                        html = OAString.lineBreak(html, lineWidth, "\n", maxRows);
                    }
                }
                html = Util.convert(html, "\r\n", "<BR>");
                html = Util.convert(html, "\n", "<BR>");
                html = Util.convert(html, "\r", "<BR>");
                html = Util.convert(html, "  ", "&nbsp;&nbsp;");
            }
            else {
                // all html needs to be on one line, since it is output in javascript code
                html = Util.convert(html, "\r\n", " ");
                html = Util.convert(html, "\n", " ");
                html = Util.convert(html, "\r", " ");
            }
            
            html = OAString.convert(html, "\\'", "\'");
            html = OAString.convert(html, "\'", "\\'");
            sb.append("$('#"+id+"').html('"+html+"');\n");
        }
        
        if (alAttribute != null) {
            for (OAHtmlAttribute at : alAttribute) {
                String s = at.getScript(id);
                if (!OAString.isEmpty(s)) sb.append(s+"\n");
            }
        }
        
        String js = sb.toString();
        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;
        
        return js;
    }

    protected String html;
    public void setHtml(String html) {
        this.html = html;
    }
    protected String getHtml() {
        if (hub == null || getHtmlPropertyPath() == null) return this.html;
        
        Object obj = hub.getAO();
        if (obj != null) {
            if (!(obj instanceof OAObject)) return OAConv.toString(obj, getFormat());
        }
        String value = getHtml((OAObject)obj);
        
        return value;
    }    

    public String getHtml(OAObject obj) {
        String value = null;
        
        if (obj != null) value = obj.getPropertyAsString(htmlPropertyPath, getFormat());
        if (value == null) value = "";
 
        int addSp = (minLineWidth <= 0) ? 0 : (minLineWidth - value.length()); 
        if (addSp > 0) {
            for (int i=0; i<addSp; i++) value += " ";
        }
        
        return value;
    }
    
    public String getVisiblePropertyPath() {
        return visiblePropertyPath;
    }
    public void setVisiblePropertyPath(String visiblePropertyPath) {
        this.visiblePropertyPath = visiblePropertyPath;
    }
    public String getHtmlPropertyPath() {
        return htmlPropertyPath;
    }
    public void setHtmlPropertyPath(String htmlPropertyPath) {
        this.htmlPropertyPath = htmlPropertyPath;
    }
    
    public void setHtml(String htmlPropertyPath, String format, int lineWidth, int minLineWidth, int maxRows) {
        setHtmlPropertyPath(htmlPropertyPath);
        if (!OAString.isEmpty(format)) setFormat(format);
        setLineWidth(lineWidth);
        setMinLineWidth(minLineWidth);
        setMaxRows(maxRows);
    }
    
    private boolean bDefaultFormat=true;
    /**
        Returns format to use for displaying value as a String.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
    public String getFormat() {
        if (format == null && bDefaultFormat && !OAString.isEmpty(htmlPropertyPath) && hub != null) {
            bDefaultFormat = false;
            OAPropertyPath pp = new OAPropertyPath(hub.getObjectClass(), htmlPropertyPath);
            if (pp != null) format = pp.getFormat();
        }
        return format;
    }
    public void setFormat(String format) {
        this.format = format;
        bDefaultFormat = false;
    }
    public int getMinLineWidth() {
        return minLineWidth;
    }
    public void setMinLineWidth(int minLineWidth) {
        this.minLineWidth = minLineWidth;
    }
    public int getLineWidth() {
        return lineWidth;
    }
    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }
    public void setMaxRows(int rows) {
        this.maxRows = rows;
    }
    public int getMaxRows() {
        return this.maxRows;
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
        this.bVisible = b;
    }
    @Override
    public boolean getVisible() {
        if (!bVisible) return false;
        if (OAString.isEmpty(visiblePropertyPath)) return bVisible;

        if (hub == null) return false;
        
        OAObject obj = (OAObject) hub.getAO();
        if (obj == null) return false;
        
        Object value = obj.getPropertyAsString(visiblePropertyPath);
        boolean b = OAConv.toBoolean(value);
        return b;
    }

}
